package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "utenti",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class Utente(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val username: String,
    val email: String,
    val nomeCompleto: String,
    val bio: String? = null,
    val immagineProfilo: String? = null,
    val copertinaProfilo: String? = null,
    
    // Informazioni personali
    val eta: Int? = null,
    val peso: Float? = null,
    val altezza: Float? = null,
    val sesso: String? = null, // M/F/Altro
    val citta: String? = null,
    val paese: String? = null,
    
    // Impostazioni privacy
    val profiloPublico: Boolean = true,
    val attivitaPubliche: Boolean = true,
    val mostraStatistiche: Boolean = true,
    val accettaFollower: Boolean = true,
    
    // Statistiche pubbliche
    val totalAttivita: Int = 0,
    val totalDistanza: Float = 0f,
    val totalTempo: Long = 0,
    val totalDislivello: Float = 0f,
    val totalKudos: Int = 0,
    val segmentiKOM: Int = 0,
    val segmentiQOM: Int = 0,
    
    // Livello e badge
    val livello: Int = 1,
    val puntiEsperienza: Int = 0,
    val badge: String? = null,
    
    // Timestamp
    val dataRegistrazione: Date = Date(),
    val ultimoAccesso: Date = Date(),
    val ultimaAttivita: Date? = null,
    
    // Stato account
    val attivo: Boolean = true,
    val verificato: Boolean = false,
    val premium: Boolean = false
)