package com.example.mover.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
// Import delle entità necessarie
import com.example.mover.data.FitnessFreshness
import com.example.mover.data.AnalisiZone
import com.example.mover.data.AnalisiPassoGara
import com.example.mover.data.MetrichePerformance

@Dao
interface AnalisiPrestazioniDao {
    
    // ==================== FITNESS & FRESHNESS ====================
    
    @Insert
    suspend fun inserisciFitnessFreshness(ctl: FitnessFreshness)
    
    @Update
    suspend fun aggiornaFitnessFreshness(ctl: FitnessFreshness)
    
    @Query("""
        SELECT * FROM fitness_freshness 
        WHERE utenteId = :utenteId 
        ORDER BY data DESC 
        LIMIT 1
    """)
    suspend fun getUltimaAnalisiFF(utenteId: String): FitnessFreshness?
    
    @Query("""
        SELECT * FROM fitness_freshness 
        WHERE utenteId = :utenteId 
        AND data BETWEEN :dataInizio AND :dataFine
        ORDER BY data ASC
    """)
    suspend fun getAnalisiFFPerPeriodo(
        utenteId: String, 
        dataInizio: Long, 
        dataFine: Long
    ): List<FitnessFreshness>
    
    @Query("""
        SELECT * FROM fitness_freshness 
        WHERE utenteId = :utenteId 
        ORDER BY data DESC 
        LIMIT :giorni
    """)
    suspend fun getAnalisiFFUltimiGiorni(utenteId: String, giorni: Int = 30): List<FitnessFreshness>
    
    @Query("""
        SELECT AVG(ctl) FROM fitness_freshness 
        WHERE utenteId = :utenteId 
        AND data >= :dataInizio
    """)
    suspend fun getFitnessMediaPeriodo(utenteId: String, dataInizio: Long): Float?
    
    @Query("""
        SELECT AVG(forma) FROM fitness_freshness 
        WHERE utenteId = :utenteId 
        AND data >= :dataInizio
    """)
    suspend fun getFormaMediaPeriodo(utenteId: String, dataInizio: Long): Float?
    
    // ==================== ANALISI ZONE ====================
    
    @Insert
    suspend fun inserisciAnalisiZone(analisi: AnalisiZone)
    
    @Update
    suspend fun aggiornaAnalisiZone(analisi: AnalisiZone)
    
    @Query("SELECT * FROM analisi_zone WHERE attivitaId = :attivitaId")
    suspend fun getAnalisiZonePerAttivita(attivitaId: Long): AnalisiZone?
    
    @Query("""
        SELECT * FROM analisi_zone 
         
        AND data BETWEEN :dataInizio AND :dataFine
        ORDER BY data DESC
    """)
    suspend fun getAnalisiZonePerPeriodo(
        dataInizio: Long, 
        dataFine: Long
    ): List<AnalisiZone>
    
