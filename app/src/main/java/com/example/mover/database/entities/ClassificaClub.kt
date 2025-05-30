package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "classifiche_club",
    foreignKeys = [
        ForeignKey(
            entity = Club::class,
            parentColumns = ["id"],
            childColumns = ["clubId"],
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
        Index(value = ["clubId"]),
        Index(value = ["utenteId"]),
        Index(value = ["periodo"]),
        Index(value = ["tipo"]),
        Index(value = ["clubId", "utenteId", "periodo", "tipo"], unique = true)
    ]
)
data class ClassificaClub(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val clubId: Long,
    val utenteId: Long,
    
    val periodo: String, // SETTIMANALE, MENSILE, ANNUALE, ALL_TIME
    val tipo: String, // DISTANZA, TEMPO, DISLIVELLO, ATTIVITA, KUDOS
    
    // Periodo specifico
    val anno: Int,
    val mese: Int? = null,
    val settimana: Int? = null,
    
    // Valori
    val valore: Float, // Valore della metrica
    val posizione: Int, // Posizione in classifica
    val totalPartecipanti: Int, // Totale partecipanti alla classifica
    
    // Dettagli
    val attivitaContate: Int = 0,
    val migliorAttivita: Long? = null, // ID dell'attività migliore
    val giorniAttivi: Int = 0,
    
    // Cambiamenti
    val posizionePrec: Int? = null,
    val valorePrec: Float? = null,
    val variazione: Int = 0, // +/- posizioni
    
    // Timestamp
    val dataAggiornamento: Date = Date(),
    val dataCalcolo: Date = Date(),
    
    // Badge e riconoscimenti
    val badge: String? = null, // PRIMO, PODIO, TOP_10, MIGLIORAMENTO
    val punti: Int = 0 // Punti assegnati per la posizione
)