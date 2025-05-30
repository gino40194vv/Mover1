package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import com.example.mover.database.entities.CategoriaTrofeo
import com.example.mover.database.entities.LivelloTrofeo

@Entity(
    tableName = "classifica_trofeo",
    foreignKeys = [
        ForeignKey(
            entity = Trofeo::class,
            parentColumns = ["id"],
            childColumns = ["trofeoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["utenteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["trofeoId"]),
        Index(value = ["utenteId"]),
        Index(value = ["posizione"]),
        Index(value = ["dataAggiornamento"])
    ]
)
data class ClassificaTrofeo(
    @PrimaryKey
    val id: String,
    val trofeoId: String,
    val utenteId: String,
    val posizione: Int,
    val punteggio: Double,
    val valoreRaggiunto: Double, // Valore specifico raggiunto (km, tempo, etc.)
    val tempoCompletamento: Long? = null, // Tempo impiegato se applicabile
    val dataRaggiungimento: Date,
    val dataAggiornamento: Date,
    val isFinale: Boolean = false, // Se è la posizione finale
    val distaccoVincitore: Double = 0.0, // Distacco dal primo posto
    val distaccoPrecedente: Double = 0.0, // Distacco dal precedente
    val categoria: CategoriaTrofeo = CategoriaTrofeo.GENERALE,
    val livelloRaggiunto: LivelloTrofeo? = null, // Se ha raggiunto un livello
    val note: String? = null
)

