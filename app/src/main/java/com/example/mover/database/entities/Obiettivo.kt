package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mover.data.Attività
import java.util.Date

@Entity(
    tableName = "obiettivi",
    foreignKeys = [
        ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["utenteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["utenteId"]),
        Index(value = ["tipo"]),
        Index(value = ["periodo"]),
        Index(value = ["isAttivo"])
    ]
)
data class Obiettivo(
    @PrimaryKey
    val id: String,
    val utenteId: String,
    val titolo: String,
    val descrizione: String? = null,
    val tipo: TipoObiettivo,
    val periodo: PeriodoObiettivo,
    val valoreTarget: Double, // Valore da raggiungere
    val unitaMisura: String, // "km", "minuti", "attività", "calorie"
    val progressoCorrente: Double = 0.0,
    val percentualeCompletamento: Double = 0.0, // 0-100%
    val dataInizio: Date,
    val dataFine: Date,
    val isAttivo: Boolean = true,
    val isCompletato: Boolean = false,
    val dataCompletamento: Date? = null,
    val sportSpecifico: String? = null, // Se l'obiettivo è per uno sport specifico
    val notificheAttive: Boolean = true,
    val frequenzaNotifiche: FrequenzaNotifica = FrequenzaNotifica.SETTIMANALE,
    val motivazione: String? = null, // Motivazione personale dell'utente
    val ricompensa: String? = null, // Ricompensa che l'utente si darà
    val difficolta: DifficoltaObiettivo = DifficoltaObiettivo.MEDIA,
    val isPublico: Boolean = false, // Se condividere con amici
    val dataCreazione: Date = Date(),
    val dataUltimaModifica: Date = Date()
)

@Entity(
    tableName = "progressi_obiettivo",
    foreignKeys = [
        ForeignKey(
            entity = Obiettivo::class,
            parentColumns = ["id"],
            childColumns = ["obiettivoId"],
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
        Index(value = ["obiettivoId"]),
        Index(value = ["attivitaId"]),
        Index(value = ["data"])
    ]
)
data class ProgressoObiettivo(
    @PrimaryKey
    val id: String,
    val obiettivoId: String,
    val attivitaId: String? = null, // Attività che ha contribuito
    val data: Date,
    val valoreAggiunto: Double, // Contributo di questa sessione
    val progressoTotale: Double, // Progresso totale fino a questa data
    val note: String? = null,
    val isManuale: Boolean = false // Se aggiunto manualmente dall'utente
)

// Entità per le notifiche motivazionali
@Entity(
    tableName = "notifiche_obiettivo",
    foreignKeys = [
        ForeignKey(
            entity = Obiettivo::class,
            parentColumns = ["id"],
            childColumns = ["obiettivoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["obiettivoId"]),
        Index(value = ["dataInvio"]),
        Index(value = ["tipo"])
    ]
)
data class NotificaObiettivo(
    @PrimaryKey
    val id: String,
    val obiettivoId: String,
    val tipo: TipoNotificaObiettivo,
    val titolo: String,
    val messaggio: String,
    val dataInvio: Date,
    val isLetta: Boolean = false,
    val azione: String? = null // Azione da eseguire quando cliccata
)

enum class TipoObiettivo {
    DISTANZA,           // Percorrere X km
    TEMPO,              // Allenarsi per X ore
    ATTIVITA_COUNT,     // Completare X attività
    CALORIE,            // Bruciare X calorie
    DISLIVELLO,         // Scalare X metri
    VELOCITA_MEDIA,     // Mantenere velocità media
    FREQUENZA,          // Allenarsi X volte
    PESO,               // Raggiungere peso target
    FREQUENZA_CARDIACA, // Migliorare FC a riposo
    PERSONALIZZATO      // Obiettivo personalizzato
}

enum class PeriodoObiettivo {
    GIORNALIERO,    // Obiettivi giornalieri
    SETTIMANALE,    // Obiettivi settimanali
    MENSILE,        // Obiettivi mensili
    TRIMESTRALE,    // Obiettivi trimestrali
    SEMESTRALE,     // Obiettivi semestrali
    ANNUALE,        // Obiettivi annuali
    PERSONALIZZATO  // Periodo personalizzato
}

enum class DifficoltaObiettivo {
    FACILE,         // Facilmente raggiungibile
    MEDIA,          // Richiede impegno costante
    DIFFICILE,      // Sfidante ma realistico
    AMBIZIOSA       // Molto sfidante
}

enum class FrequenzaNotifica {
    GIORNALIERA,    // Notifiche giornaliere
    SETTIMANALE,    // Notifiche settimanali
    MENSILE,        // Notifiche mensili
    MILESTONE,      // Solo ai traguardi importanti
    NESSUNA         // Nessuna notifica
}

enum class TipoNotificaObiettivo {
    PROMEMORIA,         // Promemoria per allenarsi
    PROGRESSO,          // Aggiornamento progresso
    INCORAGGIAMENTO,    // Messaggio motivazionale
    TRAGUARDO,          // Traguardo raggiunto
    COMPLETAMENTO,      // Obiettivo completato
    SCADENZA,           // Obiettivo in scadenza
    SUGGERIMENTO        // Suggerimento per migliorare
}
