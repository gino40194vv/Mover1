package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import com.example.mover.database.entities.LivelloTrofeo

@Entity(
    tableName = "trofeo_utente",
    primaryKeys = ["trofeoId", "utenteId"],
    foreignKeys = [
        ForeignKey(
            entity = Trofeo::class,
            parentColumns = ["id"],
            childColumns = ["trofeoId"],
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
        Index(value = ["trofeoId"]),
        Index(value = ["utenteId"]),
        Index(value = ["dataOttenimento"]),
        Index(value = ["posizione"])
    ]
)
data class TrofeoUtente(
    val trofeoId: String,
    val utenteId: String,
    val dataOttenimento: Date,
    val posizione: Int, // Posizione finale nella competizione
    val punteggio: Double, // Punteggio ottenuto
    val tempoCompletamento: Long? = null, // Tempo impiegato (se applicabile)
    val valoreRaggiunto: Double? = null, // Valore raggiunto (distanza, velocità, etc.)
    val livelloTrofeo: LivelloTrofeo, // Oro, Argento, Bronzo
    val certificato: String? = null, // URL del certificato digitale
    val condiviso: Boolean = false, // Se l'utente ha condiviso il trofeo
    val visibile: Boolean = true, // Se il trofeo è visibile nel profilo
    val note: String? = null,
    val datiPerformance: Map<String, Any> = emptyMap() // Dati aggiuntivi sulla performance
)

