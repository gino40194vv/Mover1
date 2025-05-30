package com.example.mover.database.dao

import androidx.room.*
import com.example.mover.database.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SfideDao {
    @Query("SELECT * FROM sfide WHERE isAttiva = 1 ORDER BY dataInizio DESC")
    fun getSfideAttive(): Flow<List<Sfida>>
    
    @Query("SELECT * FROM sfide WHERE id = :sfidaId")
    suspend fun getSfidaById(sfidaId: String): Sfida?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSfida(sfida: Sfida)
    
    @Update
    suspend fun updateSfida(sfida: Sfida)
    
    @Delete
    suspend fun deleteSfida(sfida: Sfida)
}
