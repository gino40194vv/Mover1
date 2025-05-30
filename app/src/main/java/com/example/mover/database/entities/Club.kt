package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "club",
    foreignKeys = [
        ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["creatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["creatorId"]),
        Index(value = ["nome"], unique = true),
        Index(value = ["pubblico"]),
        Index(value = ["dataCreazione"])
    ]
)
data class Club(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val nome: String,
    val descrizione: String,
    val creatorId: Long,
    
    // Immagini
    val immagineProfilo: String? = null,
    val immagineCopertina: String? = null,
    
    // Impostazioni
    val pubblico: Boolean = true,
    val richiedeApprovazione: Boolean = false,
    val maxMembri: Int? = null,
    
    // Localizzazione
    val citta: String? = null,
    val paese: String? = null,
    val coordinate: String? = null, // JSON lat,lng
    
    // Statistiche
    val totalMembri: Int = 1,
    val totalAttivita: Int = 0,
    val totalDistanza: Float = 0f,
    val totalDislivello: Float = 0f,
    
    // Sport principali
    val sportPrincipali: String? = null, // JSON array
    
    // Timestamp
    val dataCreazione: Date = Date(),
    val ultimaAttivita: Date = Date(),
    
    // Stato
    val attivo: Boolean = true,
    val verificato: Boolean = false,
    
    // Regole e info
    val regole: String? = null,
    val sitoWeb: String? = null,
    val social: String? = null // JSON object con link social
)