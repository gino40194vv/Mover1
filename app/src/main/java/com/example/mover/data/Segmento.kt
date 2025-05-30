package com.example.mover.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Rappresenta un segmento Strava-like per confronti e classifiche
 */
@Entity(
    tableName = "segmenti",
    indices = [
        Index(value = ["nome"]),
        Index(value = ["tipo"]),
        Index(value = ["distanza"]),
        Index(value = ["creatore"])
    ]
)
data class Segmento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // Informazioni base del segmento
    val nome: String,
    val descrizione: String? = null,
    val tipo: String, // "Corsa", "Ciclismo", "Camminata", etc.
    val creatore: String, // ID dell'utente che ha creato il segmento
    val dataCreazione: Long,
    
    // Dati geografici
    val latitudineInizio: Double,
    val longitudineInizio: Double,
    val latitudineFine: Double,
    val longitudineFine: Double,
    val puntiPercorso: String, // JSON array dei punti GPS del segmento
    
    // Metriche del segmento
    val distanza: Float, // Distanza in metri
    val dislivelloPositivo: Float? = null,
    val dislivelloNegativo: Float? = null,
    val altitudineMinima: Float? = null,
    val altitudineMassima: Float? = null,
    val pendenzaMedia: Float? = null,
    val pendenzaMassima: Float? = null,
    
    // Metadati
    val pubblico: Boolean = true, // Se il segmento è pubblico o privato
    val verificato: Boolean = false, // Se il segmento è stato verificato
    val pericoloso: Boolean = false, // Se il segmento è considerato pericoloso
    val stelle: Float = 0f, // Valutazione media (0-5 stelle)
    val numeroTentativi: Int = 0, // Numero totale di tentativi
    val numeroAtleti: Int = 0, // Numero di atleti unici che hanno completato il segmento
    
    // Record del segmento
    val recordTempoUomini: Long? = null, // Tempo record uomini in millisecondi
    val recordAtletaUomini: String? = null, // ID dell'atleta con il record uomini
    val recordDataUomini: Long? = null, // Data del record uomini
    val recordTempoDonne: Long? = null, // Tempo record donne in millisecondi
    val recordAtletaDonne: String? = null, // ID dell'atleta con il record donne
    val recordDataDonne: Long? = null, // Data del record donne
    
    // Condizioni e regole
    val condizioniMeteo: String? = null, // Condizioni meteo ideali
    val superficieTerreno: String? = null, // Tipo di superficie (asfalto, sterrato, etc.)
    val difficolta: Int = 1, // Difficoltà da 1 a 5
    val categoria: String? = null, // Categoria del segmento (sprint, salita, etc.)
    
    // Sincronizzazione
    val sincronizzatoStrava: Boolean = false,
    val idStrava: String? = null,
    val ultimaSincronizzazione: Long? = null
)

/**
 * Rappresenta un tentativo di un atleta su un segmento
 */
@Entity(
    tableName = "tentativi_segmento",
    indices = [
        Index(value = ["segmentoId"]),
        Index(value = ["attivitaId"]),
        Index(value = ["atleta"]),
        Index(value = ["tempo"]),
        Index(value = ["dataCompletamento"])
    ]
)
data class TentativoSegmento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    val segmentoId: Long, // Riferimento al segmento
    val attivitaId: Long, // Riferimento all'attività in cui è stato completato
    val atleta: String, // ID dell'atleta
    
    // Dati del tentativo
    val tempo: Long, // Tempo impiegato in millisecondi
    val dataCompletamento: Long,
    val velocitaMedia: Float? = null,
    val velocitaMassima: Float? = null,
    val frequenzaCardiacaMedia: Int? = null,
    val frequenzaCardiacaMassima: Int? = null,
    val cadenzaMedia: Float? = null,
    val potenzaMedia: Float? = null,
    val potenzaMassima: Float? = null,
    val calorie: Int? = null,
    
    // Posizione in classifica
    val posizioneGenerale: Int? = null,
    val posizioneCategoria: Int? = null, // Posizione nella categoria di età/sesso
    val posizionePR: Boolean = false, // Se è un Personal Record
    val posizioneKOM: Boolean = false, // Se è King/Queen of Mountain
    
    // Condizioni del tentativo
    val condizioniMeteo: String? = null,
    val temperatura: Float? = null,
    val vento: String? = null,
    val equipaggiamento: String? = null,
    val note: String? = null,
    
    // Analisi del tentativo
    val sforzoPercepito: Int? = null, // RPE 1-10
    val sensazione: Int? = null, // Sensazione 1-10
    val strategia: String? = null // Strategia utilizzata
)

/**
 * Classifica di un segmento per categoria
 */
@Entity(
    tableName = "classifiche_segmento",
    indices = [
        Index(value = ["segmentoId"]),
        Index(value = ["categoria"]),
        Index(value = ["posizione"])
    ]
)
data class ClassificaSegmento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    val segmentoId: Long,
    val tentativoId: Long,
    val atleta: String,
    val categoria: String, // "generale", "uomini", "donne", "età_18-29", etc.
    val posizione: Int,
    val tempo: Long,
    val dataAggiornamento: Long
)