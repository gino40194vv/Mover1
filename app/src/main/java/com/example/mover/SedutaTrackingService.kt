package com.example.mover

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mover.data.AppDatabase
import com.example.mover.data.Attività
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.withContext


class SedutaTrackingService : Service() {



    private var startTime: Long = 0L
    private val CHANNEL_ID = "SedutaTrackingServiceChannel"
    private lateinit var db: AppDatabase
    private var sedutaCount: Int = 0
    private var isAutomatica: Boolean = true
    private var isServiceRunning = false
    private var seatedTime: Long = 0L
    private var pendingSave = false
    private val NOTIFICATION_ID = 101
    private var isFirstStart = true

    companion object {
        const val ACTION_INCREMENT_SEDUTA_COUNT = "com.example.personalphysicaltracker.ACTION_INCREMENT_SEDUTA_COUNT"
        const val ACTION_SEDUTA_COUNT_UPDATE = "com.example.personalphysicaltracker.ACTION_SEDUTA_COUNT_UPDATE"
        const val ACTION_STOP_SERVICE = "com.example.personalphysicaltracker.ACTION_STOP_SERVICE"
        const val ACTION_UPDATE_TIME = "com.example.personalphysicaltracker.ACTION_UPDATE_TIME"
        const val ACTION_NEW_SESSION = "com.example.personalphysicaltracker.ACTION_NEW_SESSION"
        const val ACTION_SITTING_TIME_UPDATE = "com.example.personalphysicaltracker.ACTION_SITTING_TIME_UPDATE"
        const val EXTRA_SITTING_TIME = "com.example.personalphysicaltracker.EXTRA_SITTING_TIME"
        const val ACTION_ACTIVITY_NOT_SAVED = "com.example.personalphysicaltracker.ACTIVITY_NOT_SAVED"

    }


