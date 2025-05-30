package com.example.mover

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mover.adapter.AttivitàAdapter
import com.example.mover.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DettaglioGiornoActivity : AppCompatActivity() {

    private lateinit var adapter: AttivitàAdapter
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtGiorno: TextView
    private lateinit var txtNomeGiorno: TextView
    private lateinit var dateCardView: CardView
    private lateinit var btnFilter: ImageButton
    private var currentDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dettaglio_giorno)

        // Imposta il colore della status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.background_arancione_dark)

        // Imposta il colore della navigation bar
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background_giorno)

        db = AppDatabase.getDatabase(this)

        recyclerView = findViewById(R.id.recyclerViewDettaglioAttivita)
        txtGiorno = findViewById(R.id.txtGiorno)
        txtNomeGiorno = findViewById(R.id.txtNomeGiorno)
        dateCardView = findViewById(R.id.dateCardView)
        btnFilter = findViewById(R.id.btnFilter)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AttivitàAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        // Ottieni la data passata tramite Intent
        val dataSelezionata = intent.getStringExtra("DATA_SELEZIONATA")

        try {
            val inputFormat = SimpleDateFormat("d/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val calendar = Calendar.getInstance()
            dataSelezionata?.let {
                val date = inputFormat.parse(it)
                calendar.time = date ?: calendar.time

                // Imposta la data nel cerchio in alto
                val giornoMese = SimpleDateFormat("d", Locale.getDefault()).format(calendar.time)
                val nomeGiorno =
                    SimpleDateFormat("EEE", Locale.ITALIAN).format(calendar.time).uppercase()

                txtGiorno.text = giornoMese
                txtNomeGiorno.text = nomeGiorno

                val dataFormattata = outputFormat.format(calendar.time)
                caricaAttivitàDelGiorno(dataFormattata)
                val inputFormat = SimpleDateFormat("d/MM/yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                dataSelezionata?.let {
                    val date = inputFormat.parse(it)
                    currentDate.time = date ?: currentDate.time

                    // Imposta la data nel cerchio in alto
                    updateDateDisplay(currentDate)


                }
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
              }
            } catch (e: Exception) {
                Log.e("DettaglioGiornoActivity", "Errore nel parsing della data: ${e.message}")
                Toast.makeText(this, "Errore nel caricamento della data", Toast.LENGTH_SHORT).show()
                finish()
            }

            // Imposta il click listener sulla card
            dateCardView.setOnClickListener {
                showDatePicker()
            }

            // Click sul pulsante di filtro
            btnFilter.setOnClickListener {
                mostraDialogFiltri()
            }
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }


    private fun caricaAttivitàDelGiorno(dataSelezionata: String) {
        val attivitàDao = db.attivitàDao()

        lifecycleScope.launch {
            val attivitàDelGiorno = withContext(Dispatchers.IO) {
                attivitàDao.getAttivitàPerData(dataSelezionata)
            }

            if (attivitàDelGiorno.isNotEmpty()) {
                adapter.updateData(attivitàDelGiorno)
            } else {
                adapter.updateData(emptyList())
                Toast.makeText(
                    this@DettaglioGiornoActivity,
                    "Nessuna attività trovata per questa data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            ContextThemeWrapper(this, R.style.CustomDatePickerDialog),
            null,  // Rimuove il callback dai pulsanti
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.apply {
            // Imposta la data massima a oggi
            datePicker.maxDate = System.currentTimeMillis()

            // Rimuove i pulsanti
            setButton(DatePickerDialog.BUTTON_POSITIVE, null, null as DialogInterface.OnClickListener?)
            setButton(DatePickerDialog.BUTTON_NEGATIVE, null, null as DialogInterface.OnClickListener?)

            // Imposta il background arrotondato
            window?.setBackgroundDrawableResource(R.drawable.rounded_card_background)

            // Aggiungi il listener direttamente al DatePicker
            datePicker.setOnDateChangedListener { _, year, month, dayOfMonth ->
                currentDate.set(Calendar.YEAR, year)
                currentDate.set(Calendar.MONTH, month)
                currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                updateDateDisplay(currentDate)

                // Carica le attività per la nuova data
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dataFormattata = outputFormat.format(currentDate.time)
                caricaAttivitàDelGiorno(dataFormattata)

                dismiss()
            }
        }

        datePickerDialog.show()
    }

    private fun updateDateDisplay(calendar: Calendar) {
        val giornoMese = SimpleDateFormat("d", Locale.getDefault()).format(calendar.time)
        val nomeGiorno = SimpleDateFormat("EEE", Locale.ITALIAN).format(calendar.time).uppercase()

        txtGiorno.text = giornoMese
        txtNomeGiorno.text = nomeGiorno
    }

    private fun mostraDialogFiltri() {

        val filtri = arrayOf("Tutte", "Automatiche", "Manuali", "Camminata", "Corsa", "Bicicletta", "Macchina", "Seduto")
        var filtroSelezionato = 0 // Indice iniziale della selezione

        val builder = android.app.AlertDialog.Builder(this, R.style.FilterButtonStyle)
        builder.setTitle("Filtri Attività")

        builder.setSingleChoiceItems(filtri, filtroSelezionato) { dialog, which ->
            filtroSelezionato = which
            val tipoFiltro = filtri[which]
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataFormattata = outputFormat.format(currentDate.time)

            filtraAttività(tipoFiltro, dataFormattata)
            dialog.dismiss() // Chiude il popup dopo la selezione
        }

        val dialog = builder.create()
        dialog.show()
    }





    private fun filtraAttività(tipoFiltro: String, dataSelezionata: String) {
        lifecycleScope.launch {
            val attivitàDao = db.attivitàDao()
            val attivitàFiltrate = withContext(Dispatchers.IO) {
                when (tipoFiltro) {
                    "Tutte" -> attivitàDao.getAttivitàPerData(dataSelezionata)
                    "Automatiche" -> attivitàDao.getAttivitàPerTipo(true, dataSelezionata)
                    "Manuali" -> attivitàDao.getAttivitàPerTipo(false, dataSelezionata)
                    "Camminata" -> attivitàDao.getAttivitàPerTipo1("Camminare", dataSelezionata)
                    "Corsa" -> attivitàDao.getAttivitàPerTipo1("Corsa", dataSelezionata)
                    "Bicicletta" -> attivitàDao.getAttivitàPerTipo1("ON_BICYCLE", dataSelezionata)
                    "Veicolo" -> attivitàDao.getAttivitàPerTipo1("IN_VEHICLE", dataSelezionata)
                    "Seduto" -> attivitàDao.getAttivitàPerTipo1("Sedersi", dataSelezionata)
                    else -> emptyList()
                }
            }

            if (attivitàFiltrate.isNotEmpty()) {
                adapter.updateData(attivitàFiltrate)
            } else {
                adapter.updateData(emptyList())
                Toast.makeText(this@DettaglioGiornoActivity, "Nessuna attività trovata", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
