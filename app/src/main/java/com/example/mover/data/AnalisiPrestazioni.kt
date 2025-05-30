package com.example.mover.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Rappresenta l'analisi Fitness & Freshness (Forma e Freschezza)
 */
@Entity(
    tableName = "fitness_freshness",
    indices = [
        Index(value = ["atleta"]),
        Index(value = ["data"])
    ]
)
data class FitnessFreshness(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    val atleta: String, // ID dell'atleta
    val data: Long, // Data dell'analisi
    
    // Metriche Fitness & Freshness
    val fitness: Float, // CTL (Chronic Training Load) - Forma fisica a lungo termine
    val fatigue: Float, // ATL (Acute Training Load) - Fatica a breve termine  
    val form: Float, // TSB (Training Stress Balance) - Forma attuale
    
    // Training Stress Score
    val tssGiornaliero: Float, // TSS del giorno
    val tssSettimanale: Float, // TSS della settimana
    val tssMensile: Float, // TSS del mese
    
    // Metriche di carico
    val caricoAllenamento: Float, // Carico di allenamento giornaliero
    val intensitaMedia: Float, // Intensità media degli allenamenti
    val volumeSettimanale: Float, // Volume settimanale (ore o km)
    
    // Raccomandazioni
    val raccomandazione: String, // "Riposo", "Allenamento leggero", "Allenamento intenso", etc.
    val rischioBurnout: Float, // Rischio di sovrallenamento (0-1)
    val formaOttimale: Boolean, // Se l'atleta è in forma ottimale
    
    // Previsioni
    val previsione7Giorni: Float, // Previsione forma tra 7 giorni
    val previsione14Giorni: Float, // Previsione forma tra 14 giorni
    val previsione30Giorni: Float // Previsione forma tra 30 giorni
)

/**
 * Rappresenta l'analisi delle zone cardiache e di potenza
 */
@Entity(
    tableName = "analisi_zone",
    indices = [
        Index(value = ["attivitaId"]),
        Index(value = ["atleta"]),
        Index(value = ["data"])
    ]
)
data class AnalisiZone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    val attivitaId: Long, // Riferimento all'attività
    val atleta: String,
    val data: Long,
    
    // Zone cardiache (tempo in millisecondi)
    val zonaCardiaca1: Long = 0, // Zona 1: 50-60% FCMax (Recupero attivo)
    val zonaCardiaca2: Long = 0, // Zona 2: 60-70% FCMax (Base aerobica)
    val zonaCardiaca3: Long = 0, // Zona 3: 70-80% FCMax (Aerobico)
    val zonaCardiaca4: Long = 0, // Zona 4: 80-90% FCMax (Soglia anaerobica)
    val zonaCardiaca5: Long = 0, // Zona 5: 90-100% FCMax (Neuromuscolare)
    
    // Zone di potenza (tempo in millisecondi) - per ciclismo
    val zonaPotenza1: Long = 0, // Zona 1: <55% FTP (Recupero attivo)
    val zonaPotenza2: Long = 0, // Zona 2: 55-75% FTP (Resistenza)
    val zonaPotenza3: Long = 0, // Zona 3: 75-90% FTP (Tempo)
    val zonaPotenza4: Long = 0, // Zona 4: 90-105% FTP (Soglia)
    val zonaPotenza5: Long = 0, // Zona 5: 105-120% FTP (VO2Max)
    val zonaPotenza6: Long = 0, // Zona 6: >120% FTP (Anaerobico)
    
    // Zone di pace (tempo in millisecondi) - per corsa
    val zonaPace1: Long = 0, // Zona 1: Recupero
    val zonaPace2: Long = 0, // Zona 2: Base
    val zonaPace3: Long = 0, // Zona 3: Aerobico
    val zonaPace4: Long = 0, // Zona 4: Soglia
    val zonaPace5: Long = 0, // Zona 5: VO2Max
    val zonaPace6: Long = 0, // Zona 6: Anaerobico
    
    // Metriche derivate
    val intensitaMedia: Float, // Intensità media dell'allenamento
    val efficienza: Float, // Efficienza dell'allenamento
    val bilanciamento: Float, // Bilanciamento tra zone
    val qualitaAllenamento: String // "Recupero", "Base", "Intenso", "Misto"
)

/**
 * Rappresenta l'analisi del passo gara e simulazioni di performance
 */
