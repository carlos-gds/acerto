package com.example

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.util.Forecast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ForecastTest {

    private fun item(
        person: Long, y: Int, m: Int, cents: Long, paid: Boolean = false, mine: Boolean = false,
        type: ExpenseType = ExpenseType.CARD_PURCHASE, expense: Long = 1
    ) = InstallmentItem(
        expenseId = expense, personId = person, personName = "P", expenseTitle = "X", expenseType = type,
        installmentNumber = 1, totalInstallments = 1, year = y, month = m, amount = cents, isPaid = paid, isMyShare = mine
    )

    @Test
    fun proximosMesesSomaSoPendenteDeOutros_eViraOAno() {
        val items = listOf(
            item(1, 2026, 11, 10000), item(2, 2026, 11, 5000, paid = true),
            item(1, 2026, 12, 10000), item(0, 2026, 12, 3000, mine = true),
            item(1, 2027, 1, 7000)
        )
        val r = Forecast.nextMonths(items, 2026, 11, 4)
        assertEquals(listOf(100.0, 100.0, 70.0, 0.0), r.map { it.amount })
        assertEquals(2027 to 1, r[2].year to r[2].month)
        assertEquals(2027 to 2, r[3].year to r[3].month)
    }

    @Test
    fun quitaNaUltimaParcelaPendente() {
        val items = listOf(item(1, 2026, 11, 10000), item(1, 2027, 8, 10000), item(1, 2027, 9, 10000, paid = true))
        val p = Forecast.payoff(items, 1)!!
        assertEquals(200.0, p.remaining, 0.001)
        assertEquals(2027 to 8, p.lastYear to p.lastMonth)
        assertNull(Forecast.payoff(items, 99))
    }

    @Test
    fun totalDoMesIncluiPagas() {
        val items = listOf(item(1, 2026, 10, 10000, paid = true), item(2, 2026, 10, 5000), item(0, 2026, 10, 999, mine = true))
        assertEquals(150.0, Forecast.monthTotal(items, 2026, 10), 0.001)
    }

    @Test
    fun resumoDasContasFixasDivididas() {
        val month = listOf(
            item(0, 2026, 11, 2000, mine = true, type = ExpenseType.SHARED_BILL, expense = 5),
            item(1, 2026, 11, 2000, type = ExpenseType.SHARED_BILL, expense = 5),
            item(0, 2026, 11, 4130, mine = true, type = ExpenseType.SHARED_BILL, expense = 6),
            item(0, 2026, 11, 9999, mine = true, type = ExpenseType.SHARED_BILL, expense = 7) // não recorrente
        )
        val r = Forecast.recurring(month, setOf(5L, 6L))!!
        assertEquals(2, r.count)
        assertEquals(61.30, r.myShareMonthly, 0.001)
        assertEquals(20.0, r.othersMonthly, 0.001)
        assertNull(Forecast.recurring(month, emptySet()))
    }
}
