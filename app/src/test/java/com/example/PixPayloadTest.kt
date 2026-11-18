package com.example

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.util.DebtMessageFormatter
import com.example.util.PixPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PixPayloadTest {

    @Test
    fun crc16_bateComExemploOficialDoBancoCentral() {
        // Exemplo do Manual de Padrões para Iniciação do PIX (BCB), CRC esperado 1D3D
        val semCrc = "00020126580014br.gov.bcb.pix0136123e4567-e12b-12d1-a456-426655440000" +
            "5204000053039865802BR5913Fulano de Tal6008BRASILIA62070503***6304"
        assertEquals("1D3D", PixPayload.crc16(semCrc))
    }

    @Test
    fun montaPayloadComValorNomeECidade() {
        val p = PixPayload.build("ana@Email.com", "Ana Paula Ságio", "São Paulo", 120.5)!!
        assertTrue(p.startsWith("000201"))
        assertTrue(p.contains("0014br.gov.bcb.pix0113ana@email.com"))
        assertTrue(p.contains("5406120.50"))
        assertTrue(p.contains("5915ANA PAULA SAGIO"))
        assertTrue(p.contains("6009SAO PAULO"))
        // o código termina com um CRC válido do próprio conteúdo
        assertEquals(PixPayload.crc16(p.dropLast(4)), p.takeLast(4))
    }

    @Test
    fun semNomeOuCidadeNaoGeraCopiaECola() {
        assertNull(PixPayload.build("ana@email.com", "", "São Paulo"))
        assertNull(PixPayload.build("", "Ana", "Recife"))
        // sem cidade, vai "BRASIL" (os bancos não conferem esse campo)
        assertTrue(PixPayload.build("ana@email.com", "Ana")!!.contains("6006BRASIL"))
    }

    @Test
    fun normalizaTiposDeChave() {
        assertEquals("12345678000195", PixPayload.normalizeKey("12.345.678/0001-95"))
        assertEquals("52998224725", PixPayload.normalizeKey("529.982.247-25")) // CPF válido
        assertEquals("+5511987654321", PixPayload.normalizeKey("(11) 98765-4321"))
        assertEquals("+5511987654321", PixPayload.normalizeKey("+55 11 98765-4321"))
        assertEquals("123e4567-e12b-12d1-a456-426655440000", PixPayload.normalizeKey("123E4567-E12B-12D1-A456-426655440000"))
    }

    @Test
    fun cobrancaLevaCopiaEColaSoComNomeECidade() {
        val item = InstallmentItem(
            id = 1, expenseId = 1, personId = 2, personName = "Ana", expenseTitle = "Airfryer",
            expenseType = ExpenseType.CARD_PURCHASE, installmentNumber = 1, totalInstallments = 3,
            year = 2026, month = 11, amount = 10000, isPaid = false
        )
        val semDados = DebtMessageFormatter.format("Ana", listOf(item), pixKey = "ana@email.com")
        assertFalse(semDados.contains("opia e cola"))
        val comDados = DebtMessageFormatter.format(
            "Ana", listOf(item), pixKey = "ana@email.com", pixName = "Carlos", pixBank = "Nubank"
        )
        assertTrue(comDados.contains("Para: Carlos · Nubank"))
        assertTrue(comDados.contains("E-mail: ana@email.com"))
        // o copia e cola saiu da mensagem: nunca aparece, mesmo com nome
        assertFalse(comDados.contains("opia e cola"))
        assertFalse(comDados.contains("br.gov.bcb.pix"))
    }

    @Test
    fun detectaTipoDaChaveAutomaticamente() {
        assertEquals(PixPayload.KeyType.CPF, PixPayload.detect("529.982.247-25"))
        assertEquals(PixPayload.KeyType.CPF, PixPayload.detect("52998224725"))
        assertEquals(PixPayload.KeyType.PHONE, PixPayload.detect("(11) 98765-4321"))
        assertEquals(PixPayload.KeyType.PHONE, PixPayload.detect("11987654321")) // não é CPF válido
        assertEquals(PixPayload.KeyType.PHONE, PixPayload.detect("+55 11 98765-4321"))
        assertEquals(PixPayload.KeyType.CNPJ, PixPayload.detect("12.345.678/0001-95"))
        assertEquals(PixPayload.KeyType.EMAIL, PixPayload.detect("Ana@Email.com"))
        assertEquals(PixPayload.KeyType.RANDOM, PixPayload.detect("123e4567-e12b-12d1-a456-426655440000"))
        assertNull(PixPayload.detect("  "))
    }

    @Test
    fun chaveParaCopiarFicaLimpa() {
        assertEquals("52998224725", PixPayload.displayKey("529.982.247-25"))
        assertEquals("11987654321", PixPayload.displayKey("+55 (11) 98765-4321"))
        assertEquals("ana@email.com", PixPayload.displayKey(" Ana@Email.com "))
    }

    @Test
    fun cobrancaMostraTipoDaChave() {
        val item = InstallmentItem(
            id = 1, expenseId = 1, personId = 2, personName = "Ana", expenseTitle = "Airfryer",
            expenseType = ExpenseType.CARD_PURCHASE, installmentNumber = 1, totalInstallments = 3,
            year = 2026, month = 11, amount = 10000, isPaid = false
        )
        val msg = DebtMessageFormatter.format("Ana", listOf(item), pixKey = "529.982.247-25", pixName = "Carlos")
        assertTrue(msg.contains("CPF: 52998224725"))
        assertTrue(msg.contains("Para: Carlos"))
    }
}
