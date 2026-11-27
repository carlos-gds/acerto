package com.example

import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Person
import com.example.data.repository.FinanceRepository
import com.example.ui.components.GuideStore
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.QuemMeDeveTheme
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Calendar

/** Guia da primeira vez: "Vamos começar?" e as dicas de uma vez só. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi", sdk = [36])
class GuideTest {
    @get:Rule val rule = createComposeRule()
    private val ctx get() = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun settle(ms: Long = 2500) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }

    private fun clear(): FinanceRepository {
        val db = com.example.data.db.AppDatabase.getDatabase(ctx)
        val t = Thread { db.clearAllTables() }; t.start(); t.join()
        return FinanceRepository(db.personDao(), db.expenseDao(), db.installmentDao(), db)
    }

    private fun open() {
        rule.setContent {
            val vm: FinanceViewModel = viewModel()
            QuemMeDeveTheme(darkTheme = false) { HomeScreen(viewModel = vm) }
        }
        rule.waitUntil(30_000) { rule.onAllNodes(hasText("Falta receber")).fetchSemanticsNodes().isNotEmpty() }
        settle()
    }

    private fun has(tag: String) = rule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    @Test fun primeiraVez_mostraOsPassos_eFecharNaoVoltaMais() {
        clear()
        open()
        rule.onNodeWithTag("start_guide").assertExists()
        rule.onNodeWithTag("start_guide_close").performClick(); settle()
        assertFalse(has("start_guide"))
        assertTrue(GuideStore.isStartClosed(ctx))
    }

    @Test fun dicas_umaDeCadaVez_naOrdemCerta() {
        val repo = clear()
        val cal = Calendar.getInstance()
        runBlocking {
            val mae = Person(id = repo.insertPerson("Mãe"), name = "Mãe", phone = "")
            val vo = Person(id = repo.insertPerson("Vó"), name = "Vó", phone = "")
            repo.addCardPurchaseGroup("Geladeira", listOf(mae to 1000.0, vo to 1000.0), 10, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        }
        open()
        rule.waitUntil(15_000) { has("home_card_stack") }
        settle()
        rule.onNodeWithTag("coach_cards").assertExists()
        rule.onNodeWithTag("coach_ok").performClick(); settle()
        rule.onNodeWithTag("coach_swipe_paid").assertExists()
        rule.onNodeWithTag("coach_ok").performClick(); settle()
        assertFalse(has("coach_cards") || has("coach_swipe_paid"))
        rule.onNodeWithTag("bottom_nav_card").performTouchInput { click() }; settle()
        rule.onNodeWithTag("coach_card_open").assertExists()
        rule.onNodeWithTag("coach_ok").performClick(); settle()
        assertTrue(listOf("cards", "swipe_paid", "card_open").all { GuideStore.isTipSeen(ctx, it) })

        // "Ver as dicas de novo" (Ajustes) faz elas voltarem
        GuideStore.resetTips(ctx)
        assertFalse(GuideStore.isTipSeen(ctx, "cards"))
    }
}
