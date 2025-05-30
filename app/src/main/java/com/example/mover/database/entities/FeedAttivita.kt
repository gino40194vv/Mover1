package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mover.data.Attività
import java.util.Date

@Entity(
    tableName = "feed_attivita",
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
        Index(value = ["dataCreazione"]),
        Index(value = ["tipo"]),
        Index(value = ["priorita"])
    ]
)
data class FeedAttivita(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val utenteId: Long, // A chi mostrare nel feed
    val attivitaId: Long, // Attività da mostrare
    
    val tipo: String, // ATTIVITA_SEGUITI, KUDOS_RICEVUTI, COMMENTO_RICEVUTO, SEGMENTO_PR, CLUB_ATTIVITA, EVENTO_COMPLETATO
    val priorita: Int = 0, // Per ordinamento (più alto = più importante)
    
    val dataCreazione: Date = Date(),
    val dataScadenza: Date? = null, // Quando rimuovere dal feed
    
    // Metadati aggiuntivi
    val metadati: String? = null, // JSON con info extra (es. tipo di PR, nome segmento, etc.)
    
    // Stato
    val visualizzato: Boolean = false,
    val nascosto: Boolean = false,
    val segnalato: Boolean = false,
    
    // Engagement
    val likes: Int = 0,
    val commenti: Int = 0,
    val condivisioni: Int = 0,
    
    // Algoritmo feed
    val score: Float = 0f, // Score calcolato per ranking
    val fattoreDecadimento: Float = 1f // Per ridurre importanza nel tempo
)
