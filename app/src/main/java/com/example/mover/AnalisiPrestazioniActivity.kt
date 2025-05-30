package com.example.mover

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.adapters.AnalisiPrestazioniAdapter
import com.example.mover.data.AppDatabase
import com.example.mover.services.AnalisiPrestazioniService
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

/**
 * Activity per visualizzare le analisi delle prestazioni avanzate
 */
class AnalisiPrestazioniActivity : AppCompatActivity() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnalisiPrestazioniAdapter
    private lateinit var analisiService: AnalisiPrestazioniService
    private lateinit var database: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analisi_prestazioni)
        
        setupActionBar()
        initializeComponents()
        setupTabs()
        setupRecyclerView()
        loadAnalisiData()
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            title = "Analisi Prestazioni"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun initializeComponents() {
        tabLayout = findViewById(R.id.tabLayoutAnalisi)
        recyclerView = findViewById(R.id.recyclerViewAnalisi)
        
        database = AppDatabase.getDatabase(this)
        analisiService = AnalisiPrestazioniService(this)
        
        adapter = AnalisiPrestazioniAdapter { analisiType ->
            when (analisiType) {
                "fitness_freshness" -> showFitnessFreshnessDetail()
                "zone_analysis" -> showZoneAnalysisDetail()
                "pace_analysis" -> showPaceAnalysisDetail()
                "performance_metrics" -> showPerformanceMetricsDetail()
                "segments" -> showSegmentsDetail()
            }
        }
    }
    
    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Fitness & Freshness"))
        tabLayout.addTab(tabLayout.newTab().setText("Analisi Zone"))
        tabLayout.addTab(tabLayout.newTab().setText("Passo Gara"))
        tabLayout.addTab(tabLayout.newTab().setText("Metriche"))
        tabLayout.addTab(tabLayout.newTab().setText("Segmenti"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { loadDataForTab(it.position) }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun loadAnalisiData() {
        loadDataForTab(0) // Carica Fitness & Freshness di default
    }
    
    private fun loadDataForTab(position: Int) {
        lifecycleScope.launch {
            try {
                when (position) {
                    0 -> loadFitnessFreshnessData()
                    1 -> loadZoneAnalysisData()
                    2 -> loadPaceAnalysisData()
                    3 -> loadPerformanceMetricsData()
                    4 -> loadSegmentsData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Mostra errore all'utente
            }
        }
    }
    
    private suspend fun loadFitnessFreshnessData() {
        val atleta = "current_user" // TODO: Implementare gestione utenti
        val dataFine = System.currentTimeMillis()
        val dataInizio = dataFine - (30 * 24 * 60 * 60 * 1000L) // Ultimi 30 giorni
        
        val analisiFF = database.analisiPrestazioniDao().getAnalisiFFPerPeriodo(
            atleta, dataInizio, dataFine
        )
        
        // Se non ci sono dati, calcola l'analisi corrente
        if (analisiFF.isEmpty()) {
            val nuovaAnalisi = analisiService.calcolaFitnessFreshness(atleta)
            database.analisiPrestazioniDao().inserisciFitnessFreshness(nuovaAnalisi)
            adapter.updateFitnessFreshnessData(listOf(nuovaAnalisi))
        } else {
            adapter.updateFitnessFreshnessData(analisiFF)
        }
    }
    
    private suspend fun loadZoneAnalysisData() {
        val atleta = "current_user"
        val dataFine = System.currentTimeMillis()
        val dataInizio = dataFine - (7 * 24 * 60 * 60 * 1000L) // Ultima settimana
        
        val analisiZone = database.analisiPrestazioniDao().getAnalisiZonePerPeriodo(
            atleta, dataInizio, dataFine
        )
        
        adapter.updateZoneAnalysisData(analisiZone)
    }
    
    private suspend fun loadPaceAnalysisData() {
        val atleta = "current_user"
        
        val analisiPasso = database.analisiPrestazioniDao().getAnalisiPassoGaraPerDistanza(
            atleta, 10f // 10K come esempio
        )
        
        adapter.updatePaceAnalysisData(analisiPasso)
    }
    
    private suspend fun loadPerformanceMetricsData() {
        val atleta = "current_user"
        val dataFine = System.currentTimeMillis()
        val dataInizio = dataFine - (30 * 24 * 60 * 60 * 1000L) // Ultimo mese
        
        val metriche = database.analisiPrestazioniDao().getMetrichePerformancePerTipoEPeriodo(
            atleta, "Corsa", dataInizio, dataFine
        )
        
        adapter.updatePerformanceMetricsData(metriche)
    }
    
    private suspend fun loadSegmentsData() {
        val segmenti = database.segmentoDao().getSegmentiPubblici()
        adapter.updateSegmentsData(segmenti)
    }
    
    // Metodi per mostrare dettagli specifici
    private fun showFitnessFreshnessDetail() {
        // TODO: Implementare activity di dettaglio per Fitness & Freshness
        // Mostrerebbe grafici dettagliati, trend, previsioni
    }
    
    private fun showZoneAnalysisDetail() {
        // TODO: Implementare activity di dettaglio per analisi zone
        // Mostrerebbe distribuzione zone, grafici a torta, confronti
    }
    
    private fun showPaceAnalysisDetail() {
        // TODO: Implementare activity di dettaglio per analisi passo
        // Mostrerebbe splits dettagliati, confronti con gare precedenti
    }
    
    private fun showPerformanceMetricsDetail() {
        // TODO: Implementare activity di dettaglio per metriche performance
        // Mostrerebbe trend delle metriche, confronti, raccomandazioni
    }
    
    private fun showSegmentsDetail() {
        // TODO: Implementare activity di dettaglio per segmenti
        // Mostrerebbe mappa, classifiche, tentativi personali
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}