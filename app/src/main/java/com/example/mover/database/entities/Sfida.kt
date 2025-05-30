package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sfide")
data class Sfida(
    @PrimaryKey
    val id: String,
    val titolo: String,
    val descrizione: String,
    val tipo: TipoSfida, // DISTANZA, TEMPO, DISLIVELLO, ATTIVITA_COUNT
    val categoria: CategoriaSfida, // MENSILE, SETTIMANALE, SPECIALE, PERSONALE
    val obiettivo: Double, // Valore target (km, minuti, metri, numero)
    val unitaMisura: String, // "km", "minuti", "metri", "attività"
    val dataInizio: Date,
    val dataFine: Date,
    val isAttiva: Boolean = true,
    val isUfficiale: Boolean = false, // Sfide ufficiali Strava vs personali
    val badgeIcona: String? = null, // Nome dell'icona del badge
    val badgeColore: String? = null, // Colore del badge
    val premioDescrizione: String? = null, // Descrizione del premio
    val partecipanti: Int = 0, // Numero di partecipanti (per sfide ufficiali)
    val difficolta: DifficoltaSfida = DifficoltaSfida.MEDIA,
    val tags: List<String> = emptyList(), // Tag per categorizzare
    val sponsorNome: String? = null, // Nome del brand sponsor
    val sponsorLogo: String? = null, // Logo del brand sponsor
    val dataCreazione: Date = Date(),
    val creatoeDaUtente: Boolean = false // true se creata dall'utente
)

enum class TipoSfida {
    DISTANZA,           // Percorrere X km
    TEMPO,              // Allenarsi per X minuti
    DISLIVELLO,         // Scalare X metri di dislivello
    ATTIVITA_COUNT,     // Completare X attività
    VELOCITA_MEDIA,     // Mantenere velocità media di X km/h
    CALORIE,            // Bruciare X calorie
    GIORNI_CONSECUTIVI, // Allenarsi per X giorni consecutivi
    SPORT_SPECIFICO     // Sfida specifica per uno sport
}

enum class CategoriaSfida {
    MENSILE,     // Sfide che durano un mese
    SETTIMANALE, // Sfide settimanali
    GIORNALIERA, // Sfide giornaliere
    SPECIALE,    // Eventi speciali (Natale, Estate, etc.)
    PERSONALE,   // Obiettivi personali dell'utente
    CLUB,        // Sfide interne ai club
    GLOBALE      // Sfide globali della community
}

enum class DifficoltaSfida {
    FACILE,      // Obiettivi raggiungibili facilmente
    MEDIA,       // Richiede impegno moderato
    DIFFICILE,   // Sfida impegnativa
    ESTREMA      // Per atleti esperti
}