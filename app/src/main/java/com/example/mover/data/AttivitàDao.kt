package com.example.mover.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface AttivitàDao {
    @Insert
    suspend fun inserisciAttività(attività: Attività): Long

    @Query("SELECT * FROM attività ORDER BY oraInizio DESC")
    suspend fun getTutteLeAttività(): List<Attività>

    @Query("SELECT * FROM attività WHERE DATE(oraInizio / 1000, 'unixepoch') = :dataSelezionata")
    fun getAttivitàPerData(dataSelezionata: String): List<Attività>

    @Update
    suspend fun aggiornaAttività(attività: Attività)

    @Query("SELECT * FROM attività WHERE tipo = :tipo AND oraFine = 0 AND Automatica = :èAutomatica ORDER BY oraInizio DESC LIMIT 1")
    suspend fun getUltimaAttivitàInCorso(tipo: String, èAutomatica: Boolean): Attività?

    @Query("SELECT * FROM attività WHERE oraInizio >= :startDate")
    suspend fun getAttivitàByDate(startDate: Long): List<Attività>

    @Query("SELECT * FROM attività WHERE oraInizio BETWEEN :startDate AND :endDate")
    suspend fun getAttivitàByDateRange(startDate: Long, endDate: Long): List<Attività>

    // Opzionale: metodi specifici per ogni tipo di attività
    @Query("SELECT SUM(passi) FROM attività WHERE tipo = 'Camminare' AND oraInizio >= :startDate")
    suspend fun getPassiTotaliByDate(startDate: Long): Int?

    @Query("SELECT SUM(distanza) FROM attività WHERE tipo = 'Corsa' AND oraInizio BETWEEN :startDate AND :endDate")
    suspend fun getDistanzaCorsaByDateRange(startDate: Long, endDate: Long): Float?

    @Query("SELECT COUNT(*) FROM attività WHERE tipo = 'Sedersi' AND oraInizio >= :startDate")
    suspend fun getNumeroSeduteByDate(startDate: Long): Int

    @Query("SELECT SUM(tempo) FROM attività WHERE tipo = 'Sedersi' AND oraInizio >= :startDate")
    suspend fun getTempoTotaleSedutoByDate(startDate: Long): Long?

    @Query("SELECT SUM(distanza) FROM attività WHERE tipo = 'Guidare' AND oraInizio BETWEEN :startDate AND :endDate")
    suspend fun getDistanzaGuidaByDateRange(startDate: Long, endDate: Long): Float?

    @Query("SELECT SUM(passi) FROM attività WHERE oraInizio BETWEEN :startDate AND :endDate")
    fun getPassiTotaliByDateRange(startDate: Long, endDate: Long): Int?

    @Query("""
    SELECT * FROM attività 
    WHERE tipo = 'Corsa' 
    ORDER BY distanza DESC 
    LIMIT 1
""")
    suspend fun getBestRunByDistance(): Attività?

    @Query("""
    SELECT * FROM attività 
    WHERE tipo = 'Camminare' 
    ORDER BY passi DESC 
    LIMIT 1
""")
    suspend fun getBestWalkBySteps(): Attività?

    @Query("""
    SELECT * FROM attività 
    WHERE tipo = 'Guidare' 
    ORDER BY distanza DESC 
    LIMIT 1
""")
    suspend fun getBestDriveByDistance(): Attività?

    @Query("SELECT * FROM attività WHERE DATE(oraInizio/1000, 'unixepoch') BETWEEN :startDate AND :endDate")
    suspend fun getAttivitàPerPeriodo(startDate: String, endDate: String): List<Attività>

    @Query("SELECT SUM(tempo) FROM Attività WHERE tipo = 'Camminare' AND oraInizio >= :startOfDay AND oraInizio < :endOfDay")
    fun getTempoCamminataByDate(startOfDay: Long, endOfDay: Long): Long?

    @Query("SELECT SUM(tempo) FROM Attività WHERE tipo = 'Camminare' AND oraInizio >= :startOfWeek AND oraInizio < :endOfWeek")
    fun getTempoCamminataByDateRange(startOfWeek: Long, endOfWeek: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateActivity(activity: Attività)

    @Query("SELECT * FROM attività WHERE tipo = :activityType AND oraInizio >= :startOfDay AND oraInizio < :endOfDay")
    fun getActivityByTypeAndDay(activityType: String, startOfDay: Long, endOfDay: Long): List<Attività>

    @Update
    fun updateActivity(activity: Attività)

    @Insert
    fun insertActivity(activity: Attività)

    @Query("SELECT * FROM attività WHERE tipo = :activityType AND oraInizio >= :startOfDay AND oraInizio < :endOfDay ORDER BY id DESC LIMIT 1")
    fun getLatestActivityByTypeAndDay(activityType: String, startOfDay: Long, endOfDay: Long): Attività?

    @Query("SELECT * FROM attività WHERE oraInizio >= :startOfDay AND oraInizio < :endOfDay")
    fun getAttivitàByDate(startOfDay: Long, endOfDay: Long): List<Attività>

    @Query("SELECT SUM(passi) FROM attività WHERE tipo = 'Camminare' AND oraInizio >= :startOfDay AND oraInizio < :endOfDay")
    fun getPassiByDate(startOfDay: Long, endOfDay: Long): Int?

    @Query("SELECT SUM(tempo) FROM attività WHERE tipo = 'Guidare' AND oraInizio BETWEEN :startDate AND :endDate")
    fun getTempoGuidaByDateRange(startDate: Long, endDate: Long): Long?

    @Query("SELECT SUM(tempo) FROM attività WHERE tipo = 'Corsa' AND oraInizio BETWEEN :startDate AND :endDate")
    fun getTempoCorsaByDateRange(startDate: Long, endDate: Long): Long?

    @Query("SELECT SUM(tempo) FROM attività WHERE tipo = 'Sedersi' AND oraInizio BETWEEN :startDate AND :endDate")
    fun getTempoTotaleSedutoByDateRange(startDate: Long, endDate: Long): Long?

    @Query("SELECT COUNT(*) FROM attività WHERE tipo = 'Sedersi' AND oraInizio BETWEEN :startDate AND :endDate")
    fun getNumeroSeduteByDateRange(startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(distanza) FROM attività WHERE oraInizio BETWEEN :startDate AND :endDate")
    fun getDistanzaTotaleByDateRange(startDate: Long, endDate: Long): Float?

    @Query("SELECT SUM(tempo) FROM attività WHERE tipo = 'Corsa' AND oraInizio >= :startOfDay")
    suspend fun getTempoCorsaByDate(startOfDay: Long): Long?

    @Query("SELECT SUM(distanza) FROM attività WHERE tipo = 'Corsa'")
    suspend fun getDistanzaCorsaTotale(): Float?

    @Query("SELECT SUM(tempo) FROM attività WHERE tipo = 'Sedersi'")
    suspend fun getTempoTotaleSeduto(): Long?

    @Query("""
    SELECT COALESCE(SUM(distanza), 0.0) FROM attività 
    WHERE tipo = 'Corsa' 
    AND oraInizio >= :startOfDay 
    AND oraInizio < :endOfDay
""")
    suspend fun getDistanzaCorsaByDate(startOfDay: Long, endOfDay: Long): Float?

    @Query("SELECT * FROM attività WHERE Automatica = :isAutomatica AND DATE(oraInizio / 1000, 'unixepoch') = :data")
    fun getAttivitàPerTipo(isAutomatica: Boolean, data: String): List<Attività>

    @Query("SELECT * FROM attività WHERE tipo = :tipo AND DATE(oraInizio / 1000, 'unixepoch') = :data")
    fun getAttivitàPerTipo1(tipo: String, data: String): List<Attività>

    // Nuovi metodi per le funzionalità avanzate
    
    // Gestione punti GPS
    @Insert
    suspend fun inserisciPuntoGPS(punto: PuntoGPS)
    
    @Insert
    suspend fun inserisciPuntiGPS(punti: List<PuntoGPS>)
    
    @Query("SELECT * FROM punti_gps WHERE attivitaId = :attivitaId ORDER BY timestamp ASC")
    suspend fun getPuntiGPSPerAttività(attivitaId: Long): List<PuntoGPS>
    
    @Query("DELETE FROM punti_gps WHERE attivitaId = :attivitaId")
    suspend fun eliminaPuntiGPSPerAttività(attivitaId: Long)
    
    // Gestione segmenti
    @Insert
    suspend fun inserisciSegmento(segmento: SegmentoAttività)
    
    @Query("SELECT * FROM segmenti_attività WHERE attivitàId = :attivitaId ORDER BY puntoInizio ASC")
    suspend fun getSegmentiPerAttività(attivitaId: Long): List<SegmentoAttività>
    
    @Query("DELETE FROM segmenti_attività WHERE attivitàId = :attivitaId")
    suspend fun eliminaSegmentiPerAttività(attivitaId: Long)
    
    // Query avanzate per analisi
    @Query("""
        SELECT * FROM attività 
        WHERE tipo = :tipo 
        AND distanza IS NOT NULL 
        ORDER BY distanza DESC 
        LIMIT :limite
    """)
    suspend fun getMiglioriAttivitàPerDistanza(tipo: String, limite: Int = 10): List<Attività>
    
    @Query("""
        SELECT * FROM attività 
        WHERE tipo = :tipo 
        AND velocitaMedia IS NOT NULL 
        ORDER BY velocitaMedia DESC 
        LIMIT :limite
    """)
    suspend fun getMiglioriAttivitàPerVelocità(tipo: String, limite: Int = 10): List<Attività>
    
    @Query("""
        SELECT * FROM attività 
        WHERE tipo = :tipo 
        AND tempo IS NOT NULL 
        ORDER BY tempo DESC 
        LIMIT :limite
    """)
    suspend fun getMiglioriAttivitàPerTempo(tipo: String, limite: Int = 10): List<Attività>
    
    @Query("""
        SELECT AVG(velocitaMedia) FROM attività 
        WHERE tipo = :tipo 
        AND velocitaMedia IS NOT NULL 
        AND oraInizio >= :dataInizio
    """)
    suspend fun getVelocitaMediaPerTipo(tipo: String, dataInizio: Long): Float?
    
    @Query("""
        SELECT SUM(calorie) FROM attività 
        WHERE oraInizio BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getCalorieTotaliPerPeriodo(dataInizio: Long, dataFine: Long): Int?
    
    @Query("""
        SELECT SUM(dislivelloPositivo) FROM attività 
        WHERE oraInizio BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getDislivelloTotalePerPeriodo(dataInizio: Long, dataFine: Long): Float?
    
    // Query per zone cardiache
    @Query("""
        SELECT AVG(frequenzaCardiacaMedia) FROM attività 
        WHERE tipo = :tipo 
        AND frequenzaCardiacaMedia IS NOT NULL 
        AND oraInizio >= :dataInizio
    """)
    suspend fun getFrequenzaCardiacaMediaPerTipo(tipo: String, dataInizio: Long): Float?
    
    // Query per analisi settimanali/mensili
    @Query("""
        SELECT COUNT(*) FROM attività 
        WHERE tipo = :tipo 
        AND oraInizio BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getNumeroAttivitàPerTipoEPeriodo(tipo: String, dataInizio: Long, dataFine: Long): Int
    
    @Query("""
        SELECT * FROM attività 
        WHERE oraInizio BETWEEN :dataInizio AND :dataFine 
        ORDER BY oraInizio DESC
    """)
    suspend fun getAttivitàPerPeriodoCompleto(dataInizio: Long, dataFine: Long): List<Attività>
    
    // Eliminazione attività
    @Delete
    suspend fun eliminaAttività(attività: Attività)
    
    @Query("DELETE FROM attività WHERE id = :id")
    suspend fun eliminaAttivitàPerId(id: Long)
}
