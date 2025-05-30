package com.example.mover.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analisi_passo_gara",
    foreignKeys = [
        ForeignKey(
            entity = Attività::class,
            parentColumns = ["id"],
            childColumns = ["attivitaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["attivitaId"]),
        Index(value = ["tipoAnalisi"])
    ]
)
data class AnalisiPassoGara(
    @PrimaryKey
    val id: String,
    val attivitaId: String,
    val tipoAnalisi: TipoAnalisiPasso,
    val distanzaKm: Double, // Distanza della gara/segmento
    val tempoTotale: Long, // Tempo totale in millisecondi
    val passoMedio: Double, // Passo medio in min/km
    val velocitaMedia: Double, // Velocità media in km/h
    val split1km: List<Double> = emptyList(), // Split ogni km
    val split5km: List<Double> = emptyList(), // Split ogni 5km
    val splitPersonalizzati: List<SplitPersonalizzato> = emptyList(),
    val negativeSplit: Boolean = false, // Se la seconda metà è più veloce
    val evenSplit: Boolean = false, // Se il passo è costante
    val positiveSplit: Boolean = false, // Se la prima metà è più veloce
    val variabilitaPasso: Double = 0.0, // Coefficiente di variazione del passo
    val efficienza: Double = 0.0, // Efficienza della strategia di gara
    val puntiForza: List<String> = emptyList(), // Punti di forza identificati
    val areeeMiglioramento: List<String> = emptyList(), // Aree da migliorare
    val raccomandazioni: String? = null, // Suggerimenti per gare future
    val confrontoObiettivo: ConfrontoObiettivo? = null
)

data class SplitPersonalizzato(
    val distanza: Double, // km
    val tempo: Long, // millisecondi
    val passo: Double, // min/km
    val velocita: Double, // km/h
    val posizione: Int // Posizione nel percorso
)

data class ConfrontoObiettivo(
    val tempoObiettivo: Long, // millisecondi
    val passoObiettivo: Double, // min/km
    val differenzaTempo: Long, // millisecondi (+ se più lento, - se più veloce)
    val differenzaPasso: Double, // min/km
    val obiettivoRaggiunto: Boolean
)

enum class TipoAnalisiPasso {
    GARA_5KM,
    GARA_10KM,
    MEZZA_MARATONA,
    MARATONA,
    GARA_PERSONALIZZATA,
    ALLENAMENTO_LUNGO,
    TEMPO_RUN,
    INTERVAL_TRAINING,
    FARTLEK
}