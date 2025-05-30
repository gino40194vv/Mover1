package com.example.mover

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class ActivityMarkerView(context: Context) : MarkerView(context, R.layout.marker_view) {

    private val tvContent: TextView

    init {
        // Trova il TextView nel layout
        tvContent = findViewById<TextView>(android.R.id.text1)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e is BarEntry) {
            val stackIndex = highlight?.stackIndex ?: return
            val value = e.yVals[stackIndex].toInt()

            if (value == 0) {
                // Se il valore è zero, rendi il marker trasparente
                tvContent.alpha = 0f
                return
            } else {
                val activityName = when (stackIndex) {
                    0 -> "Camminare"
                    1 -> "Corsa"
                    2 -> "Guidare"
                    3 -> "Sedersi"
                    else -> return
                }

                tvContent.text = "$activityName: $value"
                tvContent.alpha = 1f
            }
        } else {
            // Se non è una BarEntry o se l'Entry è null, rendi il marker trasparente
            tvContent.alpha = 0f
        }

        super.refreshContent(e, highlight)
    }
    override fun getOffset(): MPPointF {
        // Centra il marker orizzontalmente e posizionalo sopra il punto
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}