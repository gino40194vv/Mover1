package com.example.mover.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mover.databinding.ActivitySfideBinding
import com.example.mover.ui.adapters.SfideAdapter
import com.example.mover.ui.viewmodels.SfideViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class SfideActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySfideBinding
    private val viewModel: SfideViewModel by viewModels()
    private lateinit var sfideAdapter: SfideAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySfideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupRecyclerView()
        setupObservers()
        setupTabs()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        binding.fabNuovaSfida.setOnClickListener {
            // Apri activity per creare nuova sfida
            startActivity(Intent(this, CreaSfidaActivity::class.java))
        }
        
        binding.btnMieSfide.setOnClickListener {
            startActivity(Intent(this, MieSfideActivity::class.java))
        }
        
        binding.btnClassifiche.setOnClickListener {
            startActivity(Intent(this, ClassificheSfideActivity::class.java))
        }
    }
    
    private fun setupRecyclerView() {
        sfideAdapter = SfideAdapter { sfida ->
            // Apri dettagli sfida
            val intent = Intent(this, DettagliSfidaActivity::class.java)
            intent.putExtra("SFIDA_ID", sfida.id)
            startActivity(intent)
        }
        
        binding.recyclerViewSfide.apply {
            layoutManager = LinearLayoutManager(this@SfideActivity)
            adapter = sfideAdapter
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.sfideAttive.collect { sfide ->
                sfideAdapter.submitList(sfide)
                binding.progressBar.visibility = View.GONE
                
                if (sfide.isEmpty()) {
                    binding.textViewNoSfide.visibility = View.VISIBLE
                    binding.recyclerViewSfide.visibility = View.GONE
                } else {
                    binding.textViewNoSfide.visibility = View.GONE
                    binding.recyclerViewSfide.visibility = View.VISIBLE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Tutte"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Distanza"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Tempo"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Velocità"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Dislivello"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Frequenza"))
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.caricaSfideAttive()
                    1 -> viewModel.caricaSfidePerCategoria(com.example.mover.database.entities.CategoriaSfida.DISTANZA)
                    2 -> viewModel.caricaSfidePerCategoria(com.example.mover.database.entities.CategoriaSfida.TEMPO)
                    3 -> viewModel.caricaSfidePerCategoria(com.example.mover.database.entities.CategoriaSfida.VELOCITA)
                    4 -> viewModel.caricaSfidePerCategoria(com.example.mover.database.entities.CategoriaSfida.DISLIVELLO)
                    5 -> viewModel.caricaSfidePerCategoria(com.example.mover.database.entities.CategoriaSfida.FREQUENZA)
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}