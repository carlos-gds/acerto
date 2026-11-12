package com.example

import android.content.Intent
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.util.CurrencyFormatter
import com.example.util.DebtMessageFormatter
import com.example.util.Money
import com.example.util.ShareIntentHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DebtMessageFormatterTest {

    private fun createItem(
        id: Long = 1L,
        expenseId: Long = 10L,
        personId: Long = 100L,
        personName: String = "Maria",
        expenseTitle: String = "Supermercado",
        expenseType: ExpenseType = ExpenseType.CARD_PURCHASE,
        installmentNumber: Int = 1,
        totalInstallments: Int = 1,
        year: Int = 2026,
        month: Int = 1,
        amount: Double = 120.0,
        isPaid: Boolean = false,
        isMyShare: Boolean = false
    ): InstallmentItem {
        return InstallmentItem(
            id = id,
            expenseId = expenseId,
            personId = personId,
            personName = personName,
            expenseTitle = expenseTitle,
            expenseType = expenseType,
            installmentNumber = installmentNumber,
            totalInstallments = totalInstallments,
            year = year,
            month = month,
            amount = Money.fromReal(amount), // entidade armazena centavos (Long)
            isPaid = isPaid,
            isMyShare = isMyShare
        )
    }

    @Test
    fun testSinglePendingItem() {
        val item = createItem(
            installmentNumber = 1,
            totalInstallments = 1,
            amount = 120.0,
            isPaid = false
        )

        val message = DebtMessageFormatter.format(
            personName = "Maria",
            items = listOf(item),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertTrue(message.contains("Olá, Maria!"))
        assertTrue(message.contains("Supermercado"))
        assertTrue(message.contains("parcela 1/1"))
        assertTrue(message.contains("Pendente"))
        assertTrue(message.contains("Total da dívida:"))
        assertTrue(message.contains("Total pendente:"))
        assertFalse(message.contains("Total já pago:")) // No paid items
        assertTrue(message.contains("Quando puder, me avise sobre o pagamento. Obrigado!"))
    }

    @Test
    fun testMultipleInstallments() {
        val item1 = createItem(
            id = 1L,
            installmentNumber = 1,
            totalInstallments = 3,
            amount = 80.0,
            month = 1,
            year = 2026
        )
        val item2 = createItem(
            id = 2L,
            installmentNumber = 2,
            totalInstallments = 3,
            amount = 80.0,
            month = 2,
            year = 2026
        )

        val message = DebtMessageFormatter.format(
            personName = "Carlos",
            items = listOf(item1, item2),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertTrue(message.contains("parcela 1/3"))
        assertTrue(message.contains("parcela 2/3"))
        assertTrue(message.contains("Janeiro/2026"))
        assertTrue(message.contains("Fevereiro/2026"))
        assertTrue(message.contains(CurrencyFormatter.format(160.0)))
    }

    @Test
    fun testPaidAndPendingItems() {
        val itemPaid = createItem(
            id = 1L,
            expenseTitle = "Farmácia",
            amount = 50.0,
            isPaid = true
        )
        val itemPending = createItem(
            id = 2L,
            expenseTitle = "Restaurante",
            amount = 100.0,
            isPaid = false
        )

        val message = DebtMessageFormatter.format(
            personName = "Lucas",
            items = listOf(itemPaid, itemPending),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertTrue(message.contains("Farmácia"))
        assertTrue(message.contains("Pago"))
        assertTrue(message.contains("Restaurante"))
        assertTrue(message.contains("Pendente"))
        assertTrue(message.contains("Total da dívida: ${CurrencyFormatter.format(150.0)}"))
        assertTrue(message.contains("Total pendente: ${CurrencyFormatter.format(100.0)}"))
        assertTrue(message.contains("Total já pago: ${CurrencyFormatter.format(50.0)}"))
    }

    @Test
    fun testTotalPendingAndTotalPaidWhenAllPaid() {
        val item = createItem(amount = 200.0, isPaid = true)

        val message = DebtMessageFormatter.format(
            personName = "Ana",
            items = listOf(item),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertTrue(message.contains("Total da dívida: ${CurrencyFormatter.format(200.0)}"))
        assertTrue(message.contains("Total pendente: ${CurrencyFormatter.format(0.0)} (Tudo quitado! 🎉)"))
        assertTrue(message.contains("Total já pago: ${CurrencyFormatter.format(200.0)}"))
    }

    @Test
    fun testFilterSinglePersonOnly() {
        val itemPerson1 = createItem(id = 1L, personId = 10L, personName = "Ana", expenseTitle = "Bolo", amount = 40.0)
        val itemPerson2 = createItem(id = 2L, personId = 20L, personName = "Beto", expenseTitle = "Tênis", amount = 300.0)

        val message = DebtMessageFormatter.format(
            personName = "Ana",
            items = listOf(itemPerson1, itemPerson2),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME,
            targetPersonId = 10L
        )

        assertTrue(message.contains("Bolo"))
        assertFalse(message.contains("Tênis"))
        assertFalse(message.contains("Beto"))
        assertTrue(message.contains(CurrencyFormatter.format(40.0)))
        assertFalse(message.contains(CurrencyFormatter.format(300.0)))
    }

    @Test
    fun testMonthlyFilterRespectsSelectedMonthAndYear() {
        val itemJan = createItem(id = 1L, month = 1, year = 2026, expenseTitle = "Janeiro Conta", amount = 100.0)
        val itemFev = createItem(id = 2L, month = 2, year = 2026, expenseTitle = "Fevereiro Conta", amount = 150.0)

        val messageMonthly = DebtMessageFormatter.format(
            personName = "Maria",
            items = listOf(itemJan, itemFev),
            scope = DebtMessageFormatter.DebtScope.SELECTED_MONTH,
            selectedMonth = 1,
            selectedYear = 2026
        )

        assertTrue(messageMonthly.contains("Janeiro de 2026"))
        assertTrue(messageMonthly.contains("Janeiro Conta"))
        assertFalse(messageMonthly.contains("Fevereiro Conta"))
        assertTrue(messageMonthly.contains(CurrencyFormatter.format(100.0)))
        assertFalse(messageMonthly.contains(CurrencyFormatter.format(150.0)))
    }

    @Test
    fun testGeneralAllTimeFilterIncludesAllMonths() {
        val itemJan = createItem(id = 1L, month = 1, year = 2026, expenseTitle = "Janeiro Item", amount = 50.0)
        val itemFev = createItem(id = 2L, month = 2, year = 2026, expenseTitle = "Fevereiro Item", amount = 70.0)

        val messageAllTime = DebtMessageFormatter.format(
            personName = "Maria",
            items = listOf(itemJan, itemFev),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertTrue(messageAllTime.contains("resumo geral de todas as suas pendências acumuladas"))
        assertTrue(messageAllTime.contains("Janeiro Item"))
        assertTrue(messageAllTime.contains("Fevereiro Item"))
        assertTrue(messageAllTime.contains(CurrencyFormatter.format(120.0)))
    }

    @Test
    fun testExclusionOfMyShare() {
        val userShare = createItem(id = 1L, expenseTitle = "Minha Parte Luz", amount = 90.0, isMyShare = true)
        val debtorShare = createItem(id = 2L, expenseTitle = "Parte Maria Luz", amount = 90.0, isMyShare = false)

        val message = DebtMessageFormatter.format(
            personName = "Maria",
            items = listOf(userShare, debtorShare),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertFalse(message.contains("Minha Parte Luz"))
        assertTrue(message.contains("Parte Maria Luz"))
        assertTrue(message.contains(CurrencyFormatter.format(90.0)))
        assertFalse(message.contains(CurrencyFormatter.format(180.0)))
    }

    @Test
    fun testCurrencyAndMonthYearFormatting() {
        val item = createItem(
            expenseTitle = "Internet",
            expenseType = ExpenseType.SHARED_BILL,
            month = 5,
            year = 2026,
            amount = 89.90
        )

        val message = DebtMessageFormatter.format(
            personName = "João",
            items = listOf(item),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertTrue(message.contains("Maio/2026"))
        assertTrue(message.contains("conta compartilhada"))
        assertTrue(message.contains("R$"))
        assertTrue(message.contains("89,90"))
    }

    @Test
    fun testAbsenceAndPresenceOfNotes() {
        val item1 = createItem(id = 1L, expenseId = 101L, expenseTitle = "Item Com Nota")
        val item2 = createItem(id = 2L, expenseId = 102L, expenseTitle = "Item Sem Nota")

        val notesMap = mapOf(101L to "Dividido em partes iguais no almoço")

        val message = DebtMessageFormatter.format(
            personName = "Carla",
            items = listOf(item1, item2),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME,
            notesByExpenseId = notesMap
        )

        assertTrue(message.contains("Item Com Nota"))
        assertTrue(message.contains("Obs: Dividido em partes iguais no almoço"))
        assertTrue(message.contains("Item Sem Nota"))
    }

    @Test
    fun testEmptyItemsList() {
        val messageAllTime = DebtMessageFormatter.format(
            personName = "Renata",
            items = emptyList(),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertTrue(messageAllTime.contains("Olá, Renata!"))
        assertTrue(messageAllTime.contains("Não há pendências registradas no momento."))

        val messageMonthly = DebtMessageFormatter.format(
            personName = "Renata",
            items = emptyList(),
            scope = DebtMessageFormatter.DebtScope.SELECTED_MONTH,
            selectedMonth = 3,
            selectedYear = 2026
        )

        assertTrue(messageMonthly.contains("Não há pendências registradas para Março de 2026."))
    }

    @Test
    fun testEmptyOrUnknownPersonName() {
        val item = createItem(personName = "")

        val msgEmpty = DebtMessageFormatter.format(
            personName = "",
            items = listOf(item),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )
        assertTrue(msgEmpty.startsWith("Olá!\n\n"))

        val msgUnknown = DebtMessageFormatter.format(
            personName = "Desconhecido",
            items = listOf(item),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )
        assertTrue(msgUnknown.startsWith("Olá!\n\n"))
    }

    @Test
    fun testPrivacyModeProducesRealValues() {
        val item = createItem(amount = 250.75)

        // Ensure that formatter never masks values with bullet points or asterisks
        val message = DebtMessageFormatter.format(
            personName = "Pedro",
            items = listOf(item),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME
        )

        assertFalse(message.contains("••••"))
        assertFalse(message.contains("R$ ••••••"))
        assertTrue(message.contains("250,75"))
    }

    @Test
    fun testSpecialCharactersAndEmojis() {
        val item = createItem(
            expenseTitle = "Compras no Supermercado & Padaria 🥖",
            amount = 65.50
        )

        val notesMap = mapOf(item.expenseId to "Não esquecer o cupom fiscal! 🧾")

        val message = DebtMessageFormatter.format(
            personName = "João & Maria 🛒",
            items = listOf(item),
            scope = DebtMessageFormatter.DebtScope.ALL_TIME,
            notesByExpenseId = notesMap
        )

        assertTrue(message.contains("Olá, João & Maria 🛒!"))
        assertTrue(message.contains("Compras no Supermercado & Padaria 🥖"))
        assertTrue(message.contains("Não esquecer o cupom fiscal! 🧾"))
        assertTrue(message.contains("65,50"))
    }

    @Test
    fun testShareIntentHelperSendIntentCreation() {
        val sampleText = "Olá, Pedro! Total pendente: R$ 100,00"
        val intent = ShareIntentHelper.createSendIntent(sampleText)

        assertNotNull(intent)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals(sampleText, intent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun testShareIntentHelperChooserIntentCreation() {
        val sampleText = "Cobrança teste"
        val chooser = ShareIntentHelper.createChooserIntent(sampleText, "Cobrar Pedro")

        assertNotNull(chooser)
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val wrappedIntent = androidx.core.content.IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java)
        assertNotNull(wrappedIntent)
        assertEquals(Intent.ACTION_SEND, wrappedIntent?.action)
        assertEquals("text/plain", wrappedIntent?.type)
        assertEquals(sampleText, wrappedIntent?.getStringExtra(Intent.EXTRA_TEXT))
    }
}
