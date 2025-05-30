package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mover.data.Attività
import java.util.Date

@Entity(
    tableName = "partecipazioni_sfide",
    foreignKeys = [
        ForeignKey(
            entity = Sfida::class,
            parentColumns = ["id"],
            childColumns = ["sfidaId"],
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
        Index(value = ["sfidaId"]),
        Index(value = ["utenteId"]),
        Index(value = ["sfidaId", "utenteId"], unique = true)
    ]
)
data class PartecipazioneSfida(
    @PrimaryKey
    val id: String,
    val sfidaId: String,
    val utenteId: String,
    val dataIscrizione: Date,
    val progressoCorrente: Double = 0.0, // Progresso attuale verso l'obiettivo
    val percentualeCompletamento: Double = 0.0, // 0-100%
    val isCompletata: Boolean = false,
    val dataCompletamento: Date? = null,
    val posizioneClassifica: Int? = null, // Posizione nella classifica globale
    val punteggioOttenuto: Int = 0, // Punti guadagnati
    val tempoImpiegato: Long? = null, // Tempo per completare (in millisecondi)
    val notePersonali: String? = null, // Note dell'utente
    val condivisa: Boolean = false, // Se ha condiviso il completamento
    val dataUltimoAggiornamento: Date = Date()
)

// Entità per tracciare i progressi giornalieri
@Entity(
    tableName = "progressi_sfida",
    foreignKeys = [
        ForeignKey(
            entity = PartecipazioneSfida::class,
            parentColumns = ["id"],
            childColumns = ["partecipazioneId"],
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
        Index(value = ["partecipazioneId"]),
        Index(value = ["attivitaId"]),
        Index(value = ["data"])
    ]
)
data class ProgressoSfida(
    @PrimaryKey
    val id: String,
    val partecipazioneId: String,
    val attivitaId: String? = null, // Attività che ha contribuito al progresso
    val data: Date,
    val valoreAggiunto: Double, // Quanto ha contribuito questa sessione
    val progressoTotale: Double, // Progresso totale fino a questa data
    val note: String? = null
)
