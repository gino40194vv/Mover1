package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mover.data.Attività
import java.util.Date

@Entity(
    tableName = "commenti",
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
        ),
        ForeignKey(
            entity = Commento::class,
            parentColumns = ["id"],
            childColumns = ["commentoPadreId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["utenteId"]),
        Index(value = ["attivitaId"]),
        Index(value = ["commentoPadreId"]),
        Index(value = ["dataCommento"])
    ]
)
data class Commento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val utenteId: Long, // Chi ha scritto il commento
    val attivitaId: Long, // Attività commentata
    val commentoPadreId: Long? = null, // Per risposte ai commenti
    
    val testo: String,
    val dataCommento: Date = Date(),
    val dataModifica: Date? = null,
    
    // Stato del commento
    val attivo: Boolean = true,
    val segnalato: Boolean = false,
    val likes: Int = 0,
    
    // Menzioni e hashtag
    val menzioni: String? = null, // JSON array di user ID menzionati
    val hashtags: String? = null // JSON array di hashtag
)
