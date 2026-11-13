package com.example

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.util.MonthSelectionPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class MonthSelectionPolicyTest {

    private var nextId = 1L
    private fun item(year: Int, month: Int, paid: Boolean, myShare: Boolean = false) = InstallmentItem(
        id = nextId++, expenseId = 1, personId = 2, personName = "Ana", expenseTitle = "Compra",
        expenseType = ExpenseType.CARD_PURCHASE, installmentNumber = 1, totalInstallments = 1,
        year = year, month = month, amount = 1000, isPaid = paid, isMyShare = myShare
    )

    @Test
    fun semParcelas_vaiParaMesAtual() {
        assertEquals(2026 to 11, MonthSelectionPolicy.autoMonth(emptyList(), 2026, 11))
    }

    @Test
    fun mesAnteriorComPendencia_naoAvanca() {
        val items = listOf(item(2026, 10, paid = false), item(2026, 11, paid = false))
        assertEquals(2026 to 10, MonthSelectionPolicy.autoMonth(items, 2026, 11))
    }

    @Test
    fun variosMesesPendentes_priorizaOMaisAntigo() {
        val items = listOf(
            item(2026, 9, paid = false),
            item(2026, 7, paid = false),
            item(2026, 10, paid = false),
            item(2026, 8, paid = true)
        )
        assertEquals(2026 to 7, MonthSelectionPolicy.autoMonth(items, 2026, 11))
    }

    @Test
    fun mesAnteriorQuitado_avancaParaOAtual() {
        val items = listOf(item(2026, 10, paid = true), item(2026, 11, paid = false))
        assertEquals(2026 to 11, MonthSelectionPolicy.autoMonth(items, 2026, 11))
    }

    @Test
    fun pendenciasFuturasNaoSeguramOMes() {
        val items = listOf(item(2027, 1, paid = false), item(2026, 12, paid = false))
        assertEquals(2026 to 11, MonthSelectionPolicy.autoMonth(items, 2026, 11))
    }

    @Test
    fun minhaParteNaoContaComoPendencia() {
        val items = listOf(item(2026, 10, paid = false, myShare = true))
        assertEquals(2026 to 11, MonthSelectionPolicy.autoMonth(items, 2026, 11))
    }

    @Test
    fun viradaDeAno_pendenciaEmDezembroSeguraJaneiro() {
        val items = listOf(item(2025, 12, paid = false))
        assertEquals(2025 to 12, MonthSelectionPolicy.autoMonth(items, 2026, 1))
    }
}
