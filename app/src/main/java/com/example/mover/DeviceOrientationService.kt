package com.example.mover

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.abs
import kotlin.math.sqrt

class DeviceOrientationService : Service(), SensorEventListener {

    private val INVISIBLE_NOTIFICATION_ID = 1234
    private val NOTIFICATION_CHANNEL_ID = "DeviceOrientationChannel"
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Stato della seduta
    private enum class SeatState {
        UNKNOWN,       // Stato iniziale
        TRANSITIONING, // Passaggio tra seduto e in piedi
        SEATED,        // Sicuramente seduto
        STANDING       // Sicuramente in piedi
    }

    // Parametri per la gestione dello stato
    private companion object {
        const val TRANSITION_TIMEOUT = 5000L      // 5 secondi per considerare transizione
        const val MIN_SEATED_DURATION = 7000L       // 7 secondi per considerare seduta stabile
        const val STANDING_CONFIRMATION_TIME = 4000L // 4 secondi per confermare che è in piedi
        const val MOVEMENT_THRESHOLD = 5.0f         // Soglia di movimento

        // Soglia minima per considerare che il dispositivo è inclinato di lato
        const val MIN_SIDE_TILT = 45.0
    }

    // Variabili di stato
    private var currentSeatState = SeatState.UNKNOWN
    private var stateTransitionTimestamp: Long = 0
    private var lastStableSeatedTimestamp: Long = 0
    private var seatedEventSent = false

    // Letture dei sensori
    private val accelerometerReading = FloatArray(3)
    private val orientationAngles = FloatArray(3)
    private val rotationMatrix = FloatArray(9)

    // Variabili per rilevamento movimento
    private var lastAccelerationMagnitude: Float = 0f
    private var movementCount = 0
    private val MAX_MOVEMENT_COUNT = 5

    private var totalSeatedTime: Long = 0L
    private var lastSeatedTimestamp: Long = 0L

    private val CHANNEL_ID = "DeviceOrientationServiceChannel"

