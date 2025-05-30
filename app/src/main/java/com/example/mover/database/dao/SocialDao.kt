package com.example.mover.database.dao

import androidx.room.*
import com.example.mover.database.entities.*

@Dao
interface SocialDao {
    
    // === SEGUACI ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeguace(seguace: Seguace): Long
    
    @Query("DELETE FROM seguaci WHERE seguaceId = :seguaceId AND seguitoId = :seguitoId")
    suspend fun removeSeguace(seguaceId: Long, seguitoId: Long)
    
    @Query("""
        SELECT * FROM seguaci 
        WHERE seguaceId = :seguaceId AND seguitoId = :seguitoId AND stato = 'ATTIVO'
    """)
    suspend fun getSeguace(seguaceId: Long, seguitoId: Long): Seguace?
    
    @Query("SELECT COUNT(*) > 0 FROM seguaci WHERE seguaceId = :seguaceId AND seguitoId = :seguitoId AND stato = 'ATTIVO'")
    suspend fun staSeguendo(seguaceId: Long, seguitoId: Long): Boolean
    
    // === KUDOS ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKudos(kudos: Kudos): Long
    
    @Query("DELETE FROM kudos WHERE utenteId = :utenteId AND attivitaId = :attivitaId")
    suspend fun removeKudos(utenteId: Long, attivitaId: Long)
    
    @Query("SELECT COUNT(*) FROM kudos WHERE attivitaId = :attivitaId")
    suspend fun getKudosCount(attivitaId: Long): Int
    
    @Query("SELECT COUNT(*) > 0 FROM kudos WHERE utenteId = :utenteId AND attivitaId = :attivitaId")
    suspend fun haKudos(utenteId: Long, attivitaId: Long): Boolean
    
    @Query("""
        SELECT u.* FROM utenti u
        INNER JOIN kudos k ON u.id = k.utenteId
        WHERE k.attivitaId = :attivitaId
        ORDER BY k.dataKudos DESC
    """)
    suspend fun getUtentiKudos(attivitaId: Long): List<Utente>
    
    // === COMMENTI ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommento(commento: Commento): Long
    
    @Update
    suspend fun updateCommento(commento: Commento)
    
    @Query("DELETE FROM commenti WHERE id = :commentoId")
    suspend fun deleteCommento(commentoId: Long)

    /*
    @Query("""
        SELECT c.*, u.username, u.immagineProfilo 
        FROM commenti c
        INNER JOIN utenti u ON c.utenteId = u.id
        WHERE c.attivitaId = :attivitaId AND c.attivo = 1
        ORDER BY c.dataCommento ASC
    """)
    suspend fun getCommentiAttivita(attivitaId: Long): List<CommentoConUtente>
    */
    
    @Query("SELECT COUNT(*) FROM commenti WHERE attivitaId = :attivitaId AND attivo = 1")
    suspend fun getCommentiCount(attivitaId: Long): Int
    
    // === FEED ATTIVITA ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedAttivita(feedAttivita: FeedAttivita): Long
    
    @Query("""
        SELECT fa.id, fa.utenteId, fa.attivitaId, fa.tipo, fa.score, fa.priorita, 
               fa.dataCreazione, fa.dataScadenza, fa.visualizzato, fa.nascosto, 
               fa.metadati, fa.fattoreDecadimento,
               u.username, u.immagineProfilo
        FROM feed_attivita fa
        INNER JOIN utenti u ON fa.utenteId = u.id
        WHERE fa.utenteId = :utenteId 
        AND fa.nascosto = 0 
        AND (fa.dataScadenza IS NULL OR fa.dataScadenza > :now)
        ORDER BY fa.priorita DESC, fa.score DESC, fa.dataCreazione DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFeedUtente(
        utenteId: Long,
        now: Long = System.currentTimeMillis(),
        limit: Int = 20,
        offset: Int = 0
    ): List<FeedAttivitaCompleto>
    
    @Query("UPDATE feed_attivita SET visualizzato = 1 WHERE id = :feedId")
    suspend fun marcaFeedVisualizzato(feedId: Long)
    
    @Query("UPDATE feed_attivita SET nascosto = 1 WHERE id = :feedId")
    suspend fun nascondiFeedItem(feedId: Long)
    
    @Query("DELETE FROM feed_attivita WHERE dataScadenza < :now")
    suspend fun pulisciFeedScaduto(now: Long = System.currentTimeMillis())
}

// Data classes per i risultati JOIN
data class CommentoConUtente(
    val id: String,
    val attivitaId: String,
    val utenteId: String,
    val testo: String,
    val dataCommento: Long,
    val commentoPadreId: String?,
    val attivo: Boolean,
    val segnalato: Boolean,
    val numeroPiace: Int,
    val numeroRisposte: Int,
    val hashtags: String?,
    val username: String,
    val immagineProfilo: String?
)

data class FeedAttivitaCompleto(
    // Campi FeedAttivita
    val id: Long,
    val utenteId: Long,
    val attivitaId: Long,
    val tipo: String,
    val score: Float,
    val priorita: Int,
    val dataCreazione: Long,
    val dataScadenza: Long?,
    val visualizzato: Boolean,
    val nascosto: Boolean,
    val metadati: String?,
    val fattoreDecadimento: Float,
    // Campi utente
    val username: String,
    val immagineProfilo: String?
    // Note: i campi dell'attività andrebbero aggiunti qui se necessari
)
