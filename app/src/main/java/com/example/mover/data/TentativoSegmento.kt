package com.example.mover.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "tentativi_segmento",
    foreignKeys = [
        ForeignKey(
            entity = Segmento::class,
            parentColumns = ["id"],
            childColumns = ["segmentoId"],
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
        Index(value = ["segmentoId"]),
        Index(value = ["attivitaId"]),
        Index(value = ["dataOra"])
    ]
)
data class TentativoSegmento(
    @PrimaryKey
    val id: String,
    val segmentoId: String,
    val attivitaId: String,
    val utenteId: String,
    val tempoImpiegato: Long, // in millisecondi
    val velocitaMedia: Double, // km/h
    val velocitaMax: Double, // km/h
    val frequenzaCardiacaMedia: Int? = null,
    val frequenzaCardiacaMax: Int? = null,
    val cadenzaMedia: Int? = null,
    val potenzaMedia: Int? = null,
    val potenzaMax: Int? = null,
    val calorieBruciate: Int? = null,
    val dataOra: Date,
    val posizione: Int? = null, // Posizione in classifica al momento del tentativo
    val isPR: Boolean = false, // Personal Record
    val isKOM: Boolean = false, // King/Queen of Mountain
    val condizioni: String? = null, // Condizioni meteo/strada
    val note: String? = null
)