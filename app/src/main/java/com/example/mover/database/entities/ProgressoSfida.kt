package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "progresso_sfida",
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
        Index(value = ["dataAggiornamento"]),
        Index(value = ["sfidaId", "utenteId"], unique = true)
    ]
)
data class ProgressoSfida(
    @PrimaryKey
    val id: String,
    val sfidaId: String,
    val utenteId: String,
    val valoreAttuale: Double, // Valore corrente raggiunto
    val valoreObiettivo: Double, // Valore target della sfida
    val percentualeCompletamento: Double, // 0-100
    val isCompletata: Boolean = false,
    val dataInizio: Date,
    val dataCompletamento: Date? = null,
    val dataAggiornamento: Date,
    val giorniRimanenti: Int = 0,
    val posizione: Int? = null, // Posizione in classifica
    val puntiGuadagnati: Int = 0,
    val attivitaContribuite: List<String> = emptyList(), // ID delle attività che contribuiscono
    val milestone: List<MilestoneSfida> = emptyList(), // Traguardi intermedi raggiunti
    val note: String? = null
)

data class MilestoneSfida(
    val percentuale: Double, // 25%, 50%, 75%, etc.
    val valore: Double, // Valore raggiunto
    val dataRaggiungimento: Date,
    val badgeAssegnato: String? = null // ID del badge assegnato per questo milestone
)