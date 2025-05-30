package com.example.mover.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mover.databinding.ActivityBadgeBinding
import com.example.mover.ui.adapters.BadgeAdapter
import com.example.mover.ui.viewmodels.BadgeViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class BadgeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBadgeBinding
    private val viewModel: BadgeViewModel by viewModels()
    private lateinit var badgeAdapter: BadgeAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBadgeBinding.inflate(layoutInflater)
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
    }
    
    private fun setupRecyclerView() {
        badgeAdapter = BadgeAdapter { badge ->
            // Mostra dettagli badge
            showBadgeDetails(badge)
        }
        
        binding.recyclerViewBadges.apply {
            layoutManager = GridLayoutManager(this@BadgeActivity, 2)
            adapter = badgeAdapter
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.badgesUtente.collect { badges ->
                badgeAdapter.submitList(badges)
                binding.progressBar.visibility = View.GONE
                
                if (badges.isEmpty()) {
                    binding.textViewNoBadges.visibility = View.VISIBLE
                    binding.recyclerViewBadges.visibility = View.GONE
                } else {
                    binding.textViewNoBadges.visibility = View.GONE
                    binding.recyclerViewBadges.visibility = View.VISIBLE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.statistiche.collect { stats ->
                stats?.let {
                    binding.textViewTotaleBadges.text = "Badge ottenuti: ${it.totaleBadges}"
                    binding.textViewPuntiTotali.text = "Punti totali: ${it.puntiTotali}"
                    
                    it.ultimoBadge?.let { ultimo ->
                        binding.textViewUltimoBadge.text = "Ultimo: ${ultimo.nome}"
                        binding.textViewUltimoBadge.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Tutti"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Distanza"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Velocità"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Sport"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Sociale"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Sfide"))
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.caricaTuttiBadges()
                    1 -> viewModel.caricaBadgesPerCategoria(com.example.mover.database.entities.CategoriaBadge.DISTANZA)
                    2 -> viewModel.caricaBadgesPerCategoria(com.example.mover.database.entities.CategoriaBadge.VELOCITA)
                    3 -> viewModel.caricaBadgesPerCategoria(com.example.mover.database.entities.CategoriaBadge.SPORT_SPECIFICO)
                    4 -> viewModel.caricaBadgesPerCategoria(com.example.mover.database.entities.CategoriaBadge.SOCIALE)
                    5 -> viewModel.caricaBadgesPerCategoria(com.example.mover.database.entities.CategoriaBadge.SFIDE)
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun loadStatistiche() {
        viewModel.caricaStatistiche()
    }
    
    private fun showBadgeDetails(badge: com.example.mover.database.dao.BadgeConDettagli) {
        // Implementa dialog o activity per mostrare dettagli badge
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(badge.nome)
            .setMessage(badge.descrizione)
            .setPositiveButton("OK", null)
            .show()
    }
}