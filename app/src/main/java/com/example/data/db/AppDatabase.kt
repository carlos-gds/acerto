package com.example.data.db

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.withTransaction
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.Person

class Converters {
    @TypeConverter
    fun fromExpenseType(type: ExpenseType): String = type.name

    @TypeConverter
    fun toExpenseType(value: String): ExpenseType = try {
        ExpenseType.valueOf(value)
    } catch (e: Exception) {
        ExpenseType.CARD_PURCHASE
    }
}

@Database(
    entities = [
        Person::class,
        Expense::class,
        InstallmentItem::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun installmentDao(): InstallmentDao

    /**
     * Atomically restores the database replacing all people, expenses, and installments
     * in a single transaction. If any error occurs, all changes are rolled back.
     */
    suspend fun restoreDatabaseAtomic(
        people: List<Person>,
        expenses: List<Expense>,
        installments: List<InstallmentItem>
    ) {
        withTransaction {
            installmentDao().deleteAllInstallments()
            expenseDao().deleteAllExpenses()
            personDao().deleteAllPeople()

            personDao().insertPeople(people)
            expenseDao().insertExpenses(expenses)
            installmentDao().insertInstallments(installments)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "divide_facil_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Versão 3: avatar da pessoa. Só adiciona a coluna com padrão NEUTRAL;
         * nenhuma tabela é recriada, então pessoas, despesas e parcelas ficam intactas.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE people ADD COLUMN avatar TEXT NOT NULL DEFAULT 'NEUTRAL'")
            }
        }

        /** Converts legacy REAL amounts to integer cents without destructive migration. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("PRAGMA foreign_keys=OFF")

                database.execSQL("ALTER TABLE expenses RENAME TO expenses_legacy")
                database.execSQL("""
                    CREATE TABLE expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        totalAmount INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        totalInstallments INTEGER NOT NULL,
                        isRecurring INTEGER NOT NULL,
                        startYear INTEGER NOT NULL,
                        startMonth INTEGER NOT NULL,
                        personId INTEGER NOT NULL,
                        myShareAmount INTEGER NOT NULL,
                        otherShareAmount INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO expenses
                    SELECT id, title, CAST(ROUND(totalAmount * 100.0) AS INTEGER), type,
                        totalInstallments, isRecurring, startYear, startMonth, personId,
                        CAST(ROUND(myShareAmount * 100.0) AS INTEGER),
                        CAST(ROUND(otherShareAmount * 100.0) AS INTEGER), notes, createdAt
                    FROM expenses_legacy
                """.trimIndent())
                database.execSQL("DROP TABLE expenses_legacy")

                database.execSQL("ALTER TABLE installments RENAME TO installments_legacy")
                database.execSQL("""
                    CREATE TABLE installments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        expenseId INTEGER NOT NULL,
                        personId INTEGER NOT NULL,
                        personName TEXT NOT NULL,
                        expenseTitle TEXT NOT NULL,
                        expenseType TEXT NOT NULL,
                        installmentNumber INTEGER NOT NULL,
                        totalInstallments INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        amount INTEGER NOT NULL,
                        isPaid INTEGER NOT NULL,
                        paidAt INTEGER,
                        isMyShare INTEGER NOT NULL,
                        FOREIGN KEY(expenseId) REFERENCES expenses(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO installments
                    SELECT id, expenseId, personId, personName, expenseTitle, expenseType,
                        installmentNumber, totalInstallments, year, month,
                        CAST(ROUND(amount * 100.0) AS INTEGER), isPaid, paidAt, isMyShare
                    FROM installments_legacy
                """.trimIndent())
                database.execSQL("DROP TABLE installments_legacy")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_installments_expenseId ON installments(expenseId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_installments_year_month ON installments(year, month)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_installments_personId ON installments(personId)")
                database.execSQL("PRAGMA foreign_keys=ON")
            }
        }
    }
}
