package com.example.util

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.ui.viewmodel.PersonMonthlyGroup

/**
 * Pure domain formatter that generates debt reminder and summary messages in Brazilian Portuguese.
 * Has no dependencies on Android Context, UI, Intent, or Room database.
 */
object DebtMessageFormatter {

    enum class DebtScope {
        SELECTED_MONTH,
        ALL_TIME
    }

    /**
     * Formats a debt reminder message for a specific person.
     *
     * @param personName Name of the person being reminded.
     * @param items List of installments / debt items.
     * @param scope Whether the scope is a selected month or all-time accumulated.
     * @param selectedMonth Target month (1..12) if scope is [DebtScope.SELECTED_MONTH].
     * @param selectedYear Target year if scope is [DebtScope.SELECTED_MONTH].
     * @param notesByExpenseId Map from expenseId to notes/observations, if available.
     * @param targetPersonId Optional personId to strictly filter items belonging to that person.
     */
    fun format(
        personName: String,
        items: List<InstallmentItem>,
        scope: DebtScope = DebtScope.ALL_TIME,
        selectedMonth: Int? = null,
        selectedYear: Int? = null,
        notesByExpenseId: Map<Long, String> = emptyMap(),
        targetPersonId: Long? = null,
        /** Chave PIX do usuário; quando preenchida, entra no texto antes do fechamento. */
        pixKey: String? = null,
        /** Nome de quem recebe (aparece em "Para:"). */
        pixName: String? = null,
        /** Banco onde está a chave (só aparece no texto). */
        pixBank: String? = null,
        /** Não usado na mensagem (mantido por compatibilidade). */
        pixCity: String? = null
    ): String {
        val trimmedName = personName.trim()
        val greeting = if (trimmedName.isBlank() || trimmedName.equals("Desconhecido", ignoreCase = true)) {
            "Oi! 👋"
        } else {
            "Oi, $trimmedName! 👋"
        }

        // Nunca inclui a "minha parte" e, se pedido, só a pessoa certa
        val validItems = items.filter { item ->
            !item.isMyShare && (targetPersonId == null || item.personId == targetPersonId)
        }.let { list ->
            if (scope == DebtScope.SELECTED_MONTH && selectedMonth != null && selectedYear != null) {
                list.filter { it.month == selectedMonth && it.year == selectedYear }
            } else {
                list
            }
        }
        val monthScope = scope == DebtScope.SELECTED_MONTH && selectedMonth != null && selectedYear != null

        if (validItems.isEmpty()) {
            val period = if (monthScope) "em ${CurrencyFormatter.getMonthName(selectedMonth!!).lowercase()} de $selectedYear" else "no momento"
            return "$greeting\n\nVocê não tem nada pendente $period. 🎉"
        }

        val totalAmount = validItems.sumOf { it.amount }
        val paidAmount = validItems.filter { it.isPaid }.sumOf { it.amount }
        val pendingAmount = totalAmount - paidAmount

        // Mensagem curta: a imagem enviada junto já mostra o detalhe.
        // "9 de 12" (e não "9/12") e sem o ano em cada linha, para o WhatsApp não virar tudo link.
        val intro = if (monthScope) {
            "Seu resumo de ${CurrencyFormatter.getMonthName(selectedMonth!!).lowercase()} 🧾"
        } else {
            "Seu resumo geral 🧾"
        }

        fun line(item: InstallmentItem): String {
            val parts = mutableListOf(item.expenseTitle.trim())
            if (item.expenseType == ExpenseType.CARD_PURCHASE) {
                if (item.totalInstallments > 1) {
                    val last = if (item.installmentNumber == item.totalInstallments) " (última)" else ""
                    parts += "${item.installmentNumber} de ${item.totalInstallments}$last"
                }
            } else {
                parts += "conta dividida"
            }
            if (!monthScope) {
                parts += "${CurrencyFormatter.getMonthShortName(item.month).lowercase()}/${item.year % 100}"
            }
            parts += CurrencyFormatter.format(item.amount) + if (item.isPaid) " ✅" else ""
            val note = notesByExpenseId[item.expenseId]?.trim()
            val notePart = if (!note.isNullOrBlank()) " (obs: $note)" else ""
            return "• " + parts.joinToString(" · ") + notePart
        }

        val ordered = validItems.sortedWith(compareBy<InstallmentItem> { it.isPaid }.thenBy { it.year * 12 + it.month })

        val totals = buildString {
            if (pendingAmount > 0) {
                append("💰 *Falta pagar: ${CurrencyFormatter.format(pendingAmount)}*")
                if (paidAmount > 0) append("\n✅ Já pago: ${CurrencyFormatter.format(paidAmount)}")
            } else {
                append("🎉 Tudo pago! Total de ${CurrencyFormatter.format(totalAmount)}.")
            }
        }

        // PIX do usuário (Ajustes), só quando há valor pendente
        val pixSection = pixKey?.trim()?.takeIf { it.isNotEmpty() && pendingAmount > 0 }?.let { key ->
            buildString {
                append("📲 *Pix*")
                val who = listOfNotNull(pixName?.trim()?.takeIf { it.isNotEmpty() }, pixBank?.trim()?.takeIf { it.isNotEmpty() })
                if (who.isNotEmpty()) append("\nPara: ${who.joinToString(" · ")}")
                val type = PixPayload.detect(key)
                val label = if (type == null || type == PixPayload.KeyType.OTHER) "Chave" else type.label
                append("\n$label: ${PixPayload.displayKey(key)}")
            }
        }

        val closing = if (pendingAmount > 0) "Quando puder, me avisa por aqui. Obrigado! 🙏" else "Obrigado! 🙏"

        return buildString {
            append(greeting).append("\n\n")
            append(intro).append("\n")
            ordered.forEach { append(line(it)).append("\n") }
            append("\n").append(totals).append("\n\n")
            if (pixSection != null) append(pixSection).append("\n\n")
            append(closing)
        }
    }

    /**
     * Convenience overload accepting a [PersonMonthlyGroup].
     */
    fun format(
        group: PersonMonthlyGroup,
        scope: DebtScope = DebtScope.ALL_TIME,
        selectedMonth: Int? = null,
        selectedYear: Int? = null,
        notesByExpenseId: Map<Long, String> = emptyMap(),
        pixKey: String? = null,
        pixName: String? = null,
        pixBank: String? = null,
        pixCity: String? = null
    ): String {
        return format(
            personName = group.personName,
            items = group.items,
            scope = scope,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            notesByExpenseId = notesByExpenseId,
            targetPersonId = group.personId,
            pixKey = pixKey,
            pixName = pixName,
            pixBank = pixBank,
            pixCity = pixCity
        )
    }
}
