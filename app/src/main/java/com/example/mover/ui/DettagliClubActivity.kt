package com.example.mover.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.R
import com.example.mover.database.AppDatabase
import com.example.mover.database.entities.social.*
import com.example.mover.services.SocialService
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import kotlinx.coroutines.launch
import java.util.*

class DettagliClubActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var socialService: SocialService
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvNomeClub: TextView
    private lateinit var tvDescrizioneClub: TextView
    private lateinit var tvNumeroMembri: TextView
    private lateinit var tvTipoClub: TextView
    private lateinit var btnAzioneClub: MaterialButton
    private lateinit var tvDistanzaTotale: TextView
    private lateinit var tvAttivitaTotali: TextView
    private lateinit var tvTempoTotale: TextView
    private lateinit var rvMembri: RecyclerView
    private lateinit var rvClassifica: RecyclerView
    
    private lateinit var membriAdapter: MembriClubAdapter
    private lateinit var classificaAdapter: ClassificaClubAdapter
    
    private var clubId: Long = -1
    private var currentClub: Club? = null
    private var isUserMember = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dettagli_club)

        // Inizializza database e service
        database = AppDatabase.getDatabase(this)
        socialService = SocialService(database)

        // Ottieni l'ID del club dall'intent
        clubId = intent.getLongExtra("CLUB_ID", -1)
        if (clubId == -1L) {
            Log.e("DettagliClubActivity", "ID club non valido")
            finish()
            return
        }

        initializeViews()
        setupRecyclerViews()
        setupToolbar()
        loadClubData()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        tvNomeClub = findViewById(R.id.tvNomeClub)
        tvDescrizioneClub = findViewById(R.id.tvDescrizioneClub)
        tvNumeroMembri = findViewById(R.id.tvNumeroMembri)
        tvTipoClub = findViewById(R.id.tvTipoClub)
        btnAzioneClub = findViewById(R.id.btnAzioneClub)
        tvDistanzaTotale = findViewById(R.id.tvDistanzaTotale)
        tvAttivitaTotali = findViewById(R.id.tvAttivitaTotali)
        tvTempoTotale = findViewById(R.id.tvTempoTotale)
        rvMembri = findViewById(R.id.rvMembri)
        rvClassifica = findViewById(R.id.rvClassifica)
    }

    private fun setupRecyclerViews() {
        // Setup RecyclerView membri
        membriAdapter = MembriClubAdapter { membro ->
            // Gestisci click su membro
            Log.d("DettagliClubActivity", "Click su membro: ${membro.utente.nome}")
        }
        rvMembri.layoutManager = LinearLayoutManager(this)
        rvMembri.adapter = membriAdapter

        // Setup RecyclerView classifica
        classificaAdapter = ClassificaClubAdapter { classifica ->
            // Gestisci click su posizione classifica
            Log.d("DettagliClubActivity", "Click su classifica: ${classifica.utente.nome}")
        }
        rvClassifica.layoutManager = LinearLayoutManager(this)
        rvClassifica.adapter = classificaAdapter
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadClubData() {
        lifecycleScope.launch {
            try {
                // Carica dati del club
                currentClub = socialService.getClubById(clubId)
                currentClub?.let { club ->
                    updateClubInfo(club)
                    
                    // Verifica se l'utente è membro
                    checkUserMembership(club.id)
                    
                    // Carica membri
                    loadMembri(club.id)
                    
                    // Carica classifica
                    loadClassifica(club.id)
                    
                    // Carica statistiche
                    loadStatistiche(club.id)
                }
            } catch (e: Exception) {
                Log.e("DettagliClubActivity", "Errore nel caricamento dati club", e)
            }
        }
    }

    private fun updateClubInfo(club: Club) {
        tvNomeClub.text = club.nome
        tvDescrizioneClub.text = club.descrizione
        tvNumeroMembri.text = club.numeroMembri.toString()
        tvTipoClub.text = if (club.pubblico) "Pubblico" else "Privato"
        
        toolbar.title = club.nome
    }

    private suspend fun checkUserMembership(clubId: Long) {
        // Simula controllo membership (sostituire con logica reale)
        val currentUserId = 1L // Sostituire con ID utente corrente
        isUserMember = socialService.isUserMemberOfClub(currentUserId, clubId)
        
        updateActionButton()
    }

    private fun updateActionButton() {
        if (isUserMember) {
            btnAzioneClub.text = "Lascia Club"
            btnAzioneClub.setOnClickListener {
                leaveClub()
            }
        } else {
            btnAzioneClub.text = "Unisciti al Club"
            btnAzioneClub.setOnClickListener {
                joinClub()
            }
        }
    }

    private fun joinClub() {
        lifecycleScope.launch {
            try {
                val currentUserId = 1L // Sostituire con ID utente corrente
                socialService.joinClub(currentUserId, clubId)
                isUserMember = true
                updateActionButton()
                loadClubData() // Ricarica i dati per aggiornare il numero di membri
            } catch (e: Exception) {
                Log.e("DettagliClubActivity", "Errore nell'unirsi al club", e)
            }
        }
    }

    private fun leaveClub() {
        lifecycleScope.launch {
            try {
                val currentUserId = 1L // Sostituire con ID utente corrente
                socialService.leaveClub(currentUserId, clubId)
                isUserMember = false
                updateActionButton()
                loadClubData() // Ricarica i dati per aggiornare il numero di membri
            } catch (e: Exception) {
                Log.e("DettagliClubActivity", "Errore nel lasciare il club", e)
            }
        }
    }

    private suspend fun loadMembri(clubId: Long) {
        try {
            val membri = socialService.getClubMembers(clubId)
            membriAdapter.updateMembri(membri)
        } catch (e: Exception) {
            Log.e("DettagliClubActivity", "Errore nel caricamento membri", e)
        }
    }

    private suspend fun loadClassifica(clubId: Long) {
        try {
            val classifica = socialService.getClubLeaderboard(clubId, "distanza")
            classificaAdapter.updateClassifica(classifica)
        } catch (e: Exception) {
            Log.e("DettagliClubActivity", "Errore nel caricamento classifica", e)
        }
    }

    private suspend fun loadStatistiche(clubId: Long) {
        try {
            val statistiche = socialService.getClubStatistics(clubId)
            
            // Aggiorna le statistiche nella UI
            tvDistanzaTotale.text = String.format("%.1f km", statistiche.distanzaTotale / 1000)
            tvAttivitaTotali.text = statistiche.attivitaTotali.toString()
            
            val ore = statistiche.tempoTotale / 3600000
            val minuti = (statistiche.tempoTotale % 3600000) / 60000
            tvTempoTotale.text = String.format("%dh %dm", ore, minuti)
            
        } catch (e: Exception) {
            Log.e("DettagliClubActivity", "Errore nel caricamento statistiche", e)
        }
    }
}