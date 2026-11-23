package com.example

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.util.Insights
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsTest {
    private fun item(person: Long, name: String, y: Int, m: Int, cents: Long, paid: Boolean = false, mine: Boolean = false) =
        InstallmentItem(
            expenseId = 1, personId = person, personName = name, expenseTitle = "x", expenseType = ExpenseType.CARD_PURCHASE,
            installmentNumber = 1, totalInstallments = 1, year = y, month = m, amount = cents, isPaid = paid, isMyShare = mine
        )

    @Test
    fun atrasado_maisAntigoPrimeiro() {
        val list = listOf(
            item(1, "Ana", 2026, 8, 10_000),
            item(2, "Bia", 2026, 10, 5_000),
            item(2, "Bia", 2026, 11, 5_000)
        )
        val r = Insights.build(list, 2026, 11)
        assertEquals(Insights.Kind.LATE, r.first().kind)
        assertEquals("Ana deve há 3 meses", r.first().title)
    }

    @Test
    fun quemMaisDeve_comPorcentagem() {
        val list = listOf(item(1, "Ana", 2026, 11, 7_500), item(2, "Bia", 2026, 11, 2_500))
        val top = Insights.build(list, 2026, 11).first { it.kind == Insights.Kind.TOP }
        assertEquals("Ana é quem mais te deve", top.title)
        assertTrue(top.detail.endsWith("75% do total"))
    }

    @Test
    fun ignoraMinhaParte_eMesQueVem() {
        val list = listOf(item(1, "Ana", 2026, 12, 4_000), item(0, "Eu", 2026, 12, 9_000, mine = true))
        val r = Insights.build(list, 2026, 11)
        val next = r.single { it.kind == Insights.Kind.NEXT_MONTH }
        assertEquals("de 1 pessoa", next.detail)
        assertTrue(next.title.contains("40,00"))
    }

    @Test
    fun recebidoNoMes() {
        val list = listOf(item(1, "Ana", 2026, 11, 3_000, paid = true), item(2, "Bia", 2026, 11, 1_000))
        val rec = Insights.build(list, 2026, 11).single { it.kind == Insights.Kind.RECEIVED }
        assertEquals("75% do que vence no mês", rec.detail)
    }
}
