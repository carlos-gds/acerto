package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.ui.viewmodel.CardPurchaseProgress
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.MonthlySummary
import com.example.ui.viewmodel.PersonMonthlyGroup
import com.example.util.CurrencyFormatter
import com.example.util.DebtMessageFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrivacyModeTest {

    @Test
    fun testCurrencyFormatterPrivacyMasking() {
        val realValue = 50.0
        val masked = CurrencyFormatter.format(realValue, isPrivacyMode = true)
        val unmasked = CurrencyFormatter.format(realValue, isPrivacyMode = false)

        assertEquals("R$ ••••••", masked)
        assertEquals(CurrencyFormatter.MASKED_VALUE, masked)
        assertFalse(masked.contains("50"))
        assertTrue(unmasked.contains("50,00"))
    }

    @Test
    fun testPersonDebtCardAmountsMasking() {
        val group = PersonMonthlyGroup(
            personId = 1L,
            personName = "Pai",
            colorHex = "#3B82F6",
            phone = "11999999999",
            totalAmount = 50.0,
            paidAmount = 0.0,
            pendingAmount = 50.0,
            items = listOf(
                InstallmentItem(
                    id = 1L,
                    expenseId = 10L,
                    personId = 1L,
                    personName = "Pai",
                    expenseTitle = "Farmácia",
                    expenseType = ExpenseType.CARD_PURCHASE,
                    installmentNumber = 1,
                    totalInstallments = 1,
                    year = 2026,
                    month = 5,
                    amount = 5000L, // R$ 50,00 em centavos
                    isPaid = false
                )
            )
        )

        // Privacy mode active (masked)
        val maskedTotal = CurrencyFormatter.format(group.totalAmount, isPrivacyMode = true)
        val maskedFalta = "Falta: ${CurrencyFormatter.format(group.pendingAmount, isPrivacyMode = true)}"
        val maskedItem = CurrencyFormatter.format(group.items.first().amount, isPrivacyMode = true)

        assertEquals("R$ ••••••", maskedTotal)
        assertEquals("Falta: R$ ••••••", maskedFalta)
        assertEquals("R$ ••••••", maskedItem)
        assertFalse(maskedTotal.contains("50"))
        assertFalse(maskedFalta.contains("50"))
        assertFalse(maskedItem.contains("50"))

        // Privacy mode inactive (unmasked)
        val unmaskedTotal = CurrencyFormatter.format(group.totalAmount, isPrivacyMode = false)
        val unmaskedFalta = "Falta: ${CurrencyFormatter.format(group.pendingAmount, isPrivacyMode = false)}"
        val unmaskedItem = CurrencyFormatter.format(group.items.first().amount, isPrivacyMode = false)

        assertTrue(unmaskedTotal.contains("50,00"))
        assertTrue(unmaskedFalta.contains("50,00"))
        assertTrue(unmaskedItem.contains("50,00"))
    }

    @Test
    fun testSummaryCardVocePagaAndOutrosDevemMasking() {
        val summary = MonthlySummary(
            totalInvoice = 1200.0,
            myShare = 450.0,
            othersTotal = 750.0,
            othersPaid = 250.0,
            othersPending = 500.0
        )

        // Privacy mode active
        val maskedTotalInvoice = CurrencyFormatter.format(summary.totalInvoice, isPrivacyMode = true)
        val maskedMyShare = CurrencyFormatter.format(summary.myShare, isPrivacyMode = true)
        val maskedOthersTotal = CurrencyFormatter.format(summary.othersTotal, isPrivacyMode = true)
        val maskedOthersPending = CurrencyFormatter.format(summary.othersPending, isPrivacyMode = true)

        assertEquals("R$ ••••••", maskedTotalInvoice)
        assertEquals("R$ ••••••", maskedMyShare)
        assertEquals("R$ ••••••", maskedOthersTotal)
        assertEquals("R$ ••••••", maskedOthersPending)

        assertFalse(maskedTotalInvoice.contains("1200"))
        assertFalse(maskedMyShare.contains("450"))
        assertFalse(maskedOthersTotal.contains("750"))
        assertFalse(maskedOthersPending.contains("500"))

        // Privacy mode inactive
        val unmaskedTotalInvoice = CurrencyFormatter.format(summary.totalInvoice, isPrivacyMode = false)
        val unmaskedMyShare = CurrencyFormatter.format(summary.myShare, isPrivacyMode = false)
        val unmaskedOthersTotal = CurrencyFormatter.format(summary.othersTotal, isPrivacyMode = false)
        val unmaskedOthersPending = CurrencyFormatter.format(summary.othersPending, isPrivacyMode = false)

        assertTrue(unmaskedTotalInvoice.contains("1.200,00"))
        assertTrue(unmaskedMyShare.contains("450,00"))
        assertTrue(unmaskedOthersTotal.contains("750,00"))
        assertTrue(unmaskedOthersPending.contains("500,00"))
    }

    @Test
    fun testCardPurchasesProgressMasking() {
        val purchaseProgress = CardPurchaseProgress(
            expense = Expense(
                id = 1L,
                title = "Geladeira",
                totalAmount = 240000L, // R$ 2.400,00 em centavos
                type = ExpenseType.CARD_PURCHASE,
                totalInstallments = 10,
                startYear = 2026,
                startMonth = 1,
                personId = 2L
            ),
            personName = "Irmão",
            personColor = "#3B82F6",
            paidInstallmentsCount = 4,
            totalInstallmentsCount = 10,
            remainingInstallmentsCount = 6,
            installmentAmount = 240.0,
            remainingAmount = 1440.0,
            progress = 0.4f
        )

        // Masked
        val maskedTotal = CurrencyFormatter.format(purchaseProgress.expense.totalAmount, isPrivacyMode = true)
        val maskedInstallment = "${purchaseProgress.totalInstallmentsCount}x de ${CurrencyFormatter.format(purchaseProgress.installmentAmount, isPrivacyMode = true)}"
        val maskedRemaining = "Restam ${purchaseProgress.remainingInstallmentsCount} parcelas (${CurrencyFormatter.format(purchaseProgress.remainingAmount, isPrivacyMode = true)})"

        assertEquals("R$ ••••••", maskedTotal)
        assertEquals("10x de R$ ••••••", maskedInstallment)
        assertEquals("Restam 6 parcelas (R$ ••••••)", maskedRemaining)

        assertFalse(maskedTotal.contains("2400"))
        assertFalse(maskedInstallment.contains("240"))
        assertFalse(maskedRemaining.contains("1440"))

        // Unmasked
        val unmaskedTotal = CurrencyFormatter.format(purchaseProgress.expense.totalAmount, isPrivacyMode = false)
        val unmaskedInstallment = "${purchaseProgress.totalInstallmentsCount}x de ${CurrencyFormatter.format(purchaseProgress.installmentAmount, isPrivacyMode = false)}"
        val unmaskedRemaining = "Restam ${purchaseProgress.remainingInstallmentsCount} parcelas (${CurrencyFormatter.format(purchaseProgress.remainingAmount, isPrivacyMode = false)})"

        assertTrue(unmaskedTotal.contains("2.400,00"))
        assertTrue(unmaskedInstallment.contains("240,00"))
        assertTrue(unmaskedRemaining.contains("1.440,00"))
    }

    @Test
    fun testDebtsDetailScreenMetricsMasking() {
        val totalOwed = 950.0
        val totalPending = 650.0
        val totalPaid = 300.0

        // Masked
        val maskedOwed = CurrencyFormatter.format(totalOwed, isPrivacyMode = true)
        val maskedPending = CurrencyFormatter.format(totalPending, isPrivacyMode = true)
        val maskedPaid = CurrencyFormatter.format(totalPaid, isPrivacyMode = true)

        assertEquals("R$ ••••••", maskedOwed)
        assertEquals("R$ ••••••", maskedPending)
        assertEquals("R$ ••••••", maskedPaid)

        // Unmasked
        val unmaskedOwed = CurrencyFormatter.format(totalOwed, isPrivacyMode = false)
        val unmaskedPending = CurrencyFormatter.format(totalPending, isPrivacyMode = false)
        val unmaskedPaid = CurrencyFormatter.format(totalPaid, isPrivacyMode = false)

        assertTrue(unmaskedOwed.contains("950,00"))
        assertTrue(unmaskedPending.contains("650,00"))
        assertTrue(unmaskedPaid.contains("300,00"))
    }

    @Test
    fun testDebtMessageFormatterAlwaysUsesRealValuesEvenWhenUIIsMasked() {
        val item = InstallmentItem(
            id = 1L,
            expenseId = 10L,
            personId = 1L,
            personName = "Pai",
            expenseTitle = "Farmácia",
            expenseType = ExpenseType.CARD_PURCHASE,
            installmentNumber = 1,
            totalInstallments = 1,
            year = 2026,
            month = 5,
            amount = 5000L, // R$ 50,00 em centavos
            isPaid = false
        )

        // The DebtMessageFormatter formats for external sharing and MUST contain real values
        val message = DebtMessageFormatter.format(
            personName = "Pai",
            items = listOf(item),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertFalse(message.contains("••••"))
        assertFalse(message.contains("R$ ••••••"))
        assertTrue(message.contains("50,00"))
    }

    @Test
    fun testViewModelPrivacyModeToggle() {
        val application: Application = ApplicationProvider.getApplicationContext()
        val viewModel = FinanceViewModel(application)

        assertFalse(viewModel.isPrivacyMode.value)

        viewModel.togglePrivacyMode()
        assertTrue(viewModel.isPrivacyMode.value)

        viewModel.togglePrivacyMode()
        assertFalse(viewModel.isPrivacyMode.value)
    }
}
