package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "seguaci",
    foreignKeys = [
        ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["seguaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Utente::class,
            parentColumns = ["id"],
            childColumns = ["seguitoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["seguaceId"]),
        Index(value = ["seguitoId"]),
        Index(value = ["seguaceId", "seguitoId"], unique = true)
    ]
)
data class Seguace(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val seguaceId: Long, // Chi segue
    val seguitoId: Long, // Chi viene seguito
    
    val dataInizio: Date = Date(),
    val stato: String = "ATTIVO", // ATTIVO, BLOCCATO, RIMOSSO
    val notificheAttive: Boolean = true,
    
    // Tipo di relazione
    val tipoRelazione: String = "NORMALE", // NORMALE, AMICO, ATLETA_PRO
    val reciproco: Boolean = false // Se entrambi si seguono
)