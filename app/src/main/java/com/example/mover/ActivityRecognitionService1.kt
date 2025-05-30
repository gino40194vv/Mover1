package com.example.mover

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.DetectedActivity
import android.Manifest
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat


class ActivityRecognitionService1 : Service() {

    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var pendingIntent: PendingIntent
    private val CHANNEL_ID = "ActivityRecognitionServiceChannel"

    override fun onCreate() {
        super.onCreate()
        Log.d("ActivityRecognitionService1", "Servizio avviato")

        ActivityTrackingApplication.isServiceRunning = true

        ActivityStateManager.apply {
            currentActivityType = DetectedActivity.UNKNOWN
            activityStartTime = 0L
            orientationServiceStarted = false
            isFirstDetectionAfterSwitch = true
            isSwitchActive = false
            hasReceivedFirstUpdate = false
        }

        // Resetta lo stato dell'attività
        ActivityStateManager.currentActivityType = DetectedActivity.UNKNOWN
        previousActivityType = DetectedActivity.UNKNOWN

        createNotificationChannel()

        // Creazione notifica di servizio in foreground
        val notification = createNotification()
        startForeground(5, notification)

        // Inizializza l'ActivityRecognitionClient
        activityRecognitionClient = ActivityRecognition.getClient(this)

        // Configura il PendingIntent per ActivityRecognitionReceiver e assegna alla variabile di classe
        val intent = Intent(this, ActivityRecognitionReceiver::class.java)
        intent.action = "com.example.personalphysicaltracker.ACTIVITY_RECOGNIZED"
        pendingIntent = PendingIntent.getBroadcast(this, 8, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        // Richiedi aggiornamenti sull'attività
        requestActivityUpdates()

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ActivityRecognitionService1", "onStartCommand chiamato")

        // Reset dello stato se necessario
        if (ActivityStateManager.currentActivityType != DetectedActivity.UNKNOWN) {
            ActivityStateManager.currentActivityType = DetectedActivity.UNKNOWN
            previousActivityType = DetectedActivity.UNKNOWN
        }

        if (!::activityRecognitionClient.isInitialized) {
            activityRecognitionClient = ActivityRecognition.getClient(this)
            requestActivityUpdates()
            Log.d("ActivityRecognitionService1", "ActivityRecognitionClient reinizializzato")
        }

        // Log dello stato attuale
        Log.d("ActivityRecognitionService1", """
        Stato corrente:
        - Switch attivo: ${ActivityStateManager.isSwitchActive}
        - Prima rilevazione: ${ActivityStateManager.isFirstDetectionAfterSwitch}
        - Attività corrente: ${ActivityStateManager.currentActivityType}
        - Attività precedente: $previousActivityType
    """.trimIndent())

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoraggio delle attività")
            .setContentText("Servizio attivo per il riconoscimento delle attività.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Sostituisci con la tua icona
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Activity Recognition Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun requestActivityUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("ActivityRecognitionService1", "Richiedo aggiornamenti attività...")

            val task = activityRecognitionClient.requestActivityUpdates(3000, pendingIntent)
            task.addOnSuccessListener {
                Log.d("ActivityRecognitionService1", """
                Aggiornamenti attività richiesti con successo
                - Intervallo: 3000ms
                - PendingIntent creato e registrato
            """.trimIndent())
            }
            task.addOnFailureListener { e ->
                Log.e("ActivityRecognitionService1", "Richiesta di aggiornamenti attività fallita: ${e.message}")
                Handler(Looper.getMainLooper()).postDelayed({
                    requestActivityUpdates()
                }, 5000)
            }
        } else {
            Log.e("ActivityRecognitionService1", "Permesso ACTIVITY_RECOGNITION non concesso")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ActivityRecognitionService1", "Servizio distrutto, rimuovo aggiornamenti attività")
        ActivityTrackingApplication.isServiceRunning = false
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            activityRecognitionClient.removeActivityUpdates(pendingIntent)
            Log.d("ActivityRecognitionService1", "Aggiornamenti attività rimossi")
        } catch (e: Exception) {
            Log.e("ActivityRecognitionService1", "Errore durante la rimozione degli aggiornamenti", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
