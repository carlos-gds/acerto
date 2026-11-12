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
        targetPersonId: Long? = null
    ): String {
        val trimmedName = personName.trim()
        val greeting = if (trimmedName.isBlank() || trimmedName.equals("Desconhecido", ignoreCase = true)) {
            "Olá!"
        } else {
            "Olá, $trimmedName!"
        }

        // Strictly filter items: NEVER include user's own share ("isMyShare"), and ensure target person match
        val validItems = items.filter { item ->
            !item.isMyShare && (targetPersonId == null || item.personId == targetPersonId)
        }.let { list ->
            if (scope == DebtScope.SELECTED_MONTH && selectedMonth != null && selectedYear != null) {
                list.filter { it.month == selectedMonth && it.year == selectedYear }
            } else {
                list
            }
        }

        // Handle empty items case cleanly
        if (validItems.isEmpty()) {
            val periodDesc = if (scope == DebtScope.SELECTED_MONTH && selectedMonth != null && selectedYear != null) {
                "para ${CurrencyFormatter.getMonthName(selectedMonth)} de $selectedYear"
            } else {
                "no momento"
            }
            return buildString {
                append(greeting)
                append("\n\n")
                append("Não há pendências registradas $periodDesc.")
                append("\n\n")
                append("Obrigado!")
            }
        }

        val totalAmount = validItems.sumOf { it.amount }
        val paidAmount = validItems.filter { it.isPaid }.sumOf { it.amount }
        val pendingAmount = totalAmount - paidAmount

        val scopeIntro = when (scope) {
            DebtScope.SELECTED_MONTH -> {
                if (selectedMonth != null && selectedYear != null) {
                    val mName = CurrencyFormatter.getMonthName(selectedMonth)
                    "Este é um resumo das suas pendências para $mName de $selectedYear:"
                } else {
                    "Este é um resumo das suas pendências do mês selecionado:"
                }
            }
            DebtScope.ALL_TIME -> {
                "Este é um resumo geral de todas as suas pendências acumuladas:"
            }
        }

        val itemsLines = validItems.map { item ->
            val mName = CurrencyFormatter.getMonthName(item.month)
            val typePart = if (item.expenseType == ExpenseType.CARD_PURCHASE) {
                "parcela ${item.installmentNumber}/${item.totalInstallments}"
            } else {
                "conta compartilhada"
            }
            val amountFormatted = CurrencyFormatter.format(item.amount)
            val periodPart = "$mName/${item.year}"
            val statusPart = if (item.isPaid) "Pago" else "Pendente"

            val note = notesByExpenseId[item.expenseId]?.trim()
            val notePart = if (!note.isNullOrBlank()) " — Obs: $note" else ""

            "• ${item.expenseTitle} — $typePart — $amountFormatted — $periodPart — $statusPart$notePart"
        }

        val totalsSection = buildString {
            append("Total da dívida: ${CurrencyFormatter.format(totalAmount)}\n")
            if (pendingAmount > 0.001) {
                append("Total pendente: ${CurrencyFormatter.format(pendingAmount)}")
            } else {
                append("Total pendente: ${CurrencyFormatter.format(0.0)} (Tudo quitado! 🎉)")
            }
            if (paidAmount > 0.001) {
                append("\nTotal já pago: ${CurrencyFormatter.format(paidAmount)}")
            }
        }

        val closing = "Quando puder, me avise sobre o pagamento. Obrigado!"

        return buildString {
            append(greeting)
            append("\n\n")
            append(scopeIntro)
            append("\n")
            itemsLines.forEach { line ->
                append(line)
                append("\n")
            }
            append("\n")
            append(totalsSection)
            append("\n\n")
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
        notesByExpenseId: Map<Long, String> = emptyMap()
    ): String {
        return format(
            personName = group.personName,
            items = group.items,
            scope = scope,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            notesByExpenseId = notesByExpenseId,
            targetPersonId = group.personId
        )
    }
}
