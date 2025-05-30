package com.example.mover.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.R
import com.example.mover.adapters.ClubAdapter
import com.example.mover.database.AppDatabase
import com.example.mover.services.SocialService
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class ClubActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClubAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var fabNuovoClub: FloatingActionButton
    
    private lateinit var socialService: SocialService
    private val currentUserId = 1L // TODO: Ottenere da SharedPreferences/Session
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_club)
        
        initializeViews()
        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupFab()
        
        val database = AppDatabase.getDatabase(this)
        socialService = SocialService(database)
        
        caricaClub()
    }
    
    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewClub)
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        fabNuovoClub = findViewById(R.id.fabNuovoClub)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Club e Gruppi"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("I Miei Club"))
        tabLayout.addTab(tabLayout.newTab().setText("Esplora"))
        tabLayout.addTab(tabLayout.newTab().setText("Locali"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> caricaMieiClub()
                    1 -> caricaClubPublici()
                    2 -> caricaClubLocali()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupRecyclerView() {
        adapter = ClubAdapter(
            onClubClick = { clubId ->
                val intent = Intent(this, DettagliClubActivity::class.java)
                intent.putExtra("CLUB_ID", clubId)
                startActivity(intent)
            },
            onIscrivitiClick = { clubId ->
                lifecycleScope.launch {
                    socialService.iscrivitiAlClub(clubId, currentUserId)
                    caricaClub()
                }
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupFab() {
        fabNuovoClub.setOnClickListener {
            val intent = Intent(this, CreaClubActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun caricaClub() {
        when (tabLayout.selectedTabPosition) {
            0 -> caricaMieiClub()
            1 -> caricaClubPublici()
            2 -> caricaClubLocali()
        }
    }
    
    private fun caricaMieiClub() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@ClubActivity)
                database.clubDao().getClubUtente(currentUserId).collect { club ->
                    adapter.submitList(club)
                }
            } catch (e: Exception) {
                // Gestisci errore
            }
        }
    }
    
    private fun caricaClubPublici() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@ClubActivity)
                val club = database.clubDao().searchClub("", 50)
                adapter.submitList(club)
            } catch (e: Exception) {
                // Gestisci errore
            }
        }
    }
    
    private fun caricaClubLocali() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@ClubActivity)
                // TODO: Ottenere città dell'utente
                val club = database.clubDao().getClubPerCitta("Milano", 50)
                adapter.submitList(club)
            } catch (e: Exception) {
                // Gestisci errore
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_club, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_search -> {
                val intent = Intent(this, RicercaClubActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_classifiche -> {
                val intent = Intent(this, ClassificheClubActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onResume() {
        super.onResume()
        caricaClub()
    }
}