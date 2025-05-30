package com.example.mover.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mover.databinding.ActivityObiettiviBinding
import com.example.mover.ui.adapters.ObiettiviAdapter
import com.example.mover.ui.viewmodels.ObiettiviViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class ObiettiviActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityObiettiviBinding
    private val viewModel: ObiettiviViewModel by viewModels()
    private lateinit var obiettiviAdapter: ObiettiviAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObiettiviBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupRecyclerView()
        setupObservers()
        setupTabs()
        loadStatistiche()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        binding.fabNuovoObiettivo.setOnClickListener {
            startActivity(Intent(this, CreaObiettivoActivity::class.java))
        }
        
        binding.btnObiettiviCompletati.setOnClickListener {
            startActivity(Intent(this, ObiettiviCompletatiActivity::class.java))
        }
    }
    
    private fun setupRecyclerView() {
        obiettiviAdapter = ObiettiviAdapter(
            onObiettivoClick = { obiettivo ->
                val intent = Intent(this, DettagliObiettivoActivity::class.java)
                intent.putExtra("OBIETTIVO_ID", obiettivo.id)
                startActivity(intent)
            },
            onEditClick = { obiettivo ->
                val intent = Intent(this, CreaObiettivoActivity::class.java)
                intent.putExtra("OBIETTIVO_ID", obiettivo.id)
                startActivity(intent)
            }
        )
        
        binding.recyclerViewObiettivi.apply {
            layoutManager = LinearLayoutManager(this@ObiettiviActivity)
            adapter = obiettiviAdapter
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.obiettiviConProgressi.collect { obiettivi ->
                obiettiviAdapter.submitList(obiettivi)
                binding.progressBar.visibility = View.GONE
                
                if (obiettivi.isEmpty()) {
                    binding.textViewNoObiettivi.visibility = View.VISIBLE
                    binding.recyclerViewObiettivi.visibility = View.GONE
                } else {
                    binding.textViewNoObiettivi.visibility = View.GONE
                    binding.recyclerViewObiettivi.visibility = View.VISIBLE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.statistiche.collect { stats ->
                stats?.let {
                    binding.textViewObiettiviAttivi.text = "Attivi: ${it.obiettiviAttivi}"
                    binding.textViewObiettiviCompletati.text = "Completati: ${it.obiettiviCompletati}"
                    binding.textViewPercentualeMedia.text = "Media: ${String.format("%.1f", it.percentualeMediaCompletamento)}%"
                }
            }
        }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Attivi"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Settimanali"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Mensili"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Annuali"))
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.caricaObiettiviAttivi()
                    1 -> viewModel.caricaObiettiviPerPeriodo(com.example.mover.database.entities.PeriodoObiettivo.SETTIMANALE)
                    2 -> viewModel.caricaObiettiviPerPeriodo(com.example.mover.database.entities.PeriodoObiettivo.MENSILE)
                    3 -> viewModel.caricaObiettiviPerPeriodo(com.example.mover.database.entities.PeriodoObiettivo.ANNUALE)
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun loadStatistiche() {
        viewModel.caricaStatistiche()
    }
}