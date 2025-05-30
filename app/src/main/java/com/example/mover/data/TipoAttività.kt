package com.example.mover.data

/**
 * Enum che definisce tutti i tipi di attività supportati dall'app
 * Basato sulle specifiche di Strava
 */
enum class TipoAttività(
    val displayName: String,
    val icon: String,
    val categoria: CategoriaAttività,
    val supportaGPS: Boolean = true,
    val supportaFrequenzaCardiaca: Boolean = true,
    val supportaCadenza: Boolean = false,
    val supportaPotenza: Boolean = false
) {
    // Attività di corsa
    CORSA("Corsa", "🏃", CategoriaAttività.CORSA, supportaCadenza = true, supportaPotenza = true),
    CORSA_TAPIS_ROULANT("Corsa su Tapis Roulant", "🏃", CategoriaAttività.CORSA, supportaGPS = false, supportaCadenza = true),
    TRAIL_RUNNING("Trail Running", "🏔️", CategoriaAttività.CORSA, supportaCadenza = true),
    
    // Attività di ciclismo
    CICLISMO("Ciclismo", "🚴", CategoriaAttività.CICLISMO, supportaCadenza = true, supportaPotenza = true),
    CICLISMO_INDOOR("Ciclismo Indoor", "🚴", CategoriaAttività.CICLISMO, supportaGPS = false, supportaCadenza = true, supportaPotenza = true),
    MOUNTAIN_BIKE("Mountain Bike", "🚵", CategoriaAttività.CICLISMO, supportaCadenza = true, supportaPotenza = true),
    CICLISMO_STRADA("Ciclismo su Strada", "🚴", CategoriaAttività.CICLISMO, supportaCadenza = true, supportaPotenza = true),
    
    // Attività di camminata
    CAMMINATA("Camminata", "🚶", CategoriaAttività.CAMMINATA, supportaCadenza = true),
    ESCURSIONISMO("Escursionismo", "🥾", CategoriaAttività.CAMMINATA, supportaCadenza = true),
    TREKKING("Trekking", "🏔️", CategoriaAttività.CAMMINATA, supportaCadenza = true),
    
    // Attività acquatiche
    NUOTO("Nuoto", "🏊", CategoriaAttività.ACQUATICI, supportaGPS = false),
    NUOTO_ACQUE_LIBERE("Nuoto in Acque Libere", "🏊", CategoriaAttività.ACQUATICI),
    CANOA("Canoa", "🛶", CategoriaAttività.ACQUATICI),
    KAYAK("Kayak", "🛶", CategoriaAttività.ACQUATICI),
    SUP("Stand Up Paddle", "🏄", CategoriaAttività.ACQUATICI),
    VELA("Vela", "⛵", CategoriaAttività.ACQUATICI),
    KITESURF("Kitesurf", "🪁", CategoriaAttività.ACQUATICI),
    WINDSURF("Windsurf", "🏄", CategoriaAttività.ACQUATICI),
    
    // Sport invernali
    SCI_ALPINO("Sci Alpino", "⛷️", CategoriaAttività.SPORT_INVERNALI),
    SNOWBOARD("Snowboard", "🏂", CategoriaAttività.SPORT_INVERNALI),
    SCI_FONDO("Sci di Fondo", "🎿", CategoriaAttività.SPORT_INVERNALI, supportaCadenza = true),
    CIASPOLE("Ciaspole", "🥾", CategoriaAttività.SPORT_INVERNALI, supportaCadenza = true),
    
    // Fitness e palestra
    ALLENAMENTO_PESI("Allenamento con i Pesi", "🏋️", CategoriaAttività.FITNESS, supportaGPS = false),
    YOGA("Yoga", "🧘", CategoriaAttività.FITNESS, supportaGPS = false, supportaFrequenzaCardiaca = false),
    PILATES("Pilates", "🤸", CategoriaAttività.FITNESS, supportaGPS = false),
    CROSS_TRAINING("Cross Training", "🏃", CategoriaAttività.FITNESS, supportaGPS = false),
    CROSSFIT("CrossFit", "🏋️", CategoriaAttività.FITNESS, supportaGPS = false),
    ALLENAMENTO_FUNZIONALE("Allenamento Funzionale", "💪", CategoriaAttività.FITNESS, supportaGPS = false),
    STRETCHING("Stretching", "🤸", CategoriaAttività.FITNESS, supportaGPS = false, supportaFrequenzaCardiaca = false),
    
    // Altri sport
    ARRAMPICATA("Arrampicata", "🧗", CategoriaAttività.ALTRI_SPORT),
    ARRAMPICATA_INDOOR("Arrampicata Indoor", "🧗", CategoriaAttività.ALTRI_SPORT, supportaGPS = false),
    PATTINAGGIO("Pattinaggio", "⛸️", CategoriaAttività.ALTRI_SPORT),
    PATTINI_LINEA("Pattini in Linea", "🛼", CategoriaAttività.ALTRI_SPORT),
    GOLF("Golf", "⛳", CategoriaAttività.ALTRI_SPORT),
    TENNIS("Tennis", "🎾", CategoriaAttività.ALTRI_SPORT),
    CALCIO("Calcio", "⚽", CategoriaAttività.ALTRI_SPORT),
    BASKET("Basket", "🏀", CategoriaAttività.ALTRI_SPORT),
    PALLAVOLO("Pallavolo", "🏐", CategoriaAttività.ALTRI_SPORT),
    
    // Attività di trasporto
    AUTO("Auto", "🚗", CategoriaAttività.TRASPORTO),
    MOTO("Moto", "🏍️", CategoriaAttività.TRASPORTO),
    TRASPORTO_PUBBLICO("Trasporto Pubblico", "🚌", CategoriaAttività.TRASPORTO, supportaGPS = false, supportaFrequenzaCardiaca = false),
    
    // Attività sedentarie
    SEDUTO("Seduto", "🪑", CategoriaAttività.SEDENTARIO, supportaGPS = false, supportaFrequenzaCardiaca = false),
    LAVORO_SCRIVANIA("Lavoro alla Scrivania", "💻", CategoriaAttività.SEDENTARIO, supportaGPS = false, supportaFrequenzaCardiaca = false),
    
    // Attività generiche
    ALLENAMENTO_GENERICO("Allenamento Generico", "💪", CategoriaAttività.FITNESS),
    ATTIVITA_GENERICA("Attività Generica", "🏃", CategoriaAttività.ALTRI_SPORT);
    
    companion object {
        fun fromString(tipo: String): TipoAttività? {
            return values().find { it.name == tipo || it.displayName == tipo }
        }
        
        fun getByCategory(categoria: CategoriaAttività): List<TipoAttività> {
            return values().filter { it.categoria == categoria }
        }
        
        fun getGPSActivities(): List<TipoAttività> {
            return values().filter { it.supportaGPS }
        }
        
        fun getIndoorActivities(): List<TipoAttività> {
            return values().filter { !it.supportaGPS }
        }
    }
}

/**
 * Categorie principali di attività
 */
enum class CategoriaAttività(val displayName: String, val colore: String) {
    CORSA("Corsa", "#FF6B35"),
    CICLISMO("Ciclismo", "#4ECDC4"),
    CAMMINATA("Camminata", "#45B7D1"),
    ACQUATICI("Sport Acquatici", "#96CEB4"),
    SPORT_INVERNALI("Sport Invernali", "#FFEAA7"),
    FITNESS("Fitness", "#DDA0DD"),
    ALTRI_SPORT("Altri Sport", "#98D8C8"),
    TRASPORTO("Trasporto", "#F7DC6F"),
    SEDENTARIO("Sedentario", "#BDC3C7")
}