package com.example.mover.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "fitness_freshness",
    indices = [
        Index(value = ["utenteId"]),
        Index(value = ["data"]),
        Index(value = ["utenteId", "data"], unique = true)
    ]
)
data class FitnessFreshness(
    @PrimaryKey
    val id: String,
    val utenteId: String,
    val data: Date,
    val ctl: Double, // Chronic Training Load (fitness)
    val atl: Double, // Acute Training Load (fatigue)
    val tsb: Double, // Training Stress Balance (freshness)
    val tss: Double, // Training Stress Score del giorno
    val intensityFactor: Double? = null, // IF del giorno
    val trainingLoad: Double, // Carico di allenamento
    val rampRate: Double? = null, // Tasso di incremento del carico
    val forma: FormaFisica = FormaFisica.NEUTRALE,
    val raccomandazione: String? = null, // Suggerimento per l'allenamento
    val note: String? = null
)

enum class FormaFisica {
    OTTIMA,      // TSB > 10
    BUONA,       // TSB 0-10
    NEUTRALE,    // TSB -10-0
    AFFATICATO,  // TSB -20--10
    SOVRALLENATO // TSB < -20
}