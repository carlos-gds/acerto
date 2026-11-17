package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.Person
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Operações de vários passos do Repository rodando dentro de transações do Room. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RepositoryTransactionTest {

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

    private suspend fun ana(): Person {
        val id = repo.insertPerson("Ana")
        return db.personDao().getPersonById(id)!!
    }

    @Test
    fun compraNoCartao_criaDespesaEParcelas() = runBlocking {
        val id = repo.addCardPurchase("Fone", 300.0, ana(), 3, 2026, 1)
        assertEquals(3, db.installmentDao().getCountByExpenseId(id))
    }

    @Test
    fun excluirDespesa_removeDespesaEParcelas() = runBlocking {
        val id = repo.addCardPurchase("Fone", 300.0, ana(), 3, 2026, 1)
        repo.deleteExpense(id)
        assertNull(db.expenseDao().getExpenseById(id))
        assertEquals(0, db.installmentDao().getCountByExpenseId(id))
    }

    @Test
    fun editarCompra_refazParcelas() = runBlocking {
        val person = ana()
        val id = repo.addCardPurchase("Fone", 300.0, person, 3, 2026, 1)
        repo.updateExpenseDetails(id, "Fone novo", 600.0, person, 6, 2026, 1)
        assertEquals(6, db.installmentDao().getCountByExpenseId(id))
        assertEquals("Fone novo", db.expenseDao().getExpenseById(id)!!.title)
    }

    @Test
    fun excluirPessoa_removePessoaESuasParcelas() = runBlocking {
        val person = ana()
        repo.addCardPurchase("Fone", 300.0, person, 3, 2026, 1)
        repo.deletePerson(person)
        assertNull(db.personDao().getPersonById(person.id))
        assertEquals(0, db.installmentDao().getAllInstallmentsList().count { it.personId == person.id })
    }

    @Test
    fun desfazerExclusao_restauraCompraEParcelas() = runBlocking {
        val id = repo.addCardPurchase("Fone", 300.0, ana(), 3, 2026, 1)
        val first = db.installmentDao().getInstallmentListByExpenseId(id).first()
        repo.setInstallmentPaid(first.id, true)
        val snapshot = repo.snapshotExpense(id)!!
        repo.deleteExpense(id)
        assertNull(db.expenseDao().getExpenseById(id))

        repo.restoreExpense(snapshot)
        val back = db.installmentDao().getInstallmentListByExpenseId(id)
        assertEquals(3, back.size)
        assertEquals(snapshot.installments.map { it.id }.sorted(), back.map { it.id }.sorted())
        assertEquals(true, back.first { it.id == first.id }.isPaid)
    }

    @Test
    fun pagamentoParcial_divideParcelaSemMudarOTotal() = runBlocking {
        val id = repo.addCardPurchase("Fone", 300.0, ana(), 3, 2026, 1)
        val first = db.installmentDao().getInstallmentListByExpenseId(id).first { it.installmentNumber == 1 }
        assertEquals(true, repo.registerPartialPayment(first.id, 4000)) // R$ 40 de R$ 100

        val parts = db.installmentDao().getInstallmentListByExpenseId(id).filter { it.installmentNumber == 1 }
        assertEquals(2, parts.size)
        assertEquals(10000L, parts.sumOf { it.amount })
        assertEquals(4000L, parts.single { it.isPaid }.amount)
        assertEquals(6000L, parts.single { !it.isPaid }.amount)
    }

    @Test
    fun pagamentoParcial_recusaValorInteiroOuZero() = runBlocking {
        val id = repo.addCardPurchase("Fone", 300.0, ana(), 3, 2026, 1)
        val first = db.installmentDao().getInstallmentListByExpenseId(id).first()
        assertEquals(false, repo.registerPartialPayment(first.id, 10000))
        assertEquals(false, repo.registerPartialPayment(first.id, 0))
        assertEquals(3, db.installmentDao().getCountByExpenseId(id))
    }

    @Test
    fun repositorioSemBanco_continuaFuncionando() = runBlocking {
        val semTx = FinanceRepository(db.personDao(), db.expenseDao(), db.installmentDao())
        val id = semTx.addCardPurchase("Livro", 100.0, ana(), 2, 2026, 1)
        assertEquals(2, db.installmentDao().getCountByExpenseId(id))
    }
}
