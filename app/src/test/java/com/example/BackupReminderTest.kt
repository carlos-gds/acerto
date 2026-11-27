package com.example

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Person
import com.example.data.repository.FinanceRepository
import com.example.ui.components.BackupReminder
import com.example.ui.components.GuideStore
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.QuemMeDeveTheme
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Calendar

/** Lembrete de backup: quando aparece, o que diz e o "Depois". */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi", sdk = [36])
class BackupReminderTest {
    @get:Rule val rule = createComposeRule()
    private val day = 24L * 60 * 60 * 1000
    private val now = 1_800_000_000_000L

    @Test fun regra() {
        // sem dados, nunca lembra
        assertFalse(BackupReminder.shouldRemind(now, hasData = false, lastAt = 0, dataSince = 0, snoozeUntil = 0))
        // nunca fez backup: só 3 dias depois do primeiro lançamento
        assertFalse(BackupReminder.shouldRemind(now, true, 0, now - 2 * day, 0))
        assertTrue(BackupReminder.shouldRemind(now, true, 0, now - 3 * day, 0))
        // já fez: lembra com 30 dias
        assertFalse(BackupReminder.shouldRemind(now, true, now - 29 * day, now - 90 * day, 0))
        assertTrue(BackupReminder.shouldRemind(now, true, now - 30 * day, now - 90 * day, 0))
        // "Depois" segura o lembrete até a data marcada
        assertFalse(BackupReminder.shouldRemind(now, true, now - 40 * day, 0, now + day))
        assertEquals("nunca", BackupReminder.lastBackupLabel(now, 0))
        assertEquals("hoje", BackupReminder.lastBackupLabel(now, now - 1000))
        assertEquals("ontem", BackupReminder.lastBackupLabel(now, now - day))
        assertEquals("há 12 dias", BackupReminder.lastBackupLabel(now, now - 12 * day))
    }

    @Test fun cartaoNaInicio_eDepoisEsconde() {
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = com.example.data.db.AppDatabase.getDatabase(ctx)
        val t = Thread { db.clearAllTables() }; t.start(); t.join()
        val repo = FinanceRepository(db.personDao(), db.expenseDao(), db.installmentDao(), db)
        val cal = Calendar.getInstance()
        runBlocking {
            val ana = Person(id = repo.insertPerson("Ana"), name = "Ana", phone = "")
            repo.addCardPurchase("Fone", 300.0, ana, 3, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        }
        GuideStore.closeStart(ctx)
        listOf("cards", "swipe_paid", "card_open").forEach { GuideStore.markTipSeen(ctx, it) }
        BackupReminder.markBackedUp(ctx, System.currentTimeMillis() - 40 * day)

        rule.setContent {
            val vm: FinanceViewModel = viewModel()
            QuemMeDeveTheme(darkTheme = false) { HomeScreen(viewModel = vm) }
        }
        rule.waitUntil(30_000) { rule.onAllNodes(hasText("Falta receber")).fetchSemanticsNodes().isNotEmpty() }
        rule.mainClock.advanceTimeBy(2500); rule.waitForIdle()

        rule.onNodeWithTag("backup_reminder").assertExists()
        rule.onNodeWithText("Faz 40 dias sem backup").assertExists()
        rule.onNodeWithTag("backup_reminder_later").performClick()
        rule.mainClock.advanceTimeBy(500); rule.waitForIdle()
        assertTrue(rule.onAllNodes(hasTestTag("backup_reminder")).fetchSemanticsNodes().isEmpty())
        assertTrue(BackupReminder.snoozeUntil(ctx) > System.currentTimeMillis())
    }
}
