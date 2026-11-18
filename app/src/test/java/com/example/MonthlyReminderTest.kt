package com.example

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.util.MonthlyReminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MonthlyReminderTest {

    private fun cal(y: Int, m: Int, d: Int, h: Int) = Calendar.getInstance().apply {
        clear(); set(y, m - 1, d, h, 0, 0)
    }

    @Test
    fun proximoLembreteEhDia1As9h() {
        val next = MonthlyReminder.nextTrigger(cal(2026, 11, 17, 15))
        assertEquals(2026, next.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, next.get(Calendar.MONTH))
        assertEquals(1, next.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, next.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun dia1AntesDas9hDisparaNoMesmoDia() {
        val next = MonthlyReminder.nextTrigger(cal(2026, 12, 1, 7))
        assertEquals(Calendar.DECEMBER, next.get(Calendar.MONTH))
        assertEquals(1, next.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun viradaDeAno() {
        val next = MonthlyReminder.nextTrigger(cal(2026, 12, 20, 10))
        assertEquals(2027, next.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, next.get(Calendar.MONTH))
    }

    private fun item(person: Long, cents: Long, paid: Boolean = false, mine: Boolean = false) = InstallmentItem(
        expenseId = 1, personId = person, personName = "P$person", expenseTitle = "X",
        expenseType = ExpenseType.CARD_PURCHASE, installmentNumber = 1, totalInstallments = 1,
        year = 2026, month = 12, amount = cents, isPaid = paid, isMyShare = mine
    )

    @Test
    fun mensagemContaPessoasEPendenteDoMes() {
        val msg = MonthlyReminder.message(
            listOf(item(1, 10000), item(1, 5000), item(2, 2000), item(3, 9999, paid = true), item(0, 7000, mine = true)),
            2026, 12
        )!!
        assertEquals("2 pessoas te devem em dezembro", msg.first)
        assertTrue(msg.second.contains("170,00"))
    }

    @Test
    fun semPendenciaNaoNotifica() {
        assertNull(MonthlyReminder.message(listOf(item(1, 100, paid = true)), 2026, 12))
    }

    @Test
    fun comVencimentoChegaTresDiasAntes() {
        val next = MonthlyReminder.nextTrigger(cal(2026, 11, 17, 15), dueDay = 25)
        assertEquals(Calendar.NOVEMBER, next.get(Calendar.MONTH))
        assertEquals(22, next.get(Calendar.DAY_OF_MONTH))
        val passou = MonthlyReminder.nextTrigger(cal(2026, 11, 23, 10), dueDay = 25)
        assertEquals(Calendar.DECEMBER, passou.get(Calendar.MONTH))
        assertEquals(22, passou.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun vencimentoNoComecoOuNoFimDoMes() {
        assertEquals(1, MonthlyReminder.reminderDay(2))
        // vencimento dia 31: em fevereiro o lembrete vai para o último dia possível (28)
        val fev = MonthlyReminder.nextTrigger(cal(2027, 2, 1, 8), dueDay = 31)
        assertEquals(Calendar.FEBRUARY, fev.get(Calendar.MONTH))
        assertEquals(28, fev.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun mensagemComVencimento() {
        val msg = MonthlyReminder.message(listOf(item(1, 10000)), 2026, 12, dueDay = 10)!!
        assertEquals("Sua fatura vence dia 10", msg.first)
        assertTrue(msg.second.contains("Cobre antes"))
    }
}
