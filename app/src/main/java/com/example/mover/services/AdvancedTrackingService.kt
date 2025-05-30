package com.example.mover.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mover.R
import com.example.mover.data.*
import com.example.mover.utils.MetricsCalculator
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Servizio avanzato per il tracking di tutte le attività fisiche
 * Supporta GPS, sensori, calcoli metriche avanzate e compatibilità Strava
 */
class AdvancedTrackingService : Service(), SensorEventListener {
    
    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: AppDatabase
    private lateinit var sensorManager: SensorManager
    
    // Sensori
    private var stepCounterSensor: Sensor? = null
    private var heartRateSensor: Sensor? = null
    private var pressureSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    
    // Stato del tracking
    private var isTracking = false
    private var isPaused = false
    private var currentActivity: Attività? = null
    private var tipoAttivitàCorrente: TipoAttività? = null
    
    // Dati GPS e sensori
    private val puntiGPS = mutableListOf<PuntoGPS>()
    private var ultimaPosizioneGPS: Location? = null
    private var ultimoTimestampGPS: Long = 0
    
    // Metriche in tempo reale
    private var distanzaTotale: Float = 0f
    private var tempoInizio: Long = 0
    private var tempoPausa: Long = 0
    private var ultimoTimestampPausa: Long = 0
    private var passiTotali: Int = 0
    private var passiIniziali: Int = 0
    private var frequenzaCardiacaCorrente: Int = 0
    private var cadenzaCorrente: Float = 0f
    private var potenzaCorrente: Float = 0f
    
