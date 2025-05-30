package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "progresso_obiettivo",
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
        Index(value = ["dataAggiornamento"]),
        Index(value = ["obiettivoId", "utenteId"], unique = true)
    ]
)
data class ProgressoObiettivo(
    @PrimaryKey
    val id: String,
    val obiettivoId: String,
    val utenteId: String,
    val valoreAttuale: Double, // Valore corrente raggiunto
    val valoreObiettivo: Double, // Valore target dell'obiettivo
    val percentualeCompletamento: Double, // 0-100
    val isCompletato: Boolean = false,
    val dataInizio: Date,
    val dataCompletamento: Date? = null,
    val dataAggiornamento: Date,
    val giorniRimanenti: Int = 0,
    val mediaGiornaliera: Double = 0.0, // Media necessaria per raggiungere l'obiettivo
    val tendenza: TendenzaProgresso = TendenzaProgresso.STABILE,
    val previsione: Date? = null, // Data prevista di completamento
    val attivitaContribuite: List<String> = emptyList(), // ID delle attività che contribuiscono
    val storiciValori: List<ValoreStorico> = emptyList(), // Storico dei valori
    val note: String? = null
)

data class ValoreStorico(
    val data: Date,
    val valore: Double,
    val incremento: Double // Incremento rispetto al valore precedente
)

enum class TendenzaProgresso {
    CRESCENTE,    // In miglioramento
    STABILE,      // Costante
    DECRESCENTE,  // In peggioramento
    ACCELERATA,   // Miglioramento rapido
    RALLENTATA    // Miglioramento lento
}