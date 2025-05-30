package com.example.mover.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction

@Dao
interface SegmentoDao {
    
    // ==================== GESTIONE SEGMENTI ====================
    
    @Insert
    suspend fun inserisciSegmento(segmento: Segmento): Long
    
    @Update
    suspend fun aggiornaSegmento(segmento: Segmento)
    
    @Delete
    suspend fun eliminaSegmento(segmento: Segmento)
    
    @Query("SELECT * FROM segmenti WHERE id = :id")
    suspend fun getSegmentoPerId(id: Long): Segmento?
    
    @Query("SELECT * FROM segmenti WHERE pubblico = 1 ORDER BY numeroTentativi DESC")
    suspend fun getSegmentiPubblici(): List<Segmento>
    
    @Query("SELECT * FROM segmenti WHERE creatore = :creatore ORDER BY dataCreazione DESC")
    suspend fun getSegmentiPerCreatore(creatore: String): List<Segmento>
    
    @Query("""
        SELECT * FROM segmenti 
        WHERE tipo = :tipo 
        AND pubblico = 1 
        ORDER BY numeroTentativi DESC 
        LIMIT :limite
    """)
    suspend fun getSegmentiPopolariPerTipo(tipo: String, limite: Int = 20): List<Segmento>
    
    @Query("""
        SELECT * FROM segmenti 
        WHERE nome LIKE '%' || :query || '%' 
        OR descrizione LIKE '%' || :query || '%'
        AND pubblico = 1
        ORDER BY numeroTentativi DESC
    """)
    suspend fun cercaSegmenti(query: String): List<Segmento>
    
    @Query("""
        SELECT * FROM segmenti 
        WHERE distanza BETWEEN :distanzaMin AND :distanzaMax
        AND tipo = :tipo
        AND pubblico = 1
        ORDER BY stelle DESC, numeroTentativi DESC
    """)
    suspend fun getSegmentiPerDistanza(
        tipo: String, 
        distanzaMin: Float, 
        distanzaMax: Float
    ): List<Segmento>
    
    // Segmenti nelle vicinanze (query semplificata)
    @Query("""
        SELECT * FROM segmenti 
        WHERE pubblico = 1
        AND tipo = :tipo
        AND ABS(latitudineInizio - :lat) < :raggio 
        AND ABS(longitudineInizio - :lng) < :raggio
        ORDER BY numeroTentativi DESC
        LIMIT :limite
    """)
    suspend fun getSegmentiNelleVicinanze(
        lat: Double, 
        lng: Double, 
        raggio: Double = 0.01, // ~1km
        tipo: String,
        limite: Int = 10
    ): List<Segmento>
    
    // ==================== GESTIONE TENTATIVI ====================
    
    @Insert
    suspend fun inserisciTentativo(tentativo: TentativoSegmento): Long
    
    @Update
    suspend fun aggiornaTentativo(tentativo: TentativoSegmento)
    
    @Query("SELECT * FROM tentativi_segmento WHERE segmentoId = :segmentoId ORDER BY tempoImpiegato ASC")
    suspend fun getTentativiPerSegmento(segmentoId: String): List<TentativoSegmento>
    
    @Query("""
        SELECT * FROM tentativi_segmento 
        WHERE segmentoId = :segmentoId 
        AND utenteId = :utenteId 
        ORDER BY tempoImpiegato ASC 
        LIMIT 1
    """)
    suspend fun getMigliorTentativoAtleta(segmentoId: String, utenteId: String): TentativoSegmento?
    
    @Query("""
        SELECT * FROM tentativi_segmento 
        WHERE segmentoId = :segmentoId 
        AND utenteId = :utenteId 
        ORDER BY dataOra DESC
    """)
    suspend fun getTentativiAtletaPerSegmento(segmentoId: String, utenteId: String): List<TentativoSegmento>
    
    @Query("""
        SELECT * FROM tentativi_segmento 
        WHERE segmentoId = :segmentoId 
        ORDER BY tempoImpiegato ASC 
        LIMIT 1
    """)
    suspend fun getRecordSegmento(segmentoId: String): TentativoSegmento?
    
    @Query("""
        SELECT * FROM tentativi_segmento 
        WHERE utenteId = :utenteId 
        AND isPR = 1 
        ORDER BY dataOra DESC
    """)
    suspend fun getPersonalRecords(utenteId: String): List<TentativoSegmento>
    
    @Query("""
        SELECT * FROM tentativi_segmento 
        WHERE utenteId = :utenteId 
        AND isKOM = 1 
        ORDER BY dataOra DESC
    """)
    suspend fun getKOMQOM(utenteId: String): List<TentativoSegmento>
    
    // ==================== CLASSIFICHE ====================
    
    @Insert
    suspend fun inserisciClassifica(classifica: ClassificaSegmento)
    
    // Temporarily commented out due to table creation issues
    /*
    @Query("DELETE FROM classifica_segmento WHERE segmentoId = :segmentoId")
    suspend fun eliminaClassificheSegmento(segmentoId: String)
    
    @Query("""
        SELECT * FROM classifica_segmento 
        WHERE segmentoId = :segmentoId 
        AND categoria = :categoria 
        ORDER BY posizione ASC 
        LIMIT :limite
    """)
    suspend fun getClassificaSegmento(
        segmentoId: String, 
        categoria: String = "generale", 
        limite: Int = 100
    ): List<ClassificaSegmento>
    
    @Query("""
        SELECT posizione FROM classifica_segmento 
        WHERE segmentoId = :segmentoId 
        AND utenteId = :utenteId 
        AND categoria = :categoria
    """)
    suspend fun getPosizioneAtleta(
        segmentoId: String, 
        utenteId: String, 
        categoria: String = "generale"
    ): Int?
    */
    
