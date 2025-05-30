package com.example.mover

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.example.mover.data.AppDatabase
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.graphics.ColorUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.example.mover.data.Attività
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import android.view.Gravity
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ProgressBar
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.mover.components.WeeklyChartsManager
import com.example.mover.ui.ActivitySelectionActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs


class MainActivity : AppCompatActivity(), OnMapReadyCallback, MapStyleUpdateListener  {

    private var attivitàCorrente: String? = null
    private var attivitàDaAvviare: String? = null
    private var oraInizio: Long = 0
    private var volteSeduto: Int = 0

    private var stepTrackingService: StepTrackingService? = null
    private var runTrackingService: RunTrackingService? = null
    private var isStepServiceBound = false
    private var isRunServiceBound = false


    private lateinit var btnCamminare: MaterialButton
    private lateinit var btnCorrere: MaterialButton
    private lateinit var btnGuidare: MaterialButton
    private lateinit var btnSedersi: MaterialButton
    private lateinit var btnMoreActivities: MaterialButton
    private lateinit var btnAnalisiPrestazioni: MaterialButton
    private lateinit var btnSocial: MaterialButton
    private lateinit var txtStato: TextView
    private lateinit var txtPassi: TextView
    private lateinit var txtVolteSeduto: TextView
    private lateinit var txtDistanza: TextView
    private lateinit var txtTempo: TextView
    private lateinit var txtVelocitaAttuale: TextView
    private lateinit var txtVelocitaMedia: TextView
    private lateinit var txtPausa: TextView
    private lateinit var contatoriCard: MaterialCardView
    private lateinit var bottomButtonsCard: MaterialCardView
    private lateinit var cardView : CardView
    private lateinit var cardContainer: FrameLayout
    private lateinit var countdownText: TextView
    private lateinit var statsLayout: LinearLayout
    private lateinit var switchAutomaticActivity: SwitchMaterial
    private lateinit var btnStorico: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var txtDistanzaUnit: TextView
    private lateinit var txtTempoSeconds: TextView
    private lateinit var txtTempoSecondsUnit: TextView
    private lateinit var txtTempoUnit: TextView
    private lateinit var txtVelocitaMediaUnit: TextView
    private lateinit var scrollView : NestedScrollView
    private lateinit var toolbar : Toolbar
    private lateinit var db: AppDatabase

    private lateinit var stepCountReceiver: BroadcastReceiver
    private lateinit var sedutaCountReceiver: BroadcastReceiver
    private var isSedutaReceiverRegistered = false
    private lateinit var btnSimulateSit: Button
    private lateinit var carReceiver: BroadcastReceiver
    private lateinit var bikeReceiver: BroadcastReceiver
    private var isCarReceiverRegistered = false
    private var isBikeReceiverRegistered = false
    private lateinit var layoutAutomaticActivityStats: LinearLayout



    private var pendingActivityToStart: String? = null

    private lateinit var runReceiver: BroadcastReceiver
    private lateinit var sittingTimeReceiver : BroadcastReceiver
    private lateinit var seatedTimeTickReceiver : BroadcastReceiver


    private lateinit var activityReceiver: BroadcastReceiver
    private var isRunReceiverRegistered = false
    private var isActivityReceiverRegistered = false

    private lateinit var fabAdd: FloatingActionButton
    private lateinit var fabWalk: FloatingActionButton
    private lateinit var fabRun: FloatingActionButton
    private lateinit var fabSit: FloatingActionButton
    private lateinit var fabDrive: FloatingActionButton
    private lateinit var layoutWalk: LinearLayout
    private lateinit var layoutRun: LinearLayout
    private lateinit var layoutSit: LinearLayout
    private lateinit var layoutDrive: LinearLayout
    private lateinit var overlayView: View
    private var isOverlayVisible = false
    private lateinit var fabInterrompi: FloatingActionButton
    private lateinit var gradientView : View
    private lateinit var view1: View
    private lateinit var view2: View
    private lateinit var actionBarTitle : ImageView
    private lateinit var statsContent : LinearLayout
    private var distanzaPercorsa: Float = 0f
    private var tempoCorsa: Long = 0
    private var tempoPausa: Long = 0
    private var inCorsa: Boolean = false
    private var isStepReceiverRegistered = false

    private lateinit var driveReceiver: BroadcastReceiver
    private var isDriveReceiverRegistered = false
    private lateinit var pauseReceiver: BroadcastReceiver
    private var isPauseReceiverRegistered = false

    private lateinit var txtRecognizedActivity: TextView
    private var lastScrollY: Int = 0
    private lateinit var spacerView: View


    private lateinit var layoutPassi: LinearLayout
    private lateinit var layoutVolteSeduto: LinearLayout
    private lateinit var layoutDistanza: LinearLayout
    private lateinit var layoutTempo: LinearLayout
    private lateinit var layoutVelocitaAttuale: LinearLayout
    private lateinit var layoutVelocitaMedia: LinearLayout
    private lateinit var layoutPaceMedio: LinearLayout

    private var initialMargin: Int = 0
    private var isCountdownRunning = false
    private lateinit var imgRecognizedActivity: ImageView

    private lateinit var txtPaceMedio: TextView
    private lateinit var txtPaceMedioUnit: TextView

    private lateinit var btnBicicletta: MaterialButton


    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private var lastScrollX = 0
    private val SCROLL_THRESHOLD = 100
    private var lastFeedbackTime = 0L
    private val FEEDBACK_DELAY = 100L

    private var currentPadding: Int = 0
    private var lastClickedButton: MaterialButton? = null

    private lateinit var runLocationReceiver: BroadcastReceiver
    private val runPathCoordinates = mutableListOf<LatLng>()
    private var polyline: Polyline? = null

    private lateinit var progressPassi: CircularProgressIndicator
    private lateinit var progressCorsa: CircularProgressIndicator
    private lateinit var progressSedute: CircularProgressIndicator

    private lateinit var txtProgressPassiLabel: TextView
    private lateinit var txtProgressCorsaLabel: TextView
    private lateinit var txtProgressSeduteLabel: TextView

    private lateinit var chartsManager: WeeklyChartsManager
    private lateinit var walkingChart: RoundedBarChart
    private lateinit var runningChart: RoundedBarChart
    private lateinit var sittingChart: RoundedBarChart

    private lateinit var statsManager: StatsManager
    private var lastStepCountFromSensor = 0  // per calcolare differenza se serve
    private var previousSensorDistance: Float = 0f

    private var originalStatusBarColor: Int = 0
    private var originalNavigationBarColor: Int = 0

    private lateinit var prefs: SharedPreferences

    private val goalSteps: Int
        get() = prefs.getInt("obiettivo_passi", 10000)

    private val goalRunningDistance: Float
        get() = prefs.getFloat("obiettivo_corsa", 5.0f) * 1000 // converte in metri

    private val goalMaxSittingTime: Long
        get() = prefs.getInt("limite_seduto", 8) * 3600000L

    companion object {
        private const val ACTIVITY_RECOGNITION_PERMISSION_CODE = 1001
        const val EXTRA_ACTIVITY_TYPE = "EXTRA_ACTIVITY_TYPE"
        const val EXTRA_ACTIVITY_CONFIDENCE = "EXTRA_ACTIVITY_CONFIDENCE"
        // Costanti per l'auto
        const val ACTION_CAR_UPDATE = "com.example.personalphysicaltracker.ACTION_CAR_UPDATE"
        const val CAR_EXTRA_DISTANCE = "extra_distance"
        const val CAR_EXTRA_SPEED = "extra_speed"
        const val CAR_EXTRA_DURATION = "extra_duration"

        private var lastColorMode: Int = -1


        // Costanti per la bici
        const val ACTION_BIKE_UPDATE = "com.example.personalphysicaltracker.ACTION_BIKE_UPDATE"
        const val BIKE_EXTRA_DISTANCE = "extra_distance"
        const val BIKE_EXTRA_SPEED = "extra_speed"
        const val BIKE_EXTRA_DURATION = "extra_duration"
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    }


    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
        when (key) {
            "obiettivo_passi" -> updateStepsProgress()
            "obiettivo_corsa" -> updateRunningProgress()
            "limite_seduto"   -> updateSittingProgress()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()  //questo è per più permessi
    ) { permissions ->
        var allGranted = true
        permissions.entries.forEach { entry ->
            if (!entry.value) {
                allGranted = false
            }
        }

        if (allGranted) {
            Log.d("MainActivity", "Permessi concessi")
            pendingActivityToStart?.let {
                startAppropriateService(it)
                pendingActivityToStart = null
            }
        } else {
            Toast.makeText(
                this,
                "Permessi necessari per il rilevamento dell'attività",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun activityTypeToString(activityType: Int): String {
        return when (activityType) {
            DetectedActivity.IN_VEHICLE -> "In Macchina"
            DetectedActivity.ON_BICYCLE -> "In Bicicletta"
            DetectedActivity.ON_FOOT -> "Camminare"
            DetectedActivity.RUNNING -> "Corsa"
            DetectedActivity.STILL -> "Fermo"
            DetectedActivity.TILTING-> "Seduto"
            DetectedActivity.WALKING -> "Camminare"
            DetectedActivity.UNKNOWN -> "Sto pensando..."
            else -> "Non riconosciuto"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize preferences
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val colorMode = prefs.getInt("app_color", 0)
        val savedTheme = prefs.getInt("app_theme", 0)

        when (savedTheme) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        // Imposta il tema in base a colorMode e alla versione di Android
        if (colorMode == 0) {
            setTheme(R.style.Theme_MyApp_MainActivity)
        } else if (colorMode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setTheme(R.style.Theme_MyApp_MainActivity_Dynamic)
            DynamicColors.applyToActivityIfAvailable(this)

        }


        super.onCreate(savedInstanceState)



// Scegli il layout da utilizzare in base al valore di colorMode
        if (colorMode == 0) {
            setContentView(R.layout.activity_main)
            Log.d("SettingsLifecycle", "Usando activity_main.xml")
        } else {
            setContentView(R.layout.activity_main_dynamic)
            Log.d("SettingsLifecycle", "Usando activity_main_dynamic.xml")
        }


        // Inizializzazione AdMob
        MobileAds.initialize(this) { initializationStatus ->
            // Callback dopo l'inizializzazione
            val statusMap = initializationStatus.adapterStatusMap
            for ((adapterClass, status) in statusMap) {
                Log.d("AdMob Init", "Adapter: $adapterClass, Status: ${status.initializationState}")
            }

            // Dopo che l'inizializzazione è completata, configura i dispositivi di test
            val testDeviceIds = listOf("33BE2250B43518CCDA7DE426D04EE231")
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }




        setupThemeAndStatusBar()
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)

// Determina se il tema dinamico è attivo
        val isDynamicTheme = prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        window.apply {
            setDecorFitsSystemWindows(false)

            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

            when {
                isDynamicTheme -> {
                    // Usa i colori del tema dinamico
                    statusBarColor = MaterialColors.getColor(
                        this@MainActivity,
                        com.google.android.material.R.attr.colorSurface,
                        if (isNightMode) resources.getColor(android.R.color.transparent)
                        else resources.getColor(android.R.color.transparent)
                    )
                    navigationBarColor = statusBarColor
                }
                else -> {
                    // Usa i colori statici
                    statusBarColor = if (isNightMode) {
                        resources.getColor(android.R.color.transparent)
                    } else {
                        resources.getColor(android.R.color.transparent)
                    }
                    navigationBarColor = statusBarColor
                }
            }

            // Configurazione della visibilità UI in base al tema
            decorView.systemUiVisibility = if (isNightMode) {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }

            originalStatusBarColor = statusBarColor
            originalNavigationBarColor = navigationBarColor

            // Imposta le icone della status bar scure o chiare in base al tema
            if (!isNightMode) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }
        }

// Imposta il tema appropriato
        when {
            isDynamicTheme -> {
                setTheme(R.style.Theme_YourApp)
                DynamicColors.applyToActivitiesIfAvailable(application)
            }
            else -> {
                setTheme(R.style.Theme_MyApp_MainActivity)
            }
        }

// Inizializza le views
        cardView = findViewById(R.id.myCardView)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        scrollView = findViewById(R.id.scrollView)
        gradientView = findViewById(R.id.gradientView)
        initialMargin = resources.getDimensionPixelSize(R.dimen.card_margin)
        actionBarTitle = findViewById(R.id.actionBarTitle)
        spacerView = findViewById(R.id.spacerView)

// Imposta il colore del titolo della action bar in base al tema
        actionBarTitle.setColorFilter(
            if (isDynamicTheme) {
                MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorPrimary,
                    ContextCompat.getColor(this, R.color.material_blue)
                )
            } else {
                ContextCompat.getColor(this, R.color.material_blue)
            }
        )

        setupViews()
        setupFadeEffect()


        // Inizializzazione delle variabili
        btnCamminare = findViewById(R.id.btnCamminare)
        btnGuidare = findViewById(R.id.btnGuidare)
        btnSedersi = findViewById(R.id.btnSedersi)
        btnBicicletta = findViewById(R.id.btnBicicletta)
        btnMoreActivities = findViewById(R.id.btnMoreActivities)
        btnAnalisiPrestazioni = findViewById(R.id.btnAnalisiPrestazioni)
        btnSocial = findViewById(R.id.btnSocial)
        fabInterrompi = findViewById(R.id.fab_interrompi)
        txtStato = findViewById(R.id.txtStato)
        txtPassi = findViewById(R.id.txtPassi)
        txtVolteSeduto = findViewById(R.id.txtVolteSeduto)
        bottomButtonsCard = findViewById(R.id.bottomButtonsCard)
        btnStorico = findViewById(R.id.btnStorico)
        btnSettings = findViewById(R.id.btnSettings)
        btnCorrere = findViewById(R.id.btnCorrere)
        txtDistanza = findViewById(R.id.txtDistanza)
        txtTempo = findViewById(R.id.txtTempo)
        txtVelocitaAttuale = findViewById(R.id.txtVelocitaAttuale)
        txtVelocitaMedia = findViewById(R.id.txtVelocitaMedia)
        txtPausa = findViewById(R.id.txtPausa)
        switchAutomaticActivity = findViewById(R.id.switchAutomaticActivity)
        layoutAutomaticActivityStats = findViewById(R.id.layoutAutomaticActivityStats)
        txtDistanzaUnit = findViewById(R.id.txtDistanzaUnit)
        txtTempoSeconds = findViewById(R.id.txtTempoSeconds)
        txtTempoSecondsUnit = findViewById(R.id.txtTempoSecondsUnit)
        txtTempoUnit = findViewById(R.id.txtTempoUnit)
        txtVelocitaMediaUnit = findViewById(R.id.txtVelocitaMediaUnit)

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        contatoriCard = findViewById(R.id.contatoriCard)
        cardContainer = findViewById(R.id.cardContainer)
        countdownText = findViewById(R.id.txtCountdown)
        statsLayout = findViewById(R.id.statsLayout)
        statsContent = findViewById(R.id.statsContent)

        layoutPassi = findViewById(R.id.layoutPassi)
        layoutVolteSeduto = findViewById(R.id.layoutVolteSeduto)
        layoutDistanza = findViewById(R.id.layoutDistanza)
        layoutTempo = findViewById(R.id.layoutTempo)
        layoutVelocitaAttuale = findViewById(R.id.layoutVelocitaAttuale)
        layoutPaceMedio = findViewById(R.id.layoutPaceMedio)
        layoutVelocitaMedia = findViewById(R.id.layoutVelocitaMedia)
        view1 = findViewById(R.id.View_1)
        view2 = findViewById(R.id.View_2)

        imgRecognizedActivity = findViewById(R.id.imgRecognizedActivity)
        txtRecognizedActivity = findViewById(R.id.txtRecognizedActivity)
        switchAutomaticActivity.setOnCheckedChangeListener { _, isChecked ->
            val isDynamicTheme = prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

            if (isChecked) {
                ActivityStateManager.isSwitchActive = true
                Log.d("MainActivity", "Riconoscimento automatico attività attivato")
                ActivityStateManager.isFirstDetectionAfterSwitch = true
                checkActivityRecognitionPermissionAndStartService1()

                // Imposta il background e i margini del RecordsLayout
                val bestRecordsLayout = findViewById<LinearLayout>(R.id.RecordsLayout)
                if (isDynamicTheme) {
                    bestRecordsLayout.background = GradientDrawable().apply {
                        cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
                        setColor(Color.TRANSPARENT)
                    }
                } else {
                    bestRecordsLayout.setBackgroundResource(R.drawable.transparent_background)
                }
                val layoutParams = bestRecordsLayout.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(0, 0, 0, 0)
                bestRecordsLayout.layoutParams = layoutParams

                animateBottoniPerRiconoscimento()

                layoutAutomaticActivityStats.visibility = View.GONE
                imgRecognizedActivity.visibility = View.VISIBLE

                // Imposta la dimensione piccola dell'icona
                val maxIconSize = resources.getDimensionPixelSize(R.dimen.icon_min_size)
                val littleIconSize = resources.getDimensionPixelSize(R.dimen.icon_little_size)
                val params = imgRecognizedActivity.layoutParams
                params.width = littleIconSize
                params.height = littleIconSize
                imgRecognizedActivity.layoutParams = params

                imgRecognizedActivity.alpha = 1f
                imgRecognizedActivity.setImageResource(R.drawable.psychology_alt)

                // Imposta il colore dell'icona in base al tema
                val iconColor = if (isDynamicTheme) {
                    MaterialColors.getColor(
                        this,
                        com.google.android.material.R.attr.colorPrimary,
                        ContextCompat.getColor(this, R.color.material_blue2)
                    )
                } else {
                    ContextCompat.getColor(this, R.color.material_blue2)
                }
                imgRecognizedActivity.setColorFilter(iconColor)

                animaBottoniInStatoThinking()
                fabInterrompi.visibility = View.VISIBLE
            } else if (!isChecked) {
                // Prima ferma l'animazione dei bottoni
                stopBottoniThinkingAnimation()

                animateSwitchDisappearance()

                ActivityStateManager.currentActivityType = DetectedActivity.UNKNOWN
                txtRecognizedActivity.visibility = View.GONE
                imgRecognizedActivity.visibility = View.GONE

                Log.d("MainActivity", "Riconoscimento automatico attività disattivato")
                ripristinaBottoni()
                resetMapSize()
            }
        }


        // Inizializzazione Floating Action Button
        fabAdd = findViewById(R.id.fab_add)
        fabWalk = findViewById(R.id.fab_walk)
        fabRun = findViewById(R.id.fab_run)
        fabDrive = findViewById(R.id.fab_drive)
        fabSit = findViewById(R.id.fab_sit)
        layoutWalk = findViewById(R.id.layout_walk)
        layoutRun = findViewById(R.id.layout_run)
        layoutSit = findViewById(R.id.layout_sit)
        layoutDrive = findViewById(R.id.layout_drive)
        overlayView = findViewById(R.id.overlayView)


        layoutWalk.visibility = View.GONE
        layoutRun.visibility = View.GONE
        layoutSit.visibility = View.GONE
        layoutDrive.visibility = View.GONE
        txtRecognizedActivity.visibility = View.GONE

        txtPaceMedio = findViewById(R.id.txtPaceMedio)
        txtPaceMedioUnit = findViewById(R.id.txtPaceMedioUnit)

        // Imposta il background e la dimensione maggiore solo per il bottone home
        val homeContainer = findViewById<FrameLayout>(R.id.homeContainer)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)

        if (colorMode == 0) {
            homeContainer.background =
                ContextCompat.getDrawable(this, R.drawable.custum_circle_main_back)
        }
        btnHome.layoutParams = FrameLayout.LayoutParams(38.dpToPx1(), 38.dpToPx1()).apply {
            gravity = Gravity.CENTER
        }

        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
        val buttonLayout = findViewById<LinearLayout>(R.id.buttonLayout)

        val mapOverlay = findViewById<View>(R.id.map_overlay)
        mapOverlay.setOnTouchListener { v, event ->
            Log.d("MapTouch", "Evento di tocco ricevuto sull'overlay")
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }

            }
            // Restituisci false per far "passare" l'evento al fragment della mappa
            false
        }

        createNotificationChannel()


        // Prepara le viste inizialmente fuori schermo o invisibili
        fabAdd.alpha = 0f
        fabAdd.scaleX = 0f
        fabAdd.scaleY = 0f

        buttonLayout.post {
            // Animazione esistente dei bottoni
            buttonLayout.translationX = -buttonLayout.width.toFloat()
            buttonLayout.animate()
                .translationX(0f)
                .setDuration(800)
                .setInterpolator(DecelerateInterpolator())
                .start()


            // Animazione del FAB
            fabAdd.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }


        fabAdd.setOnClickListener {
            if (!isOverlayVisible) {


                overlayView.visibility = View.VISIBLE
                layoutWalk.visibility = View.VISIBLE
                layoutRun.visibility = View.VISIBLE
                layoutSit.visibility = View.VISIBLE
                layoutDrive.visibility = View.VISIBLE
                window.statusBarColor = Color.parseColor("#D8000000")

                // Imposta il colore della navigation bar (Android 5.0+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.navigationBarColor = Color.parseColor("#D8000000")
                }

                fabAdd.setImageResource(android.R.drawable.ic_delete)

                // Sposta i FAB secondari verso l'alto
                layoutWalk.animate().translationY(-140f).alpha(1f).setDuration(200).start()
                layoutRun.animate().translationY(-210f).alpha(1f).setDuration(200).start()
                layoutDrive.animate().translationY(-280f).alpha(1f).setDuration(200).start()
                layoutSit.animate().translationY(-350f).alpha(1f).setDuration(200).start()

                isOverlayVisible = true
            } else {
                layoutWalk.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                    layoutWalk.visibility = View.GONE
                }.start()

                layoutRun.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                    layoutRun.visibility = View.GONE
                }.start()
                layoutDrive.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                    layoutDrive.visibility = View.GONE
                }.start()
                layoutSit.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                    layoutSit.visibility = View.GONE
                }
                overlayView.visibility = View.GONE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    overlayView.setRenderEffect(null)
                }
                hideFABs()
                isOverlayVisible = false
                // Ripristina i colori originali salvati all'avvio
                window.statusBarColor = originalStatusBarColor
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.navigationBarColor = originalNavigationBarColor
                }
            }
        }

        overlayView.setOnClickListener {
            if (isOverlayVisible) {
                // Nascondi i FAB con l'animazione
                layoutWalk.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                    layoutWalk.visibility = View.GONE
                }.start()

                layoutRun.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                    layoutRun.visibility = View.GONE
                }.start()
                layoutDrive.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                    layoutDrive.visibility = View.GONE
                }.start()

                layoutSit.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
                    layoutSit.visibility = View.GONE
                }.start()

                // Nascondi l'overlayView
                overlayView.visibility = View.GONE

                // Ripristina i colori originali salvati all'avvio
                window.statusBarColor = originalStatusBarColor
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.navigationBarColor = originalNavigationBarColor
                }
                // Rimuovi l'effetto di sfocatura
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    overlayView.setRenderEffect(null)
                }
                hideFABs()
                isOverlayVisible = false
            }
        }

        fabInterrompi.setOnClickListener {
            Log.d("MainActivity", "Button Interrompi cliccato")
            if (switchAutomaticActivity.isChecked) {
                findViewById<TextView>(R.id.txtAutomaticStatsPrimary).visibility = View.GONE
                findViewById<TextView>(R.id.txtAutomaticStatsSecondary).visibility = View.GONE
                findViewById<TextView>(R.id.txtAutomaticStatsTertiary).visibility = View.GONE
                stopBottoniThinkingAnimation()
                animateSwitchDisappearance()
                ripristinaBottoni()
            } else if (isCountdownRunning) {
                // Se siamo nel countdown, interrompiamo immediatamente
                countdownTimer?.cancel()
                countdownTimer = null
                isCountdownRunning = false
                countdownText.visibility = View.GONE
                statsLayout.visibility = View.GONE
                interrompiAttività()
                ripristinaBottoni()
                resetMapSize()
                resetAllViews()
                enableHorizontalScroll()
                smoothScrollToTop()
            } else {
                showStopConfirmation()
            }
        }

        if (savedInstanceState != null) {
            attivitàCorrente = savedInstanceState.getString("attivitàCorrente")
            oraInizio = savedInstanceState.getLong("oraInizio")
            volteSeduto = savedInstanceState.getInt("volteSeduto")

            if (attivitàCorrente != null) {
                txtStato.text = "Attività in corso: $attivitàCorrente"
                fabInterrompi.visibility = View.VISIBLE

                when (attivitàCorrente) {
                    "Camminare" -> {
                        txtPassi.visibility = View.VISIBLE
                        txtPassi.text = "0"
                        txtVolteSeduto.visibility = View.GONE
                        checkActivityRecognitionPermission("Camminare")
                    }

                    "Sedersi" -> {
                        txtVolteSeduto.visibility = View.VISIBLE
                        txtVolteSeduto.text = "0"
                        txtPassi.visibility = View.GONE
                        startSedutaTrackingService(isManualStart = true)
                    }

                    "Corsa" -> {
                        txtPassi.visibility = View.GONE
                        txtVolteSeduto.visibility = View.GONE
                        txtDistanza.visibility = View.VISIBLE
                        txtTempo.visibility = View.VISIBLE
                        startRunTrackingService(isManualStart = true)
                    }

                    "Guidare" -> {
                        txtPassi.visibility = View.GONE
                        txtVolteSeduto.visibility = View.GONE
                        txtDistanza.visibility = View.VISIBLE
                        txtTempo.visibility = View.VISIBLE
                        checkActivityRecognitionPermission("Guidare")
                    }

                    else -> {
                        txtPassi.visibility = View.GONE
                        txtVolteSeduto.visibility = View.GONE
                    }
                }
            }
        }

        progressPassi = findViewById(R.id.progressPassi)
        progressCorsa = findViewById(R.id.progressCorsa)
        progressSedute = findViewById(R.id.progressSedute)

        txtProgressPassiLabel = findViewById(R.id.txtProgressPassiLabel)
        txtProgressCorsaLabel = findViewById(R.id.txtProgressCorsaLabel)
        txtProgressSeduteLabel = findViewById(R.id.txtProgressSeduteLabel)

        listOf(progressPassi, progressCorsa, progressSedute).forEach { indicator ->
            indicator.isIndeterminate = false
            indicator.progress = 0
            indicator.max = 100
        }

        // Imposta il colore predefinito della toolbar all'avvio
        val defaultColor = getDefaultColor()
        actionBarTitle.setColorFilter(defaultColor)


        // Per il Progress dei Passi
        val passiAttuali = progressPassi.progress.toString()
        val obiettivoPassi = goalSteps.toString()

        // Determina se il tema dinamico è attivo

        // Funzione helper per ottenere il colore appropriato
        fun getThemedColor(staticColor: Int, dynamicColorAttr: Int): Int {
            return if (isDynamicTheme) {
                MaterialColors.getColor(this, dynamicColorAttr,
                    ContextCompat.getColor(this, staticColor))
            } else {
                ContextCompat.getColor(this, staticColor)
            }
        }

