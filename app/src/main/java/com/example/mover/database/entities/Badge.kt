package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey
    val id: String,
    val nome: String,
    val descrizione: String,
    val icona: String, // Nome del file icona
    val colore: String, // Colore del badge (#RRGGBB)
    val categoria: CategoriaBadge,
    val tipo: TipoBadge,
    val condizioni: CondizioniOttenimento,
    val rarità: RaritaBadge = RaritaBadge.COMUNE,
    val punti: Int = 0, // Punti assegnati per ottenere questo badge
    val isAttivo: Boolean = true,
    val dataCreazione: Date = Date(),
    val ordinamento: Int = 0 // Per ordinare i badge
)

@Entity(
    tableName = "badge_utente",
    primaryKeys = ["badgeId", "utenteId"],
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Badge::class,
            parentColumns = ["id"],
            childColumns = ["badgeId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["utenteId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["badgeId"]),
        androidx.room.Index(value = ["utenteId"]),
        androidx.room.Index(value = ["dataOttenimento"])
    ]
)
data class BadgeUtente(
    val badgeId: String,
    val utenteId: String,
    val dataOttenimento: Date,
    val progressoOttenimento: String? = null, // JSON con dettagli su come è stato ottenuto
    val condiviso: Boolean = false, // Se l'utente ha condiviso il badge
    val visibile: Boolean = true // Se il badge è visibile nel profilo
)

enum class CategoriaBadge {
    DISTANZA,        // Badge per distanze percorse
    TEMPO,           // Badge per tempo di allenamento
    VELOCITA,        // Badge per velocità raggiunte
    DISLIVELLO,      // Badge per dislivelli scalati
    FREQUENZA,       // Badge per costanza negli allenamenti
    SPORT_SPECIFICO, // Badge specifici per sport
    SOCIALE,         // Badge per attività social (kudos, commenti, etc.)
    SFIDE,           // Badge per completamento sfide
    MILESTONE,       // Badge per traguardi importanti
    STAGIONALE,      // Badge stagionali o eventi speciali
    COMMUNITY,       // Badge per contributi alla community
    ESPLORATORE      // Badge per esplorare nuovi luoghi
}

enum class TipoBadge {
    AUTOMATICO,      // Ottenuto automaticamente al raggiungimento condizioni
    SFIDA,           // Ottenuto completando una sfida specifica
    MANUALE,         // Assegnato manualmente (admin)
    EVENTO,          // Badge per partecipazione eventi
    ANNIVERSARIO,    // Badge per anniversari (1 anno app, etc.)
    ACHIEVEMENT      // Achievement speciali
}

enum class RaritaBadge {
    COMUNE,          // Facile da ottenere
    NON_COMUNE,      // Richiede un po' di impegno
    RARO,            // Difficile da ottenere
    EPICO,           // Molto difficile
    LEGGENDARIO      // Estremamente raro
}

// Classe per definire le condizioni di ottenimento
data class CondizioniOttenimento(
    val tipo: TipoCondizione,
    val valore: Double,
    val unitaMisura: String? = null,
    val sportSpecifico: String? = null,
    val periodoTempo: String? = null, // "giornaliero", "settimanale", "mensile", "totale"
    val condizioniAggiuntive: Map<String, Any> = emptyMap()
)

enum class TipoCondizione {
    DISTANZA_TOTALE,        // Percorrere X km in totale
    DISTANZA_SINGOLA,       // Percorrere X km in una singola attività
    TEMPO_TOTALE,           // Allenarsi per X ore in totale
    TEMPO_SINGOLO,          // Allenarsi per X ore in una singola sessione
    VELOCITA_MEDIA,         // Raggiungere velocità media di X km/h
    VELOCITA_MASSIMA,       // Raggiungere velocità massima di X km/h
    DISLIVELLO_TOTALE,      // Scalare X metri in totale
    DISLIVELLO_SINGOLO,     // Scalare X metri in una singola attività
    ATTIVITA_CONSECUTIVE,   // Completare X attività consecutive
    GIORNI_CONSECUTIVI,     // Allenarsi per X giorni consecutivi
    NUMERO_ATTIVITA,        // Completare X attività
    KUDOS_RICEVUTI,         // Ricevere X kudos
    COMMENTI_LASCIATI,      // Lasciare X commenti
    SFIDE_COMPLETATE,       // Completare X sfide
    CLUB_PARTECIPAZIONI,    // Partecipare a X club
    PRIMI_POSTI,            // Ottenere X primi posti in segmenti
    SEGMENTI_COMPLETATI,    // Completare X segmenti
    CALORIE_BRUCIATE,       // Bruciare X calorie
    FREQUENZA_CARDIACA,     // Raggiungere X bpm
    CADENZA,                // Mantenere X passi/min o RPM
    POTENZA                 // Raggiungere X watt
}