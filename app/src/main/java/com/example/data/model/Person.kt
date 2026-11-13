package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Avatar da pessoa. NEUTRAL (padrão) mostra as iniciais sobre a cor escolhida. */
enum class PersonAvatar {
    NEUTRAL,
    MALE,
    FEMALE;

    companion object {
        /** Leitura tolerante: valores desconhecidos viram NEUTRAL. */
        fun from(value: String?): PersonAvatar =
            entries.firstOrNull { it.name == value } ?: NEUTRAL
    }
}

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val colorHex: String = "#10B981",
    val createdAt: Long = System.currentTimeMillis(),
    /** Guardado como texto (nome do enum [PersonAvatar]); adicionado na versão 3 do banco. */
    @ColumnInfo(defaultValue = "NEUTRAL")
    val avatar: String = PersonAvatar.NEUTRAL.name
)
