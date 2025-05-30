package com.example.mover.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "classifica_segmento",
    foreignKeys = [
        ForeignKey(
            entity = Segmento::class,
            parentColumns = ["id"],
            childColumns = ["segmentoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TentativoSegmento::class,
            parentColumns = ["id"],
            childColumns = ["tentativoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["segmentoId"]),
        Index(value = ["tentativoId"]),
        Index(value = ["posizione"]),
        Index(value = ["utenteId"])
    ]
)
data class ClassificaSegmento(
    @PrimaryKey
    val id: String,
    val segmentoId: String,
    val tentativoId: String,
    val utenteId: String,
    val posizione: Int,
    val tempoImpiegato: Long, // in millisecondi
    val velocitaMedia: Double, // km/h
    val dataOra: Date,
    val isPR: Boolean = false, // Personal Record
    val isKOM: Boolean = false, // King/Queen of Mountain
    val distaccoVincitore: Long = 0, // in millisecondi
    val distaccoPrecedente: Long = 0, // in millisecondi dal precedente
    val categoria: CategoriaClassifica = CategoriaClassifica.GENERALE,
    val genere: String? = null, // M/F per classifiche separate
    val fasciaEta: String? = null // Per classifiche per età
)

enum class CategoriaClassifica {
    GENERALE,
    MASCHILE,
    FEMMINILE,
    FASCIA_ETA,
    LOCALE, // Solo utenti della zona
    AMICI, // Solo amici dell'utente
    CLUB    // Solo membri del club
}