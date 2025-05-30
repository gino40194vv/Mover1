package com.example.mover

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.example.mover.data.AppDatabase
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class StatsManager(
    private val context: Context,
    private val db: AppDatabase,
    private val uiComponents: StatisticsUIComponents

) {
    // Se hai bisogno di tracciare lo stato dell’attività corrente
    private var attivitàCorrente: String? = null
    private var oraInizio: Long = 0L
    private var currentStepCount = 0
    private var currentSteps = 0
    private var distanzaCorrente: Float = 0f
    var accumulatedSteps: Int = 0
    var accumulatedDistance: Float = 0f
    var accumulatedSittingTime: Long = 0L
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val goalSteps: Int
        get() = prefs.getInt("obiettivo_passi", 10000)

    private val goalRunningDistance: Float
        get() = prefs.getFloat("obiettivo_corsa", 5.0f) * 1000

    private val goalMaxSittingTime: Long
        get() = prefs.getInt("limite_seduto", 8) * 3600000L

    companion object {
        @Volatile
        private var instance: StatsManager? = null


        fun getInstance(context: Context, db: AppDatabase, uiComponents: StatisticsUIComponents): StatsManager {
            return instance ?: synchronized(this) {
                instance ?: StatsManager(context.applicationContext, db, uiComponents).also { instance = it }
            }
        }
    }
    init {
        // Carica i valori salvati da SharedPreferences
        val prefs = context.getSharedPreferences("progressPrefs", Context.MODE_PRIVATE)
        accumulatedSteps = prefs.getInt("accumulatedSteps", 0)
        accumulatedDistance = prefs.getFloat("accumulatedDistance", 0f)
        accumulatedSittingTime = prefs.getLong("accumulatedSittingTime", 0L)
        Log.d("StatsManager", "Caricati: accumulatedSteps=$accumulatedSteps, accumulatedDistance=$accumulatedDistance, accumulatedSittingTime=$accumulatedSittingTime")
    }


    /**
     * Esempio di data class per mappare i TextView della UI.
     * Assicurati di sostituire i campi con quelli davvero presenti nei tuoi layout.
     */
    data class StatisticsUIComponents(
        // CARD in alto (contatori compatti)
        val txtPassiGiornalieriCompact: TextView? = null,
        val txtSeduteGiornalieriCompact: TextView? = null,
        val txtCorsaGiornaliereCompact: TextView? = null,
        val txtSeduteGiornaliereCompact: TextView? = null,


        // Statistiche Passi
        val txtPassiGiornalieri: TextView? = null,
        val txtTempoCamminataGiornaliere: TextView? = null,
        val txtPassiSettimanali: TextView? = null,
        val txtTempoCamminataSettimanali: TextView? = null,

        // Statistiche Sedute
        val txtNumeroSeduteGiornaliere: TextView? = null,
        val txtTempoSeduteGiornaliere: TextView? = null,
        val txtNumeroSeduteSettimanali: TextView? = null,
        val txtTempoSeduteSettimanali: TextView? = null,

        // Statistiche Corsa
        val txtDistanzaCorsaSettimanale: TextView? = null,
        val txtTempoCorsaSettimanale: TextView? = null,

        // Statistiche Guida
        val txtDistanzaGuidaSettimanale: TextView? = null,
        val txtTempoGuidaSettimanale: TextView? = null,
        val progressPassi: CircularProgressIndicator? = null,
        val progressCorsa: CircularProgressIndicator? = null,
        val progressSedute: CircularProgressIndicator? = null,



        // Eventuali record MVP
        val bestRunDate: TextView? = null,
        val bestRunValue: TextView? = null,
        val bestWalkDate: TextView? = null,
        val bestWalkValue: TextView? = null,
        val bestDriveDate: TextView? = null,
        val bestDriveValue: TextView? = null
    )

    /**
     * Se ti serve impostare un’attività "corrente" per calcolare durate o distanze in corso.
     */
    fun setAttivitàCorrente(attività: String?, oraInizio: Long) {
        attivitàCorrente = attività
        this.oraInizio = oraInizio
    }

    /**
     * Aggiorna *tutte* le statistiche giornaliere.
     * - Passi di oggi
     * - Sedute di oggi
     * - Tempo di camminata di oggi
     * - ecc.
     */
    fun updateCurrentSteps(steps: Int) {
        currentSteps = steps
    }

    // Funzione per aggiungere passi al conteggio cumulativo per il progress bar
    fun addStepsForProgress(newSteps: Int) {
        accumulatedSteps += newSteps
        // Salva il valore aggiornato in SharedPreferences
        val prefs = context.getSharedPreferences("progressPrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("accumulatedSteps", accumulatedSteps).apply()
    }

    fun resetAccumulatedSteps() {
        accumulatedSteps = 0
        val prefs = context.getSharedPreferences("progressPrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("accumulatedSteps", 0).apply()
    }
    fun addRunningDistance(newDistance: Float) {
        accumulatedDistance += newDistance
        saveValue("accumulatedDistance", accumulatedDistance)
    }

    // Nuovo metodo per le sedute: aggiungi tempo cumulativo
    fun addSittingTime(newSittingTime: Long) {
        accumulatedSittingTime += newSittingTime
        saveValue("accumulatedSittingTime", accumulatedSittingTime)
    }

    // Metodi per resettare i progressi al raggiungimento degli obiettivi (se necessario)

    fun resetAccumulatedDistance() {
        accumulatedDistance = 0f
        saveValue("accumulatedDistance", 0f)
    }
    fun resetAccumulatedSittingTime() {
        accumulatedSittingTime = 0L
        saveValue("accumulatedSittingTime", 0L)
    }


    fun pluralizzaVolte(numero: Int): String {
        return when {
            numero == 0 -> "volte"
            numero == 1 -> "volta"
            else -> "volte"
        }
    }


    // Metodo helper per salvare i valori in SharedPreferences
    private fun saveValue(key: String, value: Any) {
        val prefs = context.getSharedPreferences("progressPrefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            when (value) {
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("Tipo non supportato")
            }
            apply()
        }
    }
    fun updateCurrentDistance(distanza: Float) {
        distanzaCorrente = distanza
    }

    fun updateOraInizio(ora: Long) {
        oraInizio = ora
    }

    fun aggiornaStatisticheGiornaliere() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val endOfDay = calendar.timeInMillis

                // Per le statistiche giornaliere
                val passiDB = db.attivitàDao().getPassiTotaliByDate(startOfDay) ?: 0
                val totale = passiDB + currentSteps
                val seduteOggi = db.attivitàDao().getNumeroSeduteByDate(startOfDay)
                val tempoSedutoOggi = if (attivitàCorrente == "Sedersi") {
                    (db.attivitàDao().getTempoTotaleSedutoByDate(startOfDay) ?: 0L) + (System.currentTimeMillis() - oraInizio)
                } else {
                    db.attivitàDao().getTempoTotaleSedutoByDate(startOfDay) ?: 0L
                }

                val tempoCamminataOggi = if (attivitàCorrente == "Camminare") {
                    (db.attivitàDao().getTempoCamminataByDate(startOfDay, endOfDay) ?: 0L) + (System.currentTimeMillis() - oraInizio)
                } else {
                    db.attivitàDao().getTempoCamminataByDate(startOfDay, endOfDay) ?: 0L
                }

                // Per i progress bar
                val passiTotali = prefs.getInt("accumulatedSteps", 0) // dal SharedPreferences
                val distanzaCorsaTotale = db.attivitàDao().getDistanzaCorsaTotale() ?: 0f
                val distanzaTotaleCorsa = if (attivitàCorrente == "Corsa") {
                    distanzaCorsaTotale + distanzaCorrente
                } else {
                    distanzaCorsaTotale
                }
                val distanzaCorsaOggi = if (attivitàCorrente == "Corsa") {
                    (db.attivitàDao().getDistanzaCorsaByDate(startOfDay, endOfDay) ?: 0f) + distanzaCorrente
                } else {
                    db.attivitàDao().getDistanzaCorsaByDate(startOfDay, endOfDay) ?: 0f
                }

                val tempoSedutoTotale = db.attivitàDao().getTempoTotaleSeduto() ?: 0L
                val tempoTotaleSeduto = if (attivitàCorrente == "Sedersi") {
                    tempoSedutoTotale + (System.currentTimeMillis() - oraInizio)
                } else {
                    tempoSedutoTotale
                }

                withContext(Dispatchers.Main) {

                    // Statistiche giornaliere
                    uiComponents.txtPassiGiornalieriCompact?.text = totale.toString()
                    uiComponents.txtPassiGiornalieri?.text = "$totale passi"
                    uiComponents.txtTempoCamminataGiornaliere?.text = formattaTempoMillisecondi(tempoCamminataOggi)
                    uiComponents.txtSeduteGiornaliereCompact?.text = formattaTempoMillisecondi(tempoSedutoOggi)
                    uiComponents.txtNumeroSeduteGiornaliere?.text = "$seduteOggi volte"
                    uiComponents.txtTempoSeduteGiornaliere?.text = formattaTempoMillisecondi(tempoSedutoOggi)
                    uiComponents.txtNumeroSeduteGiornaliere?.text = "$seduteOggi ${pluralizzaVolte(seduteOggi)}"
                    uiComponents.txtCorsaGiornaliereCompact?.text = formattaDistanzaMetri(distanzaCorsaOggi)


                    // Progress bar
                    uiComponents.progressPassi?.setProgressCompat(
                        ((passiTotali.toFloat() / goalSteps) * 100).toInt(),
                        true
                    )
                    uiComponents.progressCorsa?.setProgressCompat(
                        ((distanzaTotaleCorsa / goalRunningDistance) * 100).toInt(),
                        true
                    )
                    uiComponents.progressSedute?.setProgressCompat(
                        ((tempoTotaleSeduto.toFloat() / goalMaxSittingTime) * 100).toInt(),
                        true
                    )
                }
            } catch (e: Exception) {
                Log.e("StatsManager", "Errore", e)
            }
        }
    }


    fun aggiornaStatisticheSettimanali() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val calendar = Calendar.getInstance()
                // Imposta il calendario all'inizio della settimana (Lunedì)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val startOfWeek = calendar.timeInMillis
                val endOfWeek = System.currentTimeMillis()

                // Passi
                val passiDB = db.attivitàDao().getPassiTotaliByDateRange(startOfWeek, endOfWeek) ?: 0
                val passiTotali = if (attivitàCorrente == "Camminare") passiDB + currentSteps else passiDB

                // Tempo di camminata settimanale
                val tempoCamminataDB = db.attivitàDao().getTempoCamminataByDateRange(startOfWeek, endOfWeek) ?: 0L
                val tempoCamminataSettimana = if (attivitàCorrente == "Camminare") {
                    tempoCamminataDB + (System.currentTimeMillis() - oraInizio)
                } else {
                    tempoCamminataDB
                }

                // Sedute
                val seduteDB = db.attivitàDao().getNumeroSeduteByDateRange(startOfWeek, endOfWeek)
                val tempoSedutoTotale = if (attivitàCorrente == "Sedersi") {
                    (db.attivitàDao().getTempoTotaleSedutoByDateRange(startOfWeek, endOfWeek) ?: 0L) +
                            (System.currentTimeMillis() - oraInizio)
                } else {
                    db.attivitàDao().getTempoTotaleSedutoByDateRange(startOfWeek, endOfWeek) ?: 0L
                }

                // Corsa
                val distanzaCorsaDB = db.attivitàDao().getDistanzaCorsaByDateRange(startOfWeek, endOfWeek) ?: 0f
                val tempoCorsaDB = db.attivitàDao().getTempoCorsaByDateRange(startOfWeek, endOfWeek) ?: 0L
                val distanzaCorsa = if (attivitàCorrente == "Corsa") distanzaCorsaDB + distanzaCorrente else distanzaCorsaDB
                val tempoCorsa = if (attivitàCorrente == "Corsa") {
                    val tempoAggiuntivo = System.currentTimeMillis() - oraInizio
                    tempoCorsaDB + tempoAggiuntivo
                } else {
                    tempoCorsaDB
                }

                // Guida
                val distanzaGuidaDB = db.attivitàDao().getDistanzaGuidaByDateRange(startOfWeek, endOfWeek) ?: 0f
                val tempoGuidaDB = db.attivitàDao().getTempoGuidaByDateRange(startOfWeek, endOfWeek) ?: 0L
                val distanzaGuida = if (attivitàCorrente == "Guidare") distanzaGuidaDB + distanzaCorrente else distanzaGuidaDB
                val tempoGuida = if (attivitàCorrente == "Guidare") tempoGuidaDB + (System.currentTimeMillis() - oraInizio) else tempoGuidaDB

                withContext(Dispatchers.Main) {
                    // Passi
                    uiComponents.txtPassiSettimanali?.text = "$passiTotali passi"
                    uiComponents.txtTempoCamminataSettimanali?.text = formattaTempoMillisecondi(tempoCamminataSettimana)
                    uiComponents.progressPassi?.setProgressCompat(((passiTotali.toFloat() / goalSteps) * 100).toInt(), true)

                    // Sedute
                    uiComponents.txtNumeroSeduteSettimanali?.text = "$seduteDB volte"
                    uiComponents.txtTempoSeduteSettimanali?.text = formattaTempoMillisecondi(tempoSedutoTotale)
                    uiComponents.txtNumeroSeduteSettimanali?.text = "$seduteDB ${seduteDB?.let {
                        pluralizzaVolte(
                            it
                        )
                    }}"

                    // Corsa
                    uiComponents.txtDistanzaCorsaSettimanale?.text = formattaDistanzaMetri(distanzaCorsa)
                    uiComponents.txtTempoCorsaSettimanale?.text = formattaTempoMillisecondi(tempoCorsa, forzaConversione = true)

                    // Guida
                    uiComponents.txtDistanzaGuidaSettimanale?.text = formattaDistanzaMetri(distanzaGuida)
                    uiComponents.txtTempoGuidaSettimanale?.text = formattaTempoMillisecondi(tempoCorsa, forzaConversione = true)
                }
            } catch (e: Exception) {
                Log.e("StatsManager", "Errore", e)
            }
        }
    }

    private fun formattaDistanzaMetri(metri: Float): String {
        return if (metri < 1000) {
            "${metri.toInt()} m"
        } else {
            val km = metri / 1000f
            String.format("%.1f km", km)
        }
    }

    private fun formattaTempoMillisecondi(milliseconds: Long, forzaConversione: Boolean = false): String {

        if (milliseconds <= 0) {
            return "0 min"
        }

        // Converte sempre i millisecondi in minuti
        val minuti = TimeUnit.MILLISECONDS.toMinutes(milliseconds)

        return formattaTempoMinuti(minuti)
    }



    private fun formattaTempoMinuti(minuti: Long): String {

        return if (minuti < 60) {
            "$minuti min"
        } else {
            val ore = minuti / 60
            val minutiRestanti = minuti % 60


            if (minutiRestanti == 0L) {
                "${ore}h"
            } else {
                "${ore}h ${minutiRestanti}m"
            }
        }
    }

}
