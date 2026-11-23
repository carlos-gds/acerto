package com.example

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.util.CsvExport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExportTest {
    @Test
    fun linhaComVirgulaDecimal_eAspas() {
        val csv = CsvExport.build(
            listOf(
                InstallmentItem(
                    expenseId = 1, personId = 2, personName = "Ana", expenseTitle = "Tênis; \"Nike\"",
                    expenseType = ExpenseType.CARD_PURCHASE, installmentNumber = 2, totalInstallments = 10,
                    year = 2026, month = 3, amount = 12_345, isPaid = false
                )
            )
        )
        assertTrue(csv.startsWith(CsvExport.BOM + "Pessoa;Descrição;Tipo"))
        val line = csv.removePrefix(CsvExport.BOM).split("\r\n")[1]
        assertEquals("Ana;\"Tênis; \"\"Nike\"\"\";Cartão;2/10;03/2026;123,45;Pendente;", line)
    }
}
