package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.Person
import com.example.ui.screens.EditPurchaseFlow
import com.example.ui.screens.EditSplitFlow
import com.example.ui.theme.QuemMeDeveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Editar compra e conta: abre no resumo, troca valor/parcelas/pessoa e salva deslizando. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi", sdk = [36])
class EditFlowTest {
    @get:Rule val rule = createComposeRule()

    private val people = listOf(
        Person(id = 3, name = "Pai", phone = "", colorHex = "#F87171", avatar = "MALE"),
        Person(id = 4, name = "Vó", phone = "", colorHex = "#34D399", avatar = "FEMALE")
    )

    private fun settle() { rule.mainClock.advanceTimeBy(1200); rule.waitForIdle() }

    @Test fun editarCompra() {
        var saved: String? = null
        val item = InstallmentItem(
            id = 10, expenseId = 5, personId = 3, personName = "Pai", expenseTitle = "Feira",
            expenseType = ExpenseType.CARD_PURCHASE, installmentNumber = 2, totalInstallments = 2,
            year = 2026, month = 11, amount = 7500
        )
        rule.setContent {
            QuemMeDeveTheme(darkTheme = true) {
                EditPurchaseFlow(
                    item = item, people = people, onAddPerson = {},
                    onConfirm = { id, title, total, p, n, y, m -> saved = "$id|$title|$total|${p.name}|$n|$y|$m" },
                    onClose = {}
                )
            }
        }
        settle()
        rule.onNodeWithTag("flow_summary").performClick(); settle()
        rule.onNodeWithTag("purchase_installment_3").performClick(); settle()
        rule.onNodeWithTag("edit_next").performClick(); settle()
        rule.onNodeWithTag("edit_person_row").performClick(); settle()
        rule.onNodeWithTag("edit_person_4").performClick(); settle()
        rule.onNodeWithTag("swipe_to_confirm").performTouchInput { swipeRight(durationMillis = 400) }; settle()
        // total 2 × 75,00; primeira parcela um mês antes da 2ª (outubro)
        assertEquals("5|Feira|150.0|Vó|3|2026|10", saved)
    }

    @Test fun editarConta() {
        var saved: String? = null
        val item = InstallmentItem(
            id = 20, expenseId = 7, personId = 3, personName = "Pai", expenseTitle = "Internet",
            expenseType = ExpenseType.SHARED_BILL, installmentNumber = 1, totalInstallments = 12,
            year = 2026, month = 11, amount = 6000
        )
        val exp = Expense(
            id = 7, title = "Internet", totalAmount = 12000, type = ExpenseType.SHARED_BILL, totalInstallments = 12,
            isRecurring = true, startYear = 2026, startMonth = 11, personId = 3, myShareAmount = 4000, otherShareAmount = 8000
        )
        rule.setContent {
            QuemMeDeveTheme(darkTheme = true) {
                EditSplitFlow(
                    item = item, people = people, selectedYear = 2026, selectedMonth = 11,
                    onLoadExpense = { exp }, onAddPerson = {},
                    onConfirm = { id, title, total, p, mine, other, all -> saved = "$id|$title|$total|${p.name}|$mine|$other|$all" },
                    onClose = {}
                )
            }
        }
        settle()
        rule.onNodeWithTag("edit_split_all_months").performClick(); settle()
        rule.onNodeWithTag("swipe_to_confirm").performTouchInput { swipeRight(durationMillis = 400) }; settle()
        // carregou a divisão personalizada (40/80) e salvou só para a fatura aberta
        assertEquals("7|Internet|120.0|Pai|40.0|80.0|false", saved)
    }
}