@Entity(
    tableName = "analisi_passo_gara",
    indices = [
        Index(value = ["attivitaId"]),
        Index(value = ["atleta"]),
        Index(value = ["distanzaGara"])
    ]
)
data class AnalisiPassoGara(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    val attivitaId: Long,
    val atleta: String,
    val data: Long,
    
    // Distanza di gara analizzata
    val distanzaGara: Float, // 5K, 10K, 21K, 42K, etc.
    val tipoGara: String, // "5K", "10K", "Mezza Maratona", "Maratona", etc.
    
    // Analisi del passo
    val paceMedio: Float, // Pace medio in min/km
    val paceTarget: Float, // Pace target per la gara
    val variabilitaPace: Float, // Variabilità del passo
    val consistenza: Float, // Consistenza del passo (0-1)
    
    // Split analysis
    val split1km: Float, // Tempo primo km
    val split5km: Float, // Tempo primi 5km
    val split10km: Float, // Tempo primi 10km
    val splitMeta: Float, // Tempo prima metà
    val splitFinale: Float, // Tempo seconda metà
    
    // Strategia di gara
    val strategia: String, // "Negative Split", "Positive Split", "Even Pace"
    val fadingIndex: Float, // Indice di calo (-1 a 1)
    val kickFinale: Boolean, // Se c'è stato uno sprint finale
    
    // Previsioni performance
    val tempoPrevistoVelocita: Long, // Tempo previsto basato su velocità
    val tempoPrevistoHR: Long, // Tempo previsto basato su FC
    val tempoPrevistoRPE: Long, // Tempo previsto basato su sforzo percepito
    val tempoMiglioreStimato: Long, // Miglior tempo stimato
    
    // Raccomandazioni
    val raccomandazioniTattiche: String, // Raccomandazioni tattiche
    val raccomandazioniAllenamento: String, // Raccomandazioni di allenamento
    val puntiForza: String, // Punti di forza identificati
    val areeeMiglioramento: String // Aree di miglioramento
)

/**
 * Rappresenta le metriche di performance avanzate
 */
@Entity(
    tableName = "metriche_performance",
    indices = [
        Index(value = ["attivitaId"]),
        Index(value = ["atleta"]),
        Index(value = ["data"])
    ]
)
data class MetrichePerformance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    val attivitaId: Long,
    val atleta: String,
    val data: Long,
    val tipoAttivita: String,
    
    // Metriche di potenza (ciclismo)
    val potenzaNormalizzata: Float? = null, // Normalized Power (NP)
    val intensityFactor: Float? = null, // Intensity Factor (IF)
    val trainingStressScore: Float? = null, // Training Stress Score (TSS)
    val variabilityIndex: Float? = null, // Variability Index (VI)
    val efficiencyFactor: Float? = null, // Efficiency Factor (EF)
    
    // Metriche di corsa
    val gradeAdjustedPace: Float? = null, // Pace aggiustato per pendenza
    val runningEffectiveness: Float? = null, // Efficacia della corsa
    val verticalOscillation: Float? = null, // Oscillazione verticale
    val groundContactTime: Float? = null, // Tempo di contatto al suolo
    val strideLength: Float? = null, // Lunghezza del passo
    val runningIndex: Float? = null, // Indice di corsa
    
    // Metriche cardiache avanzate
    val hrVariability: Float? = null, // Variabilità della frequenza cardiaca
    val hrDrift: Float? = null, // Deriva della frequenza cardiaca
    val hrEfficiency: Float? = null, // Efficienza cardiaca
    val recoveryHR: Int? = null, // FC di recupero dopo 1 minuto
    val maxHRReached: Int? = null, // FC massima raggiunta
    
    // Metriche metaboliche
    val vo2Stimato: Float? = null, // VO2 stimato
    val consumoEnergetico: Float? = null, // Consumo energetico totale
    val contributoAerobico: Float? = null, // % contributo aerobico
    val contributoAnaerobico: Float? = null, // % contributo anaerobico
    val lattato: Float? = null, // Lattato stimato
    
    // Metriche ambientali
    val heatStress: Float? = null, // Stress da calore
    val altitudeEffect: Float? = null, // Effetto dell'altitudine
    val windEffect: Float? = null, // Effetto del vento
    val temperatureEffect: Float? = null, // Effetto della temperatura
    
    // Punteggi complessivi
    val performanceScore: Float? = null, // Punteggio performance (0-100)
    val effortScore: Float? = null, // Punteggio sforzo (0-100)
    val efficiencyScore: Float? = null, // Punteggio efficienza (0-100)
    val qualityScore: Float? = null // Punteggio qualità allenamento (0-100)
)