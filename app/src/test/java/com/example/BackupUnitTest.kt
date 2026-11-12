package com.example

import com.example.data.backup.BackupPayload
import com.example.data.backup.BackupSerializer
import com.example.data.backup.BackupValidationResult
import com.example.data.backup.BackupValidator
import com.example.data.backup.ExpenseBackupDto
import com.example.data.backup.InstallmentBackupDto
import com.example.data.backup.PersonBackupDto
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.MY_PERSON_ID
import com.example.data.model.Person
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupUnitTest {

    @Test
    fun testValidEmptyBackupSerializationAndValidation() {
        val payload = BackupPayload(
            backupVersion = 1,
            appName = "Quem Me Deve",
            createdAt = 1700000000000L,
            people = emptyList(),
            expenses = emptyList(),
            installments = emptyList()
        )

        val json = BackupSerializer.serialize(payload)
        val deserialized = BackupSerializer.deserialize(json)

        assertEquals(1, deserialized.backupVersion)
        assertEquals("Quem Me Deve", deserialized.appName)
        assertEquals(1700000000000L, deserialized.createdAt)
        assertTrue(deserialized.people.isEmpty())
        assertTrue(deserialized.expenses.isEmpty())
        assertTrue(deserialized.installments.isEmpty())

        val validation = BackupValidator.validate(deserialized)
        assertTrue(validation is BackupValidationResult.Valid)
    }

    @Test
    fun testFullPayloadSerializationAndPreservationOfPaidAtAndIds() {
        val person = PersonBackupDto(
            id = 101L,
            name = "Lucas Ferreira",
            phone = "11999999999",
            colorHex = "#3B82F6",
            createdAt = 1710000000000L
        )

        val expense = ExpenseBackupDto(
            id = 201L,
            title = "Tênis Nike",
            totalAmount = 300.0,
            type = "CARD_PURCHASE",
            totalInstallments = 2,
            isRecurring = false,
            startYear = 2026,
            startMonth = 5,
            personId = 101L,
            myShareAmount = 0.0,
            otherShareAmount = 300.0,
            notes = "Presente",
            createdAt = 1710000050000L
        )

        val installmentPaid = InstallmentBackupDto(
            id = 301L,
            expenseId = 201L,
            personId = 101L,
            personName = "Lucas Ferreira",
            expenseTitle = "Tênis Nike",
            expenseType = "CARD_PURCHASE",
            installmentNumber = 1,
            totalInstallments = 2,
            year = 2026,
            month = 5,
            amount = 150.0,
            isPaid = true,
            paidAt = 1715000000000L,
            isMyShare = false
        )

        val installmentUnpaid = InstallmentBackupDto(
            id = 302L,
            expenseId = 201L,
            personId = 101L,
            personName = "Lucas Ferreira",
            expenseTitle = "Tênis Nike",
            expenseType = "CARD_PURCHASE",
            installmentNumber = 2,
            totalInstallments = 2,
            year = 2026,
            month = 6,
            amount = 150.0,
            isPaid = false,
            paidAt = null,
            isMyShare = false
        )

        val payload = BackupPayload(
            backupVersion = 1,
            appName = "Quem Me Deve",
            createdAt = 1710000060000L,
            people = listOf(person),
            expenses = listOf(expense),
            installments = listOf(installmentPaid, installmentUnpaid)
        )

        val json = BackupSerializer.serialize(payload)
        val restored = BackupSerializer.deserialize(json)

        val validation = BackupValidator.validate(restored)
        assertTrue(validation is BackupValidationResult.Valid)

        // Verify entity conversions and field preservations
        val restoredPerson = restored.people[0].toEntity()
        assertEquals(101L, restoredPerson.id)
        assertEquals("Lucas Ferreira", restoredPerson.name)

        val restoredExpense = restored.expenses[0].toEntity()
        assertEquals(201L, restoredExpense.id)
        assertEquals(30000L, restoredExpense.totalAmount)
        assertEquals(ExpenseType.CARD_PURCHASE, restoredExpense.type)

        val inst1 = restored.installments[0].toEntity()
        assertEquals(301L, inst1.id)
        assertTrue(inst1.isPaid)
        assertEquals(1715000000000L, inst1.paidAt)

        val inst2 = restored.installments[1].toEntity()
        assertEquals(302L, inst2.id)
        assertTrue(!inst2.isPaid)
        assertNull(inst2.paidAt)
    }

    @Test
    fun testRejectsUnknownBackupVersion() {
        val payload = BackupPayload(
            backupVersion = 99,
            appName = "Quem Me Deve"
        )
        val validation = BackupValidator.validate(payload)
        assertTrue(validation is BackupValidationResult.Invalid)
        assertTrue((validation as BackupValidationResult.Invalid).reason.contains("Versão de backup não suportada"))
    }

    @Test
    fun testRejectsInvalidJsonAndEmptyString() {
        var emptyThrew = false
        try {
            BackupSerializer.deserialize("")
        } catch (e: IllegalArgumentException) {
            emptyThrew = true
        }
        assertTrue(emptyThrew)

        var malformedThrew = false
        try {
            BackupSerializer.deserialize("{ not json at all")
        } catch (e: IllegalArgumentException) {
            malformedThrew = true
        }
        assertTrue(malformedThrew)
    }

    @Test
    fun testRejectsInstallmentWithoutCorrespondingExpense() {
        val installment = InstallmentBackupDto(
            id = 500L,
            expenseId = 9999L, // Does not exist
            personId = MY_PERSON_ID,
            personName = "Eu",
            expenseTitle = "Internet",
            expenseType = "SHARED_BILL",
            installmentNumber = 1,
            totalInstallments = 1,
            year = 2026,
            month = 5,
            amount = 100.0,
            isPaid = false
        )
        val payload = BackupPayload(
            backupVersion = 1,
            installments = listOf(installment)
        )
        val validation = BackupValidator.validate(payload)
        assertTrue(validation is BackupValidationResult.Invalid)
        assertTrue((validation as BackupValidationResult.Invalid).reason.contains("despesa inexistente"))
    }

    @Test
    fun testRejectsInvalidMonth() {
        val expense = ExpenseBackupDto(
            id = 1L,
            title = "Luz",
            totalAmount = 80.0,
            type = "SHARED_BILL",
            totalInstallments = 1,
            startYear = 2026,
            startMonth = 13, // Invalid month!
            personId = MY_PERSON_ID
        )
        val payload = BackupPayload(
            backupVersion = 1,
            expenses = listOf(expense)
        )
        val validation = BackupValidator.validate(payload)
        assertTrue(validation is BackupValidationResult.Invalid)
        assertTrue((validation as BackupValidationResult.Invalid).reason.contains("mês de início inválido"))
    }

    @Test
    fun testRejectsNegativeAmounts() {
        val expense = ExpenseBackupDto(
            id = 1L,
            title = "Mercado",
            totalAmount = -50.0, // Negative!
            type = "SHARED_BILL",
            totalInstallments = 1,
            startYear = 2026,
            startMonth = 5,
            personId = MY_PERSON_ID
        )
        val payload = BackupPayload(
            backupVersion = 1,
            expenses = listOf(expense)
        )
        val validation = BackupValidator.validate(payload)
        assertTrue(validation is BackupValidationResult.Invalid)
        assertTrue((validation as BackupValidationResult.Invalid).reason.contains("valores negativos indevidos"))
    }
}
