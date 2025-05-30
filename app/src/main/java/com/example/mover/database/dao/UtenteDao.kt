package com.example.mover.database.dao

import androidx.room.*
import com.example.mover.database.entities.Utente
import kotlinx.coroutines.flow.Flow

@Dao
interface UtenteDao {
    
    @Query("SELECT * FROM utenti WHERE id = :id")
    suspend fun getUtenteById(id: Long): Utente?
    
    @Query("SELECT * FROM utenti WHERE username = :username")
    suspend fun getUtenteByUsername(username: String): Utente?
    
    @Query("SELECT * FROM utenti WHERE email = :email")
    suspend fun getUtenteByEmail(email: String): Utente?
    
    @Query("SELECT * FROM utenti WHERE attivo = 1 ORDER BY ultimoAccesso DESC")
    fun getAllUtentiAttivi(): Flow<List<Utente>>
    
    @Query("""
        SELECT * FROM utenti 
        WHERE (nomeCompleto LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%') 
        AND attivo = 1 
        AND profiloPublico = 1
        ORDER BY 
            CASE WHEN username LIKE :query || '%' THEN 1 ELSE 2 END,
            totalKudos DESC
        LIMIT :limit
    """)
    suspend fun searchUtenti(query: String, limit: Int = 20): List<Utente>
    
    @Query("""
        SELECT u.* FROM utenti u
        INNER JOIN seguaci s ON u.id = s.seguitoId
        WHERE s.seguaceId = :utenteId AND s.stato = 'ATTIVO'
        ORDER BY u.ultimaAttivita DESC
    """)
    fun getSeguiti(utenteId: Long): Flow<List<Utente>>
    
    @Query("""
        SELECT u.* FROM utenti u
        INNER JOIN seguaci s ON u.id = s.seguaceId
        WHERE s.seguitoId = :utenteId AND s.stato = 'ATTIVO'
        ORDER BY s.dataInizio DESC
    """)
    fun getSeguaci(utenteId: Long): Flow<List<Utente>>
    
    @Query("""
        SELECT COUNT(*) FROM seguaci 
        WHERE seguitoId = :utenteId AND stato = 'ATTIVO'
    """)
    suspend fun getNumeroSeguaci(utenteId: Long): Int
    
    @Query("""
        SELECT COUNT(*) FROM seguaci 
        WHERE seguaceId = :utenteId AND stato = 'ATTIVO'
    """)
    suspend fun getNumeroSeguiti(utenteId: Long): Int
    
    @Query("""
        SELECT * FROM utenti 
        WHERE citta = :citta AND attivo = 1 AND profiloPublico = 1
        ORDER BY totalKudos DESC
        LIMIT :limit
    """)
    suspend fun getUtentiPerCitta(citta: String, limit: Int = 50): List<Utente>
    
    @Query("""
        SELECT * FROM utenti 
        WHERE livello >= :livelloMin AND attivo = 1 AND profiloPublico = 1
        ORDER BY puntiEsperienza DESC
        LIMIT :limit
    """)
    suspend fun getUtentiTopLevel(livelloMin: Int = 10, limit: Int = 100): List<Utente>
    
    @Query("""
        UPDATE utenti SET 
            totalAttivita = totalAttivita + 1,
            ultimaAttivita = :dataAttivita,
            puntiEsperienza = puntiEsperienza + :punti
        WHERE id = :utenteId
    """)
    suspend fun aggiornaStatisticheAttivita(utenteId: Long, dataAttivita: Long, punti: Int)
    
    @Query("""
        UPDATE utenti SET 
            totalDistanza = totalDistanza + :distanza,
            totalTempo = totalTempo + :tempo,
            totalDislivello = totalDislivello + :dislivello
        WHERE id = :utenteId
    """)
    suspend fun aggiornaStatisticheTotali(utenteId: Long, distanza: Float, tempo: Long, dislivello: Float)
    
    @Query("UPDATE utenti SET totalKudos = totalKudos + 1 WHERE id = :utenteId")
    suspend fun incrementaKudos(utenteId: Long)
    
    @Query("UPDATE utenti SET segmentiKOM = segmentiKOM + 1 WHERE id = :utenteId")
    suspend fun incrementaKOM(utenteId: Long)
    
    @Query("UPDATE utenti SET segmentiQOM = segmentiQOM + 1 WHERE id = :utenteId")
    suspend fun incrementaQOM(utenteId: Long)
    
    @Query("UPDATE utenti SET ultimoAccesso = :timestamp WHERE id = :utenteId")
    suspend fun aggiornaUltimoAccesso(utenteId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUtente(utente: Utente): Long
    
    @Update
    suspend fun updateUtente(utente: Utente)
    
    @Delete
    suspend fun deleteUtente(utente: Utente)
    
    @Query("DELETE FROM utenti WHERE id = :id")
    suspend fun deleteUtenteById(id: Long)
}