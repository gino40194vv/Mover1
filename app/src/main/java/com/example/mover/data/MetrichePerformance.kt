package com.example.mover.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "metriche_performance",
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
        Index(value = ["dataCalcolo"]),
        Index(value = ["tipoMetrica"])
    ]
)
data class MetrichePerformance(
    @PrimaryKey
    val id: String,
    val attivitaId: String,
    val dataCalcolo: Date,
    val tipoMetrica: TipoMetricaPerformance,
    
    // Metriche di potenza
    val potenzaMedia: Int? = null, // Watt
    val potenzaNormalizzata: Int? = null, // NP - Normalized Power
    val intensityFactor: Double? = null, // IF - Intensity Factor
    val trainingStressScore: Double? = null, // TSS
    val variabilityIndex: Double? = null, // VI - Variability Index
    val efficiencyFactor: Double? = null, // EF - Efficiency Factor
    
    // Metriche di frequenza cardiaca
    val frequenzaCardiacaMedia: Int? = null,
    val frequenzaCardiacaMax: Int? = null,
    val hrReserveMedia: Double? = null, // % di FC Reserve
    val hrZone1Tempo: Long = 0, // secondi in zona 1
    val hrZone2Tempo: Long = 0,
    val hrZone3Tempo: Long = 0,
    val hrZone4Tempo: Long = 0,
    val hrZone5Tempo: Long = 0,
    
    // Metriche di passo/velocità
    val passoMedio: Double? = null, // min/km
    val velocitaMedia: Double? = null, // km/h
    val velocitaMax: Double? = null, // km/h
    val cadenzaMedia: Int? = null, // passi/min o RPM
    val lunghezzaPassoMedia: Double? = null, // metri
    
    // Metriche di efficienza
    val economiaCorsa: Double? = null, // ml/kg/km
    val indiceEfficienza: Double? = null, // 0-100
    val rapportoPotenzaVelocita: Double? = null, // W/(km/h)
    val rapportoFCVelocita: Double? = null, // bpm/(km/h)
    
    // Metriche di carico
    val caricoAllenamento: Double? = null, // Training Load
    val rpe: Int? = null, // Rate of Perceived Exertion (1-10)
    val impulsoAllenamento: Double? = null, // TRIMP
    
    // Metriche ambientali
    val temperaturaMedia: Double? = null, // °C
    val umidita: Double? = null, // %
    val altitudine: Double? = null, // metri
    val dislivelloPositivo: Double? = null, // metri
    val dislivelloNegativo: Double? = null, // metri
    
    // Metriche comparative
    val miglioramentoPersonale: Boolean = false,
    val percentilePerformance: Double? = null, // 0-100
    val punteggioPerformance: Double? = null, // Score calcolato
    
    val note: String? = null
)

enum class TipoMetricaPerformance {
    POTENZA,
    FREQUENZA_CARDIACA,
    PASSO_VELOCITA,
    EFFICIENZA,
    CARICO_ALLENAMENTO,
    AMBIENTALE,
    COMPARATIVA,
    COMPLETA // Include tutte le metriche disponibili
}