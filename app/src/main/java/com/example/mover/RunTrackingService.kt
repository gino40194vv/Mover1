package com.example.mover

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mover.data.AppDatabase
import com.example.mover.data.Attività
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class RunTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: AppDatabase
    private var oraInizio: Long = 0
    private var distanzaCorsa: Float = 0f
    private var tempoCorsa: Long = 0L
    private val timer = Timer()
    private var velocitaAttuale: Float = 0f
    private var velocitaMedia: Float = 0f
    private var posizionePrecedente: Location? = null
    private var lastUpdateTime: Long = 0L
    private val updateInterval: Long = 1000 // 1 secondo

    private val updateHandler = Handler(Looper.getMainLooper())

    var isPaused: Boolean = false
    private var pausaInizio: Long = 0L
    private var tempoTotaleInPausa: Long = 0L
    private val VELOCITA_SOGLIA_PAUSA = 0.5f // km/h
    private var contatoreSottoSoglia = 0
    private val CONTATORE_SOGLIA_PAUSA = 5
    private var isAutomatica: Boolean = true
    private var lastPositions = mutableListOf<Location>() // tiene traccia delle ultime posizioni
    private val MAX_RADIUS_METERS = 3f // raggio massimo di movimento in metri
    private var lastSpeedUpdateTime = 0L
    private val MAX_TIME_WITHOUT_SPEED_UPDATE = 2000L // 2 secondi
    private var paceMedia: Float = 0f  // minuti per kilometro
   private var isGpsInitialized = false
    private val MIN_ACCURACY = 20f  // metri
    private val MIN_GPS_SAMPLES = 5
    private var gpsSampleCount = 0

    private val stopServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("RunTrackingService", "Comando ricevuto per fermare il servizio")
            stopSelf() // Ferma il servizio
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()

            // Se non ci sono stati aggiornamenti di velocità per 2 secondi
            if (currentTime - lastSpeedUpdateTime > MAX_TIME_WITHOUT_SPEED_UPDATE) {
                velocitaAttuale = 0f
                Log.d("PausaDebug", "Velocità resettata a zero per mancanza di aggiornamenti")
            }

            Log.d("PausaDebug", """
            UpdateRunnable:
            isPaused: $isPaused
            velocitaAttuale: $velocitaAttuale
            tempoCorsa: $tempoCorsa
        """.trimIndent())

            // Aggiungiamo qui un controllo sulla velocità
            if (velocitaAttuale == 0f) {
                contatoreSottoSoglia++
                Log.d("PausaDebug", "Velocità zero rilevata in updateRunnable, contatore: $contatoreSottoSoglia")
                if (contatoreSottoSoglia >= CONTATORE_SOGLIA_PAUSA && !isPaused) {
                    Log.d("PausaDebug", "Attivazione pausa da updateRunnable")
                    iniziaPausaAutomatica()
                }
            } else {
                contatoreSottoSoglia = 0
            }

            if (!isPaused) {
                tempoCorsa = System.currentTimeMillis() - oraInizio - tempoTotaleInPausa
                Log.d("PausaDebug", """
                Calcolo tempo:
                currentTime: ${System.currentTimeMillis()}
                oraInizio: $oraInizio
                tempoTotaleInPausa: $tempoTotaleInPausa
            """.trimIndent())
            }

            sendRunUpdate(distanzaCorsa, tempoCorsa, velocitaAttuale, velocitaMedia)
            updateNotification()
            updateHandler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("RunTrackingService", "onCreate chiamato")

        val filter = IntentFilter("STOP_RUN_TRACKING_SERVICE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(stopServiceReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stopServiceReceiver, filter)
        }

        createNotificationChannel()
        startForegroundService()



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        db = AppDatabase.getDatabase(this)

        oraInizio = System.currentTimeMillis()
        lastUpdateTime = oraInizio

        // Aggiorna la notifica con un timer ogni secondo
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateNotification()
            }
        }, 0, 1000)

        updateHandler.post(updateRunnable)
        startLocationUpdates()
    }

    private fun startForegroundService() {
        Log.d("RunTrackingService", "Avvio del servizio in foreground con notifica")
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                2,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
            Log.d("RunTrackingService", "Servizio in foreground avviato per Android Q+")
        } else {
            startForeground(2, notification)
            Log.d("RunTrackingService", "Servizio in foreground avviato per Android pre-Q")
        }
    }

    private fun createNotification(): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, "run_tracking_service")
            .setSmallIcon(R.drawable.directions_run_24px)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setShowWhen(false)

        if (isPaused) {
            notificationBuilder
                .setContentTitle("Corsa in pausa")
                .setContentText("Sei in pausa? Attività stoppata!")
        } else {
            val notificationContent = getNotificationContent()
            notificationBuilder
                .setContentTitle("Tracciamento corsa attivo")
                .setContentText(notificationContent)
        }

        return notificationBuilder.build()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 500
            fastestInterval = 250
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 0.5f
        }

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                Log.d("RunTrackingService", "Richiesti aggiornamenti di posizione ad alta frequenza")
            } else {
                Log.e("RunTrackingService", "Permesso ACCESS_FINE_LOCATION non concesso")
                stopSelf()
            }
        } catch (e: SecurityException) {
            Log.e("RunTrackingService", "Permesso di localizzazione non concesso", e)
            stopSelf()
        }
    }

    private fun sendRunUpdate(distance: Float, elapsedTime: Long, velocitaAttuale: Float, velocitaMedia: Float) {
        val intent = Intent(ACTION_RUN_UPDATE)
        intent.setPackage(packageName)
        intent.putExtra(EXTRA_DISTANZA, distance)
        intent.putExtra(EXTRA_TEMPO_CORSA, elapsedTime)
        intent.putExtra(EXTRA_VELOCITA_ATTUALE, velocitaAttuale)
        intent.putExtra(EXTRA_VELOCITA_MEDIA, velocitaMedia)
        intent.putExtra(EXTRA_PACE_MEDIO, paceMedia)
        intent.putExtra(EXTRA_IS_PAUSED, isPaused)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun calcolaVelocitaFiltrata(nuovaVelocita: Float): Float {
        val MAX_VELOCITA = 25f // km/h
        val MIN_VELOCITA = 0.5f // km/h
        return when {
            nuovaVelocita > MAX_VELOCITA -> MAX_VELOCITA  // limita velocità massima
            nuovaVelocita < MIN_VELOCITA -> 0f
            else -> nuovaVelocita
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                        // Verifica l'accuratezza e l'inizializzazione del GPS
                        if (!isGpsInitialized) {
                            if (location.accuracy <= MIN_ACCURACY) {
                                gpsSampleCount++
                                if (gpsSampleCount >= MIN_GPS_SAMPLES) {
                                    isGpsInitialized = true
                                    Log.d("RunTrackingService", "GPS inizializzato con accuratezza: ${location.accuracy}m")
                                } else {
                                    velocitaAttuale = 0f
                                    continue  // Salta questa lettura
                                }
                            } else {
                                velocitaAttuale = 0f
                                Log.d("RunTrackingService", "In attesa di migliore accuratezza GPS: ${location.accuracy}m")
                                continue  // Salta questa lettura
                            }
                        }

                        val velocitaIstantanea = location.speed * 3.6f
                lastSpeedUpdateTime = System.currentTimeMillis()

                // Se la velocità istantanea è molto bassa o non c'è movimento significativo
                if (velocitaIstantanea <= 0.1f || location.speed <= 0.1f) {
                    velocitaAttuale = 0f  // Forza la velocità a zero
                    contatoreSottoSoglia++
                    Log.d("PausaDebug", "Velocità forzata a zero, contatore: $contatoreSottoSoglia")
                } else {
                    velocitaAttuale = calcolaVelocitaFiltrata(velocitaIstantanea)
                }

                Log.d("PausaDebug", """
                LocationCallback:
                velocitaIstantanea: $velocitaIstantanea
                velocitaAttuale: $velocitaAttuale
                location.speed: ${location.speed}
                accuracy: ${location.accuracy}
            """.trimIndent())

                // Se non c'è movimento significativo
                if (velocitaAttuale <= 0.1f) {
                    contatoreSottoSoglia++
                    Log.d("PausaDebug", "Movimento minimo rilevato, contatore: $contatoreSottoSoglia")
                    if (contatoreSottoSoglia >= CONTATORE_SOGLIA_PAUSA && !isPaused) {
                        Log.d("PausaDebug", "Attivazione pausa da locationCallback")
                        iniziaPausaAutomatica()
                    }
                } else {
                    contatoreSottoSoglia = 0
                    if (isPaused) {
                        terminaPausaAutomatica()
                    }
                }

                if (!isPaused) {
                    if (posizionePrecedente != null) {
                        if (location.accuracy <= 20f) {  // Usa solo posizioni con buona accuratezza
                            val distanza = posizionePrecedente!!.distanceTo(location)
                            val tempoTraScorsoTraPunti = (location.time - posizionePrecedente!!.time) / 1000f // in secondi

                            // Calcola la velocità tra questi due punti
                            val velocitaTraPunti = if (tempoTraScorsoTraPunti > 0)
                                (distanza / tempoTraScorsoTraPunti) * 3.6f // km/h
                            else
                                0f

                            // Aggiorna la distanza solo se il movimento è realistico
                            if (distanza >= 1.0f &&
                                velocitaTraPunti > VELOCITA_SOGLIA_PAUSA &&
                                velocitaTraPunti < 25f &&
                                tempoTraScorsoTraPunti > 0) {

                                Log.d("VelocitaDebug", """
                    Aggiunta distanza:
                    distanza: $distanza m
                    tempo tra punti: $tempoTraScorsoTraPunti s
                    velocità tra punti: $velocitaTraPunti km/h
                """.trimIndent())

                                distanzaCorsa += distanza
                            }
                        }

                        tempoCorsa = System.currentTimeMillis() - oraInizio - tempoTotaleInPausa

                        // Calcolo velocità media
                        if (tempoCorsa > 0 && distanzaCorsa > 0) {
                            val tempoOre = tempoCorsa / (1000.0f * 3600.0f)
                            val nuovaVelocitaMedia = (distanzaCorsa / 1000.0f) / tempoOre

                            // Calcolo del pace (minuti per km)
                            paceMedia = if (distanzaCorsa >= 1) {
                                (tempoCorsa / 60000.0f) / (distanzaCorsa / 1000.0f)
                            } else {
                                0f
                            }

                            Log.d("VelocitaDebug", """
        Calcolo velocità media:
        distanza totale: $distanzaCorsa m
        tempo effettivo: $tempoCorsa ms
        tempo in ore: $tempoOre
        velocità attuale: $velocitaAttuale km/h
        velocità media calcolata: $nuovaVelocitaMedia km/h
        pace medio: ${String.format("%.2f", paceMedia)} min/km
    """.trimIndent())

                            velocitaMedia = minOf(nuovaVelocitaMedia, 25f)
                        }
                    }
                    posizionePrecedente = location
                }  else {
                velocitaAttuale = 0f
                    posizionePrecedente = null

                Log.d("VelocitaDebug", """
        In pausa:
        distanza totale: $distanzaCorsa m
        tempo effettivo: $tempoCorsa ms
        velocità attuale: $velocitaAttuale km/h
    """.trimIndent())
            }

                // Aggiorna la posizione sulla mappa
                val latLng = LatLng(location.latitude, location.longitude)
                sendLocationUpdate(latLng)
                sendRunUpdate(distanzaCorsa, tempoCorsa, velocitaAttuale, velocitaMedia)
            }
        }
    }


    private fun sendLocationUpdate(latLng: LatLng) {
        val intent = Intent(ACTION_RUN_LOCATION_UPDATE)
        intent.putExtra(EXTRA_LATITUDE, latLng.latitude)
        intent.putExtra(EXTRA_LONGITUDE, latLng.longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun iniziaPausaAutomatica() {
        Log.d("PausaDebug", """
        Tentativo di iniziare pausa automatica
        isPaused attuale: $isPaused
        velocitaAttuale: $velocitaAttuale
        distanzaCorsa: $distanzaCorsa
    """.trimIndent())

        if (!isPaused) {
            isPaused = true
            pausaInizio = System.currentTimeMillis()
            velocitaAttuale = 0f
            Log.d("PausaDebug", "Pausa automatica ATTIVATA")
            mostraNotificaPausa()
            sendRunUpdate(distanzaCorsa, tempoCorsa, velocitaAttuale, velocitaMedia)
            updateNotification()
        }
    }

    private fun terminaPausaAutomatica() {
        if (isPaused) {
            val pausaDurata = System.currentTimeMillis() - pausaInizio
            Log.d("PausaDebug", """
            Terminazione pausa:
            Inizio pausa: $pausaInizio
            Fine pausa: ${System.currentTimeMillis()}
            Durata pausa: $pausaDurata
            Tempo totale in pausa prima: $tempoTotaleInPausa
        """.trimIndent())

            tempoTotaleInPausa += pausaDurata

            Log.d("PausaDebug", "Tempo totale in pausa dopo: $tempoTotaleInPausa")
            isPaused = false
            pausaInizio = 0L
            contatoreSottoSoglia = 0
            updateNotification()
        }
    }

    private fun mostraNotificaPausa() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "run_tracking_service")
            .setContentTitle("Attività in pausa")
            .setContentText("Sei in pausa? Attività stoppata!")
            .setSmallIcon(R.drawable.directions_run_24px)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .build()
        notificationManager.notify(2, notification)
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notificationBuilder = NotificationCompat.Builder(this, "run_tracking_service")
            .setSmallIcon(R.drawable.directions_run_24px)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)

        if (isPaused) {
            notificationBuilder
                .setContentTitle("Attività in pausa")
                .setContentText("Sei in pausa? Attività stoppata!")
        } else {
            val notificationContent = getNotificationContent()
            notificationBuilder
                .setContentTitle("Tracciamento corsa attivo")
                .setContentText(notificationContent)
        }

        val notification = notificationBuilder.build()
        notificationManager.notify(2, notification)
    }

    private fun getNotificationContent(): String {
        val distanza = if (distanzaCorsa < 1000) {
            "${distanzaCorsa.toInt()} metri"
        } else {
            "${String.format("%.2f", distanzaCorsa / 1000)} km"
        }
        val tempoTrascorso = tempoCorsa / 1000
        val minuti = tempoTrascorso / 60
        val secondi = tempoTrascorso % 60
        val tempo = if (minuti > 0) {
            String.format("%02d:%02d", minuti, secondi)
        } else {
            String.format("%02d sec", secondi)
        }

        val velocita = String.format("%.2f", velocitaAttuale) + " km/h"
        val velocitaMediaText = String.format("%.2f", velocitaMedia) + " km/h"
        val paceText = String.format("%.2f", paceMedia) + " min/km"

        return "Distanza: $distanza, Tempo: $tempo, Velocità: $velocita, Media: $velocitaMediaText, Pace: $paceText"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "run_tracking_service",
                "Run Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            Log.d("RunTrackingService", "Canale di notifica creato con successo")
        }
    }

    companion object {
        const val ACTION_RUN_UPDATE = "com.example.personalphysicaltracker.ACTION_RUN_UPDATE"
        const val EXTRA_DISTANZA = "com.example.personalphysicaltracker.EXTRA_DISTANZA"
        const val EXTRA_TEMPO_CORSA = "com.example.personalphysicaltracker.EXTRA_TEMPO_CORSA"
        const val EXTRA_VELOCITA_ATTUALE = "com.example.personalphysicaltracker.EXTRA_VELOCITA_ATTUALE"
        const val EXTRA_VELOCITA_MEDIA = "com.example.personalphysicaltracker.EXTRA_VELOCITA_MEDIA"
        const val EXTRA_IS_PAUSED = "com.example.personalphysicaltracker.EXTRA_IS_PAUSED"

        const val ACTION_RUN_LOCATION_UPDATE = "com.example.personalphysicaltracker.ACTION_RUN_LOCATION_UPDATE"
        const val EXTRA_LATITUDE = "EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "EXTRA_LONGITUDE"
        const val EXTRA_PACE_MEDIO = "com.example.personalphysicaltracker.EXTRA_PACE_MEDIO"

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isAutomatica = intent?.getBooleanExtra("Automatica", true) ?: true
        Log.d("RunTrackingService", "onStartCommand chiamato")
        val resetRunCount = intent?.getBooleanExtra("resetRunCount", false) ?: false
        if (resetRunCount) {
            distanzaCorsa = 0f
            tempoCorsa = 0L
            oraInizio = System.currentTimeMillis()
            posizionePrecedente = null
            Log.d("RunTrackingService", "Contatore corsa resettato")
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RunTrackingService", "onDestroy chiamato")

        // Deregistra il BroadcastReceiver
        unregisterReceiver(stopServiceReceiver)

        timer.cancel()
        updateHandler.removeCallbacks(updateRunnable)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        val oraFine = System.currentTimeMillis()

        if (distanzaCorsa > 0 && tempoCorsa > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val nuovaAttività = Attività(
                        tipo = "Corsa",
                        oraInizio = oraInizio,
                        oraFine = oraFine,
                        distanza = distanzaCorsa,
                        tempo = tempoCorsa,
                        velocitaMedia = velocitaMedia,
                        paceMedio = paceMedia,
                        tempoInPausa = tempoTotaleInPausa,
                        Automatica = isAutomatica
                    )
                    db.attivitàDao().inserisciAttività(nuovaAttività)
                    Log.d("RunTrackingService", "Attività corsa salvata nel database")
                } catch (e: Exception) {
                    Log.e("RunTrackingService", "Errore nel salvataggio dell'attività", e)
                }
            }
        } else {
            Log.d("RunTrackingService", "Attività non salvata: dati insufficienti")
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): RunTrackingService = this@RunTrackingService
    }
}