    override fun onCreate() {
        super.onCreate()

        // Crea un canale di notifica invisibile
        createNotificationChannel()

        // Crea una notifica con priorità minima
        val notification = createForegroundNotification()

        // Avvia il servizio in primo piano con tipo HEALTH
        startForeground(
            INVISIBLE_NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupSensors()
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Aggiorna la lettura dell'accelerometro
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)

            // Calcola magnitudine dell'accelerazione
            val currentAccelerationMagnitude = calculateAccelerationMagnitude(event)
            // Valuta se c'è movimento significativo
            val isMoving = isSignificantMovement(currentAccelerationMagnitude)
            // Calcola pitch (per log o per eventuali altri controlli)
            val pitch = calculatePitch()

            val roll = calculateRoll()

            val isInSeatedRange = abs(roll) >= MIN_SIDE_TILT

            updateSeatState(isInSeatedRange, isMoving)
        }
    }

    private fun calculateAccelerationMagnitude(event: SensorEvent): Float {
        return sqrt(
            event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
        )
    }

    private fun isSignificantMovement(currentAccelerationMagnitude: Float): Boolean {
        val movementDelta = abs(currentAccelerationMagnitude - lastAccelerationMagnitude)
        lastAccelerationMagnitude = currentAccelerationMagnitude
        return movementDelta > MOVEMENT_THRESHOLD
    }

    private fun calculatePitch(): Double {
        val gravity = FloatArray(3)
        gravity[0] = accelerometerReading[0]
        gravity[1] = accelerometerReading[1]
        gravity[2] = accelerometerReading[2]

        val normGravity = sqrt(
            gravity[0] * gravity[0] +
                    gravity[1] * gravity[1] +
                    gravity[2] * gravity[2]
        )

        if (normGravity == 0f) return 0.0

        gravity[0] /= normGravity
        gravity[1] /= normGravity
        gravity[2] /= normGravity

        return Math.toDegrees(Math.asin(gravity[1].toDouble()))
    }

    private fun calculateRoll(): Double {
        val gravity = FloatArray(3)
        gravity[0] = accelerometerReading[0]
        gravity[1] = accelerometerReading[1]
        gravity[2] = accelerometerReading[2]

        val normGravity = sqrt(
            gravity[0] * gravity[0] +
                    gravity[1] * gravity[1] +
                    gravity[2] * gravity[2]
        )

        if (normGravity == 0f) return 0.0

        gravity[0] /= normGravity
        gravity[1] /= normGravity
        gravity[2] /= normGravity

        // Calcola il roll se il dispositivo è perfettamente dritto
        return Math.toDegrees(Math.atan2(gravity[0].toDouble(), gravity[2].toDouble()))
    }


    private fun updateSeatState(isInSeatedRange: Boolean, isMoving: Boolean) {
        val currentTime = System.currentTimeMillis()

        when (currentSeatState) {
            SeatState.UNKNOWN -> {
                if (isInSeatedRange) {
                    currentSeatState = SeatState.TRANSITIONING
                    stateTransitionTimestamp = currentTime
                    totalSeatedTime = 0L
                    seatedEventSent = false
                    Log.d("DeviceOrientationService", "Inizia nuova transizione - Reset contatori")
                }
            }

            SeatState.TRANSITIONING -> {
                if (isInSeatedRange) {
                    if (currentTime - stateTransitionTimestamp >= TRANSITION_TIMEOUT) {
                        currentSeatState = SeatState.SEATED
                        lastStableSeatedTimestamp = currentTime
                        lastSeatedTimestamp = currentTime
                        movementCount = 0
                        Log.d("DeviceOrientationService", "Transition from TRANSITIONING to SEATED")
                    }
                } else {
                    currentSeatState = SeatState.STANDING
                    stateTransitionTimestamp = currentTime
                    Log.d("DeviceOrientationService", "Transition from TRANSITIONING to STANDING (inclinazione laterale persa)")
                }
            }

            SeatState.SEATED -> {
                totalSeatedTime += (currentTime - lastSeatedTimestamp)
                lastSeatedTimestamp = currentTime

                if (!isInSeatedRange) {
                    currentSeatState = SeatState.STANDING
                    stateTransitionTimestamp = currentTime
                    sendFinalSeatedTime()
                    Log.d("DeviceOrientationService", "Transizione da SEATED a STANDING")
                } else {
                    if (isMoving) {
                        movementCount++
                        Log.d("DeviceOrientationService", "Movimento rilevato mentre SEATED. Conteggio: $movementCount")
                    }
                    if (movementCount > MAX_MOVEMENT_COUNT) {
                        currentSeatState = SeatState.TRANSITIONING
                        stateTransitionTimestamp = currentTime
                        movementCount = 0
                        sendFinalSeatedTime()
                        Log.d("DeviceOrientationService", "Troppo movimento, reset stato")
                    }
                    checkAndSendSeatedEvent(currentTime)
                }
            }

            SeatState.STANDING -> {
                if (isInSeatedRange) {
                    if (currentTime - stateTransitionTimestamp < STANDING_CONFIRMATION_TIME) {
                        currentSeatState = SeatState.SEATED
                        lastSeatedTimestamp = currentTime
                        Log.d("DeviceOrientationService", "Ritorno breve a SEATED")
                    } else {
                        handleStandingConfirmed()
                        currentSeatState = SeatState.TRANSITIONING
                        stateTransitionTimestamp = currentTime
                        Log.d("DeviceOrientationService", "Nuova transizione dopo standing confermato")
                    }
                } else {
                    if (currentTime - stateTransitionTimestamp >= STANDING_CONFIRMATION_TIME) {
                        handleStandingConfirmed()
                    }
                }
            }
        }
    }

    private fun handleStandingConfirmed() {
        Log.d("DeviceOrientationService", "handleStandingConfirmed chiamato. Tempo totale seduto: ${totalSeatedTime/1000.0} secondi")
        // Prima inviamo il tempo attuale
        val updateIntent = Intent(this, SedutaTrackingService::class.java).apply {
            action = SedutaTrackingService.ACTION_UPDATE_TIME
            putExtra("seatedTime", totalSeatedTime)
        }
        startService(updateIntent)

        // Reset solo quando confermiamo che siamo in piedi
        totalSeatedTime = 0L
        seatedEventSent = false
        lastStableSeatedTimestamp = 0L
        lastSeatedTimestamp = System.currentTimeMillis()
        Log.d("DeviceOrientationService", "Standing confermato - Reset contatori")
    }


    private fun sendFinalSeatedTime() {
        val intent = Intent(this, SedutaTrackingService::class.java).apply {
            action = SedutaTrackingService.ACTION_UPDATE_TIME
            putExtra("seatedTime", totalSeatedTime)
        }
        startService(intent)
    }

    private fun checkAndSendSeatedEvent(currentTime: Long) {
        if (!seatedEventSent && currentTime - lastStableSeatedTimestamp >= MIN_SEATED_DURATION) {
            sendSedutaIncrement()
            seatedEventSent = true
        }
    }

    private fun sendSedutaIncrement() {
        val intent = Intent(this, SedutaTrackingService::class.java).apply {
            action = SedutaTrackingService.ACTION_INCREMENT_SEDUTA_COUNT
        }
        startService(intent)
    }


    override fun onDestroy() {
        // Se siamo in stato SEATED, aggiorna il tempo totale seduto
        if (currentSeatState == SeatState.SEATED) {
            val finalTime = System.currentTimeMillis()
            totalSeatedTime += (finalTime - lastSeatedTimestamp)
            Log.d("DeviceOrientationService", "onDestroy: Updated totalSeatedTime: ${totalSeatedTime/1000.0} seconds")
        }

        // Invia sempre il tempo finale
        val intent = Intent(this, SedutaTrackingService::class.java).apply {
            action = SedutaTrackingService.ACTION_UPDATE_TIME
            putExtra("seatedTime", totalSeatedTime)
        }
        startService(intent)
        Log.d("DeviceOrientationService", "Service destroyed. Sent final seated time: ${totalSeatedTime/1000.0} seconds")
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun setupSensors() {
        val orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        if (orientationSensor != null) {
        } else {
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Device Orientation Service Channel",
                NotificationManager.IMPORTANCE_MIN
            )
            serviceChannel.description = "Monitoraggio dell'orientamento del dispositivo"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createForegroundNotification(): Notification {
        Log.d("DeviceOrientationService", "Creating foreground notification")
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentTitle("Monitoraggio orientamento")
            .setContentText("Monitoraggio posizione seduta in corso")
            .setSmallIcon(R.drawable.seat_24px)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
}