// Colori per i testi
        val currentColor = getThemedColor(
            R.color.colore_fab1,
            com.google.android.material.R.attr.colorPrimary
        )
        val slashColor = getThemedColor(
            R.color.background_arancione_dark,
            com.google.android.material.R.attr.colorSurfaceVariant
        )
        val goalColor = getThemedColor(
            R.color.command_center_color1,
            com.google.android.material.R.attr.colorSecondary
        )

// Passi
        val spannablePassi = SpanUtils.createColoredString(
            current = passiAttuali,
            currentColor = currentColor,
            slashColor = slashColor,
            goal = obiettivoPassi,
            goalColor = goalColor
        )
        txtProgressPassiLabel.text = spannablePassi

// Corsa
        val distanzaAttuale = if (progressCorsa.progress < 1000) {
            "${progressCorsa.progress}m"
        } else {
            String.format("%.2fkm", progressCorsa.progress / 1000f)
        }
        val obiettivoDistanza = String.format("%.0fkm", goalRunningDistance / 1000f)
        val spannableCorsa = SpanUtils.createColoredString(
            current = distanzaAttuale,
            currentColor = currentColor,
            slashColor = slashColor,
            goal = obiettivoDistanza,
            goalColor = goalColor
        )
        txtProgressCorsaLabel.text = spannableCorsa

