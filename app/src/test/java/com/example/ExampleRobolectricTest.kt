package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BillParticipantShare
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Acerto", appName)
  }

  @Test
  fun `bill split between other registered people without me`() {
    val participants = listOf(
      BillParticipantShare(personId = 1L, personName = "Carlos", amount = 60.0, isMyShare = false),
      BillParticipantShare(personId = 2L, personName = "Ana", amount = 60.0, isMyShare = false)
    )
    assertEquals(2, participants.size)
    assertEquals(120.0, participants.sumOf { it.amount }, 0.001)
    // "Eu" does not participate in this split
    assertEquals(0.0, participants.filter { it.isMyShare }.sumOf { it.amount }, 0.001)
    // Full 120.0 is owed by Carlos and Ana
    assertEquals(120.0, participants.filter { !it.isMyShare }.sumOf { it.amount }, 0.001)
  }

  @Test
  fun `bill split with MY_PERSON_ID participant`() {
    val participants = listOf(
      BillParticipantShare(
        personId = com.example.data.model.MY_PERSON_ID,
        personName = "Eu",
        amount = 50.0,
        isMyShare = true
      ),
      BillParticipantShare(
        personId = 1L,
        personName = "Carlos",
        amount = 50.0,
        isMyShare = false
      )
    )
    val myShare = participants.filter { it.isMyShare }.sumOf { it.amount }
    val othersShare = participants.filter { !it.isMyShare }.sumOf { it.amount }
    assertEquals(50.0, myShare, 0.001)
    assertEquals(50.0, othersShare, 0.001)
    assertEquals(com.example.data.model.MY_PERSON_ID, participants.first { it.isMyShare }.personId)
    assertEquals(0L, com.example.data.model.MY_PERSON_ID)
  }
}
