package com.example.mover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

object ActivityStateManager {
    var currentActivityType: Int = DetectedActivity.UNKNOWN
    var activityStartTime: Long = 0L
    var orientationServiceStarted: Boolean = false // Stato per DeviceOrientationService
    var isFirstDetectionAfterSwitch: Boolean = true
    var isSwitchActive: Boolean = false
    var hasReceivedFirstUpdate: Boolean = false
}
var previousActivityType: Int = DetectedActivity.UNKNOWN


class ActivityRecognitionReceiver : BroadcastReceiver() {

    private var lastActivityType: Int = DetectedActivity.UNKNOWN
    private var lastActivityTimestamp: Long = 0
    private val ACTIVITY_CHANGE_THRESHOLD_MS = 5_000 // 5 secondi
    private val CONFIDENCE_THRESHOLD = 55 // Confidenza minima più alta
    private var consecutiveActivityCount = 0
    private val CONSECUTIVE_ACTIVITY_THRESHOLD = 2 // Richiedi 2 rilevamenti consecutivi





    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ActivityRecognitionReceiver", "\n====== NUOVO AGGIORNAMENTO ATTIVITÀ ======")
        Log.d("ActivityRecognitionReceiver", "Intent action: ${intent.action}")
        Log.d("ActivityRecognitionReceiver", "Intent extras: ${intent.extras}")

