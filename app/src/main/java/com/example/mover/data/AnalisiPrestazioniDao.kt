package com.example.mover.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AnalisiPrestazioniDao {
    
    // ==================== FITNESS & FRESHNESS ====================
    
    @Insert
    suspend fun inserisciFitnessFreshness(fitness: FitnessFreshness)
    
    @Update
    suspend fun aggiornaFitnessFreshness(fitness: FitnessFreshness)
    
    @Query("""
        SELECT * FROM fitness_freshness 
        WHERE atleta = :atleta 
        ORDER BY data DESC 
        LIMIT 1
    """)
    suspend fun getUltimaAnalisiFF(atleta: String): FitnessFreshness?
    
    @Query("""
        SELECT * FROM fitness_freshness 
        WHERE atleta = :atleta 
        AND data BETWEEN :dataInizio AND :dataFine
        ORDER BY data ASC
    """)
    suspend fun getAnalisiFFPerPeriodo(
        atleta: String, 
        dataInizio: Long, 
        dataFine: Long
    ): List<FitnessFreshness>
    
    @Query("""
        SELECT * FROM fitness_freshness 
        WHERE atleta = :atleta 
        ORDER BY data DESC 
        LIMIT :giorni
    """)
    suspend fun getAnalisiFFUltimiGiorni(atleta: String, giorni: Int = 30): List<FitnessFreshness>
    
    @Query("""
        SELECT AVG(fitness) FROM fitness_freshness 
        WHERE atleta = :atleta 
        AND data >= :dataInizio
    """)
    suspend fun getFitnessMediaPeriodo(atleta: String, dataInizio: Long): Float?
    
    @Query("""
        SELECT AVG(form) FROM fitness_freshness 
        WHERE atleta = :atleta 
        AND data >= :dataInizio
    """)
    suspend fun getFormaMediaPeriodo(atleta: String, dataInizio: Long): Float?
    
    // ==================== ANALISI ZONE ====================
    
    @Insert
    suspend fun inserisciAnalisiZone(analisi: AnalisiZone)
    
    @Update
    suspend fun aggiornaAnalisiZone(analisi: AnalisiZone)
    
    @Query("SELECT * FROM analisi_zone WHERE attivitaId = :attivitaId")
    suspend fun getAnalisiZonePerAttivita(attivitaId: Long): AnalisiZone?
    
    @Query("""
        SELECT * FROM analisi_zone 
        WHERE atleta = :atleta 
        AND data BETWEEN :dataInizio AND :dataFine
        ORDER BY data DESC
    """)
    suspend fun getAnalisiZonePerPeriodo(
        atleta: String, 
        dataInizio: Long, 
        dataFine: Long
    ): List<AnalisiZone>
    
    @Query("""
        SELECT 
            SUM(zonaCardiaca1) as zona1,
            SUM(zonaCardiaca2) as zona2,
            SUM(zonaCardiaca3) as zona3,
            SUM(zonaCardiaca4) as zona4,
            SUM(zonaCardiaca5) as zona5
        FROM analisi_zone 
        WHERE atleta = :atleta 
        AND data BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getDistribuzioneZoneCardiachePerPeriodo(
        atleta: String, 
        dataInizio: Long, 
        dataFine: Long
    ): DistribuzioneZoneCardiache
    
    @Query("""
        SELECT 
            SUM(zonaPotenza1) as zona1,
            SUM(zonaPotenza2) as zona2,
            SUM(zonaPotenza3) as zona3,
            SUM(zonaPotenza4) as zona4,
            SUM(zonaPotenza5) as zona5,
            SUM(zonaPotenza6) as zona6
        FROM analisi_zone 
        WHERE atleta = :atleta 
        AND data BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getDistribuzioneZonePotenzaPerPeriodo(
        atleta: String, 
        dataInizio: Long, 
        dataFine: Long
    ): DistribuzioneZonePotenza
    
    @Query("""
        SELECT AVG(intensitaMedia) FROM analisi_zone 
        WHERE atleta = :atleta 
        AND data BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getIntensitaMediaPerPeriodo(
        atleta: String, 
        dataInizio: Long, 
        dataFine: Long
    ): Float?
    
    // ==================== ANALISI PASSO GARA ====================
    
    @Insert
    suspend fun inserisciAnalisiPassoGara(analisi: AnalisiPassoGara)
    
    @Update
    suspend fun aggiornaAnalisiPassoGara(analisi: AnalisiPassoGara)
    
    @Query("SELECT * FROM analisi_passo_gara WHERE attivitaId = :attivitaId")
    suspend fun getAnalisiPassoGaraPerAttivita(attivitaId: Long): AnalisiPassoGara?
    
    @Query("""
        SELECT * FROM analisi_passo_gara 
        WHERE atleta = :atleta 
        AND distanzaGara = :distanza
        ORDER BY data DESC
    """)
    suspend fun getAnalisiPassoGaraPerDistanza(
        atleta: String, 
        distanza: Float
    ): List<AnalisiPassoGara>
    
    @Query("""
        SELECT * FROM analisi_passo_gara 
        WHERE atleta = :atleta 
        ORDER BY data DESC 
        LIMIT 1
    """)
    suspend fun getUltimaAnalisiPassoGara(atleta: String): AnalisiPassoGara?
    
    @Query("""
        SELECT MIN(paceMedio) FROM analisi_passo_gara 
        WHERE atleta = :atleta 
        AND distanzaGara = :distanza
    """)
    suspend fun getMigliorPacePerDistanza(atleta: String, distanza: Float): Float?
    
    @Query("""
        SELECT AVG(consistenza) FROM analisi_passo_gara 
        WHERE atleta = :atleta 
        AND data >= :dataInizio
    """)
    suspend fun getConsistenzaMediaPeriodo(atleta: String, dataInizio: Long): Float?
    
    // ==================== METRICHE PERFORMANCE ====================
    
    @Insert
    suspend fun inserisciMetrichePerformance(metriche: MetrichePerformance)
    
    @Update
    suspend fun aggiornaMetrichePerformance(metriche: MetrichePerformance)
    
    @Query("SELECT * FROM metriche_performance WHERE attivitaId = :attivitaId")
    suspend fun getMetrichePerformancePerAttivita(attivitaId: Long): MetrichePerformance?
    
    @Query("""
        SELECT * FROM metriche_performance 
        WHERE atleta = :atleta 
        AND tipoAttivita = :tipo
        AND data BETWEEN :dataInizio AND :dataFine
        ORDER BY data DESC
    """)
    suspend fun getMetrichePerformancePerTipoEPeriodo(
        atleta: String, 
        tipo: String,
        dataInizio: Long, 
        dataFine: Long
    ): List<MetrichePerformance>
    
    @Query("""
        SELECT AVG(performanceScore) FROM metriche_performance 
        WHERE atleta = :atleta 
        AND tipoAttivita = :tipo
        AND data >= :dataInizio
    """)
    suspend fun getPerformanceScoreMediaPerTipo(
        atleta: String, 
        tipo: String,
        dataInizio: Long
    ): Float?
    
    @Query("""
        SELECT AVG(efficiencyScore) FROM metriche_performance 
        WHERE atleta = :atleta 
        AND data >= :dataInizio
    """)
    suspend fun getEfficiencyScoreMediaPeriodo(atleta: String, dataInizio: Long): Float?
    
    @Query("""
        SELECT MAX(vo2Stimato) FROM metriche_performance 
        WHERE atleta = :atleta 
        AND vo2Stimato IS NOT NULL
    """)
    suspend fun getMigliorVO2Stimato(atleta: String): Float?
    
    // ==================== QUERY AVANZATE PER DASHBOARD ====================
    
    @Query("""
        SELECT 
            COUNT(*) as totaleAttivita,
            AVG(performanceScore) as performanceMedia,
            AVG(efficiencyScore) as efficienzaMedia,
            MAX(vo2Stimato) as migliorVO2
        FROM metriche_performance 
        WHERE atleta = :atleta 
        AND data BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getRiepilogoPerformancePerPeriodo(
        atleta: String, 
        dataInizio: Long, 
        dataFine: Long
    ): RiepilogoPerformance
    
    @Query("""
        SELECT 
            tipoAttivita,
            COUNT(*) as numeroAttivita,
            AVG(performanceScore) as performanceMedia,
            AVG(efficiencyScore) as efficienzaMedia
        FROM metriche_performance 
        WHERE atleta = :atleta 
        AND data >= :dataInizio
        GROUP BY tipoAttivita
        ORDER BY numeroAttivita DESC
    """)
    suspend fun getStatistichePerTipoAttivita(
        atleta: String, 
        dataInizio: Long
    ): List<StatisticheTipoAttivita>
    
    @Query("""
        SELECT 
            DATE(data/1000, 'unixepoch') as giorno,
            AVG(fitness) as fitnessMedia,
            AVG(fatigue) as faticaMedia,
            AVG(form) as formaMedia
        FROM fitness_freshness 
        WHERE atleta = :atleta 
        AND data BETWEEN :dataInizio AND :dataFine
        GROUP BY DATE(data/1000, 'unixepoch')
        ORDER BY giorno ASC
    """)
    suspend fun getTrendFitnessFreshnessPerPeriodo(
        atleta: String, 
        dataInizio: Long, 
        dataFine: Long
    ): List<TrendFitnessFreshness>
    
    // ==================== CONFRONTI E PROGRESSI ====================
    
    @Query("""
        SELECT 
            a1.data as data1,
            a1.performanceScore as score1,
            a2.data as data2,
            a2.performanceScore as score2,
            (a2.performanceScore - a1.performanceScore) as miglioramento
        FROM metriche_performance a1
        INNER JOIN metriche_performance a2 ON a1.atleta = a2.atleta 
        WHERE a1.atleta = :atleta
        AND a1.tipoAttivita = :tipo
        AND a2.tipoAttivita = :tipo
        AND a1.data < a2.data
        ORDER BY a2.data DESC
        LIMIT 10
    """)
    suspend fun getProgressiPerformancePerTipo(
        atleta: String,
        tipo: String
    ): List<ProgressoPerformance>
    
    @Query("""
        SELECT 
            distanzaGara,
            MIN(paceMedio) as migliorPace,
            MAX(consistenza) as migliorConsistenza,
            COUNT(*) as numeroTentativi
        FROM analisi_passo_gara 
        WHERE atleta = :atleta
        GROUP BY distanzaGara
        ORDER BY distanzaGara ASC
    """)
    suspend fun getRiepilogoPersonalBest(atleta: String): List<PersonalBest>
}

// Data classes per i risultati delle query
data class DistribuzioneZoneCardiache(
    val zona1: Long,
    val zona2: Long,
    val zona3: Long,
    val zona4: Long,
    val zona5: Long
)

data class DistribuzioneZonePotenza(
    val zona1: Long,
    val zona2: Long,
    val zona3: Long,
    val zona4: Long,
    val zona5: Long,
    val zona6: Long
)

data class RiepilogoPerformance(
    val totaleAttivita: Int,
    val performanceMedia: Float?,
    val efficienzaMedia: Float?,
    val migliorVO2: Float?
)

data class StatisticheTipoAttivita(
    val tipoAttivita: String,
    val numeroAttivita: Int,
    val performanceMedia: Float?,
    val efficienzaMedia: Float?
)

data class TrendFitnessFreshness(
    val giorno: String,
    val fitnessMedia: Float?,
    val faticaMedia: Float?,
    val formaMedia: Float?
)

data class ProgressoPerformance(
    val data1: Long,
    val score1: Float,
    val data2: Long,
    val score2: Float,
    val miglioramento: Float
)

data class PersonalBest(
    val distanzaGara: Float,
    val migliorPace: Float,
    val migliorConsistenza: Float,
    val numeroTentativi: Int
)
