package com.example.mover.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.R
import com.example.mover.database.entities.social.*
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView

data class MembroClubWithDetails(
    val utente: Utente,
    val membro: MembroClub,
    val statistiche: String = ""
)

class MembriClubAdapter(
    private val onMembroClick: (MembroClubWithDetails) -> Unit
) : RecyclerView.Adapter<MembriClubAdapter.MembroViewHolder>() {

    private var membri = listOf<MembroClubWithDetails>()

    fun updateMembri(nuoviMembri: List<MembroClubWithDetails>) {
        membri = nuoviMembri
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_membro_club, parent, false)
        return MembroViewHolder(view)
    }

    override fun onBindViewHolder(holder: MembroViewHolder, position: Int) {
        holder.bind(membri[position])
    }

    override fun getItemCount(): Int = membri.size

    inner class MembroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatarMembro: ShapeableImageView = itemView.findViewById(R.id.ivAvatarMembro)
        private val tvNomeMembro: TextView = itemView.findViewById(R.id.tvNomeMembro)
        private val tvRuoloMembro: TextView = itemView.findViewById(R.id.tvRuoloMembro)
        private val tvStatisticheMembro: TextView = itemView.findViewById(R.id.tvStatisticheMembro)
        private val chipRuolo: Chip = itemView.findViewById(R.id.chipRuolo)

        fun bind(membroDetails: MembroClubWithDetails) {
            val utente = membroDetails.utente
            val membro = membroDetails.membro

            tvNomeMembro.text = "${utente.nome} ${utente.cognome}"
            
            // Imposta il ruolo
            val ruolo = when (membro.ruolo) {
                "admin" -> "Amministratore"
                "moderator" -> "Moderatore"
                "member" -> "Membro"
                else -> "Membro"
            }
            tvRuoloMembro.text = ruolo

            // Mostra statistiche se disponibili
            if (membroDetails.statistiche.isNotEmpty()) {
                tvStatisticheMembro.text = membroDetails.statistiche
                tvStatisticheMembro.visibility = View.VISIBLE
            } else {
                tvStatisticheMembro.visibility = View.GONE
            }

            // Mostra chip per ruoli speciali
            when (membro.ruolo) {
                "admin" -> {
                    chipRuolo.text = "Admin"
                    chipRuolo.visibility = View.VISIBLE
                }
                "moderator" -> {
                    chipRuolo.text = "Mod"
                    chipRuolo.visibility = View.VISIBLE
                }
                else -> {
                    chipRuolo.visibility = View.GONE
                }
            }

            // Imposta avatar placeholder
            ivAvatarMembro.setImageResource(R.drawable.ic_person)

            // Click listener
            itemView.setOnClickListener {
                onMembroClick(membroDetails)
            }
        }
    }
}