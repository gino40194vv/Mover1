package com.example.mover.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.R
import com.example.mover.database.entities.social.*
import com.google.android.material.imageview.ShapeableImageView

data class ClassificaClubWithDetails(
    val utente: Utente,
    val classifica: ClassificaClub,
    val posizione: Int,
    val dettagli: String = ""
)

class ClassificaClubAdapter(
    private val onClassificaClick: (ClassificaClubWithDetails) -> Unit
) : RecyclerView.Adapter<ClassificaClubAdapter.ClassificaViewHolder>() {

    private var classifiche = listOf<ClassificaClubWithDetails>()

    fun updateClassifica(nuoveClassifiche: List<ClassificaClubWithDetails>) {
        classifiche = nuoveClassifiche
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassificaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_classifica_club, parent, false)
        return ClassificaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassificaViewHolder, position: Int) {
        holder.bind(classifiche[position])
    }

    override fun getItemCount(): Int = classifiche.size

    inner class ClassificaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPosizione: TextView = itemView.findViewById(R.id.tvPosizione)
        private val ivAvatarUtente: ShapeableImageView = itemView.findViewById(R.id.ivAvatarUtente)
        private val tvNomeUtente: TextView = itemView.findViewById(R.id.tvNomeUtente)
        private val tvDettagliClassifica: TextView = itemView.findViewById(R.id.tvDettagliClassifica)
        private val tvValoreMetrica: TextView = itemView.findViewById(R.id.tvValoreMetrica)
        private val tvTipoMetrica: TextView = itemView.findViewById(R.id.tvTipoMetrica)

        fun bind(classificaDetails: ClassificaClubWithDetails) {
            val utente = classificaDetails.utente
            val classifica = classificaDetails.classifica

            // Posizione
            tvPosizione.text = classificaDetails.posizione.toString()

            // Nome utente
            tvNomeUtente.text = "${utente.nome} ${utente.cognome}"

            // Dettagli se disponibili
            if (classificaDetails.dettagli.isNotEmpty()) {
                tvDettagliClassifica.text = classificaDetails.dettagli
                tvDettagliClassifica.visibility = View.VISIBLE
            } else {
                tvDettagliClassifica.visibility = View.GONE
            }

            // Valore e tipo metrica
            when (classifica.tipoMetrica) {
                "distanza" -> {
                    tvValoreMetrica.text = String.format("%.1f km", classifica.valore / 1000)
                    tvTipoMetrica.text = "Distanza"
                }
                "tempo" -> {
                    val ore = (classifica.valore / 3600000).toInt()
                    val minuti = ((classifica.valore % 3600000) / 60000).toInt()
                    tvValoreMetrica.text = String.format("%dh %dm", ore, minuti)
                    tvTipoMetrica.text = "Tempo"
                }
                "dislivello" -> {
                    tvValoreMetrica.text = String.format("%.0f m", classifica.valore)
                    tvTipoMetrica.text = "Dislivello"
                }
                "attivita" -> {
                    tvValoreMetrica.text = classifica.valore.toInt().toString()
                    tvTipoMetrica.text = "Attività"
                }
                else -> {
                    tvValoreMetrica.text = classifica.valore.toString()
                    tvTipoMetrica.text = "Punti"
                }
            }

            // Imposta avatar placeholder
            ivAvatarUtente.setImageResource(R.drawable.ic_person)

            // Click listener
            itemView.setOnClickListener {
                onClassificaClick(classificaDetails)
            }
        }
    }
}