    // Handler per aggiornamenti periodici
    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isTracking && !isPaused) {
                aggiornaMetriche()
                inviaAggiornamento()
            }
            updateHandler.postDelayed(this, UPDATE_INTERVAL)
        }
    }
    
    // Callback per location updates
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            
            if (!isTracking || isPaused) return
            
            locationResult.lastLocation?.let { location ->
                elaboraPosizioneGPS(location)
            }
        }
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): AdvancedTrackingService = this@AdvancedTrackingService
    }
    
    override fun onCreate() {
        super.onCreate()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        db = AppDatabase.getDatabase(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        inizializzaSensori()
        creaNotificationChannel()
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val tipoAttivita = intent.getStringExtra(EXTRA_ACTIVITY_TYPE)
                tipoAttivita?.let { 
                    val tipo = TipoAttività.fromString(it)
                    if (tipo != null) {
                        iniziaTracking(tipo)
                    }
                }
            }
            ACTION_PAUSE_TRACKING -> pausaTracking()
            ACTION_RESUME_TRACKING -> riprendi()
            ACTION_STOP_TRACKING -> fermaTracking()
        }
        
        return START_STICKY
    }
    
    private fun inizializzaSensori() {
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }
    
    private fun creaNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Tracking Attività",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifiche per il tracking delle attività fisiche"
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun iniziaTracking(tipoAttività: TipoAttività) {
        if (isTracking) return
        
        tipoAttivitàCorrente = tipoAttività
        isTracking = true
        isPaused = false
        tempoInizio = System.currentTimeMillis()
        
        // Reset metriche
        distanzaTotale = 0f
        tempoPausa = 0
        puntiGPS.clear()
        ultimaPosizioneGPS = null
        
        // Crea nuova attività nel database
        CoroutineScope(Dispatchers.IO).launch {
            currentActivity = Attività(
                tipo = tipoAttività.name,
                sottotipo = tipoAttività.displayName,
                oraInizio = tempoInizio,
                oraFine = 0,
                Automatica = false
            )

            val insertedId = db.attivitàDao().inserisciAttività(currentActivity!!)
            currentActivity = currentActivity!!.copy(id = insertedId)
            
            withContext(Dispatchers.Main) {
                avviaServiziForeground()
                if (tipoAttività.supportaGPS) {
                    avviaTrackingGPS()
                }
                avviaSensori()
                updateHandler.post(updateRunnable)
            }
        }
    }
    
    private fun avviaServiziForeground() {
        val notification = creaNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
    
    private fun avviaTrackingGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            GPS_UPDATE_INTERVAL
        ).apply {
            setMinUpdateDistanceMeters(MIN_DISTANCE_CHANGE)
            setMaxUpdateDelayMillis(GPS_UPDATE_INTERVAL * 2)
        }.build()
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
    
    private fun avviaSensori() {
        // Registra sensori disponibili
        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            // Ottieni il valore iniziale dei passi
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Il sensore TYPE_STEP_COUNTER fornisce il numero totale di passi dal boot
                passiIniziali = 0 // Verrà aggiornato nel primo evento del sensore
            }
        }
        
        heartRateSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        
        pressureSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        accelerometerSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }
    
    private fun elaboraPosizioneGPS(location: Location) {
        val timestamp = System.currentTimeMillis()
        
        // Calcola distanza dal punto precedente
        var distanzaDalPrecedente = 0f
        var tempoDalPrecedente = 0L
        var velocita = 0f
        
        ultimaPosizioneGPS?.let { ultimaPosizione ->
            distanzaDalPrecedente = ultimaPosizione.distanceTo(location)
            tempoDalPrecedente = timestamp - ultimoTimestampGPS
            velocita = MetricsCalculator.calcolaVelocita(distanzaDalPrecedente, tempoDalPrecedente)
            
            // Aggiorna distanza totale solo se il movimento è significativo
            if (distanzaDalPrecedente > MIN_DISTANCE_CHANGE && velocita < 50.0f) {
                distanzaTotale += distanzaDalPrecedente
            }
        }
        
        // Crea punto GPS
        val puntoGPS = PuntoGPS(
            attivitaId = currentActivity?.id ?: 0L,
            latitudine = location.latitude,
            longitudine = location.longitude,
            altitudine = if (location.hasAltitude()) location.altitude else null,
            precisione = if (location.hasAccuracy()) location.accuracy else null,
            timestamp = timestamp,
            tempoRelativo = timestamp - tempoInizio,
            velocita = velocita,
            direzione = if (location.hasBearing()) location.bearing else null,
            distanzaDalPrecedente = distanzaDalPrecedente,
            tempoDalPrecedente = tempoDalPrecedente,
            frequenzaCardiaca = if (frequenzaCardiacaCorrente > 0) frequenzaCardiacaCorrente else null,
            cadenza = if (cadenzaCorrente > 0) cadenzaCorrente else null,
            potenza = if (potenzaCorrente > 0) potenzaCorrente else null
        )
        
        puntiGPS.add(puntoGPS)
        ultimaPosizioneGPS = location
        ultimoTimestampGPS = timestamp
        
        // Salva punto GPS nel database ogni 10 punti per ottimizzare le performance
        if (puntiGPS.size % 10 == 0) {
            CoroutineScope(Dispatchers.IO).launch {
                db.attivitàDao().inserisciPuntiGPS(puntiGPS.takeLast(10))
            }
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                if (passiIniziali == 0) {
                    passiIniziali = event.values[0].toInt()
                }
                passiTotali = event.values[0].toInt() - passiIniziali
                
                // Calcola cadenza (passi per minuto)
                val tempoTrascorso = (System.currentTimeMillis() - tempoInizio) / 60000f
                if (tempoTrascorso > 0) {
                    cadenzaCorrente = passiTotali / tempoTrascorso
                }
            }
            
            Sensor.TYPE_HEART_RATE -> {
                frequenzaCardiacaCorrente = event.values[0].toInt()
            }
            
            Sensor.TYPE_PRESSURE -> {
                // Usa la pressione per calcolare l'altitudine se il GPS non è disponibile
                // Altitudine = 44330 * (1 - (pressione/pressione_livello_mare)^(1/5.255))
                val pressione = event.values[0]
                val altitudine = 44330 * (1 - Math.pow((pressione / 1013.25), (1 / 5.255)))
                
                // Aggiorna l'ultimo punto GPS con l'altitudine calcolata se disponibile
                if (puntiGPS.isNotEmpty() && puntiGPS.last().altitudine == null) {
                    val ultimoPunto = puntiGPS.last()
                    puntiGPS[puntiGPS.size - 1] = ultimoPunto.copy(altitudine = altitudine)
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Gestisci cambiamenti di precisione dei sensori se necessario
    }
    
    fun pausaTracking() {
        if (!isTracking || isPaused) return
        
        isPaused = true
        ultimoTimestampPausa = System.currentTimeMillis()
        
        // Ferma location updates per risparmiare batteria
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        inviaAggiornamento()
    }
    
    fun riprendi() {
        if (!isTracking || !isPaused) return
        
        isPaused = false
        tempoPausa += System.currentTimeMillis() - ultimoTimestampPausa
        
        // Riprendi location updates
        tipoAttivitàCorrente?.let { tipo ->
            if (tipo.supportaGPS) {
                avviaTrackingGPS()
            }
        }
        
        inviaAggiornamento()
    }
    
    fun fermaTracking() {
        if (!isTracking) return
        
        isTracking = false
        isPaused = false
        
        // Ferma tutti i servizi
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
        updateHandler.removeCallbacks(updateRunnable)
        
        // Calcola statistiche finali e salva attività
        CoroutineScope(Dispatchers.IO).launch {
            salvaAttivitàCompleta()
        }
        
        stopForeground(true)
        stopSelf()
    }
    
    private suspend fun salvaAttivitàCompleta() {
        currentActivity?.let { attività ->
            // Salva tutti i punti GPS rimanenti
            if (puntiGPS.isNotEmpty()) {
                db.attivitàDao().inserisciPuntiGPS(puntiGPS)
            }
            
            // Calcola statistiche finali
            val statistiche = MetricsCalculator.calcolaStatistiche(puntiGPS)
            val tempoTotale = System.currentTimeMillis() - tempoInizio
            val tempoInMovimento = tempoTotale - tempoPausa
            
            // Calcola calorie se abbiamo il peso dell'utente
            val sharedPrefs = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            val pesoUtente = sharedPrefs.getFloat("peso", 70f)
            val calorie = tipoAttivitàCorrente?.let { tipo ->
                MetricsCalculator.calcolaCalorie(
                    tipo, 
                    pesoUtente, 
                    tempoInMovimento / 60000f
                )
            }
            
            // Aggiorna attività con tutti i dati
            val attivitàCompleta = attività.copy(
                oraFine = System.currentTimeMillis(),
                tempo = tempoTotale,
                tempoInMovimento = tempoInMovimento,
                tempoInPausa = tempoPausa,
                distanza = statistiche.distanzaTotale,
                velocitaMedia = statistiche.velocitaMedia,
                velocitaMassima = statistiche.velocitaMassima,
                paceMedio = statistiche.paceMedia,
                dislivelloPositivo = statistiche.dislivelloPositivo,
                dislivelloNegativo = statistiche.dislivelloNegativo,
                altitudineMinima = statistiche.altitudineMinima,
                altitudineMassima = statistiche.altitudineMassima,
                calorie = calorie,
                frequenzaCardiacaMedia = statistiche.frequenzaCardiacaMedia,
                frequenzaCardiacaMassima = statistiche.frequenzaCardiacaMassima,
                cadenzaMedia = statistiche.cadenzaMedia,
                potenzaMedia = statistiche.potenzaMedia,
                potenzaMassima = statistiche.potenzaMassima,
                passi = if (passiTotali > 0) passiTotali else null,
                latitudineInizio = puntiGPS.firstOrNull()?.latitudine,
                longitudineInizio = puntiGPS.firstOrNull()?.longitudine,
                latitudineFine = puntiGPS.lastOrNull()?.latitudine,
                longitudineFine = puntiGPS.lastOrNull()?.longitudine,
                precisione = puntiGPS.mapNotNull { it.precisione }.average().takeIf { !it.isNaN() }?.toFloat()
            )
            
            db.attivitàDao().aggiornaAttività(attivitàCompleta)
            
            withContext(Dispatchers.Main) {
                inviaCompletamento(attivitàCompleta)
            }
        }
    }
    
    private fun aggiornaMetriche() {
        // Aggiorna metriche in tempo reale se necessario
        // Questo metodo viene chiamato ogni secondo durante il tracking
    }
    
    private fun inviaAggiornamento() {
        val intent = Intent(ACTION_TRACKING_UPDATE).apply {
            putExtra(EXTRA_DISTANCE, distanzaTotale)
            putExtra(EXTRA_TIME, System.currentTimeMillis() - tempoInizio - tempoPausa)
            putExtra(EXTRA_SPEED, ultimaPosizioneGPS?.speed ?: 0f)
            putExtra(EXTRA_STEPS, passiTotali)
            putExtra(EXTRA_HEART_RATE, frequenzaCardiacaCorrente)
            putExtra(EXTRA_CADENCE, cadenzaCorrente)
            putExtra(EXTRA_POWER, potenzaCorrente)
            putExtra(EXTRA_IS_PAUSED, isPaused)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    
    private fun inviaCompletamento(attività: Attività) {
        val intent = Intent(ACTION_TRACKING_COMPLETED).apply {
            putExtra(EXTRA_ACTIVITY_ID, attività.id)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    
    private fun creaNotification(): Notification {
        val tipoAttività = tipoAttivitàCorrente?.displayName ?: "Attività"
        val stato = if (isPaused) "In pausa" else "In corso"
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("$tipoAttività $stato")
            .setContentText(getNotificationText())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun getNotificationText(): String {
        val tempo = (System.currentTimeMillis() - tempoInizio - tempoPausa) / 1000
        val minuti = tempo / 60
        val secondi = tempo % 60
        
        return when {
            distanzaTotale > 0 -> String.format("%.2f km • %02d:%02d", distanzaTotale / 1000, minuti, secondi)
            passiTotali > 0 -> String.format("%d passi • %02d:%02d", passiTotali, minuti, secondi)
            else -> String.format("%02d:%02d", minuti, secondi)
        }
    }
    
    // Getters per l'UI
    fun isCurrentlyTracking(): Boolean = isTracking
    fun isCurrentlyPaused(): Boolean = isPaused
    fun getCurrentActivityType(): TipoAttività? = tipoAttivitàCorrente
    fun getCurrentDistance(): Float = distanzaTotale
    fun getCurrentTime(): Long = if (isTracking) System.currentTimeMillis() - tempoInizio - tempoPausa else 0
    fun getCurrentSteps(): Int = passiTotali
    fun getCurrentHeartRate(): Int = frequenzaCardiacaCorrente
    fun getCurrentCadence(): Float = cadenzaCorrente
    fun getCurrentPower(): Float = potenzaCorrente
    
    companion object {
        private const val TAG = "AdvancedTrackingService"
        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1001
        
        // Intervalli di aggiornamento
        private const val GPS_UPDATE_INTERVAL = 1000L // 1 secondo
        private const val UPDATE_INTERVAL = 1000L // 1 secondo
        private const val MIN_DISTANCE_CHANGE = 1f // 1 metro
        
        // Actions
        const val ACTION_START_TRACKING = "com.example.personalphysicaltracker.START_TRACKING"
        const val ACTION_PAUSE_TRACKING = "com.example.personalphysicaltracker.PAUSE_TRACKING"
        const val ACTION_RESUME_TRACKING = "com.example.personalphysicaltracker.RESUME_TRACKING"
        const val ACTION_STOP_TRACKING = "com.example.personalphysicaltracker.STOP_TRACKING"
        const val ACTION_TRACKING_UPDATE = "com.example.personalphysicaltracker.TRACKING_UPDATE"
        const val ACTION_TRACKING_COMPLETED = "com.example.personalphysicaltracker.TRACKING_COMPLETED"
        
        // Extras
        const val EXTRA_ACTIVITY_TYPE = "activity_type"
        const val EXTRA_ACTIVITY_ID = "activity_id"
        const val EXTRA_DISTANCE = "distance"
        const val EXTRA_TIME = "time"
        const val EXTRA_SPEED = "speed"
        const val EXTRA_STEPS = "steps"
        const val EXTRA_HEART_RATE = "heart_rate"
        const val EXTRA_CADENCE = "cadence"
        const val EXTRA_POWER = "power"
        const val EXTRA_IS_PAUSED = "is_paused"
    }
}
