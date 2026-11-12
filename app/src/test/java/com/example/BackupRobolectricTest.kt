package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupPayload
import com.example.data.backup.BackupRepository
import com.example.data.backup.ExpenseBackupDto
import com.example.data.backup.InstallmentBackupDto
import com.example.data.backup.PersonBackupDto
import com.example.data.db.AppDatabase
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.MY_PERSON_ID
import com.example.data.model.Person
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var backupRepository: BackupRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupRepository = BackupRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testExportAndRestoreCycle() = runBlocking {
        // 1. Insert initial dummy data
        val person = Person(id = 1L, name = "Mariana", phone = "11988887777", colorHex = "#EC4899")
        db.personDao().insertPerson(person)

        val expense = Expense(
            id = 10L,
            title = "Sofá",
            totalAmount = 1200.0,
            type = ExpenseType.CARD_PURCHASE,
            totalInstallments = 3,
            startYear = 2026,
            startMonth = 1,
            personId = 1L
        )
        db.expenseDao().insertExpense(expense)

        val inst1 = InstallmentItem(
            id = 100L,
            expenseId = 10L,
            personId = 1L,
            personName = "Mariana",
            expenseTitle = "Sofá",
            expenseType = ExpenseType.CARD_PURCHASE,
            installmentNumber = 1,
            totalInstallments = 3,
            year = 2026,
            month = 1,
            amount = 400.0,
            isPaid = true,
            paidAt = 1705000000000L
        )
        db.installmentDao().insertInstallments(listOf(inst1))

        // 2. Export to payload
        val exported = backupRepository.createBackupPayload()
        assertEquals(1, exported.people.size)
        assertEquals(1, exported.expenses.size)
        assertEquals(1, exported.installments.size)
        assertEquals("Mariana", exported.people[0].name)
        assertTrue(exported.installments[0].isPaid)
        assertEquals(1705000000000L, exported.installments[0].paidAt)

        // 3. Prepare replacement backup payload
        val newPerson = PersonBackupDto(id = 2L, name = "Roberto", phone = "11911112222", colorHex = "#10B981")
        val newExpense = ExpenseBackupDto(
            id = 20L,
            title = "Geladeira",
            totalAmount = 2000.0,
            type = "CARD_PURCHASE",
            totalInstallments = 1,
            startYear = 2026,
            startMonth = 2,
            personId = 2L
        )
        val newInst = InstallmentBackupDto(
            id = 200L,
            expenseId = 20L,
            personId = 2L,
            personName = "Roberto",
            expenseTitle = "Geladeira",
            expenseType = "CARD_PURCHASE",
            installmentNumber = 1,
            totalInstallments = 1,
            year = 2026,
            month = 2,
            amount = 2000.0,
            isPaid = false
        )

        val restorePayload = BackupPayload(
            backupVersion = 1,
            people = listOf(newPerson),
            expenses = listOf(newExpense),
            installments = listOf(newInst)
        )

        // 4. Restore: should completely replace Mariana with Roberto
        val result = backupRepository.restoreBackup(restorePayload)
        assertTrue(result.isSuccess)

        val currentPeople = db.personDao().getAllPeopleList()
        assertEquals(1, currentPeople.size)
        assertEquals("Roberto", currentPeople[0].name)
        assertEquals(2L, currentPeople[0].id)

        val currentExpenses = db.expenseDao().getAllExpensesList()
        assertEquals(1, currentExpenses.size)
        assertEquals("Geladeira", currentExpenses[0].title)

        val currentInstallments = db.installmentDao().getAllInstallmentsList()
        assertEquals(1, currentInstallments.size)
        assertEquals(200L, currentInstallments[0].id)
    }

    @Test
    fun testRepeatedImportDoesNotDuplicate() = runBlocking {
        val person = PersonBackupDto(id = 5L, name = "Ana", phone = "", colorHex = "#8B5CF6")
        val expense = ExpenseBackupDto(
            id = 50L,
            title = "Internet",
            totalAmount = 100.0,
            type = "SHARED_BILL",
            totalInstallments = 1,
            startYear = 2026,
            startMonth = 3,
            personId = MY_PERSON_ID
        )
        val inst = InstallmentBackupDto(
            id = 500L,
            expenseId = 50L,
            personId = MY_PERSON_ID,
            personName = "Eu",
            expenseTitle = "Internet",
            expenseType = "SHARED_BILL",
            installmentNumber = 1,
            totalInstallments = 1,
            year = 2026,
            month = 3,
            amount = 100.0,
            isPaid = true
        )

        val payload = BackupPayload(
            backupVersion = 1,
            people = listOf(person),
            expenses = listOf(expense),
            installments = listOf(inst)
        )

        // First import
        backupRepository.restoreBackup(payload)
        // Second import of same backup
        backupRepository.restoreBackup(payload)

        assertEquals(1, db.personDao().getAllPeopleList().size)
        assertEquals(1, db.expenseDao().getAllExpensesList().size)
        assertEquals(1, db.installmentDao().getAllInstallmentsList().size)
    }

    @Test
    fun testInvalidBackupDoesNotModifyDatabase() = runBlocking {
        // Insert initial person
        db.personDao().insertPerson(Person(id = 1L, name = "Carlos"))
        assertEquals(1, db.personDao().getAllPeopleList().size)

        // Attempt restore with invalid payload (referencing nonexistent expense)
        val invalidPayload = BackupPayload(
            backupVersion = 1,
            people = listOf(PersonBackupDto(id = 2L, name = "Bruno")),
            expenses = emptyList(),
            installments = listOf(
                InstallmentBackupDto(
                    id = 1L,
                    expenseId = 999L, // Nonexistent
                    personId = 2L,
                    personName = "Bruno",
                    expenseTitle = "Erro",
                    expenseType = "CARD_PURCHASE",
                    installmentNumber = 1,
                    totalInstallments = 1,
                    year = 2026,
                    month = 1,
                    amount = 50.0,
                    isPaid = false
                )
            )
        )

        val result = backupRepository.restoreBackup(invalidPayload)
        assertTrue(result.isFailure)

        // Carlos must still be there, untouched!
        val people = db.personDao().getAllPeopleList()
        assertEquals(1, people.size)
        assertEquals("Carlos", people[0].name)
    }
}
