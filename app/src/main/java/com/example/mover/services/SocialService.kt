package com.example.mover.services

import com.example.mover.database.AppDatabase
import com.example.mover.database.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.*

class SocialService(private val database: AppDatabase) {
    
    private val socialDao = database.socialDao()
    private val utenteDao = database.utenteDao()
    private val clubDao = database.clubDao()
    private val eventoDao = database.eventoDao()
    
    // === GESTIONE SEGUACI ===
    suspend fun seguiUtente(seguaceId: Long, seguitoId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            if (seguaceId == seguitoId) return@withContext false
            
            val esistente = socialDao.getSeguace(seguaceId, seguitoId)
            if (esistente != null) return@withContext false
            
            val seguace = Seguace(
                seguaceId = seguaceId,
                seguitoId = seguitoId,
                dataInizio = Date(),
                stato = "ATTIVO"
            )
            
            socialDao.insertSeguace(seguace)
            
            // Verifica se è reciproco
            val reciproco = socialDao.getSeguace(seguitoId, seguaceId)
            if (reciproco != null) {
                // Aggiorna entrambi come reciproci
                socialDao.insertSeguace(seguace.copy(reciproco = true))
                socialDao.insertSeguace(reciproco.copy(reciproco = true))
            }
            
            // Crea feed item per i seguaci del nuovo seguito
            creaFeedSeguiUtente(seguaceId, seguitoId)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun smettiDiSeguire(seguaceId: Long, seguitoId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            socialDao.removeSeguace(seguaceId, seguitoId)
            
            // Rimuovi reciprocità se esisteva
            val altroSeguace = socialDao.getSeguace(seguitoId, seguaceId)
            if (altroSeguace?.reciproco == true) {
                socialDao.insertSeguace(altroSeguace.copy(reciproco = false))
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // === GESTIONE KUDOS ===
    suspend fun daKudos(utenteId: Long, attivitaId: Long, tipo: String = "NORMALE"): Boolean = withContext(Dispatchers.IO) {
        try {
            val esistente = socialDao.haKudos(utenteId, attivitaId)
            if (esistente) return@withContext false
            
            val kudos = Kudos(
                utenteId = utenteId,
                attivitaId = attivitaId,
                tipo = tipo,
                dataKudos = Date()
            )
            
            socialDao.insertKudos(kudos)
            
            // Aggiorna statistiche utente proprietario attività
            val attivita = database.attivitaDao().getAttivitaById(attivitaId)
            attivita?.let {
                utenteDao.incrementaKudos(it.utenteId)
                
                // Crea feed item per kudos ricevuto
                creaFeedKudosRicevuto(it.utenteId, attivitaId, utenteId)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun rimuoviKudos(utenteId: Long, attivitaId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            socialDao.removeKudos(utenteId, attivitaId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // === GESTIONE COMMENTI ===
    suspend fun aggiungiCommento(
        utenteId: Long, 
        attivitaId: Long, 
        testo: String, 
        commentoPadreId: Long? = null
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val commento = Commento(
                utenteId = utenteId,
                attivitaId = attivitaId,
                commentoPadreId = commentoPadreId,
                testo = testo,
                dataCommento = Date()
            )
            
            val commentoId = socialDao.insertCommento(commento)
            
            // Crea feed item per commento ricevuto
            val attivita = database.attivitaDao().getAttivitaById(attivitaId)
            attivita?.let {
                if (it.utenteId != utenteId) { // Non notificare se commenta la propria attività
                    creaFeedCommentoRicevuto(it.utenteId, attivitaId, utenteId)
                }
            }
            
            commentoId
        } catch (e: Exception) {
            null
        }
    }
    
    // === GESTIONE FEED ===
    suspend fun generaFeedUtente(utenteId: Long, limit: Int = 20, offset: Int = 0): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            // Pulisci feed scaduto
            socialDao.pulisciFeedScaduto()
            
            // Ottieni feed personalizzato
            val feed = socialDao.getFeedUtente(utenteId, System.currentTimeMillis(), limit, offset)
            
            // Calcola score dinamico per ranking
            feed.forEach { item ->
                val feedId = item["id"] as Long
                val dataCreazione = item["dataCreazione"] as Long
                val kudos = item["likes"] as? Int ?: 0
                val commenti = item["commenti"] as? Int ?: 0
                
                val score = calcolaScoreFeed(dataCreazione, kudos, commenti)
                // Aggiorna score nel database se necessario
            }
            
            feed
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun creaFeedSeguiUtente(seguaceId: Long, seguitoId: Long) {
        val seguaci = utenteDao.getSeguaci(seguaceId)
        // Implementa logica per notificare i seguaci
    }
    
    private suspend fun creaFeedKudosRicevuto(proprietarioId: Long, attivitaId: Long, kudosUserId: Long) {
        val feedItem = FeedAttivita(
            utenteId = proprietarioId,
            attivitaId = attivitaId,
            tipo = "KUDOS_RICEVUTI",
            priorita = 3,
            dataCreazione = Date(),
            metadati = """{"kudosUserId": $kudosUserId}"""
        )
        socialDao.insertFeedAttivita(feedItem)
    }
    
    private suspend fun creaFeedCommentoRicevuto(proprietarioId: Long, attivitaId: Long, commentoUserId: Long) {
        val feedItem = FeedAttivita(
            utenteId = proprietarioId,
            attivitaId = attivitaId,
            tipo = "COMMENTO_RICEVUTO",
            priorita = 4,
            dataCreazione = Date(),
            metadati = """{"commentoUserId": $commentoUserId}"""
        )
        socialDao.insertFeedAttivita(feedItem)
    }
    
    private fun calcolaScoreFeed(dataCreazione: Long, kudos: Int, commenti: Int): Float {
        val now = System.currentTimeMillis()
        val orePassate = (now - dataCreazione) / (1000 * 60 * 60).toFloat()
        
        // Algoritmo simile a Reddit/Hacker News
        val engagement = kudos + (commenti * 2)
        val decadimento = 1f / (1f + orePassate / 24f) // Decade nel tempo
        
        return engagement * decadimento
    }
    
    // === GESTIONE CLUB ===
    suspend fun creaClub(
        nome: String,
        descrizione: String,
        creatorId: Long,
        pubblico: Boolean = true
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val club = Club(
                nome = nome,
                descrizione = descrizione,
                creatorId = creatorId,
                pubblico = pubblico,
                dataCreazione = Date()
            )
            
            val clubId = clubDao.insertClub(club)
            
            // Aggiungi il creatore come membro
            val membroCreator = MembroClub(
                clubId = clubId,
                utenteId = creatorId,
                ruolo = "CREATOR",
                dataAdesione = Date()
            )
            clubDao.insertMembroClub(membroCreator)
            
            clubId
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun iscrivitiAlClub(clubId: Long, utenteId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val club = clubDao.getClubById(clubId) ?: return@withContext false
            val giaMembro = clubDao.isMembro(clubId, utenteId)
            
            if (giaMembro) return@withContext false
            
            val membro = MembroClub(
                clubId = clubId,
                utenteId = utenteId,
                ruolo = "MEMBRO",
                dataAdesione = Date(),
                stato = if (club.richiedeApprovazione) "RICHIESTA_PENDING" else "ATTIVO"
            )
            
            clubDao.insertMembroClub(membro)
            
            if (!club.richiedeApprovazione) {
                clubDao.incrementaMembri(clubId)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // === GESTIONE EVENTI ===
    suspend fun creaEvento(
        nome: String,
        descrizione: String,
        creatorId: Long,
        dataInizio: Date,
        dataFine: Date,
        tipo: String,
        clubId: Long? = null
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val evento = EventoVirtuale(
                nome = nome,
                descrizione = descrizione,
                creatorId = creatorId,
                clubId = clubId,
                dataInizio = dataInizio,
                dataFine = dataFine,
                tipo = tipo,
                sportConsentiti = """["CORSA", "CICLISMO", "CAMMINATA"]""",
                dataCreazione = Date()
            )
            
            eventoDao.insertEvento(evento)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun iscrivitiEvento(eventoId: Long, utenteId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val evento = eventoDao.getEventoById(eventoId) ?: return@withContext false
            val giaIscritto = eventoDao.isPartecipante(eventoId, utenteId)
            
            if (giaIscritto) return@withContext false
            
            val partecipazione = PartecipazioneEvento(
                eventoId = eventoId,
                utenteId = utenteId,
                dataIscrizione = Date(),
                stato = "ISCRITTO"
            )
            
            eventoDao.insertPartecipazione(partecipazione)
            eventoDao.incrementaPartecipanti(eventoId)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // === CALCOLO CLASSIFICHE ===
    suspend fun calcolaClassificheClub(clubId: Long, periodo: String, anno: Int, mese: Int? = null, settimana: Int? = null) = withContext(Dispatchers.IO) {
        try {
            val membri = clubDao.getMembriClub(clubId)
            val tipi = listOf("DISTANZA", "TEMPO", "DISLIVELLO", "ATTIVITA")
            
            tipi.forEach { tipo ->
                membri.forEachIndexed { index, membro ->
                    val utenteId = membro["utenteId"] as Long
                    val valore = when (tipo) {
                        "DISTANZA" -> membro["distanzaNelClub"] as? Float ?: 0f
                        "TEMPO" -> (membro["tempoNelClub"] as? Long ?: 0L).toFloat()
                        "DISLIVELLO" -> membro["dislivelloNelClub"] as? Float ?: 0f
                        "ATTIVITA" -> (membro["attivitaNelClub"] as? Int ?: 0).toFloat()
                        else -> 0f
                    }
                    
                    val classifica = ClassificaClub(
                        clubId = clubId,
                        utenteId = utenteId,
                        periodo = periodo,
                        tipo = tipo,
                        anno = anno,
                        mese = mese,
                        settimana = settimana,
                        valore = valore,
                        posizione = index + 1,
                        totalPartecipanti = membri.size,
                        dataAggiornamento = Date()
                    )
                    
                    clubDao.insertClassificaClub(classifica)
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }
}