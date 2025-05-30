package com.example.mover.database.dao

import androidx.room.*
import com.example.mover.database.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ObiettiviDao {
    
    // === OBIETTIVI ===
    @Query("SELECT * FROM obiettivi WHERE utenteId = :utenteId AND isAttivo = 1 ORDER BY dataCreazione DESC")
    fun getObiettiviAttivi(utenteId: String): Flow<List<Obiettivo>>
    
    @Query("SELECT * FROM obiettivi WHERE utenteId = :utenteId AND isCompletato = 1 ORDER BY dataCompletamento DESC")
    fun getObiettiviCompletati(utenteId: String): Flow<List<Obiettivo>>
    
    @Query("SELECT * FROM obiettivi WHERE utenteId = :utenteId AND periodo = :periodo AND isAttivo = 1")
    fun getObiettiviPerPeriodo(utenteId: String, periodo: PeriodoObiettivo): Flow<List<Obiettivo>>
    
    @Query("SELECT * FROM obiettivi WHERE utenteId = :utenteId AND tipo = :tipo AND isAttivo = 1")
    fun getObiettiviPerTipo(utenteId: String, tipo: TipoObiettivo): Flow<List<Obiettivo>>
    
    @Query("""
        SELECT * FROM obiettivi 
        WHERE utenteId = :utenteId 
        AND dataFine >= :oggi 
        AND isAttivo = 1 
        ORDER BY dataFine ASC
    """)
    fun getObiettiviInCorso(utenteId: String, oggi: Date = Date()): Flow<List<Obiettivo>>
    
    @Query("""
        SELECT * FROM obiettivi 
        WHERE utenteId = :utenteId 
        AND dataFine BETWEEN :oggi AND :traUnaSett
        AND isAttivo = 1 
        AND isCompletato = 0
    """)
    fun getObiettiviInScadenza(utenteId: String, oggi: Date = Date(), traUnaSett: Date): Flow<List<Obiettivo>>
    
    @Query("SELECT * FROM obiettivi WHERE id = :obiettivoId")
    suspend fun getObiettivoById(obiettivoId: String): Obiettivo?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObiettivo(obiettivo: Obiettivo)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObiettivi(obiettivi: List<Obiettivo>)
    
    @Update
    suspend fun updateObiettivo(obiettivo: Obiettivo)
    
    @Delete
    suspend fun deleteObiettivo(obiettivo: Obiettivo)
    
    // === PROGRESSI OBIETTIVI ===
    @Query("SELECT * FROM progressi_obiettivo WHERE obiettivoId = :obiettivoId ORDER BY data DESC")
    fun getProgressiObiettivo(obiettivoId: String): Flow<List<ProgressoObiettivo>>
    
    @Query("""
        SELECT * FROM progressi_obiettivo 
        WHERE obiettivoId = :obiettivoId 
        AND data BETWEEN :dataInizio AND :dataFine 
        ORDER BY data ASC
    """)
    fun getProgressiObiettivoPerPeriodo(
        obiettivoId: String, 
        dataInizio: Date, 
        dataFine: Date
    ): Flow<List<ProgressoObiettivo>>
    
    @Query("""
        SELECT SUM(valoreAggiunto) 
        FROM progressi_obiettivo 
        WHERE obiettivoId = :obiettivoId
    """)
    suspend fun getProgressoTotaleObiettivo(obiettivoId: String): Double?
    
    @Query("""
        SELECT SUM(valoreAggiunto) 
        FROM progressi_obiettivo 
        WHERE obiettivoId = :obiettivoId 
        AND data BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getProgressoObiettivoPerPeriodo(
        obiettivoId: String, 
        dataInizio: Date, 
        dataFine: Date
    ): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressoObiettivo(progresso: ProgressoObiettivo)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressiObiettivo(progressi: List<ProgressoObiettivo>)
    
    @Update
    suspend fun updateProgressoObiettivo(progresso: ProgressoObiettivo)
    
    @Delete
    suspend fun deleteProgressoObiettivo(progresso: ProgressoObiettivo)
    
    // === NOTIFICHE OBIETTIVI ===
    @Query("SELECT * FROM notifiche_obiettivo WHERE obiettivoId = :obiettivoId ORDER BY dataInvio DESC")
    fun getNotificheObiettivo(obiettivoId: String): Flow<List<NotificaObiettivo>>

    // Simplified queries to avoid JOIN issues
    @Query("SELECT * FROM notifiche_obiettivo WHERE isLetta = 0 ORDER BY dataInvio DESC")
    fun getNotificheNonLette(): Flow<List<NotificaObiettivo>>

    @Query("SELECT COUNT(*) FROM notifiche_obiettivo WHERE isLetta = 0")
    suspend fun getNumeroNotificheNonLette(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificaObiettivo(notifica: NotificaObiettivo)
    
    @Update
    suspend fun updateNotificaObiettivo(notifica: NotificaObiettivo)
    
    @Delete
    suspend fun deleteNotificaObiettivo(notifica: NotificaObiettivo)
    
    // === QUERY STATISTICHE ===
    @Query("SELECT COUNT(*) FROM obiettivi WHERE utenteId = :utenteId AND isCompletato = 1")
    suspend fun getNumeroObiettiviCompletati(utenteId: String): Int
    
    @Query("SELECT COUNT(*) FROM obiettivi WHERE utenteId = :utenteId AND isAttivo = 1")
    suspend fun getNumeroObiettiviAttivi(utenteId: String): Int
    
    @Query("""
        SELECT AVG(percentualeCompletamento) 
        FROM obiettivi 
        WHERE utenteId = :utenteId AND isCompletato = 1
    """)
    suspend fun getPercentualeMediaCompletamento(utenteId: String): Double?
    
    @Query("""
        SELECT AVG(julianday(dataCompletamento) - julianday(dataInizio)) as giorni
        FROM obiettivi 
        WHERE utenteId = :utenteId AND isCompletato = 1 AND dataCompletamento IS NOT NULL
    """)
    suspend fun getTempoMedioCompletamento(utenteId: String): Double?
    
    // === OBIETTIVI CON PROGRESSI ===
    @Query("""
        SELECT o.*, 
               COALESCE(SUM(po.valoreAggiunto), 0) as progressoTotale,
               CASE 
                   WHEN o.valoreTarget > 0 THEN (COALESCE(SUM(po.valoreAggiunto), 0) * 100.0 / o.valoreTarget)
                   ELSE 0 
               END as percentualeCalcolata
        FROM obiettivi o
        LEFT JOIN progressi_obiettivo po ON o.id = po.obiettivoId
        WHERE o.utenteId = :utenteId AND o.isAttivo = 1
        GROUP BY o.id
        ORDER BY o.dataCreazione DESC
    """)
    fun getObiettiviConProgressi(utenteId: String): Flow<List<ObiettivoConProgresso>>

    // === TIPI E PERIODI OBIETTIVI ===
    @Query(
        """
        SELECT tipo, COUNT(*) as count 
        FROM obiettivi 
        WHERE utenteId = :utenteId AND isAttivo = 1 
        GROUP BY tipo 
        ORDER BY count DESC
    """
    )
    suspend fun getTipiObiettiviPreferiti(utenteId: String): List<TipoObiettivoConConteggio>

    @Query(
        """
        SELECT periodo, COUNT(*) as count 
        FROM obiettivi 
        WHERE utenteId = :utenteId AND isAttivo = 1 
        GROUP BY periodo 
        ORDER BY count DESC
    """
    )
    suspend fun getPeriodiObiettiviPreferiti(utenteId: String): List<PeriodoObiettivoConConteggio>
}

// Data classes per le query complesse
data class ObiettivoConProgresso(
    val id: String,
    val utenteId: String,
    val titolo: String,
    val descrizione: String?,
    val tipo: TipoObiettivo,
    val periodo: PeriodoObiettivo,
    val valoreTarget: Double,
    val unitaMisura: String,
    val dataInizio: Date,
    val dataFine: Date,
    val isAttivo: Boolean,
    val isCompletato: Boolean,
    val difficolta: DifficoltaObiettivo,
    val progressoTotale: Double,
    val percentualeCalcolata: Double
)

data class TipoObiettivoConConteggio(
    val tipo: TipoObiettivo,
    val count: Int
)

data class PeriodoObiettivoConConteggio(
    val periodo: PeriodoObiettivo,
    val count: Int
)