    @Query("""
        SELECT 
            SUM(zona1Tempo) as zona1,
            SUM(zona2Tempo) as zona2,
            SUM(zona3Tempo) as zona3,
            SUM(zona4Tempo) as zona4,
            SUM(zona5Tempo) as zona5
        FROM analisi_zone 
         
        AND data BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getDistribuzioneZoneCardiachePerPeriodo(
        utenteId: String, 
        dataInizio: Long, 
        dataFine: Long
    ): DistribuzioneZoneCardiache
    
    @Query("""
        SELECT 
            SUM(zona1Tempo) as zona1,
            SUM(zona2Tempo) as zona2,
            SUM(zona3Tempo) as zona3,
            SUM(zona4Tempo) as zona4,
            SUM(zona5Tempo) as zona5,
            SUM(zonaPotenza6) as zona6
        FROM analisi_zone 
         
        AND data BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getDistribuzioneZonePotenzaPerPeriodo(
        utenteId: String, 
        dataInizio: Long, 
        dataFine: Long
    ): DistribuzioneZonePotenza
    
    @Query("""
        SELECT AVG(intensitaMedia) FROM analisi_zone 
         
        AND data BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getIntensitaMediaPerPeriodo(
        utenteId: String, 
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
         
        AND distanzaGara = :distanza
        ORDER BY data DESC
    """)
    suspend fun getAnalisiPassoGaraPerDistanza(
        utenteId: String, 
        distanza: Float
    ): List<AnalisiPassoGara>
    
    @Query("""
        SELECT * FROM analisi_passo_gara 
         
        ORDER BY data DESC 
        LIMIT 1
    """)
    suspend fun getUltimaAnalisiPassoGara(): AnalisiPassoGara?
    
    @Query("""
        SELECT MIN(paceMedio) FROM analisi_passo_gara 
         
        AND distanzaGara = :distanza
    """)
    suspend fun getMigliorPacePerDistanza(distanza: Float): Float?
    
    @Query("""
        SELECT AVG(consistenza) FROM analisi_passo_gara 
         
        AND data >= :dataInizio
    """)
    suspend fun getConsistenzaMediaPeriodo(utenteId: String, dataInizio: Long): Float?
    
    // ==================== METRICHE PERFORMANCE ====================
    
    @Insert
    suspend fun inserisciMetrichePerformaance(metriche: MetrichePerformaance)
    
    @Update
    suspend fun aggiornaMetrichePerformaance(metriche: MetrichePerformaance)
    
    @Query("SELECT * FROM metriche_performaance WHERE attivitaId = :attivitaId")
    suspend fun getMetrichePerformaancePerAttivita(attivitaId: Long): MetrichePerformaance?
    
    @Query("""
        SELECT * FROM metriche_performaance 
         
        AND tipoAttivita = :tipo
        AND data BETWEEN :dataInizio AND :dataFine
        ORDER BY data DESC
    """)
    suspend fun getMetrichePerformaancePerTipoEPeriodo(
        utenteId: String, 
        tipo: String,
        dataInizio: Long, 
        dataFine: Long
    ): List<MetrichePerformaance>
    
    @Query("""
        SELECT AVG(performaanceScore) FROM metriche_performaance 
        WHERE utenteId = :utenteId 
        AND tipoAttivita = :tipo
        AND data >= :dataInizio
    """)
    suspend fun getPerformaanceScoreMediaPerTipo(
        utenteId: String, 
        tipo: String,
        dataInizio: Long
    ): Float?
    
    @Query("""
        SELECT AVG(efficiencyFactor) FROM metriche_performaance 
        WHERE utenteId = :utenteId 
        AND data >= :dataInizio
    """)
    suspend fun getEfficiencyScoreMediaPeriodo(utenteId: String, dataInizio: Long): Float?
    
    @Query("""
        SELECT MAX(trainingStressScore) FROM metriche_performaance 
        WHERE utenteId = :utenteId 
        AND trainingStressScore IS NOT NULL
    """)
    suspend fun getMigliorVO2Stimato(utenteId: String): Float?
    
    // ==================== QUERY AVANZATE PER DASHBOARD ====================
    
    @Query("""
        SELECT 
            COUNT(*) as totaleAttivita,
            AVG(performaanceScore) as performaanceMedia,
            AVG(efficiencyFactor) as efficienzaMedia,
            MAX(trainingStressScore) as migliorVO2
        FROM metriche_performaance 
        WHERE utenteId = :utenteId 
        AND data BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getRiepilogoPerformancePerPeriodo(
        utenteId: String, 
        dataInizio: Long, 
        dataFine: Long
    ): RiepilogoPerformance
    
    @Query("""
        SELECT 
            tipoAttivita,
            COUNT(*) as numeroAttivita,
            AVG(performaanceScore) as performaanceMedia,
            AVG(efficiencyFactor) as efficienzaMedia
        FROM metriche_performaance 
        WHERE utenteId = :utenteId 
        AND data >= :dataInizio
        GROUP BY tipoAttivita
        ORDER BY numeroAttivita DESC
    """)
    suspend fun getStatistichePerTipoAttivita(
        utenteId: String, 
        dataInizio: Long
    ): List<StatisticheTipoAttivita>
    
    @Query("""
        SELECT 
            DATE(data/1000, 'unixepoch') as giorno,
            AVG(ctl) as ctlMedia,
            AVG(atl) as faticaMedia,
            AVG(forma) as formaMedia
        FROM fitness_freshness 
        WHERE utenteId = :utenteId 
        AND data BETWEEN :dataInizio AND :dataFine
        GROUP BY DATE(data/1000, 'unixepoch')
        ORDER BY giorno ASC
    """)
    suspend fun getTrendFitnessFreshnessPerPeriodo(
        utenteId: String, 
        dataInizio: Long, 
        dataFine: Long
    ): List<TrendFitnessFreshness>
    
    // ==================== CONFRONTI E PROGRESSI ====================
    
    @Query("""
        SELECT 
            a1.data as data1,
            a1.performaanceScore as score1,
            a2.data as data2,
            a2.performaanceScore as score2,
            (a2.performaanceScore - a1.performaanceScore) as miglioramento
        FROM metriche_performaance a1
        INNER JOIN metriche_performaance a2 ON a1.atleta = a2.atleta 
        WHERE a1.utenteId = :utenteId
        AND a1.tipoAttivita = :tipo
        AND a2.tipoAttivita = :tipo
        AND a1.data < a2.data
        ORDER BY a2.data DESC
        LIMIT 10
    """)
    suspend fun getProgressiPerformaancePerTipo(
        utenteId: String,
        tipo: String
    ): List<ProgressoPerformance>
    
    @Query("""
        SELECT 
            distanzaGara,
            MIN(paceMedio) as migliorPace,
            MAX(consistenza) as migliorConsistenza,
            COUNT(*) as numeroTentativi
        FROM analisi_passo_gara 
        
        GROUP BY distanzaGara
        ORDER BY distanzaGara ASC
    """)
    suspend fun getRiepilogoPersonalBest(utenteId: String): List<PersonalBest>
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
    val performaanceMedia: Float?,
    val efficienzaMedia: Float?,
    val migliorVO2: Float?
)

data class StatisticheTipoAttivita(
    val tipoAttivita: String,
    val numeroAttivita: Int,
    val performaanceMedia: Float?,
    val efficienzaMedia: Float?
)

data class TrendFitnessFreshness(
    val giorno: String,
    val ctlMedia: Float?,
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
