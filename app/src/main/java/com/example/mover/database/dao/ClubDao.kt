package com.example.mover.database.dao

import androidx.room.*
import com.example.mover.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDao {
    
    // === CLUB ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClub(club: Club): Long
    
    @Update
    suspend fun updateClub(club: Club)
    
    @Query("SELECT * FROM club WHERE id = :clubId")
    suspend fun getClubById(clubId: Long): Club?
    
    @Query("""
        SELECT * FROM club 
        WHERE nome LIKE '%' || :query || '%' 
        AND attivo = 1 
        AND pubblico = 1
        ORDER BY totalMembri DESC
        LIMIT :limit
    """)
    suspend fun searchClub(query: String, limit: Int = 20): List<Club>
    
    @Query("""
        SELECT c.* FROM club c
        INNER JOIN membri_club mc ON c.id = mc.clubId
        WHERE mc.utenteId = :utenteId AND mc.stato = 'ATTIVO'
        ORDER BY mc.dataAdesione DESC
    """)
    fun getClubUtente(utenteId: Long): Flow<List<Club>>
    
    @Query("""
        SELECT * FROM club 
        WHERE citta = :citta AND attivo = 1 AND pubblico = 1
        ORDER BY totalMembri DESC
        LIMIT :limit
    """)
    suspend fun getClubPerCitta(citta: String, limit: Int = 50): List<Club>
    
    @Query("UPDATE club SET totalMembri = totalMembri + 1 WHERE id = :clubId")
    suspend fun incrementaMembri(clubId: Long)
    
    @Query("UPDATE club SET totalMembri = totalMembri - 1 WHERE id = :clubId AND totalMembri > 0")
    suspend fun decrementaMembri(clubId: Long)
    
    // === MEMBRI CLUB ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembroClub(membro: MembroClub): Long
    
    @Update
    suspend fun updateMembroClub(membro: MembroClub)
    
    @Query("DELETE FROM membri_club WHERE clubId = :clubId AND utenteId = :utenteId")
    suspend fun removeMembroClub(clubId: Long, utenteId: Long)

    /*
    @Query("""
        SELECT mc.*, u.username, u.immagineProfilo, u.nomeCompleto
        FROM membri_club mc
        INNER JOIN utenti u ON mc.utenteId = u.id
        WHERE mc.clubId = :clubId AND mc.stato = 'ATTIVO'
        ORDER BY 
            CASE mc.ruolo 
                WHEN 'CREATOR' THEN 1 
                WHEN 'ADMIN' THEN 2 
                WHEN 'MODERATORE' THEN 3 
                ELSE 4 
            END,
            mc.dataAdesione ASC
    """)
    suspend fun getMembriClub(clubId: Long): List<MembroClubConUtente>
    */
    
    @Query("""
        SELECT * FROM membri_club 
        WHERE clubId = :clubId AND utenteId = :utenteId
    """)
    suspend fun getMembroClub(clubId: Long, utenteId: Long): MembroClub?
    
    @Query("SELECT COUNT(*) > 0 FROM membri_club WHERE clubId = :clubId AND utenteId = :utenteId AND stato = 'ATTIVO'")
    suspend fun isMembro(clubId: Long, utenteId: Long): Boolean
    
    @Query("""
        UPDATE membri_club SET 
            attivitaNelClub = attivitaNelClub + 1,
            distanzaNelClub = distanzaNelClub + :distanza,
            tempoNelClub = tempoNelClub + :tempo,
            dislivelloNelClub = dislivelloNelClub + :dislivello,
            dataUltimaAttivita = :dataAttivita
        WHERE clubId = :clubId AND utenteId = :utenteId
    """)
    suspend fun aggiornaStatisticheMembro(clubId: Long, utenteId: Long, distanza: Float, tempo: Long, dislivello: Float, dataAttivita: Long)
    
    // === CLASSIFICHE CLUB ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassificaClub(classifica: ClassificaClub): Long

    /*
    @Query("""
        SELECT cc.*, u.username, u.immagineProfilo, u.nomeCompleto
        FROM classifiche_club cc
        INNER JOIN utenti u ON cc.utenteId = u.id
        WHERE cc.clubId = :clubId 
        AND cc.periodo = :periodo 
        AND cc.tipo = :tipo
        AND cc.anno = :anno
        AND (:mese IS NULL OR cc.mese = :mese)
        AND (:settimana IS NULL OR cc.settimana = :settimana)
        ORDER BY cc.posizione ASC
        LIMIT :limit
    """)
    suspend fun getClassificaClub(
        clubId: Long, 
        periodo: String, 
        tipo: String, 
        anno: Int, 
        mese: Int? = null, 
        settimana: Int? = null,
        limit: Int = 100
    ): List<ClassificaClubConUtente>
    */
    
    @Query("""
        SELECT * FROM classifiche_club 
        WHERE clubId = :clubId AND utenteId = :utenteId 
        AND periodo = :periodo AND tipo = :tipo
        AND anno = :anno
        AND (:mese IS NULL OR mese = :mese)
        AND (:settimana IS NULL OR settimana = :settimana)
    """)
    suspend fun getPosizioneUtente(
        clubId: Long, 
        utenteId: Long, 
        periodo: String, 
        tipo: String, 
        anno: Int, 
        mese: Int? = null, 
        settimana: Int? = null
    ): ClassificaClub?
}

// Data classes per i risultati JOIN
data class MembroClubConUtente(
    // Campi MembroClub
    val id: Long,
    val clubId: Long,
    val utenteId: Long,
    val ruolo: String,
    val stato: String,
    val dataAdesione: Long,
    val dataUltimaAttivita: Long?,
    val attivitaNelClub: Int,
    val distanzaNelClub: Double,
    val tempoNelClub: Long,
    val dislivelloNelClub: Double,
    val punteggio: Int,
    val noteAmministrative: String?,
    // Campi Utente
    val username: String,
    val immagineProfilo: String?,
    val nomeCompleto: String?
)

data class ClassificaClubConUtente(
    // Campi ClassificaClub
    val id: Long,
    val clubId: Long,
    val utenteId: Long,
    val periodo: String,
    val tipo: String,
    val posizione: Int,
    val valore: Double,
    val anno: Int,
    val mese: Int?,
    val settimana: Int?,
    val punti: Int,
    val dataAggiornamento: Long,
    // Campi Utente
    val username: String,
    val immagineProfilo: String?,
    val nomeCompleto: String?
)
