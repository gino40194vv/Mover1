package com.example.mover.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Qualità del segnale GPS
 */
enum class QualitaSegnale {
    ECCELLENTE,     // Precisione < 5m
    BUONA,          // Precisione 5-10m
    MEDIA,          // Precisione 10-20m
    SCARSA,         // Precisione > 20m
    MOLTO_SCARSA    // Precisione > 50m o segnale perso
}

/**
 * Rappresenta un singolo punto GPS registrato durante un'attività
 */
@Entity(
    tableName = "punti_gps",
    foreignKeys = [
        ForeignKey(
            entity = Attività::class,
            parentColumns = ["id"],
            childColumns = ["attivitaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["attivitaId"])]
)
data class PuntoGPS(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attivitaId: Long, // Riferimento all'attività
    
    // Coordinate GPS
    val latitudine: Double,
    val longitudine: Double,
    val altitudine: Double? = null, // Altitudine in metri
    val precisione: Float? = null, // Precisione GPS in metri
    
    // Timestamp
    val timestamp: Long, // Timestamp in millisecondi
    val tempoRelativo: Long, // Tempo dall'inizio dell'attività in millisecondi
    
    // Dati di movimento
    val velocita: Float? = null, // Velocità istantanea in m/s
    val direzione: Float? = null, // Direzione in gradi (0-360)
    val distanzaDalPrecedente: Float? = null, // Distanza dal punto precedente in metri
    val tempoDalPrecedente: Long? = null, // Tempo dal punto precedente in millisecondi
    
    // Dati biometrici (se disponibili)
    val frequenzaCardiaca: Int? = null, // BPM
    val cadenza: Float? = null, // Passi/min o RPM
    val potenza: Float? = null, // Potenza in Watt
    
    // Dati ambientali (se disponibili)
    val temperatura: Float? = null, // Temperatura in Celsius
    val umidita: Float? = null, // Umidità percentuale
    val pressione: Float? = null, // Pressione atmosferica in hPa
    
    // Metadati
    val tipoSegmento: TipoSegmento = TipoSegmento.MOVIMENTO, // Tipo di segmento
    val qualitaSegnale: QualitaSegnale = QualitaSegnale.BUONA // Qualità del segnale GPS
)

/**
 * Tipo di segmento del percorso
 */
enum class TipoSegmento {
    MOVIMENTO,      // Movimento normale
    PAUSA,          // In pausa
    FERMO,          // Fermo (velocità molto bassa)
    SALITA,         // In salita
    DISCESA,        // In discesa
    PIANURA,        // Terreno pianeggiante
    CURVA,          // Curva significativa
    SPRINT,         // Sprint/accelerazione
    RECUPERO        // Fase di recupero
}
