package com.example.mover

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Calendar

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    fun Int.toHexString(): String = String.format("#%08X", this)


    override fun onCreate(savedInstanceState: Bundle?) {

        prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        // Carica il tema salvato prima di super.onCreate()
        val savedTheme = prefs.getInt("app_theme", 0)
        applyTheme(savedTheme)
        val colorMode = prefs.getInt("app_color", 0) // 0 = Usa activity_settings_layout, 1 = Usa activity_settings_layout1

        if (colorMode == 0) {
            setTheme(R.style.Theme_Settings) // Applica il tema personalizzato
        }else if (colorMode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setTheme(R.style.Theme_Settings1)
        }

        super.onCreate(savedInstanceState)

        // 🔹 Se colorMode è 0, usa il layout standard; altrimenti, usa il layout alternativo
        if (colorMode == 0) {
            setContentView(R.layout.activity_settings_layout)
            Log.d("SettingsLifecycle", "Usando activity_settings_layout.xml")
        } else {
            setContentView(R.layout.activity_settings_layout1)
            Log.d("SettingsLifecycle", "Usando activity_settings_layout1.xml")
        }

        setupToolbar()
        setupThemeAndStatusBar()
        setupClickListeners()
        loadSettings()
        findViewById<LinearLayout>(R.id.nondisturbare_container).setOnClickListener {
            Log.d("SettingsLifecycle", "Click su nondisturbare_container")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    startActivity(intent)
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                }
            }
        }
        applySettingsItemStyle(colorMode)

    }

    override fun onResume() {
        super.onResume()

        // Controlla il permesso effettivo delle notifiche
        val notificationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
        findViewById<SwitchMaterial>(R.id.switchNotificheObiettivi).isChecked = notificationEnabled
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        val nestedScrollView = findViewById<NestedScrollView>(R.id.nested_scroll_view)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        // Check color mode and night mode
        val colorMode = prefs.getInt("app_color", 0)
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        // Set toolbar background color based on color mode and theme
        if (colorMode == 0 && nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            toolbar.setBackgroundColor(resources.getColor(R.color.nero_sfondo_dark))
        }

        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            Log.d("ScrollDebug", "ScrollY: $scrollY")
            Log.d("ScrollDebug", "OldScrollY: $oldScrollY")

            // Test con valori di soglia per l'animazione
            val maxScroll = 200f // punto in cui l'animazione si completa
            val percentage = (scrollY.coerceAtMost(maxScroll.toInt()) / maxScroll).coerceIn(0f, 1f)
            Log.d("ScrollDebug", "Percentage: $percentage")

            val maxTextSize = 32f
            val minTextSize = 24f
            val newTextSize = maxTextSize - ((maxTextSize - minTextSize) * percentage)
            Log.d("ScrollDebug", "New Text Size: $newTextSize")

            toolbarTitle.textSize = newTextSize
        })
    }

    private fun setupThemeAndStatusBar() {
        val colorMode = prefs.getInt("app_color", 0)
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val mainContainer = findViewById<LinearLayout>(R.id.main_container)
        val isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        // Trova il LinearLayout interno che deve usare rounded2 in tema scuro
        val innerLayout = mainContainer.getChildAt(0) as? LinearLayout
        val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
        val rootView = window.decorView.findViewById<View>(android.R.id.content)

        // Trova la toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        if (colorMode == 1) {
            // Modalità colori dinamici
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                window.decorView.systemUiVisibility = 0
                val grigioChiaro = ContextCompat.getColor(this, R.color.grigio_chiaro)
                mainContainer.setBackgroundColor(grigioChiaro)
                window.navigationBarColor = grigioChiaro // Imposta il colore della navigation bar
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                val grigioScuro = ContextCompat.getColor(this, R.color.grigio_scuro)
                mainContainer.setBackgroundColor(grigioScuro)
                window.navigationBarColor = grigioScuro // Imposta il colore della navigation bar
            }
        } else {
            // Modalità colori dell'app
            if (isNightMode) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                // Tema scuro standard
                val grigioDark = ContextCompat.getColor(this, R.color.grigio_chiaro) // Assicurati di definire questo colore

                appBarLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.nero_sfondo_dark))
                window.statusBarColor = ContextCompat.getColor(this, R.color.nero_sfondo_dark)
                mainContainer.setBackgroundColor(grigioDark)
                window.navigationBarColor = grigioDark
                window.decorView.systemUiVisibility = 0
                rootView.setBackgroundColor(grigioDark)

                // Imposta rounded2 per il layout interno in tema scuro standard
                innerLayout?.setBackgroundResource(R.drawable.rounded2)
            } // Tema chiaro standard
            val grigioScuro = ContextCompat.getColor(this, R.color.grigio_scuro)

            mainContainer.setBackgroundColor(grigioScuro)
            window.navigationBarColor = grigioScuro
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    private fun setupClickListeners() {
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnStorico = findViewById<ImageButton>(R.id.btnStorico)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        // Pulsante Home → Torna alla schermata principale
        btnHome.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val intent = Intent(this, MainActivity::class.java)
            // Optional: Clear the back stack so MainActivity is the only activity remaining
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        // Pulsante Storico → Apre StoricoAttivitaActivity
        btnStorico.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            else {
                it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            val intent = Intent(this, StoricoAttivitaActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        // Switch per TUTTE le notifiche
        val switchAllNotifiche = findViewById<SwitchMaterial>(R.id.switchAllNotifiche)
        // Switch Notifiche obiettivi (già esistente, ti basta ricontrollare se lo hai definito altrove)
        val switchNotificheObiettivi = findViewById<SwitchMaterial>(R.id.switchNotificheObiettivi)

        switchAllNotifiche.setOnCheckedChangeListener { _, isChecked ->
            // Se TUTTE le notifiche sono abilitate, abilito/sblocco l’altro switch
            switchNotificheObiettivi.isEnabled = isChecked
            prefs.edit().putBoolean("tutte_notifiche", isChecked).apply()
        }

        // Notifiche obiettivi
        findViewById<SwitchMaterial>(R.id.switchNotificheObiettivi).setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                // Apri le impostazioni di sistema per le notifiche
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            prefs.edit().putBoolean("notifiche_obiettivi", isChecked).apply()
        }

        // Non disturbare
        findViewById<LinearLayout>(R.id.nondisturbare_container).setOnClickListener {
            showNonDisturbarePicker()
        }

        // Obiettivo passi
        findViewById<LinearLayout>(R.id.obiettivo_passi_container).setOnClickListener {
            showPassiPicker()
        }
        findViewById<LinearLayout>(R.id.obiettivo_passi_container1).setOnClickListener {
            showPassiPicker()
        }

        // Obiettivo corsa
        findViewById<LinearLayout>(R.id.obiettivo_corsa_container).setOnClickListener {
            showCorsaPicker()
        }

        findViewById<LinearLayout>(R.id.obiettivo_corsa_container1).setOnClickListener {
            showCorsaPicker()
        }

        // Limite tempo seduto
        findViewById<LinearLayout>(R.id.limite_seduto_container).setOnClickListener {
            showTempoSedutoPicker()
        }

        findViewById<LinearLayout>(R.id.limite_seduto_container1).setOnClickListener {
            showTempoSedutoPicker()
        }

        // Stile mappa
        findViewById<LinearLayout>(R.id.stile_mappa_container).setOnClickListener {
            showMapStylePicker()
        }

        findViewById<LinearLayout>(R.id.stile_mappa_container1).setOnClickListener {
            showMapStylePicker()
        }

        // Tema
        findViewById<LinearLayout>(R.id.tema_container).setOnClickListener {
            showThemePicker()
        }

        findViewById<LinearLayout>(R.id.tema_container1).setOnClickListener {
            showThemePicker()
        }

        // Colore principale
        findViewById<LinearLayout>(R.id.colore_container).setOnClickListener {
            showColorPicker()
        }

        findViewById<LinearLayout>(R.id.colore_container1).setOnClickListener {
            showColorPicker()
        }
    }

    private fun showThemePicker() {
        val themes = arrayOf("Predefinito di sistema", "Chiaro", "Scuro")
        val currentTheme = prefs.getInt("app_theme", 0)
        val colorMode = prefs.getInt("app_color", 0)
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        val dialogBuilder = if (colorMode == 0) {
            MaterialAlertDialogBuilder(this,
                if (isNightMode) R.style.ThemeOverlay_App_MaterialAlertDialog_Dark
                else R.style.ThemeOverlay_App_MaterialAlertDialog
            )
        } else {
            MaterialAlertDialogBuilder(this)
        }

        dialogBuilder
            .setTitle("Scegli il tema")
            .setSingleChoiceItems(themes, currentTheme) { dialog, which ->
                prefs.edit().putInt("app_theme", which).apply()
                updateThemeText()
                applyTheme(which)
                dialog.dismiss()
            }
            .show()
    }

    private fun showColorPicker() {
        val themes = arrayOf("Colori dell'app", "Colori del sistema")
        val currentColor = prefs.getInt("app_color", 0)
        val colorMode = prefs.getInt("app_color", 0)
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        val dialogBuilder = if (colorMode == 0) {
            MaterialAlertDialogBuilder(this,
                if (isNightMode) R.style.ThemeOverlay_App_MaterialAlertDialog_Dark
                else R.style.ThemeOverlay_App_MaterialAlertDialog
            )
        } else {
            MaterialAlertDialogBuilder(this)
        }

        dialogBuilder
            .setTitle("Scegli il colore principale")
            .setSingleChoiceItems(themes, currentColor) { dialog, which ->
                prefs.edit().putInt("app_color", which).apply()
                dialog.dismiss()
                val intent = intent
                finish()
                startActivity(intent)
            }
            .show()
    }


    private fun updateColorText() {
        val colorMode = prefs.getInt("app_color", 0)
        val text = when (colorMode) {
            0 -> "Colori dell'app"
            1 -> "Colori del sistema"
            else -> "Colori dell'app"
        }
        findViewById<TextView>(R.id.txt_colore_value).text = text
    }



    private fun updateAppThemeWithCustomColor(selectedColor: Int) {
        // Salva la preferenza per i colori di sistema
        prefs.edit()
            .putInt("app_color", 1) // 1 = colori di sistema
            .apply()


        // Ricrea l'activity per applicare i cambiamenti
        recreate()
    }



    private fun updateThemeText() {
        val theme = when(prefs.getInt("app_theme", 0)) {
            0 -> "Predefinito di sistema"
            1 -> "Chiaro"
            2 -> "Scuro"
            else -> "Predefinito di sistema"
        }
        findViewById<TextView>(R.id.txt_tema_value).text = theme
    }


    private fun applyTheme(theme: Int) {
        when (theme) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }



    private fun loadSettings() {
        // Carica le impostazioni salvate
        findViewById<SwitchMaterial>(R.id.switchNotificheObiettivi).isChecked =
            prefs.getBoolean("notifiche_obiettivi", true)

        val allNotifiche = prefs.getBoolean("tutte_notifiche", true)
        findViewById<SwitchMaterial>(R.id.switchAllNotifiche).isChecked = allNotifiche

        val obiettiviNotifiche = prefs.getBoolean("notifiche_obiettivi", true)
        findViewById<SwitchMaterial>(R.id.switchNotificheObiettivi).apply {
            isChecked = obiettiviNotifiche
            isEnabled = allNotifiche   // se “tutte notifiche” è false, disabilita
        }

        // Aggiorna i testi con i valori salvati
        updateObjectiveTexts()
        updateThemeText()
        updateColorText()
        updateMapStyleText()

    }




    private fun updateObjectiveTexts() {
        // Aggiorna i testi con i valori attuali
        val passiGoal = prefs.getInt("obiettivo_passi", 10000)
        findViewById<TextView>(R.id.txt_obiettivo_passi_value).text = "$passiGoal passi"

        val corsaGoal = prefs.getFloat("obiettivo_corsa", 5.0f)
        findViewById<TextView>(R.id.txt_obiettivo_corsa_value).text = "$corsaGoal km"

        val sedutoLimit = prefs.getInt("limite_seduto", 8)
        findViewById<TextView>(R.id.txt_limite_seduto_value).text = "$sedutoLimit ore"
    }

    private fun showNonDisturbarePicker() {
        val startTime = prefs.getInt("non_disturbare_start", 2200)
        val endTime = prefs.getInt("non_disturbare_end", 800)
        val colorMode = prefs.getInt("app_color", 0)
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        val builder = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(startTime / 100)
            .setMinute(startTime % 100)
            .setTitleText("Seleziona l'ora di inizio")

        if (colorMode == 0) {
            builder.setTheme(
                if (isNightMode) R.style.ThemeOverlay_App_TimePicker_Dark
                else R.style.ThemeOverlay_App_TimePicker
            )
        }

        val startPicker = builder.build()

        startPicker.addOnPositiveButtonClickListener {
            val newStartTime = startPicker.hour * 100 + startPicker.minute
            prefs.edit().putInt("non_disturbare_start", newStartTime).apply()
            showEndTimePicker(newStartTime, endTime)
        }

        startPicker.show(supportFragmentManager, "start_time_picker")
    }

    private fun showEndTimePicker(newStartTime: Int, currentEndTime: Int) {
        val colorMode = prefs.getInt("app_color", 0)
        val builder = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(currentEndTime / 100)
            .setMinute(currentEndTime % 100)
            .setTitleText("Seleziona l'ora di fine")

        if (colorMode == 0) {
            builder.setTheme(R.style.ThemeOverlay_App_TimePicker)
        }

        val endPicker = builder.build()

        endPicker.addOnPositiveButtonClickListener {
            val newEndTime = endPicker.hour * 100 + endPicker.minute
            prefs.edit().putInt("non_disturbare_end", newEndTime).apply()
            updateNonDisturbare()
            scheduleDndAlarms(this, newStartTime, newEndTime)
        }

        endPicker.show(supportFragmentManager, "end_time_picker")
    }

    private fun showPassiPicker() {
        val currentGoal = prefs.getInt("obiettivo_passi", 10000)
        val colorMode = prefs.getInt("app_color", 0)
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val view = layoutInflater.inflate(R.layout.dialog_slider, null)
        val slider = view.findViewById<Slider>(R.id.slider)

        slider.apply {
            valueFrom = 5000f
            valueTo = 20000f
            stepSize = 1000f
            value = currentGoal.toFloat()
        }

        val dialogBuilder = if (colorMode == 0) {
            MaterialAlertDialogBuilder(this,
                if (isNightMode) R.style.ThemeOverlay_App_MaterialAlertDialog_Dark
                else R.style.ThemeOverlay_App_MaterialAlertDialog
            )
        } else {
            MaterialAlertDialogBuilder(this)
        }

        dialogBuilder
            .setTitle("Obiettivo passi giornaliero")
            .setView(view)
            .setPositiveButton("Conferma") { _, _ ->
                val newGoal = slider.value.toInt()
                prefs.edit().putInt("obiettivo_passi", newGoal).apply()
                updateObjectiveTexts()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun showCorsaPicker() {
        val currentGoal = prefs.getFloat("obiettivo_corsa", 5.0f)
        val colorMode = prefs.getInt("app_color", 0)
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val view = layoutInflater.inflate(R.layout.dialog_slider, null)
        val slider = view.findViewById<Slider>(R.id.slider)

        slider.apply {
            valueFrom = 1f
            valueTo = 20f
            stepSize = 0.5f
            value = currentGoal
        }

        val dialogBuilder = if (colorMode == 0) {
            MaterialAlertDialogBuilder(this,
                if (isNightMode) R.style.ThemeOverlay_App_MaterialAlertDialog_Dark
                else R.style.ThemeOverlay_App_MaterialAlertDialog
            )
        } else {
            MaterialAlertDialogBuilder(this)
        }

        dialogBuilder
            .setTitle("Obiettivo corsa settimanale")
            .setView(view)
            .setPositiveButton("Conferma") { _, _ ->
                val newGoal = slider.value
                prefs.edit().putFloat("obiettivo_corsa", newGoal).apply()
                updateObjectiveTexts()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun showTempoSedutoPicker() {
        val currentLimit = prefs.getInt("limite_seduto", 8)
        val colorMode = prefs.getInt("app_color", 0)
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val view = layoutInflater.inflate(R.layout.dialog_slider, null)
        val slider = view.findViewById<Slider>(R.id.slider)

        slider.apply {
            valueFrom = 4f
            valueTo = 12f
            stepSize = 1f
            value = currentLimit.toFloat()
        }

        val dialogBuilder = if (colorMode == 0) {
            MaterialAlertDialogBuilder(this,
                if (isNightMode) R.style.ThemeOverlay_App_MaterialAlertDialog_Dark
                else R.style.ThemeOverlay_App_MaterialAlertDialog
            )
        } else {
            MaterialAlertDialogBuilder(this)
        }

        dialogBuilder
            .setTitle("Limite tempo seduto")
            .setView(view)
            .setPositiveButton("Conferma") { _, _ ->
                val newLimit = slider.value.toInt()
                prefs.edit().putInt("limite_seduto", newLimit).apply()
                updateObjectiveTexts()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }



    private fun showMapStylePicker() {
        val styles = arrayOf("Automatico", "Chiaro", "Scuro")
        val currentStyle = prefs.getInt("map_style", 0)
        val colorMode = prefs.getInt("app_color", 0)
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        val dialogBuilder = if (colorMode == 0) {
            MaterialAlertDialogBuilder(this,
                if (isNightMode) R.style.ThemeOverlay_App_MaterialAlertDialog_Dark
                else R.style.ThemeOverlay_App_MaterialAlertDialog
            )
        } else {
            MaterialAlertDialogBuilder(this)
        }

        dialogBuilder
            .setTitle("Stile mappa")
            .setSingleChoiceItems(styles, currentStyle) { dialog, which ->
                prefs.edit().putInt("map_style", which).apply()
                updateMapStyleText()
                dialog.dismiss()
            }
            .show()
    }

    private fun updateMapStyleText() {
        val style = when(prefs.getInt("map_style", 0)) {
            0 -> "Automatico (segue il tema del sistema)"
            1 -> "Chiaro"
            2 -> "Scuro"
            else -> "Automatico"
        }
        Log.d("SettingsActivity", "Stile mappa aggiornato a: $style")
        findViewById<TextView>(R.id.txt_stile_mappa_value).text = style
    }


    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "Le notifiche sono disabilitate",
                Toast.LENGTH_LONG
            ).show()
            findViewById<SwitchMaterial>(R.id.switchNotificheObiettivi).isChecked = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateNonDisturbare() {
        val startTime = prefs.getInt("non_disturbare_start", 2200)
        val endTime = prefs.getInt("non_disturbare_end", 800)

        val startHour = startTime / 100
        val startMinute = startTime % 100
        val endHour = endTime / 100
        val endMinute = endTime % 100

        val startTimeFormatted = String.format("%02d:%02d", startHour, startMinute)
        val endTimeFormatted = String.format("%02d:%02d", endHour, endMinute)

        findViewById<TextView>(R.id.txt_non_disturbare_value).text =
            "$startTimeFormatted - $endTimeFormatted"
    }

    fun scheduleDndAlarms(context: Context, startHHmm: Int, endHHmm: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val activateIntent = Intent(context, DndReceiver::class.java).apply {
            action = "ACTION_ENABLE_DND"
        }
        val activatePendingIntent = PendingIntent.getBroadcast(
            context,
            1001, // requestCode
            activateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val activateTimeInMillis = calculateNextTriggerMillis(startHHmm)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            activateTimeInMillis,
            AlarmManager.INTERVAL_DAY,
            activatePendingIntent
        )

        val deactivateIntent = Intent(context, DndReceiver::class.java).apply {
            action = "ACTION_DISABLE_DND"
        }
        val deactivatePendingIntent = PendingIntent.getBroadcast(
            context,
            1002, // requestCode
            deactivateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val deactivateTimeInMillis = calculateNextTriggerMillis(endHHmm)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            deactivateTimeInMillis,
            AlarmManager.INTERVAL_DAY,
            deactivatePendingIntent
        )
    }

    fun calculateNextTriggerMillis(hhmm: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            val hour = hhmm / 100
            val minute = hhmm % 100
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return target.timeInMillis
    }

    private fun applySettingsItemStyle(colorMode: Int) {
        if (colorMode == 1) {
            // Applica lo stile dinamico a tutti gli elementi
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.colorSecondary, typedValue, true)
        }
    }

}