        try {
            val hasResult = ActivityRecognitionResult.hasResult(intent)
            Log.d("ActivityRecognitionReceiver", "Contiene risultato attività: $hasResult")

            if (hasResult) {
                val result = ActivityRecognitionResult.extractResult(intent)

                // Log delle attività rilevate
                Log.d("ActivityRecognitionReceiver", "\nTutte le attività rilevate:")
                result?.probableActivities?.forEach { activity ->
                    Log.d("ActivityRecognitionReceiver", """
                    |Attività: ${getActivityName(activity.type)}
                    |   - Tipo: ${activity.type}
                    |   - Confidenza: ${activity.confidence}%
                    |   - Timestamp: ${System.currentTimeMillis()}
                """.trimMargin())
                }

                result?.mostProbableActivity?.let { activity ->
                    val activityType = activity.type
                    val confidence = activity.confidence
                    val currentTime = System.currentTimeMillis()

                    Log.d("ActivityRecognitionReceiver", """
                    |\nATTIVITÀ PIÙ PROBABILE:
                    |Tipo: ${getActivityName(activityType)}
                    |Confidenza: $confidence%
                    |Switch Attivo: ${ActivityStateManager.isSwitchActive}
                    |Prima Rilevazione: ${ActivityStateManager.isFirstDetectionAfterSwitch}
                    |Attività Precedente: ${getActivityName(previousActivityType)}
                    |Tempo dall'ultima attività: ${currentTime - lastActivityTimestamp} ms
                    |Rilevamenti consecutivi: $consecutiveActivityCount
                """.trimMargin())

                    when {
                        // Stessa attività dei rilevamenti precedenti
                        activityType == lastActivityType -> {
                            consecutiveActivityCount++

                            // Solo se abbiamo rilevamenti consecutivi e alta confidenza
                            if (consecutiveActivityCount >= CONSECUTIVE_ACTIVITY_THRESHOLD &&
                                confidence >= CONFIDENCE_THRESHOLD) {

                                // Meno di 10 secondi dall'ultima attività
                                if (currentTime - lastActivityTimestamp < ACTIVITY_CHANGE_THRESHOLD_MS) {
                                    Log.d("ActivityRecognitionReceiver", "Attività troppo recente, ignoro")
                                    return
                                }

                                // Procedi con il cambio di attività
                                Log.d("ActivityRecognitionReceiver", "Cambio attività confermato")
                                stopAllServices(context, activityType)
                                startServiceForActivity(context, activityType)
                                broadcastActivityRecognition(activityType, confidence, context)

                                // Resetta i contatori
                                lastActivityType = activityType
                                lastActivityTimestamp = currentTime
                                consecutiveActivityCount = 0
                            }
                        }

                        // Nuova attività
                        activityType != lastActivityType -> {
                            // Resetta il conteggio per la nuova attività
                            consecutiveActivityCount = 1
                            lastActivityType = activityType
                        }
                    }

                    if (confidence >= 50) {
                        Log.d("ActivityRecognitionReceiver", "\nAVVIO SERVIZI per ${getActivityName(activityType)}")
                        stopAllServices(context, activityType)
                        startServiceForActivity(context, activityType)
                        broadcastActivityRecognition(activityType, confidence, context)
                    } else {
                        Log.d("ActivityRecognitionReceiver", "\nConfidenza troppo bassa ($confidence%), nessuna azione intrapresa")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ActivityRecognitionReceiver", "ERRORE DURANTE L'ELABORAZIONE", e)
            e.printStackTrace()
        }
        Log.d("ActivityRecognitionReceiver", "====== FINE AGGIORNAMENTO ATTIVITÀ ======\n")
    }



    private fun getActivityName(activityType: Int): String {
        return when (activityType) {
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            0 -> "ZERO (Probabilmente STILL)"
            else -> "TIPO SCONOSCIUTO ($activityType)"
        }
    }

    private fun startServiceForActivity(context: Context, activityType: Int) {
        when (activityType) {
            DetectedActivity.WALKING, DetectedActivity.ON_FOOT -> {
                val stepIntent = Intent(context, StepTrackingService::class.java)
                ContextCompat.startForegroundService(context, stepIntent)
                Log.d("ActivityRecognitionReceiver", "Avviato StepTrackingService")
            }
            DetectedActivity.RUNNING -> {
                val runIntent = Intent(context, RunTrackingService::class.java)
                ContextCompat.startForegroundService(context, runIntent)
                Log.d("ActivityRecognitionReceiver", "Avviato RunTrackingService")
            }
            DetectedActivity.STILL, DetectedActivity.TILTING -> {
                handleDeviceOrientationService(context, activityType)
            }
            DetectedActivity.IN_VEHICLE -> {
                val carIntent = Intent(context, CarTrackingService::class.java)
                ContextCompat.startForegroundService(context, carIntent)
                Log.d("ActivityRecognitionReceiver", "Avviato CarTrackingService")
            }
            DetectedActivity.ON_BICYCLE -> {
                val bikeIntent = Intent(context, BikeTrackingService::class.java)
                ContextCompat.startForegroundService(context, bikeIntent)
                Log.d("ActivityRecognitionReceiver", "Avviato BikeTrackingService")
            }
        }
    }

    private fun handleDeviceOrientationService(context: Context, activityType: Int) {
        Log.d("ActivityRecognitionReceiver", "======== INIZIO GESTIONE ORIENTAMENTO ========")
        Log.d("ActivityRecognitionReceiver", "Stato attuale orientationServiceStarted: ${ActivityStateManager.orientationServiceStarted}")
        Log.d("ActivityRecognitionReceiver", "Attività corrente: ${getActivityName(activityType)}")
        Log.d("ActivityRecognitionReceiver", "Attività precedente: ${getActivityName(previousActivityType)}")

        when (activityType) {
            DetectedActivity.STILL -> {
                Log.d("ActivityRecognitionReceiver", ">> Rilevato STILL")
                previousActivityType = DetectedActivity.STILL
                if (!ActivityStateManager.orientationServiceStarted) {
                    Log.d("ActivityRecognitionReceiver", ">> Avvio servizi STILL")
                    startDeviceOrientationService(context)
                    startSedutaTrackingService(context)
                }
            }
            DetectedActivity.TILTING -> {
                Log.d("ActivityRecognitionReceiver", ">> Rilevato TILTING")
                if (previousActivityType == DetectedActivity.STILL) {
                    Log.d("ActivityRecognitionReceiver", ">> Sequenza STILL->TILTING confermata")
                    if (!ActivityStateManager.orientationServiceStarted) {
                        Log.d("ActivityRecognitionReceiver", ">> Avvio servizi TILTING")
                        startDeviceOrientationService(context)
                    }
                }
            }
            else -> {
                Log.d("ActivityRecognitionReceiver", ">> Rilevata altra attività: ${getActivityName(activityType)}")
                Log.d("ActivityRecognitionReceiver", ">> Fermo servizi orientamento e seduta")
                stopDeviceOrientationService(context)
                stopSedutaTrackingService(context)
                previousActivityType = DetectedActivity.UNKNOWN
            }
        }
        Log.d("ActivityRecognitionReceiver", "======== FINE GESTIONE ORIENTAMENTO ========")
    }


    private fun startDeviceOrientationService(context: Context) {
        val orientationIntent = Intent(context, DeviceOrientationService::class.java)
        ContextCompat.startForegroundService(context, orientationIntent)
        ActivityStateManager.orientationServiceStarted = true
        Log.d("ActivityRecognitionReceiver", "Avviato DeviceOrientationService")
    }

    private fun stopAllServices(context: Context, currentActivityType: Int) {
        when (currentActivityType) {
            DetectedActivity.WALKING, DetectedActivity.ON_FOOT -> {
                // Ferma i servizi non pertinenti
                stopRunTrackingService(context)
                stopDriveTrackingService(context)
                stopSedutaTrackingService(context)
                stopDeviceOrientationService(context)
                Log.d("ActivityRecognitionReceiver", "Servizi non pertinenti fermati per WALKING/ON_FOOT")
            }
            DetectedActivity.RUNNING -> {
                // Ferma i servizi non pertinenti
                stopStepTrackingService(context)
                stopDriveTrackingService(context)
                stopSedutaTrackingService(context)
                stopDeviceOrientationService(context)
                Log.d("ActivityRecognitionReceiver", "Servizi non pertinenti fermati per RUNNING")
            }
            DetectedActivity.STILL, DetectedActivity.TILTING -> {
                // Ferma solo i servizi non pertinenti
                stopStepTrackingService(context)
                stopRunTrackingService(context)
                stopDriveTrackingService(context)
                Log.d("ActivityRecognitionReceiver", "Servizi non pertinenti fermati per STILL/TILTING")
                // Non fermare SedutaTrackingService e DeviceOrientationService
            }
            DetectedActivity.IN_VEHICLE, DetectedActivity.ON_BICYCLE -> {
                // Ferma i servizi non pertinenti
                stopStepTrackingService(context)
                stopRunTrackingService(context)
                stopSedutaTrackingService(context)
                stopDeviceOrientationService(context)
                Log.d("ActivityRecognitionReceiver", "Servizi non pertinenti fermati per IN_VEHICLE/ON_BICYCLE")
            }
            else -> {
                // Ferma tutti i servizi per attività sconosciute
                stopStepTrackingService(context)
                stopRunTrackingService(context)
                stopDriveTrackingService(context)
                stopSedutaTrackingService(context)
                stopDeviceOrientationService(context)
                Log.d("ActivityRecognitionReceiver", "Tutti i servizi fermati per attività sconosciuta")
            }
        }
    }

    private fun stopDeviceOrientationService(context: Context) {
        val orientationIntent = Intent(context, DeviceOrientationService::class.java)
        context.stopService(orientationIntent)
        ActivityStateManager.orientationServiceStarted = false
        Log.d("ActivityRecognitionReceiver", "Fermato DeviceOrientationService")
    }

    private fun stopSedutaTrackingService(context: Context) {
        val sedutaIntent = Intent(context, SedutaTrackingService::class.java)
        context.stopService(sedutaIntent)
        Log.d("ActivityRecognitionReceiver", "Fermato SedutaTrackingService")
    }

    private fun stopStepTrackingService(context: Context) {
        val stepIntent = Intent(context, StepTrackingService::class.java)
        context.stopService(stepIntent)
        Log.d("ActivityRecognitionReceiver", "Fermato StepTrackingService")
    }

    private fun stopRunTrackingService(context: Context) {
        val runIntent = Intent(context, RunTrackingService::class.java)
        context.stopService(runIntent)
        Log.d("ActivityRecognitionReceiver", "Fermato RunTrackingService")
    }

    private fun stopDriveTrackingService(context: Context) {
        val driveIntent = Intent(context, CarTrackingService::class.java)
        context.stopService(driveIntent)
        Log.d("ActivityRecognitionReceiver", "Fermato DriveTrackingService")
    }
    private fun startSedutaTrackingService(context: Context) {
        val sedutaIntent = Intent(context, SedutaTrackingService::class.java)
        ContextCompat.startForegroundService(context, sedutaIntent)
        Log.d("ActivityRecognitionReceiver", "Avviato SedutaTrackingService")
    }

    private fun broadcastActivityRecognition(activityType: Int, confidence: Int, context: Context) {
        // Aggiorna sempre il tipo di attività corrente
        ActivityStateManager.currentActivityType = activityType

        // Resetta la flag del primo rilevamento
        if (ActivityStateManager.isFirstDetectionAfterSwitch) {
            ActivityStateManager.isFirstDetectionAfterSwitch = false
        }

        // Imposta che ha ricevuto il primo aggiornamento
        ActivityStateManager.hasReceivedFirstUpdate = true

        val intent = Intent("com.example.personalphysicaltracker.ACTIVITY_RECOGNIZED")
        intent.putExtra(MainActivity.EXTRA_ACTIVITY_TYPE, activityType)
        intent.putExtra(MainActivity.EXTRA_ACTIVITY_CONFIDENCE, confidence)


        // Aggiunge valori iniziali basati sul tipo di attività
        when (activityType) {
            DetectedActivity.WALKING, DetectedActivity.ON_FOOT -> {
                intent.putExtra("initial_primary", "0 PASSI")
                intent.putExtra("initial_secondary", "0 M")
                intent.putExtra("initial_tertiary", "0 SEC")
            }
            DetectedActivity.RUNNING -> {
                intent.putExtra("initial_primary", "0 M")
                intent.putExtra("initial_secondary", "0 KM/H")
                intent.putExtra("initial_tertiary", "0 SEC")
            }
            DetectedActivity.IN_VEHICLE -> {
                intent.putExtra("initial_primary", "0 M")
                intent.putExtra("initial_secondary", "0 KM/H")
                intent.putExtra("initial_tertiary", "0 SEC")
            }
            DetectedActivity.STILL, DetectedActivity.TILTING -> {
                intent.putExtra("initial_primary", "VOLTE SEDUTO")
                intent.putExtra("initial_secondary", "0")
                intent.putExtra("initial_tertiary", "")
            }
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

}
