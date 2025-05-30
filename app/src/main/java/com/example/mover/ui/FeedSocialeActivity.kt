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
import com.example.mover.adapters.FeedSocialeAdapter
import com.example.mover.database.AppDatabase
import com.example.mover.services.SocialService
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class FeedSocialeActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedSocialeAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var fabNuovaAttivita: FloatingActionButton
    
    private lateinit var socialService: SocialService
    private val currentUserId = 1L // TODO: Ottenere da SharedPreferences/Session
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_sociale)
        
        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupFab()
        
        val database = AppDatabase.getDatabase(this)
        socialService = SocialService(database)
        
        caricaFeed()
    }
    
    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewFeed)
        toolbar = findViewById(R.id.toolbar)
        fabNuovaAttivita = findViewById(R.id.fabNuovaAttivita)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Feed"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = FeedSocialeAdapter(
            onKudosClick = { attivitaId -> 
                lifecycleScope.launch {
                    socialService.daKudos(currentUserId, attivitaId)
                    caricaFeed() // Ricarica per aggiornare
                }
            },
            onCommentoClick = { attivitaId -> 
                // Apri dialog commenti
                mostraDialogCommenti(attivitaId)
            },
            onProfiloClick = { utenteId ->
                // Apri profilo utente
                val intent = Intent(this, ProfiloUtenteActivity::class.java)
                intent.putExtra("utenteId", utenteId)
                startActivity(intent)
            },
            onAttivitaClick = { attivitaId ->
                // Apri dettagli attività
                val intent = Intent(this, DettagliAttivitaActivity::class.java)
                intent.putExtra("attivitaId", attivitaId)
                startActivity(intent)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Scroll infinito
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 5) {
                    caricaAltroFeed()
                }
            }
        })
    }
    
    private fun setupFab() {
        fabNuovaAttivita.setOnClickListener {
            val intent = Intent(this, ActivitySelectionActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun caricaFeed() {
        lifecycleScope.launch {
            try {
                val feedItems = socialService.generaFeedUtente(currentUserId, 20, 0)
                adapter.submitList(feedItems)
            } catch (e: Exception) {
                // Gestisci errore
            }
        }
    }
    
    private fun caricaAltroFeed() {
        lifecycleScope.launch {
            try {
                val currentSize = adapter.itemCount
                val feedItems = socialService.generaFeedUtente(currentUserId, 20, currentSize)
                adapter.addItems(feedItems)
            } catch (e: Exception) {
                // Gestisci errore
            }
        }
    }
    
    private fun mostraDialogCommenti(attivitaId: Long) {
        val dialog = CommentiDialogFragment.newInstance(attivitaId)
        dialog.show(supportFragmentManager, "CommentiDialog")
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_feed_sociale, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_search -> {
                val intent = Intent(this, RicercaUtentiActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_notifiche -> {
                val intent = Intent(this, NotificheActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_profilo -> {
                val intent = Intent(this, ProfiloUtenteActivity::class.java)
                intent.putExtra("utenteId", currentUserId)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onResume() {
        super.onResume()
        caricaFeed() // Ricarica quando si torna all'activity
    }
}