package com.example.util

import android.content.Context
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem

/**
 * Fatura do cartão no mês. O app conhece só as compras que os outros fizeram no
 * seu cartão; o valor total da fatura é você quem informa (fica só no aparelho).
 * A sua parte é o total menos o que é dos outros.
 */
object Fatura {
    private const val PREFS = "acerto_prefs"
    private fun key(year: Int, month: Int) = "fatura_total_${year}_$month"

    data class Summary(
        /** Parcelas de compras dos outros nesta fatura. */
        val othersCents: Long,
        /** Quanto disso já foi recebido. */
        val receivedCents: Long,
        /** Valor total da fatura, se você informou. */
        val totalCents: Long?
    ) {
        /** Sua parte: total − dos outros (null sem total; nunca negativa). */
        val mineCents: Long? get() = totalCents?.let { (it - othersCents).coerceAtLeast(0L) }
        /** Total informado menor que as compras dos outros (provavelmente digitado errado). */
        val totalTooLow: Boolean get() = totalCents != null && totalCents < othersCents
    }

    fun summary(installments: List<InstallmentItem>, year: Int, month: Int, totalCents: Long?): Summary {
        val others = installments.filter {
            it.expenseType == ExpenseType.CARD_PURCHASE && !it.isMyShare && it.year == year && it.month == month
        }
        return Summary(
            othersCents = others.sumOf { it.amount },
            receivedCents = others.filter { it.isPaid }.sumOf { it.amount },
            totalCents = totalCents
        )
    }

    fun getTotal(context: Context, year: Int, month: Int): Long? {
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return if (p.contains(key(year, month))) p.getLong(key(year, month), 0L) else null
    }

    fun setTotal(context: Context, year: Int, month: Int, cents: Long?) {
        val e = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        if (cents == null || cents <= 0L) e.remove(key(year, month)) else e.putLong(key(year, month), cents)
        e.apply()
    }
}