    // ==================== STATISTICHE AVANZATE ====================
    
    @Query("""
        SELECT COUNT(*) FROM tentativi_segmento 
        WHERE segmentoId = :segmentoId
    """)
    suspend fun getNumeroTentativiSegmento(segmentoId: String): Int
    
    @Query("""
        SELECT COUNT(DISTINCT utenteId) FROM tentativi_segmento 
        WHERE segmentoId = :segmentoId
    """)
    suspend fun getNumeroAtletiSegmento(segmentoId: String): Int
    
    @Query("""
        SELECT AVG(tempoImpiegato) FROM tentativi_segmento 
        WHERE segmentoId = :segmentoId
    """)
    suspend fun getTempoMedioSegmento(segmentoId: String): Float?
    
    @Query("""
        SELECT AVG(velocitaMedia) FROM tentativi_segmento 
        WHERE segmentoId = :segmentoId 
        AND velocitaMedia IS NOT NULL
    """)
    suspend fun getVelocitaMediaSegmento(segmentoId: String): Float?
    
    // ==================== ANALISI PRESTAZIONI ====================
    
    @Query("""
        SELECT * FROM tentativi_segmento 
        WHERE utenteId = :utenteId 
        AND dataOra BETWEEN :dataInizio AND :dataFine
        ORDER BY dataOra DESC
    """)
    suspend fun getTentativiAtletaPerPeriodo(
        utenteId: String, 
        dataInizio: Long, 
        dataFine: Long
    ): List<TentativoSegmento>
    
    @Query("""
        SELECT COUNT(*) FROM tentativi_segmento 
        WHERE utenteId = :utenteId 
        AND posizione = 1 
        AND dataOra BETWEEN :dataInizio AND :dataFine
    """)
    suspend fun getNumeroPersonalRecordsPerPeriodo(
        utenteId: String, 
        dataInizio: Long, 
        dataFine: Long
    ): Int

    // ==================== GESTIONE RECORD ====================

    /*
    @Query("""
        SELECT s.id as segmento_id, s.nome as segmento_nome, s.descrizione as segmento_descrizione, 
               s.distanza as segmento_distanza, s.dislivelloPositivo as segmento_dislivelloPositivo, 
               s.tipo as segmento_tipo, s.puntoInizio as segmento_puntoInizio, s.puntoFine as segmento_puntoFine,
               s.latitudineInizio as segmento_latitudineInizio, s.longitudineInizio as segmento_longitudineInizio,
               s.latitudineFine as segmento_latitudineFine, s.longitudineFine as segmento_longitudineFine,
               s.difficolta as segmento_difficolta, s.stelle as segmento_stelle, s.pubblico as segmento_pubblico,
               s.creatore as segmento_creatore, s.dataCreazione as segmento_dataCreazione, s.numeroTentativi as segmento_numeroTentativi,
               t.tempo as migliorTempo, t.dataOra as dataMigliorTempo
        FROM segmenti s
        INNER JOIN tentativi_segmento t ON s.id = t.segmentoId
        WHERE t.atleta = :atleta
        AND t.posizione = 1
        ORDER BY t.dataOra DESC
    """)
    suspend fun getSegmentiConPersonalRecord(utenteId: String): List<SegmentoConRecord>
    */
    
    // ==================== TRANSAZIONI COMPLESSE ====================
    
    @Transaction
    suspend fun creaSegmentoCompleto(
        segmento: Segmento,
        tentativoIniziale: TentativoSegmento
    ): Long {
        val segmentoId = inserisciSegmento(segmento)
        val tentativoConSegmento = tentativoIniziale.copy(segmentoId = segmentoId)
        inserisciTentativo(tentativoConSegmento)
        return segmentoId
    }
    
    @Transaction
    suspend fun aggiornaClassificheSegmento(segmentoId: String) {
        // Elimina classifiche esistenti
        eliminaClassificheSegmento(segmentoId)
        
        // Ricostruisce le classifiche (implementazione semplificata)
        // In una implementazione reale, questo sarebbe più complesso
        val tentativi = getTentativiPerSegmento(segmentoId)
        tentativi.sortedBy { it.tempo }.forEachIndexed { index, tentativo ->
            val classifica = ClassificaSegmento(
                segmentoId = segmentoId,
                tentativoId = tentativo.id,
                utenteId = tentativo.utenteId,
                categoria = "generale",
                posizione = index + 1,
                tempo = tentativo.tempo,
                dataAggiornamento = System.currentTimeMillis()
            )
            inserisciClassifica(classifica)
        }
    }
}

// Data class per i risultati JOIN
data class SegmentoConRecord(
    val id: Long,
    val nome: String,
    val descrizione: String?,
    val distanza: Float,
    val dislivelloPositivo: Float,
    val tipo: String,
    val puntoInizio: String,
    val puntoFine: String,
    val latitudineInizio: Double,
    val longitudineInizio: Double,
    val latitudineFine: Double,
    val longitudineFine: Double,
    val difficolta: Int,
    val stelle: Float,
    val pubblico: Boolean,
    val creatore: String,
    val dataCreazione: Long,
    val numeroTentativi: Int,
    val migliorTempo: Float,
    val dataMigliorTempo: Long
)
