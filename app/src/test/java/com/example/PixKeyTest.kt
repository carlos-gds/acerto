package com.example

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.util.DebtMessageFormatter
import com.example.util.PixKeyStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PixKeyTest {

    private fun item(paid: Boolean) = InstallmentItem(
        id = 1, expenseId = 1, personId = 2, personName = "Ana", expenseTitle = "Tênis",
        expenseType = ExpenseType.CARD_PURCHASE, installmentNumber = 1, totalInstallments = 3,
        year = 2026, month = 11, amount = 10000, isPaid = paid
    )

    @Test
    fun cobrancaIncluiChavePixQuandoHaPendencia() {
        val msg = DebtMessageFormatter.format("Ana", listOf(item(paid = false)), pixKey = "ana@email.com")
        assertTrue(msg.contains("E-mail: ana@email.com"))
        // A chave vem antes do fechamento
        assertTrue(msg.indexOf("E-mail:") < msg.indexOf("Quando puder"))
    }

    @Test
    fun cobrancaSemChaveOuSemPendenciaNaoMostraPix() {
        assertFalse(DebtMessageFormatter.format("Ana", listOf(item(false))).contains("Pix"))
        assertFalse(DebtMessageFormatter.format("Ana", listOf(item(false)), pixKey = "  ").contains("Pix"))
        assertFalse(DebtMessageFormatter.format("Ana", listOf(item(true)), pixKey = "ana@email.com").contains("Pix"))
    }

    @Test
    fun detectaTipoDaChave() {
        assertEquals("E-mail", PixKeyStore.describe("ana@email.com"))
        assertEquals("CPF", PixKeyStore.describe("123.456.789-09"))
        assertEquals("CNPJ", PixKeyStore.describe("12.345.678/0001-95"))
        assertEquals("Celular", PixKeyStore.describe("+55 11 98765-4321"))
        assertEquals("Chave aleatória", PixKeyStore.describe("123e4567-e89b-12d3-a456-426614174000"))
        assertEquals("", PixKeyStore.describe(""))
    }
}
