package com.example.util

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem

/**
 * Contas de previsão (funções puras, testáveis): quanto falta receber nos
 * próximos meses, quando cada pessoa termina de pagar e o resumo das contas
 * fixas divididas. Considera só parcelas de outras pessoas (não "minha parte").
 */
object Forecast {

    data class MonthAmount(val year: Int, val month: Int, val amount: Double)

    private fun index(year: Int, month: Int) = year * 12 + (month - 1)
    private fun fromIndex(i: Int) = (i / 12) to (i % 12 + 1)

    /** Pendente a receber em cada um dos [count] meses a partir de [startYear]/[startMonth]. */
    fun nextMonths(items: List<InstallmentItem>, startYear: Int, startMonth: Int, count: Int = 6): List<MonthAmount> {
        val start = index(startYear, startMonth)
        val pending = items.filter { !it.isMyShare && !it.isPaid }
            .groupBy { index(it.year, it.month) }
            .mapValues { (_, v) -> Money.toReal(v.sumOf { it.amount }) }
        return (0 until count).map { i ->
            val (y, m) = fromIndex(start + i)
            MonthAmount(y, m, pending[start + i] ?: 0.0)
        }
    }

    /** Total (pago ou não) que outras pessoas devem no mês. */
    fun monthTotal(items: List<InstallmentItem>, year: Int, month: Int): Double =
        Money.toReal(items.filter { !it.isMyShare && it.year == year && it.month == month }.sumOf { it.amount })

    data class Payoff(val remaining: Double, val lastYear: Int, val lastMonth: Int)

    /** Quanto a pessoa ainda deve no total e em que mês paga a última parcela. */
    fun payoff(items: List<InstallmentItem>, personId: Long): Payoff? {
        val open = items.filter { it.personId == personId && !it.isMyShare && !it.isPaid }
        if (open.isEmpty()) return null
        val last = open.maxOf { index(it.year, it.month) }
        val (y, m) = fromIndex(last)
        return Payoff(Money.toReal(open.sumOf { it.amount }), y, m)
    }

    data class Recurring(val count: Int, val myShareMonthly: Double, val othersMonthly: Double)

    /** Contas fixas divididas no mês: quantas são, a sua parte e a dos outros. */
    fun recurring(monthItems: List<InstallmentItem>, recurringExpenseIds: Set<Long>): Recurring? {
        val bills = monthItems.filter { it.expenseType == ExpenseType.SHARED_BILL && it.expenseId in recurringExpenseIds }
        if (bills.isEmpty()) return null
        return Recurring(
            count = bills.map { it.expenseId }.distinct().size,
            myShareMonthly = Money.toReal(bills.filter { it.isMyShare }.sumOf { it.amount }),
            othersMonthly = Money.toReal(bills.filter { !it.isMyShare }.sumOf { it.amount })
        )
    }
}
