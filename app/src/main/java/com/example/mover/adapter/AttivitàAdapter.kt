package com.example.mover.adapter

import android.content.Context
import android.graphics.PorterDuff
import com.example.mover.data.Attività
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.R
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AttivitàAdapter(
    private var listaAttività: List<Attività>,
    private val context: Context
) : RecyclerView.Adapter<AttivitàAdapter.AttivitàViewHolder>() {

    inner class AttivitàViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val btnShowMap: ImageButton = itemView.findViewById(R.id.btnShowMap)

        val container: View = itemView.findViewById(R.id.container)
        val imgTipoAttivita: ImageView = itemView.findViewById(R.id.imgTipoAttivita)
        val txtOraInizio: TextView = itemView.findViewById(R.id.txtOraInizio)
        val txtOraFine: TextView = itemView.findViewById(R.id.txtOraFine)

        val layoutPassi: LinearLayout = itemView.findViewById(R.id.layoutPassi)
        val layoutDistanza: LinearLayout = itemView.findViewById(R.id.layoutDistanza)
        val layoutTempo: LinearLayout = itemView.findViewById(R.id.layoutTempo)
        val layoutVelocita: LinearLayout = itemView.findViewById(R.id.layoutVelocita)
        val layoutTempoPausa: LinearLayout = itemView.findViewById(R.id.layoutTempoPausa)
        val layoutPassoMedio: LinearLayout = itemView.findViewById(R.id.layoutPaceMedio)

        val txtPassi: TextView = itemView.findViewById(R.id.txtPassi)
        val distanzaView: TextView = itemView.findViewById(R.id.distanzaView)
        val tempoView: TextView = itemView.findViewById(R.id.tempoView)
        val velocitaMediaView: TextView = itemView.findViewById(R.id.velocitaMediaView)
        val txtTempoInPausa: TextView = itemView.findViewById(R.id.txtTempoInPausa)
        val layoutPaceMedio: LinearLayout = itemView.findViewById(R.id.layoutPaceMedio)
        val paceMedioView: TextView = itemView.findViewById(R.id.paceMedioView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttivitàViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attivita, parent, false)
        return AttivitàViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttivitàViewHolder, position: Int) {
        val attività = listaAttività[position]

        val (iconaResId, coloreTint) = when(attività.tipo) {
            "Camminare" -> Pair(
                R.drawable.footprint_24px,
                ContextCompat.getColor(context, R.color.colorCamminare)
            )
            "Corsa" -> Pair(
                R.drawable.directions_run_24px,
                ContextCompat.getColor(context, R.color.colorCorrere)
            )
            "Sedersi" -> Pair(
                R.drawable.seat_24px,
                ContextCompat.getColor(context, R.color.colorSedersi)
            )
            "Guidare" -> Pair(
                R.drawable.directions_car,
                ContextCompat.getColor(context, R.color.colorGuidare)
            )
            "Bicicletta" -> Pair(
                R.drawable.directions_bike,
                ContextCompat.getColor(context, R.color.colorBici)
            )
            else -> Pair(
                R.drawable.psychology_alt,
                ContextCompat.getColor(context, R.color.colorCamminare)
            )
        }

        holder.imgTipoAttivita.apply {
            setImageResource(iconaResId)
            setColorFilter(coloreTint, PorterDuff.Mode.SRC_IN)
        }
        // Formatta e imposta gli orari
        holder.txtOraInizio.text = convertiOra(attività.oraInizio)
        holder.txtOraFine.text = convertiOra(attività.oraFine)

        // Nascondi tutti i layout inizialmente
        holder.layoutPassi.visibility = View.GONE
        holder.layoutDistanza.visibility = View.GONE
        holder.layoutTempo.visibility = View.GONE
        holder.layoutVelocita.visibility = View.GONE
        holder.layoutTempoPausa.visibility = View.GONE
        holder.layoutPassoMedio.visibility = View.GONE


        holder.btnShowMap.setOnClickListener {
            showBottomSheetDetails(attività)
        }

        when (attività.tipo) {
            "Camminare" -> {
                if (attività.passi != null) {
                    holder.layoutPassi.visibility = View.VISIBLE
                    holder.txtPassi.text = "${attività.passi}"
                } else {
                    holder.layoutPassi.visibility = View.GONE
                }

                if (attività.distanza != null) {
                    holder.layoutDistanza.visibility = View.VISIBLE
                    holder.distanzaView.text = formattaDistanza(attività.distanza)
                } else {
                    holder.layoutDistanza.visibility = View.GONE
                }

                if (attività.tempo != null) {
                    holder.layoutTempo.visibility = View.VISIBLE
                    holder.tempoView.text = formattaTempo(attività.tempo)
                } else {
                    holder.layoutTempo.visibility = View.GONE
                }
            }


            "Corsa" -> {
                if (attività.distanza != null) {
                    holder.layoutDistanza.visibility = View.VISIBLE
                    holder.distanzaView.text = formattaDistanza(attività.distanza)
                }

                if (attività.tempo != null) {
                    holder.layoutTempo.visibility = View.VISIBLE
                    holder.tempoView.text = formattaTempo(attività.tempo)
                }

                if (attività.velocitaMedia != null) {
                    holder.layoutVelocita.visibility = View.VISIBLE
                    holder.velocitaMediaView.text = String.format("%.2f km/h", attività.velocitaMedia)
                }


                if (attività.tempoInPausa != null && attività.tempoInPausa > 0) {
                    holder.layoutTempoPausa.visibility = View.VISIBLE
                    holder.txtTempoInPausa.text = formattaTempo(attività.tempoInPausa)
                }

                if (attività.paceMedio != null) {
                    holder.layoutPaceMedio.visibility = View.VISIBLE
                    // Converti il pace in minuti:secondi
                    val paceMinuti = attività.paceMedio.toInt()
                    val paceSecondi = ((attività.paceMedio - paceMinuti) * 60).toInt()
                    holder.paceMedioView.text = String.format("%d:%02d min/km", paceMinuti, paceSecondi)
                }
            }

            "Sedersi" -> {
                    if (attività.volteSeduto != null) {
                        holder.layoutPassi.visibility = View.VISIBLE
                        holder.layoutPassi.findViewById<ImageView>(R.id.icona_passi)?.setImageResource(R.drawable.seat_24px)
                        holder.layoutPassi.findViewById<TextView>(R.id.passiLabel)?.text = "Volte seduto"
                        holder.txtPassi.text = "${attività.volteSeduto}"
                    } else {
                        holder.layoutPassi.visibility = View.GONE
                    }

                if (attività.tempo != null) {
                    holder.layoutTempo.visibility = View.VISIBLE
                    holder.tempoView.text = formattaTempo(attività.tempo)
                }

            }
            "Guidare" -> {
                if (attività.distanza != null) {
                    holder.layoutDistanza.visibility = View.VISIBLE
                    holder.distanzaView.text = formattaDistanza(attività.distanza)
                }

                if (attività.tempo != null) {
                    holder.layoutTempo.visibility = View.VISIBLE
                    holder.tempoView.text = formattaTempo(attività.tempo)
                }

                if (attività.velocitaMedia != null) {
                    holder.layoutVelocita.visibility = View.VISIBLE
                    holder.velocitaMediaView.text = String.format("%.2f km/h", attività.velocitaMedia)
                }
            }

            "Bicicletta" -> {
                if (attività.distanza != null) {
                    holder.layoutDistanza.visibility = View.VISIBLE
                    holder.distanzaView.text = formattaDistanza(attività.distanza)
                }

                if (attività.tempo != null) {
                    holder.layoutTempo.visibility = View.VISIBLE
                    holder.tempoView.text = formattaTempo(attività.tempo)
                }

                if (attività.velocitaMedia != null) {
                    holder.layoutVelocita.visibility = View.VISIBLE
                    holder.velocitaMediaView.text = String.format("%.2f km/h", attività.velocitaMedia)
                }
            }
        }
        if (attività.Automatica) {
            holder.container.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.automatica)
            )
        } else {
            holder.container.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.automatica2)
            )
        }
    }

    override fun getItemCount() = listaAttività.size

    private fun convertiOra(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formattaDistanza(distanza: Float): String {
        return if (distanza < 1000) {
            "${distanza.toInt()} m"
        } else {
            String.format("%.2f km", distanza / 1000)
        }
    }

    private fun formattaTempo(tempoInMillis: Long): String {
        // Se il tempo è minore di un minuto, mostra solo i secondi
        if (tempoInMillis < 60000) {
            val secondi = (tempoInMillis / 1000.0).toInt()
            return String.format("%d sec", secondi)
        }

        // mostra minuti e secondi
        val minuti = (tempoInMillis / 60000).toInt()
        val secondi = ((tempoInMillis % 60000) / 1000).toInt()
        return String.format("%d min %d sec", minuti, secondi)
    }

    fun updateData(nuovaLista: List<Attività>) {
        listaAttività = nuovaLista
        notifyDataSetChanged()
    }

    fun showBottomSheetDetails(attività: Attività) {
        val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(context).inflate(R.layout.activity_details_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        val screenHeight = context.resources.displayMetrics.heightPixels
        val peekHeight = (screenHeight * 0.9).toInt()

        val includedView = view.findViewById<View>(R.id.included_item_attivita)
        includedView.elevation = 0f


        behavior.apply {
            this.peekHeight = peekHeight
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
            isDraggable = false
            expandedOffset = screenHeight - peekHeight
        }

        val mapFragment = (context as AppCompatActivity)
            .supportFragmentManager
            .findFragmentById(R.id.mapDetail) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            googleMap.uiSettings.apply {
                setAllGesturesEnabled(true)
                isMyLocationButtonEnabled = false
            }

        }

        view.findViewById<ImageButton>(R.id.btnShowMap)?.visibility = View.GONE
        view.findViewById<ImageView>(R.id.imgTipoAttivita)?.visibility = View.GONE
        view.findViewById<View>(R.id.linea_separatrice)?.visibility = View.GONE

        // Imposta il background bianco per il container
        val container = view.findViewById<View>(R.id.container)



        val txtOraInizio = view.findViewById<TextView>(R.id.txtOraInizio)
        val txtOraFine = view.findViewById<TextView>(R.id.txtOraFine)
        val layoutPassi = view.findViewById<LinearLayout>(R.id.layoutPassi)
        val layoutDistanza = view.findViewById<LinearLayout>(R.id.layoutDistanza)
        val layoutTempo = view.findViewById<LinearLayout>(R.id.layoutTempo)
        val layoutVelocita = view.findViewById<LinearLayout>(R.id.layoutVelocita)
        val layoutTempoPausa = view.findViewById<LinearLayout>(R.id.layoutTempoPausa)
        val layoutPaceMedio = view.findViewById<LinearLayout>(R.id.layoutPaceMedio)

        // TextViews per i valori
        val txtPassi = view.findViewById<TextView>(R.id.txtPassi)
        val distanzaView = view.findViewById<TextView>(R.id.distanzaView)
        val tempoView = view.findViewById<TextView>(R.id.tempoView)
        val velocitaMediaView = view.findViewById<TextView>(R.id.velocitaMediaView)
        val txtTempoInPausa = view.findViewById<TextView>(R.id.txtTempoInPausa)
        val paceMedioView = view.findViewById<TextView>(R.id.paceMedioView)

        val (iconaResId, coloreTint) = when(attività.tipo) {
            "Camminare" -> Pair(R.drawable.footprint_24px, ContextCompat.getColor(context, R.color.colorCamminare))
            "Corsa" -> Pair(R.drawable.directions_run_24px, ContextCompat.getColor(context, R.color.colorCorrere))
            "Sedersi" -> Pair(R.drawable.seat_24px, ContextCompat.getColor(context, R.color.colorSedersi))
            "Guidare" -> Pair(R.drawable.directions_car, ContextCompat.getColor(context, R.color.colorGuidare))
            "Bicicletta" -> Pair(R.drawable.directions_bike, ContextCompat.getColor(context, R.color.colorBici))
            else -> Pair(R.drawable.psychology_alt, ContextCompat.getColor(context, R.color.colorCamminare))
        }

        txtOraInizio.text = convertiOra(attività.oraInizio)
        txtOraFine.text = convertiOra(attività.oraFine)

        layoutPassi.visibility = View.GONE
        layoutDistanza.visibility = View.GONE
        layoutTempo.visibility = View.GONE
        layoutVelocita.visibility = View.GONE
        layoutTempoPausa.visibility = View.GONE
        layoutPaceMedio.visibility = View.GONE

        when (attività.tipo) {
            "Camminare" -> {
                if (attività.passi != null) {
                    layoutPassi.visibility = View.VISIBLE
                    txtPassi.text = "${attività.passi}"
                }
                if (attività.distanza != null) {
                    layoutDistanza.visibility = View.VISIBLE
                    distanzaView.text = formattaDistanza(attività.distanza)
                }
                if (attività.tempo != null) {
                    layoutTempo.visibility = View.VISIBLE
                    tempoView.text = formattaTempo(attività.tempo)
                }
            }
            "Corsa" -> {
                if (attività.distanza != null) {
                    layoutDistanza.visibility = View.VISIBLE
                    distanzaView.text = formattaDistanza(attività.distanza)
                }
                if (attività.tempo != null) {
                    layoutTempo.visibility = View.VISIBLE
                    tempoView.text = formattaTempo(attività.tempo)
                }
                if (attività.velocitaMedia != null) {
                    layoutVelocita.visibility = View.VISIBLE
                    velocitaMediaView.text = String.format("%.2f km/h", attività.velocitaMedia)
                }
                if (attività.tempoInPausa != null && attività.tempoInPausa > 0) {
                    layoutTempoPausa.visibility = View.VISIBLE
                    txtTempoInPausa.text = formattaTempo(attività.tempoInPausa)
                }
                if (attività.paceMedio != null) {
                    layoutPaceMedio.visibility = View.VISIBLE
                    val paceMinuti = attività.paceMedio.toInt()
                    val paceSecondi = ((attività.paceMedio - paceMinuti) * 60).toInt()
                    paceMedioView.text = String.format("%d:%02d min/km", paceMinuti, paceSecondi)
                }
            }
            "Sedersi" -> {
                if (attività.volteSeduto != null) {
                    layoutPassi.visibility = View.VISIBLE
                    view.findViewById<ImageView>(R.id.icona_passi)?.setImageResource(R.drawable.seat_24px)
                    view.findViewById<TextView>(R.id.passiLabel)?.text = "Volte seduto"
                    txtPassi.text = "${attività.volteSeduto}"
                }
                if (attività.tempo != null) {
                    layoutTempo.visibility = View.VISIBLE
                    tempoView.text = formattaTempo(attività.tempo)
                }
            }

            "Guidare" -> {
                if (attività.distanza != null) {
                    layoutDistanza.visibility = View.VISIBLE
                    distanzaView.text = formattaDistanza(attività.distanza)
                }
                if (attività.tempo != null) {
                    layoutTempo.visibility = View.VISIBLE
                    tempoView.text = formattaTempo(attività.tempo)
                }
                if (attività.velocitaMedia != null) {
                    layoutVelocita.visibility = View.VISIBLE
                    velocitaMediaView.text = String.format("%.2f km/h", attività.velocitaMedia)
                }
            }

            "Bicicletta" -> {
                if (attività.distanza != null) {
                    layoutDistanza.visibility = View.VISIBLE
                    distanzaView.text = formattaDistanza(attività.distanza)
                }
                if (attività.tempo != null) {
                    layoutTempo.visibility = View.VISIBLE
                    tempoView.text = formattaTempo(attività.tempo)
                }
                if (attività.velocitaMedia != null) {
                    layoutVelocita.visibility = View.VISIBLE
                    velocitaMediaView.text = String.format("%.2f km/h", attività.velocitaMedia)
                }
            }

        }

        // Imposta il background per attività automatica/manuale
        if (attività.Automatica) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.automatica))
        } else {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.automatica2))
        }
        val dragArea = view.findViewById<View>(R.id.dragHandle)
        dragArea.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    behavior.isDraggable = true
                }
            }
            false
        }
        bottomSheetDialog.setOnDismissListener {
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            val fragment = fragmentManager.findFragmentById(R.id.mapDetail)
            if (fragment != null) {
                fragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
            }
        }

        bottomSheetDialog.show()
    }
}