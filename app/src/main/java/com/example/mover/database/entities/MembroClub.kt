package com.example.mover.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "membri_club",
    foreignKeys = [
        ForeignKey(
            entity = Club::class,
            parentColumns = ["id"],
            childColumns = ["clubId"],
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
        Index(value = ["clubId"]),
        Index(value = ["utenteId"]),
        Index(value = ["clubId", "utenteId"], unique = true),
        Index(value = ["ruolo"]),
        Index(value = ["dataAdesione"])
    ]
)
data class MembroClub(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val clubId: Long,
    val utenteId: Long,
    
    val ruolo: String = "MEMBRO", // MEMBRO, MODERATORE, ADMIN, CREATOR
    val dataAdesione: Date = Date(),
    val dataUltimaAttivita: Date = Date(),
    
    // Stato membership
    val stato: String = "ATTIVO", // ATTIVO, SOSPESO, BANNATO, RICHIESTA_PENDING
    val notificheAttive: Boolean = true,
    
    // Statistiche nel club
    val attivitaNelClub: Int = 0,
    val distanzaNelClub: Float = 0f,
    val tempoNelClub: Long = 0,
    val dislivelloNelClub: Float = 0f,
    val kudosRicevuti: Int = 0,
    val kudosDati: Int = 0,
    
    // Permessi speciali
    val puoInvitare: Boolean = false,
    val puoModerare: Boolean = false,
    val puoCreareSfide: Boolean = false,
    
    // Note admin
    val noteAdmin: String? = null
)