    override fun onCreate() {
        super.onCreate()
        Log.d("SedutaTrackingService", "onCreate chiamato")
        db = AppDatabase.getDatabase(this)
        startTime = System.currentTimeMillis()  // Inizializziamo startTime
        createNotificationChannel()
        val notification = createNotification()
        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        )
        isServiceRunning = true
        Log.d("SedutaTrackingService", "Service avviato in foreground")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SedutaTrackingService", "onDestroy chiamato")
        if (isServiceRunning) {
            saveActivityAndStop()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun sendSedutaCountUpdate() {
        val intent = Intent(ACTION_SEDUTA_COUNT_UPDATE).apply {
            putExtra("sedutaCount", sedutaCount)
            putExtra("sedutaDurata", seatedTime)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


    private fun createNotification(): Notification {
        Log.d("SedutaTrackingService", "Creazione della notifica per il servizio in foreground")

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoraggio seduta in corso")
            .setContentText("Stiamo monitorando il tuo stato di seduta.")
            .setSmallIcon(R.drawable.seat_24px)
            .setContentIntent(pendingIntent)
            .build()
    }




    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Controlla se è un avvio manuale
        val isManualStart = intent?.getBooleanExtra("isManualStart", false) ?: false
        isAutomatica = !isManualStart

        if (isFirstStart) {
            val isManualStart = intent?.getBooleanExtra("isManualStart", false) ?: false
            isAutomatica = !isManualStart
            startTime = System.currentTimeMillis()
            isFirstStart = false
        }

        when (intent?.action) {
            ACTION_INCREMENT_SEDUTA_COUNT -> {
                if (isServiceRunning) {
                    sedutaCount++
                    sendSedutaCountUpdate()
                }
            }
            ACTION_UPDATE_TIME -> {
                val newDelta = intent.getLongExtra("seatedTime", 0L)
                if (newDelta > 0) {
                    seatedTime += newDelta
                    val prefs = getSharedPreferences("sedutaPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putLong("accumulatedSittingTime", seatedTime).apply()
                    sendSittingTimeUpdate()

                    if (pendingSave) {
                        performSaveAndStop()
                    }
                }
            }
            ACTION_NEW_SESSION -> {
                val currentTime = seatedTime
                val currentCount = sedutaCount
                val currentStart = startTime

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val nuovaAttività = Attività(
                            tipo = "Sedersi",
                            oraInizio = currentStart,
                            oraFine = System.currentTimeMillis(),
                            tempo = currentTime,
                            volteSeduto = currentCount,
                            Automatica = isAutomatica
                        )
                        db.attivitàDao().inserisciAttività(nuovaAttività)
                    } catch (e: Exception) {
                        Log.e("SedutaTrackingService", "Errore nel salvataggio", e)
                    }
                }
                startTime = System.currentTimeMillis()
                sedutaCount = 0
                seatedTime = 0L
                val prefs = getSharedPreferences("sedutaPrefs", Context.MODE_PRIVATE)
                prefs.edit().putLong("accumulatedSittingTime", 0L).apply()
                isServiceRunning = true
                Log.d("SedutaTrackingService", "Nuova sessione iniziata")
            }
            ACTION_STOP_SERVICE -> {
                pendingSave = true
            }
        }
        return START_STICKY
    }


    private fun performSaveAndStop() {
        Log.d("SedutaTrackingService", "Salvataggio attività finale. Tempo: ${seatedTime/1000.0} secondi, Sedute: $sedutaCount")

        // Controlla che ci siano sia sedute che tempo
        if (sedutaCount > 0 && seatedTime > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val nuovaAttività = Attività(
                        tipo = "Sedersi",
                        oraInizio = startTime,
                        oraFine = System.currentTimeMillis(),
                        tempo = seatedTime,
                        volteSeduto = sedutaCount,
                        Automatica = isAutomatica
                    )
                    db.attivitàDao().inserisciAttività(nuovaAttività)
                    Log.d("SedutaTrackingService", "Attività finale salvata. Sedute: $sedutaCount, Tempo: ${seatedTime/1000.0} secondi")
                } catch (e: Exception) {
                    Log.e("SedutaTrackingService", "Errore nel salvataggio", e)
                }

                withContext(Dispatchers.Main) {
                    isServiceRunning = false
                    pendingSave = false
                    stopForeground(true)
                    stopSelf()
                }
            }
        } else {
            // Invia un broadcast per mostrare il popup
            val intent = Intent(ACTION_ACTIVITY_NOT_SAVED)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

            Log.d("SedutaTrackingService", "Nessuna attività salvata: sedute o tempo insufficienti")
            stopForeground(true)
            stopSelf()
        }
    }


    private fun saveActivityAndStop() {
        val currentTimestamp = System.currentTimeMillis()
        Log.d("SedutaTrackingService", """
            Tentativo di salvare attività:
            - Sedute: $sedutaCount
            - Tempo: ${seatedTime/1000.0} secondi
            - Ora inizio: $startTime
            - Ora fine: $currentTimestamp
        """.trimIndent())

        if (sedutaCount > 0 && seatedTime > 0) {
            Log.d("SedutaTrackingService", "Condizioni di salvataggio verificate")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val nuovaAttività = Attività(
                        tipo = "Sedersi",
                        oraInizio = startTime,
                        oraFine = currentTimestamp,
                        tempo = seatedTime,
                        volteSeduto = sedutaCount,
                        Automatica = isAutomatica
                    )
                    Log.d("SedutaTrackingService", "Creato oggetto Attività: $nuovaAttività")
                    db.attivitàDao().inserisciAttività(nuovaAttività)
                    Log.d("SedutaTrackingService", "Attività salvata con successo nel DB")
                } catch (e: Exception) {
                    Log.e("SedutaTrackingService", "Errore nel salvataggio", e)
                    e.printStackTrace()
                }

                withContext(Dispatchers.Main) {
                    isServiceRunning = false
                    stopForeground(true)
                    stopSelf()
                }
            }
        } else {
            Log.d("SedutaTrackingService", "Salvataggio non effettuato - Sedute: $sedutaCount, Tempo: ${seatedTime/1000.0}")
            val intent = Intent(ACTION_ACTIVITY_NOT_SAVED)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            stopForeground(true)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        Log.d("SedutaTrackingService", "Creazione del canale di notifica")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Seduta Tracking Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun sendSittingTimeUpdate() {
        Log.d("SedutaTrackingService", "Invio update UI per tempo seduto: ${seatedTime/1000.0} secondi")
        val intent = Intent(ACTION_SITTING_TIME_UPDATE).apply {
            putExtra(EXTRA_SITTING_TIME, seatedTime)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
