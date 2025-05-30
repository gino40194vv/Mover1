package com.example.mover.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entità che rappresenta un segmento di attività per analisi dettagliate
 * Simile ai segmenti di Strava per confronti e classifiche
 */
@Entity(
    tableName = "segmenti_attività",
    foreignKeys = [
        ForeignKey(
            entity = Attività::class,
            parentColumns = ["id"],
            childColumns = ["attivitàId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["attivitàId"])]
)
data class SegmentoAttività(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val attivitàId: Long,
    val nome: String,
    val descrizione: String? = null,
    
    // Posizione del segmento nell'attività
    val puntoInizio: Int, // Indice del punto GPS di inizio
    val puntoFine: Int,   // Indice del punto GPS di fine
    
    // Metriche del segmento
    val distanza: Double = 0.0, // metri
    val durata: Long = 0, // millisecondi
    val velocitàMedia: Double = 0.0, // m/s
    val velocitàMassima: Double = 0.0, // m/s
    val dislivelloPositivo: Double = 0.0, // metri
    val dislivelloNegativo: Double = 0.0, // metri
    val pendenzaMedia: Double = 0.0, // percentuale
    val pendenzaMassima: Double = 0.0, // percentuale
    
    // Metriche biometriche (se disponibili)
    val frequenzaCardiacaMedia: Int? = null, // bpm
    val frequenzaCardiacaMassima: Int? = null, // bpm
    val cadenzaMedia: Int? = null, // passi/min o RPM
    val potenzaMedia: Double? = null, // watt
    val potenzaMassima: Double? = null, // watt
    val potenzaNormalizzata: Double? = null, // watt
    
    // Classificazione e confronti
    val tempoPersonaleBest: Boolean = false,
    val posizione: Int? = null, // Posizione in classifica (se pubblico)
    val numeroPartecipanti: Int? = null, // Numero totale partecipanti
    
    // Metadati
    val pubblico: Boolean = false,
    val dataCreazione: Long = System.currentTimeMillis()
)