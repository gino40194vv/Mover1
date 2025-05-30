package com.example.mover.utils

import com.example.mover.data.PuntoGPS
import com.example.mover.data.TipoAttività
import kotlin.math.*

/**
 * Classe per calcolare metriche avanzate delle attività fisiche
 * Implementa algoritmi simili a quelli utilizzati da Strava
 */
class MetricsCalculator {
    
    companion object {
        private const val EARTH_RADIUS = 6371000.0 // Raggio della Terra in metri
        private const val MIN_SPEED_THRESHOLD = 0.5f // Soglia minima velocità per considerare movimento (m/s)
        private const val MAX_REASONABLE_SPEED = 50.0f // Velocità massima ragionevole (m/s) ~180 km/h
        private const val MIN_ELEVATION_CHANGE = 3.0f // Cambio minimo di elevazione per considerare salita/discesa
        
        /**
         * Calcola la distanza tra due punti GPS usando la formula di Haversine
         */
        fun calcolaDistanza(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return (EARTH_RADIUS * c).toFloat()
        }
        
        /**
         * Calcola la velocità istantanea tra due punti
         */
        fun calcolaVelocita(distanza: Float, tempoMs: Long): Float {
            if (tempoMs <= 0) return 0f
            return distanza / (tempoMs / 1000f)
        }
        
        /**
         * Calcola il pace (min/km) dalla velocità
         */
        fun calcolaPace(velocitaMs: Float): Float {
            if (velocitaMs <= 0) return 0f
            return (1000f / velocitaMs) / 60f // minuti per km
        }
        
        /**
         * Calcola la pendenza tra due punti
         */
        fun calcolaPendenza(distanzaOrizzontale: Float, differenzaAltitudine: Float): Float {
            if (distanzaOrizzontale <= 0) return 0f
            return (differenzaAltitudine / distanzaOrizzontale) * 100f // percentuale
        }
        
        /**
         * Calcola le calorie bruciate basandosi su tipo di attività, peso, durata e intensità
         */
        fun calcolaCalorie(
            tipoAttività: TipoAttività,
            pesoKg: Float,
            durataMinuti: Float,
            intensita: Float = 1.0f // fattore di intensità 0.5-2.0
        ): Int {
            val met = getMETValue(tipoAttività, intensita)
            return ((met * pesoKg * durataMinuti) / 60f).roundToInt()
        }
        
        /**
         * Ottiene il valore MET (Metabolic Equivalent of Task) per un tipo di attività
         */
        private fun getMETValue(tipoAttività: TipoAttività, intensita: Float): Float {
            val baseMET = when (tipoAttività) {
                TipoAttività.CAMMINATA -> 3.5f
                TipoAttività.CORSA -> 8.0f
                TipoAttività.CORSA_TAPIS_ROULANT -> 8.0f
                TipoAttività.TRAIL_RUNNING -> 10.0f
                TipoAttività.CICLISMO -> 7.5f
                TipoAttività.CICLISMO_INDOOR -> 7.0f
                TipoAttività.MOUNTAIN_BIKE -> 8.5f
                TipoAttività.NUOTO -> 8.0f
                TipoAttività.NUOTO_ACQUE_LIBERE -> 8.5f
                TipoAttività.ESCURSIONISMO -> 6.0f
                TipoAttività.SCI_ALPINO -> 7.0f
                TipoAttività.SCI_FONDO -> 9.0f
                TipoAttività.SNOWBOARD -> 6.0f
                TipoAttività.ALLENAMENTO_PESI -> 6.0f
                TipoAttività.YOGA -> 2.5f
                TipoAttività.PILATES -> 3.0f
                TipoAttività.CROSS_TRAINING -> 8.0f
                TipoAttività.CROSSFIT -> 10.0f
                TipoAttività.ARRAMPICATA -> 8.0f
                TipoAttività.TENNIS -> 7.0f
                TipoAttività.CALCIO -> 8.0f
                TipoAttività.BASKET -> 8.0f
                else -> 4.0f
            }
            return baseMET * intensita
        }
        
        /**
         * Calcola la cadenza media da una lista di punti GPS
         */
        fun calcolaCadenzaMedia(punti: List<PuntoGPS>): Float? {
            val cadenze = punti.mapNotNull { it.cadenza }.filter { it > 0 }
            return if (cadenze.isNotEmpty()) cadenze.average().toFloat() else null
        }
        
        /**
         * Calcola la potenza normalizzata (algoritmo simile a quello di TrainingPeaks)
         */
        fun calcolaPotenzaNormalizzata(potenze: List<Float>): Float? {
            if (potenze.isEmpty()) return null
            
            // Calcola la media mobile di 30 secondi
            val mediasMobili = mutableListOf<Float>()
            val finestra = 30
            
            for (i in 0 until potenze.size - finestra + 1) {
                val media = potenze.subList(i, i + finestra).average().toFloat()
                mediasMobili.add(media)
            }
            
            // Eleva alla quarta potenza, calcola la media e poi la radice quarta
            val mediaQuartaPotenza = mediasMobili.map { it.pow(4) }.average()
            return mediaQuartaPotenza.pow(0.25).toFloat()
        }
        
        /**
         * Calcola il Training Stress Score (TSS)
         */
        fun calcolaTSS(
            potenzaNormalizzata: Float,
            potenzaSoglia: Float, // FTP (Functional Threshold Power)
            durataOre: Float
        ): Float {
            val intensityFactor = potenzaNormalizzata / potenzaSoglia
            return (durataOre * potenzaNormalizzata * intensityFactor) / (potenzaSoglia * 3600) * 100
        }
        
        /**
         * Calcola le zone cardiache basate sulla frequenza cardiaca massima
         */
        fun calcolaZoneCardiache(fcMax: Int): Map<Int, IntRange> {
            return mapOf(
                1 to (fcMax * 0.50).toInt()..(fcMax * 0.60).toInt(), // Recupero attivo
                2 to (fcMax * 0.60).toInt()..(fcMax * 0.70).toInt(), // Base aerobica
                3 to (fcMax * 0.70).toInt()..(fcMax * 0.80).toInt(), // Aerobica
                4 to (fcMax * 0.80).toInt()..(fcMax * 0.90).toInt(), // Soglia anaerobica
                5 to (fcMax * 0.90).toInt()..fcMax                   // Neuromuscolare
            )
        }
        
        /**
         * Calcola le zone di potenza basate sulla FTP
         */
        fun calcolaZonePotenza(ftp: Float): Map<Int, IntRange> {
            return mapOf(
                1 to 0..(ftp * 0.55).toInt(),                        // Recupero attivo
                2 to (ftp * 0.55).toInt()..(ftp * 0.75).toInt(),     // Resistenza
                3 to (ftp * 0.75).toInt()..(ftp * 0.90).toInt(),     // Tempo
                4 to (ftp * 0.90).toInt()..(ftp * 1.05).toInt(),     // Soglia
                5 to (ftp * 1.05).toInt()..(ftp * 1.20).toInt(),     // VO2 Max
                6 to (ftp * 1.20).toInt()..Int.MAX_VALUE             // Anaerobica
            )
        }
        
        /**
         * Filtra i punti GPS per rimuovere outlier e dati errati
         */
        fun filtraPuntiGPS(punti: List<PuntoGPS>): List<PuntoGPS> {
            if (punti.size < 2) return punti
            
            val puntiFiltrati = mutableListOf<PuntoGPS>()
            puntiFiltrati.add(punti.first())
            
            for (i in 1 until punti.size) {
                val puntoCorrente = punti[i]
                val puntoPrecedente = puntiFiltrati.last()
                
                // Calcola distanza e velocità
                val distanza = calcolaDistanza(
                    puntoPrecedente.latitudine, puntoPrecedente.longitudine,
                    puntoCorrente.latitudine, puntoCorrente.longitudine
                )
                
                val tempoTrascorso = puntoCorrente.timestamp - puntoPrecedente.timestamp
                val velocita = if (tempoTrascorso > 0) {
                    distanza / (tempoTrascorso / 1000f)
                } else 0f
                
                // Filtra punti con velocità irrealistiche o precisione GPS scarsa
                val precisione = puntoCorrente.precisione ?: Float.MAX_VALUE
                
                if (velocita <= MAX_REASONABLE_SPEED && precisione <= 50f) {
                    puntiFiltrati.add(puntoCorrente.copy(
                        velocita = velocita,
                        distanzaDalPrecedente = distanza,
                        tempoDalPrecedente = tempoTrascorso
                    ))
                }
            }
            
            return puntiFiltrati
        }
        
        /**
         * Calcola statistiche aggregate da una lista di punti GPS
         */
        fun calcolaStatistiche(punti: List<PuntoGPS>): StatisticheAttività {
            if (punti.isEmpty()) {
                return StatisticheAttività()
            }
            
            val puntiFiltrati = filtraPuntiGPS(punti)
            
            val distanzaTotale = puntiFiltrati.sumOf { (it.distanzaDalPrecedente ?: 0f).toDouble() }.toFloat()
            val tempoTotale = if (puntiFiltrati.isNotEmpty()) {
                puntiFiltrati.last().timestamp - puntiFiltrati.first().timestamp
            } else 0L
            
            val velocita = puntiFiltrati.mapNotNull { it.velocita }.filter { it > MIN_SPEED_THRESHOLD }
            val velocitaMedia = if (velocita.isNotEmpty()) velocita.average().toFloat() else 0f
            val velocitaMassima = velocita.maxOrNull() ?: 0f
            
            val altitudini = puntiFiltrati.mapNotNull { it.altitudine }
            val dislivelloPositivo = calcolaDislivelloPositivo(altitudini)
            val dislivelloNegativo = calcolaDislivelloNegativo(altitudini)
            
            return StatisticheAttività(
                distanzaTotale = distanzaTotale,
                tempoTotale = tempoTotale,
                velocitaMedia = velocitaMedia,
                velocitaMassima = velocitaMassima,
                paceMedia = calcolaPace(velocitaMedia),
                dislivelloPositivo = dislivelloPositivo,
                dislivelloNegativo = dislivelloNegativo,
                altitudineMinima = altitudini.minOrNull()?.toFloat(),
                altitudineMassima = altitudini.maxOrNull()?.toFloat(),
                frequenzaCardiacaMedia = puntiFiltrati.mapNotNull { it.frequenzaCardiaca }.average().takeIf { !it.isNaN() }?.toInt(),
                frequenzaCardiacaMassima = puntiFiltrati.mapNotNull { it.frequenzaCardiaca }.maxOrNull(),
                cadenzaMedia = calcolaCadenzaMedia(puntiFiltrati),
                potenzaMedia = puntiFiltrati.mapNotNull { it.potenza }.average().takeIf { !it.isNaN() }?.toFloat(),
                potenzaMassima = puntiFiltrati.mapNotNull { it.potenza }.maxOrNull()
            )
        }
        
        /**
         * Calcola il dislivello positivo
         */
        private fun calcolaDislivelloPositivo(altitudini: List<Double>): Float {
            if (altitudini.size < 2) return 0f
            
            var dislivello = 0f
            for (i in 1 until altitudini.size) {
                val differenza = altitudini[i] - altitudini[i - 1]
                if (differenza > MIN_ELEVATION_CHANGE) {
                    dislivello += differenza.toFloat()
                }
            }
            return dislivello
        }
        
        /**
         * Calcola il dislivello negativo
         */
        private fun calcolaDislivelloNegativo(altitudini: List<Double>): Float {
            if (altitudini.size < 2) return 0f
            
            var dislivello = 0f
            for (i in 1 until altitudini.size) {
                val differenza = altitudini[i - 1] - altitudini[i]
                if (differenza > MIN_ELEVATION_CHANGE) {
                    dislivello += differenza.toFloat()
                }
            }
            return dislivello
        }
    }
}

/**
 * Data class per le statistiche aggregate di un'attività
 */
data class StatisticheAttività(
    val distanzaTotale: Float = 0f,
    val tempoTotale: Long = 0L,
    val velocitaMedia: Float = 0f,
    val velocitaMassima: Float = 0f,
    val paceMedia: Float = 0f,
    val dislivelloPositivo: Float = 0f,
    val dislivelloNegativo: Float = 0f,
    val altitudineMinima: Float? = null,
    val altitudineMassima: Float? = null,
    val frequenzaCardiacaMedia: Int? = null,
    val frequenzaCardiacaMassima: Int? = null,
    val cadenzaMedia: Float? = null,
    val potenzaMedia: Float? = null,
    val potenzaMassima: Float? = null
)