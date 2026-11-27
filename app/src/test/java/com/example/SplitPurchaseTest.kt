package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupPayload
import com.example.data.backup.BackupSerializer
import com.example.data.backup.ExpenseBackupDto
import com.example.data.db.AppDatabase
import com.example.data.model.Person
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Compra dividida: uma parte por pessoa, ligadas pelo mesmo groupId. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SplitPurchaseTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: FinanceRepository

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = FinanceRepository(db.personDao(), db.expenseDao(), db.installmentDao(), db)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun pessoa(nome: String): Person = db.personDao().getPersonById(repo.insertPerson(nome))!!

    @Test
    fun geladeiraDe2000Em10x_cadaUmaPaga100PorMes() = runBlocking {
        val mae = pessoa("Mãe")
        val vo = pessoa("Vó")
        val groupId = repo.addCardPurchaseGroup("Geladeira", listOf(mae to 1000.0, vo to 1000.0), 10, 2026, 11)

        val parts = repo.getExpensesByGroupId(groupId)
        assertEquals(2, parts.size)
        assertTrue(parts.all { it.groupId == groupId && it.title == "Geladeira" && it.totalInstallments == 10 })
        assertEquals(listOf(mae.id, vo.id), parts.map { it.personId })

        parts.forEach { e ->
            val items = db.installmentDao().getInstallmentListByExpenseId(e.id)
            assertEquals(10, items.size)
            assertTrue(items.all { it.amount == 10000L && it.personId == e.personId })
        }
    }

    @Test
    fun divisaoPersonalizada_120e80() = runBlocking {
        val gid = repo.addCardPurchaseGroup("Geladeira", listOf(pessoa("Mãe") to 1200.0, pessoa("Vó") to 800.0), 10, 2026, 11)
        val (m, v) = repo.getExpensesByGroupId(gid)
        assertEquals(12000L, db.installmentDao().getInstallmentListByExpenseId(m.id).first().amount)
        assertEquals(8000L, db.installmentDao().getInstallmentListByExpenseId(v.id).first().amount)
    }

    @Test
    fun compraDeUmaPessoa_ficaSemGrupo() = runBlocking {
        val gid = repo.addCardPurchaseGroup("Fone", listOf(pessoa("Ana") to 300.0), 3, 2026, 11)
        assertNull(db.expenseDao().getExpenseById(gid)!!.groupId)
    }

    @Test
    fun editarUmaParte_mantemALigacao() = runBlocking {
        val mae = pessoa("Mãe")
        val gid = repo.addCardPurchaseGroup("Geladeira", listOf(mae to 1000.0, pessoa("Vó") to 1000.0), 10, 2026, 11)
        val first = repo.getExpensesByGroupId(gid).first()
        repo.updateExpenseDetails(first.id, "Geladeira nova", 1100.0, mae, 10, 2026, 11)
        assertEquals(gid, db.expenseDao().getExpenseById(first.id)!!.groupId)
    }

    @Test
    fun excluirAsPartes_apagaTudo() = runBlocking {
        val gid = repo.addCardPurchaseGroup("Geladeira", listOf(pessoa("Mãe") to 1000.0, pessoa("Vó") to 1000.0), 10, 2026, 11)
        repo.getExpensesByGroupId(gid).forEach { repo.deleteExpense(it.id) }
        assertTrue(repo.getExpensesByGroupId(gid).isEmpty())
        assertTrue(db.installmentDao().getAllInstallmentsList().isEmpty())
    }

    @Test
    fun backup_guardaALigacao() {
        val dto = ExpenseBackupDto(
            id = 1, title = "Geladeira", totalAmount = 1000.0, type = "CARD_PURCHASE",
            totalInstallments = 10, startYear = 2026, startMonth = 11, personId = 2, groupId = 1
        )
        val solta = dto.copy(id = 3, groupId = null)
        val back = BackupSerializer.deserialize(BackupSerializer.serialize(BackupPayload(expenses = listOf(dto, solta))))
        assertEquals(1L, back.expenses[0].groupId)
        assertNull(back.expenses[1].groupId)
        assertEquals(1L, back.expenses[0].toEntity().groupId)
    }
}
