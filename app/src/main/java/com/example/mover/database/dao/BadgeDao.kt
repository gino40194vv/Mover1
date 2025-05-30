package com.example.mover.database.dao

import androidx.room.*
import com.example.mover.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges WHERE isAttivo = 1 ORDER BY categoria, ordinamento")
    fun getBadgesAttivi(): Flow<List<Badge>>
    
    @Query("SELECT * FROM badges WHERE id = :badgeId")
    suspend fun getBadgeById(badgeId: String): Badge?
    
    @Query("SELECT b.* FROM badges b INNER JOIN badge_utente bu ON b.id = bu.badgeId WHERE bu.utenteId = :utenteId AND bu.visibile = 1 ORDER BY bu.dataOttenimento DESC")
    fun getBadgesUtente(utenteId: String): Flow<List<Badge>>
    
    @Query("SELECT * FROM badge_utente WHERE utenteId = :utenteId AND badgeId = :badgeId")
    suspend fun getBadgeUtente(utenteId: String, badgeId: String): BadgeUtente?
    
    @Query("SELECT * FROM badges WHERE categoria = :categoria AND isAttivo = 1 ORDER BY ordinamento")
    fun getBadgesByCategoria(categoria: CategoriaBadge): Flow<List<Badge>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badge)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadgeUtente(badgeUtente: BadgeUtente)
    
    @Update
    suspend fun updateBadge(badge: Badge)
    
    @Update
    suspend fun updateBadgeUtente(badgeUtente: BadgeUtente)
    
    @Delete
    suspend fun deleteBadge(badge: Badge)
    
    @Delete
    suspend fun deleteBadgeUtente(badgeUtente: BadgeUtente)
    
    @Query("SELECT COUNT(*) FROM badge_utente WHERE utenteId = :utenteId")
    suspend fun getCountBadgesUtente(utenteId: String): Int
    
    @Query("SELECT SUM(b.punti) FROM badges b INNER JOIN badge_utente bu ON b.id = bu.badgeId WHERE bu.utenteId = :utenteId")
    suspend fun getTotalPuntiBadges(utenteId: String): Int?
}