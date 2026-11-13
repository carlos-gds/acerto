package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupPayload
import com.example.data.backup.BackupRepository
import com.example.data.backup.BackupSerializer
import com.example.data.backup.PersonBackupDto
import com.example.data.db.AppDatabase
import com.example.data.model.Person
import com.example.data.model.PersonAvatar
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** O avatar das pessoas entra no backup, sem quebrar backups antigos. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupAvatarTest {

    @Test
    fun serializacao_idaEVolta_preservaAvatar() {
        val payload = BackupPayload(
            people = listOf(PersonBackupDto(id = 1, name = "Ana", colorHex = "#EC4899", avatar = PersonAvatar.FEMALE.name))
        )
        val json = BackupSerializer.serialize(payload)
        assertTrue(Regex("\"avatar\"\\s*:\\s*\"FEMALE\"").containsMatchIn(json))
        val back = BackupSerializer.deserialize(json)
        assertEquals(PersonAvatar.FEMALE.name, back.people.first().avatar)
        assertEquals(PersonAvatar.FEMALE.name, back.people.first().toEntity().avatar)
    }

    @Test
    fun backupAntigoSemAvatar_restauraComoNeutro() {
        val legacyJson = """
            {"backupVersion":1,"appName":"Quem Me Deve","createdAt":1700000000000,
             "people":[{"id":7,"name":"Vó Maria","phone":"","colorHex":"#8B5CF6","createdAt":1700000000000}],
             "expenses":[],"installments":[]}
        """.trimIndent()
        val payload = BackupSerializer.deserialize(legacyJson)
        val person = payload.people.first()
        assertEquals("Vó Maria", person.name)
        assertEquals("#8B5CF6", person.colorHex)
        assertEquals(PersonAvatar.NEUTRAL.name, person.avatar)
    }

    @Test
    fun avatarDesconhecidoNoArquivo_viraNeutro() {
        val json = """
            {"backupVersion":1,"appName":"Acerto","createdAt":1,
             "people":[{"id":1,"name":"X","avatar":"ROBO"}],"expenses":[],"installments":[]}
        """.trimIndent()
        assertEquals(PersonAvatar.NEUTRAL.name, BackupSerializer.deserialize(json).people.first().avatar)
    }

    @Test
    fun exportarERestaurar_noBanco_preservaAvatar() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        try {
            db.personDao().insertPerson(Person(id = 5, name = "Carlos", colorHex = "#3B82F6", avatar = PersonAvatar.MALE.name))
            val repo = BackupRepository(db)
            val json = BackupSerializer.serialize(repo.createBackupPayload())

            db.personDao().deleteAllPeople()
            val result = repo.restoreBackup(BackupSerializer.deserialize(json))
            assertTrue(result.isSuccess)

            val restored = db.personDao().getPersonById(5)!!
            assertEquals(PersonAvatar.MALE.name, restored.avatar)
            assertEquals("#3B82F6", restored.colorHex)
        } finally {
            db.close()
        }
    }
}
