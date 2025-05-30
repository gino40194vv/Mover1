package com.example.mover.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.R
import com.example.mover.data.CategoriaAttività
import com.example.mover.data.TipoAttività
import com.example.mover.services.AdvancedTrackingService
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout

/**
 * Activity per la selezione del tipo di attività da tracciare
 * Organizzata per categorie con interfaccia simile a Strava
 */
class ActivitySelectionActivity : AppCompatActivity() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnStartActivity: MaterialButton
    
    private lateinit var adapter: ActivityTypeAdapter
    private var selectedActivityType: TipoAttività? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)
        
        initializeViews()
        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupStartButton()
        
        // Mostra la prima categoria di default
        showActivitiesForCategory(CategoriaAttività.CORSA)
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.recyclerViewActivities)
        btnStartActivity = findViewById(R.id.btnStartActivity)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Seleziona Attività"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupTabs() {
        // Aggiungi tab per ogni categoria
        CategoriaAttività.values().forEach { categoria ->
            val tab = tabLayout.newTab()
            tab.text = categoria.displayName
            tab.tag = categoria
            tabLayout.addTab(tab)
        }
        
        // Listener per cambio tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.tag?.let { categoria ->
                    showActivitiesForCategory(categoria as CategoriaAttività)
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupRecyclerView() {
        adapter = ActivityTypeAdapter { activityType ->
            selectedActivityType = activityType
            updateStartButton()
        }
        
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }
    
    private fun setupStartButton() {
        btnStartActivity.setOnClickListener {
            selectedActivityType?.let { activityType ->
                startActivityTracking(activityType)
            } ?: run {
                Toast.makeText(this, "Seleziona un tipo di attività", Toast.LENGTH_SHORT).show()
            }
        }
        
        updateStartButton()
    }
    
    private fun showActivitiesForCategory(categoria: CategoriaAttività) {
        val activities = TipoAttività.getByCategory(categoria)
        adapter.updateActivities(activities)
        
        // Reset selezione quando cambia categoria
        selectedActivityType = null
        updateStartButton()
    }
    
    private fun updateStartButton() {
        btnStartActivity.isEnabled = selectedActivityType != null
        btnStartActivity.text = selectedActivityType?.let { 
            "Inizia ${it.displayName}" 
        } ?: "Seleziona Attività"
    }
    
    private fun startActivityTracking(activityType: TipoAttività) {
        // Avvia il servizio di tracking avanzato
        val intent = Intent(this, AdvancedTrackingService::class.java).apply {
            action = AdvancedTrackingService.ACTION_START_TRACKING
            putExtra(AdvancedTrackingService.EXTRA_ACTIVITY_TYPE, activityType.name)
        }
        
        startForegroundService(intent)
        
        // Torna alla MainActivity con informazioni sull'attività avviata
        val resultIntent = Intent().apply {
            putExtra("activity_type", activityType.name)
            putExtra("activity_display_name", activityType.displayName)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

/**
 * Adapter per la RecyclerView dei tipi di attività
 */
class ActivityTypeAdapter(
    private val onActivitySelected: (TipoAttività) -> Unit
) : RecyclerView.Adapter<ActivityTypeAdapter.ActivityViewHolder>() {
    
    private var activities = listOf<TipoAttività>()
    private var selectedPosition = -1
    
    fun updateActivities(newActivities: List<TipoAttività>) {
        activities = newActivities
        selectedPosition = -1
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ActivityViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_type, parent, false)
        return ActivityViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        holder.bind(activity, position == selectedPosition) { selectedActivity ->
            val oldPosition = selectedPosition
            selectedPosition = position
            
            // Aggiorna solo le card che cambiano stato
            if (oldPosition != -1) notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            
            onActivitySelected(selectedActivity)
        }
    }
    
    override fun getItemCount() = activities.size
    
    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardActivity)
        private val iconText: android.widget.TextView = itemView.findViewById(R.id.txtActivityIcon)
        private val nameText: android.widget.TextView = itemView.findViewById(R.id.txtActivityName)
        private val featuresText: android.widget.TextView = itemView.findViewById(R.id.txtActivityFeatures)
        
        fun bind(
            activity: TipoAttività, 
            isSelected: Boolean, 
            onSelected: (TipoAttività) -> Unit
        ) {
            iconText.text = activity.icon
            nameText.text = activity.displayName
            
            // Mostra le caratteristiche supportate
            val features = mutableListOf<String>()
            if (activity.supportaGPS) features.add("GPS")
            if (activity.supportaFrequenzaCardiaca) features.add("❤️")
            if (activity.supportaCadenza) features.add("👟")
            if (activity.supportaPotenza) features.add("⚡")
            
            featuresText.text = features.joinToString(" ")
            featuresText.visibility = if (features.isNotEmpty()) View.VISIBLE else View.GONE
            
            // Aggiorna aspetto in base alla selezione
            cardView.isChecked = isSelected
            cardView.strokeWidth = if (isSelected) 4 else 1
            
            // Listener per selezione
            cardView.setOnClickListener {
                onSelected(activity)
            }
        }
    }
}