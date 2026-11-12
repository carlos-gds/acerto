package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InstallmentItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM installments WHERE year = :year AND month = :month ORDER BY isMyShare ASC, personName ASC")
    fun getInstallmentsForMonth(year: Int, month: Int): Flow<List<InstallmentItem>>

    @Query("SELECT * FROM installments WHERE personId = :personId ORDER BY year ASC, month ASC, installmentNumber ASC")
    fun getInstallmentsForPerson(personId: Long): Flow<List<InstallmentItem>>

    @Query("SELECT * FROM installments WHERE expenseId = :expenseId ORDER BY installmentNumber ASC")
    fun getInstallmentsForExpense(expenseId: Long): Flow<List<InstallmentItem>>

    @Query("SELECT * FROM installments ORDER BY year ASC, month ASC")
    fun getAllInstallments(): Flow<List<InstallmentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(items: List<InstallmentItem>)

    @Update
    suspend fun updateInstallment(item: InstallmentItem)

    @Update
    suspend fun updateInstallments(items: List<InstallmentItem>)

    @Query("UPDATE installments SET isPaid = :isPaid, paidAt = :paidAt WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, isPaid: Boolean, paidAt: Long?)

    @Query("DELETE FROM installments WHERE expenseId = :expenseId")
    suspend fun deleteByExpenseId(expenseId: Long)

    @Query("DELETE FROM installments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM installments WHERE id = :id")
    suspend fun getInstallmentById(id: Long): InstallmentItem?

    @Query("SELECT * FROM installments WHERE expenseId = :expenseId ORDER BY installmentNumber ASC")
    suspend fun getInstallmentListByExpenseId(expenseId: Long): List<InstallmentItem>

    @Query("SELECT COUNT(*) FROM installments WHERE expenseId = :expenseId")
    suspend fun getCountByExpenseId(expenseId: Long): Int

    @Query("UPDATE installments SET personName = :newName WHERE personId = :personId")
    suspend fun updatePersonName(personId: Long, newName: String)

    @Query("DELETE FROM installments WHERE personId = :personId")
    suspend fun deleteByPersonId(personId: Long)

    @Query("SELECT * FROM installments ORDER BY year ASC, month ASC, installmentNumber ASC")
    suspend fun getAllInstallmentsList(): List<InstallmentItem>

    @Query("DELETE FROM installments")
    suspend fun deleteAllInstallments()
}
