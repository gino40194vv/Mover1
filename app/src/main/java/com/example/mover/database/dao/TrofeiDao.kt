package com.example.mover.database.dao

import androidx.room.*
import com.example.mover.database.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TrofeiDao {
    
    // === TROFEI ===
    @Query("SELECT * FROM trofei WHERE isAttivo = 1 ORDER BY categoria, ordinamento")
    fun getAllTrofei(): Flow<List<Trofeo>>
    
    @Query("SELECT * FROM trofei WHERE categoria = :categoria AND isAttivo = 1 ORDER BY ordinamento")
    fun getTrofeiPerCategoria(categoria: CategoriaTrofeo): Flow<List<Trofeo>>
    
    @Query("SELECT * FROM trofei WHERE livello = :livello AND isAttivo = 1 ORDER BY ordinamento")
    fun getTrofeiPerLivello(livello: LivelloTrofeo): Flow<List<Trofeo>>
    
    @Query("SELECT * FROM trofei WHERE rarità = :rarita AND isAttivo = 1 ORDER BY ordinamento")
    fun getTrofeiPerRarita(rarita: RaritaTrofeo): Flow<List<Trofeo>>
    
    @Query("SELECT * FROM trofei WHERE id = :trofeoId")
    suspend fun getTrofeoById(trofeoId: String): Trofeo?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrofeo(trofeo: Trofeo)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrofei(trofei: List<Trofeo>)
    
    @Update
    suspend fun updateTrofeo(trofeo: Trofeo)
    
    @Delete
    suspend fun deleteTrofeo(trofeo: Trofeo)
    
    // === TROFEI UTENTE ===
    @Query("""
        SELECT t.*, tu.dataOttenimento, tu.posizione, tu.valore, tu.condiviso, tu.visibile
        FROM trofei t 
        INNER JOIN trofei_utente tu ON t.id = tu.trofeoId 
        WHERE tu.utenteId = :utenteId AND tu.visibile = 1
        ORDER BY tu.dataOttenimento DESC
    """)
    fun getTrofeiUtente(utenteId: String): Flow<List<TrofeoConDettagli>>
    
    @Query("""
        SELECT t.*, tu.dataOttenimento, tu.posizione, tu.valore, tu.condiviso, tu.visibile
        FROM trofei t 
        INNER JOIN trofei_utente tu ON t.id = tu.trofeoId 
        WHERE tu.utenteId = :utenteId AND t.categoria = :categoria AND tu.visibile = 1
        ORDER BY tu.dataOttenimento DESC
    """)
    fun getTrofeiUtentePerCategoria(utenteId: String, categoria: CategoriaTrofeo): Flow<List<TrofeoConDettagli>>
    
    @Query("""
        SELECT t.*, tu.dataOttenimento, tu.posizione, tu.valore, tu.condiviso, tu.visibile
        FROM trofei t 
        INNER JOIN trofei_utente tu ON t.id = tu.trofeoId 
        WHERE tu.utenteId = :utenteId AND t.livello = :livello AND tu.visibile = 1
        ORDER BY tu.dataOttenimento DESC
    """)
    fun getTrofeiUtentePerLivello(utenteId: String, livello: LivelloTrofeo): Flow<List<TrofeoConDettagli>>
    
    @Query("SELECT * FROM trofei_utente WHERE trofeoId = :trofeoId AND utenteId = :utenteId")
    suspend fun getTrofeoUtente(trofeoId: String, utenteId: String): TrofeoUtente?
    
    @Query("SELECT COUNT(*) FROM trofei_utente WHERE utenteId = :utenteId")
    suspend fun getNumeroTrofeiUtente(utenteId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrofeoUtente(trofeoUtente: TrofeoUtente)
    
    @Update
    suspend fun updateTrofeoUtente(trofeoUtente: TrofeoUtente)
    
    @Delete
    suspend fun deleteTrofeoUtente(trofeoUtente: TrofeoUtente)
    
    // === CLASSIFICHE TROFEI ===
    @Query("SELECT * FROM classifiche_trofeo WHERE trofeoId = :trofeoId ORDER BY posizione ASC")
    fun getClassificaTrofeo(trofeoId: String): Flow<List<ClassificaTrofeo>>

    @Query("SELECT ct.*, u.nomeCompleto, u.immagineProfilo FROM classifiche_trofeo ct INNER JOIN utenti u ON ct.utenteId = u.id WHERE ct.trofeoId = :trofeoId ORDER BY ct.posizione ASC LIMIT :limit")
    fun getClassificaTrofeoConUtenti(trofeoId: String, limit: Int = 10): Flow<List<ClassificaTrofeoConUtente>>

    @Query("SELECT ct.posizione FROM classifiche_trofeo ct WHERE ct.trofeoId = :trofeoId AND ct.utenteId = :utenteId")
    suspend fun getPosizioneUtenteTrofeo(trofeoId: String, utenteId: String): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassificaTrofeo(classifica: ClassificaTrofeo)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassificheTrofeo(classifiche: List<ClassificaTrofeo>)
    
    @Update
    suspend fun updateClassificaTrofeo(classifica: ClassificaTrofeo)
    
    @Delete
    suspend fun deleteClassificaTrofeo(classifica: ClassificaTrofeo)
    
    // === QUERY STATISTICHE ===
    @Query("SELECT SUM(t.punti) FROM trofei t INNER JOIN trofei_utente tu ON t.id = tu.trofeoId WHERE tu.utenteId = :utenteId")
    suspend fun getPuntiTotaliTrofei(utenteId: String): Int?

    @Query("SELECT livello, COUNT(*) as count FROM trofei t INNER JOIN trofei_utente tu ON t.id = tu.trofeoId WHERE tu.utenteId = :utenteId GROUP BY livello ORDER BY count DESC")
    suspend fun getTrofeiPerLivelloUtente(utenteId: String): List<LivelloTrofeoConConteggio>

    @Query("SELECT categoria, COUNT(*) as count FROM trofei t INNER JOIN trofei_utente tu ON t.id = tu.trofeoId WHERE tu.utenteId = :utenteId GROUP BY categoria ORDER BY count DESC")
    suspend fun getTrofeiPerCategoriaUtente(utenteId: String): List<CategoriaTrofeoConConteggio>

    @Query("SELECT t.*, tu.dataOttenimento FROM trofei t INNER JOIN trofei_utente tu ON t.id = tu.trofeoId WHERE tu.utenteId = :utenteId ORDER BY tu.dataOttenimento DESC LIMIT 1")
    @Query("SELECT t.*, tu.dataOttenimento, tu.posizione, tu.valore, tu.condiviso, tu.visibile FROM trofei t INNER JOIN trofei_utente tu ON t.id = tu.trofeoId WHERE tu.utenteId = :utenteId ORDER BY tu.dataOttenimento DESC LIMIT 1")
    @Query("SELECT t.*, tu.dataOttenimento, tu.posizione, tu.valore FROM trofei t INNER JOIN trofei_utente tu ON t.id = tu.trofeoId WHERE tu.utenteId = :utenteId ORDER BY t.rarità DESC, tu.dataOttenimento DESC LIMIT 5")
    suspend fun getTrofeiPiuRari(utenteId: String): List<TrofeoConDettagli>
    @Query("SELECT t.*, tu.dataOttenimento, tu.posizione, tu.valore, tu.condiviso, tu.visibile FROM trofei t INNER JOIN trofei_utente tu ON t.id = tu.trofeoId WHERE tu.utenteId = :utenteId ORDER BY t.rarità DESC, tu.dataOttenimento DESC LIMIT 5")
    // === TROFEI RECENTI COMMUNITY ===
    @Query("SELECT t.*, tu.dataOttenimento, tu.posizione, tu.valore, u.nomeCompleto, u.immagineProfilo FROM trofei t INNER JOIN trofei_utente tu ON t.id = tu.trofeoId INNER JOIN utenti u ON tu.utenteId = u.id WHERE tu.condiviso = 1 ORDER BY tu.dataOttenimento DESC LIMIT :limit")
    suspend fun getTrofeiRecentiCommunity(limit: Int = 20): List<TrofeoConUtenteDettagli>
    
    // === STATISTICHE COMPLETE ===
    @Query("SELECT COUNT(*) as totaliOttenuti, SUM(t.punti) as puntiTotali FROM trofei t INNER JOIN trofei_utente tu ON t.id = tu.trofeoId WHERE tu.utenteId = :utenteId")
    suspend fun getStatisticheTrofeiUtente(utenteId: String): StatisticheTrofeiSemplificata?
}

// Data classes per le query complesse
data class TrofeoConDettagli(
    val id: String,
    val nome: String,
    val descrizione: String,
    val icona: String,
    val colore: String,
    val categoria: CategoriaTrofeo,
    val tipo: TipoTrofeo,
    val livello: LivelloTrofeo,
    val punti: Int,
    val condizioni: String,
    val isAttivo: Boolean,
    val rarità: RaritaTrofeo,
    val dataCreazione: Date,
    val ordinamento: Int,
    val dataOttenimento: Date,
    val posizione: Int?,
    val valore: Double?,
    val condiviso: Boolean,
    val visibile: Boolean
)

data class TrofeoConUtenteDettagli(
    val id: String,
    val nome: String,
    val descrizione: String,
    val icona: String,
    val colore: String,
    val categoria: CategoriaTrofeo,
    val tipo: TipoTrofeo,
    val livello: LivelloTrofeo,
    val punti: Int,
    val condizioni: String,
    val isAttivo: Boolean,
    val rarità: RaritaTrofeo,
    val dataCreazione: Date,
    val ordinamento: Int,
    val dataOttenimento: Date,
    val posizione: Int?,
    val valore: Double?,
    val nomeCompleto: String,
    val immagineProfilo: String?
)

data class ClassificaTrofeoConUtente(
    val id: String,
    val trofeoId: String,
    val utenteId: String,
    val posizione: Int,
    val punteggio: Double,
    val tempo: Long?,
    val dataOttenimento: Date,
    val nomeCompleto: String,
    val immagineProfilo: String?
)

data class LivelloTrofeoConConteggio(
    val livello: LivelloTrofeo,
    val count: Int
)

data class CategoriaTrofeoConConteggio(
    val categoria: CategoriaTrofeo,
    val count: Int
)

data class StatisticheTrofeiUtente(
    val totaliOttenuti: Int,
    val bronzo: Int,
    val argento: Int,
    val oro: Int,
    val platino: Int,
    val diamante: Int,
    val puntiTotali: Int
)

data class StatisticheTrofeiSemplificata(
    val totaliOttenuti: Int,
    val puntiTotali: Int
)
