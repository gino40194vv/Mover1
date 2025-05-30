package com.example.mover

import android.app.Application
import android.content.Intent
import android.util.Log

class ActivityTrackingApplication : Application() {

    companion object {
        // Variabile  per tenere traccia se il servizio è in esecuzione
        var isServiceRunning: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        // Eventuali inizializzazioni globali
    }

    fun startActivityRecognition() {
        if (!isServiceRunning) {
            // Avvia il servizio
            val intent = Intent(this, ActivityRecognitionService1::class.java)
            startService(intent)
            isServiceRunning = true
            Log.d("ActivityTrackingApplication", "Servizio di riconoscimento attività avviato")
        } else {
            Log.d("ActivityTrackingApplication", "Il servizio è già in esecuzione")
        }
    }

    fun stopActivityRecognition() {
        try {
            // Ferma il servizio
            val intent = Intent(this, ActivityRecognitionService1::class.java)
            stopService(intent)
            isServiceRunning = false
            Log.d("ActivityTrackingApplication", "Servizio di riconoscimento attività fermato")
        } catch (e: Exception) {
            Log.e("ActivityTrackingApplication", "Errore durante l'arresto del servizio", e)
        }
    }
}
