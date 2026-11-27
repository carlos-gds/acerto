package com.example

import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
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

