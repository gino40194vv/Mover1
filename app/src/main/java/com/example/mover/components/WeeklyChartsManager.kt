package com.example.mover.components

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.mover.R
import com.example.mover.RoundedBarChart
import com.example.mover.data.AppDatabase
import com.example.mover.data.Attività
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Calendar

class WeeklyChartsManager(private val context: Context, private val db: AppDatabase) {
    private val weekDays = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")

    suspend fun setupCharts(walkingChart: RoundedBarChart, runningChart: RoundedBarChart, sittingChart: RoundedBarChart) {
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val activities = db.attivitàDao().getAttivitàByDateRange(startOfWeek, System.currentTimeMillis())

        setupWalkingChart(walkingChart, activities)
        setupRunningChart(runningChart, activities)
        setupSittingChart(sittingChart, activities)
    }

    fun animateCharts(walkingChart: RoundedBarChart, runningChart: RoundedBarChart, sittingChart: RoundedBarChart) {
        walkingChart.animateY(1000)
        runningChart.animateY(1200)
        sittingChart.animateY(1400)
    }

    private fun setupWalkingChart(chart: RoundedBarChart, activities: List<Attività>) {
        val walkingActivities = activities.filter { it.tipo == "Camminare" }
        val entries = mutableListOf<BarEntry>()
        val averageEntries = mutableListOf<BarEntry>()

        // Calcola la distanza per ogni giorno della settimana corrente
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val adjustedDayOfWeek = (currentDayOfWeek + 5) % 7  // Converti a indice 0-6

        val dailySteps = (0..adjustedDayOfWeek).map { dayIndex ->
            walkingActivities
                .filter { getDayOfWeek(it.oraInizio) == dayIndex }
                .sumOf { it.passi ?: 0 }
                .toFloat()
        }

        // Calcola la media giornaliera considerando solo i giorni passati
        val averageSteps = if (dailySteps.isNotEmpty()) dailySteps.sum() / dailySteps.size else 0f

        // Crea le entries per il grafico
        (0..adjustedDayOfWeek).forEachIndexed { index, _ ->
            entries.add(BarEntry(index.toFloat(), dailySteps[index]))
            averageEntries.add(BarEntry(index.toFloat(), averageSteps))
        }

        // Se tutti i valori sono 0, mostra "nessun dato"
        if (entries.all { it.y == 0f }) {
            chart.clear()
            chart.setNoDataText("ancora nessun dato")
            chart.setNoDataTextColor(ContextCompat.getColor(context, R.color.myNoDataColor))
            chart.invalidate()
            return
        }

        val currentDataSet = BarDataSet(entries, "Passi").apply {
            color = ContextCompat.getColor(context, R.color.colorCamminare)
            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = ContextCompat.getColor(context, R.color.colorCamminare)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value == 0f) "" else String.format("%.0f", value)
                }
            }
        }

        val averageDataSet = BarDataSet(averageEntries, "Media").apply {
            color = ContextCompat.getColor(context, R.color.colorCamminare_transparent)
            setDrawValues(false)
        }

        chart.data = BarData(averageDataSet, currentDataSet)
        chart.setDrawValueAboveBar(true)
        chart.setRadius(20)
        styleChart(chart, "Passi Settimanali", "Camminata", ContextCompat.getColor(context, R.color.colorCamminare))
        chart.animateY(1000)
        chart.invalidate()
    }

    private fun setupRunningChart(chart: RoundedBarChart, activities: List<Attività>) {
        val runningActivities = activities.filter { it.tipo == "Corsa" }
        val entries = mutableListOf<BarEntry>()
        val averageEntries = mutableListOf<BarEntry>()

        // Calcola la distanza per ogni giorno della settimana corrente
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val adjustedDayOfWeek = (currentDayOfWeek + 5) % 7  // Converti a indice 0-6

        val dailyDistances = (0..adjustedDayOfWeek).map { dayIndex ->
            runningActivities
                .filter { getDayOfWeek(it.oraInizio) == dayIndex }
                .sumOf { (it.distanza ?: 0f).toDouble() }
                .toFloat()
        }

        // Calcola la media giornaliera considerando solo i giorni passati
        val averageDistance = if (dailyDistances.isNotEmpty()) dailyDistances.sum() / dailyDistances.size else 0f

        (0..adjustedDayOfWeek).forEachIndexed { index, _ ->
            entries.add(BarEntry(index.toFloat(), dailyDistances[index]))
            averageEntries.add(BarEntry(index.toFloat(), averageDistance))
        }

        if (entries.all { it.y == 0f }) {
            chart.clear()
            chart.setNoDataText("ancora nessun dato")
            chart.setNoDataTextColor(ContextCompat.getColor(context, R.color.myNoDataColor))
            chart.invalidate()
            return
        }

        val dataSet = BarDataSet(entries, "Distanza").apply {
            color = ContextCompat.getColor(context, R.color.colorCorrere)
            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = ContextCompat.getColor(context, R.color.colorCorrere)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when {
                        value == 0f -> ""
                        value < 1000 -> String.format("%.0f m", value)
                        else -> String.format("%.1f km", value / 1000)
                    }
                }
            }
        }

        val averageDataSet = BarDataSet(averageEntries, "Media").apply {
            color = ContextCompat.getColor(context, R.color.colorCorrere_transparent)
            setDrawValues(false)
        }

        chart.data = BarData(averageDataSet, dataSet)
        chart.setDrawValueAboveBar(true)
        chart.setRadius(20)
        styleChart(chart, "Km Corsa Settimanali", "CORSA", ContextCompat.getColor(context, R.color.colorCorrere))
        chart.animateY(1200)
        chart.invalidate()
    }

    private fun setupSittingChart(chart: RoundedBarChart, activities: List<Attività>) {
        val sittingActivities = activities.filter { it.tipo == "Sedersi" }
        val entries = mutableListOf<BarEntry>()
        val averageEntries = mutableListOf<BarEntry>()

        // Calcola la distanza per ogni giorno della settimana corrente
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val adjustedDayOfWeek = (currentDayOfWeek + 5) % 7  // Converti a indice 0-6

        val dailyHours = (0..adjustedDayOfWeek).map { dayIndex ->
            sittingActivities
                .filter { getDayOfWeek(it.oraInizio) == dayIndex }
                .sumOf { (it.tempo ?: 0L).toDouble() }
                .toFloat() / 3600000  // Converti da millisecondi a ore
        }

        // Calcola la media giornaliera considerando solo i giorni passati
        val averageHours = if (dailyHours.isNotEmpty()) dailyHours.sum() / dailyHours.size else 0f

        (0..adjustedDayOfWeek).forEachIndexed { index, _ ->
            entries.add(BarEntry(index.toFloat(), dailyHours[index]))
            averageEntries.add(BarEntry(index.toFloat(), averageHours))
        }

        if (entries.all { it.y == 0f }) {
            chart.clear()
            chart.setNoDataText("ancora nessun dato")
            chart.setNoDataTextColor(ContextCompat.getColor(context, R.color.myNoDataColor))
            chart.invalidate()
            return
        }

        val dataSet = BarDataSet(entries, "Ore").apply {
            color = ContextCompat.getColor(context, R.color.colorSedersi)
            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = ContextCompat.getColor(context, R.color.colorSedersi)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when {
                        value == 0f -> ""
                        value < 1 -> String.format("%.0f min", value * 60)
                        else -> String.format("%.1f h", value)
                    }
                }
            }
        }

        val averageDataSet = BarDataSet(averageEntries, "Media").apply {
            color = ContextCompat.getColor(context, R.color.colorSedersi_transparent)
            setDrawValues(false)
        }

        chart.data = BarData(averageDataSet, dataSet)
        chart.setRadius(20)
        styleChart(chart, "Sedute Settimanali", "SEDUTA", ContextCompat.getColor(context, R.color.colorSedersi))
        chart.animateY(1400)
        chart.invalidate()
    }

    private fun styleChart(chart: RoundedBarChart, chartTitle: String, activityTitle: String, titleColor: Int) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)

            xAxis.isEnabled = false

            setViewPortOffsets(0f, 20f, 0f, 0f)  // Riduci i margini

            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            setHighlightPerTapEnabled(false)
            setHighlightPerDragEnabled(false)

            setExtraOffsets(10f, 10f, 10f, 10f)
            isScaleYEnabled = false

        }
    }

    private fun getDayOfWeek(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }
}