// Sedute
        val tempoSeduto = getSessionSittingTime()
        val ore = TimeUnit.MILLISECONDS.toHours(tempoSeduto)
        val minuti = TimeUnit.MILLISECONDS.toMinutes(tempoSeduto) % 60
        val tempoAttuale = "${ore}h ${minuti}m"

        val oreObiettivo = TimeUnit.MILLISECONDS.toHours(goalMaxSittingTime)
        val minutiObiettivo = TimeUnit.MILLISECONDS.toMinutes(goalMaxSittingTime) % 60
        val tempoObiettivo = "${oreObiettivo}h"

        val spannableSedute = SpanUtils.createColoredString(
            current = tempoAttuale,
            currentColor = currentColor,
            slashColor = slashColor,
            goal = tempoObiettivo,
            goalColor = goalColor
        )
        txtProgressSeduteLabel.text = spannableSedute

        // Inizializza la mappa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_dynamic) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        attivitàDaAvviare = savedInstanceState?.getString("attivitàDaAvviare")

        horizontalScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            val currentTime = System.currentTimeMillis()
            if (Math.abs(scrollX - lastScrollX) > SCROLL_THRESHOLD &&
                (currentTime - lastFeedbackTime) > FEEDBACK_DELAY
            ) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                lastScrollX = scrollX
                lastFeedbackTime = currentTime
            }
            val maxScroll = buttonLayout.width - horizontalScrollView.width


            when {
                scrollX == 0 -> {
                    // All'inizio dello scroll
                    ObjectAnimator.ofFloat(view2, "alpha", 1f, 0f).apply {
                        duration = 500
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                view2.visibility = View.GONE
                            }
                        })
                        start()
                    }

                    view1.visibility = View.VISIBLE
                    view1.alpha = 1f
                }
                scrollX >= maxScroll -> {

                    ObjectAnimator.ofFloat(view1, "alpha", 1f, 0f).apply {
                        duration = 500
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                view1.visibility = View.GONE
                            }
                        })
                        start()
                    }

                    view2.visibility = View.VISIBLE
                    view2.alpha = 1f
                }
                else -> {
                    // Durante lo scroll
                    view1.visibility = View.VISIBLE
                    view1.alpha = 1f
                    view2.visibility = View.VISIBLE
                    view2.alpha = 1f
                }
            }

        }

        setupScrollIndicator()

        // All'avvio dell'app, controlla se il servizio è già in esecuzione
        switchAutomaticActivity.isChecked = ActivityTrackingApplication.isServiceRunning
        Log.d("MainActivity", "onCreate - Stato servizio: ${ActivityTrackingApplication.isServiceRunning}")
        Log.d("MainActivity", "onCreate - Switch impostato a: ${switchAutomaticActivity.isChecked}")

        ActivityStateManager.isSwitchActive = ActivityTrackingApplication.isServiceRunning
        Log.d("MainActivity", "onCreate - ActivityStateManager.isSwitchActive impostato a: ${ActivityStateManager.isSwitchActive}")


        db = AppDatabase.getDatabase(this)
        Log.d("MainActivity", "Database inizializzato: $db")

        walkingChart = findViewById(R.id.walkingChart)
        runningChart = findViewById(R.id.runningChart)
        sittingChart = findViewById(R.id.sittingChart)
        chartsManager = WeeklyChartsManager(this, db)

        setupCharts()

        btnCamminare.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (attivitàCorrente == null && !isCountdownRunning) {
                Log.d("MainActivity", "Button Camminare cliccato")
                attivitàDaAvviare = "Camminare"
                resetAllButtons()
                activateButton(btnCamminare, R.color.colorCamminare)
                centerButtonInScrollView(btnCamminare)
                startCountdownAnimation("Camminare")
                smoothScrollToBottomWithDelay()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "   Continua a camminare cosi!",
                    Snackbar.LENGTH_SHORT
                )
                    .setAction("", null)
                    .show()
            }
        }

        val uiRefs = StatsManager.StatisticsUIComponents(
            // Contatori compatti
            txtPassiGiornalieriCompact = findViewById(R.id.txtPassiGiornalieriCompact),
            txtSeduteGiornalieriCompact = findViewById(R.id.txtSeduteGiornaliereCompact),
            txtCorsaGiornaliereCompact = findViewById(R.id.txtCorsaGiornaliereCompact),

            // Statistiche Passi
            txtPassiGiornalieri = findViewById(R.id.txtPassiGiornalieri),
            txtTempoCamminataGiornaliere = findViewById(R.id.txtTempoCamminataGiornaliere),
            txtPassiSettimanali = findViewById(R.id.txtPassiSettimanali),
            txtTempoCamminataSettimanali = findViewById(R.id.txtTempoCamminataSettimanali),

            // Statistiche Sedute
            txtNumeroSeduteGiornaliere = findViewById(R.id.txtNumeroSeduteGiornaliere),
            txtTempoSeduteGiornaliere = findViewById(R.id.txtTempoSeduteGiornaliere),
            txtNumeroSeduteSettimanali = findViewById(R.id.txtNumeroSeduteSettimanali),
            txtTempoSeduteSettimanali = findViewById(R.id.txtTempoSeduteSettimanali),
            txtSeduteGiornaliereCompact = findViewById(R.id.txtSeduteGiornaliereCompact),
            // Statistiche Corsa
            txtDistanzaCorsaSettimanale = findViewById(R.id.txtDistanzaCorsaSettimanale),
            txtTempoCorsaSettimanale = findViewById(R.id.txtTempoCorsaSettimanale),

            // Statistiche Guida
            txtDistanzaGuidaSettimanale = findViewById(R.id.txtDistanzaGuidaSettimanale),
            txtTempoGuidaSettimanale = findViewById(R.id.txtTempoGuidaSettimanale),


            // MVP Records
            bestRunDate = findViewById(R.id.bestRunDate),
            bestRunValue = findViewById(R.id.bestRunValue),
            bestWalkDate = findViewById(R.id.bestWalkDate),
            bestWalkValue = findViewById(R.id.bestWalkValue),
            bestDriveDate = findViewById(R.id.bestDriveDate),
            bestDriveValue = findViewById(R.id.bestDriveValue)
        )

        statsManager = StatsManager(this, db, uiRefs)
        statsManager = StatsManager.getInstance(this, db, uiRefs)

        statsManager.aggiornaStatisticheGiornaliere()
        statsManager.aggiornaStatisticheSettimanali()
        setupGoalsProgress()
        updateActivityStats()

        setupBestRecords()

        fun Long.formatDuration(): String {
            val hours = TimeUnit.MILLISECONDS.toHours(this)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60

            return when {
                hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }

        activityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Non processare gli aggiornamenti se lo switch non è attivo
                if (!ActivityStateManager.isSwitchActive) {
                    return
                }

                val activityType = intent.getIntExtra(EXTRA_ACTIVITY_TYPE, DetectedActivity.UNKNOWN)
                val activityName = activityTypeToString(activityType)

                // Aggiorna immediatamente i valori con quelli iniziali
                val txtPrimary = findViewById<TextView>(R.id.txtAutomaticStatsPrimary)
                val txtSecondary = findViewById<TextView>(R.id.txtAutomaticStatsSecondary)
                val txtTertiary = findViewById<TextView>(R.id.txtAutomaticStatsTertiary)

                txtPrimary.text = intent.getStringExtra("initial_primary") ?: ""
                txtSecondary.text = intent.getStringExtra("initial_secondary") ?: ""
                txtTertiary.text = intent.getStringExtra("initial_tertiary") ?: ""

                if (activityType == DetectedActivity.UNKNOWN) {
                    txtRecognizedActivity.visibility = View.VISIBLE
                    txtRecognizedActivity.alpha = 1f
                    imgRecognizedActivity.alpha = 1f
                    txtRecognizedActivity.text = "sto pensando..."
                    imgRecognizedActivity.visibility = View.VISIBLE
                    imgRecognizedActivity.setImageResource(R.drawable.psychology_alt)
                    imgRecognizedActivity.setColorFilter(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.material_blue2
                        )
                    )
                } else {
                    txtRecognizedActivity.visibility = View.VISIBLE
                    txtRecognizedActivity.alpha = 1f
                    // Quando viene rilevata un'attività specifica
                    txtRecognizedActivity.text = "$activityName"
                    imgRecognizedActivity.visibility = View.VISIBLE

                    val (activityIcon, activityColorResId) = when (activityType) {
                        DetectedActivity.WALKING, DetectedActivity.ON_FOOT ->
                            Pair(R.drawable.footprint_24px, R.color.colorCamminare)

                        DetectedActivity.RUNNING ->
                            Pair(R.drawable.directions_run_24px, R.color.colorCorrere)

                        DetectedActivity.IN_VEHICLE, DetectedActivity.ON_BICYCLE ->
                            Pair(R.drawable.directions_car, R.color.colorGuidare)

                        DetectedActivity.STILL->
                            Pair(R.drawable.man_24dp, R.color.colorSedersi)

                        DetectedActivity.TILTING ->
                            Pair(R.drawable.seat_24px, R.color.colorSedersi)

                        else ->
                            Pair(R.drawable.psychology_alt, R.color.material_blue2)
                    }

                    imgRecognizedActivity.setImageResource(activityIcon)
                    val color = ContextCompat.getColor(this@MainActivity, activityColorResId)
                    imgRecognizedActivity.setColorFilter(
                        color,
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    centraBottoneAttivitaRilevata(activityType)

                    val confidence = intent.getIntExtra(EXTRA_ACTIVITY_CONFIDENCE, 0)

                    // Trova le TextView per le statistiche automatiche
                    val layoutAutomaticActivityStats =
                        findViewById<LinearLayout>(R.id.layoutAutomaticActivityStats)
                    val txtPrimary = findViewById<TextView>(R.id.txtAutomaticStatsPrimary)
                    val txtSecondary = findViewById<TextView>(R.id.txtAutomaticStatsSecondary)
                    val txtTertiary = findViewById<TextView>(R.id.txtAutomaticStatsTertiary)

                    // Colore di default
                    val activityColor = when (activityType) {
                        DetectedActivity.WALKING, DetectedActivity.ON_FOOT ->
                            ContextCompat.getColor(this@MainActivity, R.color.colorCamminare)

                        DetectedActivity.RUNNING ->
                            ContextCompat.getColor(this@MainActivity, R.color.colorCorrere)

                        DetectedActivity.IN_VEHICLE ->
                            ContextCompat.getColor(this@MainActivity, R.color.colorGuidare)

                        DetectedActivity.ON_BICYCLE ->
                            ContextCompat.getColor(this@MainActivity, R.color.colorBici)

                        DetectedActivity.STILL, DetectedActivity.TILTING ->
                            ContextCompat.getColor(this@MainActivity, R.color.colorSedersi)

                        else -> ContextCompat.getColor(this@MainActivity, R.color.material_blue)
                    }

                    // Colora i testi
                    txtPrimary.setTextColor(activityColor)
                    txtSecondary.setTextColor(activityColor)
                    txtTertiary.setTextColor(activityColor)

                    layoutPassi.visibility = View.GONE
                    layoutDistanza.visibility = View.GONE
                    layoutTempo.visibility = View.GONE
                    layoutVelocitaAttuale.visibility = View.GONE
                    layoutPaceMedio.visibility = View.GONE
                    layoutVelocitaMedia.visibility = View.GONE
                    layoutVolteSeduto.visibility = View.GONE

                    when (activityType) {
                        DetectedActivity.WALKING, DetectedActivity.ON_FOOT -> {
                            if (switchAutomaticActivity.isChecked) {
                                imgRecognizedActivity.visibility = View.VISIBLE
                                imgRecognizedActivity.setImageResource(R.drawable.footprint_24px)
                                txtRecognizedActivity.text = "$activityName"
                                imgRecognizedActivity.setColorFilter(activityColor)

                                startStepTrackingService(isManualStart = false)
                            }
                        }

                        DetectedActivity.RUNNING -> {
                            if (switchAutomaticActivity.isChecked) {
                                imgRecognizedActivity.visibility = View.VISIBLE
                                imgRecognizedActivity.setImageResource(R.drawable.directions_run_24px)
                                txtRecognizedActivity.text = "$activityName"
                                imgRecognizedActivity.setColorFilter(activityColor)

                                checkLocationPermissionAndStartService("Corsa")
                            }
                        }

                        DetectedActivity.IN_VEHICLE -> {
                            if (switchAutomaticActivity.isChecked) {
                                imgRecognizedActivity.visibility = View.VISIBLE
                                imgRecognizedActivity.setImageResource(R.drawable.directions_car)
                                txtRecognizedActivity.text = "$activityName"
                                imgRecognizedActivity.setColorFilter(activityColor)
                                checkLocationPermissionAndStartService("Guidare")
                            }
                        }

                        DetectedActivity.ON_BICYCLE -> {
                            if (switchAutomaticActivity.isChecked) {
                                imgRecognizedActivity.visibility = View.VISIBLE
                                imgRecognizedActivity.setImageResource(R.drawable.directions_bike)
                                txtRecognizedActivity.text = "$activityName ($confidence%)"
                                imgRecognizedActivity.setColorFilter(activityColor)
                                checkLocationPermissionAndStartService("Bicicletta")
                            }
                        }

                        DetectedActivity.STILL, DetectedActivity.TILTING -> {

                            if (switchAutomaticActivity.isChecked) {
                                imgRecognizedActivity.visibility = View.VISIBLE
                                txtRecognizedActivity.text = "$activityName"
                                imgRecognizedActivity.setColorFilter(activityColor)
                                startSedutaTrackingService(isManualStart = false)
                            }
                        }
                    }
                }
            }
        }


        sedutaCountReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == SedutaTrackingService.ACTION_SEDUTA_COUNT_UPDATE) {
                    val count = intent.getIntExtra("sedutaCount", 0)
                    val durata = intent.getLongExtra("sedutaDurata", 0L)

                    // Log dettagliato sul tempo
                    val hours = TimeUnit.MILLISECONDS.toHours(durata)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(durata) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(durata) % 60

                    Log.d(
                        "MainActivity",
                        "Dati seduta ricevuti: " +
                                "Volte Seduto=$count, " +
                                "Durata totale=${durata}ms, " +
                                "Formato leggibile: ${hours}h ${minutes}m ${seconds}s"
                    )

                    statsManager.aggiornaStatisticheGiornaliere()
                    updateVolteSeduto(count)

                    if (switchAutomaticActivity.isChecked &&
                        (ActivityStateManager.currentActivityType == DetectedActivity.STILL)) {

                        val txtPrimary = findViewById<TextView>(R.id.txtAutomaticStatsPrimary)
                        val txtSecondary = findViewById<TextView>(R.id.txtAutomaticStatsSecondary)
                        val txtTertiary = findViewById<TextView>(R.id.txtAutomaticStatsTertiary)
                        val layoutAutomaticActivityStats = findViewById<LinearLayout>(R.id.layoutAutomaticActivityStats)

                        layoutAutomaticActivityStats.visibility = View.GONE
                        txtPrimary.text = "   Volte Seduto"
                        txtSecondary.text = "$count"
                        txtTertiary.text = ""

                    }
                }
            }
        }

        sedutaCountReceiver?.let { receiver ->
            LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver,
                IntentFilter().apply {
                    addAction(SedutaTrackingService.ACTION_SEDUTA_COUNT_UPDATE)
                    addAction("com.example.personalphysicaltracker.ACTIVITY_NOT_SAVED")
                }
            )
        }



        // Inizializza il BroadcastReceiver per i passi
        stepCountReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != StepTrackingService.ACTION_STEP_COUNT_UPDATE) {
                    Log.w("StepReceiver", "Intent non riconosciuto: ${intent?.action}")
                    return
                }

                // Estrai i dati dall'intent
                val sensorStepCount = intent.getIntExtra(StepTrackingService.EXTRA_STEP_COUNT, 0)
                val distance = intent.getFloatExtra(StepTrackingService.EXTRA_DISTANCE, 0f)
                val elapsedTime = intent.getLongExtra(StepTrackingService.EXTRA_ELAPSED_TIME, 0L)

                Log.d("StepReceiver", "Passi totali: $sensorStepCount")

                // Aggiorna le statistiche usando i passi totali
                statsManager.addStepsForProgress(sensorStepCount)
                updateStepCount(sensorStepCount, distance, elapsedTime)
                statsManager.updateCurrentSteps(sensorStepCount)
                statsManager.setAttivitàCorrente("Camminare", oraInizio)
                updateStepsProgress()

                if (switchAutomaticActivity.isChecked &&
                    (ActivityStateManager.currentActivityType == DetectedActivity.WALKING ||
                            ActivityStateManager.currentActivityType == DetectedActivity.ON_FOOT)) {

                    runOnUiThread {
                        try {
                            val txtPrimary = findViewById<TextView>(R.id.txtAutomaticStatsPrimary)
                            val txtSecondary = findViewById<TextView>(R.id.txtAutomaticStatsSecondary)
                            val txtTertiary = findViewById<TextView>(R.id.txtAutomaticStatsTertiary)
                            val layoutAutomaticActivityStats = findViewById<LinearLayout>(R.id.layoutAutomaticActivityStats)
                            layoutAutomaticActivityStats.visibility = View.VISIBLE

                            // Aggiorna UI con i passi totali
                            txtPrimary?.text = "$sensorStepCount passi"
                            txtSecondary?.text = if (distance < 1000) {
                                "${String.format("%.1f", distance)} m"
                            } else {
                                "${String.format("%.1f", distance / 1000)} km"
                            }
                            txtTertiary?.text = elapsedTime.formatDuration()


                            Log.d("StepReceiver", "UI aggiornata con passi totali: $sensorStepCount")
                        } catch (e: Exception) {
                            Log.e("StepReceiver", "Errore durante l'aggiornamento dell'UI", e)
                        }
                    }
                }
            }
        }

        driveReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == CarTrackingService.ACTION_CAR_UPDATE) {
                    val distanza = intent.getIntExtra(CarTrackingService.EXTRA_DISTANCE, 0)
                    val velocita = intent.getFloatExtra(CarTrackingService.EXTRA_SPEED, 0f)
                    val durata = intent.getLongExtra(CarTrackingService.EXTRA_DURATION, 0L)
                    val isBicycle = intent.getBooleanExtra("isBicycle", false)

                    // Se il servizio restituisce un valore, calcola il delta
                    val deltaDistance = if (previousSensorDistance == 0f) {
                        distanza.toFloat()
                    } else {
                        distanza.toFloat() - previousSensorDistance
                    }
                    previousSensorDistance = distanza.toFloat()

                    // Aggiungi il delta alla distanza cumulativa
                    statsManager.addRunningDistance(deltaDistance)
                    Log.d("MainActivity", "Nuova distanza calcolata (delta): $deltaDistance")

                    // Aggiorna eventuali dati interni
                    updateVehicleData(distanza, velocita, durata, isAuto = true)
                    statsManager.updateCurrentDistance(distanza.toFloat())
                    statsManager.setAttivitàCorrente("Guidare", oraInizio)
                    statsManager.aggiornaStatisticheSettimanali()


                }

                if (intent?.action == CarTrackingService.ACTION_CAR_UPDATE &&
                    switchAutomaticActivity.isChecked &&
                    (ActivityStateManager.currentActivityType == DetectedActivity.IN_VEHICLE)) {

                    val distanza = intent.getIntExtra(CarTrackingService.EXTRA_DISTANCE, 0)
                    val velocita = intent.getFloatExtra(CarTrackingService.EXTRA_SPEED, 0f)
                    val durata = intent.getLongExtra(CarTrackingService.EXTRA_DURATION, 0L)

                    runOnUiThread {
                        val txtPrimary = findViewById<TextView>(R.id.txtAutomaticStatsPrimary)
                        val txtSecondary = findViewById<TextView>(R.id.txtAutomaticStatsSecondary)
                        val txtTertiary = findViewById<TextView>(R.id.txtAutomaticStatsTertiary)
                        val layoutAutomaticActivityStats = findViewById<LinearLayout>(R.id.layoutAutomaticActivityStats)

                        // Distanza totale
                        txtPrimary.text = if (distanza < 1000) {
                            "${distanza} m"
                        } else {
                            "${String.format("%.1f", distanza / 1000.0)} km"
                        }

                        // Velocità totale
                        txtSecondary.text = "${String.format("%.1f", velocita)} km/h"

                        // Durata totale
                        txtTertiary.text = durata.formatDuration()

                        layoutAutomaticActivityStats.visibility = View.VISIBLE
                    }
                }
            }
        }

        bikeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BikeTrackingService.ACTION_BIKE_UPDATE) {
                    val distanza = intent.getIntExtra(BikeTrackingService.EXTRA_DISTANCE, 0)
                    val velocita = intent.getFloatExtra(BikeTrackingService.EXTRA_SPEED, 0f)
                    val durata = intent.getLongExtra(BikeTrackingService.EXTRA_DURATION, 0L)

                    // Se il servizio restituisce un valore assoluto, calcola il delta
                    val deltaDistance = if (previousSensorDistance == 0f) {
                        distanza.toFloat()
                    } else {
                        distanza.toFloat() - previousSensorDistance
                    }
                    previousSensorDistance = distanza.toFloat()

                    // Aggiungi il delta alla distanza cumulativa
                    statsManager.addRunningDistance(deltaDistance)
                    Log.d("MainActivity", "Nuova distanza calcolata (delta): $deltaDistance")

                    // Aggiorna eventuali dati interni
                    updateVehicleData(distanza, velocita, durata, isAuto = false)
                    statsManager.updateCurrentDistance(distanza.toFloat())
                    statsManager.setAttivitàCorrente("Bicicletta", oraInizio)
                    statsManager.aggiornaStatisticheSettimanali()

                }

                if (intent?.action == BikeTrackingService.ACTION_BIKE_UPDATE &&
                    switchAutomaticActivity.isChecked &&
                    (ActivityStateManager.currentActivityType == DetectedActivity.ON_BICYCLE)) {

                    val distanza = intent.getIntExtra(BikeTrackingService.EXTRA_DISTANCE, 0)
                    val velocita = intent.getFloatExtra(BikeTrackingService.EXTRA_SPEED, 0f)
                    val durata = intent.getLongExtra(BikeTrackingService.EXTRA_DURATION, 0L)

                    runOnUiThread {
                        val txtPrimary = findViewById<TextView>(R.id.txtAutomaticStatsPrimary)
                        val txtSecondary = findViewById<TextView>(R.id.txtAutomaticStatsSecondary)
                        val txtTertiary = findViewById<TextView>(R.id.txtAutomaticStatsTertiary)
                        val layoutAutomaticActivityStats = findViewById<LinearLayout>(R.id.layoutAutomaticActivityStats)

                        // Distanza totale
                        txtPrimary.text = if (distanza < 1000) {
                            "${distanza} m"
                        } else {
                            "${String.format("%.1f", distanza / 1000.0)} km"
                        }

                        // Velocità totale
                        txtSecondary.text = "${String.format("%.1f", velocita)} km/h"

                        // Durata totale
                        txtTertiary.text = durata.formatDuration()

                        layoutAutomaticActivityStats.visibility = View.VISIBLE
                    }
                }
            }
        }


        // Inizializza il BroadcastReceiver per la corsa
        runReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == RunTrackingService.ACTION_RUN_UPDATE) {
                    val distanza = intent.getFloatExtra(RunTrackingService.EXTRA_DISTANZA, 0f)
                    val tempo = intent.getLongExtra(RunTrackingService.EXTRA_TEMPO_CORSA, 0L)
                    val velocitaAttuale = intent.getFloatExtra(RunTrackingService.EXTRA_VELOCITA_ATTUALE, 0f)
                    val velocitaMedia = intent.getFloatExtra(RunTrackingService.EXTRA_VELOCITA_MEDIA, 0f)
                    val paceMedio = intent.getFloatExtra(RunTrackingService.EXTRA_PACE_MEDIO, 0f)
                    val isPaused = intent.getBooleanExtra(RunTrackingService.EXTRA_IS_PAUSED, false)

                    // Se il servizio restituisce un valore assoluto, calcola il delta
                    val deltaDistance = if (previousSensorDistance == 0f) {
                        distanza
                    } else {
                        distanza - previousSensorDistance
                    }
                    previousSensorDistance = distanza

                    // Aggiungi il delta alla distanza cumulativa
                    statsManager.addRunningDistance(deltaDistance)
                    Log.d("MainActivity", "Nuova distanza calcolata (delta): $deltaDistance")

                    // Aggiorna eventuali dati interni (ad es. grafici o statistiche)
                    updateRunData(distanza, tempo, velocitaAttuale, velocitaMedia, paceMedio, isPaused)
                    statsManager.updateCurrentDistance(distanza)
                    statsManager.setAttivitàCorrente("Corsa", oraInizio)
                    statsManager.aggiornaStatisticheSettimanali()

                    // Aggiorna il progress bar della corsa
                    updateRunningProgress()
                }
                if (intent?.action == RunTrackingService.ACTION_RUN_UPDATE &&
                    switchAutomaticActivity.isChecked &&
                    (ActivityStateManager.currentActivityType == DetectedActivity.RUNNING)) {

                    val distanza = intent.getFloatExtra(RunTrackingService.EXTRA_DISTANZA, 0f)
                    val velocitaMedia = intent.getFloatExtra(RunTrackingService.EXTRA_VELOCITA_MEDIA, 0f)
                    val tempoCorsa = intent.getLongExtra(RunTrackingService.EXTRA_TEMPO_CORSA, 0L)

                    runOnUiThread {
                        val txtPrimary = findViewById<TextView>(R.id.txtAutomaticStatsPrimary)
                        val txtSecondary = findViewById<TextView>(R.id.txtAutomaticStatsSecondary)
                        val txtTertiary = findViewById<TextView>(R.id.txtAutomaticStatsTertiary)
                        val layoutAutomaticActivityStats = findViewById<LinearLayout>(R.id.layoutAutomaticActivityStats)

                        // Distanza totale
                        txtPrimary.text = if (distanza < 1000) {
                            "${String.format("%.1f", distanza)} m"
                        } else {
                            "${String.format("%.1f", distanza / 1000)} km"
                        }

                        // Velocità media
                        txtSecondary.text = "${String.format("%.1f", velocitaMedia)} km/h"

                        // Tempo totale
                        txtTertiary.text = tempoCorsa.formatDuration()

                        layoutAutomaticActivityStats.visibility = View.VISIBLE
                    }
                }
            }
        }


        pauseReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.personalphysicaltracker.ACTION_ACTIVITY_PAUSE_STATE") {
                    val paused = intent.getBooleanExtra("isPaused", false)
                    if (paused) {
                        txtStato.text = "Attività in pausa: $attivitàCorrente"
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Attività in pausa automaticamente",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        txtStato.text = "Attività in corso: $attivitàCorrente"
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Attività ripresa",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        runLocationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val latitude = intent?.getDoubleExtra(RunTrackingService.EXTRA_LATITUDE, 0.0)
                val longitude = intent?.getDoubleExtra(RunTrackingService.EXTRA_LONGITUDE, 0.0)
                if (latitude != null && longitude != null) {
                    val latLng = LatLng(latitude, longitude)
                    runPathCoordinates.add(latLng)
                    updateMapPath(runPathCoordinates)
                }
            }
        }

        btnGuidare.setOnClickListener {
            try {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                if (attivitàCorrente == null && !isCountdownRunning) {
                    attivitàDaAvviare = "Guidare"
                    resetAllButtons()
                    activateButton(btnGuidare, R.color.colorGuidare)
                    Log.d("MainActivity", "Button Guidare cliccato")
                    centerButtonInScrollView(btnGuidare)
                    txtStato.visibility = View.GONE
                    startCountdownAnimation("Guidare")
                    smoothScrollToBottomWithDelay()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "     Guida guida e guida!",
                        Snackbar.LENGTH_SHORT
                    ).setAction("", null).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Errore nell'avvio dell'attività Guidare", e)
                Toast.makeText(this, "Errore nell'avvio dell'attività", Toast.LENGTH_SHORT).show()
            }
        }

        btnBicicletta.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (attivitàCorrente == null && !isCountdownRunning) {
                attivitàDaAvviare = "Bicicletta"
                resetAllButtons()
                activateButton(btnBicicletta, R.color.colorBici)
                Log.d("MainActivity", "Button Bicicletta cliccato")
                centerButtonInScrollView(btnBicicletta)
                txtStato.visibility = View.GONE
                startCountdownAnimation("Bicicletta")
                smoothScrollToBottomWithDelay()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "    Vai pedala!",
                    Snackbar.LENGTH_SHORT
                ).setAction("", null).show()
            }
        }

        btnSedersi.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (attivitàCorrente == null && !isCountdownRunning) {
                attivitàDaAvviare = "Sedersi"
                resetAllButtons()
                activateButton(btnSedersi, R.color.colorSedersi)
                Log.d("MainActivity", "Button Sedersi cliccato")
                centerButtonInScrollView(btnSedersi)
                volteSeduto = 0
                txtVolteSeduto.visibility = View.VISIBLE
                txtVolteSeduto.text = "0 volte"
                startCountdownAnimation("Sedersi")
                smoothScrollToBottomWithDelay()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "    Svegliaaa",
                    Snackbar.LENGTH_SHORT
                )
                    .setAction("", null)
                    .show()
            }
        }

        // Listener per il pulsante "Più Attività" (Strava-like)
        btnMoreActivities.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Button Più Attività cliccato")
            
            // Apri la nuova Activity per la selezione delle attività
            val intent = Intent(this, ActivitySelectionActivity::class.java)
            startActivity(intent)
        }

        // Listener per il pulsante "Analisi Prestazioni"
        btnAnalisiPrestazioni.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Button Analisi Prestazioni cliccato")

            // Apri l'Activity per l'analisi delle prestazioni
            val intent = Intent(this, AnalisiPrestazioniActivity::class.java)
            startActivity(intent)
        }

        // Listener per il pulsante "Social"
        btnSocial.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Button Social cliccato")

            // Apri l'Activity per il feed sociale
            val intent = Intent(this, com.example.mover.ui.FeedSocialeActivity::class.java)
            startActivity(intent)
        }

        // Listener per il pulsante "Segmenti"
        findViewById<MaterialButton>(R.id.btnSegmenti).setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Button Segmenti cliccato")

            // Apri l'Activity per l'analisi delle prestazioni (focus sui segmenti)
            val intent = Intent(this, AnalisiPrestazioniActivity::class.java)
            intent.putExtra("focus_tab", "segmenti")
            startActivity(intent)
        }

        // Listener per il pulsante "Club"
        findViewById<MaterialButton>(R.id.btnClub).setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Button Club cliccato")

            // Apri l'Activity per i club
            val intent = Intent(this, com.example.mover.ui.ClubActivity::class.java)
            startActivity(intent)
        }

        // Listener per le cards
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardAnalisiPrestazioni).setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Card Analisi Prestazioni cliccata")

            val intent = Intent(this, AnalisiPrestazioniActivity::class.java)
            startActivity(intent)
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardFeedSociale).setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Card Feed Sociale cliccata")

            val intent = Intent(this, com.example.mover.ui.FeedSocialeActivity::class.java)
            startActivity(intent)
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardClub).setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Card Club cliccata")

            val intent = Intent(this, com.example.mover.ui.ClubActivity::class.java)
            startActivity(intent)
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardSegmenti).setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Card Segmenti cliccata")

            val intent = Intent(this, AnalisiPrestazioniActivity::class.java)
            intent.putExtra("focus_tab", "segmenti")
            startActivity(intent)
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardStatisticheSettimanali).setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Card Statistiche Settimanali cliccata")

            val intent = Intent(this, AnalisiPrestazioniActivity::class.java)
            intent.putExtra("focus_tab", "statistiche")
            startActivity(intent)
        }

        btnCorrere.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (attivitàCorrente == null && !isCountdownRunning) {  // Controlla se non c'è un'attività in corso
                Log.d("MainActivity", "Button Correre cliccato")
                resetAllButtons()
                activateButton(btnCorrere, R.color.colorCorrere)
                centerButtonInScrollView(btnCorrere)
                attivitàDaAvviare = "Corsa"
                startCountdownAnimation("Corsa")
                smoothScrollToBottomWithDelay()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "    Stoppa l'attività prima di visualizzare lo storico",
                    Snackbar.LENGTH_SHORT
                )
                    .setAction("", null)
                    .show()
            }
        }



        btnStorico.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            // Procedi con l'apertura della StoricoAttivitaActivity
            val intent = Intent(this, StoricoAttivitaActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(Intent(this, SettingsActivity::class.java))
        }


        // Listener per il pulsante "Camminare"
        fabWalk.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Button Camminare cliccato")
            centerButtonInScrollView(btnCamminare)
            startActivity("Camminare")
            hideFABs()
            smoothScrollToBottom()
        }

        // Listener per il pulsante "Correre"
        fabRun.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Button Correre cliccato")
            centerButtonInScrollView(btnCorrere)
            startActivity("Corsa")
            hideFABs()
            smoothScrollToBottom()
        }
        // Listener per il pulsante "Sedersi"
        fabSit.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Button Sedersi cliccato")
            centerButtonInScrollView(btnSedersi)
            startActivity("Sedersi")
            hideFABs()  // Nascondi i FAB e attiva la sfocatura
            smoothScrollToBottom()
        }

        fabDrive.setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Log.d("MainActivity", "Button Guidare cliccato")
            centerButtonInScrollView(btnGuidare)
            startActivity("Guidare")
            hideFABs()  // Nascondi i FAB e attiva la sfocatura
            smoothScrollToBottom()
        }

        if (savedInstanceState != null) {
            attivitàCorrente = savedInstanceState.getString("attivitàCorrente")
            oraInizio = savedInstanceState.getLong("oraInizio")
            volteSeduto = savedInstanceState.getInt("volteSeduto")

            Log.d(
                "MainActivity",
                "Recuperato da savedInstanceState: attivitàCorrente=$attivitàCorrente"
            )
        } else {
            Log.d("MainActivity", "savedInstanceState è null")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("attivitàCorrente", attivitàCorrente)
        outState.putLong("oraInizio", oraInizio)
        outState.putInt("volteSeduto", volteSeduto)
        outState.putString("attivitàDaAvviare", attivitàDaAvviare)

    }

    // Funzione per gestire l'inizio delle attività
    private fun startActivity(attività: String) {
        Log.d("MainActivity", "startActivity chiamato con attività: $attività")
        if (attivitàCorrente == null) {
            // Animazione colore toolbar
            val currentToolbarColor = (toolbar.background as? ColorDrawable)?.color ?: Color.TRANSPARENT
            val targetColor = when (attività) {
                "Camminare" -> ContextCompat.getColor(this, R.color.colorCamminare)
                "Corsa" -> ContextCompat.getColor(this, R.color.colorCorrere)
                "Guidare" -> ContextCompat.getColor(this, R.color.colorGuidare)
                "Bicicletta" -> ContextCompat.getColor(this, R.color.colorBici)
                "Sedersi" -> ContextCompat.getColor(this, R.color.colorSedersi)
                else -> ContextCompat.getColor(this, R.color.material_blue)
            }

            // Crea e imposta il gradient drawable
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(targetColor, ColorUtils.setAlphaComponent(targetColor, 0))
            ).apply {
                cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 8f, 8f, 8f, 8f)
                gradientType = GradientDrawable.LINEAR_GRADIENT
            }
            gradientView.background = gradientDrawable

            // Aggiorna StatsManager quando parte una nuova attività
            statsManager.setAttivitàCorrente(attivitàCorrente, oraInizio)

            // Animazione colore toolbar
            val toolbarAnimator = ValueAnimator.ofArgb(currentToolbarColor, targetColor)
            toolbarAnimator.duration = 300
            toolbarAnimator.interpolator = FastOutSlowInInterpolator()
            toolbarAnimator.addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                toolbar.setBackgroundColor(color)
                window.statusBarColor = color
                actionBarTitle.setColorFilter(Color.WHITE)
            }
            toolbarAnimator.start()


            attivitàCorrente = attività
            Log.d("MainActivity", "attivitàCorrente impostata a: $attivitàCorrente")
            oraInizio = System.currentTimeMillis()
            updateStatsVisualsForActivity(attività)

            fabInterrompi.visibility = View.VISIBLE
            switchAutomaticActivity.visibility = View.GONE
            setupBestRecords()

            val activityColor = when (attivitàCorrente) {
                "Camminare" -> ContextCompat.getColor(this, R.color.colorCamminare)
                "Corsa" -> ContextCompat.getColor(this, R.color.colorCorrere)
                "Guidare" -> ContextCompat.getColor(this, R.color.colorGuidare)
                "Bicicletta" -> ContextCompat.getColor(this, R.color.colorBici)
                "Sedersi" -> ContextCompat.getColor(this, R.color.colorSedersi)
                else -> ContextCompat.getColor(this, R.color.material_blue)
            }

            layoutDistanza.findViewById<ImageView>(R.id.iconDistanza)?.setColorFilter(activityColor)
            layoutTempo.findViewById<ImageView>(R.id.iconTempo)?.setColorFilter(activityColor)
            layoutPassi.findViewById<ImageView>(R.id.iconPassi)?.setColorFilter(activityColor)
            layoutVelocitaAttuale.findViewById<ImageView>(R.id.iconVelocitaAttuale)?.setColorFilter(activityColor)
            layoutVelocitaMedia.findViewById<ImageView>(R.id.iconVelocitaMedia)?.setColorFilter(activityColor)
            layoutVolteSeduto.findViewById<ImageView>(R.id.iconVolteSeduto)?.setColorFilter(activityColor)
            layoutPaceMedio.findViewById<ImageView>(R.id.iconPaceMedio)?.setColorFilter(activityColor)

            txtDistanza.setTextColor(activityColor)
            txtTempo.setTextColor(activityColor)
            txtTempoSeconds.setTextColor(activityColor)
            txtPassi.setTextColor(activityColor)
            txtVelocitaAttuale.setTextColor(activityColor)
            txtVelocitaMedia.setTextColor(activityColor)
            txtVolteSeduto.setTextColor(activityColor)
            txtPaceMedio.setTextColor(activityColor)

            when (attività) {
                "Camminare" -> {
                    layoutPassi.visibility = View.VISIBLE
                    layoutDistanza.visibility = View.VISIBLE
                    layoutTempo.visibility = View.VISIBLE
                    checkActivityRecognitionPermission("Camminare")
                }
                "Sedersi" -> {
                    layoutVolteSeduto.visibility = View.VISIBLE
                    txtVolteSeduto.text = "$volteSeduto"
                    startSedutaTrackingService(isManualStart = true)

                }
                "Corsa" -> {
                    layoutDistanza.visibility = View.VISIBLE
                    layoutTempo.visibility = View.VISIBLE
                    layoutVelocitaAttuale.visibility = View.VISIBLE
                    layoutPaceMedio .visibility = View.VISIBLE
                    layoutVelocitaMedia.visibility = View.VISIBLE
                    checkLocationPermissionAndStartService("Corsa")
                }
                "Guidare" -> {
                    layoutDistanza.visibility = View.VISIBLE
                    layoutTempo.visibility = View.VISIBLE
                    layoutVelocitaAttuale.visibility = View.VISIBLE
                    checkLocationPermissionAndStartService("Guidare")
                    registerCarReceiver()
                }
                "Bicicletta" -> {
                    layoutDistanza.visibility = View.VISIBLE
                    layoutTempo.visibility = View.VISIBLE
                    layoutVelocitaAttuale.visibility = View.VISIBLE
                    checkLocationPermissionAndStartService("Bicicletta")
                    registerBikeReceiver()
                }
                else -> {
                    txtPassi.visibility = View.GONE
                    txtVolteSeduto.visibility = View.GONE
                    txtDistanza.visibility = View.GONE
                    txtTempo.visibility = View.GONE
                    Log.d("MainActivity", "Attività già in corso: $attivitàCorrente")
                }
            }
        }
    }

    private fun setupThemeAndStatusBar() {
        val colorMode = prefs.getInt("app_color", 0)
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        // Enable edge-to-edge experience
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (colorMode == 1) {
            // Dynamic colors mode
            val backgroundColor = MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorSurface,
                if (isNightMode) Color.BLACK else Color.WHITE
            )

            // Make status and navigation bars transparent
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT

            // Set appropriate system bar appearance
            if (isNightMode) {
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            } else {
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        } else {
            // Standard colors mode
            if (isNightMode) {
                // Transparent status and navigation bars
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT

                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            } else {
                // Transparent status and navigation bars
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT

                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }

        // If you're using a CoordinatorLayout or similar, add this to your layout
        // android:fitsSystemWindows="true"
    }

    // Funzione per nascondere i FAB secondari e rimuovere la sfocatura
    private fun hideFABs() {
        layoutWalk.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
            layoutWalk.visibility = View.GONE
        }.start()

        layoutRun.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
            layoutRun.visibility = View.GONE
        }.start()

        layoutSit.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
            layoutSit.visibility = View.GONE
        }.start()

        layoutDrive.animate().translationY(0f).alpha(0f).setDuration(200).withEndAction {
            layoutDrive.visibility = View.GONE
        }.start()

        overlayView.visibility = View.GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            overlayView.setRenderEffect(null)
        }

        fabAdd.setImageResource(R.drawable.play_arrow_24dp)

        isOverlayVisible = false
    }
    private fun updateStepCount(stepCount: Int, distance: Float, elapsedTime: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            layoutPassi.visibility = View.VISIBLE
            layoutDistanza.visibility = View.VISIBLE
            layoutTempo.visibility = View.VISIBLE

            txtPassi.text = android.text.Html.fromHtml("<b>$stepCount</b>")

            // Calcola e mostra distanza
            if (distance < 1000) {
                txtDistanza.text = if (distance == 0f) "0" else String.format("%.1f", distance)
                txtDistanzaUnit.text = "m"
            } else {
                txtDistanza.text = String.format("%.2f", distance / 1000)
                txtDistanzaUnit.text = "km"
            }

            // Calcola e mostra tempo con formattazione più precisa
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60

            when {
                minutes == 0L -> {
                    // Solo secondi
                    txtTempo.text = seconds.toString()
                    txtTempoUnit.text = "sec"
                    txtTempoSeconds.visibility = View.GONE
                    txtTempoSecondsUnit.visibility = View.GONE
                }
                minutes < 10L -> {
                    // Minuti e secondi
                    txtTempo.text = minutes.toString()
                    txtTempoUnit.text = "min"
                    txtTempoSeconds.text = seconds.toString()
                    txtTempoSecondsUnit.text = "sec"
                    txtTempoSeconds.visibility = View.VISIBLE
                    txtTempoSecondsUnit.visibility = View.VISIBLE
                }
                else -> {
                    // Solo minuti
                    txtTempo.text = minutes.toString()
                    txtTempoUnit.text = "min"
                    txtTempoSeconds.visibility = View.GONE
                    txtTempoSecondsUnit.visibility = View.GONE
                }
            }

            statsManager.aggiornaStatisticheGiornaliere()
            statsManager.aggiornaStatisticheSettimanali()
            updateStepsProgress()
        }
    }

    private fun updateVolteSeduto(count: Int) {
        volteSeduto = count
        layoutVolteSeduto.visibility = View.VISIBLE
        txtVolteSeduto.text = android.text.Html.fromHtml("<b>$volteSeduto</b>")
        Log.d("MainActivity", "updateVolteSeduto chiamato, volteSeduto=$volteSeduto")
        updateSittingProgress()
    }

    private fun getSessionSittingTime(): Long {
        return if (attivitàCorrente == "Sedersi") {
            System.currentTimeMillis() - oraInizio
        } else {
            0L
        }
    }

    private fun registerCarReceiver() {
        if (!isCarReceiverRegistered) {
            carReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d("MainActivity", "Car broadcast ricevuto!")
                    if (intent?.action == ACTION_CAR_UPDATE) {
                        val distanza = intent.getIntExtra(CAR_EXTRA_DISTANCE, 0)
                        val velocita = intent.getFloatExtra(CAR_EXTRA_SPEED, 0f)
                        val durata = intent.getLongExtra(CAR_EXTRA_DURATION, 0L)
                        Log.d("MainActivity", "Car Data: distanza=$distanza, velocita=$velocita, durata=$durata")
                        updateVehicleData(distanza, velocita, durata, isAuto = true)
                    }
                }
            }
            LocalBroadcastManager.getInstance(this).registerReceiver(
                carReceiver,
                IntentFilter(ACTION_CAR_UPDATE)
            )
            isCarReceiverRegistered = true
            Log.d("MainActivity", "Car receiver registrato")
        }
    }

    private fun registerBikeReceiver() {
        if (!isBikeReceiverRegistered) {
            bikeReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == ACTION_BIKE_UPDATE) {
                        val distanza = intent.getIntExtra(BIKE_EXTRA_DISTANCE, 0)
                        val velocita = intent.getFloatExtra(BIKE_EXTRA_SPEED, 0f)
                        val durata = intent.getLongExtra(BIKE_EXTRA_DURATION, 0L)
                        updateVehicleData(distanza, velocita, durata, isAuto = false)
                    }
                }
            }
            LocalBroadcastManager.getInstance(this).registerReceiver(
                bikeReceiver,
                IntentFilter(ACTION_BIKE_UPDATE)
            )
            isBikeReceiverRegistered = true
        }
    }


    private fun updateVehicleData(distanza: Int, velocita: Float, durata: Long, isAuto: Boolean) {
        if (isFinishing || isDestroyed) return

        try {
            if (layoutDistanza != null && layoutTempo != null && layoutVelocitaAttuale != null) {
                runOnUiThread {
                    try {
                        layoutDistanza.visibility = View.VISIBLE
                        layoutTempo.visibility = View.VISIBLE
                        layoutVelocitaAttuale.visibility = View.VISIBLE


                        if (distanza < 1000) {
                            txtDistanza.text = if (distanza == 0) "0" else String.format(
                                "%.1f",
                                distanza.toFloat()
                            )
                            txtDistanzaUnit.text = "m"
                        } else {
                            txtDistanza.text = String.format("%.2f", distanza / 1000f)
                            txtDistanzaUnit.text = "km"
                        }

                        if (durata < 60000) {
                            val seconds = TimeUnit.MILLISECONDS.toSeconds(durata)
                            txtTempo.text = seconds.toString()
                            txtTempoUnit.text = "sec"
                            txtTempoSeconds.visibility = View.GONE
                            txtTempoSecondsUnit.visibility = View.GONE
                        } else {
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(durata)
                            val seconds = TimeUnit.MILLISECONDS.toSeconds(durata) % 60
                            txtTempo.text = minutes.toString()
                            txtTempoUnit.text = "min"
                            txtTempoSeconds.text = seconds.toString()
                            txtTempoSecondsUnit.text = "sec"
                            txtTempoSeconds.visibility = View.VISIBLE
                            txtTempoSecondsUnit.visibility = View.VISIBLE
                        }
                        txtVelocitaAttuale.text = String.format("%.1f", velocita)
                        txtVelocitaMediaUnit.text = "km/h"

                        val activityColor = if (isAuto) {
                            ContextCompat.getColor(this, R.color.colorGuidare)
                        } else {
                            ContextCompat.getColor(this, R.color.colorBici)
                        }

                        if ((isAuto && velocita > 130) || (!isAuto && velocita > 25)) {
                            txtVelocitaAttuale.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.red
                                )
                            )
                            txtVelocitaAttuale.typeface = android.graphics.Typeface.create(
                                txtVelocitaAttuale.typeface,
                                android.graphics.Typeface.BOLD
                            )
                        } else {
                            txtVelocitaAttuale.setTextColor(activityColor)
                            txtVelocitaAttuale.typeface = android.graphics.Typeface.create(
                                txtVelocitaAttuale.typeface,
                                android.graphics.Typeface.NORMAL
                            )
                        }

                        findViewById<ImageView>(R.id.iconDistanza)?.setColorFilter(activityColor)
                        findViewById<ImageView>(R.id.iconTempo)?.setColorFilter(activityColor)
                        val iconVelocitaAttuale = findViewById<ImageView>(R.id.iconVelocitaAttuale)
                        iconVelocitaAttuale?.setColorFilter(activityColor)
                        if (isAuto) {
                            iconVelocitaAttuale?.setImageResource(R.drawable.readiness)
                        }else{
                            iconVelocitaAttuale?.setImageResource(R.drawable.electric_bike_24dp)
                        }

                        txtStato.text =
                            "Attività in corso: ${if (isAuto) "Guidare" else "Bicicletta"}"

                    } catch (e: Exception) {
                        Log.e("MainActivity", "Errore nell'aggiornamento UI", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Errore globale in updateVehicleData", e)
        }
    }



    private val stepServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StepTrackingService.LocalBinder
            stepTrackingService = binder.getService()
            isStepServiceBound = true

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isStepServiceBound = false
        }
    }

    private fun resetAllViews() {
        layoutPassi.visibility = View.GONE
        layoutVolteSeduto.visibility = View.GONE
        layoutDistanza.visibility = View.GONE
        layoutTempo.visibility = View.GONE
        layoutVelocitaAttuale.visibility = View.GONE
        layoutPaceMedio.visibility = View.GONE
        layoutVelocitaMedia.visibility = View.GONE
        val cardView = findViewById<CardView>(R.id.myCardView)

        // Reset dei margini della cardView
        val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = initialMargin
        layoutParams.marginEnd = initialMargin
        cardView.layoutParams = layoutParams
    }


    private fun getLayoutForCurrentTheme(): Int {
        val colorMode = prefs.getInt("app_color", 0)
        return if (colorMode == 0) {
            R.layout.activity_main
        } else {
            R.layout.activity_main_dynamic
        }
    }

    // Metodo per aggiornare i colori dinamicamente
    private fun updateDynamicColors() {
        val colorMode = prefs.getInt("app_color", 0)
        if (colorMode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)

            // Aggiorna il colore della toolbar
            val defaultColor = getDefaultColor()
            actionBarTitle.setColorFilter(defaultColor)

            // Aggiorna altri elementi UI che usano i colori dinamici
            updateToolbarAppearance(0f) // Resetta l'aspetto della toolbar

            // Forza un aggiornamento della UI
            toolbar.requestLayout()
            window.decorView.requestLayout()
        }
    }


    override fun onResume() {

        super.onResume()

        // Controlla se il tema è cambiato
        val currentColorMode = prefs.getInt("app_color", 0)
        if (currentColorMode != lastColorMode) {
            lastColorMode = currentColorMode
            // Se il tema è cambiato, ricrea l'activity
            recreate()
        } else {
            // Altrimenti aggiorna solo i colori
            updateDynamicColors()
        }
        // Sincronizza lo stato dello switch con lo stato effettivo del servizio
        if (ActivityTrackingApplication.isServiceRunning) {
            Log.d("MainActivity", "onResume - Servizio attivo, sincronizzazione UI")
            switchAutomaticActivity.isChecked = true
            ActivityStateManager.isSwitchActive = true

            // Ripristina l'ultima attività rilevata se disponibile
            ActivityStateManager.currentActivityType?.let { lastActivity ->
                Log.d("MainActivity", "onResume - Ripristino ultima attività: ${activityTypeToString(lastActivity)}")
                txtRecognizedActivity.visibility = View.VISIBLE
                txtRecognizedActivity.text = activityTypeToString(lastActivity)
                imgRecognizedActivity.visibility = View.VISIBLE

                val (activityIcon, activityColorResId) = when (lastActivity) {
                    DetectedActivity.WALKING, DetectedActivity.ON_FOOT ->
                        Pair(R.drawable.footprint_24px, R.color.colorCamminare)
                    DetectedActivity.RUNNING ->
                        Pair(R.drawable.directions_run_24px, R.color.colorCorrere)
                    DetectedActivity.IN_VEHICLE ->
                        Pair(R.drawable.directions_car, R.color.colorGuidare)
                    DetectedActivity.STILL ->
                        Pair(R.drawable.man_24dp, R.color.colorSedersi)
                    DetectedActivity.TILTING ->
                        Pair(R.drawable.seat_24px, R.color.colorSedersi)
                    else ->
                        Pair(R.drawable.psychology_alt, R.color.material_blue2)
                }

                imgRecognizedActivity.setImageResource(activityIcon)
                val color = ContextCompat.getColor(this, activityColorResId)
                imgRecognizedActivity.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
        } else if (attivitàCorrente != null) {
            Log.d("MainActivity", "onResume - Ripristino attività manuale: $attivitàCorrente")

            cardContainer.visibility = View.VISIBLE
            contatoriCard.visibility = View.VISIBLE
            statsLayout.visibility = View.VISIBLE

            ripristinaStatoAttivitàManuale(attivitàCorrente!!)

        }

        val mapStyleSetting = prefs.getInt("map_style", 0)

        // Controllo se la mappa è pronta
        if (::mMap.isInitialized) {
            applyMapStyle(mapStyleSetting)
        }

        val runLocationFilter = IntentFilter(RunTrackingService.ACTION_RUN_LOCATION_UPDATE)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(runLocationReceiver, runLocationFilter)

        if (!isStepReceiverRegistered) {
            val stepFilter = IntentFilter(StepTrackingService.ACTION_STEP_COUNT_UPDATE)
            LocalBroadcastManager.getInstance(this).registerReceiver(stepCountReceiver, stepFilter)
            isStepReceiverRegistered = true
            Log.d("MainActivity", "StepCountReceiver registrato in onResume()")
        }

        if (!isActivityReceiverRegistered) {
            val filter = IntentFilter("com.example.personalphysicaltracker.ACTIVITY_RECOGNIZED")
            LocalBroadcastManager.getInstance(this).registerReceiver(activityReceiver, filter)
            isActivityReceiverRegistered = true
            Log.d("MainActivity", "activityReceiver registrato in onResume()")
        }

        if (!isSedutaReceiverRegistered) {
            val sedutaFilter = IntentFilter(SedutaTrackingService.ACTION_SEDUTA_COUNT_UPDATE)
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(sedutaCountReceiver, sedutaFilter)
            isSedutaReceiverRegistered = true
            Log.d("MainActivity", "SedutaCountReceiver registrato in onResume()")
        }

        if (!isRunReceiverRegistered) {
            val runFilter = IntentFilter(RunTrackingService.ACTION_RUN_UPDATE)
            LocalBroadcastManager.getInstance(this).registerReceiver(runReceiver, runFilter)
            isRunReceiverRegistered = true
            Log.d("MainActivity", "RunReceiver registrato in onResume()")
            Log.d("MainActivity", "MainActivity resumed.");
        }
        if (!isDriveReceiverRegistered) {
            val driveFilter = IntentFilter(CarTrackingService.ACTION_CAR_UPDATE)
            LocalBroadcastManager.getInstance(this).registerReceiver(driveReceiver, driveFilter)
            isDriveReceiverRegistered = true
            Log.d("MainActivity", "DriveReceiver registrato in onResume()")
        }

        if (!isBikeReceiverRegistered) {
            val bikeFilter = IntentFilter(BikeTrackingService.ACTION_BIKE_UPDATE)
            LocalBroadcastManager.getInstance(this).registerReceiver(bikeReceiver, bikeFilter)
            isBikeReceiverRegistered = true
            Log.d("MainActivity", "BikeReceiver registrato in onResume()")
        }

        if (!isPauseReceiverRegistered) {
            val pauseFilter =
                IntentFilter("com.example.personalphysicaltracker.ACTION_ACTIVITY_PAUSE_STATE")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                registerReceiver(pauseReceiver, pauseFilter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(pauseReceiver, pauseFilter, android.content.Context.RECEIVER_NOT_EXPORTED)
            }
            isPauseReceiverRegistered = true
            Log.d("MainActivity", "PauseReceiver registrato in onResume()")
        }
    }


    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause chiamato")

        LocalBroadcastManager.getInstance(this).unregisterReceiver(runLocationReceiver)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy chiamato")

        LocalBroadcastManager.getInstance(this).unregisterReceiver(sedutaCountReceiver)

        prefs.unregisterOnSharedPreferenceChangeListener(preferenceListener)

        if (isActivityReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(activityReceiver)
                isActivityReceiverRegistered = false
                Log.d("MainActivity", "ActivityReceiver deregistrato in onDestroy()")
            } catch (e: IllegalArgumentException) {
                Log.e("MainActivity", "Errore durante la deregistrazione di ActivityReceiver", e)
            }
        }
    }

    private fun ripristinaStatoAttivitàManuale(attività: String) {
        // Rendi visibili le card dei contatori
        cardContainer.visibility = View.VISIBLE
        contatoriCard.visibility = View.VISIBLE
        statsLayout.visibility = View.VISIBLE  // Rendi visibile il layout delle statistiche

        // Ripristina UI per attività manuale
        when (attività) {
            "Camminare" -> {
                activateButton(btnCamminare, R.color.colorCamminare)
                layoutPassi.visibility = View.VISIBLE
                layoutDistanza.visibility = View.VISIBLE
                layoutTempo.visibility = View.VISIBLE
                // Nascondi gli altri layout
                layoutVolteSeduto.visibility = View.GONE
                layoutVelocitaAttuale.visibility = View.GONE
                layoutVelocitaMedia.visibility = View.GONE
                layoutPaceMedio.visibility = View.GONE
            }
            "Corsa" -> {
                activateButton(btnCorrere, R.color.colorCorrere)
                layoutDistanza.visibility = View.VISIBLE
                layoutTempo.visibility = View.VISIBLE
                layoutVelocitaAttuale.visibility = View.VISIBLE
                layoutVelocitaMedia.visibility = View.VISIBLE
                layoutPaceMedio.visibility = View.VISIBLE
                // Nascondi gli altri layout
                layoutPassi.visibility = View.GONE
                layoutVolteSeduto.visibility = View.GONE
            }
            "Guidare" -> {
                activateButton(btnGuidare, R.color.colorGuidare)
                layoutDistanza.visibility = View.VISIBLE
                layoutTempo.visibility = View.VISIBLE
                layoutVelocitaAttuale.visibility = View.VISIBLE
                // Nascondi gli altri layout
                layoutPassi.visibility = View.GONE
                layoutVolteSeduto.visibility = View.GONE
                layoutVelocitaMedia.visibility = View.GONE
                layoutPaceMedio.visibility = View.GONE
            }
            "Bicicletta" -> {
                activateButton(btnBicicletta, R.color.colorBici)
                layoutDistanza.visibility = View.VISIBLE
                layoutTempo.visibility = View.VISIBLE
                layoutVelocitaAttuale.visibility = View.VISIBLE
                // Nascondi gli altri layout
                layoutPassi.visibility = View.GONE
                layoutVolteSeduto.visibility = View.GONE
                layoutVelocitaMedia.visibility = View.GONE
                layoutPaceMedio.visibility = View.GONE
            }
            "Sedersi" -> {
                activateButton(btnSedersi, R.color.colorSedersi)
                layoutVolteSeduto.visibility = View.VISIBLE
                // Nascondi gli altri layout
                layoutPassi.visibility = View.GONE
                layoutDistanza.visibility = View.GONE
                layoutTempo.visibility = View.GONE
                layoutVelocitaAttuale.visibility = View.GONE
                layoutVelocitaMedia.visibility = View.GONE
                layoutPaceMedio.visibility = View.GONE
            }
        }

        // Ripristina stato generale UI
        fabInterrompi.visibility = View.VISIBLE
        switchAutomaticActivity.visibility = View.GONE
        updateStatsVisualsForActivity(attività)
        animateMapSize(expand = true)
        animaBottoniInStatoMinimo(attività)
    }

    private fun applyMapStyle(styleSetting: Int) {
        try {
            when (styleSetting) {
                0 -> {
                    val isDarkMode = (resources.configuration.uiMode
                            and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    if (isDarkMode) {
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
                    } else {
                        mMap.setMapStyle(null)
                    }
                }
                1 -> mMap.setMapStyle(null) // Chiaro
                2 -> mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark)) // Scuro
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MainActivity", "Risorsa di stile non trovata. Errore: ", e)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isMyLocationButtonEnabled = false
            isScrollGesturesEnabled = true
            isZoomGesturesEnabled = true
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = true
        }

        val lastLat = intent.getDoubleExtra("LAST_KNOWN_LAT", 0.0)
        val lastLng = intent.getDoubleExtra("LAST_KNOWN_LNG", 0.0)
        val lastZoom = intent.getFloatExtra("LAST_KNOWN_ZOOM", 15f)

        if (lastLat != 0.0 && lastLng != 0.0) {
            val lastPosition = LatLng(lastLat, lastLng)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, lastZoom))
        }

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val mapStyleSetting = prefs.getInt("map_style", 0)

        Log.d("MainActivity", "Stile mappa letto dalle preferenze: $mapStyleSetting")

        try {
            when (mapStyleSetting) {
                0 -> { // Automatico
                    val isDarkMode = resources.configuration.uiMode and
                            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

                    Log.d("MainActivity", "Modalità scura di sistema: $isDarkMode")

                    if (isDarkMode) {
                        val success = mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark)
                        )
                        Log.d("MainActivity", "Applicazione stile scuro: $success")
                        if (!success) {
                            Log.e("MainActivity", "Stile della mappa scuro non applicato.")
                        }
                    } else {
                        Log.d("MainActivity", "Applicazione stile chiaro (predefinito)")
                        mMap.setMapStyle(null)
                    }
                }
                1 -> { // Chiaro
                    Log.d("MainActivity", "Forzato stile chiaro")
                    mMap.setMapStyle(null)
                }
                2 -> { // Scuro
                    Log.d("MainActivity", "Forzato stile scuro")
                    val success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark)
                    )
                    Log.d("MainActivity", "Applicazione stile scuro forzato: $success")
                    if (!success) {
                        Log.e("MainActivity", "Stile della mappa scuro non applicato.")
                    }
                }
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MainActivity", "Risorsa di stile non trovata. Errore: ", e)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                    getCurrentLocation()
                    pendingActivityToStart?.let {
                        startAppropriateService(it)
                        pendingActivityToStart = null
                    }
                }
            } else {
                Toast.makeText(this, "Permesso di localizzazione negato", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Ottieni la posizione attuale dell'utente
    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.setPadding(0, 400, 0, 50)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("Sei qui"))
                }
            }
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Permesso di localizzazione non concesso", e)
        }
    }

    // Funzione per richiedere il permesso ACTIVITY_RECOGNITION
    private fun checkActivityRecognitionPermission(activityToStart: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionsToRequest = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BODY_SENSORS
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BODY_SENSORS)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            when {
                permissionsToRequest.isEmpty() -> {
                    startAppropriateService(activityToStart)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.BODY_SENSORS) -> {
                    Toast.makeText(
                        this,
                        "I permessi sono necessari per monitorare l'attività.",
                        Toast.LENGTH_LONG
                    ).show()
                    pendingActivityToStart = activityToStart
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
                else -> {
                    pendingActivityToStart = activityToStart
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            }
        } else {
            startAppropriateService(activityToStart)
        }
    }

    private fun startAppropriateService(activityToStart: String) {
        when (activityToStart) {
            "Camminare" -> startStepTrackingService(isManualStart = true)
            "Sedersi" -> startSedutaTrackingService(isManualStart = true)
            "Corsa" -> {
                startRunTrackingService(isManualStart = true)
            }
            "Guidare" -> startDriveTrackingService(isManualStart = true)
            "Bicicletta" -> startBikeTrackingService(isManualStart = true)
        }
    }

    private fun startStepTrackingService(isManualStart: Boolean) {
        val intent = Intent(this, StepTrackingService::class.java)
        intent.putExtra("Automatica", !isManualStart)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
        Log.d("MainActivity", "StepTrackingService avviato")
    }


    private fun startRunTrackingService(isManualStart: Boolean) {
        val intent = Intent(this, RunTrackingService::class.java)
        intent.putExtra("Automatica", !isManualStart)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
        Log.d("MainActivity", "RunTrackingService avviato")
    }

    private fun updateRunData(
        distanza: Float,
        tempo: Long,
        velocitaAttuale: Float,
        velocitaMedia: Float,
        paceMedio: Float,
        isPaused: Boolean
    ) {
        findViewById<LinearLayout>(R.id.layoutDistanza).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layoutTempo).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layoutVelocitaAttuale).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layoutVelocitaMedia).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layoutPaceMedio).visibility = View.VISIBLE


        findViewById<ImageView>(R.id.iconTempo)?.visibility =
            if (isPaused) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.txtTempo)?.visibility =
            if (isPaused) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.txtTempoUnit)?.visibility =
            if (isPaused) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.txtTempoSeconds)?.visibility =
            if (isPaused) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.txtTempoSecondsUnit)?.visibility =
            if (isPaused) View.GONE else View.VISIBLE

        findViewById<ImageView>(R.id.iconPausa)?.visibility =
            if (isPaused) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.txtPausa)?.visibility =
            if (isPaused) View.VISIBLE else View.GONE

        val activityColor = when (attivitàCorrente) {
            "Corsa" -> ContextCompat.getColor(this, R.color.colorCorrere)
            else -> ContextCompat.getColor(this, R.color.material_blue)
        }
        findViewById<ImageView>(R.id.iconPausa).setColorFilter(activityColor)
        findViewById<TextView>(R.id.txtPausa).setTextColor(activityColor)

        // Aggiunta del pace medio
        if (paceMedio > 0) {
            val paceMinuti = paceMedio.toInt()
            val paceSecondi = ((paceMedio - paceMinuti) * 60).toInt()
            txtPaceMedio.text = String.format("%d:%02d", paceMinuti, paceSecondi)
        } else {
            txtPaceMedio.text = "-:-"
        }

        try {
            if (distanza < 1000) {
                txtDistanza.text = if (distanza == 0f) "0" else String.format("%.1f", distanza)
                txtDistanzaUnit.text = "m"

            } else {
                txtDistanza.text = String.format("%.2f", distanza / 1000)  // Due decimali per i km
                txtDistanzaUnit.text = "km"

            }

            if (tempo < 60000) {
                val seconds = TimeUnit.MILLISECONDS.toSeconds(tempo)
                txtTempo.text = seconds.toString()
                txtTempoUnit.text = "sec"
                txtTempoSeconds.visibility = View.GONE
                txtTempoSecondsUnit.visibility = View.GONE
            } else {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(tempo)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(tempo) % 60
                txtTempo.text = minutes.toString()
                txtTempoUnit.text = "min"
                txtTempoSeconds.text = seconds.toString()
                txtTempoSecondsUnit.text = "sec"
                txtTempoSeconds.visibility = View.VISIBLE
                txtTempoSecondsUnit.visibility = View.VISIBLE
            }

            // Aggiorna velocità attuale con il colore dell'attività
            txtVelocitaAttuale.text = String.format("%.2f", velocitaAttuale)

            // Logica per il cambio di colore basato sulla velocità
            if (velocitaAttuale > 15) {
                txtVelocitaAttuale.setTextColor(ContextCompat.getColor(this, R.color.red))
                txtVelocitaAttuale.typeface = android.graphics.Typeface.create(
                    txtVelocitaAttuale.typeface,
                    android.graphics.Typeface.BOLD
                )
            } else {
                txtVelocitaAttuale.setTextColor(activityColor)
                txtVelocitaAttuale.typeface = android.graphics.Typeface.create(
                    txtVelocitaAttuale.typeface,
                    android.graphics.Typeface.NORMAL
                )
            }

            txtVelocitaMedia.visibility = View.VISIBLE
            // Mostra la velocità media in km/h
            if (velocitaMedia > 0) {
                txtVelocitaMedia.text = String.format("%.2f", velocitaMedia)
                txtVelocitaMediaUnit.text = "km/h"
            } else {
                txtVelocitaMedia.text = "-:-"
                txtVelocitaMediaUnit.text = "km/h"
            }
            if (isPaused) {
                txtStato.text = "Attività in pausa"
                txtPausa.visibility = View.VISIBLE
            } else {
                txtStato.text = "Attività in corso: Corsa"
                txtPausa.visibility = View.GONE
            }
            updateRunningProgress()
        } catch (e: UninitializedPropertyAccessException) {
            Log.e("MainActivity", "Errore nell'aggiornamento dei dati", e)
        }
    }

    private fun stopRunTrackingService() {
        val intent = Intent(this, RunTrackingService::class.java)
        stopService(intent)
        Log.d("MainActivity", "RunTrackingService fermato")
    }

    private fun startSedutaTrackingService(isManualStart: Boolean) {
        val orientationIntent = Intent(this, DeviceOrientationService::class.java)
        ContextCompat.startForegroundService(this, orientationIntent)

        val intent = Intent(this, SedutaTrackingService::class.java)
        intent.putExtra("isManualStart", isManualStart)
        intent.putExtra("Automatica", !isManualStart)
        intent.putExtra("isManualStart", isManualStart)
        intent.putExtra("isCounting", true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
        Log.d(
            "MainActivity",
            "SedutaTrackingService avviato direttamente con isManualStart = $isManualStart"
        )


        findViewById<LinearLayout>(R.id.layoutVolteSeduto).visibility = View.VISIBLE


    }

    private fun startDriveTrackingService(isManualStart: Boolean) {
        val intent = Intent(this, CarTrackingService::class.java)
        intent.putExtra("isManualStart", isManualStart)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
        // Registra il receiver
        registerCarReceiver()
        // Imposta lo stato dell'attività
        attivitàCorrente = "Guidare"
        txtStato.text = "Attività in corso: Guidare"
        // Mostra i contatori appropriati
        layoutDistanza.visibility = View.VISIBLE
        layoutTempo.visibility = View.VISIBLE
        layoutVelocitaAttuale.visibility = View.VISIBLE
        Log.d("MainActivity", "DriveTrackingService avviato")
    }

    private fun startBikeTrackingService(isManualStart: Boolean) {
        val intent = Intent(this, BikeTrackingService::class.java)
        intent.putExtra("isManualStart", isManualStart)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
        registerBikeReceiver()
        attivitàCorrente = "Bicicletta"
        txtStato.text = "Attività in corso: Bicicletta"
        layoutDistanza.visibility = View.VISIBLE
        layoutTempo.visibility = View.VISIBLE
        layoutVelocitaAttuale.visibility = View.VISIBLE
        Log.d("MainActivity", "BikeTrackingService avviato")
    }


    private fun interrompiAttività() {

        val oraFine = System.currentTimeMillis()
        Log.d("MainActivity", "Interrompo attività. Ora fine: $oraFine")
        enableHorizontalScroll()
        animateMapSize(expand = false)
        updateActivityStats()
        setupCharts()
        isNearLockPoint = false


        isNearLockPoint = false
        findViewById<MaterialCardView>(R.id.bottomButtonsCard).visibility = View.VISIBLE

        polyline?.remove()
        polyline = null
        runPathCoordinates.clear()

        spacerView.visibility = View.GONE
        cardContainer.visibility = View.VISIBLE
        contatoriCard.visibility = View.VISIBLE
        attivitàDaAvviare = null
        val bottomButtonsCard = findViewById<MaterialCardView>(R.id.bottomButtonsCard)
        bottomButtonsCard.visibility = View.GONE
        fabAdd.visibility = View.GONE

        val height = cardContainer.height.toFloat()
        if (height == 0f) {
            cardContainer.post {
                animateCardDisappearance()
                smoothScrollToTop()
            }
        } else {
            cardContainer.post {
                animateCardDisappearance()
                smoothScrollToTop()
            }
        }

        ripristinaBottoni()
        resetMapSize()
        resetButtonsPositionWithAnimation()
        resetAllButtons()

        findViewById<LinearLayout>(R.id.layoutPaceMedio)?.visibility = View.GONE
        findViewById<LinearLayout>(R.id.RecordsLayout).visibility = View.GONE
        // Reset completo della toolbar
        gradientView.visibility = View.GONE // Nasconde il gradiente
        toolbar.setBackgroundColor(Color.TRANSPARENT)
        toolbar.elevation = 0f
        gradientView.visibility = View.GONE

        // Usa il colore predefinito considerando il tema dinamico
        val defaultColor = getDefaultColor()
        actionBarTitle.setColorFilter(defaultColor)
        actionBarTitle.alpha = 1f

        // Reset della status bar
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        window.statusBarColor = if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            resources.getColor(R.color.transparent_b)
        } else {
            resources.getColor(android.R.color.transparent)
        }

        findViewById<LinearLayout>(R.id.RecordsLayout).apply {
            visibility = View.VISIBLE
            alpha = 0f
            setBackgroundResource(R.drawable.rounded_background)  // Ripristina il background originale
            val params = layoutParams as ViewGroup.MarginLayoutParams
            layoutParams = params
            animate()
                .alpha(1f)
                .setDuration(100)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }


        fabInterrompi.visibility = View.GONE
        isOverlayVisible = false
        fabAdd.setImageResource(R.drawable.play_arrow_24dp)
        findViewById<LinearLayout>(R.id.RecordsLayout).visibility = View.VISIBLE


        btnStorico.apply {
            alpha = 1.0f
            setOnTouchListener(null)
        }

        btnCamminare.alpha = 1.0f
        btnGuidare.alpha = 1.0f
        btnBicicletta.alpha = 1.0f
        btnSedersi.alpha = 1.0f
        btnCorrere.alpha = 1.0f

        findViewById<ImageView>(R.id.iconPausa)?.visibility = View.GONE
        findViewById<TextView>(R.id.txtPausa)?.visibility = View.GONE

        findViewById<ImageView>(R.id.iconTempo)?.visibility = View.VISIBLE
        findViewById<TextView>(R.id.txtTempo)?.visibility = View.VISIBLE
        findViewById<TextView>(R.id.txtTempoUnit)?.visibility = View.VISIBLE
        findViewById<TextView>(R.id.txtTempoSeconds)?.visibility = View.VISIBLE
        findViewById<TextView>(R.id.txtTempoSecondsUnit)?.visibility = View.VISIBLE

        if (attivitàCorrente == "Camminare") {
            stopStepTrackingService()
            setupBestRecords()
        } else if (attivitàCorrente == "Sedersi") {
            val intent = Intent(this, SedutaTrackingService::class.java)
            stopService(intent)
            // Ferma il DeviceOrientationService
            val orientationServiceIntent = Intent(this, DeviceOrientationService::class.java)
            stopService(orientationServiceIntent)
            Log.d("MainActivity", "DeviceOrientationService fermato")
        } else if (attivitàCorrente == "Corsa") {
            stopRunTrackingService()
            val distanzaTotale = distanzaPercorsa
            val tempoTotale = System.currentTimeMillis() - oraInizio
            val velocitaMediaAttuale = if (tempoTotale > 0) {
                (distanzaTotale / (tempoTotale / 1000f)) * 3.6f // km/h
                setupBestRecords()
            } else {
                0f
            }
        } else if (attivitàCorrente == "Guidare") {
            stopDriveTrackingService()
            txtStato.visibility = View.GONE
            setupBestRecords()
        }
        else if (attivitàCorrente == "Bicicletta") {
            val intent = Intent(this, BikeTrackingService::class.java)
            stopService(intent)
            Log.d("MainActivity", "BikeTrackingService fermato")
            if (isBikeReceiverRegistered) {
                try {
                    LocalBroadcastManager.getInstance(this).unregisterReceiver(bikeReceiver)
                    isBikeReceiverRegistered = false
                } catch (e: Exception) {
                    Log.e("MainActivity", "Errore deregistrazione BikeReceiver", e)
                }
            }
            txtStato.visibility = View.GONE
        }

        distanzaPercorsa = 0f
        txtStato.text = "Nessuna attività in corso"
        attivitàCorrente = null

    }


    private fun getDefaultColor(): Int {
        // Verifica se il tema dinamico è abilitato
        val colorMode = prefs.getInt("app_color", 0)
        val isDynamicEnabled = colorMode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        return if (isDynamicEnabled) {
            // Usa il colore primario del tema dinamico
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary,
                ContextCompat.getColor(this, R.color.material_blue))
        } else {
            // Usa il colore statico
            ContextCompat.getColor(this, R.color.material_blue)
        }
    }


    // Funzione per interrompere il servizio di tracciamento dei passi
    private fun stopStepTrackingService() {
        val intent = Intent(this, StepTrackingService::class.java)
        stopService(intent)
        Log.d("MainActivity", "StepTrackingService interrotto")
    }

    private fun stopDriveTrackingService() {
        val intent = Intent(this, CarTrackingService::class.java)
        stopService(intent)
        Log.d("MainActivity", "DriveTrackingService fermato")

        if (isDriveReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(driveReceiver)
                isDriveReceiverRegistered = false
                Log.d("MainActivity", "DriveReceiver deregistrato in stopDriveTrackingService()")
            } catch (e: Exception) {
                Log.e("MainActivity", "Errore deregistrazione DriveReceiver", e)
            }
        }
    }


    private fun checkLocationPermissionAndStartService(activityToStart: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startAppropriateService(activityToStart)
        } else {
            pendingActivityToStart = activityToStart
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun stopAutomaticTrackingService() {
        try {
            (application as ActivityTrackingApplication).stopActivityRecognition()

            val servicesToStop = listOf(
                StepTrackingService::class.java,
                RunTrackingService::class.java,
                SedutaTrackingService::class.java,
                DeviceOrientationService::class.java,
                CarTrackingService::class.java,
                BikeTrackingService::class.java,
                ActivityRecognitionService1::class.java
            )

            servicesToStop.forEach { serviceClass ->
                // Invia un broadcast per servizi con receiver
                when (serviceClass) {
                    StepTrackingService::class.java ->
                        sendBroadcast(Intent("STOP_STEP_TRACKING_SERVICE"))
                    RunTrackingService::class.java ->
                        sendBroadcast(Intent("STOP_RUN_TRACKING_SERVICE"))
                    SedutaTrackingService::class.java -> {
                        val sedutaIntent = Intent(this, SedutaTrackingService::class.java).apply {
                            action = SedutaTrackingService.ACTION_STOP_SERVICE
                        }
                        startService(sedutaIntent)
                    }
                }

                val intent = Intent(this, serviceClass)
                stopService(intent)
            }

            ActivityStateManager.apply {
                currentActivityType = DetectedActivity.UNKNOWN
                activityStartTime = 0L
                orientationServiceStarted = false
                isFirstDetectionAfterSwitch = true
                isSwitchActive = false
                hasReceivedFirstUpdate = false
            }

            previousActivityType = DetectedActivity.UNKNOWN

            Log.d("MainActivity", "Tutti i servizi di tracciamento automatico fermati")

        } catch (e: Exception) {
            Log.e("MainActivity", "Errore durante l'arresto dei servizi", e)
        }
    }



    private fun checkActivityRecognitionPermissionAndStartService1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionsToRequest = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BODY_SENSORS
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BODY_SENSORS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            when {
                permissionsToRequest.isEmpty() -> {
                    (application as ActivityTrackingApplication).startActivityRecognition()
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!ActivityStateManager.hasReceivedFirstUpdate) {
                            Log.d("MainActivity", "Nessun aggiornamento ricevuto, riavvio servizio")
                            (application as ActivityTrackingApplication).stopActivityRecognition()
                            (application as ActivityTrackingApplication).startActivityRecognition()
                        }
                    }, 1000)
                }
                else -> {
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            }
        } else {
            (application as ActivityTrackingApplication).startActivityRecognition()
        }
    }



    private fun activateButton(button: MaterialButton, color: Int) {
        button.setBackgroundColor(ContextCompat.getColor(this, color))
        button.setTextColor(ContextCompat.getColor(this, R.color.white))
        button.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
    }

    private fun getButtonColor(): Int {
        val colorMode = prefs.getInt("app_color", 0)
        return if (colorMode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Tema dinamico
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceVariant,
                ContextCompat.getColor(this, R.color.rossino))
        } else {
            // Tema statico
            ContextCompat.getColor(this, R.color.rossino)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun getFabColor(isSelected: Boolean): Int {
        val colorMode = prefs.getInt("app_color", 0)
        return if (colorMode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Tema dinamico
            MaterialColors.getColor(this,
                if (isSelected) com.google.android.material.R.attr.colorPrimary
                else com.google.android.material.R.attr.colorOnSurfaceVariant,
                if (isSelected) R.color.colore_fab2 else R.color.colore_fab)
        } else {
            // Tema statico
            ContextCompat.getColor(this,
                if (isSelected) R.color.colore_fab2 else R.color.colore_fab)
        }
    }

    private fun resetButton(button: MaterialButton, originalColor: Int, textColor: Int) {
        val backgroundColor = getButtonColor()
        button.setBackgroundColor(backgroundColor)

        val finalTextColor = if (prefs.getInt("app_color", 0) == 1 &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Per il tema dinamico, usa il colore appropriato del sistema
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant,
                ContextCompat.getColor(this, textColor))
        } else {
            ContextCompat.getColor(this, textColor)
        }

        button.setTextColor(finalTextColor)
        button.iconTint = ColorStateList.valueOf(finalTextColor)
    }

    @SuppressLint("ResourceType")
    private fun resetAllButtons() {
        val backgroundColor = getButtonColor()

        resetButton(btnCamminare, backgroundColor, R.color.colorCamminare)
        resetButton(btnCorrere, backgroundColor, R.color.colorCorrere)
        resetButton(btnGuidare, backgroundColor, R.color.colorGuidare)
        resetButton(btnBicicletta, backgroundColor, R.color.colorBici)
        resetButton(btnSedersi, backgroundColor, R.color.colorSedersi)

        val bottomButtonsLayout = findViewById<LinearLayout>(R.id.bottomButtonsLayout)
        bottomButtonsLayout.setBackgroundColor(backgroundColor)

        val homeContainer = findViewById<FrameLayout>(R.id.homeContainer)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnStorico = findViewById<ImageButton>(R.id.btnStorico)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        if (prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Per il tema dinamico, usa un drawable con il colore del sistema
            homeContainer.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(MaterialColors.getColor(this@MainActivity,
                    com.google.android.material.R.attr.colorSurfaceVariant,
                    ContextCompat.getColor(this@MainActivity, R.drawable.custum_circle_main_back1)))
            }
        } else {
            homeContainer.background = ContextCompat.getDrawable(this, R.drawable.custum_circle_main_back)
        }

        // Imposta i colori delle FAB
        btnHome.setColorFilter(getFabColor(true))
        btnStorico.setColorFilter(getFabColor(false))
        btnSettings.setColorFilter(getFabColor(false))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val isDarkMode =
            newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES


        if (::mMap.isInitialized) {
            if (isDarkMode) {
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
            } else {
                mMap.setMapStyle(null)
            }
        }
    }

    private fun smoothScrollToBottom(duration: Long = 1000) {
        val scrollView = findViewById<NestedScrollView>(R.id.scrollView)
        val lastChild = scrollView.getChildAt(scrollView.childCount - 1)
        val bottomDetector = lastChild.bottom + scrollView.paddingBottom
        val scrollViewHeight = scrollView.height + scrollView.scrollY

        if (bottomDetector > scrollViewHeight) {
            val targetY = bottomDetector - scrollViewHeight
            val startY = scrollView.scrollY
            val animator = ValueAnimator.ofInt(startY, targetY)
            animator.duration = duration
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                scrollView.scrollTo(0, value)
            }
            animator.start()
        }
    }

    private fun smoothScrollToBottomWithDelay(delay: Long = 200, duration: Long = 400, targetY: Int? = null) {
        // Ottiene la densità dello schermo
        val scale = resources.displayMetrics.density

        // Valori base in dp
        val baseCardBottomOffsetDp = 110
        val cardBottomOffsetDp = when (attivitàDaAvviare) {
            "Corsa" -> 120  // Esempio: offset diverso per corsa
            "Sedersi" -> 100
            else -> baseCardBottomOffsetDp
        }

        // Converte dp in pixel
        val cardBottomOffsetPx = (cardBottomOffsetDp * scale).toInt()

        Handler(Looper.getMainLooper()).postDelayed({
            val scrollView = findViewById<NestedScrollView>(R.id.scrollView)
            val contatoriCard = findViewById<MaterialCardView>(R.id.contatoriCard)
            spacerView = findViewById<View>(R.id.spacerView)

            val scrollTarget = targetY ?: (contatoriCard.bottom + contatoriCard.marginBottom - cardBottomOffsetPx)

            val animator = ValueAnimator.ofInt(scrollView.scrollY, scrollTarget)
            animator.duration = duration
            animator.interpolator = DecelerateInterpolator()

            animator.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                scrollView.smoothScrollTo(0, value)
            }

            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    scrollView.post {
                        if (targetY == null) {
                            val cardBottom = contatoriCard.bottom + contatoriCard.marginBottom
                            val scrollViewHeight = scrollView.height
                            val currentScroll = scrollView.scrollY

                            if (cardBottom > (currentScroll + scrollViewHeight)) {
                                val adjustment = cardBottom - (currentScroll + scrollViewHeight)
                                scrollView.smoothScrollBy(0, adjustment)
                            }
                        }
                    }
                }
            })

            animator.start()
        }, delay)
    }



    private var countdownTimer: CountDownTimer? = null

    private fun startCountdownAnimation(attivitàDaAvviare: String) {
        isCountdownRunning = true
        cardContainer.visibility = View.VISIBLE
        contatoriCard.visibility = View.VISIBLE
        countdownText.visibility = View.VISIBLE
        statsLayout.visibility = View.VISIBLE
        switchAutomaticActivity.visibility = View.GONE
        statsContainer.visibility = View.GONE
        bottomButtonsCard.visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layoutPaceMedio)?.visibility = View.GONE

        bottomButtonsCard.alpha = 1f
        animateMapSize(expand = true)
        when (attivitàDaAvviare) {
            "Camminare" -> {
                btnGuidare.alpha = 0.5f
                btnSedersi.alpha = 0.5f
                btnCorrere.alpha = 0.5f
                btnBicicletta.alpha = 0.5f
                animaBottoniInStatoMinimo(attivitàDaAvviare)
            }

            "Corsa" -> {
                btnGuidare.alpha = 0.5f
                btnCamminare.alpha = 0.5f
                btnSedersi.alpha = 0.5f
                btnBicicletta.alpha = 0.5f
                animaBottoniInStatoMinimo(attivitàDaAvviare)
            }

            "Guidare" -> {
                btnCamminare.alpha = 0.5f
                btnSedersi.alpha = 0.5f
                btnCorrere.alpha = 0.5f
                btnBicicletta.alpha = 0.5f
                val iconVelocitaAttuale = findViewById<ImageView>(R.id.iconVelocitaAttuale)
                iconVelocitaAttuale?.setImageResource(R.drawable.readiness)
                animaBottoniInStatoMinimo(attivitàDaAvviare)
            }
            "Bicicletta" -> {
                btnCamminare.alpha = 0.5f
                btnSedersi.alpha = 0.5f
                btnCorrere.alpha = 0.5f
                btnGuidare.alpha = 0.5f
                val iconVelocitaAttuale = findViewById<ImageView>(R.id.iconVelocitaAttuale)
                iconVelocitaAttuale?.setImageResource(R.drawable.electric_bike_24dp)
                animaBottoniInStatoMinimo(attivitàDaAvviare)
            }
            "Sedersi" -> {
                btnGuidare.alpha = 0.5f
                btnCamminare.alpha = 0.5f
                btnCorrere.alpha = 0.5f
                btnBicicletta.alpha = 0.5f
                animaBottoniInStatoMinimo(attivitàDaAvviare)
            }
        }



        // Resetta tutti i valori all'inizio
        txtPassi.text = "0"
        txtDistanza.text = "0.0"
        txtDistanzaUnit.text = "m"
        txtTempo.text = "0"
        txtTempoUnit.text = "sec"
        txtVelocitaAttuale.text = "0.0"
        txtVelocitaMedia.text = "0.0"
        txtVolteSeduto.text = "0"
        // Nascondi tutti i TextView di statsLayout
        layoutPassi.visibility = View.GONE
        layoutVolteSeduto.visibility = View.GONE
        layoutDistanza.visibility = View.GONE
        layoutTempo.visibility = View.GONE
        layoutVelocitaAttuale.visibility = View.GONE
        layoutPaceMedio.visibility = View.GONE
        layoutVelocitaMedia.visibility = View.GONE

        val defaultBlue = ContextCompat.getColor(this, R.color.material_blue2)

        // Aggiungi il bordo durante il countdown
        contatoriCard.strokeWidth =
            resources.getDimensionPixelSize(R.dimen.countdown_stroke_width) // 2dp
        contatoriCard.setStrokeColor(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    this,
                    R.color.material_blue
                )
            )
        )

        spacerView.visibility = View.VISIBLE

        // Quando l'attività viene avviata, espandi statsContainer
        Handler(Looper.getMainLooper()).postDelayed({
            expandStatsContainer()
        }, 500)


        txtDistanza.setTextColor(defaultBlue)
        txtTempo.setTextColor(defaultBlue)
        txtTempoSeconds.setTextColor(defaultBlue)
        txtPassi.setTextColor(defaultBlue)
        txtVelocitaAttuale.setTextColor(defaultBlue)
        txtVelocitaMedia.setTextColor(defaultBlue)
        txtVolteSeduto.setTextColor(defaultBlue)
        txtPaceMedio.setTextColor(defaultBlue)

        findViewById<TextView>(R.id.labelPassi)?.setTextColor(defaultBlue)
        findViewById<TextView>(R.id.labelTempo)?.setTextColor(defaultBlue)
        findViewById<TextView>(R.id.labelVelocitaAttuale)?.setTextColor(defaultBlue)
        findViewById<TextView>(R.id.labelVelocitaAttualeSub)?.setTextColor(defaultBlue)
        findViewById<TextView>(R.id.labelVelocitaMedia)?.setTextColor(defaultBlue)
        findViewById<TextView>(R.id.labelVelocitaMediaSub)?.setTextColor(defaultBlue)
        findViewById<TextView>(R.id.labelPaceMedio)?.setTextColor(defaultBlue)
        findViewById<TextView>(R.id.labelPaceMedioSub)?.setTextColor(defaultBlue)
        findViewById<TextView>(R.id.labelVolteSeduto)?.setTextColor(defaultBlue)
        findViewById<TextView>(R.id.labelDistanza)?.setTextColor(defaultBlue)

        // Imposta il background trasparente e i margini a 0
        val bestRecordsLayout = findViewById<LinearLayout>(R.id.RecordsLayout)
        bestRecordsLayout.setBackgroundResource(R.drawable.transparent_background)
        val layoutParams = bestRecordsLayout.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(0, 0, 0, 0)
        bestRecordsLayout.layoutParams = layoutParams

        updateLabelColors(defaultBlue)
        updateIconColors(defaultBlue)

        findViewById<MaterialCardView>(R.id.contatoriCard)?.let { card ->
            card.post {
                // Imposta il bordo blu durante il countdown
                card.strokeWidth =
                    resources.getDimensionPixelSize(R.dimen.countdown_stroke_width) // 2dp
                card.setStrokeColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this,
                            R.color.material_blue2
                        )
                    )
                )
                val cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
                val backgroundColor = ColorUtils.setAlphaComponent(Color.WHITE, 8)
                val bgDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    this.cornerRadius = cornerRadius
                    setColor(backgroundColor)
                }

                // Crea il layer superiore con il bordo blu
                val topDrawable = GradientDrawable()
                topDrawable.shape = GradientDrawable.RECTANGLE
                topDrawable.cornerRadius = cornerRadius
                topDrawable.setColor(
                    ColorUtils.setAlphaComponent(
                        ContextCompat.getColor(
                            this,
                            R.color.material_blue
                        ), 8
                    )
                )
                topDrawable.setStroke(2, ContextCompat.getColor(this, R.color.material_blue))

                val layers = arrayOf(bgDrawable, topDrawable)
                val layerDrawable = LayerDrawable(layers)

                val percentage = 0.8f  // 75% della larghezza
                val startPosition = 0
                val endOffset = (card.width * (1 - percentage)).toInt()
                layerDrawable.setLayerInset(1, startPosition, 0, endOffset, 0)

                // Imposta il background della CardView
                card.background = layerDrawable
            }
        }

        // Applica l'effetto di sfocatura solo a statsLayout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contatoriCard.setRenderEffect(
                RenderEffect.createBlurEffect(
                    12f,
                    12f,
                    Shader.TileMode.DECAL
                )
            )
        }

        when (attivitàDaAvviare) {
            "Camminare" -> {
                findViewById<LinearLayout>(R.id.layoutPassi).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutDistanza).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutTempo).visibility = View.VISIBLE
            }

            "Sedersi" -> {
                findViewById<LinearLayout>(R.id.layoutVolteSeduto).visibility = View.VISIBLE
            }

            "Corsa" -> {
                findViewById<LinearLayout>(R.id.layoutDistanza).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutTempo).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutVelocitaAttuale).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutVelocitaMedia).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutPaceMedio).visibility = View.VISIBLE

            }

            "Guidare" -> {
                findViewById<LinearLayout>(R.id.layoutDistanza).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutTempo).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutVelocitaAttuale).visibility = View.VISIBLE
            }
            "Bicicletta" -> {
                findViewById<LinearLayout>(R.id.layoutDistanza).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutTempo).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.layoutVelocitaAttuale).visibility = View.VISIBLE
            }
        }
        fabInterrompi.visibility = View.VISIBLE

        countdownTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isCountdownRunning) return

                val secondsLeft = millisUntilFinished / 1000
                if (secondsLeft > 0) {
                    countdownText.text = secondsLeft.toString()
                } else {
                    countdownText.text = "VIA!"
                }
            }

            override fun onFinish() {
                if (!isCountdownRunning) return

                isCountdownRunning = false
                countdownText.visibility = View.GONE
                statsLayout.visibility = View.VISIBLE
                contatoriCard.strokeWidth = resources.getDimensionPixelSize(R.dimen.normal_stroke_width)
                contatoriCard.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#20000000")))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    contatoriCard.setRenderEffect(null)
                }

                attivitàDaAvviare?.let { startActivity(it) }
            }
        }.start()
    }

    private var isAutoScrolling = false


    private fun performHapticFeedback(feedbackType: Int = HapticFeedbackConstants.KEYBOARD_TAP) {
        if (!isAutoScrolling) {
            val view = window.decorView
            view.performHapticFeedback(
                feedbackType,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }


    private fun animateCardDisappearance() {
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenHeight = displayMetrics.heightPixels

        // Nascondi subito lo switch e i bottoni in basso
        switchAutomaticActivity.alpha = 0f
        fabAdd.visibility = View.GONE

        statsContainer.animate()
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                // Dopo che statsContainer è scomparso, fai cadere la card
                cardContainer.animate()
                    .translationY(screenHeight.toFloat())
                    .alpha(0.8f)
                    .setDuration(500)
                    .setInterpolator(AccelerateInterpolator(1.5f))
                    .rotationX(-10f)
                    .withEndAction {
                        cardContainer.translationY = 0f
                        cardContainer.alpha = 1f
                        cardContainer.rotationX = 0f
                        statsContainer.visibility = View.GONE

                        cardContainer.visibility = View.GONE
                        contatoriCard.visibility = View.GONE

                        // Mostra e anima lo switch
                        switchAutomaticActivity.visibility = View.VISIBLE
                        switchAutomaticActivity.alpha = 0f
                        switchAutomaticActivity.translationY = 50f


                        animateBottomButtons()

                        resetAllViews()
                    }
                    .start()
            }
            .start()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }


    private fun animateBottomButtons(fromSwitch: Boolean = false) {
        val bottomCard = findViewById<MaterialCardView>(R.id.bottomButtonsCard)
        val btnStorico = findViewById<ImageButton>(R.id.btnStorico)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        bottomButtonsCard.visibility = View.VISIBLE

        if (!fromSwitch) {
            switchAutomaticActivity.visibility = View.VISIBLE
            switchAutomaticActivity.alpha = 0f
            switchAutomaticActivity.translationY = 50f
        }

        bottomCard.visibility = View.VISIBLE
        bottomCard.alpha = 0f
        bottomCard.translationY = 100f
        btnStorico.alpha = 0f
        btnSettings.alpha = 0f
        fabAdd.alpha = 0f

        if (fromSwitch) {
            statsContainer.visibility = View.VISIBLE
            statsContainer.alpha = 0f
            statsContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    bottomCard.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction {
                            btnStorico.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .setInterpolator(DecelerateInterpolator())
                                .withEndAction {
                                    btnSettings.animate()
                                        .alpha(1f)
                                        .setDuration(200)
                                        .setInterpolator(DecelerateInterpolator())
                                        .withEndAction {
                                            fabAdd.animate()
                                                .alpha(1f)
                                                .setDuration(300)
                                                .setInterpolator(DecelerateInterpolator())
                                                .start()
                                        }
                                        .start()
                                }
                                .start()
                        }
                        .start()
                }
                .start()
        } else {
            switchAutomaticActivity.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    statsContainer.visibility = View.VISIBLE
                    statsContainer.alpha = 0f
                    statsContainer.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction {
                            bottomCard.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(400)
                                .setInterpolator(DecelerateInterpolator())
                                .withEndAction {
                                    btnStorico.animate()
                                        .alpha(1f)
                                        .setDuration(200)
                                        .setInterpolator(DecelerateInterpolator())
                                        .withEndAction {
                                            btnSettings.animate()
                                                .alpha(1f)
                                                .setDuration(200)
                                                .setInterpolator(DecelerateInterpolator())
                                                .withEndAction {
                                                    fabAdd.animate()
                                                        .alpha(1f)
                                                        .setDuration(300)
                                                        .setInterpolator(DecelerateInterpolator())
                                                        .start()
                                                }
                                        }
                                        .start()
                                }
                                .start()
                        }
                        .start()
                }
                .start()
        }
    }



    private fun setupBestRecords() {
        lifecycleScope.launch {
            val bestRun = withContext(Dispatchers.IO) {
                db.attivitàDao().getBestRunByDistance()
            }
            val bestWalk = withContext(Dispatchers.IO) {
                db.attivitàDao().getBestWalkBySteps()
            }
            val bestDrive = withContext(Dispatchers.IO) {
                db.attivitàDao().getBestDriveByDistance()
            }

            updateBestRunCard(bestRun)
            updateBestWalkCard(bestWalk)
            updateBestDriveCard(bestDrive)
        }
    }

    private fun updateBestRunCard(activity: Attività?) {
        activity?.let {
            val distance = it.distanza ?: 0f
            // Verifica che la data non sia il default (1970)
            if (it.oraInizio > 86400000) { // 24 ore in millisecondi dal 1970
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(it.oraInizio))

                findViewById<TextView>(R.id.bestRunValue).text =
                    String.format("%.2f km", distance/1000)
                findViewById<TextView>(R.id.bestRunDate).text = date
            } else {
                // Se non c'è un record valido, mostra un testo di default
                findViewById<TextView>(R.id.bestRunValue).text = "Nessun record"
                findViewById<TextView>(R.id.bestRunDate).text = "---"
            }
        } ?: run {
            // Se activity è null, mostra un testo di default
            findViewById<TextView>(R.id.bestRunValue).text = "Nessun record"
            findViewById<TextView>(R.id.bestRunDate).text = "---"
        }
    }

    private fun updateBestWalkCard(activity: Attività?) {
        activity?.let {
            val steps = it.passi ?: 0
            if (it.oraInizio > 86400000) {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(it.oraInizio))

                findViewById<TextView>(R.id.bestWalkValue).text = "$steps passi"
                findViewById<TextView>(R.id.bestWalkDate).text = date
            } else {
                findViewById<TextView>(R.id.bestWalkValue).text = "Nessun record"
                findViewById<TextView>(R.id.bestWalkDate).text = "---"
            }
        } ?: run {
            findViewById<TextView>(R.id.bestWalkValue).text = "Nessun record"
            findViewById<TextView>(R.id.bestWalkDate).text = "---"
        }
    }

    private fun updateBestDriveCard(activity: Attività?) {
        activity?.let {
            val distance = it.distanza ?: 0f
            if (it.oraInizio > 86400000) {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(it.oraInizio))

                findViewById<TextView>(R.id.bestDriveValue).text =
                    String.format("%.2f km", distance / 1000)
                findViewById<TextView>(R.id.bestDriveDate).text = date
            } else {
                findViewById<TextView>(R.id.bestDriveValue).text = "Nessun record"
                findViewById<TextView>(R.id.bestDriveDate).text = "---"
            }
        } ?: run {
            findViewById<TextView>(R.id.bestDriveValue).text = "Nessun record"
            findViewById<TextView>(R.id.bestDriveDate).text = "---"
        }
    }

    private fun updateStatsVisualsForActivity(activityType: String) {
        val color = when (activityType) {
            "Camminare" -> ContextCompat.getColor(this, R.color.colorCamminare)
            "Corsa" -> ContextCompat.getColor(this, R.color.colorCorrere)
            "Guidare" -> ContextCompat.getColor(this, R.color.colorGuidare)
            "Bicicletta" -> ContextCompat.getColor(this, R.color.colorBici)
            "Sedersi" -> ContextCompat.getColor(this, R.color.colorSedersi)
            else -> ContextCompat.getColor(this, R.color.material_blue)
        }

        val darkerColor = ColorUtils.blendARGB(color, Color.BLACK, 0.3f)

        val lighterColor = ColorUtils.blendARGB(color, Color.WHITE, 0.7f)

        val homeContainer = findViewById<FrameLayout>(R.id.homeContainer)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnStorico = findViewById<ImageButton>(R.id.btnStorico)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)


        val backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.custum_circle_main_back)?.mutate()
        backgroundDrawable?.setTint(darkerColor)
        homeContainer.background = backgroundDrawable


        btnHome.setColorFilter(lighterColor)

        btnStorico.setColorFilter(darkerColor)
        btnSettings.setColorFilter(darkerColor)

        val lightBorderColor = ColorUtils.setAlphaComponent(color, 40)
        val darkBorderColor = ColorUtils.setAlphaComponent(color, 60)
        val lightBackgroundColor = ColorUtils.setAlphaComponent(color, 10)
        val darkBackgroundColor = ColorUtils.setAlphaComponent(color, 12)

        findViewById<MaterialCardView>(R.id.contatoriCard)?.let { card ->

            card.strokeWidth = 0

            val cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
            val layers = arrayOf(
                // Layer di base con sfondo chiaro per tutta la card
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    this.cornerRadius = cornerRadius
                    setColor(lightBackgroundColor)
                    setStroke(8, lightBorderColor)
                },
                // Layer per la sezione destra
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    // Imposta i corner radius con angoli esterni a sinistra
                    cornerRadii = floatArrayOf(
                        cornerRadius, cornerRadius,
                        cornerRadius, cornerRadius,
                        cornerRadius, cornerRadius,
                        cornerRadius, cornerRadius
                    )
                    setColor(darkBackgroundColor)
                    setStroke(8, darkBorderColor)
                }
            )


            val layerDrawable = LayerDrawable(layers)
            val percentage = 0.75f  // 70% della larghezza
            val startPosition = 0
            val endOffset = 800
            layerDrawable.setLayerInset(1, startPosition, 0, card.width - endOffset, 0)

            card.background = layerDrawable

        }

        // Aggiorna colore di bottomButtonsLayout
        val bottomButtonsLayout = findViewById<LinearLayout>(R.id.bottomButtonsLayout)
        bottomButtonsLayout.setBackgroundColor(color)
        updateLabelColors(color)
        updateIconColors(color)

    }

    private fun updateLabelColors(color: Int) {
        // Aggiorna le etichette principali
        val labelIds = listOf(
            R.id.labelPassi,
            R.id.labelTempo,
            R.id.labelVelocitaAttuale,
            R.id.labelVelocitaMedia,
            R.id.labelVolteSeduto,
            R.id.labelDistanza,
            R.id.labelPaceMedio
        )

        val subLabelIds = listOf(
            R.id.labelVelocitaAttualeSub,
            R.id.labelVelocitaMediaSub,
            R.id.labelPaceMedio,
            R.id.labelPaceMedioSub,

            )

        labelIds.forEach { id ->
            findViewById<TextView>(id)?.setTextColor(color)
        }

        subLabelIds.forEach { id ->
            findViewById<TextView>(id)?.setTextColor(color)
        }
    }

    private fun updateIconColors(color: Int) {
        val icons = listOf(
            R.id.iconDistanza,
            R.id.iconTempo,
            R.id.iconPassi,
            R.id.iconVelocitaAttuale,
            R.id.iconVelocitaMedia,
            R.id.iconVolteSeduto,
            R.id.iconPaceMedio

        )

        icons.forEach { iconId ->
            findViewById<ImageView>(iconId)?.setColorFilter(color)
        }
    }



    // Funzione per centrare il bottone con animazione
    private fun centerButtonInScrollView(button: MaterialButton) {
        val scrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
        val buttonLayout = findViewById<LinearLayout>(R.id.buttonLayout)

        val leftSpacer = findViewById<View>(R.id.leftSpacer)
        val rightSpacer = findViewById<View>(R.id.rightSpacer)


        // Converti i dp in pixel
        val spaceInDp = 100
        val scale = resources.displayMetrics.density
        val spaceInPx = (spaceInDp * scale).toInt()

        if (button == btnCamminare) {
            leftSpacer.layoutParams = leftSpacer.layoutParams.apply {
                width = spaceInPx
            }
            leftSpacer.visibility = View.VISIBLE
            rightSpacer.layoutParams = rightSpacer.layoutParams.apply {
                width = 0
            }
            rightSpacer.visibility = View.GONE

        } else if (button == btnSedersi) {
            rightSpacer.layoutParams = rightSpacer.layoutParams.apply {
                width = spaceInPx
            }
            rightSpacer.visibility = View.VISIBLE
            leftSpacer.layoutParams = leftSpacer.layoutParams.apply {
                width = 0
            }
            leftSpacer.visibility = View.GONE
        } else {
            leftSpacer.layoutParams = leftSpacer.layoutParams.apply {
                width = 0
            }
            leftSpacer.visibility = View.GONE
            rightSpacer.layoutParams = rightSpacer.layoutParams.apply {
                width = 0
            }
            rightSpacer.visibility = View.GONE
        }

        scrollView.post {
            val screenWidth = scrollView.width
            val buttonWidth = button.width
            val layoutWidth = buttonLayout.width

            val buttonLeft = button.left
            val scrollTarget = buttonLeft - (screenWidth / 2) + (buttonWidth / 2)

            val maxScroll = layoutWidth - screenWidth
            val adjustedScrollTarget = scrollTarget.coerceIn(0, maxScroll)

            val animator = ValueAnimator.ofInt(scrollView.scrollX, adjustedScrollTarget)
            animator.duration = 300
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animation ->
                scrollView.scrollTo(animation.animatedValue as Int, 0)
            }
            animator.start()

            findViewById<View>(R.id.View_1).visibility = View.GONE
            findViewById<View>(R.id.View_2).visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({
                findViewById<View>(R.id.View_1).visibility = View.VISIBLE
                findViewById<View>(R.id.View_2).visibility = View.VISIBLE
            }, 300)
        }
    }


    private fun disableHorizontalScroll() {
        val scrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
        scrollView.setOnTouchListener { _, _ -> true }
        scrollView.isHorizontalScrollBarEnabled = false
    }

    private fun enableHorizontalScroll() {
        val scrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
        scrollView.setOnTouchListener(null)
        scrollView.isHorizontalScrollBarEnabled = true
    }

    private fun resetButtonsPositionWithAnimation() {
        val scrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
        val buttonLayout = findViewById<LinearLayout>(R.id.buttonLayout)
        val view1 = findViewById<View>(R.id.View_1)
        val view2 = findViewById<View>(R.id.View_2)

        val leftSpacer = findViewById<View>(R.id.leftSpacer)
        val rightSpacer = findViewById<View>(R.id.rightSpacer)

        // Resetta le dimensioni di entrambi gli spacer
        leftSpacer.layoutParams = (leftSpacer.layoutParams).apply {
            width = 0
        }
        rightSpacer.layoutParams = (rightSpacer.layoutParams).apply {
            width = 0
        }

        leftSpacer.visibility = View.GONE
        rightSpacer.visibility = View.GONE

        val scrollAnimator = ValueAnimator.ofInt(scrollView.scrollX, 0)
        scrollAnimator.duration = 300
        scrollAnimator.interpolator = DecelerateInterpolator()

        scrollAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            scrollView.scrollTo(animatedValue, 0)
        }

        scrollAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                lastClickedButton = null
                enableHorizontalScroll()
                listOf(btnCamminare, btnCorrere, btnGuidare, btnBicicletta, btnSedersi).forEach { button ->
                    button.iconPadding = 2.dpToPx(this@MainActivity)
                }
            }
        })

        scrollAnimator.start()
    }

    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

    private fun Int.dpToPx1(): Int =
        (this * this@MainActivity.resources.displayMetrics.density).toInt()

    private fun smoothScrollToTop(duration: Long = 1000) {
        val scrollView = findViewById<NestedScrollView>(R.id.scrollView)
        val targetY = 0
        val startY = scrollView.scrollY
        val animator = ValueAnimator.ofInt(startY, targetY)
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            scrollView.scrollTo(0, value)
        }
        animator.start()
    }


    private fun animateSwitchDisappearance() {
        ActivityStateManager.isSwitchActive = false

        stopAutomaticTrackingService()

        ActivityStateManager.apply {
            currentActivityType = DetectedActivity.UNKNOWN
            activityStartTime = 0L
        }
        previousActivityType = DetectedActivity.UNKNOWN
        ActivityStateManager.currentActivityType = DetectedActivity.UNKNOWN
        ActivityStateManager.orientationServiceStarted = false
        previousActivityType = DetectedActivity.UNKNOWN
        txtRecognizedActivity.text = "sto pensando..."
        imgRecognizedActivity.setImageResource(R.drawable.psychology_alt)
        imgRecognizedActivity.setColorFilter(
            ContextCompat.getColor(this, R.color.material_blue2)
        )

        val txtPrimary = findViewById<TextView>(R.id.txtAutomaticStatsPrimary)
        val txtSecondary = findViewById<TextView>(R.id.txtAutomaticStatsSecondary)
        val txtTertiary = findViewById<TextView>(R.id.txtAutomaticStatsTertiary)
        val layoutStats = findViewById<LinearLayout>(R.id.layoutAutomaticActivityStats)

        val fadeAnimation = AlphaAnimation(1f, 0f).apply {
            duration = 200
            fillAfter = true
        }

        txtPrimary.startAnimation(fadeAnimation)
        txtSecondary.startAnimation(fadeAnimation)
        txtTertiary.startAnimation(fadeAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            txtPrimary.text = ""
            txtSecondary.text = ""
            txtTertiary.text = ""
            layoutStats.visibility = View.GONE
        }, 200)

        statsContainer.animate()
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                // Reset dell'icona e del testo
                imgRecognizedActivity.apply {
                    setImageResource(R.drawable.psychology_alt)
                    setColorFilter(ContextCompat.getColor(context, R.color.material_blue2))
                    scaleX = 1f
                    scaleY = 1f
                    alpha = 0f
                    visibility = View.GONE
                }

                txtRecognizedActivity.apply {
                    visibility = View.GONE
                }

                statsContainer.visibility = View.GONE

                switchAutomaticActivity.isChecked = false

                fabInterrompi.visibility = View.GONE

                animateBottomButtons(fromSwitch = true)

                val view1 = findViewById<View>(R.id.View_1)
                val view2 = findViewById<View>(R.id.View_2)

                // Mostra solo view1 quando lo switch viene disattivato
                view2.visibility = View.GONE
                view2.alpha = 1f
                view1.visibility = View.VISIBLE
                view1.alpha = 1f

                val fadeViewAnimator = ValueAnimator.ofInt(0, resources.getDimensionPixelSize(R.dimen.fade_view_height)).apply {
                    duration = 100
                    addUpdateListener { animator ->
                        val value = animator.animatedValue as Int
                        view1.layoutParams = view1.layoutParams.apply {
                            height = value
                            width = resources.getDimensionPixelSize(R.dimen.fade_view_width)
                        }
                    }
                    start()
                }
            }
            .start()

        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }


    private fun animaBottoniInStatoMinimo(attivita: String) {
        val bottoni = listOf(btnCamminare, btnCorrere, btnGuidare,btnBicicletta, btnSedersi)
        val altezzaMinima = resources.getDimensionPixelSize(R.dimen.minimal_button_height)
        val larghezzaMinima = resources.getDimensionPixelSize(R.dimen.minimal_button_width)

        // Nascondi le view di fade completamente
        val view1 = findViewById<View>(R.id.View_1)
        val view2 = findViewById<View>(R.id.View_2)

        // Anima le dimensioni delle view a 0
        val fadeViewAnimator = ValueAnimator.ofInt(view1.height, 0).apply {
            duration = 500
            addUpdateListener { animator ->
                val value = animator.animatedValue as Int
                view1.layoutParams = view1.layoutParams.apply {
                    height = value
                    width = 0
                }
                view2.layoutParams = view2.layoutParams.apply {
                    height = value
                    width = 0
                }
            }
            doOnEnd {
                view1.visibility = View.GONE
                view2.visibility = View.GONE
            }
        }
        fadeViewAnimator.start()


        val coloreSfondoAttivo = when (attivita) {
            "Camminare" -> ContextCompat.getColor(this, R.color.colorCamminare)
            "Corsa" -> ContextCompat.getColor(this, R.color.colorCorrere)
            "Guidare" -> ContextCompat.getColor(this, R.color.colorGuidare)
            "Bicicletta" -> ContextCompat.getColor(this, R.color.colorBici)
            "Sedersi" -> ContextCompat.getColor(this, R.color.colorSedersi)
            else -> ContextCompat.getColor(this, R.color.material_blue)
        }

        // Determina il colore del testo in base al tema
        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val coloreTesto = if (isDarkMode) Color.parseColor("#E0E0E0") else Color.WHITE



        bottoni.forEach { button ->
            val isButtonAttivo = when (button) {
                btnCamminare -> attivita == "Camminare"
                btnCorrere -> attivita == "Corsa"
                btnGuidare -> attivita == "Guidare"
                btnBicicletta -> attivita == "Bicicletta"
                btnSedersi -> attivita == "Sedersi"
                else -> false
            }
            val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
            horizontalScrollView.setOnTouchListener { _, _ -> true }

            if (isButtonAttivo) {
                button.visibility = View.VISIBLE
                val set = AnimatorSet()

                // Animazione altezza e larghezza
                val animazioneAltezza = ValueAnimator.ofInt(button.height, altezzaMinima).apply {
                    addUpdateListener { animator ->
                        val params = button.layoutParams
                        params.height = animator.animatedValue as Int
                        button.layoutParams = params
                    }
                }
                val animazioneLarghezza = ValueAnimator.ofInt(button.width, larghezzaMinima).apply {
                    addUpdateListener { animator ->
                        val params = button.layoutParams
                        params.width = animator.animatedValue as Int
                        button.layoutParams = params
                    }
                }

                val animazioneIcona = ObjectAnimator.ofInt(button, "iconSize", button.iconSize, 0)
                val animazioneColore = ValueAnimator.ofArgb(Color.TRANSPARENT, coloreSfondoAttivo).apply {
                    addUpdateListener { animator ->
                        button.setBackgroundColor(animator.animatedValue as Int)
                        button.setTextColor(coloreTesto)
                        button.strokeWidth = 0
                    }
                }

                set.playTogether(
                    animazioneAltezza,
                    animazioneLarghezza,
                    animazioneIcona,
                    animazioneColore
                )

                set.duration = 500
                set.interpolator = FastOutSlowInInterpolator()


                set.doOnEnd {
                    button.icon = null
                    button.iconPadding = 0
                    button.setPadding(16, 0, 16, 0)
                }

                set.start()
            } else {
                val animazioneAltezza = ValueAnimator.ofInt(button.height, altezzaMinima).apply {
                    addUpdateListener { animator ->
                        val params = button.layoutParams
                        params.height = animator.animatedValue as Int
                        button.layoutParams = params
                    }
                }
                animazioneAltezza.duration = 500
                animazioneAltezza.start()
                button.visibility = View.INVISIBLE

            }
        }

        view1.visibility = View.GONE
        view2.visibility = View.GONE
    }

    private fun getActivityColor(activityType: String): Int {
        val isDynamicTheme = prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        return if (isDynamicTheme) {
            // Usa i colori del tema dinamico
            when (activityType) {
                "Camminare" -> MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary,
                    ContextCompat.getColor(this, R.color.colorCamminare))
                "Corsa" -> MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary,
                    ContextCompat.getColor(this, R.color.colorCorrere))
                "Guidare" -> MaterialColors.getColor(this, com.google.android.material.R.attr.colorTertiary,
                    ContextCompat.getColor(this, R.color.colorGuidare))
                "Bicicletta" -> MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary,
                    ContextCompat.getColor(this, R.color.colorBici))
                "Sedersi" -> MaterialColors.getColor(this, com.google.android.material.R.attr.colorTertiary,
                    ContextCompat.getColor(this, R.color.colorSedersi))
                else -> MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary,
                    ContextCompat.getColor(this, R.color.material_blue))
            }
        } else {
            // Usa i colori statici
            when (activityType) {
                "Camminare" -> ContextCompat.getColor(this, R.color.colorCamminare)
                "Corsa" -> ContextCompat.getColor(this, R.color.colorCorrere)
                "Guidare" -> ContextCompat.getColor(this, R.color.colorGuidare)
                "Bicicletta" -> ContextCompat.getColor(this, R.color.colorBici)
                "Sedersi" -> ContextCompat.getColor(this, R.color.colorSedersi)
                else -> ContextCompat.getColor(this, R.color.material_blue)
            }
        }
    }

    private fun getBackgroundColor(): Int {
        val isDynamicTheme = prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        return if (isDynamicTheme) {
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceVariant,
                ContextCompat.getColor(this, R.color.rossino))
        } else {
            ContextCompat.getColor(this, R.color.rossino)
        }
    }

    private fun ripristinaBottoni() {
        val bottoni = listOf(btnCamminare, btnCorrere, btnGuidare, btnBicicletta, btnSedersi)
        val altezzaOriginale = resources.getDimensionPixelSize(R.dimen.original_button_height)

        // Ripristina le view
        val view1 = findViewById<View>(R.id.View_1)
        val view2 = findViewById<View>(R.id.View_2)

        val fadeViewAnimator = ValueAnimator.ofInt(0, resources.getDimensionPixelSize(R.dimen.fade_view_height)).apply {
            duration = 300
            addUpdateListener { animator ->
                val value = animator.animatedValue as Int
                view1.layoutParams = view1.layoutParams.apply {
                    height = value
                    width = resources.getDimensionPixelSize(R.dimen.fade_view_width)
                }
                view2.layoutParams = view2.layoutParams.apply {
                    height = value
                    width = resources.getDimensionPixelSize(R.dimen.fade_view_width)
                }
            }
            doOnEnd {
                val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
                val buttonLayout = findViewById<LinearLayout>(R.id.buttonLayout)
                val maxScroll = buttonLayout.width - horizontalScrollView.width

                when {
                    horizontalScrollView.scrollX <= 0 -> {
                        view2.visibility = View.GONE
                        view1.visibility = View.VISIBLE
                    }
                    horizontalScrollView.scrollX >= maxScroll -> {
                        view1.visibility = View.GONE
                        view2.visibility = View.VISIBLE
                    }
                    else -> {
                        view1.visibility = View.VISIBLE
                        view2.visibility = View.VISIBLE
                    }
                }
            }
        }
        fadeViewAnimator.start()

        bottoni.forEach { button ->
            val set = AnimatorSet()

            val animazioneAltezza = ValueAnimator.ofInt(button.height, altezzaOriginale).apply {
                addUpdateListener { animator ->
                    val params = button.layoutParams
                    params.height = animator.animatedValue as Int
                    button.layoutParams = params
                }
            }
            val animazioneIcona = ObjectAnimator.ofInt(button, "iconSize", 0,
                resources.getDimensionPixelSize(R.dimen.button_icon_size))

            set.playTogether(
                animazioneAltezza,
                animazioneIcona
            )

            set.duration = 300
            set.interpolator = FastOutSlowInInterpolator()

            set.doOnStart {
                button.visibility = View.VISIBLE
                button.icon = when (button) {
                    btnCamminare -> ContextCompat.getDrawable(this, R.drawable.footprint_24px)
                    btnCorrere -> ContextCompat.getDrawable(this, R.drawable.directions_run_24px)
                    btnGuidare -> ContextCompat.getDrawable(this, R.drawable.directions_car)
                    btnBicicletta -> ContextCompat.getDrawable(this, R.drawable.directions_bike)
                    btnSedersi -> ContextCompat.getDrawable(this, R.drawable.seat_24px)
                    else -> null
                }
                button.iconPadding = resources.getDimensionPixelSize(R.dimen.button_icon_padding)
            }

            set.doOnEnd {
                button.iconPadding = 2.dpToPx(this)
                button.isClickable = true

                // Imposta il colore del testo in base al tema
                val textColor = when (button) {
                    btnCamminare -> getActivityColor("Camminare")
                    btnCorrere -> getActivityColor("Corsa")
                    btnGuidare -> getActivityColor("Guidare")
                    btnBicicletta -> getActivityColor("Bicicletta")
                    btnSedersi -> getActivityColor("Sedersi")
                    else -> getActivityColor("")
                }
                button.setTextColor(textColor)
                button.iconTint = ColorStateList.valueOf(textColor)

                // Imposta il colore di sfondo
                button.setBackgroundColor(getBackgroundColor())
            }

            set.start()
        }

        findViewById<LinearLayout>(R.id.RecordsLayout).apply {
            visibility = View.VISIBLE
            alpha = 0f

            // Imposta il background in base al tema
            if (prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                background = GradientDrawable().apply {
                    cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
                    setColor(MaterialColors.getColor(this@MainActivity,
                        com.google.android.material.R.attr.colorSurfaceVariant,
                        ContextCompat.getColor(this@MainActivity, R.color.rossino)))
                }
            } else {
                setBackgroundResource(R.drawable.rounded_background)
            }

            val params = layoutParams as ViewGroup.MarginLayoutParams
            layoutParams = params
            animate()
                .alpha(1f)
                .setDuration(100)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    fun animateBottoniPerRiconoscimento() {
        findViewById<LinearLayout>(R.id.RecordsLayout).let { recordsLayout ->
            recordsLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    recordsLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    (recordsLayout.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        marginStart = 0
                        marginEnd = 0
                    }
                    recordsLayout.layoutParams = recordsLayout.layoutParams

                    findViewById<HorizontalScrollView>(R.id.horizontalScrollView1).apply {
                        (layoutParams as ViewGroup.MarginLayoutParams).apply {
                            marginStart = 0
                            marginEnd = 0
                        }
                        layoutParams = layoutParams
                    }
                }
            })
        }

        val bottoni = listOf(btnCamminare, btnCorrere, btnGuidare, btnBicicletta, btnSedersi)
        val altezzaMinima = resources.getDimensionPixelSize(R.dimen.minimal_button_height)
        val larghezzaMinima = resources.getDimensionPixelSize(R.dimen.minimal_button_width)

        // Determina se il tema dinamico è attivo
        val isDynamicTheme = prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        // Determina il colore del testo in base al tema
        val coloreTesto = if (isDynamicTheme) {
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary, Color.WHITE)
        } else {
            Color.WHITE
        }

        bottoni.forEach { button ->
            // Determina il colore di sfondo per ogni bottone in base al tema
            val coloreSfondo = if (isDynamicTheme) {
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary,
                    ContextCompat.getColor(this, R.color.colore_fab))
            } else {
                ContextCompat.getColor(this, R.color.colore_fab)
            }

            val set = AnimatorSet()

            val animazioneAltezza = ValueAnimator.ofInt(button.height, altezzaMinima).apply {
                addUpdateListener { animator ->
                    val params = button.layoutParams
                    params.height = animator.animatedValue as Int
                    button.layoutParams = params
                }
            }

            val animazioneLarghezza = ValueAnimator.ofInt(button.width, larghezzaMinima).apply {
                addUpdateListener { animator ->
                    val params = button.layoutParams
                    params.width = animator.animatedValue as Int
                    button.layoutParams = params
                }
            }

            // Nascondi le view
            val view1 = findViewById<View>(R.id.View_1)
            val view2 = findViewById<View>(R.id.View_2)

            val fadeViewAnimator = ValueAnimator.ofInt(view1.height, 0).apply {
                duration = 500
                addUpdateListener { animator ->
                    val value = animator.animatedValue as Int
                    view1.layoutParams = view1.layoutParams.apply {
                        height = value
                        width = 0
                    }
                    view2.layoutParams = view2.layoutParams.apply {
                        height = value
                        width = 0
                    }
                }
                doOnEnd {
                    view1.visibility = View.GONE
                    view2.visibility = View.GONE
                }
            }
            fadeViewAnimator.start()

            val animazioneIcona = ObjectAnimator.ofInt(button, "iconSize", button.iconSize, 0)

            val animazioneColore = ValueAnimator.ofArgb(
                Color.TRANSPARENT,
                coloreSfondo
            ).apply {
                addUpdateListener { animator ->
                    button.setBackgroundColor(animator.animatedValue as Int)
                    button.setTextColor(coloreTesto)
                    button.strokeWidth = 0
                }
            }

            set.playTogether(
                animazioneAltezza,
                animazioneLarghezza,
                animazioneIcona,
                animazioneColore
            )

            set.duration = 300
            set.interpolator = FastOutSlowInInterpolator()

            set.doOnStart {
                button.visibility = View.VISIBLE
            }

            set.doOnEnd {
                button.icon = null
                button.iconPadding = 0
                button.setPadding(0, 0, 0, 0)
                button.isClickable = false
            }

            set.start()
        }

        // Nascondi i separatori
        view1.visibility = View.GONE
        view2.visibility = View.GONE
    }


    // Funzione per centrare il bottone dell'attività rilevata
    fun centraBottoneAttivitaRilevata(activityType: Int) {
        val bottone = when (activityType) {
            DetectedActivity.WALKING, DetectedActivity.ON_FOOT -> btnCamminare
            DetectedActivity.RUNNING -> btnCorrere
            DetectedActivity.IN_VEHICLE -> btnGuidare
            DetectedActivity.ON_BICYCLE -> btnBicicletta
            DetectedActivity.STILL, DetectedActivity.TILTING -> btnSedersi
            else -> null
        }

        bottone?.let {
            val scrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
            val buttonPos = IntArray(2)
            it.getLocationOnScreen(buttonPos)
            val buttonCenter = buttonPos[0] + it.width / 2
            val screenCenter = scrollView.width / 2
            val scrollTo = scrollView.scrollX + (buttonCenter - screenCenter)

            val animator = ValueAnimator.ofInt(scrollView.scrollX, scrollTo)
            animator.duration = 300
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animation ->
                scrollView.scrollTo(animation.animatedValue as Int, 0)
            }
            animator.start()
        }
    }

    private fun updateMapPath(pathCoordinates: List<LatLng>) {
        if (::mMap.isInitialized) {
            if (polyline == null) {
                // Crea una nuova Polyline
                val polylineOptions = PolylineOptions()
                    .color(ContextCompat.getColor(this, R.color.colorCorrere))
                    .width(15f)
                    .addAll(pathCoordinates)
                polyline = mMap.addPolyline(polylineOptions)
            } else {
                // Aggiorna la Polyline esistente
                polyline?.points = pathCoordinates
            }

            // Muovi la camera alla posizione più recente
            val lastPosition = pathCoordinates.last()
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 15f))
        }
    }

    private fun updateActivityStats() {
        statsManager.aggiornaStatisticheGiornaliere()
        statsManager.aggiornaStatisticheSettimanali()


        CoroutineScope(Dispatchers.IO).launch {
            // calcolo inizio/fine giorno, inizio settimana
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val startOfWeek = calendar.timeInMillis

            val attivitaOggi = db.attivitàDao().getAttivitàByDate(startOfDay)
            val attivitaSettimanali = db.attivitàDao().getAttivitàByDateRange(startOfWeek, System.currentTimeMillis())

            withContext(Dispatchers.Main) {
                // Aggiorni i grafici
                updateCharts(attivitaOggi, attivitaSettimanali)
                setupCharts()
            }
        }
    }

    private fun updateCharts(attivitàGiornaliere: List<Attività>, attivitàSettimanali: List<Attività>) {
    }

    private fun animateMapSize(expand: Boolean, triggeredBySwitch: Boolean = false) {
        val cardView = findViewById<CardView>(R.id.myCardView)
        val startHeight = cardView.height
        val endHeight = if (expand) {
            if (triggeredBySwitch) {
                resources.getDimensionPixelSize(R.dimen.map_height_expanded_switch) // Nuova dimensione per lo switch
            } else {
                resources.getDimensionPixelSize(R.dimen.map_height_expanded) // Dimensione normale
            }
        } else {
            resources.getDimensionPixelSize(R.dimen.map_height_default) // Dimensione predefinita
        }

        val animator = ValueAnimator.ofInt(startHeight, endHeight)
        animator.duration = 800
        animator.interpolator = FastOutSlowInInterpolator()

        animator.addUpdateListener { animation ->
            val params = cardView.layoutParams
            params.height = animation.animatedValue as Int
            cardView.layoutParams = params
        }

        animator.start()
    }

    private fun resetMapSize() {
        val cardView = findViewById<CardView>(R.id.myCardView)
        val params = cardView.layoutParams
        params.height = resources.getDimensionPixelSize(R.dimen.map_height_default) // Aggiungi questa dimensione nel dimens.xml (es: 350dp)
        cardView.layoutParams = params
    }

    private var initialStatsHeight = 0
    private lateinit var statsContainer: LinearLayout
    private lateinit var weeklyStatsContainer: LinearLayout
    private lateinit var chartContainer: LinearLayout
    private lateinit var dailyStatsContainer: LinearLayout
    private lateinit var contentContainer: LinearLayout
    private var isStatsExpanded = false
    var isStatsContainerVisible = false
    private var isNearLockPoint = false
    private var lockScrollPosition = 0
    private var lockScrollPositionPx = 0
    private var releaseThresholdPx = 0
    private var lockThresholdPx = 0

    private var lastScrollVelocity = 0f
    private var lastScrollTime = 0L
    private var isTouchingMap = false

    private fun setupViews() {

        val scale = resources.displayMetrics.density

        val thresholdDp = 25
        val statsThresholdDp = 30
        val releaseThresholdDp = 50
        val lockThresholdDp = 20

        val scrollY_420dp = 200

        val scrollThresholdStartDp = 30f
        val scrollThresholdMidDp   = 420f
        val scrollStableEndDp      = 1100f
        val scrollThresholdEndDp   = 1300f

        val thresholdPx       = (thresholdDp       * scale).toInt()
        val statsThresholdPx  = (statsThresholdDp  * scale).toInt()
        releaseThresholdPx    = (releaseThresholdDp   * scale).toInt()
        lockThresholdPx       = (lockThresholdDp      * scale).toInt()

        val scrollY_420 = (scrollY_420dp * scale).toInt()

        val scrollThresholdStart = (scrollThresholdStartDp * scale)
        val scrollThresholdMid   = (scrollThresholdMidDp   * scale)
        val scrollStableEnd      = (scrollStableEndDp      * scale)
        val scrollThresholdEnd   = (scrollThresholdEndDp   * scale)



        statsContainer = findViewById(R.id.statsContainer)
        dailyStatsContainer = findViewById(R.id.dailyStatsContainer)
        contentContainer = findViewById(R.id.statsContent)
        chartContainer = findViewById(R.id.chartContainer)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_dynamic) as SupportMapFragment
        var isAtTop = true
        isTouchingMap = false
        val mapView = mapFragment.view

        dailyStatsContainer.visibility = View.VISIBLE
        contentContainer.visibility = View.GONE
        isStatsExpanded = false
        var lastScrollY = 0

        // Un unico listener dello scroll che combina tutte le logiche
        scrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, oldScrollY ->

                // Ottieni la densità
                val scale = resources.displayMetrics.density

                // Converte lo scroll in dp: scrollYDp = scrollY / scale
                val scrollYDp = scrollY / scale
                val oldScrollYDp = oldScrollY / scale


                val scrollY_380dp = when (attivitàDaAvviare) {
                    "Sedersi" -> 80
                    else      -> 200
                }

                val scrollY_380 = (scrollY_380dp * scale).toInt()
                val bottomButtonThreshold = (scrollView.height * 0.7).toInt()
                val lockScrollPositionDp = when (attivitàDaAvviare) {
                    "Corsa" -> 160
                    "Sedersi" -> 60
                    else -> 110
                }
                lockScrollPositionPx = (lockScrollPositionDp * scale).toInt()

                if (attivitàDaAvviare != null || attivitàCorrente != null) {
                    Log.d("ScrollDebug", "Attività in corso rilevata")
                    if (attivitàDaAvviare == "Corsa" && scrollY > scrollY_420 && !isStatsContainerVisible) {
                        bottomButtonsCard.visibility = View.VISIBLE
                        bottomButtonsCard.alpha = 1f
                        isStatsContainerVisible = true
                        statsContainer.visibility = View.VISIBLE
                        statsContainer.alpha = 0f
                        statsContainer.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                    } else if (attivitàDaAvviare != "Corsa" && scrollY > scrollY_380 && !isStatsContainerVisible) {
                        bottomButtonsCard.visibility = View.VISIBLE
                        bottomButtonsCard.alpha = 1f
                        isStatsContainerVisible = true
                        statsContainer.visibility = View.VISIBLE
                        statsContainer.alpha = 0f
                        statsContainer.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                    } else if (scrollY < scrollY_380 && isStatsContainerVisible) {
                        bottomButtonsCard.visibility = View.VISIBLE
                        bottomButtonsCard.alpha = 1f
                        isStatsContainerVisible = false
                        statsContainer.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .setInterpolator(AccelerateInterpolator())
                            .withEndAction {
                                statsContainer.visibility = View.GONE
                            }
                            .start()
                    }
                }

                // Gestione espansione statsContainer
                if (scrollY <= thresholdPx && isStatsExpanded) {
                    collapseStatsContainer()
                } else if (scrollY > thresholdPx && !isStatsExpanded) {
                    expandStatsContainer()
                }

                handleScrollAnimations(scrollY, v)
                lastScrollY = scrollY

                val scrollY_80dp = (80 * scale).toInt()
                if (scrollY <= scrollY_80dp) {
                    isAtTop = true
                } else {
                    isAtTop = false
                }

                // Dimensioni delle icone
                val minIconSize = resources.getDimensionPixelSize(R.dimen.icon_normal_size)    // media
                val maxIconSize = resources.getDimensionPixelSize(R.dimen.icon_min_size)       // grande
                val littleIconSize = resources.getDimensionPixelSize(R.dimen.icon_little_size) // piccola

                val minTextSize = 16f
                val maxTextSize = 22f

                if (scrollY > 0) {
                    val params = imgRecognizedActivity.layoutParams
                    val statsLayout = findViewById<LinearLayout>(R.id.layoutAutomaticActivityStats)
                    val txtPrimary = findViewById<TextView>(R.id.txtAutomaticStatsPrimary)
                    val txtSecondary = findViewById<TextView>(R.id.txtAutomaticStatsSecondary)
                    val txtTertiary = findViewById<TextView>(R.id.txtAutomaticStatsTertiary)

                    when {
                        scrollY < scrollThresholdStart -> {
                            if (switchAutomaticActivity.isChecked) {
                                dailyStatsContainer.visibility = View.VISIBLE
                                dailyStatsContainer.alpha = 1f
                                txtRecognizedActivity.visibility = View.GONE
                                layoutAutomaticActivityStats.visibility = View.GONE
                            }

                            params.width = littleIconSize
                            params.height = littleIconSize

                            val textAlpha = (scrollY / scrollThresholdStart).coerceIn(0f, 1f)
                            txtRecognizedActivity.alpha = textAlpha
                            txtRecognizedActivity.scaleX = littleIconSize.toFloat() / maxIconSize
                            txtRecognizedActivity.scaleY = littleIconSize.toFloat() / maxIconSize

                            statsLayout.alpha = 0f
                        }

                        scrollY <= scrollThresholdMid -> {
                            if (switchAutomaticActivity.isChecked) {
                                val progress = (scrollY - scrollThresholdStart) /
                                        (scrollThresholdMid - scrollThresholdStart)
                                dailyStatsContainer.alpha = 1f - progress
                                if (progress >= 1f) {
                                    dailyStatsContainer.visibility = View.GONE
                                }
                            }
                            // Prima transizione: da piccola a media
                            val progress = (scrollY - scrollThresholdStart) /
                                    (scrollThresholdMid - scrollThresholdStart)
                            val currentSize = littleIconSize + ((minIconSize - littleIconSize) * progress)
                            params.width = currentSize.toInt()
                            params.height = currentSize.toInt()

                            if (switchAutomaticActivity.isChecked) {
                                txtRecognizedActivity.visibility = View.VISIBLE
                                statsLayout.visibility = View.VISIBLE

                                val currentTextSize = minTextSize + ((maxTextSize - minTextSize) * progress)
                                statsLayout.alpha = progress
                                txtPrimary.textSize = currentTextSize
                                txtSecondary.textSize = currentTextSize
                                txtTertiary.textSize = currentTextSize
                            }

                            txtRecognizedActivity.alpha = 1f
                            val scaleFactor = currentSize / maxIconSize
                            txtRecognizedActivity.scaleX = scaleFactor
                            txtRecognizedActivity.scaleY = scaleFactor
                        }

                        scrollY <= scrollStableEnd -> {
                            params.width = minIconSize
                            params.height = minIconSize
                            if (switchAutomaticActivity.isChecked) {
                                txtRecognizedActivity.visibility = View.VISIBLE
                                layoutAutomaticActivityStats.visibility = View.VISIBLE
                            }
                            txtRecognizedActivity.alpha = 1f
                            val scaleFactor = minIconSize.toFloat() / maxIconSize
                            txtRecognizedActivity.scaleX = scaleFactor
                            txtRecognizedActivity.scaleY = scaleFactor
                            dailyStatsContainer.visibility = View.GONE
                        }

                        scrollY <= scrollThresholdEnd -> {
                            // Seconda transizione: da media a grande
                            val progress = (scrollY - scrollStableEnd) /
                                    (scrollThresholdEnd - scrollStableEnd)
                            val currentSize = minIconSize + ((maxIconSize - minIconSize) * progress)
                            params.width = currentSize.toInt()
                            params.height = currentSize.toInt()

                            if (switchAutomaticActivity.isChecked) {
                                txtRecognizedActivity.visibility = View.VISIBLE
                                layoutAutomaticActivityStats.visibility = View.VISIBLE
                            }

                            txtRecognizedActivity.alpha = 1f
                            val scaleFactor = currentSize / maxIconSize
                            txtRecognizedActivity.scaleX = scaleFactor
                            txtRecognizedActivity.scaleY = scaleFactor
                        }

                        else -> {
                            // Dimensione finale (grande)
                            params.width = maxIconSize
                            params.height = maxIconSize
                            if (switchAutomaticActivity.isChecked) {
                                txtRecognizedActivity.visibility = View.VISIBLE
                                layoutAutomaticActivityStats.visibility = View.VISIBLE

                                txtPrimary.scaleX = 1f
                                txtPrimary.scaleY = 1f
                                txtSecondary.scaleX = 1f
                                txtSecondary.scaleY = 1f
                                txtTertiary.scaleX = 1f
                                txtTertiary.scaleY = 1f

                                txtPrimary.alpha = 1f
                                txtSecondary.alpha = 1f
                                txtTertiary.alpha = 1f
                            }
                            txtRecognizedActivity.alpha = 1f
                            txtRecognizedActivity.scaleX = 1f
                            txtRecognizedActivity.scaleY = 1f
                        }
                    }

                    imgRecognizedActivity.layoutParams = params
                } else {
                    // scrollY <= 0
                    if (switchAutomaticActivity.isChecked) {
                        dailyStatsContainer.visibility = View.VISIBLE
                        dailyStatsContainer.alpha = 1f
                        txtRecognizedActivity.visibility = View.GONE
                        layoutAutomaticActivityStats.visibility = View.GONE
                    }

                    // Stato iniziale (piccola) con testo nascosto
                    val params = imgRecognizedActivity.layoutParams
                    params.width = littleIconSize
                    params.height = littleIconSize
                    imgRecognizedActivity.layoutParams = params

                    txtRecognizedActivity.visibility = View.GONE
                    layoutAutomaticActivityStats.visibility = View.GONE
                }

                val bottomButtonsCard = findViewById<MaterialCardView>(R.id.bottomButtonsCard)
                val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
                val fabInterrompi = findViewById<FloatingActionButton>(R.id.fab_interrompi)

                // Logica del punto di aggancio
                if (attivitàCorrente != null) {
                    val currentTime = System.currentTimeMillis()
                    val timeDelta = currentTime - lastScrollTime
                    if (timeDelta > 0) {
                        lastScrollVelocity = Math.abs(scrollY - oldScrollY) / timeDelta.toFloat()
                    }
                    lastScrollTime = currentTime

                    val distanceFromLockPoint = Math.abs(scrollY - lockScrollPositionPx)
                    when {
                        // vicini al punto di blocco
                        distanceFromLockPoint < lockThresholdPx -> {
                            if (!isNearLockPoint) {
                                isNearLockPoint = true
                                v.smoothScrollTo(0, lockScrollPositionPx)
                                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        }
                        // Scroll abbastanza veloce/forte per superare il blocco
                        lastScrollVelocity > 0.5f || distanceFromLockPoint > releaseThresholdPx -> {
                            if (isNearLockPoint) {
                                isNearLockPoint = false
                            }
                        }
                    }
                }

                // Logica di visibilità degli elementi UI
                val fixedThresholdDp = 250
                val fixedThresholdPx = (fixedThresholdDp * scale).toInt()

                // Calcola se stai scorrendo verso il basso
                val isScrollingDown = scrollY > oldScrollY

                if (scrollY < fixedThresholdPx) {
                    // Se lo scroll è inferiore a 400dp  mantieni visibili bottomButtonsCard e fabAdd
                    bottomButtonsCard.visibility = View.VISIBLE
                    fabAdd.visibility = View.VISIBLE
                } else {
                    if (isScrollingDown && scrollY > fixedThresholdPx + lockThresholdPx) {
                        val hideAnimatorSet = AnimatorSet()
                        if (bottomButtonsCard.visibility == View.VISIBLE) {
                            val hideBottomCardAnimator =
                                ObjectAnimator.ofFloat(bottomButtonsCard, "alpha", 1f, 0f).apply {
                                    duration = 100
                                    addListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator) {
                                            bottomButtonsCard.visibility = View.GONE
                                        }
                                    })
                                }
                            hideAnimatorSet.playTogether(hideBottomCardAnimator)
                        }
                        if (fabAdd.visibility == View.VISIBLE) {
                            val hideFabAnimator =
                                ObjectAnimator.ofFloat(fabAdd, "alpha", 1f, 0f).apply {
                                    duration = 100
                                    addListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator) {
                                            fabAdd.visibility = View.GONE
                                        }
                                    })
                                }
                            hideAnimatorSet.playTogether(hideFabAnimator)
                        }
                        hideAnimatorSet.start()
                    } else {
                        val showAnimatorSet = AnimatorSet()
                        if (bottomButtonsCard.visibility != View.VISIBLE) {
                            bottomButtonsCard.visibility = View.VISIBLE
                            val showBottomCardAnimator =
                                ObjectAnimator.ofFloat(bottomButtonsCard, "alpha", 0f, 1f).apply {
                                    duration = 100
                                }
                            showAnimatorSet.playTogether(showBottomCardAnimator)
                        }
                        if (fabAdd.visibility != View.VISIBLE) {
                            fabAdd.visibility = View.VISIBLE
                            val showFabAnimator =
                                ObjectAnimator.ofFloat(fabAdd, "alpha", 0f, 1f).apply {
                                    duration = 100
                                }
                            showAnimatorSet.playTogether(showFabAnimator)
                        }
                        showAnimatorSet.start()
                    }
                }

                // Logica per gli effetti della toolbar e del margine
                val thresholdToolPx = (30 * scale).toInt()
                val isNearBottom = v.getChildAt(0).bottom - (v.height + scrollY) <= thresholdToolPx

                when {
                    isNearBottom && scrollY > thresholdPx -> {
                        handleBottomReached()
                    }
                    !v.canScrollVertically(-1) -> {
                        handleTopScroll()
                    }
                    else -> {
                        handleProgressiveScroll(scrollY)
                    }
                }

                // Posizionamento della card dei contatori
                val contatoriCard = findViewById<MaterialCardView>(R.id.contatoriCard)
                if (attivitàCorrente != null && contatoriCard.visibility == View.VISIBLE) {
                    val cardBottom = contatoriCard.bottom
                    val scrollViewHeight = v.height
                    val targetScrollPosition = cardBottom + contatoriCard.marginTop + contatoriCard.marginBottom
                    if (cardBottom > (scrollY + scrollViewHeight)) {
                        v.smoothScrollTo(0, targetScrollPosition)
                    }
                }

                lastScrollY = scrollY
            }
        )
    }




    private fun handleScrollAnimations(scrollY: Int, v: NestedScrollView) {
        val threshold = 200
        val isNearBottom = v.getChildAt(0).bottom - (v.height + scrollY) <= threshold

        when {
            isNearBottom && scrollY > 200 -> handleBottomReached()
            !v.canScrollVertically(-1) -> handleTopScroll()
        }

        // Gestione posizione card contatori
        val contatoriCard = findViewById<MaterialCardView>(R.id.contatoriCard)
        if (attivitàCorrente != null && contatoriCard.visibility == View.VISIBLE) {
            handleContatoriCardPosition(contatoriCard, scrollY, v)
        }

        lastScrollY = scrollY
    }

    private fun handleContatoriCardPosition(
        contatoriCard: MaterialCardView,
        scrollY: Int,
        scrollView: NestedScrollView
    ) {
        val cardBottom = contatoriCard.bottom
        val scrollViewHeight = scrollView.height
        val targetScrollPosition = cardBottom + contatoriCard.marginTop + contatoriCard.marginBottom

        if (cardBottom > (scrollY + scrollViewHeight)) {
            scrollView.smoothScrollTo(0, targetScrollPosition)
        }
    }

    private fun getActivityColor(): Int {
        // Verifica se il tema dinamico è abilitato
        val colorMode = prefs.getInt("app_color", 0)
        val isDynamicEnabled = colorMode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        // Se il tema dinamico non è abilitato, usa i colori statici
        if (!isDynamicEnabled) {
            return when (attivitàCorrente) {
                "Camminare" -> ContextCompat.getColor(this, R.color.colorCamminare)
                "Corsa" -> ContextCompat.getColor(this, R.color.colorCorrere)
                "Guidare" -> ContextCompat.getColor(this, R.color.colorGuidare)
                "Bicicletta" -> ContextCompat.getColor(this, R.color.colorBici)
                "Sedersi" -> ContextCompat.getColor(this, R.color.colorSedersi)
                else -> ContextCompat.getColor(this, R.color.material_blue)
            }
        }

        // Con tema dinamico, usa il colore primario del tema
        return MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary,
            ContextCompat.getColor(this, R.color.material_blue))
    }

    private fun handleBottomReached() {
        val activityColor = getActivityColor()
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        toolbar.setBackgroundColor(activityColor)
        // Usa il colore onPrimary per il testo quando si raggiunge il fondo
        val textColor = if (isNightMode) Color.WHITE else Color.BLACK
        actionBarTitle.setColorFilter(textColor)
        toolbar.elevation = 4f
        window.statusBarColor = activityColor
    }



    private fun handleTopScroll() {
        toolbar.setBackgroundColor(Color.TRANSPARENT)
        val iconColor = getActivityColor()

        val desiredMargin = resources.getDimensionPixelSize(R.dimen.card_margin)
        val mapCard = findViewById<CardView>(R.id.myCardView)
        val layoutParams = mapCard.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = desiredMargin
        layoutParams.marginEnd = desiredMargin
        mapCard.layoutParams = layoutParams
        updateMapHeight(0)

        actionBarTitle.setColorFilter(iconColor)
        gradientView.visibility = View.GONE
        toolbar.elevation = 0f
        actionBarTitle.alpha = 1f

        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        window.statusBarColor = if (isNightMode) {
            resources.getColor(R.color.transparent_b)
        } else {
            resources.getColor(android.R.color.transparent)
        }
    }


    private fun handleProgressiveScroll(scrollY: Int) {
        val ratio = ((scrollY - 100).toFloat() / (300 - 100)).coerceIn(0f, 1f)
        val newMargin = (initialMargin * (1 - ratio)).toInt()
        val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = newMargin
        layoutParams.marginEnd = newMargin
        updateMapHeight(scrollY)


        // Gestione dell'altezza della mappa durante lo scroll quando lo switch è attivo
        if (switchAutomaticActivity.isChecked) {
            val defaultHeight = resources.getDimensionPixelSize(R.dimen.map_height_default) // 350dp
            val expandedHeight = resources.getDimensionPixelSize(R.dimen.map_height_expanded_switch) // 550dp

            val heightRatio = ((scrollY).toFloat() / 300).coerceIn(0f, 1f)
            val targetHeight = defaultHeight + ((expandedHeight - defaultHeight) * heightRatio)

            layoutParams.height = targetHeight.toInt()
        }

        cardView.layoutParams = layoutParams

        val ratio1 = ((scrollY - 400).toFloat() / (400 - 200)).coerceIn(0f, 1f)
        updateToolbarAppearance(ratio1)
        updateToolbarAppearance(ratio)
    }

    private fun updateToolbarAppearance(ratio: Float) {
        val baseColor = getActivityColor()
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        val initialAlpha = 0.0f
        val startColor = ColorUtils.setAlphaComponent(baseColor, (255 * initialAlpha).toInt())

        val colorWithTransparency = ColorUtils.blendARGB(
            startColor,
            baseColor,
            ratio
        )

        // Determina il colore del testo in base al tema e allo scroll
        val textColor = if (isNightMode) Color.WHITE else Color.BLACK
        val logoColor = ColorUtils.blendARGB(
            baseColor,
            textColor,
            ratio
        )

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(baseColor, ColorUtils.setAlphaComponent(baseColor, 0))
        ).apply {
            cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 8f, 8f, 8f, 8f)
            gradientType = GradientDrawable.LINEAR_GRADIENT
        }
        gradientView.background = gradientDrawable

        actionBarTitle.setColorFilter(logoColor)
        actionBarTitle.alpha = 1f - (ratio * 0.2f)
        toolbar.setBackgroundColor(colorWithTransparency)
        toolbar.elevation = ratio * 4f

        gradientView.visibility = View.VISIBLE
        gradientView.alpha = ratio

        val statusBarColor = ColorUtils.blendARGB(
            if (isNightMode) resources.getColor(R.color.transparent_b)
            else resources.getColor(android.R.color.transparent),
            baseColor,
            ratio
        )
        window.statusBarColor = statusBarColor
    }

    private var isExpanded = false
    private var isFirstExpand = true

    private fun expandStatsContainer() {
        isExpanded = true

        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)

        findViewById<TextView>(R.id.activityTitle).text = "Attività"
        findViewById<TextView>(R.id.txtPassiGiornalieri).visibility = View.VISIBLE
        findViewById<TextView>(R.id.txtNumeroSeduteGiornaliere).visibility = View.VISIBLE
        isStatsExpanded = true

        dailyStatsContainer.animate()
            .alpha(0f)
            .setDuration(100)
            .setInterpolator(FastOutSlowInInterpolator())
            .withEndAction {
                dailyStatsContainer.visibility = View.GONE
            }
            .start()

        // Mostra tutto il contenuto insieme
        statsContent.visibility = View.VISIBLE
        chartContainer.visibility = View.VISIBLE
        statsContent.alpha = 0f
        chartContainer.alpha = 0f

        // Anima tutto insieme
        val fadeIn = AnimatorSet()
        fadeIn.playTogether(
            ObjectAnimator.ofFloat(statsContent, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(chartContainer, "alpha", 0f, 1f)
        )
        fadeIn.duration = 300
        fadeIn.interpolator = FastOutSlowInInterpolator()
        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                chartsManager.animateCharts(walkingChart, runningChart, sittingChart)

                if (isFirstExpand) {
                    startSequentialDarkening()
                    isFirstExpand = false
                }
            }
        })
        fadeIn.start()
    }

    private fun collapseStatsContainer() {
        isExpanded = false
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        findViewById<TextView>(R.id.activityTitle).text = "Attività Oggi"
        isStatsExpanded = false

        dailyStatsContainer.visibility = View.VISIBLE
        dailyStatsContainer.alpha = 0f
        dailyStatsContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        contentContainer.animate()
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
            .withEndAction {
                contentContainer.visibility = View.GONE
            }
            .start()
    }



    private var currentDarkeningLevel = 0f
    private val MAX_DARKENING = 0.5f
    private val ITEM_ANIMATION_DURATION = 400L
    private val SEQUENCE_DELAY = 200L
    private var currentCycle = 0
    private val MAX_CYCLES = 4
    private val DARKENING_STEPS = 0.10f
    private var viewsToAnimate = mutableListOf<View>()
    private var lastAnimatedIndex = 0
    private val DYNAMIC_THEME_COLOR_MODE = 1
    private val DYNAMIC_THEME_MIN_SDK = Build.VERSION_CODES.S

    private var viewsInitialized = false

    private fun startSequentialDarkening() {
        if (currentCycle >= MAX_CYCLES) {
            // Accelera il reset completo
            resetAllViewsWithAnimation(viewsToAnimate)
            currentCycle = 0
            currentDarkeningLevel = 0f
            lastAnimatedIndex = 0
            return
        }

        if (!viewsInitialized) {
            viewsInitialized = true
            viewsToAnimate.clear()
            val statsContent = findViewById<LinearLayout>(R.id.statsContent)
            collectAnimatableViews(statsContent, viewsToAnimate)

            // Verifica se il tema dinamico è attivo
            val colorMode = prefs.getInt("app_color", 0)
            val isDynamicTheme = colorMode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

            viewsToAnimate.forEach { view ->
                if (viewCategories[view] == "sempre_scuro") {
                    animateContinuousView(view)
                }

                // Inizializza immediatamente con il colore appropriato per evitare flash
                val initialColor = when {
                    isDynamicTheme -> {
                        // Usa i colori del tema dinamico
                        when (viewCategories[view]) {
                            "stats" -> MaterialColors.getColor(view, com.google.android.material.R.attr.colorPrimaryContainer)
                            "charts" -> MaterialColors.getColor(view, com.google.android.material.R.attr.colorSecondaryContainer)
                            "records" -> MaterialColors.getColor(view, com.google.android.material.R.attr.colorTertiaryContainer)
                            "title" -> MaterialColors.getColor(view, com.google.android.material.R.attr.colorPrimary)
                            else -> MaterialColors.getColor(view, com.google.android.material.R.attr.colorSurfaceVariant)
                        }
                    }
                    isDarkMode -> {
                        // Usa colori scuri predefiniti subito per evitare flash
                        when (viewCategories[view]) {
                            "stats" -> Color.parseColor("#1E1E2F")
                            "charts" -> Color.parseColor("#242438")
                            "records" -> Color.parseColor("#27273D")
                            "title" -> Color.parseColor("#1A1A2C")
                            else -> Color.parseColor("#212133")
                        }
                    }
                    else -> {
                        // Usa i colori statici dell'app
                        when (viewCategories[view]) {
                            "stats" -> Color.parseColor("#37FF7070")
                            "charts" -> Color.parseColor("#37FF7070")
                            "records" -> Color.parseColor("#37FF7070")
                            "title" -> Color.parseColor("#37FF7070")
                            else -> Color.parseColor("#37FF7070")
                        }
                    }
                }

                // Applica immediatamente il colore iniziale senza animazione
                when (view) {
                    is ImageView -> {
                        view.setColorFilter(initialColor, PorterDuff.Mode.SRC_IN)
                    }
                    else -> {
                        val newBackground = GradientDrawable().apply {
                            setColor(initialColor)
                            cornerRadius = resources.getDimension(R.dimen.stats_margin1)
                        }
                        view.background = newBackground
                    }
                }

                viewColors[view] = initialColor
            }
        }

        currentDarkeningLevel = (currentDarkeningLevel + DARKENING_STEPS)
            .coerceAtMost(MAX_DARKENING)

        animateNextView(viewsToAnimate, lastAnimatedIndex)
    }

    private fun resetAllViewsWithAnimation(views: List<View>) {
        val isDynamicTheme = prefs.getInt("app_color", 0) == DYNAMIC_THEME_COLOR_MODE &&
                Build.VERSION.SDK_INT >= DYNAMIC_THEME_MIN_SDK

        views.forEachIndexed { index, view ->
            val tag = viewCategories[view] ?: "animatable"
            val currentColor = viewColors[view] ?: getDefaultColorForView(tag, isDynamicTheme)

            val targetColor = when {
                isDynamicTheme -> getDynamicThemeBaseColor(tag)
                else -> getStaticThemeBaseColor(tag)
            }

            val animator = ValueAnimator.ofArgb(currentColor, targetColor).apply {
                duration = ITEM_ANIMATION_DURATION * 2
                startDelay = (index * SEQUENCE_DELAY / 2).toLong()
                interpolator = FastOutSlowInInterpolator()

                addUpdateListener { animation ->
                    val color = animation.animatedValue as Int
                    when (view) {
                        is ImageView -> {
                            view.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                        }
                        else -> {
                            val newBackground = GradientDrawable().apply {
                                setColor(color)
                                cornerRadius = resources.getDimension(R.dimen.stats_margin1)
                            }
                            view.background = newBackground
                        }
                    }
                    viewColors[view] = color
                }

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (index == views.size - 1) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                startSequentialDarkening()
                            }, SEQUENCE_DELAY * 2)
                        }
                    }
                })
            }

            animator.start()
        }
    }

    private fun getDefaultColorForView(tag: String, isDynamic: Boolean): Int {
        return if (isDynamic) getDynamicThemeBaseColor(tag) else getStaticThemeBaseColor(tag)
    }

    private fun getDynamicThemeBaseColor(tag: String): Int {
        return when (tag) {
            "stats" -> resolveDynamicColor(R.attr.colorSurfaceContainerHigh)
            "charts" -> resolveDynamicColor(R.attr.colorSurfaceContainer)
            "records" -> resolveDynamicColor(R.attr.colorSurfaceContainerLow)
            "title" -> resolveDynamicColor(R.attr.colorPrimaryContainer)
            else -> resolveDynamicColor(R.attr.colorSecondaryContainer)
        }
    }

    private fun getStaticThemeBaseColor(tag: String): Int {
        val colorMode = prefs.getInt("app_color", 0)
        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

        return if (colorMode == 0) {  // Tema dell'app (non dinamico)
            if (isDarkMode) {
                when (tag) {
                    "stats" -> Color.parseColor("#1E1E2F")     // Blu scuro più attenuato
                    "charts" -> Color.parseColor("#242438")    // Blu scuro leggermente più chiaro
                    "records" -> Color.parseColor("#27273D")   // Blu scuro ancora più chiaro
                    "title" -> Color.parseColor("#1A1A2C")     // Blu scuro per titoli
                    else -> Color.parseColor("#212133")        // Sfondo generale attenuato
                }
            } else {
                when (tag) {
                    "stats" -> Color.parseColor("#37FF7070")     // Rosa molto tenue
                    "charts" -> Color.parseColor("#37FF7070")    // Rosa ancora più tenue
                    "records" -> Color.parseColor("#37FF7070")   // Quasi bianco con leggera sfumatura rosa
                    "title" -> Color.parseColor("#37FF7070")     // Rosa attenuato per titoli
                    else -> Color.parseColor("#37FF7070")        // Sfondo generale molto tenue
                }
            }
        } else {  // Tema dinamico (gestito altrove)
            getDynamicThemeBaseColor(tag)  // Riutilizza i colori dinamici
        }
    }



    private fun resolveDynamicColor(@AttrRes colorAttr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(colorAttr, typedValue, true)
        return ContextCompat.getColor(this, typedValue.resourceId)
    }

    private fun animateContinuousView(view: View) {
        val colorMode = prefs.getInt("app_color", 0)
        val isDynamicTheme = colorMode == DYNAMIC_THEME_COLOR_MODE &&
                Build.VERSION.SDK_INT >= DYNAMIC_THEME_MIN_SDK

        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

        // Colori per tema dinamico (non modificati)
        val dynamicStartColor = resolveDynamicColor(R.attr.colorSurfaceContainerHigh)
        val dynamicEndColor = resolveDynamicColor(R.attr.colorSurfaceContainer)

        // Colori per tema chiaro dell'app (non dinamico) - più tenui
        val lightStartColor = Color.parseColor("#FFF2F5") // Quasi bianco con sfumatura rosa
        val lightEndColor = Color.parseColor("#FFF8FA") // Bianco con leggera sfumatura rosa

        // Colori per tema scuro dell'app (non dinamico) - più tenui
        val darkStartColor = Color.parseColor("#222234") // Blu scuro molto attenuato
        val darkEndColor = Color.parseColor("#2A2A3C") // Leggermente più chiaro ma ancora tenue

        val (startColor, endColor) = when {
            isDynamicTheme -> dynamicStartColor to dynamicEndColor
            isDarkMode -> darkStartColor to darkEndColor
            else -> lightStartColor to lightEndColor
        }

        val animator = ValueAnimator.ofArgb(startColor, endColor).apply {
            duration = 800L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                val color = animation.animatedValue as Int
                when (view) {
                    is ImageView -> {
                        view.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                        view.background?.mutate()?.let {
                            (it as GradientDrawable).setColor(color)
                        }
                    }
                    else -> {
                        view.background.mutate().let {
                            (it as GradientDrawable).setColor(color)
                        }
                    }
                }
            }
        }
        animator.start()
    }

    private val viewCategories = mutableMapOf<View, String>()  // Per tracciare la categoria di ogni vista

    private fun collectAnimatableViews(view: View, list: MutableList<View>) {
        if (view is ViewGroup) {
            if (view.id == R.id.dailyStatsContainer_dentro) return

            val tag = view.tag as? String
            if (tag in listOf("stats", "charts", "records", "title", "animatable", "sempre_scuro")) {
                // Aggiungi solo se non è già presente
                if (!list.contains(view)) {
                    list.add(view)
                    viewCategories[view] = tag!!
                }
            }

            for (i in 0 until view.childCount) {
                collectAnimatableViews(view.getChildAt(i), list)
            }
        }
    }

    private val viewColors = mutableMapOf<View, Int>()

    private fun animateNextView(views: List<View>, currentIndex: Int) {
        if (currentIndex >= views.size) {
            currentCycle++
            lastAnimatedIndex = 0
            Handler(Looper.getMainLooper()).postDelayed({
                startSequentialDarkening()
            }, SEQUENCE_DELAY * 2)
            return
        }

        val view = views[currentIndex]
        val tag = viewCategories[view] ?: "animatable"
        val currentColor = viewColors[view] ?: Color.parseColor("#37FF7070")

        if (tag == "sempre_scuro") {
            animateNextView(views, currentIndex + 1)
            return
        }

        val targetDarkening = (currentDarkeningLevel / MAX_DARKENING).coerceIn(0f, 1f)

        val isDynamicTheme = prefs.getInt("app_color", 0) == DYNAMIC_THEME_COLOR_MODE &&
                Build.VERSION.SDK_INT >= DYNAMIC_THEME_MIN_SDK

        val baseColor = when {
            isDynamicTheme -> getDynamicThemeBaseColor(tag)
            else -> getStaticThemeBaseColor(tag)
        }

        val endColor = when {
            isDynamicTheme -> ColorUtils.blendARGB(
                baseColor,
                resolveDynamicColor(R.attr.colorOutline),
                targetDarkening
            )
            else -> ColorUtils.blendARGB(
                baseColor,
                Color.parseColor("#FF7070"),
                targetDarkening
            )
        }




        val animator = ValueAnimator.ofArgb(currentColor, endColor).apply {
            duration = ITEM_ANIMATION_DURATION
            interpolator = FastOutSlowInInterpolator()

            addUpdateListener { animation ->
                val color = animation.animatedValue as Int
                when (view) {
                    is ImageView -> {
                        view.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                    else -> {
                        val newBackground = GradientDrawable().apply {
                            setColor(color)
                            cornerRadius = resources.getDimension(R.dimen.stats_margin1)
                        }
                        view.background = newBackground
                    }
                }
                viewColors[view] = color
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    lastAnimatedIndex = currentIndex + 1
                    Handler(Looper.getMainLooper()).postDelayed({
                        animateNextView(views, lastAnimatedIndex)
                    }, SEQUENCE_DELAY)
                }
            })
        }

        animator.start()
    }



    private var scrollAnimator: ValueAnimator? = null
    private var originalScrollListener: View.OnScrollChangeListener? = null

    private fun animaBottoniInStatoThinking() {

        isAutoScrolling = true

        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
        val buttonLayout = findViewById<LinearLayout>(R.id.buttonLayout)
        val buttonLayout1 = findViewById<FrameLayout>(R.id.buttonFrameLayout)

        buttonLayout1.post {
            val contentWidth = buttonLayout1.width
            val scrollDistance = contentWidth

            if (scrollDistance > 0) {
                scrollAnimator = ValueAnimator.ofInt(0, scrollDistance).apply {
                    duration = 800
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.REVERSE
                    interpolator = FastOutSlowInInterpolator()

                    addUpdateListener { animation ->
                        val value = animation.animatedValue as Int
                        horizontalScrollView.scrollTo(value, 0)
                    }

                    start()
                }
            }
        }
    }

    private fun stopBottoniThinkingAnimation() {

        isAutoScrolling = false

        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
        val buttonLayout1 = findViewById<FrameLayout>(R.id.buttonFrameLayout)
        val recordsLayout = findViewById<LinearLayout>(R.id.RecordsLayout)
        val view1 = findViewById<View>(R.id.View_1)
        val view2 = findViewById<View>(R.id.View_2)

        // Ferma l'animatore se è attivo
        scrollAnimator?.cancel()
        scrollAnimator = null

        // Ripristina i margini originali
        recordsLayout.apply {
            (layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.default_margin)
                marginEnd = resources.getDimensionPixelSize(R.dimen.default_margin)
            }
            layoutParams = recordsLayout.layoutParams
        }

        horizontalScrollView.apply {
            (layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.default_margin)
                marginEnd = resources.getDimensionPixelSize(R.dimen.default_margin)
            }
            layoutParams = horizontalScrollView.layoutParams
        }


        buttonLayout1.animate()
            .translationX(0f)
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        horizontalScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            val currentTime = System.currentTimeMillis()
            if (Math.abs(scrollX - lastScrollX) > SCROLL_THRESHOLD &&
                (currentTime - lastFeedbackTime) > FEEDBACK_DELAY
            ) {
                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                lastScrollX = scrollX
                lastFeedbackTime = currentTime
            }
        }

        horizontalScrollView.smoothScrollTo(0, 0)
    }

    private fun updateMapHeight(scrollY: Int) {
        if (switchAutomaticActivity.isChecked && attivitàCorrente == null) {
            val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
            val defaultHeight = resources.getDimensionPixelSize(R.dimen.map_height_default)
            val expandedHeight = resources.getDimensionPixelSize(R.dimen.map_height_expanded_switch)

            val heightRatio = if (scrollY <= 0) {
                0f // Altezza minima quando siamo in cima
            } else {
                ((scrollY).toFloat() / 300).coerceIn(0f, 1f)
            }

            val targetHeight = defaultHeight + ((expandedHeight - defaultHeight) * heightRatio)
            layoutParams.height = targetHeight.toInt()
            cardView.layoutParams = layoutParams

        }
    }

    private fun setupScrollIndicator() {
        val scrollArrow = findViewById<ImageView>(R.id.scrollArrow)
        val scrollView = findViewById<NestedScrollView>(R.id.scrollView)
        val switchAuto = findViewById<SwitchMaterial>(R.id.switchAutomaticActivity)

        scrollArrow.setOnClickListener {
            // expandStatsContainer(), scrolliamo direttamente
            val totalHeight = scrollView.getChildAt(0).height
            val scrollViewHeight = scrollView.height
            val maxScroll = totalHeight - scrollViewHeight
            val currentPosition = scrollView.scrollY

            val targetPosition = if (switchAuto.isChecked) {
                (maxScroll * 4).toInt()  // 40% se checked
            } else {
                (maxScroll * 2).toInt()  // 25% se non checked
            }

            ObjectAnimator.ofInt(scrollView, "scrollY", currentPosition, targetPosition).apply {
                duration = 600
                interpolator = DecelerateInterpolator()
                start()
            }
        }

        val animation = AnimationSet(true).apply {
            addAnimation(AlphaAnimation(0.2f, 1.0f).apply {
                duration = 1000
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            })

            addAnimation(TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -0.1f,
                Animation.RELATIVE_TO_SELF, 0.1f
            ).apply {
                duration = 1000
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            })

            interpolator = AccelerateDecelerateInterpolator()
        }

        fun updateArrowAnimation(scrollY: Int) {
            val threshold = 50
            if (scrollY > threshold) {
                scrollArrow.clearAnimation()
                scrollArrow.visibility = View.GONE
            } else {
                scrollArrow.visibility = View.VISIBLE
                if (scrollArrow.animation == null) {
                    scrollArrow.startAnimation(animation)
                }
            }
        }

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            updateArrowAnimation(scrollY)
        }

        scrollArrow.startAnimation(animation)
    }

    private fun setupFadeEffect() {
        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView1)
        val fadeLeft = findViewById<View>(R.id.View_2)

        horizontalScrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollX = horizontalScrollView.scrollX
            fadeLeft.visibility = if (scrollX > 0) View.VISIBLE else View.GONE
        }
    }

    private fun showStopConfirmation() {
        val confirmationCard = findViewById<MaterialCardView>(R.id.confirmationCard)
        val timerProgressBar = findViewById<ProgressBar>(R.id.timerProgressBar)
        val btnConfirmStop = findViewById<TextView>(R.id.btnConfirmStop)
        val btnContinueActivity = findViewById<LinearLayout>(R.id.btnContinueActivity)
        val adContainer = findViewById<FrameLayout>(R.id.adContainer)
        val fabInterrompi = findViewById<FloatingActionButton>(R.id.fab_interrompi)
        val overlayView = findViewById<View>(R.id.overlayView)

        fabInterrompi.visibility = View.GONE
        fabAdd.visibility= View.GONE
        fabInterrompi.isEnabled = false
        fabAdd.isEnabled = false
        overlayView.visibility = View.VISIBLE

        adContainer.removeAllViews()

        // Crea un CardView per contenere l'annuncio
        val adCardView = MaterialCardView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 16f
            elevation = 8f

            // Imposta il colore in base al tema
            val isDynamicTheme = prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            if (isDynamicTheme) {
                setCardBackgroundColor(MaterialColors.getColor(
                    this@MainActivity, // Corretto il riferimento all'Activity
                    com.google.android.material.R.attr.colorPrimary,
                    ContextCompat.getColor(context, R.color.material_blue)
                ))
                strokeColor = MaterialColors.getColor(
                    this@MainActivity, // Corretto il riferimento all'Activity
                    com.google.android.material.R.attr.colorOnPrimary,
                    Color.WHITE
                )
            } else {
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.material_blue))
                strokeColor = Color.WHITE
            }

            strokeWidth = 2
        }

        val adView = AdView(this).apply {
            setAdSize(AdSize.MEDIUM_RECTANGLE)
            adUnitId = AD_UNIT_ID
            setPadding(8, 8, 8, 8)
        }

        adCardView.addView(adView)
        adContainer.addView(adCardView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adContainer.alpha = 0f
                adContainer.visibility = View.VISIBLE
                adContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                adContainer.visibility = View.GONE
            }
        }

        confirmationCard.visibility = View.VISIBLE
        confirmationCard.translationY = confirmationCard.height.toFloat()
        confirmationCard.animate()
            .translationY(0f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .start()

        timerProgressBar.max = 5000
        timerProgressBar.progress = 5000

        val timer = object : CountDownTimer(5000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                timerProgressBar.progress = millisUntilFinished.toInt()
            }
            override fun onFinish() {
                confirmationCard.animate()
                    .translationY(confirmationCard.height.toFloat())
                    .setDuration(300)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction {
                        confirmationCard.visibility = View.GONE
                        overlayView.visibility = View.GONE
                        Handler(Looper.getMainLooper()).postDelayed({
                            interrompiAttività()
                        }, 300)
                    }
                    .start()
            }
        }
        timer.start()

        btnConfirmStop.setOnClickListener {
            timer.cancel()
            confirmationCard.animate()
                .translationY(confirmationCard.height.toFloat())
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    confirmationCard.visibility = View.GONE
                    fabAdd.isEnabled = true
                    fabInterrompi.isEnabled = true
                    overlayView.visibility = View.GONE
                    Handler(Looper.getMainLooper()).postDelayed({
                        interrompiAttività()
                    }, 300)
                }
                .start()
        }

        btnContinueActivity.setOnClickListener {
            timer.cancel()
            confirmationCard.animate()
                .translationY(confirmationCard.height.toFloat())
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    confirmationCard.visibility = View.GONE
                    fabInterrompi.visibility = View.VISIBLE
                    fabInterrompi.isEnabled = true
                    overlayView.visibility = View.GONE
                }
                .start()
        }
    }

    private fun updateStepsProgress() {
        val passiDB = 0
        val totale = passiDB + statsManager.accumulatedSteps

        val progressPercent = ((totale.toFloat() / goalSteps) * 100).toInt()
        progressPassi.setIndicatorColor(ContextCompat.getColor(this, R.color.colore_fab2))
        progressPassi.max = 100

        if (totale >= goalSteps) {
            if (statsManager.accumulatedSteps > 0) {
                sendGoalReachedNotification()
            }

            statsManager.resetAccumulatedSteps()
            progressPassi.progress = 0

            val resetSpannable = SpanUtils.createColoredString(
                current = "0",
                currentColor = ContextCompat.getColor(this, R.color.colore_fab3),
                slashColor = ContextCompat.getColor(this, R.color.background_arancione_dark),
                goal = goalSteps.toString(),  // Usa goalSteps
                goalColor = ContextCompat.getColor(this, R.color.command_center_color1)
            )
            txtProgressPassiLabel.text = resetSpannable

        } else {
            progressPassi.progress = progressPercent

            val spannablePassi = SpanUtils.createColoredString(
                current = totale.toString(),
                currentColor = ContextCompat.getColor(this, R.color.colore_fab3),
                slashColor = ContextCompat.getColor(this, R.color.background_arancione_dark),
                goal = goalSteps.toString(),  // Usa goalSteps
                goalColor = ContextCompat.getColor(this, R.color.command_center_color1)
            )
            txtProgressPassiLabel.text = spannablePassi
        }
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("goal_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendGoalReachedNotification() {
        if (!prefs.getBoolean("all_notifications_enabled", true)) {
            Log.d("MainActivity", "Tutte le notifiche sono disabilitate: non invio la notifica.")
            return // Salta l'invio
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val contentText = when (attivitàCorrente) {
            "Camminare" -> "Hai raggiunto l'obiettivo di $goalSteps passi!"
            "Corsa" -> "Hai raggiunto l'obiettivo di ${goalRunningDistance / 1000}km di corsa!"
            "Sedersi" -> "Hai raggiunto l'obiettivo di ${goalMaxSittingTime / (1000 * 60 * 60)}h di seduta!"
            else -> "Hai raggiunto un obiettivo!"
        }

        val notification = NotificationCompat.Builder(this, "goal_channel_id")
            .setSmallIcon(R.drawable.barefoot_24dp)
            .setContentTitle("Obiettivo raggiunto!")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
        Log.d("MainActivity", "Notifica inviata: $contentText")
    }




    private fun updateRunningProgress() {
        val totalDistance = statsManager.accumulatedDistance
        val progress = ((totalDistance / goalRunningDistance) * 100).toInt().coerceIn(0, 100)
        progressCorsa.setIndicatorColor(ContextCompat.getColor(this, R.color.material_blue2))
        progressCorsa.max = 100

        if (totalDistance >= goalRunningDistance) {
            // Obiettivo raggiunto
            if (statsManager.accumulatedDistance > 0) {
                sendGoalReachedNotification()
            }
            statsManager.resetAccumulatedDistance()
            progressCorsa.progress = 0

            val resetSpannable = SpanUtils.createColoredString(
                current = "0m",
                currentColor = ContextCompat.getColor(this, R.color.colore_fab1),
                slashColor = ContextCompat.getColor(this, R.color.background_arancione_dark),
                goal = String.format("%.1fkm", goalRunningDistance / 1000f),
                goalColor = ContextCompat.getColor(this, R.color.command_center_color1)
            )
            txtProgressCorsaLabel.text = resetSpannable

            Log.d("RunningProgress", "Obiettivo Corsa raggiunto! Reset completo effettuato")
        } else {
            progressCorsa.progress = progress

            val distanceText = if (totalDistance < 1000) {
                "${totalDistance.toInt()}m"
            } else {
                String.format("%.2fkm", totalDistance / 1000f)
            }
            val goalText = String.format("%.1fkm", goalRunningDistance / 1000f)

            val spannableCorsa = SpanUtils.createColoredString(
                current = distanceText,
                currentColor = ContextCompat.getColor(this, R.color.colore_fab1),
                slashColor = ContextCompat.getColor(this, R.color.background_arancione_dark),
                goal = goalText,
                goalColor = ContextCompat.getColor(this, R.color.command_center_color1)
            )
            txtProgressCorsaLabel.text = spannableCorsa
        }
    }


    // Aggiorna il progress delle sedute
    private fun updateSittingProgress() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbTotalTime = db.attivitàDao().getTempoTotaleSeduto() ?: 0L
                val memoryTime = statsManager.accumulatedSittingTime
                val totalSittingTime = dbTotalTime + memoryTime

                val progressPercent = ((totalSittingTime.toFloat() / goalMaxSittingTime) * 100).toInt().coerceIn(0, 100)

                withContext(Dispatchers.Main) {
                    progressSedute.setIndicatorColor(ContextCompat.getColor(this@MainActivity, R.color.material_blue2))
                    progressSedute.max = 100

                    if (totalSittingTime >= goalMaxSittingTime) {
                        if (memoryTime > 0) {
                            sendGoalReachedNotification()
                        }
                        statsManager.resetAccumulatedSittingTime()
                        progressSedute.progress = 0

                        val resetSpannable = SpanUtils.createColoredString(
                            current = "0h",
                            currentColor = ContextCompat.getColor(this@MainActivity, R.color.colore_fab1),
                            slashColor = ContextCompat.getColor(this@MainActivity, R.color.background_arancione_dark),
                            goal = formatSittingTime(goalMaxSittingTime),
                            goalColor = ContextCompat.getColor(this@MainActivity, R.color.command_center_color1)
                        )
                        txtProgressSeduteLabel.text = resetSpannable

                        Log.d("SittingProgress", "Obiettivo Seduta raggiunto! Reset completo effettuato")
                    } else {
                        progressSedute.progress = progressPercent

                        val currentSittingText = formatSittingTime(totalSittingTime)
                        val goalSittingText = formatSittingTime(goalMaxSittingTime)

                        val spannableSedute = SpanUtils.createColoredString(
                            current = currentSittingText,
                            currentColor = ContextCompat.getColor(this@MainActivity, R.color.colore_fab1),
                            slashColor = ContextCompat.getColor(this@MainActivity, R.color.background_arancione_dark),
                            goal = goalSittingText,
                            goalColor = ContextCompat.getColor(this@MainActivity, R.color.command_center_color1)
                        )
                        txtProgressSeduteLabel.text = spannableSedute
                    }
                }
            } catch (e: Exception) {
                Log.e("SittingProgress", "Errore nell'aggiornamento del progresso", e)
            }
        }
    }


    // Funzione helper per formattare il tempo
    private fun formatSittingTime(timeMs: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs) % 60
        return "${hours}h"
    }



    private fun setupGoalsProgress() {
        progressPassi = findViewById(R.id.progressPassi)
        progressCorsa = findViewById(R.id.progressCorsa)
        progressSedute = findViewById(R.id.progressSedute)

        // Configura i progress indicator
        listOf(progressPassi, progressCorsa, progressSedute).forEach { progress ->
            progress.apply {
                this.progress = 0
            }
        }
        updateStepsProgress()
        updateRunningProgress()
        updateSittingProgress()
        // Log dei valori iniziali
        Log.d("SetupGoalsProgress", "Passi progress: ${progressPassi.progress}, max: ${progressPassi.max}")
        Log.d("SetupGoalsProgress", "Corsa progress: ${progressCorsa.progress}, max: ${progressCorsa.max}")
        Log.d("SetupGoalsProgress", "Sedute progress: ${progressSedute.progress}, max: ${progressSedute.max}")
    }

    private fun setupCharts() {
        val scrollView = findViewById<HorizontalScrollView>(R.id.chartsScrollView)
        val indicators = findViewById<LinearLayout>(R.id.pageIndicators)
        indicators.removeAllViews() // Pulisce eventuali indicatori esistenti

        val chartWidth = resources.getDimensionPixelSize(R.dimen.chart_width)
        val marginWidth = resources.getDimensionPixelSize(R.dimen.chart_margin)
        val totalItemWidth = chartWidth + (marginWidth * 2)

        // Determina il colore degli indicatori in base al tema
        val dotColor = when {
            // Se il tema dinamico è attivo
            prefs.getInt("app_color", 0) == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                MaterialColors.getColor(indicators, com.google.android.material.R.attr.colorPrimary)
            }
            // Altrimenti usa il colore statico
            else -> {
                ContextCompat.getColor(this, R.color.material_blue)
            }
        }

        // Crea gli indicatori con il colore corretto
        repeat(3) { i ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(12, 12).apply {
                    marginStart = if (i > 0) 16 else 0
                }
                // Crea un drawable programmaticamente con il colore corretto
                val shape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(dotColor)
                }
                background = shape
                alpha = if (i == 0) 1f else 0.4f
            }
            indicators.addView(dot)
        }

        var startScrollX = 0

        scrollView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startScrollX = scrollView.scrollX
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    val currentScrollX = scrollView.scrollX
                    val scrollDiff = currentScrollX - startScrollX

                    val currentPage = (currentScrollX + totalItemWidth/2) / totalItemWidth
                    val targetPage = when {
                        scrollDiff > totalItemWidth/4 -> minOf(currentPage + 1, 2)
                        scrollDiff < -totalItemWidth/4 -> maxOf(currentPage - 1, 0)
                        else -> currentPage
                    }

                    // Animazione smooth dello scroll
                    scrollView.smoothScrollTo(targetPage * totalItemWidth, 0)

                    // Aggiorna l'alpha degli indicatori con animazione
                    repeat(3) { i ->
                        indicators.getChildAt(i)?.animate()
                            ?.alpha(if (i == targetPage) 1f else 0.4f)
                            ?.setDuration(300)
                            ?.start()
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    // Aggiorna l'alpha degli indicatori durante lo scroll
                    val scrollProgress = scrollView.scrollX.toFloat() / totalItemWidth
                    repeat(3) { i ->
                        val distance = abs(i - scrollProgress)
                        val alpha = 1f - (distance * 0.6f).coerceIn(0f, 0.6f)
                        indicators.getChildAt(i)?.alpha = alpha
                    }
                    false
                }
                else -> false
            }
        }

        lifecycleScope.launch {
            chartsManager.setupCharts(walkingChart, runningChart, sittingChart)
        }
    }

    override fun onMapStyleChanged(style: Int) {
        // Quando lo stile cambia, aggiorna la mappa
        if (::mMap.isInitialized) {
            // Salva nelle preferenze
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            prefs.edit().putInt("map_style", style).apply()
            onMapReady(mMap)
        }
    }

}

