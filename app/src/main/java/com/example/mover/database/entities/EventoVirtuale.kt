package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "eventi_virtuali",
    foreignKeys = [
        ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["creatorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Club::class,
            parentColumns = ["id"],
            childColumns = ["clubId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["creatorId"]),
        Index(value = ["clubId"]),
        Index(value = ["dataInizio"]),
        Index(value = ["dataFine"]),
        Index(value = ["tipo"]),
        Index(value = ["pubblico"])
    ]
)
data class EventoVirtuale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val nome: String,
    val descrizione: String,
    val creatorId: Long,
    val clubId: Long? = null, // Se è un evento di club
    
    // Timing
    val dataInizio: Date,
    val dataFine: Date,
    val dataCreazione: Date = Date(),
    
    // Tipo evento
    val tipo: String, // VIRTUAL_RUN, VIRTUAL_RIDE, CHALLENGE, RACE, TRAINING
    val categoria: String? = null, // DISTANCE, TIME, ELEVATION, SPEED
    
    // Obiettivi
    val obiettivoDistanza: Float? = null,
    val obiettivoTempo: Long? = null,
    val obiettivoDislivello: Float? = null,
    val obiettivoVelocita: Float? = null,
    
    // Regole
    val sportConsentiti: String, // JSON array di sport
    val regole: String? = null,
    val premiazione: String? = null,
    
    // Impostazioni
    val pubblico: Boolean = true,
    val richiedeApprovazione: Boolean = false,
    val maxPartecipanti: Int? = null,
    val quotaIscrizione: Float? = null,
    
    // Immagini
    val immagineProfilo: String? = null,
    val immagineCopertina: String? = null,
    
    // Statistiche
    val totalPartecipanti: Int = 0,
    val totalAttivita: Int = 0,
    val totalDistanza: Float = 0f,
    val totalTempo: Long = 0,
    
    // Stato
    val stato: String = "PROGRAMMATO", // PROGRAMMATO, ATTIVO, COMPLETATO, CANCELLATO
    val attivo: Boolean = true,
    
    // Sponsor e premi
    val sponsor: String? = null, // JSON object
    val premi: String? = null, // JSON object
    val hashtag: String? = null
)