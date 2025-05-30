package com.example.mover

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.CalendarView
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat.performHapticFeedback
import androidx.lifecycle.lifecycleScope
import com.example.mover.data.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class StoricoAttivitaActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var calendarView: CalendarView
    private lateinit var activityChart: BarChart
    private lateinit var timeSegmentGroup: LinearLayout
    private lateinit var yearButton: SegmentedButton
    private lateinit var monthButton: SegmentedButton
    private lateinit var tenDaysButton: SegmentedButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var prefs: SharedPreferences

    private var currentDayOffset = 0
    private var currentMonthOffset = 0
    private var currentYearOffset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Ottieni le preferenze e controlla la modalità colore prima di applicare il tema
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val colorMode = prefs.getInt("app_color", 0) // 0 = Colori standard, 1 = Colori dinamici

        // Applica il tema appropriato in base alla modalità colore
        if (colorMode == 0) {
            setTheme(R.style.Theme_MyApp_StoricoAttivitaActivity)
        } else if (colorMode == 1) {
            setTheme(R.style.Theme_MyApp_StoricoAttivitaActivity_Dynamic)

            // Applica i colori dinamici se disponibili
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                DynamicColors.applyToActivityIfAvailable(this)
            }
        }

        super.onCreate(savedInstanceState)

        // Carica il layout appropriato in base alla modalità colore
        if (colorMode == 0) {
            setContentView(R.layout.activity_storico_attivita)
            Log.d("StoricoLifecycle", "Usando activity_storico_attivita.xml")
        } else {
            setContentView(R.layout.activity_storico_attivita_dynamic)

            Log.d("StoricoLifecycle", "Usando activity_storico_attivita_dynamic.xml")
        }



        // Imposta lo stato grafico solo se non si usa il tema dinamico
        if (colorMode == 0) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.command_center_color1)

            if (isLightTheme()) {
                window.navigationBarColor = ContextCompat.getColor(this, R.color.colore_Storico_back)
            } else {
                window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent_b)
            }
        }


        // Continua con il resto del codice di inizializzazione...
        findViewById<FrameLayout>(R.id.homeContainer).background = null
        val storicoContainer = findViewById<FrameLayout>(R.id.storicoContainer)
        val btnStorico = findViewById<ImageButton>(R.id.btnStorico)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        btnHome.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val intent = Intent(this, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }


        if (colorMode == 0) {
            // Applica lo sfondo personalizzato solo se NON è attivo il tema dinamico
            storicoContainer.background = ContextCompat.getDrawable(this, R.drawable.custum_circle_main_back)
        }

        db = AppDatabase.getDatabase(this)
        calendarView = findViewById(R.id.calendarView)
        activityChart = findViewById(R.id.activityChart)

        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)

        setupTimeSegments()

        btnPrevious.setOnClickListener {
            when {
                tenDaysButton.isSelected -> {
                    currentDayOffset += 1
                    setupActivityChart()
                }
                monthButton.isSelected -> {
                    currentMonthOffset += 1
                    setupMonthlyChart()
                }
                yearButton.isSelected -> {
                    currentYearOffset += 1
                    setupYearlyChart()
                }
            }
            updateButtonVisibility()
        }


        btnNext.setOnClickListener {
            when {
                tenDaysButton.isSelected -> {
                    if (currentDayOffset > 0) {
                        currentDayOffset -= 1
                        setupActivityChart()
                    }
                }
                monthButton.isSelected -> {
                    if (currentMonthOffset > 0) {
                        currentMonthOffset -= 1
                        setupMonthlyChart()
                    }
                }
                yearButton.isSelected -> {
                    if (currentYearOffset > 0) {
                        currentYearOffset -= 1
                        setupYearlyChart()
                    }
                }
            }
            updateButtonVisibility()
        }

        calendarView.maxDate = Calendar.getInstance().timeInMillis
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dataSelezionata = "$dayOfMonth/${month + 1}/$year"
            val intent = Intent(this, DettaglioGiornoActivity::class.java).apply {
                putExtra("DATA_SELEZIONATA", dataSelezionata)
            }
            startActivity(intent)
        }

        if (colorMode == 1) {
            // Tema dinamico
            calendarView.setBackgroundColor(getColorFromAttr(com.google.android.material.R.attr.colorSurfaceVariant))

            // Imposta i colori del selettore di date
            try {
                // Accedi ai campi privati del CalendarView per personalizzare i colori
                val weekDayTextAppearanceId = resources.getIdentifier("weekDayTextAppearance", "id", "android")
                if (weekDayTextAppearanceId != 0) {
                    val field = CalendarView::class.java.getDeclaredField("mWeekDayTextAppearanceResId")
                    field.isAccessible = true
                    field.set(calendarView, getColorFromAttr(com.google.android.material.R.attr.colorPrimary).toInt())
                }
            } catch (e: Exception) {
                Log.e("StoricoActivity", "Errore nell'applicare i colori al calendario: ${e.message}")
            }
        }

        val typedValue = TypedValue()
        // Imposta sfondo colorato per l'area del calendario
        val calendarContainer = findViewById<com.google.android.material.card.MaterialCardView>(R.id.calendarCard)
        theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true)
        calendarContainer.setCardBackgroundColor(typedValue.data)

        val rippleColor = ColorStateList.valueOf(
            ColorUtils.setAlphaComponent(
                ContextCompat.getColor(this, R.color.material_blue),
                80
            )
        )

        btnPrevious.apply {
            background = null
            foreground = RippleDrawable(rippleColor, null, null)
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
        }

        btnNext.apply {
            background = null
            foreground = RippleDrawable(rippleColor, null, null)
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
        }

        btnPrevious.visibility = View.GONE
        btnNext.visibility = View.GONE
        val clickEffect = ColorStateList.valueOf(rippleColor.defaultColor)
        btnPrevious.foreground = RippleDrawable(clickEffect, null, null)
        btnNext.foreground = RippleDrawable(clickEffect, null, null)
        setupActivityChart()
        updateButtonVisibility()
    }

    private fun setupActivityChart() {
        val chartTitle = when (currentDayOffset) {
            0 -> "Ultimi 10 giorni"
            1 -> "10 giorni precedenti"
            else -> "Altri 10 giorni"
        }
        findViewById<TextView>(R.id.chartTitle).text = chartTitle

        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val offsetDays = currentDayOffset * 10
            calendar.add(Calendar.DAY_OF_YEAR, -9 - offsetDays)
            val entries = mutableListOf<BarEntry>()
            val dates = mutableListOf<String>()

            for (i in 0..9) {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                val displayDate = when {
                    i == 9 && currentDayOffset == 0 -> "Oggi"
                    i == 8 && currentDayOffset == 0 -> "Ieri"
                    else -> {
                        val day = SimpleDateFormat("d", Locale("it")).format(calendar.time)
                        val month = calendar.get(Calendar.MONTH)
                        val monthLetter = when (month) {
                            0 -> "g"  // Gennaio
                            1 -> "f"  // Febbraio
                            2 -> "m"  // Marzo
                            3 -> "a"  // Aprile
                            4 -> "m"  // Maggio
                            5 -> "g"  // Giugno
                            6 -> "l"  // Luglio
                            7 -> "a"  // Agosto
                            8 -> "s"  // Settembre
                            9 -> "o"  // Ottobre
                            10 -> "n" // Novembre
                            11 -> "d" // Dicembre
                            else -> ""
                        }
                        "$day$monthLetter"
                    }
                }
                dates.add(displayDate)
                val activities = withContext(Dispatchers.IO) {
                    db.attivitàDao().getAttivitàPerData(date)
                }

                val walkCount = activities.count { it.tipo == "Camminare" }.toFloat()
                val runCount = activities.count { it.tipo == "Corsa" }.toFloat()
                val driveCount = activities.count { it.tipo == "Guidare" }.toFloat()
                val sedersiCount = activities.count { it.tipo == "Sedersi" }.toFloat()

                entries.add(BarEntry(i.toFloat(), floatArrayOf(walkCount, runCount, driveCount, sedersiCount)))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            configureChart(entries, dates)
        }
    }

    private fun setupMonthlyChart() {
        val chartTitle = when (currentMonthOffset) {
            0 -> "Questo mese"
            1 -> "Mese precedente"
            else -> "$currentMonthOffset mesi fa"
        }
        findViewById<TextView>(R.id.chartTitle).text = chartTitle

        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -currentMonthOffset)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            val currentMonthName = SimpleDateFormat("MMMM", Locale("it"))
                .format(calendar.time)
                .replaceFirstChar { it.uppercaseChar() }
            monthButton.text = currentMonthName

            val currentMonth = calendar.get(Calendar.MONTH)
            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val entries = mutableListOf<BarEntry>()
            val weeks = mutableListOf<String>()

            val numWeeks = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)

            for (week in 0 until numWeeks) {
                val weekStartCal = calendar.clone() as Calendar
                val weekStart = weekStartCal.time
                val weekEndCal = calendar.clone() as Calendar
                weekEndCal.add(Calendar.DAY_OF_WEEK, 6)

                if (weekEndCal.get(Calendar.MONTH) != currentMonth) {
                    weekEndCal.set(Calendar.MONTH, currentMonth)
                    weekEndCal.set(Calendar.DAY_OF_MONTH, lastDayOfMonth)
                } else if (weekEndCal.get(Calendar.DAY_OF_MONTH) > lastDayOfMonth) {
                    weekEndCal.set(Calendar.DAY_OF_MONTH, lastDayOfMonth)
                }

                val weekEnd = weekEndCal.time
                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(weekStart)
                val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(weekEnd)

                val startDay = SimpleDateFormat("d", Locale.getDefault()).format(weekStart)
                val endDay = SimpleDateFormat("d", Locale.getDefault()).format(weekEnd)
                val weekLabel = "dal $startDay al $endDay"
                weeks.add(weekLabel)

                val activities = withContext(Dispatchers.IO) {
                    db.attivitàDao().getAttivitàPerPeriodo(startDate, endDate)
                }

                val walkCount = activities.count { it.tipo == "Camminare" }.toFloat()
                val runCount = activities.count { it.tipo == "Corsa" }.toFloat()
                val driveCount = activities.count { it.tipo == "Guidare" }.toFloat()
                val sedersiCount = activities.count { it.tipo == "Sedersi" }.toFloat()

                entries.add(BarEntry(week.toFloat(), floatArrayOf(walkCount, runCount, driveCount, sedersiCount)))

                calendar.time = weekStart
                calendar.add(Calendar.DAY_OF_MONTH, 7)
            }

            configureChart(entries, weeks)
        }
    }

    private fun setupYearlyChart() {
        val chartTitle = when (currentYearOffset) {
            0 -> "Questo anno"
            1 -> "Anno precedente"
            else -> "$currentYearOffset anni fa"
        }
        findViewById<TextView>(R.id.chartTitle).text = chartTitle

        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -currentYearOffset)

            val displayYear = calendar.get(Calendar.YEAR).toString()
            yearButton.text = displayYear

            val entries = mutableListOf<BarEntry>()
            val months = mutableListOf<String>()

            calendar.set(Calendar.DAY_OF_YEAR, 1)

            for (month in 0..11) {
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, 1)

                val abbreviatedMonthName = SimpleDateFormat("MMM", Locale("it"))
                    .format(calendar.time)
                    .replaceFirstChar { it.uppercaseChar() }
                months.add(abbreviatedMonthName)

                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, lastDay)
                val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                calendar.set(Calendar.DAY_OF_MONTH, 1)

                val activities = withContext(Dispatchers.IO) {
                    db.attivitàDao().getAttivitàPerPeriodo(startDate, endDate)
                }

                val walkCount = activities.count { it.tipo == "Camminare" }.toFloat()
                val runCount = activities.count { it.tipo == "Corsa" }.toFloat()
                val driveCount = activities.count { it.tipo == "Guidare" }.toFloat()
                val sedersiCount = activities.count { it.tipo == "Sedersi" }.toFloat()

                entries.add(BarEntry(month.toFloat(), floatArrayOf(walkCount, runCount, driveCount, sedersiCount)))
            }

            configureChart(entries, months)
        }
    }

    // Replace the configureChart method in StoricoAttivitaActivity
    private fun configureChart(entries: List<BarEntry>, labels: List<String>) {
        activityChart.apply {
            clear()
            xAxis.valueFormatter = null
            data = null

            val colorMode = prefs.getInt("app_color", 0)

            val dataSet = BarDataSet(entries, "").apply {
                if (colorMode == 0) {
                    // Colori statici
                    setColors(
                        ContextCompat.getColor(context, R.color.colorCamminare),
                        ContextCompat.getColor(context, R.color.colorCorrere),
                        ContextCompat.getColor(context, R.color.colorGuidare),
                        ContextCompat.getColor(context, R.color.colorSedersi)
                    )
                } else {
                    // Ottieni colori dal tema attuale
                    val typedValue = TypedValue()

                    context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
                    val primaryColor = typedValue.data

                    context.theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true)
                    val secondaryColor = typedValue.data

                    // Crea colori derivati
                    val tertiaryColor = ColorUtils.setAlphaComponent(primaryColor, 150)
                    val quaternaryColor = ColorUtils.setAlphaComponent(secondaryColor, 150)

                    setColors(primaryColor, secondaryColor, tertiaryColor, quaternaryColor)
                }
                setDrawValues(false)
            }

            val barData = BarData(dataSet).apply { barWidth = 0.8f }

            data = barData

            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            setScaleEnabled(false)
            isDoubleTapToZoomEnabled = false
            setPinchZoom(false)
            animateY(1000)

            val marker = ActivityMarkerView(context)
            marker.chartView = this
            markerView = marker

            setDrawMarkers(true)
            isHighlightPerTapEnabled = true
            isHighlightPerDragEnabled = true

            invalidate()
        }
        updateDateBar(labels)
    }

    // Aggiungi questo metodo alla tua classe StoricoAttivitaActivity

    private fun updateSegmentButtonsForDynamicTheme() {
        val colorMode = prefs.getInt("app_color", 0)

        if (colorMode == 1) {
            // Ottieni colori dal tema dinamico
            val typedValue = TypedValue()

            // Colore primario per il bottone selezionato
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            val colorPrimary = typedValue.data

            // Colore del testo per il bottone selezionato
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
            val colorOnPrimary = typedValue.data

            // Colore di sfondo per i bottoni non selezionati
            theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true)
            val colorSurfaceVariant = typedValue.data

            // Colore del testo per i bottoni non selezionati
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true)
            val colorOnSurfaceVariant = typedValue.data

            // Codice personalizzato per i bottoni specifici
            // Bottone anno
            yearButton.setOnClickListener {
                yearButton.isSelected = true
                monthButton.isSelected = false
                tenDaysButton.isSelected = false

                // Applica colori personalizzati
                yearButton.setBackgroundColor(colorPrimary)
                yearButton.setTextColor(colorOnPrimary)

                monthButton.setBackgroundColor(colorSurfaceVariant)
                monthButton.setTextColor(colorOnSurfaceVariant)

                tenDaysButton.setBackgroundColor(colorSurfaceVariant)
                tenDaysButton.setTextColor(colorOnSurfaceVariant)

                setupYearlyChart()
                updateButtonVisibility()
            }

            // Bottone mese
            monthButton.setOnClickListener {
                yearButton.isSelected = false
                monthButton.isSelected = true
                tenDaysButton.isSelected = false

                // Applica colori personalizzati
                yearButton.setBackgroundColor(colorSurfaceVariant)
                yearButton.setTextColor(colorOnSurfaceVariant)

                monthButton.setBackgroundColor(colorPrimary)
                monthButton.setTextColor(colorOnPrimary)

                tenDaysButton.setBackgroundColor(colorSurfaceVariant)
                tenDaysButton.setTextColor(colorOnSurfaceVariant)

                setupMonthlyChart()
                updateButtonVisibility()
            }

            // Bottone 10 giorni
            tenDaysButton.setOnClickListener {
                yearButton.isSelected = false
                monthButton.isSelected = false
                tenDaysButton.isSelected = true

                // Applica colori personalizzati
                yearButton.setBackgroundColor(colorSurfaceVariant)
                yearButton.setTextColor(colorOnSurfaceVariant)

                monthButton.setBackgroundColor(colorSurfaceVariant)
                monthButton.setTextColor(colorOnSurfaceVariant)

                tenDaysButton.setBackgroundColor(colorPrimary)
                tenDaysButton.setTextColor(colorOnPrimary)

                setupActivityChart()
                updateButtonVisibility()
            }

            // Imposta stato iniziale
            if (tenDaysButton.isSelected) {
                tenDaysButton.setBackgroundColor(colorPrimary)
                tenDaysButton.setTextColor(colorOnPrimary)
                yearButton.setBackgroundColor(colorSurfaceVariant)
                yearButton.setTextColor(colorOnSurfaceVariant)
                monthButton.setBackgroundColor(colorSurfaceVariant)
                monthButton.setTextColor(colorOnSurfaceVariant)
            } else if (monthButton.isSelected) {
                monthButton.setBackgroundColor(colorPrimary)
                monthButton.setTextColor(colorOnPrimary)
                yearButton.setBackgroundColor(colorSurfaceVariant)
                yearButton.setTextColor(colorOnSurfaceVariant)
                tenDaysButton.setBackgroundColor(colorSurfaceVariant)
                tenDaysButton.setTextColor(colorOnSurfaceVariant)
            } else if (yearButton.isSelected) {
                yearButton.setBackgroundColor(colorPrimary)
                yearButton.setTextColor(colorOnPrimary)
                monthButton.setBackgroundColor(colorSurfaceVariant)
                monthButton.setTextColor(colorOnSurfaceVariant)
                tenDaysButton.setBackgroundColor(colorSurfaceVariant)
                tenDaysButton.setTextColor(colorOnSurfaceVariant)
            }
        }
    }

    private fun updateDateBar(labels: List<String>) {
        val dateBarLayout = findViewById<LinearLayout>(R.id.dateBarLayout)
        dateBarLayout.removeAllViews()

        val colorMode = prefs.getInt("app_color", 0)
        val textColor = if (colorMode == 1) {
            getColorFromAttr(com.google.android.material.R.attr.colorPrimary)
        } else {
            ContextCompat.getColor(this, R.color.material_blue)
        }

        for (label in labels) {
            val dateView = TextView(this)
            val spannable = android.text.SpannableString(label)

            val dalIndex = label.indexOf("dal")
            if (dalIndex != -1) {
                spannable.setSpan(
                    android.text.style.RelativeSizeSpan(0.8f),
                    dalIndex,
                    dalIndex + 3,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            val alIndex = label.indexOf(" al ")
            if (alIndex != -1) {
                spannable.setSpan(
                    android.text.style.RelativeSizeSpan(0.8f),
                    alIndex + 1,
                    alIndex + 3,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            dateView.apply {
                text = spannable
                textSize = 12f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setTextColor(textColor)
            }
            dateBarLayout.addView(dateView)
        }
    }

    private fun setupTimeSegments() {
        timeSegmentGroup = findViewById(R.id.timeSegmentCard)

        yearButton = findViewById<SegmentedButton>(R.id.btnYear).apply {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            text = currentYear.toString()
            setPosition(SegmentedButton.POSITION_START)
            isSelected = false
            setCustomCornerRadii(topLeft = 8f, topRight = 8f, bottomRight = 8f)
            setOnClickListener {
                setupYearlyChart()
                updateButtonVisibility()
                isSelected = true
                monthButton.isSelected = false
                tenDaysButton.isSelected = false
            }
        }

        monthButton = findViewById<SegmentedButton>(R.id.btnMonth).apply {
            val currentMonthName = SimpleDateFormat("MMMM", Locale.ITALIAN)
                .format(Calendar.getInstance().time)
                .replaceFirstChar { it.uppercaseChar() }
            text = currentMonthName
            setPosition(SegmentedButton.POSITION_CENTER)
            isSelected = false
            setOnClickListener {
                setupMonthlyChart()
                updateButtonVisibility()
                isSelected = true
                yearButton.isSelected = false
                tenDaysButton.isSelected = false
            }
        }

        tenDaysButton = findViewById<SegmentedButton>(R.id.btn10Days).apply {
            text = "10 Giorni"
            setPosition(SegmentedButton.POSITION_END)
            setCustomCornerRadii(topLeft = 8f, topRight = 8f, bottomLeft = 8f)
            isSelected = true
            setOnClickListener {
                setupActivityChart()
                updateButtonVisibility()
                isSelected = true
                yearButton.isSelected = false
                monthButton.isSelected = false
            }
        }

        if (prefs.getInt("app_color", 0) == 1) {
            updateSegmentButtonsForDynamicTheme()
        }

    }

    private fun isLightTheme(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_YES
    }

    private fun Int.dpToPx(): Int =
        (this * this@StoricoAttivitaActivity.resources.displayMetrics.density).toInt()

    private fun updateButtonVisibility() {
        when {
            tenDaysButton.isSelected -> {
                btnPrevious.visibility = View.VISIBLE
                btnNext.visibility = if (currentDayOffset == 0) View.GONE else View.VISIBLE
            }
            monthButton.isSelected -> {
                btnPrevious.visibility = View.VISIBLE
                btnNext.visibility = if (currentMonthOffset == 0) View.GONE else View.VISIBLE
            }
            yearButton.isSelected -> {
                btnPrevious.visibility = View.VISIBLE
                btnNext.visibility = if (currentYearOffset == 0) View.GONE else View.VISIBLE
            }
        }
    }

    private fun getColorFromAttr(attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }


}

