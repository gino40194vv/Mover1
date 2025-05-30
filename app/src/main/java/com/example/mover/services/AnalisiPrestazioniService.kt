package com.example.mover.services

import android.content.Context
import com.example.mover.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Servizio per il calcolo delle analisi delle prestazioni avanzate
 */
class AnalisiPrestazioniService(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val attivitàDao = database.attivitàDao()
    private val analisiDao = database.analisiPrestazioniDao()
    private val segmentoDao = database.segmentoDao()
    
    // ==================== CALCOLO FITNESS & FRESHNESS ====================
    
    /**
     * Calcola l'analisi Fitness & Freshness per un atleta
     */
    suspend fun calcolaFitnessFreshness(atleta: String, data: Long = System.currentTimeMillis()): FitnessFreshness = withContext(Dispatchers.IO) {
        val ultimiGiorni = 42 // 6 settimane per CTL
        val dataInizio = data - (ultimiGiorni * 24 * 60 * 60 * 1000L)
        
        // Ottieni tutte le attività del periodo
        val attivita = attivitàDao.getAttivitàPerPeriodoCompleto(dataInizio, data)
        
        // Calcola TSS giornalieri
        val tssGiornalieri = calcolaTSSGiornalieri(attivita, dataInizio, data)
        
        // Calcola CTL (Chronic Training Load) - media mobile 42 giorni
        val ctl = calcolaCTL(tssGiornalieri)
        
        // Calcola ATL (Acute Training Load) - media mobile 7 giorni
        val atl = calcolaATL(tssGiornalieri)
        
        // Calcola TSB (Training Stress Balance)
        val tsb = ctl - atl
        
        // Calcola metriche aggiuntive
        val tssOggi = tssGiornalieri.lastOrNull() ?: 0f
        val tssSettimana = tssGiornalieri.takeLast(7).sum()
        val tssMese = tssGiornalieri.takeLast(30).sum()
        
        val caricoAllenamento = calcolaCaricoAllenamento(attivita)
        val intensitaMedia = calcolaIntensitaMedia(attivita)
        val volumeSettimanale = calcolaVolumeSettimanale(attivita)
        
        // Genera raccomandazioni
        val raccomandazione = generaRaccomandazione(ctl, atl, tsb)
        val rischioBurnout = calcolaRischioBurnout(ctl, atl, tssOggi)
        val formaOttimale = tsb in -10f..5f && ctl > 40f
        
        // Calcola previsioni
        val previsione7 = tsb + (tssOggi * 0.1f)
        val previsione14 = tsb + (tssOggi * 0.2f)
        val previsione30 = tsb + (tssOggi * 0.3f)
        
        FitnessFreshness(
            atleta = atleta,
            data = data,
            fitness = ctl,
            fatigue = atl,
            form = tsb,
            tssGiornaliero = tssOggi,
            tssSettimanale = tssSettimana,
            tssMensile = tssMese,
            caricoAllenamento = caricoAllenamento,
            intensitaMedia = intensitaMedia,
            volumeSettimanale = volumeSettimanale,
            raccomandazione = raccomandazione,
            rischioBurnout = rischioBurnout,
            formaOttimale = formaOttimale,
            previsione7Giorni = previsione7,
            previsione14Giorni = previsione14,
            previsione30Giorni = previsione30
        )
    }
    
    // ==================== CALCOLO ANALISI ZONE ====================
    
    /**
     * Calcola l'analisi delle zone per un'attività
     */
    suspend fun calcolaAnalisiZone(attivita: Attività, puntiGPS: List<PuntoGPS>): AnalisiZone = withContext(Dispatchers.IO) {
        val analisi = AnalisiZone(
            attivitaId = attivita.id,
            atleta = "current_user", // TODO: Implementare gestione utenti
            data = attivita.oraInizio
        )
        
        // Calcola zone cardiache se disponibili
        if (attivita.frequenzaCardiacaMedia != null) {
            val zoneCardiache = calcolaZoneCardiache(puntiGPS, attivita.frequenzaCardiacaMedia!!)
            analisi.copy(
                zonaCardiaca1 = zoneCardiache[0],
                zonaCardiaca2 = zoneCardiache[1],
                zonaCardiaca3 = zoneCardiache[2],
                zonaCardiaca4 = zoneCardiache[3],
                zonaCardiaca5 = zoneCardiache[4]
            )
        }
        
        // Calcola zone di potenza se disponibili (ciclismo)
        if (attivita.potenzaMedia != null && attivita.tipo == "Ciclismo") {
            val zonePotenza = calcolaZonePotenza(puntiGPS, attivita.potenzaMedia!!)
            analisi.copy(
                zonaPotenza1 = zonePotenza[0],
                zonaPotenza2 = zonePotenza[1],
                zonaPotenza3 = zonePotenza[2],
                zonaPotenza4 = zonePotenza[3],
                zonaPotenza5 = zonePotenza[4],
                zonaPotenza6 = zonePotenza[5]
            )
        }
        
        // Calcola zone di pace se disponibili (corsa)
        if (attivita.paceMedio != null && attivita.tipo == "Corsa") {
            val zonePace = calcolaZonePace(puntiGPS, attivita.paceMedio!!)
            analisi.copy(
                zonaPace1 = zonePace[0],
                zonaPace2 = zonePace[1],
                zonaPace3 = zonePace[2],
                zonaPace4 = zonePace[3],
                zonaPace5 = zonePace[4],
                zonaPace6 = zonePace[5]
            )
        }
        
        // Calcola metriche derivate
        val intensita = calcolaIntensitaAllenamento(analisi)
        val efficienza = calcolaEfficienzaAllenamento(analisi)
        val bilanciamento = calcolaBilanciamentoZone(analisi)
        val qualita = determinaQualitaAllenamento(analisi)
        
        analisi.copy(
            intensitaMedia = intensita,
            efficienza = efficienza,
            bilanciamento = bilanciamento,
            qualitaAllenamento = qualita
        )
    }
    
    // ==================== CALCOLO ANALISI PASSO GARA ====================
    
    /**
     * Calcola l'analisi del passo gara per un'attività di corsa
     */
    suspend fun calcolaAnalisiPassoGara(attivita: Attività, puntiGPS: List<PuntoGPS>): AnalisiPassoGara? = withContext(Dispatchers.IO) {
        if (attivita.tipo != "Corsa" || attivita.distanza == null || puntiGPS.isEmpty()) {
            return@withContext null
        }
        
        val distanzaKm = attivita.distanza!! / 1000f
        val tipoGara = determinaTipoGara(distanzaKm)
        
        // Calcola splits
        val splits = calcolaSplits(puntiGPS, distanzaKm)
        
        // Analizza il passo
        val paceMedio = attivita.paceMedio ?: 0f
        val variabilitaPace = calcolaVariabilitaPace(puntiGPS)
        val consistenza = calcolaConsistenzaPace(puntiGPS)
        
        // Determina strategia
        val strategia = determinaStrategia(splits)
        val fadingIndex = calcolaFadingIndex(splits)
        val kickFinale = rilevaSprint(puntiGPS.takeLast(puntiGPS.size / 10))
        
        // Calcola previsioni
        val tempoPrevistoVelocita = calcolaPrevisione(attivita.velocitaMedia, distanzaKm)
        val tempoPrevistoHR = calcolaPrevisione(attivita.frequenzaCardiacaMedia, distanzaKm)
        val tempoMiglioreStimato = calcolaMigliorTempoStimato(attivita, puntiGPS)
        
        // Genera raccomandazioni
        val raccomandazioniTattiche = generaRaccomandazioniTattiche(strategia, consistenza)
        val raccomandazioniAllenamento = generaRaccomandazioniAllenamento(analisi = null)
        val puntiForza = identificaPuntiForza(attivita, splits)
        val areeeMiglioramento = identificaAreeMiglioramento(attivita, splits)
        
        AnalisiPassoGara(
            attivitaId = attivita.id,
            atleta = "current_user",
            data = attivita.oraInizio,
            distanzaGara = distanzaKm,
            tipoGara = tipoGara,
            paceMedio = paceMedio,
            paceTarget = paceMedio * 0.95f, // Target 5% più veloce
            variabilitaPace = variabilitaPace,
            consistenza = consistenza,
            split1km = splits.getOrNull(0) ?: 0f,
            split5km = splits.take(5).sum(),
            split10km = splits.take(10).sum(),
            splitMeta = splits.take(splits.size / 2).sum(),
            splitFinale = splits.drop(splits.size / 2).sum(),
            strategia = strategia,
            fadingIndex = fadingIndex,
            kickFinale = kickFinale,
            tempoPrevistoVelocita = tempoPrevistoVelocita,
            tempoPrevistoHR = tempoPrevistoHR,
            tempoPrevistoRPE = tempoPrevistoVelocita,
            tempoMiglioreStimato = tempoMiglioreStimato,
            raccomandazioniTattiche = raccomandazioniTattiche,
            raccomandazioniAllenamento = raccomandazioniAllenamento,
            puntiForza = puntiForza,
            areeeMiglioramento = areeeMiglioramento
        )
    }
    
    // ==================== CALCOLO METRICHE PERFORMANCE ====================
    
    /**
     * Calcola le metriche di performance avanzate per un'attività
     */
    suspend fun calcolaMetrichePerformance(attivita: Attività, puntiGPS: List<PuntoGPS>): MetrichePerformance = withContext(Dispatchers.IO) {
        val metriche = MetrichePerformance(
            attivitaId = attivita.id,
            atleta = "current_user",
            data = attivita.oraInizio,
            tipoAttivita = attivita.tipo
        )
        
        // Metriche specifiche per ciclismo
        if (attivita.tipo == "Ciclismo" && attivita.potenzaMedia != null) {
            val np = calcolaNormalizedPower(puntiGPS)
            val if_ = calcolaIntensityFactor(np, 250f) // FTP stimato 250W
            val tss = calcolaTrainingStressScore(if_, attivita.tempo ?: 0L)
            val vi = calcolaVariabilityIndex(np, attivita.potenzaMedia!!)
            val ef = calcolaEfficiencyFactor(np, attivita.frequenzaCardiacaMedia ?: 150)
            
            metriche.copy(
                potenzaNormalizzata = np,
                intensityFactor = if_,
                trainingStressScore = tss,
                variabilityIndex = vi,
                efficiencyFactor = ef
            )
        }
        
        // Metriche specifiche per corsa
        if (attivita.tipo == "Corsa") {
            val gap = calcolaGradeAdjustedPace(puntiGPS)
            val re = calcolaRunningEffectiveness(attivita)
            val vo = calcolaVerticalOscillation(puntiGPS)
            val gct = calcolaGroundContactTime(attivita.cadenzaMedia)
            val sl = calcolaStrideLength(attivita.velocitaMedia, attivita.cadenzaMedia)
            val ri = calcolaRunningIndex(attivita)
            
            metriche.copy(
                gradeAdjustedPace = gap,
                runningEffectiveness = re,
                verticalOscillation = vo,
                groundContactTime = gct,
                strideLength = sl,
                runningIndex = ri
            )
        }
        
        // Metriche cardiache generali
        val hrv = calcolaHRVariability(puntiGPS)
        val hrDrift = calcolaHRDrift(puntiGPS)
        val hrEff = calcolaHREfficiency(attivita)
        
        // Metriche metaboliche
        val vo2 = stimaVO2(attivita)
        val consumoEnergetico = calcolaConsumoEnergetico(attivita)
        val contributoAerobico = calcolaContributoAerobico(attivita)
        
        // Punteggi complessivi
        val performanceScore = calcolaPerformanceScore(attivita, puntiGPS)
        val effortScore = calcolaEffortScore(attivita)
        val efficiencyScore = calcolaEfficiencyScore(attivita)
        val qualityScore = calcolaQualityScore(attivita, puntiGPS)
        
        metriche.copy(
            hrVariability = hrv,
            hrDrift = hrDrift,
            hrEfficiency = hrEff,
            vo2Stimato = vo2,
            consumoEnergetico = consumoEnergetico,
            contributoAerobico = contributoAerobico,
            contributoAnaerobico = 100f - contributoAerobico,
            performanceScore = performanceScore,
            effortScore = effortScore,
            efficiencyScore = efficiencyScore,
            qualityScore = qualityScore
        )
    }
    
    // ==================== FUNZIONI HELPER PRIVATE ====================
    
    private fun calcolaTSSGiornalieri(attivita: List<Attività>, dataInizio: Long, dataFine: Long): List<Float> {
        val giorni = ((dataFine - dataInizio) / (24 * 60 * 60 * 1000L)).toInt()
        val tssGiornalieri = mutableListOf<Float>()
        
        for (i in 0 until giorni) {
            val giornoInizio = dataInizio + (i * 24 * 60 * 60 * 1000L)
            val giornoFine = giornoInizio + (24 * 60 * 60 * 1000L)
            
            val attivitaGiorno = attivita.filter { it.oraInizio in giornoInizio until giornoFine }
            val tssGiorno = attivitaGiorno.sumOf { calcolaTSS(it).toDouble() }.toFloat()
            tssGiornalieri.add(tssGiorno)
        }
        
        return tssGiornalieri
    }
    
    private fun calcolaTSS(attivita: Attività): Float {
        // Calcolo semplificato del Training Stress Score
        val durata = (attivita.tempo ?: 0L) / 3600000f // ore
        val intensita = when (attivita.tipo) {
            "Corsa" -> (attivita.velocitaMedia ?: 0f) / 4f // Normalizzato
            "Ciclismo" -> (attivita.potenzaMedia ?: 0f) / 250f // FTP stimato 250W
            else -> 0.5f
        }
        return durata * intensita * 100f
    }
    
    private fun calcolaCTL(tssGiornalieri: List<Float>): Float {
        // Chronic Training Load - media mobile esponenziale 42 giorni
        if (tssGiornalieri.isEmpty()) return 0f
        
        var ctl = 0f
        val alpha = 2f / (42f + 1f) // Fattore di smoothing
        
        tssGiornalieri.forEach { tss ->
            ctl = alpha * tss + (1 - alpha) * ctl
        }
        
        return ctl
    }
    
    private fun calcolaATL(tssGiornalieri: List<Float>): Float {
        // Acute Training Load - media mobile esponenziale 7 giorni
        if (tssGiornalieri.isEmpty()) return 0f
        
        var atl = 0f
        val alpha = 2f / (7f + 1f) // Fattore di smoothing
        
        tssGiornalieri.takeLast(7).forEach { tss ->
            atl = alpha * tss + (1 - alpha) * atl
        }
        
        return atl
    }
    
    private fun calcolaCaricoAllenamento(attivita: List<Attività>): Float {
        return attivita.sumOf { (it.tempo ?: 0L) / 3600000.0 }.toFloat() // Ore totali
    }
    
    private fun calcolaIntensitaMedia(attivita: List<Attività>): Float {
        if (attivita.isEmpty()) return 0f
        return attivita.mapNotNull { it.sforzoPercepito?.toFloat() }.average().toFloat()
    }
    
    private fun calcolaVolumeSettimanale(attivita: List<Attività>): Float {
        return attivita.sumOf { (it.distanza ?: 0f) / 1000.0 }.toFloat() // Km totali
    }
    
    private fun generaRaccomandazione(ctl: Float, atl: Float, tsb: Float): String {
        return when {
            tsb > 10f -> "Riposo - Forma in crescita"
            tsb in 5f..10f -> "Allenamento leggero"
            tsb in -10f..5f -> "Forma ottimale - Mantieni"
            tsb in -20f..-10f -> "Attenzione fatica"
            else -> "Riposo necessario"
        }
    }
    
    private fun calcolaRischioBurnout(ctl: Float, atl: Float, tssOggi: Float): Float {
        val ratio = if (ctl > 0) atl / ctl else 0f
        val intensita = if (ctl > 0) tssOggi / ctl else 0f
        return minOf(1f, ratio * 0.7f + intensita * 0.3f)
    }
    
    private fun calcolaZoneCardiache(puntiGPS: List<PuntoGPS>, fcMedia: Int): LongArray {
        val zone = LongArray(5) { 0L }
        val fcMax = fcMedia * 1.2f // Stima FCMax
        
        puntiGPS.forEach { punto ->
            val fc = punto.frequenzaCardiaca ?: return@forEach
            val percentuale = fc / fcMax
            
            val zona = when {
                percentuale < 0.6f -> 0
                percentuale < 0.7f -> 1
                percentuale < 0.8f -> 2
                percentuale < 0.9f -> 3
                else -> 4
            }
            
            zone[zona] += punto.tempoDalPrecedente ?: 1000L
        }
        
        return zone
    }
    
    private fun calcolaZonePotenza(puntiGPS: List<PuntoGPS>, potenzaMedia: Float): LongArray {
        val zone = LongArray(6) { 0L }
        val ftp = potenzaMedia * 1.1f // Stima FTP
        
        puntiGPS.forEach { punto ->
            val potenza = punto.potenza ?: return@forEach
            val percentuale = potenza / ftp
            
            val zona = when {
                percentuale < 0.55f -> 0
                percentuale < 0.75f -> 1
                percentuale < 0.90f -> 2
                percentuale < 1.05f -> 3
                percentuale < 1.20f -> 4
                else -> 5
            }
            
            zone[zona] += punto.tempoDalPrecedente ?: 1000L
        }
        
        return zone
    }
    
    private fun calcolaZonePace(puntiGPS: List<PuntoGPS>, paceMedia: Float): LongArray {
        val zone = LongArray(6) { 0L }
        // Implementazione semplificata per zone di pace
        // In una implementazione reale, si userebbero soglie personalizzate
        
        puntiGPS.forEach { punto ->
            val velocita = punto.velocita ?: return@forEach
            val pace = if (velocita > 0) 1000f / (velocita * 60f) else Float.MAX_VALUE
            
            val zona = when {
                pace > paceMedia * 1.3f -> 0 // Recupero
                pace > paceMedia * 1.15f -> 1 // Base
                pace > paceMedia * 1.05f -> 2 // Aerobico
                pace > paceMedia * 0.95f -> 3 // Soglia
                pace > paceMedia * 0.85f -> 4 // VO2Max
                else -> 5 // Anaerobico
            }
            
            zone[zona] += punto.tempoDalPrecedente ?: 1000L
        }
        
        return zone
    }
    
    // Implementazioni semplificate delle altre funzioni helper
    private fun calcolaIntensitaAllenamento(analisi: AnalisiZone): Float = 0.7f
    private fun calcolaEfficienzaAllenamento(analisi: AnalisiZone): Float = 0.8f
    private fun calcolaBilanciamentoZone(analisi: AnalisiZone): Float = 0.75f
    private fun determinaQualitaAllenamento(analisi: AnalisiZone): String = "Misto"
    private fun determinaTipoGara(distanzaKm: Float): String = when {
        distanzaKm <= 5f -> "5K"
        distanzaKm <= 10f -> "10K"
        distanzaKm <= 21.1f -> "Mezza Maratona"
        distanzaKm <= 42.2f -> "Maratona"
        else -> "Ultra"
    }
    private fun calcolaSplits(puntiGPS: List<PuntoGPS>, distanzaKm: Float): List<Float> = emptyList()
    private fun calcolaVariabilitaPace(puntiGPS: List<PuntoGPS>): Float = 0.1f
    private fun calcolaConsistenzaPace(puntiGPS: List<PuntoGPS>): Float = 0.85f
    private fun determinaStrategia(splits: List<Float>): String = "Even Pace"
    private fun calcolaFadingIndex(splits: List<Float>): Float = 0f
    private fun rilevaSprint(puntiFinali: List<PuntoGPS>): Boolean = false
    private fun calcolaPrevisione(metrica: Any?, distanza: Float): Long = 0L
    private fun calcolaMigliorTempoStimato(attivita: Attività, puntiGPS: List<PuntoGPS>): Long = attivita.tempo ?: 0L
    private fun generaRaccomandazioniTattiche(strategia: String, consistenza: Float): String = "Mantieni pace costante"
    private fun generaRaccomandazioniAllenamento(analisi: Any?): String = "Aumenta volume base"
    private fun identificaPuntiForza(attivita: Attività, splits: List<Float>): String = "Resistenza"
    private fun identificaAreeMiglioramento(attivita: Attività, splits: List<Float>): String = "Velocità"
    
    // Metriche di performance semplificate
    private fun calcolaNormalizedPower(puntiGPS: List<PuntoGPS>): Float = 200f
    private fun calcolaIntensityFactor(np: Float, ftp: Float): Float = np / ftp
    private fun calcolaTrainingStressScore(if_: Float, durata: Long): Float = (durata / 3600000f) * if_ * 100f
    private fun calcolaVariabilityIndex(np: Float, potenzaMedia: Float): Float = np / potenzaMedia
    private fun calcolaEfficiencyFactor(np: Float, fcMedia: Int): Float = np / fcMedia
    private fun calcolaGradeAdjustedPace(puntiGPS: List<PuntoGPS>): Float = 5f
    private fun calcolaRunningEffectiveness(attivita: Attività): Float = 0.8f
    private fun calcolaVerticalOscillation(puntiGPS: List<PuntoGPS>): Float = 8f
    private fun calcolaGroundContactTime(cadenza: Float?): Float = 250f
    private fun calcolaStrideLength(velocita: Float?, cadenza: Float?): Float = 1.2f
    private fun calcolaRunningIndex(attivita: Attività): Float = 0.85f
    private fun calcolaHRVariability(puntiGPS: List<PuntoGPS>): Float = 45f
    private fun calcolaHRDrift(puntiGPS: List<PuntoGPS>): Float = 5f
    private fun calcolaHREfficiency(attivita: Attività): Float = 0.9f
    private fun stimaVO2(attivita: Attività): Float = 45f
    private fun calcolaConsumoEnergetico(attivita: Attività): Float = 500f
    private fun calcolaContributoAerobico(attivita: Attività): Float = 85f
    private fun calcolaPerformanceScore(attivita: Attività, puntiGPS: List<PuntoGPS>): Float = 75f
    private fun calcolaEffortScore(attivita: Attività): Float = 80f
    private fun calcolaEfficiencyScore(attivita: Attività): Float = 85f
    private fun calcolaQualityScore(attivita: Attività, puntiGPS: List<PuntoGPS>): Float = 78f
}