package com.example.mover.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attività")
data class Attività(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    
    // Informazioni base
    val tipo: String, // Tipo di attività (corsa, ciclismo, nuoto, etc.)
    val sottotipo: String? = null, // Sottotipo (es. "tapis roulant" per corsa indoor)
    val oraInizio: Long,
    var oraFine: Long,
    val Automatica: Boolean = false,
    
    // Dati temporali
    val tempo: Long? = null, // Tempo totale in millisecondi
    val tempoInMovimento: Long? = null, // Tempo effettivo in movimento
    val tempoInPausa: Long? = null, // Tempo totale in pausa
    
    // Dati di distanza e velocità
    val distanza: Float? = null, // Distanza in metri
    val velocitaMedia: Float? = null, // Velocità media in m/s
    val velocitaMassima: Float? = null, // Velocità massima in m/s
    val paceMedio: Float? = null, // Pace medio in min/km
    
    // Dati di elevazione
    val dislivelloPositivo: Float? = null, // Dislivello positivo in metri
    val dislivelloNegativo: Float? = null, // Dislivello negativo in metri
    val altitudineMinima: Float? = null, // Altitudine minima in metri
    val altitudineMassima: Float? = null, // Altitudine massima in metri
    
    // Dati biometrici e sensori
    val calorie: Int? = null, // Calorie consumate
    val frequenzaCardiacaMedia: Int? = null, // BPM medio
    val frequenzaCardiacaMassima: Int? = null, // BPM massimo
    val cadenzaMedia: Float? = null, // Passi/min o RPM per ciclismo
    val cadenzaMassima: Float? = null, // Cadenza massima
    val potenzaMedia: Float? = null, // Potenza media in Watt
    val potenzaMassima: Float? = null, // Potenza massima in Watt
    
    // Dati specifici per attività
    val passi: Int? = null, // Numero di passi (per camminata/corsa)
    val volteSeduto: Int? = null, // Numero di volte seduto
    val numeroVasche: Int? = null, // Per nuoto in piscina
    val stileNuoto: String? = null, // Stile di nuoto
    
    // Dati ambientali
    val temperaturaMedia: Float? = null, // Temperatura in Celsius
    val temperaturaMinima: Float? = null,
    val temperaturaMassima: Float? = null,
    val umidita: Float? = null, // Umidità percentuale
    val velocitaVento: Float? = null, // Velocità del vento in m/s
    val direzioneVento: Float? = null, // Direzione del vento in gradi
    
    // Dati GPS e percorso
    val percorsoGpx: String? = null, // Percorso in formato GPX
    val latitudineInizio: Double? = null,
    val longitudineInizio: Double? = null,
    val latitudineFine: Double? = null,
    val longitudineFine: Double? = null,
    val precisione: Float? = null, // Precisione GPS media
    
    // Metadati
    val note: String? = null, // Note dell'utente
    val foto: String? = null, // Path delle foto associate
    val equipaggiamento: String? = null, // Equipaggiamento utilizzato (scarpe, bici, etc.)
    val condizioni: String? = null, // Condizioni (soleggiato, piovoso, etc.)
    val sensazione: Int? = null, // Sensazione da 1 a 10
    val sforzoPercepito: Int? = null, // RPE (Rate of Perceived Exertion) 1-10
    
    // Dati di allenamento avanzati
    val zonaCardiaca1: Long? = null, // Tempo in zona cardiaca 1 (ms)
    val zonaCardiaca2: Long? = null, // Tempo in zona cardiaca 2 (ms)
    val zonaCardiaca3: Long? = null, // Tempo in zona cardiaca 3 (ms)
    val zonaCardiaca4: Long? = null, // Tempo in zona cardiaca 4 (ms)
    val zonaCardiaca5: Long? = null, // Tempo in zona cardiaca 5 (ms)
    val zonaPotenza1: Long? = null, // Tempo in zona potenza 1 (ms)
    val zonaPotenza2: Long? = null, // Tempo in zona potenza 2 (ms)
    val zonaPotenza3: Long? = null, // Tempo in zona potenza 3 (ms)
    val zonaPotenza4: Long? = null, // Tempo in zona potenza 4 (ms)
    val zonaPotenza5: Long? = null, // Tempo in zona potenza 5 (ms)
    
    // Training Load e Fitness
    val trainingLoad: Float? = null, // Carico di allenamento
    val intensityFactor: Float? = null, // Fattore di intensità
    val tss: Float? = null, // Training Stress Score
    val normalizedPower: Float? = null, // Potenza normalizzata
    
    // Dati di recupero
    val variabilitaFrequenzaCardiaca: Float? = null, // HRV
    val tempoRecupero: Long? = null, // Tempo di recupero stimato in ore
    
    // Sincronizzazione
    val sincronizzatoStrava: Boolean = false,
    val idStrava: String? = null,
    val ultimaSincronizzazione: Long? = null
)
