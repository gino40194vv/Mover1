package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mover.data.Attività
import java.util.Date

@Entity(
    tableName = "kudos",
    foreignKeys = [
        ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["utenteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Attività::class,
            parentColumns = ["id"],
            childColumns = ["attivitaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["utenteId"]),
        Index(value = ["attivitaId"]),
        Index(value = ["utenteId", "attivitaId"], unique = true)
    ]
)
data class Kudos(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val utenteId: Long, // Chi ha dato il kudos
    val attivitaId: Long, // Attività che ha ricevuto il kudos
    
    val dataKudos: Date = Date(),
    val tipo: String = "NORMALE", // NORMALE, SUPER_KUDOS, REACTION
    val emoji: String? = null // Per reaction personalizzate
)
