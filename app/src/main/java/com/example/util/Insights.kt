package com.example.util

import com.example.data.model.InstallmentItem

/**
 * Destaques automáticos: frases curtas tiradas das parcelas, sem nada para
 * configurar. Ex.: "Vó Maria deve há 3 meses", "Mês que vem entram R$ 420".
 */
object Insights {
    enum class Kind { LATE, TOP, NEXT_MONTH, RECEIVED }

    data class Insight(val kind: Kind, val title: String, val detail: String, val personId: Long? = null)

    private fun index(year: Int, month: Int) = year * 12 + (month - 1)

    fun build(installments: List<InstallmentItem>, year: Int, month: Int, hidden: Boolean = false): List<Insight> {
        val now = index(year, month)
        val owed = installments.filter { !it.isMyShare && it.amount > 0 }
        val open = owed.filter { !it.isPaid }
        val out = mutableListOf<Insight>()
        fun money(cents: Long) = CurrencyFormatter.format(Money.toReal(cents), hidden)

        // 1. Quem está atrasado há mais tempo (parcela vencida mais antiga)
        open.filter { index(it.year, it.month) < now }
            .groupBy { it.personId }
            .map { (id, list) -> Triple(id, list, now - list.minOf { index(it.year, it.month) }) }
            .maxWithOrNull(compareBy<Triple<Long, List<InstallmentItem>, Int>> { it.third }.thenBy { t -> t.second.sumOf { it.amount } })
            ?.let { (id, list, months) ->
                val name = list.first().personName
                out += Insight(
                    Kind.LATE,
                    if (months == 1) "$name está atrasado desde o mês passado" else "$name deve há $months meses",
                    "${money(list.sumOf { it.amount })} vencido",
                    id
                )
            }

        // 2. Quem mais deve no total
        val byPerson = open.groupBy { it.personId }
        val total = open.sumOf { it.amount }
        if (byPerson.size >= 2 && total > 0) {
            val (id, list) = byPerson.maxBy { (_, l) -> l.sumOf { it.amount } }
            val sum = list.sumOf { it.amount }
            val pct = (sum * 100 / total).toInt()
            out += Insight(Kind.TOP, "${list.first().personName} é quem mais te deve", "${money(sum)} · $pct% do total", id)
        }

        // 3. O que entra no mês que vem
        val next = open.filter { index(it.year, it.month) == now + 1 }
        if (next.isNotEmpty()) {
            val people = next.map { it.personId }.distinct().size
            out += Insight(
                Kind.NEXT_MONTH,
                "Mês que vem entram ${money(next.sumOf { it.amount })}",
                if (people == 1) "de 1 pessoa" else "de $people pessoas"
            )
        }

        // 4. Quanto já entrou neste mês
        val paidNow = owed.filter { it.isPaid && index(it.year, it.month) == now }
        if (paidNow.isNotEmpty()) {
            val monthTotal = owed.filter { index(it.year, it.month) == now }.sumOf { it.amount }
            val paid = paidNow.sumOf { it.amount }
            val pct = if (monthTotal > 0) (paid * 100 / monthTotal).toInt() else 100
            out += Insight(Kind.RECEIVED, "Você já recebeu ${money(paid)} este mês", "$pct% do que vence no mês")
        }
        return out
    }
}
