package com.example.mover.data

import androidx.room.*
import com.example.mover.database.dao.*
import com.example.mover.database.entities.*

@Database(
    entities = [
        // Solo entità base senza foreign key complesse
        Attività::class,
        Segmento::class,
        Utente::class,
        Badge::class,
        Obiettivo::class,
        Trofeo::class
    ], 
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attivitàDao(): AttivitàDao
    abstract fun segmentoDao(): SegmentoDao
    abstract fun utenteDao(): UtenteDao
    abstract fun badgeDao(): BadgeDao
    abstract fun obiettiviDao(): ObiettiviDao
    abstract fun trofeiDao(): TrofeiDao
}