package com.example.util

import com.example.data.model.InstallmentItem

/**
 * Qual mês o app abre automaticamente.
 *
 * - Se algum mês anterior ao atual ainda tem parcela/dívida de terceiros pendente,
 *   o app fica no mês pendente MAIS ANTIGO (não avança para o mês novo).
 * - Quando tudo dos meses anteriores está quitado, vai para o mês atual.
 * - Parcelas futuras pendentes não contam (são normais), e a "minha parte" das
 *   contas divididas também não (ela não é dívida de ninguém com você).
 *
 * Só decide o mês exibido; não altera nenhum valor.
 */
object MonthSelectionPolicy {

    fun autoMonth(items: List<InstallmentItem>, currentYear: Int, currentMonth: Int): Pair<Int, Int> {
        val currentKey = currentYear * 12 + (currentMonth - 1)
        val oldestPending = items.asSequence()
            .filter { !it.isPaid && !it.isMyShare }
            .map { it.year * 12 + (it.month - 1) }
            .filter { it < currentKey }
            .minOrNull()
        val key = oldestPending ?: currentKey
        return (key / 12) to (key % 12 + 1)
    }
}
