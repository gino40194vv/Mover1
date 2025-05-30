package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "trofei")
data class Trofeo(
    @PrimaryKey
    val id: String,
    val nome: String,
    val descrizione: String,
    val icona: String, // Nome del file icona
    val colore: String, // Colore del trofeo
    val categoria: CategoriaTrofeo,
    val tipo: TipoTrofeo,
    val livello: LivelloTrofeo = LivelloTrofeo.BRONZO,
    val punti: Int, // Punti assegnati
    val condizioni: String, // JSON con le condizioni per ottenerlo
    val isAttivo: Boolean = true,
    val rarità: RaritaTrofeo = RaritaTrofeo.COMUNE,
    val dataCreazione: Date = Date(),
    val ordinamento: Int = 0
)

@Entity(
    tableName = "trofei_utente",
    primaryKeys = ["trofeoId", "utenteId"],
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
        Index(value = ["dataOttenimento"])
    ]
)
data class TrofeoUtente(
    val trofeoId: String,
    val utenteId: String,
    val dataOttenimento: Date,
    val posizione: Int? = null, // Posizione nella classifica (se applicabile)
    val valore: Double? = null, // Valore ottenuto (tempo, distanza, etc.)
    val dettagli: String? = null, // JSON con dettagli aggiuntivi
    val condiviso: Boolean = false,
    val visibile: Boolean = true
)

// Entità per le classifiche dei trofei
@Entity(
    tableName = "classifiche_trofeo",
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
        Index(value = ["posizione"])
    ]
)
data class ClassificaTrofeo(
    @PrimaryKey
    val id: String,
    val trofeoId: String,
    val utenteId: String,
    val posizione: Int,
    val punteggio: Double,
    val tempo: Long? = null, // Tempo impiegato (se rilevante)
    val dataOttenimento: Date,
    val dettagliPrestazione: String? = null // JSON con dettagli
)

enum class CategoriaTrofeo {
    PRESTAZIONE,        // Trofei per prestazioni eccezionali
    COSTANZA,           // Trofei per costanza negli allenamenti
    DISTANZA,           // Trofei per distanze percorse
    VELOCITA,           // Trofei per velocità raggiunte
    DISLIVELLO,         // Trofei per dislivelli scalati
    SPORT_SPECIFICO,    // Trofei specifici per sport
    SOCIALE,            // Trofei per attività social
    ESPLORAZIONE,       // Trofei per esplorare nuovi luoghi
    SFIDE,              // Trofei per vincere sfide
    COMMUNITY,          // Trofei per contributi alla community
    STAGIONALE,         // Trofei stagionali
    ANNIVERSARIO        // Trofei per anniversari
}

enum class TipoTrofeo {
    PRESTAZIONE_SINGOLA,    // Per una singola prestazione eccezionale
    PRESTAZIONE_CUMULATIVA, // Per prestazioni cumulative nel tempo
    CLASSIFICA,             // Per posizioni in classifica
    PARTECIPAZIONE,         // Per partecipazione a eventi
    COMPLETAMENTO,          // Per completamento obiettivi
    PRIMO_POSTO,            // Per primi posti in competizioni
    RECORD_PERSONALE,       // Per record personali
    MILESTONE               // Per traguardi importanti
}

enum class LivelloTrofeo {
    BRONZO,     // Livello base
    ARGENTO,    // Livello intermedio
    ORO,        // Livello avanzato
    PLATINO,    // Livello esperto
    DIAMANTE    // Livello leggendario
}

enum class RaritaTrofeo {
    COMUNE,         // Facile da ottenere
    NON_COMUNE,     // Richiede impegno
    RARO,           // Difficile da ottenere
    EPICO,          // Molto difficile
    LEGGENDARIO,    // Estremamente raro
    MITICO          // Quasi impossibile
}

// Data class per le statistiche dei trofei
data class StatisticheTrofei(
    val totaliOttenuti: Int,
    val bronzo: Int,
    val argento: Int,
    val oro: Int,
    val platino: Int,
    val diamante: Int,
    val puntiTotali: Int,
    val ultimoOttenuto: Date?,
    val categoriaPreferita: CategoriaTrofeo?
)