package com.example.mover.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.mover.database.entities.*
import com.example.mover.database.dao.*
import com.example.mover.data.Converters
import com.example.mover.data.AnalisiPrestazioniDao
// Explicit imports for entities in data package
import com.example.mover.data.Attività
import com.example.mover.data.PuntoGPS
import com.example.mover.data.SegmentoAttività
import com.example.mover.data.Segmento
import com.example.mover.data.TentativoSegmento
import com.example.mover.data.ClassificaSegmento
import com.example.mover.data.FitnessFreshness
import com.example.mover.data.AnalisiZone
import com.example.mover.data.AnalisiPassoGara
import com.example.mover.data.MetrichePerformance

@Database(
    entities = [
        Attività::class, 
        PuntoGPS::class, 
        SegmentoAttività::class,
        Segmento::class,
        TentativoSegmento::class,
        ClassificaSegmento::class,
        FitnessFreshness::class,
        AnalisiZone::class,
        AnalisiPassoGara::class,
        MetrichePerformance::class,
        // Entità Social Point 3
        Utente::class,
        Seguace::class,
        Kudos::class,
        Commento::class,
        Club::class,
        MembroClub::class,
        EventoVirtuale::class,
        PartecipazioneEvento::class,
        FeedAttivita::class,
        ClassificaClub::class,
        // Entità Sfide e Competizioni Point 4
        Sfida::class,
        PartecipazioneSfida::class,
        ProgressoSfida::class,
        Badge::class,
        BadgeUtente::class,
        Obiettivo::class,
        ProgressoObiettivo::class,
        NotificaObiettivo::class,
        Trofeo::class,
        TrofeoUtente::class,
        ClassificaTrofeo::class
    ], 
    version = 17,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attivitàDao(): AttivitàDao
    abstract fun segmentoDao(): SegmentoDao
    abstract fun analisiPrestazioniDao(): AnalisiPrestazioniDao
    
    // DAO Social Point 3
    abstract fun utenteDao(): UtenteDao
    abstract fun socialDao(): SocialDao
    abstract fun clubDao(): ClubDao
    abstract fun eventoDao(): EventoDao
    
    // DAO Sfide e Competizioni Point 4
    abstract fun sfideDao(): SfideDao
    abstract fun badgeDao(): BadgeDao
    abstract fun obiettiviDao(): ObiettiviDao
    abstract fun trofeiDao(): TrofeiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
