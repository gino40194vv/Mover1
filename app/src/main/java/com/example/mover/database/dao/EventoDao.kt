package com.example.mover.database.dao

import androidx.room.*
import com.example.mover.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    
    // === EVENTI VIRTUALI ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvento(evento: EventoVirtuale): Long
    
    @Update
    suspend fun updateEvento(evento: EventoVirtuale)
    
    @Query("SELECT * FROM eventi_virtuali WHERE id = :eventoId")
    suspend fun getEventoById(eventoId: Long): EventoVirtuale?
    
    @Query("""
        SELECT * FROM eventi_virtuali 
        WHERE attivo = 1 
        AND dataFine > :now
        AND (pubblico = 1 OR creatorId = :utenteId OR clubId IN (
            SELECT clubId FROM membri_club WHERE utenteId = :utenteId AND stato = 'ATTIVO'
        ))
        ORDER BY dataInizio ASC
        LIMIT :limit
    """)
    suspend fun getEventiDisponibili(utenteId: Long, now: Long = System.currentTimeMillis(), limit: Int = 50): List<EventoVirtuale>
    
    @Query("""
        SELECT ev.* FROM eventi_virtuali ev
        INNER JOIN partecipazioni_evento pe ON ev.id = pe.eventoId
        WHERE pe.utenteId = :utenteId 
        AND pe.stato IN ('ISCRITTO', 'PARTECIPATO', 'COMPLETATO')
        ORDER BY ev.dataInizio DESC
    """)
    fun getEventiUtente(utenteId: Long): Flow<List<EventoVirtuale>>
    
    @Query("""
        SELECT * FROM eventi_virtuali 
        WHERE nome LIKE '%' || :query || '%' 
        AND attivo = 1 
        AND pubblico = 1
        AND dataFine > :now
        ORDER BY dataInizio ASC
        LIMIT :limit
    """)
    suspend fun searchEventi(query: String, now: Long = System.currentTimeMillis(), limit: Int = 20): List<EventoVirtuale>
    
    @Query("""
        SELECT * FROM eventi_virtuali 
        WHERE clubId = :clubId 
        AND attivo = 1
        ORDER BY dataInizio DESC
        LIMIT :limit
    """)
    suspend fun getEventiClub(clubId: Long, limit: Int = 50): List<EventoVirtuale>
    
    @Query("""
        SELECT * FROM eventi_virtuali 
        WHERE tipo = :tipo 
        AND attivo = 1 
        AND pubblico = 1
        AND dataFine > :now
        ORDER BY dataInizio ASC
        LIMIT :limit
    """)
    suspend fun getEventiPerTipo(tipo: String, now: Long = System.currentTimeMillis(), limit: Int = 50): List<EventoVirtuale>
    
    @Query("UPDATE eventi_virtuali SET totalPartecipanti = totalPartecipanti + 1 WHERE id = :eventoId")
    suspend fun incrementaPartecipanti(eventoId: Long)
    
    @Query("UPDATE eventi_virtuali SET totalPartecipanti = totalPartecipanti - 1 WHERE id = :eventoId AND totalPartecipanti > 0")
    suspend fun decrementaPartecipanti(eventoId: Long)
    
    // === PARTECIPAZIONI EVENTO ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartecipazione(partecipazione: PartecipazioneEvento): Long
    
    @Update
    suspend fun updatePartecipazione(partecipazione: PartecipazioneEvento)
    
    @Query("DELETE FROM partecipazioni_evento WHERE eventoId = :eventoId AND utenteId = :utenteId")
    suspend fun removePartecipazione(eventoId: Long, utenteId: Long)
    
    @Query("""
        SELECT * FROM partecipazioni_evento 
        WHERE eventoId = :eventoId AND utenteId = :utenteId
    """)
    suspend fun getPartecipazione(eventoId: Long, utenteId: Long): PartecipazioneEvento?
    
    @Query("SELECT COUNT(*) > 0 FROM partecipazioni_evento WHERE eventoId = :eventoId AND utenteId = :utenteId AND stato != 'RITIRATO'")
    suspend fun isPartecipante(eventoId: Long, utenteId: Long): Boolean
    
    @Query("""
        SELECT pe.*, u.username, u.immagineProfilo, u.nomeCompleto
        FROM partecipazioni_evento pe
        INNER JOIN utenti u ON pe.utenteId = u.id
        WHERE pe.eventoId = :eventoId 
        AND pe.stato IN ('ISCRITTO', 'PARTECIPATO', 'COMPLETATO')
        ORDER BY 
            CASE WHEN pe.obiettivoRaggiunto THEN 1 ELSE 2 END,
            pe.distanzaTotale DESC,
            pe.tempoTotale ASC
        LIMIT :limit
    """)
    suspend fun getClassificaEvento(
        eventoId: Long,
        limit: Int = 100
    ): List<PartecipazioneEventoConUtente>
    
    @Query("""
        UPDATE partecipazioni_evento SET 
            attivitaCompletate = attivitaCompletate + 1,
            distanzaTotale = distanzaTotale + :distanza,
            tempoTotale = tempoTotale + :tempo,
            dislivelloTotale = dislivelloTotale + :dislivello,
            ultimaAttivita = :dataAttivita
        WHERE eventoId = :eventoId AND utenteId = :utenteId
    """)
    suspend fun aggiornaProgressoPartecipazione(
        eventoId: Long, 
        utenteId: Long, 
        distanza: Float, 
        tempo: Long, 
        dislivello: Float, 
        dataAttivita: Long
    )
    
    @Query("""
        SELECT COUNT(*) FROM partecipazioni_evento 
        WHERE eventoId = :eventoId 
        AND stato IN ('ISCRITTO', 'PARTECIPATO', 'COMPLETATO')
    """)
    suspend fun getNumeroPartecipanti(eventoId: Long): Int
}

// Data class per le query JOIN
data class PartecipazioneEventoConUtente(
    val id: Long,
    val eventoId: Long,
    val utenteId: Long,
    val stato: String,
    val dataIscrizione: Long,
    val obiettivoRaggiunto: Boolean,
    val attivitaCompletate: Int,
    val distanzaTotale: Double,
    val tempoTotale: Long,
    val dislivelloTotale: Double,
    val ultimaAttivita: Long?,
    val notePersonali: String?,
    val username: String,
    val immagineProfilo: String?,
    val nomeCompleto: String
)
