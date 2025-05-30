package com.example.mover.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AnalisiPrestazioniDao {
    
    @Insert
    suspend fun inserisciFitnessFreshness(ctl: FitnessFreshness)
    
    @Query("SELECT * FROM fitness_freshness WHERE utenteId = :utenteId ORDER BY data DESC LIMIT 1")
    suspend fun getUltimaAnalisiFF(utenteId: String): FitnessFreshness?
}