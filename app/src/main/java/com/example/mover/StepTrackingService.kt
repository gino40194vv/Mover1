package com.example.mover

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mover.data.AppDatabase
import com.example.mover.data.Attività
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StepTrackingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensorePassi: Sensor? = null
    var passiEffettuati: Int = 0
    private lateinit var db: AppDatabase
    private var oraInizio: Long = 0
    private var isAutomatica: Boolean = true
    private var distanzaPercorsa: Float = 0f
    private var tempoTrascorso: Long = 0L



    private val stopServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("StepTrackingService", "Comando ricevuto per fermare il servizio")
            stopSelf()
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Registrare il BroadcastReceiver per fermare il servizio
        val filter = IntentFilter("STOP_STEP_TRACKING_SERVICE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopServiceReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stopServiceReceiver, filter)
        }
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorePassi = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        db = AppDatabase.getDatabase(this)
        updateHandler.post(updateRunnable)

        if (sensorePassi != null) {
            sensorManager.registerListener(this, sensorePassi, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("StepTrackingService", "Registrato al sensore dei passi")
        } else {
            Log.e("StepTrackingService", "Sensore di passi non disponibile.")
            stopSelf()
            return
        }
        oraInizio = System.currentTimeMillis()

        startForegroundService()
    }

    private fun startForegroundService() {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "step_tracking_service")
            .setContentTitle("Tracciamento passi attivo")
            .setContentText("Sto monitorando i tuoi passi")
            .setSmallIcon(R.drawable.footprint_24px)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(1, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "step_tracking_service",
                "Step Tracking Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val ACTION_STEP_COUNT_UPDATE = "com.example.personalphysicaltracker.ACTION_STEP_COUNT_UPDATE"
        const val EXTRA_STEP_COUNT = "EXTRA_STEP_COUNT"
        const val EXTRA_DISTANCE = "EXTRA_DISTANCE"
        const val EXTRA_ELAPSED_TIME = "EXTRA_ELAPSED_TIME"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isAutomatica = intent?.getBooleanExtra("Automatica", true) ?: true
        val resetStepCount = intent?.getBooleanExtra("resetStepCount", false) ?: false
        if (resetStepCount) {
            passiEffettuati = 0
            oraInizio = System.currentTimeMillis()
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            passiEffettuati++
            Log.d("StepTrackingService", "Passi effettuati: $passiEffettuati")

            distanzaPercorsa = passiEffettuati * 0.7f

            // Invia un broadcast con il conteggio aggiornato
            val intent = Intent(ACTION_STEP_COUNT_UPDATE)
            intent.putExtra(EXTRA_STEP_COUNT, passiEffettuati)
            intent.putExtra(EXTRA_ELAPSED_TIME, tempoTrascorso)
            intent.putExtra(EXTRA_DISTANCE, distanzaPercorsa)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    private val updateHandler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {
            tempoTrascorso = System.currentTimeMillis() - oraInizio

            // Invia un broadcast con tutti i dati aggiornati ogni secondo
            val intent = Intent(ACTION_STEP_COUNT_UPDATE)
            intent.putExtra(EXTRA_STEP_COUNT, passiEffettuati)
            intent.putExtra(EXTRA_ELAPSED_TIME, tempoTrascorso)
            intent.putExtra(EXTRA_DISTANCE, distanzaPercorsa)
            LocalBroadcastManager.getInstance(this@StepTrackingService).sendBroadcast(intent)

            updateHandler.postDelayed(this, 1000)
        }
    }

    // Binder per il servizio
    inner class LocalBinder : Binder() {
        fun getService(): StepTrackingService = this@StepTrackingService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        val oraFine = System.currentTimeMillis()
        unregisterReceiver(stopServiceReceiver)
        if (passiEffettuati > 0 && (oraFine - oraInizio) > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val nuovaAttività = Attività(
                        tipo = "Camminare",
                        oraInizio = oraInizio,
                        oraFine = oraFine,
                        passi = passiEffettuati,
                        distanza = distanzaPercorsa,
                        tempo = tempoTrascorso,
                        Automatica = isAutomatica
                    )
                    db.attivitàDao().inserisciAttività(nuovaAttività)
                } catch (e: Exception) {
                    Log.e("StepTrackingService", "Errore nel salvataggio dell'attività", e)
                }
            }
        } else {
            Log.d("StepTrackingService", "Attività non salvata: nessun passo registrato o durata zero")
        }
        updateHandler.removeCallbacks(updateRunnable)
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
