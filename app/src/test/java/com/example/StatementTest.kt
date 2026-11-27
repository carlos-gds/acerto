package com.example

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.util.Statement
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class StatementTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val nov12 = 1_794_441_600_000L // 12/11/2026 00:00 UTC
    private val oct5 = 1_791_158_400_000L  // 05/10/2026 00:00 UTC

    private fun item(
        id: Long, expense: Long, title: String, n: Int, total: Int, month: Int, cents: Long,
        paid: Boolean, at: Long?, type: ExpenseType = ExpenseType.CARD_PURCHASE, mine: Boolean = false
    ) = InstallmentItem(
        id = id, expenseId = expense, personId = 1, personName = "Mãe", expenseTitle = title, expenseType = type,
        installmentNumber = n, totalInstallments = total, year = 2026, month = month, amount = cents,
        isPaid = paid, paidAt = at, isMyShare = mine
    )

    private val items = listOf(
        // um "Receber" de R$ 300 quitou duas parcelas no mesmo instante
        item(1, 10, "Geladeira", 1, 10, 11, 20000, true, nov12),
        item(2, 20, "Fone", 1, 3, 11, 10000, true, nov12),
        // conta dividida paga em outubro
        item(3, 30, "Internet", 1, 12, 10, 6000, true, oct5, ExpenseType.SHARED_BILL),
        // a minha parte da conta não entra no extrato
        item(4, 30, "Internet", 1, 12, 10, 6000, true, oct5, ExpenseType.SHARED_BILL, mine = true),
        // parcela paga em parte: R$ 50 pagos e R$ 150 ainda em aberto
        item(5, 10, "Geladeira", 2, 10, 12, 5000, true, nov12 + 3_600_000),
        item(6, 10, "Geladeira", 2, 10, 12, 15000, false, null),
        // pendentes não aparecem
        item(7, 20, "Fone", 2, 3, 12, 10000, false, null)
    )

    @Test fun juntaOQueFoiPagoNoMesmoMomento_maisRecentePrimeiro() {
        val e = Statement.build(items)
        assertEquals(3, e.size)
        assertEquals(listOf("Geladeira 2/10 (parte)"), e[0].items)
        assertEquals(5000L, e[0].amountCents)
        assertEquals(listOf("Fone 1/3", "Geladeira 1/10"), e[1].items)
        assertEquals(30000L, e[1].amountCents)
        assertEquals(listOf("Internet out/26"), e[2].items)
        assertEquals(6000L, e[2].amountCents)
    }

    @Test fun semDataFicaNoFim() {
        val e = Statement.build(listOf(item(1, 10, "Tênis", 1, 1, 9, 9000, true, null), item(2, 20, "Fone", 1, 1, 11, 1000, true, nov12)))
        assertEquals(listOf("Fone", "Tênis"), e.map { it.items.single() })
        assertEquals("Sem data", Statement.date(e[1].paidAt))
    }

    @Test fun textoParaCompartilhar() {
        val text = Statement.shareText("Mãe", items, utc)
        val expected = listOf(
            "Extrato de Mãe",
            "Já pagou R\$ 410,00 · falta R\$ 250,00",
            "",
            "12/11/2026 · R\$ 50,00 · Geladeira 2/10 (parte)",
            "12/11/2026 · R\$ 300,00 · Fone 1/3, Geladeira 1/10",
            "05/10/2026 · R\$ 60,00 · Internet out/26"
        ).joinToString("\n")
        assertEquals(expected, text.replace(' ', ' '))
    }
}
