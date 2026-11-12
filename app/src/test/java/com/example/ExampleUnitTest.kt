package com.example

import com.example.data.service.GeminiExpenseParser
import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCurrencyFormatter() {
    val month1 = CurrencyFormatter.getMonthName(1)
    assertEquals("Janeiro", month1)
    val month12 = CurrencyFormatter.getMonthName(12)
    assertEquals("Dezembro", month12)
  }

  @Test
  fun testGeminiLocalFallbackParser() {
    val parser = GeminiExpenseParser()

    val res1 = parser.parseWithLocalFallback("Compra de 240 em 3x pra vó")
    assertNotNull(res1)
    assertEquals(240.0, res1!!.total_amount, 0.01)
    assertEquals(3, res1.installments)
    assertEquals("Vó", res1.target_person)

    val res2 = parser.parseWithLocalFallback("Tênis de 300 em 3x pro Lucas")
    assertNotNull(res2)
    assertEquals(300.0, res2!!.total_amount, 0.01)
    assertEquals(3, res2.installments)
    assertEquals("Lucas", res2.target_person)

    val res3 = parser.parseWithLocalFallback("Farmácia 75 pra Mãe")
    assertNotNull(res3)
    assertEquals(75.0, res3!!.total_amount, 0.01)
    assertEquals(1, res3.installments)
    assertEquals("Mãe", res3.target_person)
  }

  @Test
  fun testSharedBillSplitCalculation() {
    val totalAmount = 150.0
    val myShare = 50.0
    val otherShare = totalAmount - myShare

    assertEquals(100.0, otherShare, 0.001)
    assertEquals(totalAmount, myShare + otherShare, 0.001)

    val halfShare = totalAmount / 2.0
    assertEquals(75.0, halfShare, 0.001)
  }

  @Test
  fun testPersonDebtsCalculation() {
    val items = listOf(
      50.0 to false, // 50 pending
      75.0 to true,  // 75 paid
      30.0 to false  // 30 pending
    )
    val total = items.sumOf { it.first }
    val paid = items.filter { it.second }.sumOf { it.first }
    val pending = total - paid

    assertEquals(155.0, total, 0.001)
    assertEquals(75.0, paid, 0.001)
    assertEquals(80.0, pending, 0.001)
  }

  @Test
  fun testMyPersonIdConstant() {
    assertEquals(0L, com.example.data.model.MY_PERSON_ID)
    val myShare = com.example.data.model.BillParticipantShare(
      personName = "Eu",
      amount = 75.0,
      isMyShare = true
    )
    assertEquals(com.example.data.model.MY_PERSON_ID, myShare.personId)
  }
}

