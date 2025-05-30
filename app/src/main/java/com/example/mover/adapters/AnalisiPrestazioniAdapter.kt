package com.example.mover.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.R
import com.example.mover.data.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter per visualizzare le analisi delle prestazioni
 */
class AnalisiPrestazioniAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private var currentDataType = DataType.FITNESS_FRESHNESS
    private var fitnessFreshnessData = listOf<FitnessFreshness>()
    private var zoneAnalysisData = listOf<AnalisiZone>()
    private var paceAnalysisData = listOf<AnalisiPassoGara>()
    private var performanceMetricsData = listOf<MetrichePerformance>()
    private var segmentsData = listOf<Segmento>()
    
    enum class DataType {
        FITNESS_FRESHNESS,
        ZONE_ANALYSIS,
        PACE_ANALYSIS,
        PERFORMANCE_METRICS,
        SEGMENTS
    }
    
    companion object {
        private const val TYPE_FITNESS_FRESHNESS = 0
        private const val TYPE_ZONE_ANALYSIS = 1
        private const val TYPE_PACE_ANALYSIS = 2
        private const val TYPE_PERFORMANCE_METRICS = 3
        private const val TYPE_SEGMENTS = 4
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (currentDataType) {
            DataType.FITNESS_FRESHNESS -> TYPE_FITNESS_FRESHNESS
            DataType.ZONE_ANALYSIS -> TYPE_ZONE_ANALYSIS
            DataType.PACE_ANALYSIS -> TYPE_PACE_ANALYSIS
            DataType.PERFORMANCE_METRICS -> TYPE_PERFORMANCE_METRICS
            DataType.SEGMENTS -> TYPE_SEGMENTS
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        
        return when (viewType) {
            TYPE_FITNESS_FRESHNESS -> {
                val view = inflater.inflate(R.layout.item_fitness_freshness, parent, false)
                FitnessFreshnessViewHolder(view)
            }
            TYPE_ZONE_ANALYSIS -> {
                val view = inflater.inflate(R.layout.item_zone_analysis, parent, false)
                ZoneAnalysisViewHolder(view)
            }
            TYPE_PACE_ANALYSIS -> {
                val view = inflater.inflate(R.layout.item_pace_analysis, parent, false)
                PaceAnalysisViewHolder(view)
            }
            TYPE_PERFORMANCE_METRICS -> {
                val view = inflater.inflate(R.layout.item_performance_metrics, parent, false)
                PerformanceMetricsViewHolder(view)
            }
            TYPE_SEGMENTS -> {
                val view = inflater.inflate(R.layout.item_segment, parent, false)
                SegmentViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FitnessFreshnessViewHolder -> {
                if (position < fitnessFreshnessData.size) {
                    holder.bind(fitnessFreshnessData[position])
                }
            }
            is ZoneAnalysisViewHolder -> {
                if (position < zoneAnalysisData.size) {
                    holder.bind(zoneAnalysisData[position])
                }
            }
            is PaceAnalysisViewHolder -> {
                if (position < paceAnalysisData.size) {
                    holder.bind(paceAnalysisData[position])
                }
            }
            is PerformanceMetricsViewHolder -> {
                if (position < performanceMetricsData.size) {
                    holder.bind(performanceMetricsData[position])
                }
            }
            is SegmentViewHolder -> {
                if (position < segmentsData.size) {
                    holder.bind(segmentsData[position])
                }
            }
        }
    }
    
    override fun getItemCount(): Int {
        return when (currentDataType) {
            DataType.FITNESS_FRESHNESS -> fitnessFreshnessData.size
            DataType.ZONE_ANALYSIS -> zoneAnalysisData.size
            DataType.PACE_ANALYSIS -> paceAnalysisData.size
            DataType.PERFORMANCE_METRICS -> performanceMetricsData.size
            DataType.SEGMENTS -> segmentsData.size
        }
    }
    
    // Metodi per aggiornare i dati
    fun updateFitnessFreshnessData(data: List<FitnessFreshness>) {
        currentDataType = DataType.FITNESS_FRESHNESS
        fitnessFreshnessData = data
        notifyDataSetChanged()
    }
    
    fun updateZoneAnalysisData(data: List<AnalisiZone>) {
        currentDataType = DataType.ZONE_ANALYSIS
        zoneAnalysisData = data
        notifyDataSetChanged()
    }
    
    fun updatePaceAnalysisData(data: List<AnalisiPassoGara>) {
        currentDataType = DataType.PACE_ANALYSIS
        paceAnalysisData = data
        notifyDataSetChanged()
    }
    
    fun updatePerformanceMetricsData(data: List<MetrichePerformance>) {
        currentDataType = DataType.PERFORMANCE_METRICS
        performanceMetricsData = data
        notifyDataSetChanged()
    }
    
    fun updateSegmentsData(data: List<Segmento>) {
        currentDataType = DataType.SEGMENTS
        segmentsData = data
        notifyDataSetChanged()
    }
    
    // ViewHolders semplificati per evitare errori di layout
    inner class FitnessFreshnessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(fitness: FitnessFreshness) {
            // Layout semplificato - useremo un TextView generico
            val textView = itemView as? TextView ?: return
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            textView.text = """
                ${dateFormat.format(Date(fitness.data))}
                Fitness: ${String.format("%.1f", fitness.fitness)}
                Fatica: ${String.format("%.1f", fitness.fatigue)}
                Forma: ${String.format("%.1f", fitness.form)}
                ${fitness.raccomandazione}
                TSS: ${String.format("%.0f", fitness.tssGiornaliero)}
            """.trimIndent()
            
            itemView.setOnClickListener {
                onItemClick("fitness_freshness")
            }
        }
    }
    
    inner class ZoneAnalysisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(zone: AnalisiZone) {
            val textView = itemView as? TextView ?: return
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            textView.text = """
                ${dateFormat.format(Date(zone.data))}
                Intensità: ${String.format("%.1f", zone.intensitaMedia)}
                Efficienza: ${String.format("%.1f", zone.efficienza)}
                Qualità: ${zone.qualitaAllenamento}
                Z1: ${zone.zonaCardiaca1 / 60000}min
                Z2: ${zone.zonaCardiaca2 / 60000}min
                Z3: ${zone.zonaCardiaca3 / 60000}min
            """.trimIndent()
            
            itemView.setOnClickListener {
                onItemClick("zone_analysis")
            }
        }
    }
    
    inner class PaceAnalysisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(pace: AnalisiPassoGara) {
            val textView = itemView as? TextView ?: return
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            textView.text = """
                ${dateFormat.format(Date(pace.data))}
                ${pace.tipoGara} (${String.format("%.1f", pace.distanzaGara)}km)
                Pace: ${String.format("%.2f", pace.paceMedio)} min/km
                Consistenza: ${String.format("%.1f", pace.consistenza * 100)}%
                Strategia: ${pace.strategia}
            """.trimIndent()
            
            itemView.setOnClickListener {
                onItemClick("pace_analysis")
            }
        }
    }
    
    inner class PerformanceMetricsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(metrics: MetrichePerformance) {
            val textView = itemView as? TextView ?: return
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            textView.text = """
                ${dateFormat.format(Date(metrics.data))}
                ${metrics.tipoAttivita}
                Performance: ${String.format("%.0f", metrics.performanceScore ?: 0f)}/100
                Efficienza: ${String.format("%.0f", metrics.efficiencyScore ?: 0f)}/100
                VO2: ${String.format("%.1f", metrics.vo2Stimato ?: 0f)} ml/kg/min
            """.trimIndent()
            
            itemView.setOnClickListener {
                onItemClick("performance_metrics")
            }
        }
    }
    
    inner class SegmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(segment: Segmento) {
            val textView = itemView as? TextView ?: return
            
            textView.text = """
                ${segment.nome}
                ${segment.tipo} - ${String.format("%.2f", segment.distanza / 1000f)} km
                ${segment.numeroTentativi} tentativi
                ★ ${String.format("%.1f", segment.stelle)}
            """.trimIndent()
            
            itemView.setOnClickListener {
                onItemClick("segments")
            }
        }
    }
}