package com.example.mover

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mover.data.AppDatabase
import com.example.mover.data.Attività
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CarTrackingService : Service() {
    private var isManualStart: Boolean = false
    private var distanceTraveled: Float = 0f
    private var startTime: Long = 0L
    private var lastLocation: Location? = null
    private var isTracking: Boolean = false
    private lateinit var db: AppDatabase
    private var velocitaAttuale: Float = 0f
    private var isServiceRunning = true

    private val CHANNEL_ID = "DriveTrackingServiceChannel"
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val ACTION_CAR_UPDATE = "com.example.personalphysicaltracker.ACTION_CAR_UPDATE"
        const val EXTRA_DISTANCE = "extra_distance"
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_DURATION = "extra_duration"
    }

    private val updateInterval: Long = 1000 // 1 secondo
    private val updateHandler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {
            try {
                Log.d("CarTrackingService", "UpdateRunnable - isServiceRunning: $isServiceRunning, isTracking: $isTracking")
                if (isServiceRunning && isTracking) {
                    val durata = System.currentTimeMillis() - startTime
                    broadcastCarUpdate(distanceTraveled, velocitaAttuale, durata)
                    updateHandler.postDelayed(this, updateInterval)
                }
            } catch (e: Exception) {
                Log.e("CarTrackingService", "Errore in updateRunnable", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("DriveTrackingService", "DriveTrackingService creato")
        createNotificationChannel()
        updateHandler.post(updateRunnable)
        val notification = createForegroundNotification()
        startForeground(1, notification)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        db = AppDatabase.getDatabase(this)
        Log.d("DriveTrackingService", "Database inizializzato: $db")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DriveTrackingService", "onStartCommand chiamato")
        isManualStart = true
        startTracking()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CarTrackingService", "onDestroy chiamato")
        isServiceRunning = false
        isTracking = false
        updateHandler.removeCallbacksAndMessages(null)

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e("CarTrackingService", "Errore nella rimozione degli update", e)
        }

        val oraFine = System.currentTimeMillis()
        if (distanceTraveled > 0 && (oraFine - startTime) > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val nuovaAttività = Attività(
                        tipo ="Guidare",
                        oraInizio = startTime,
                        oraFine = oraFine,
                        distanza = distanceTraveled,
                        velocitaMedia = calculateAverageSpeed(),
                        tempo = oraFine - startTime,
                        Automatica = !isManualStart
                    )
                    db.attivitàDao().inserisciAttività(nuovaAttività)
                } catch (e: Exception) {
                    Log.e("TrackingService", "Errore nel salvataggio dell'attività", e)
                }
            }
        } else {
            Log.d("TrackingService", "Attività non salvata: nessuna distanza percorsa o durata zero")
        }

    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        try {
            Log.d("DriveTrackingService", "startTracking chiamato")
            startTime = System.currentTimeMillis()
            distanceTraveled = 0f
            isTracking = true
            isServiceRunning = true
            requestLocationUpdates()
            Log.d("DriveTrackingService", "Monitoraggio guida avviato")
        } catch (e: Exception) {
            Log.e("DriveTrackingService", "Errore nell'avvio del tracking", e)
            stopSelf()
        }
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500L)
            .setMinUpdateIntervalMillis(100L)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d("DriveTrackingService", "Richieste di aggiornamento posizione inviate")
        } catch (e: SecurityException) {
            Log.e("DriveTrackingService", "Permesso di localizzazione non concesso", e)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (!isTracking) return
            val location = locationResult.lastLocation ?: return

            if (lastLocation != null) {
                val distance = lastLocation!!.distanceTo(location)
                if (distance > 0.5f) {
                    distanceTraveled += distance
                }
            }

            velocitaAttuale = location.speed * 3.6f // Converte m/s in km/h

            // Controlla velocità e avvisi
            when {
                velocitaAttuale > 130 -> {
                    sendSpeedAlert("Attenzione: Velocità eccessiva! Stai superando il limite autostradale.")
                }
                velocitaAttuale > 110 -> {
                    sendSpeedAlert("Attenzione: Stai superando il limite di velocità standard.")
                }
                velocitaAttuale > 50 -> {
                    sendSpeedAlert("Attenzione: Velocità elevata in zona urbana.")
                }
            }

            broadcastCarUpdate(distanceTraveled, velocitaAttuale, System.currentTimeMillis() - startTime)
            lastLocation = location
        }
    }

    private fun broadcastCarUpdate(distance: Float, speed: Float, duration: Long) {
        val intent = Intent(ACTION_CAR_UPDATE).apply {
            putExtra(EXTRA_DISTANCE, distance.roundToInt())
            putExtra(EXTRA_SPEED, speed)
            putExtra(EXTRA_DURATION, duration)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d("CarTrackingService", "Broadcasting update - Distance: ${distance.roundToInt()}, Speed: $speed, Duration: $duration")
    }

    private fun sendSpeedAlert(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Avviso Velocità")
            .setContentText(message)
            .setSmallIcon(R.drawable.directions_car)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(2, notification)
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoraggio Guida Auto")
            .setContentText("Monitoraggio del viaggio in auto in corso")
            .setSmallIcon(R.drawable.directions_car)
            .setOngoing(true)
            .addAction(
                R.drawable.directions_car,
                "Interrompi",
                getPendingIntent()
            )
            .build()
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Drive Tracking Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun calculateAverageSpeed(): Float {
        val durataInOre = (System.currentTimeMillis() - startTime) / 1000f / 3600f
        return if (durataInOre > 0) {
            (distanceTraveled / 1000f) / durataInOre // km/h
        } else {
            0f
        }
    }
}