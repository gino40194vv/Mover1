package com.example.mover.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.R
import java.text.SimpleDateFormat
import java.util.*

class FeedSocialeAdapter(
    private val onKudosClick: (Long) -> Unit,
    private val onCommentoClick: (Long) -> Unit,
    private val onProfiloClick: (Long) -> Unit,
    private val onAttivitaClick: (Long) -> Unit
) : ListAdapter<Map<String, Any>, FeedSocialeAdapter.FeedViewHolder>(FeedDiffCallback()) {
    
    private val items = mutableListOf<Map<String, Any>>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed_attivita, parent, false)
        return FeedViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun addItems(newItems: List<Map<String, Any>>) {
        items.addAll(newItems)
        submitList(items.toList())
    }
    
    override fun submitList(list: List<Map<String, Any>>?) {
        items.clear()
        list?.let { items.addAll(it) }
        super.submitList(items.toList())
    }
    
    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfilo: ImageView = itemView.findViewById(R.id.imgProfilo)
        private val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        private val txtTempo: TextView = itemView.findViewById(R.id.txtTempo)
        private val txtTipoAttivita: TextView = itemView.findViewById(R.id.txtTipoAttivita)
        private val txtTitolo: TextView = itemView.findViewById(R.id.txtTitolo)
        private val txtDescrizione: TextView = itemView.findViewById(R.id.txtDescrizione)
        private val txtStatistiche: TextView = itemView.findViewById(R.id.txtStatistiche)
        private val btnKudos: ImageButton = itemView.findViewById(R.id.btnKudos)
        private val btnCommenti: ImageButton = itemView.findViewById(R.id.btnCommenti)
        private val btnCondividi: ImageButton = itemView.findViewById(R.id.btnCondividi)
        private val txtKudosCount: TextView = itemView.findViewById(R.id.txtKudosCount)
        private val txtCommentiCount: TextView = itemView.findViewById(R.id.txtCommentiCount)
        
        fun bind(item: Map<String, Any>) {
            val attivitaId = item["attivitaId"] as Long
            val utenteId = item["utenteId"] as? Long ?: 0L
            val username = item["username"] as? String ?: "Utente"
            val tipoAttivita = item["tipoAttivita"] as? String ?: "Attività"
            val titolo = item["titolo"] as? String ?: ""
            val descrizione = item["descrizione"] as? String ?: ""
            val distanza = item["distanza"] as? Float ?: 0f
            val durata = item["durata"] as? Long ?: 0L
            val velocitaMedia = item["velocitaMedia"] as? Float ?: 0f
            val dislivello = item["dislivello"] as? Float ?: 0f
            val dataInizio = item["dataInizio"] as? Long ?: System.currentTimeMillis()
            val kudosCount = item["likes"] as? Int ?: 0
            val commentiCount = item["commenti"] as? Int ?: 0
            
            // Imposta dati base
            txtUsername.text = username
            txtTipoAttivita.text = tipoAttivita
            txtTitolo.text = if (titolo.isNotEmpty()) titolo else "Attività $tipoAttivita"
            
            if (descrizione.isNotEmpty()) {
                txtDescrizione.text = descrizione
                txtDescrizione.visibility = View.VISIBLE
            } else {
                txtDescrizione.visibility = View.GONE
            }
            
            // Formatta tempo
            val dateFormat = SimpleDateFormat("dd MMM 'alle' HH:mm", Locale.getDefault())
            txtTempo.text = dateFormat.format(Date(dataInizio))
            
            // Formatta statistiche
            val statistiche = buildString {
                if (distanza > 0) {
                    append("${String.format("%.2f", distanza)} km")
                }
                if (durata > 0) {
                    if (isNotEmpty()) append(" • ")
                    val ore = durata / 3600000
                    val minuti = (durata % 3600000) / 60000
                    val secondi = (durata % 60000) / 1000
                    
                    when {
                        ore > 0 -> append("${ore}h ${minuti}m")
                        minuti > 0 -> append("${minuti}m ${secondi}s")
                        else -> append("${secondi}s")
                    }
                }
                if (velocitaMedia > 0) {
                    if (isNotEmpty()) append(" • ")
                    append("${String.format("%.1f", velocitaMedia)} km/h")
                }
                if (dislivello > 0) {
                    if (isNotEmpty()) append(" • ")
                    append("↗ ${dislivello.toInt()}m")
                }
            }
            txtStatistiche.text = statistiche
            
            // Imposta contatori
            txtKudosCount.text = if (kudosCount > 0) kudosCount.toString() else ""
            txtCommentiCount.text = if (commentiCount > 0) commentiCount.toString() else ""
            
            // Click listeners
            imgProfilo.setOnClickListener { onProfiloClick(utenteId) }
            txtUsername.setOnClickListener { onProfiloClick(utenteId) }
            
            itemView.setOnClickListener { onAttivitaClick(attivitaId) }
            
            btnKudos.setOnClickListener { onKudosClick(attivitaId) }
            btnCommenti.setOnClickListener { onCommentoClick(attivitaId) }
            
            btnCondividi.setOnClickListener {
                // Implementa condivisione
                condividiAttivita(attivitaId, username, tipoAttivita, distanza)
            }
            
            // TODO: Carica immagine profilo con Glide/Picasso
            // Glide.with(itemView.context).load(immagineProfilo).into(imgProfilo)
        }
        
        private fun condividiAttivita(attivitaId: Long, username: String, tipo: String, distanza: Float) {
            val context = itemView.context
            val shareText = "$username ha completato una $tipo di ${String.format("%.2f", distanza)} km!"
            
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }
            
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Condividi attività"))
        }
    }
    
    class FeedDiffCallback : DiffUtil.ItemCallback<Map<String, Any>>() {
        override fun areItemsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
            return oldItem["id"] == newItem["id"]
        }
        
        override fun areContentsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
            return oldItem == newItem
        }
    }
}