package com.example.mover.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analisi_zone",
    foreignKeys = [
        ForeignKey(
            entity = Attività::class,
            parentColumns = ["id"],
            childColumns = ["attivitaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["attivitaId"]),
        Index(value = ["tipoZona"])
    ]
)
data class AnalisiZone(
    @PrimaryKey
    val id: String,
    val attivitaId: String,
    val tipoZona: TipoZona,
    val zona1Tempo: Long = 0, // in secondi
    val zona2Tempo: Long = 0,
    val zona3Tempo: Long = 0,
    val zona4Tempo: Long = 0,
    val zona5Tempo: Long = 0,
    val zona1Percentuale: Double = 0.0,
    val zona2Percentuale: Double = 0.0,
    val zona3Percentuale: Double = 0.0,
    val zona4Percentuale: Double = 0.0,
    val zona5Percentuale: Double = 0.0,
    val zonaMedia: Double = 0.0, // Zona media pesata
    val tempoInZonaTarget: Long = 0, // Tempo nella zona target
    val intensitaMedia: Double = 0.0, // Intensità media dell'allenamento
    val efficienza: Double = 0.0, // Efficienza dell'allenamento (0-100)
    val note: String? = null
)

enum class TipoZona {
    FREQUENZA_CARDIACA, // Zone basate su FC
    POTENZA,           // Zone basate su potenza
    PASSO,             // Zone basate su passo/velocità
    PERCEZIONE         // Zone basate su RPE (Rate of Perceived Exertion)
}