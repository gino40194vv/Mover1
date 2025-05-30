package com.example.mover.database.dao

import androidx.room.*
import com.example.mover.database.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SfideDao {
    
    // Sfida operations
    @Query("SELECT * FROM sfide")
    fun getAllSfide(): Flow<List<Sfida>>
    
    @Query("SELECT * FROM sfide WHERE id = :id")
    suspend fun getSfidaById(id: String): Sfida?
    
    @Query("SELECT * FROM sfide WHERE isAttiva = 1 ORDER BY dataInizio DESC")
    fun getSfideAttive(): Flow<List<Sfida>>
    
    @Query("SELECT * FROM sfide WHERE categoria = :categoria")
    fun getSfideByCategoria(categoria: CategoriaSfida): Flow<List<Sfida>>
    
    @Query("SELECT * FROM sfide WHERE tipo = :tipo")
    fun getSfideByTipo(tipo: TipoSfida): Flow<List<Sfida>>
    
    @Query("SELECT * FROM sfide WHERE dataInizio <= :oggi AND dataFine >= :oggi")
    fun getSfideInCorso(oggi: Date): Flow<List<Sfida>>
    
    @Query("SELECT * FROM sfide WHERE dataFine < :oggi")
    fun getSfideTerminate(oggi: Date): Flow<List<Sfida>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSfida(sfida: Sfida)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSfide(sfide: List<Sfida>)
    
    @Update
    suspend fun updateSfida(sfida: Sfida)
    
    @Delete
    suspend fun deleteSfida(sfida: Sfida)
    
    @Query("DELETE FROM sfide WHERE id = :id")
    suspend fun deleteSfidaById(id: String)
    
    // PartecipazioneSfida operations
    @Query("SELECT * FROM partecipazioni_sfide WHERE sfidaId = :sfidaId")
    fun getPartecipazioniSfida(sfidaId: String): Flow<List<PartecipazioneSfida>>
    
    @Query("SELECT * FROM partecipazioni_sfide WHERE utenteId = :utenteId")
    fun getPartecipazioniUtente(utenteId: String): Flow<List<PartecipazioneSfida>>
    
    @Query("SELECT * FROM partecipazioni_sfide WHERE sfidaId = :sfidaId AND utenteId = :utenteId")
    suspend fun getPartecipazione(sfidaId: String, utenteId: String): PartecipazioneSfida?
    
    @Query("SELECT * FROM partecipazioni_sfide WHERE sfidaId = :sfidaId AND isCompletata = 1")
    fun getPartecipazioniCompletate(sfidaId: String): Flow<List<PartecipazioneSfida>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartecipazione(partecipazione: PartecipazioneSfida)
    
    @Update
    suspend fun updatePartecipazione(partecipazione: PartecipazioneSfida)
    
    @Delete
    suspend fun deletePartecipazione(partecipazione: PartecipazioneSfida)
    
    // ProgressoSfida operations
    @Query("SELECT * FROM progresso_sfida WHERE sfidaId = :sfidaId")
    fun getProgressiSfida(sfidaId: String): Flow<List<ProgressoSfida>>
    
    @Query("SELECT * FROM progresso_sfida WHERE utenteId = :utenteId")
    fun getProgressiUtente(utenteId: String): Flow<List<ProgressoSfida>>
    
    @Query("SELECT * FROM progresso_sfida WHERE sfidaId = :sfidaId AND utenteId = :utenteId")
    suspend fun getProgresso(sfidaId: String, utenteId: String): ProgressoSfida?
    
    @Query("SELECT * FROM progresso_sfida WHERE sfidaId = :sfidaId AND isCompletata = 1")
    fun getProgressiCompletati(sfidaId: String): Flow<List<ProgressoSfida>>
    
    @Query("SELECT * FROM progresso_sfida WHERE sfidaId = :sfidaId ORDER BY valoreAttuale DESC")
    fun getClassificaSfida(sfidaId: String): Flow<List<ProgressoSfida>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgresso(progresso: ProgressoSfida)
    
    @Update
    suspend fun updateProgresso(progresso: ProgressoSfida)
    
    @Delete
    suspend fun deleteProgresso(progresso: ProgressoSfida)
    
    // Complex queries
    @Query("""
        SELECT s.* FROM sfide s 
        INNER JOIN partecipazioni_sfide p ON s.id = p.sfidaId 
        WHERE p.utenteId = :utenteId AND s.isAttiva = 1
    """)
    fun getSfideAttivaUtente(utenteId: String): Flow<List<Sfida>>
    
    @Query("""
        SELECT COUNT(*) FROM partecipazioni_sfide 
        WHERE sfidaId = :sfidaId
    """)
    suspend fun getNumeroPartecipanti(sfidaId: String): Int
    
    @Query("""
        SELECT COUNT(*) FROM progresso_sfida 
        WHERE sfidaId = :sfidaId AND isCompletata = 1
    """)
    suspend fun getNumeroCompletamenti(sfidaId: String): Int
    
    @Query("""
        SELECT AVG(percentualeCompletamento) FROM progresso_sfida 
        WHERE sfidaId = :sfidaId
    """)
    suspend fun getPercentualeMediaCompletamento(sfidaId: String): Double?
}
