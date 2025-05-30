package com.example.mover.services

import com.example.mover.data.AppDatabase
import com.example.mover.database.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SfideService @Inject constructor(
    private val database: AppDatabase
) {
    private val sfideDao = database.sfideDao()
    private val badgeDao = database.badgeDao()
    private val obiettiviDao = database.obiettiviDao()
    private val trofeiDao = database.trofeiDao()
    private val attivitàDao = database.attivitàDao()

    // === GESTIONE SFIDE ===
    
    fun getSfideAttive(): Flow<List<Sfida>> = sfideDao.getSfideAttive()
    
    fun getSfidePerCategoria(categoria: CategoriaSfida): Flow<List<Sfida>> = 
        sfideDao.getSfidePerCategoria(categoria)
    
    fun getSfideUtente(utenteId: String): Flow<List<SfidaConProgresso>> = 
        sfideDao.getSfideUtente(utenteId)
    
    suspend fun iscrivitiASfida(sfidaId: String, utenteId: String): Boolean {
        return try {
            val sfida = sfideDao.getSfidaById(sfidaId) ?: return false
            val partecipazione = PartecipazioneSfida(
                id = UUID.randomUUID().toString(),
                sfidaId = sfidaId,
                utenteId = utenteId,
                dataIscrizione = Date(),
                progressoCorrente = 0.0,
                percentualeCompletamento = 0.0,
                isCompletata = false
            )
            sfideDao.insertPartecipazioneSfida(partecipazione)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun aggiornaProgressoSfida(
        partecipazioneId: String, 
        valoreAggiunto: Double, 
        attivitaId: String? = null
    ): Boolean {
        return try {
            val partecipazione = sfideDao.getPartecipazioneSfida(
                partecipazioneId.split("_")[0], 
                partecipazioneId.split("_")[1]
            ) ?: return false
            
            val progresso = ProgressoSfida(
                id = UUID.randomUUID().toString(),
                partecipazioneId = partecipazioneId,
                attivitaId = attivitaId,
                data = Date(),
                valoreAggiunto = valoreAggiunto,
                progressoTotale = partecipazione.progressoCorrente + valoreAggiunto
            )
            
            sfideDao.insertProgressoSfida(progresso)
            
            // Aggiorna la partecipazione
            val nuovoProgresso = partecipazione.progressoCorrente + valoreAggiunto
            val sfida = sfideDao.getSfidaById(partecipazione.sfidaId)!!
            val percentuale = (nuovoProgresso / sfida.obiettivo * 100).coerceAtMost(100.0)
            
            val partecipazioneAggiornata = partecipazione.copy(
                progressoCorrente = nuovoProgresso,
                percentualeCompletamento = percentuale,
                isCompletata = percentuale >= 100.0,
                dataCompletamento = if (percentuale >= 100.0) Date() else null
            )
            
            sfideDao.updatePartecipazioneSfida(partecipazioneAggiornata)
            
            // Verifica se ha completato la sfida per assegnare badge/trofei
            if (percentuale >= 100.0) {
                verificaCompletamentoSfida(partecipazione.utenteId, sfida)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun verificaCompletamentoSfida(utenteId: String, sfida: Sfida) {
        // Assegna badge se presente
        sfida.badgeId?.let { badgeId ->
            val badgeUtente = BadgeUtente(
                badgeId = badgeId,
                utenteId = utenteId,
                dataOttenimento = Date(),
                progressoOttenimento = "Completamento sfida: ${sfida.titolo}",
                condiviso = true
            )
            badgeDao.insertBadgeUtente(badgeUtente)
        }
        
        // Verifica trofei basati su completamento sfide
        verificaTrofeiSfide(utenteId)
    }
    
    // === GESTIONE BADGE ===
    
    fun getBadgesUtente(utenteId: String): Flow<List<BadgeConDettagli>> = 
        badgeDao.getBadgesUtente(utenteId)
    
    suspend fun assegnaBadge(badgeId: String, utenteId: String, dettagli: String? = null): Boolean {
        return try {
            val badge = badgeDao.getBadgeById(badgeId) ?: return false
            val badgeUtente = BadgeUtente(
                badgeId = badgeId,
                utenteId = utenteId,
                dataOttenimento = Date(),
                progressoOttenimento = dettagli,
                condiviso = true
            )
            badgeDao.insertBadgeUtente(badgeUtente)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun verificaBadgeAutomatici(utenteId: String, attivita: Attivita) {
        val badges = badgeDao.getBadgesNonOttenuti(utenteId)
        
        for (badge in badges) {
            if (badge.tipo == TipoBadge.AUTOMATICO) {
                val soddisfaCondizioni = verificaCondizioniBadge(badge, utenteId, attivita)
                if (soddisfaCondizioni) {
                    assegnaBadge(badge.id, utenteId, "Ottenuto automaticamente")
                }
            }
        }
    }
    
    private suspend fun verificaCondizioniBadge(
        badge: Badge, 
        utenteId: String, 
        attivita: Attivita
    ): Boolean {
        return when (badge.condizioni.tipo) {
            TipoCondizione.DISTANZA_SINGOLA -> {
                attivita.distanza >= badge.condizioni.valore
            }
            TipoCondizione.VELOCITA_MEDIA -> {
                attivita.velocitaMedia >= badge.condizioni.valore
            }
            TipoCondizione.DISLIVELLO_SINGOLO -> {
                attivita.dislivelloPositivo >= badge.condizioni.valore
            }
            TipoCondizione.TEMPO_SINGOLO -> {
                attivita.durata >= badge.condizioni.valore.toLong()
            }
            else -> false // Implementare altre condizioni
        }
    }
    
    // === GESTIONE OBIETTIVI ===
    
    fun getObiettiviAttivi(utenteId: String): Flow<List<Obiettivo>> = 
        obiettiviDao.getObiettiviAttivi(utenteId)
    
    fun getObiettiviConProgressi(utenteId: String): Flow<List<ObiettivoConProgresso>> = 
        obiettiviDao.getObiettiviConProgressi(utenteId)
    
    suspend fun creaObiettivo(obiettivo: Obiettivo): Boolean {
        return try {
            obiettiviDao.insertObiettivo(obiettivo)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun aggiornaProgressoObiettivo(
        obiettivoId: String, 
        valoreAggiunto: Double, 
        attivitaId: String? = null
    ): Boolean {
        return try {
            val obiettivo = obiettiviDao.getObiettivoById(obiettivoId) ?: return false
            
            val progresso = ProgressoObiettivo(
                id = UUID.randomUUID().toString(),
                obiettivoId = obiettivoId,
                attivitaId = attivitaId,
                data = Date(),
                valoreAggiunto = valoreAggiunto,
                progressoTotale = obiettivo.progressoCorrente + valoreAggiunto
            )
            
            obiettiviDao.insertProgressoObiettivo(progresso)
            
            // Aggiorna l'obiettivo
            val nuovoProgresso = obiettivo.progressoCorrente + valoreAggiunto
            val percentuale = (nuovoProgresso / obiettivo.valoreTarget * 100).coerceAtMost(100.0)
            
            val obiettivoAggiornato = obiettivo.copy(
                progressoCorrente = nuovoProgresso,
                percentualeCompletamento = percentuale,
                isCompletato = percentuale >= 100.0,
                dataCompletamento = if (percentuale >= 100.0) Date() else null,
                dataUltimaModifica = Date()
            )
            
            obiettiviDao.updateObiettivo(obiettivoAggiornato)
            
            // Verifica completamento per badge/trofei
            if (percentuale >= 100.0) {
                verificaCompletamentoObiettivo(obiettivo.utenteId, obiettivo)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun verificaCompletamentoObiettivo(utenteId: String, obiettivo: Obiettivo) {
        // Verifica trofei basati su completamento obiettivi
        verificaTrofeiObiettivi(utenteId)
    }
    
    // === GESTIONE TROFEI ===
    
    fun getTrofeiUtente(utenteId: String): Flow<List<TrofeoConDettagli>> = 
        trofeiDao.getTrofeiUtente(utenteId)
    
    suspend fun assegnaTrofeo(
        trofeoId: String, 
        utenteId: String, 
        posizione: Int? = null, 
        valore: Double? = null
    ): Boolean {
        return try {
            val trofeo = trofeiDao.getTrofeoById(trofeoId) ?: return false
            val trofeoUtente = TrofeoUtente(
                trofeoId = trofeoId,
                utenteId = utenteId,
                dataOttenimento = Date(),
                posizione = posizione,
                valore = valore,
                condiviso = true
            )
            trofeiDao.insertTrofeoUtente(trofeoUtente)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun verificaTrofeiSfide(utenteId: String) {
        val sfideCompletate = sfideDao.getNumeroSfideCompletate(utenteId)
        
        // Esempio: Trofeo per 10 sfide completate
        if (sfideCompletate >= 10) {
            // Assegna trofeo se non già ottenuto
            val trofeoId = "trofeo_10_sfide"
            val trofeoEsistente = trofeiDao.getTrofeoUtente(trofeoId, utenteId)
            if (trofeoEsistente == null) {
                assegnaTrofeo(trofeoId, utenteId)
            }
        }
    }
    
    private suspend fun verificaTrofeiObiettivi(utenteId: String) {
        val obiettiviCompletati = obiettiviDao.getNumeroObiettiviCompletati(utenteId)
        
        // Esempio: Trofeo per 5 obiettivi completati
        if (obiettiviCompletati >= 5) {
            val trofeoId = "trofeo_5_obiettivi"
            val trofeoEsistente = trofeiDao.getTrofeoUtente(trofeoId, utenteId)
            if (trofeoEsistente == null) {
                assegnaTrofeo(trofeoId, utenteId)
            }
        }
    }
    
    // === STATISTICHE GENERALI ===
    
    suspend fun getStatisticheSfide(utenteId: String): StatisticheSfide {
        return StatisticheSfide(
            sfidePartecipate = sfideDao.getNumeroSfidePartecipate(utenteId),
            sfideCompletate = sfideDao.getNumeroSfideCompletate(utenteId),
            percentualeMediaCompletamento = sfideDao.getPercentualeMediaCompletamento(utenteId) ?: 0.0,
            categoriePreferite = sfideDao.getCategoriePreferite(utenteId)
        )
    }
    
    suspend fun getStatisticheBadge(utenteId: String): StatisticheBadge {
        return StatisticheBadge(
            totaleBadges = badgeDao.getNumeroBadgesUtente(utenteId),
            puntiTotali = badgeDao.getPuntiTotaliBadges(utenteId) ?: 0,
            ultimoBadge = badgeDao.getUltimoBadgeOttenuto(utenteId),
            badgesPiuRari = badgeDao.getBadgesPiuRari(utenteId),
            categoriePreferite = badgeDao.getBadgesPerCategoriaUtente(utenteId)
        )
    }
    
    suspend fun getStatisticheTrofei(utenteId: String): StatisticheTrofeiUtente? {
        return trofeiDao.getStatisticheTrofeiUtente(utenteId)
    }
    
    suspend fun getStatisticheObiettivi(utenteId: String): StatisticheObiettivi {
        return StatisticheObiettivi(
            obiettiviAttivi = obiettiviDao.getNumeroObiettiviAttivi(utenteId),
            obiettiviCompletati = obiettiviDao.getNumeroObiettiviCompletati(utenteId),
            percentualeMediaCompletamento = obiettiviDao.getPercentualeMediaCompletamento(utenteId) ?: 0.0,
            tempoMedioCompletamento = obiettiviDao.getTempoMedioCompletamento(utenteId) ?: 0.0,
            tipiPreferiti = obiettiviDao.getTipiObiettiviPreferiti(utenteId)
        )
    }
}

// Data classes per le statistiche
data class StatisticheSfide(
    val sfidePartecipate: Int,
    val sfideCompletate: Int,
    val percentualeMediaCompletamento: Double,
    val categoriePreferite: List<CategoriaConConteggio>
)

data class StatisticheBadge(
    val totaleBadges: Int,
    val puntiTotali: Int,
    val ultimoBadge: BadgeConDettagli?,
    val badgesPiuRari: List<BadgeConDettagli>,
    val categoriePreferite: List<CategoriaBadgeConConteggio>
)

data class StatisticheObiettivi(
    val obiettiviAttivi: Int,
    val obiettiviCompletati: Int,
    val percentualeMediaCompletamento: Double,
    val tempoMedioCompletamento: Double,
    val tipiPreferiti: List<TipoObiettivoConConteggio>
)