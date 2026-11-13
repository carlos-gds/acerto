package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.Person
import com.example.data.model.PersonAvatar
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Migration 2 -> 3 (avatar da pessoa): um banco real no formato da versão 2 é aberto
 * pelo Room atual; nenhuma pessoa, despesa ou parcela pode se perder.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PersonAvatarMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration-avatar-test.db"
    private var room: AppDatabase? = null

    @Before
    fun createVersion2Database() {
        context.deleteDatabase(dbName)
        val file = context.getDatabasePath(dbName).apply { parentFile?.mkdirs() }
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        // Esquema da versão 2 (o mesmo que o Room criava antes do avatar)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS people (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, phone TEXT NOT NULL, colorHex TEXT NOT NULL, createdAt INTEGER NOT NULL)"
        )
        db.execSQL(
            """
            CREATE TABLE expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, totalAmount INTEGER NOT NULL,
                type TEXT NOT NULL, totalInstallments INTEGER NOT NULL, isRecurring INTEGER NOT NULL,
                startYear INTEGER NOT NULL, startMonth INTEGER NOT NULL, personId INTEGER NOT NULL,
                myShareAmount INTEGER NOT NULL, otherShareAmount INTEGER NOT NULL, notes TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE installments (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, expenseId INTEGER NOT NULL, personId INTEGER NOT NULL,
                personName TEXT NOT NULL, expenseTitle TEXT NOT NULL, expenseType TEXT NOT NULL,
                installmentNumber INTEGER NOT NULL, totalInstallments INTEGER NOT NULL, year INTEGER NOT NULL,
                month INTEGER NOT NULL, amount INTEGER NOT NULL, isPaid INTEGER NOT NULL, paidAt INTEGER,
                isMyShare INTEGER NOT NULL,
                FOREIGN KEY(expenseId) REFERENCES expenses(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_installments_expenseId ON installments(expenseId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_installments_year_month ON installments(year, month)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_installments_personId ON installments(personId)")
        db.execSQL("INSERT INTO people VALUES (7, 'Vó Maria', '11987654321', '#8B5CF6', 1700000000000)")
        db.execSQL(
            "INSERT INTO expenses VALUES (3, 'Tênis', 36000, 'CARD_PURCHASE', 6, 0, 2026, 11, 7, 0, 36000, 'obs', 1700000000000)"
        )
        db.execSQL(
            "INSERT INTO installments VALUES (9, 3, 7, 'Vó Maria', 'Tênis', 'CARD_PURCHASE', 1, 6, 2026, 11, 6000, 0, NULL, 0)"
        )
        db.version = 2
        db.close()
    }

    @After
    fun tearDown() {
        room?.close()
        context.deleteDatabase(dbName)
    }

    private fun openMigrated(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()
            .also { room = it }

    @Test
    fun migracao_preservaDadosEUsaAvatarNeutro() = runBlocking {
        val db = openMigrated()
        val people = db.personDao().getAllPeopleList()
        assertEquals(1, people.size)
        val p = people.first()
        assertEquals(7L, p.id)
        assertEquals("Vó Maria", p.name)
        assertEquals("11987654321", p.phone)
        assertEquals("#8B5CF6", p.colorHex)
        assertEquals(PersonAvatar.NEUTRAL.name, p.avatar)

        val expenses = db.expenseDao().getAllExpensesList()
        assertEquals(1, expenses.size)
        assertEquals(36000L, expenses.first().totalAmount)

        val installments = db.installmentDao().getAllInstallmentsList()
        assertEquals(1, installments.size)
        assertEquals(6000L, installments.first().amount)
        assertEquals(7L, installments.first().personId)
    }

    @Test
    fun avatarEscolhidoEPersistido() = runBlocking {
        val db = openMigrated()
        val id = db.personDao().insertPerson(
            Person(name = "Ana", colorHex = "#EC4899", avatar = PersonAvatar.FEMALE.name)
        )
        val saved = db.personDao().getPersonById(id)!!
        assertEquals(PersonAvatar.FEMALE, PersonAvatar.from(saved.avatar))
        assertEquals("#EC4899", saved.colorHex)

        db.personDao().updatePerson(saved.copy(avatar = PersonAvatar.MALE.name))
        assertEquals(PersonAvatar.MALE.name, db.personDao().getPersonById(id)!!.avatar)
    }

    @Test
    fun valorDesconhecidoDeAvatarViraNeutro() {
        assertEquals(PersonAvatar.NEUTRAL, PersonAvatar.from("ALIEN"))
        assertEquals(PersonAvatar.NEUTRAL, PersonAvatar.from(null))
        assertEquals(PersonAvatar.FEMALE, PersonAvatar.from("FEMALE"))
    }
}
