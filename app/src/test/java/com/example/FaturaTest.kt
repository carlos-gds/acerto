package com.example

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.Person
import com.example.data.repository.FinanceRepository
import com.example.ui.components.BackupReminder
import com.example.ui.components.GuideStore
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.QuemMeDeveTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.Fatura
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Calendar

/** Fatura do mês: dos outros, recebido e a sua parte quando você informa o total. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi", sdk = [36])
class FaturaTest {
    @get:Rule val rule = createComposeRule()
    private val ctx get() = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun inst(id: Long, month: Int, cents: Long, paid: Boolean, type: ExpenseType = ExpenseType.CARD_PURCHASE, mine: Boolean = false) =
        InstallmentItem(
            id = id, expenseId = id, personId = 1, personName = "Mãe", expenseTitle = "X", expenseType = type,
            installmentNumber = 1, totalInstallments = 1, year = 2026, month = month, amount = cents, isPaid = paid, isMyShare = mine
        )

    @Test fun resumo() {
        val items = listOf(
            inst(1, 11, 20000, paid = true),
            inst(2, 11, 63000, paid = false),
            inst(3, 12, 50000, paid = false),                                       // outro mês
            inst(4, 11, 6000, paid = false, type = ExpenseType.SHARED_BILL),         // conta dividida não é do cartão
            inst(5, 11, 6000, paid = true, type = ExpenseType.SHARED_BILL, mine = true)
        )
        val s = Fatura.summary(items, 2026, 11, totalCents = 123000)
        assertEquals(83000L, s.othersCents)
        assertEquals(20000L, s.receivedCents)
        assertEquals(40000L, s.mineCents)
        assertFalse(s.totalTooLow)
        assertNull(Fatura.summary(items, 2026, 11, null).mineCents)
        assertTrue(Fatura.summary(items, 2026, 11, 50000).totalTooLow)

        Fatura.setTotal(ctx, 2026, 11, 123000)
        assertEquals(123000L, Fatura.getTotal(ctx, 2026, 11))
        Fatura.setTotal(ctx, 2026, 11, null)
        assertNull(Fatura.getTotal(ctx, 2026, 11))
    }

    @Test fun informarOTotalNoCartao() {
        val db = com.example.data.db.AppDatabase.getDatabase(ctx)
        val t = Thread { db.clearAllTables() }; t.start(); t.join()
        val repo = FinanceRepository(db.personDao(), db.expenseDao(), db.installmentDao(), db)
        val cal = Calendar.getInstance()
        runBlocking {
            val mae = Person(id = repo.insertPerson("Mãe"), name = "Mãe", phone = "")
            val vo = Person(id = repo.insertPerson("Vó"), name = "Vó", phone = "")
            repo.addCardPurchaseGroup("Geladeira", listOf(mae to 1000.0, vo to 1000.0), 10, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            repo.addCardPurchase("Fone", 900.0, mae, 3, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        }
        GuideStore.closeStart(ctx)
        listOf("cards", "swipe_paid", "card_open").forEach { GuideStore.markTipSeen(ctx, it) }
        BackupReminder.markBackedUp(ctx)

        rule.setContent {
            val vm: FinanceViewModel = viewModel()
            QuemMeDeveTheme(darkTheme = false) { HomeScreen(viewModel = vm) }
        }
        rule.waitUntil(30_000) { rule.onAllNodes(hasText("Falta receber")).fetchSemanticsNodes().isNotEmpty() }
        rule.mainClock.advanceTimeBy(2500); rule.waitForIdle()
        rule.onNodeWithTag("bottom_nav_card").performTouchInput { click() }
        rule.mainClock.advanceTimeBy(1500); rule.waitForIdle()

        // 2 × R$ 100 (geladeira) + R$ 300 (fone) = R$ 500 dos outros
        rule.onNodeWithTag("fatura_value", useUnmergedTree = true).assertTextContains("500,00", substring = true)
        rule.onNodeWithTag("fatura_tile").performClick()
        rule.mainClock.advanceTimeBy(800); rule.waitForIdle()
        listOf("1", "2", "3", "0", "00").forEach { rule.onNodeWithTag("fatura_key_$it").performClick() }
        rule.onNodeWithTag("fatura_mine").assertTextContains("Sua parte: R$ 730,00", substring = true)
        rule.onNodeWithTag("fatura_next").performClick()
        rule.mainClock.advanceTimeBy(1000); rule.waitForIdle()

        rule.onNodeWithTag("fatura_value", useUnmergedTree = true).assertTextContains("1.230,00", substring = true)
        rule.onNodeWithTag("fatura_split", useUnmergedTree = true).assertTextContains("sua parte", substring = true)
        rule.onNodeWithTag("fatura_split", useUnmergedTree = true).assertTextContains("730,00", substring = true)
        assertEquals(123000L, Fatura.getTotal(ctx, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1))
    }
}
