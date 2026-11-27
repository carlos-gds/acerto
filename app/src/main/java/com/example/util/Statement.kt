package com.example.util

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Extrato da pessoa: o que ela já pagou, do mais recente para o mais antigo.
 * Parcelas quitadas no mesmo instante (um "Receber" que quita várias) viram uma
 * linha só. Funções puras, testáveis.
 */
object Statement {

    data class Entry(
        /** Quando foi pago (null = pago antes de o app guardar a data). */
        val paidAt: Long?,
        val amountCents: Long,
        /** "Geladeira 3/10", "Internet nov/26", "Fone 2/3 (parte)" */
        val items: List<String>
    )

    fun build(items: List<InstallmentItem>): List<Entry> {
        val owed = items.filter { !it.isMyShare }
        val paid = owed.filter { it.isPaid }
        // parcela paga em parte: a parte paga e o restante ficam em duas linhas
        val partial = owed.filter { !it.isPaid }.map { it.expenseId to it.installmentNumber }.toSet()
        return paid.groupBy { it.paidAt }
            .map { (at, list) ->
                Entry(
                    paidAt = at,
                    amountCents = list.sumOf { it.amount },
                    items = list.sortedWith(compareBy({ it.year * 12 + it.month }, { it.expenseTitle }))
                        .map { label(it) + if ((it.expenseId to it.installmentNumber) in partial) " (parte)" else "" }
                )
            }
            .sortedWith(compareByDescending<Entry> { it.paidAt != null }.thenByDescending { it.paidAt ?: 0L })
    }

    fun label(item: InstallmentItem): String = when {
        item.expenseType == ExpenseType.SHARED_BILL ->
            "${item.expenseTitle} ${CurrencyFormatter.getMonthShortName(item.month).lowercase()}/${item.year % 100}"
        item.totalInstallments > 1 -> "${item.expenseTitle} ${item.installmentNumber}/${item.totalInstallments}"
        else -> item.expenseTitle
    }

    fun date(paidAt: Long?, zone: TimeZone = TimeZone.getDefault()): String {
        if (paidAt == null) return "Sem data"
        return SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply { timeZone = zone }.format(java.util.Date(paidAt))
    }

    /** Texto para mandar à pessoa (WhatsApp etc.). */
    fun shareText(name: String, items: List<InstallmentItem>, zone: TimeZone = TimeZone.getDefault()): String {
        val owed = items.filter { !it.isMyShare }
        val paid = owed.filter { it.isPaid }.sumOf { it.amount }
        val open = owed.filter { !it.isPaid }.sumOf { it.amount }
        val entries = build(items)
        return buildString {
            append("Extrato de ").append(name).append('\n')
            append("Já pagou ").append(CurrencyFormatter.format(Money.toReal(paid)))
            if (open > 0) append(" · falta ").append(CurrencyFormatter.format(Money.toReal(open)))
            append('\n')
            if (entries.isEmpty()) {
                append("\nNenhum pagamento ainda.")
            } else {
                append('\n')
                entries.forEach { e ->
                    append(date(e.paidAt, zone)).append(" · ").append(CurrencyFormatter.format(Money.toReal(e.amountCents)))
                    append(" · ").append(e.items.joinToString(", ")).append('\n')
                }
            }
        }.trimEnd()
    }
}
