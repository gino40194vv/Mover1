package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "partecipazioni_evento",
    foreignKeys = [
        ForeignKey(
            entity = EventoVirtuale::class,
            parentColumns = ["id"],
            childColumns = ["eventoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["utenteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["eventoId"]),
        Index(value = ["utenteId"]),
        Index(value = ["eventoId", "utenteId"], unique = true),
        Index(value = ["dataIscrizione"]),
        Index(value = ["stato"])
    ]
)
data class PartecipazioneEvento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val eventoId: Long,
    val utenteId: Long,
    
    val dataIscrizione: Date = Date(),
    val stato: String = "ISCRITTO", // ISCRITTO, PARTECIPATO, COMPLETATO, RITIRATO
    
    // Risultati
    val attivitaCompletate: Int = 0,
    val distanzaTotale: Float = 0f,
    val tempoTotale: Long = 0,
    val dislivelloTotale: Float = 0f,
    val velocitaMedia: Float = 0f,
    val velocitaMassima: Float = 0f,
    
    // Posizione in classifica
    val posizioneGenerale: Int? = null,
    val posizioneCategoria: Int? = null,
    val categoria: String? = null, // Categoria di età/sesso
    
    // Obiettivo raggiunto
    val obiettivoRaggiunto: Boolean = false,
    val percentualeCompletamento: Float = 0f,
    
    // Premi e riconoscimenti
    val badge: String? = null,
    val premio: String? = null,
    val certificato: String? = null,
    
    // Note
    val note: String? = null,
    val condiviso: Boolean = false,
    
    // Timestamp
    val dataCompletamento: Date? = null,
    val ultimaAttivita: Date? = null
)