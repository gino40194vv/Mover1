package com.example.mover

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class BikeTrackingService : Service() {
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private var isManualStart: Boolean = false
    private var distanceTraveled: Float = 0f
    private var startTime: Long = 0L
    private var lastLocation: Location? = null
    private var isTracking: Boolean = false
    private lateinit var db: AppDatabase
    private var isServiceRunning = true

    private var velocitaAttuale: Float = 0f
    private val updateInterval: Long = 1000 // 1 secondo
    private val updateHandler = Handler(Looper.getMainLooper())

    private val CHANNEL_ID = "BikeTrackingServiceChannel"
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val ACTION_BIKE_UPDATE = "com.example.personalphysicaltracker.ACTION_BIKE_UPDATE"
        const val EXTRA_DISTANCE = "extra_distance"
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_DURATION = "extra_duration"
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isServiceRunning) {  // Controlla se il servizio è ancora attivo
                if (!isPaused) {
                    val durata = System.currentTimeMillis() - startTime
                    broadcastBikeUpdate(distanceTraveled, velocitaAttuale, durata)
                    Log.d("BikeTrackingService", "Update inviato - durata: $durata, velocità: $velocitaAttuale")
                }
                // Schedula il prossimo aggiornamento solo se il servizio è attivo
                updateHandler.postDelayed(this, updateInterval)
            }
        }
    }

    // Soglia di pausa in millisecondi
    private val bikePauseThreshold: Long = 1 * 60 * 1000 // 1 minuto

    private var isPaused: Boolean = false
    private var pauseJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("BikeTrackingService", "BikeTrackingService creato")
        createNotificationChannel()
        val notification = createForegroundNotification()
        startForeground(1, notification)

        activityRecognitionClient = ActivityRecognition.getClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        db = AppDatabase.getDatabase(this)
        Log.d("BikeTrackingService", "Avvio updateHandler")
        // Avvia l'updateRunnable
        updateHandler.post(updateRunnable)
        Log.d("BikeTrackingService", "updateHandler avviato")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isManualStart = true
        startTracking()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacks(updateRunnable)
        stopTracking()

        val oraFine = System.currentTimeMillis()
        if (distanceTraveled > 0 && (oraFine - startTime) > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val nuovaAttività = Attività(
                        tipo = "Bicicletta",
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
        startTime = System.currentTimeMillis()
        distanceTraveled = 0f
        isTracking = true
        requestLocationUpdates()
    }

    private fun stopTracking() {
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500L) // mezzo secondo
            .setMinUpdateIntervalMillis(100L) // minimo 100ms tra gli update
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("BikeTrackingService", "Permesso di localizzazione non concesso", e)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d("BikeTrackingService", "Nuovo aggiornamento posizione ricevuto")
            if (!isTracking) {
                Log.d("BikeTrackingService", "Aggiornamento ignorato - tracking non attivo")
                return
            }
            val location = locationResult.lastLocation ?: return

            if (lastLocation != null) {
                val distance = lastLocation!!.distanceTo(location)
                Log.d("BikeTrackingService", "Distanza dall'ultima posizione: $distance metri")
                if (distance > 0.5f) {
                    distanceTraveled += distance
                    Log.d("BikeTrackingService", "Nuova distanza totale: $distanceTraveled metri")
                }
                velocitaAttuale = location.speed * 3.6f
                Log.d("BikeTrackingService", "Velocità attuale aggiornata: $velocitaAttuale km/h")
            }
            lastLocation = location
        }
    }

    private fun sendSpeedAlert(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Avviso Velocità")
            .setContentText(message)
            .setSmallIcon(R.drawable.directions_bike)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(2, notification)
    }

    private fun broadcastBikeUpdate(distance: Float, speed: Float, duration: Long) {
        val intent = Intent(ACTION_BIKE_UPDATE).apply {
            putExtra(EXTRA_DISTANCE, distance.roundToInt())
            putExtra(EXTRA_SPEED, speed)
            putExtra(EXTRA_DURATION, duration)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Bike Tracking Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoraggio bicicletta")
            .setContentText("Monitoraggio della velocità e distanza in bici in corso")
            .setSmallIcon(R.drawable.directions_bike)
            .build()
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