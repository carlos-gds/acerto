package com.example.data.backup

import com.example.data.db.AppDatabase
import com.example.data.db.ExpenseDao
import com.example.data.db.InstallmentDao
import com.example.data.db.PersonDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Dedicated data access repository for creating and restoring full application backups.
 */
class BackupRepository(
    private val database: AppDatabase,
    private val personDao: PersonDao = database.personDao(),
    private val expenseDao: ExpenseDao = database.expenseDao(),
    private val installmentDao: InstallmentDao = database.installmentDao()
) {

    suspend fun createBackupPayload(): BackupPayload = withContext(Dispatchers.IO) {
        val people = personDao.getAllPeopleList().map { PersonBackupDto.fromEntity(it) }
        val expenses = expenseDao.getAllExpensesList().map { ExpenseBackupDto.fromEntity(it) }
        val installments = installmentDao.getAllInstallmentsList().map { InstallmentBackupDto.fromEntity(it) }

        BackupPayload(
            backupVersion = 1,
            appName = "Acerto",
            createdAt = System.currentTimeMillis(),
            people = people,
            expenses = expenses,
            installments = installments
        )
    }

    fun validateBackup(payload: BackupPayload): BackupValidationResult {
        return BackupValidator.validate(payload)
    }

    suspend fun restoreBackup(payload: BackupPayload): Result<Unit> = withContext(Dispatchers.IO) {
        val validation = BackupValidator.validate(payload)
        if (validation is BackupValidationResult.Invalid) {
            return@withContext Result.failure(IllegalArgumentException(validation.reason))
        }

        try {
            val peopleEntities = payload.people.map { it.toEntity() }
            val expenseEntities = payload.expenses.map { it.toEntity() }
            val installmentEntities = payload.installments.map { it.toEntity() }

            database.restoreDatabaseAtomic(
                people = peopleEntities,
                expenses = expenseEntities,
                installments = installmentEntities
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
