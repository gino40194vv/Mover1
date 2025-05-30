package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

enum class AzioneNotifica {
    VISUALIZZA_OBIETTIVO,
    AGGIORNA_PROGRESSO,
    CONDIVIDI_RISULTATO,
    MODIFICA_OBIETTIVO,
    ELIMINA_OBIETTIVO
}

@Entity(
    tableName = "notifiche_obiettivo",
    foreignKeys = [
        ForeignKey(
            entity = Obiettivo::class,
            parentColumns = ["id"],
            childColumns = ["obiettivoId"],
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
        Index(value = ["obiettivoId"]),
        Index(value = ["utenteId"]),
        Index(value = ["dataCreazione"]),
        Index(value = ["isLetta"])
    ]
)
data class NotificaObiettivo(
    @PrimaryKey
    val id: String,
    val obiettivoId: String,
    val utenteId: String,
    val tipo: TipoNotificaObiettivo,
    val titolo: String,
    val messaggio: String,
    val dataCreazione: Date,
    val dataScadenza: Date? = null,
    val isLetta: Boolean = false,
    val isImportante: Boolean = false,
    val azione: AzioneNotifica? = null, // Azione che l'utente può compiere
    val datiExtra: Map<String, Any> = emptyMap(), // Dati aggiuntivi per la notifica
    val iconaNotifica: String? = null,
    val coloreNotifica: String? = null
)

enum class TipoNotificaObiettivo {
    PROMEMORIA,           // Promemoria per allenarsi
    MILESTONE_RAGGIUNTO,  // Traguardo intermedio raggiunto
    OBIETTIVO_COMPLETATO, // Obiettivo completato
    SCADENZA_VICINA,      // Scadenza dell'obiettivo vicina
    OBIETTIVO_FALLITO,    // Obiettivo non raggiunto
    INCORAGGIAMENTO,      // Messaggio di incoraggiamento
    SUGGERIMENTO,         // Suggerimento per migliorare
    AGGIORNAMENTO,        // Aggiornamento sui progressi
    SFIDA_DISPONIBILE,    // Nuova sfida disponibile correlata
    BADGE_SBLOCCATO       // Badge sbloccato grazie all'obiettivo
}

data class AzioneNotifica(
    val tipo: TipoAzione,
    val testo: String, // Testo del pulsante/link
    val url: String? = null, // URL da aprire
    val attivitaId: String? = null, // ID attività da aprire
    val obiettivoId: String? = null, // ID obiettivo da aprire
    val sfidaId: String? = null // ID sfida da aprire
)

enum class TipoAzione {
    APRI_OBIETTIVO,
    APRI_ATTIVITA,
    APRI_SFIDA,
    APRI_URL,
    CREA_ALLENAMENTO,
    CONDIVIDI,
    IGNORA
}