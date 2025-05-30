package com.example.mover.database

import androidx.room.TypeConverter
import java.util.Date
import com.example.mover.database.entities.*
import com.example.mover.data.*

class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Enum converters for Sfida
    @TypeConverter
    fun fromTipoSfida(value: TipoSfida?): String? = value?.name

    @TypeConverter
    fun toTipoSfida(value: String?): TipoSfida? = value?.let { TipoSfida.valueOf(it) }

    @TypeConverter
    fun fromCategoriaSfida(value: CategoriaSfida?): String? = value?.name

    @TypeConverter
    fun toCategoriaSfida(value: String?): CategoriaSfida? =
        value?.let { CategoriaSfida.valueOf(it) }

    @TypeConverter
    fun fromDifficoltaSfida(value: DifficoltaSfida?): String? = value?.name

    @TypeConverter
    fun toDifficoltaSfida(value: String?): DifficoltaSfida? =
        value?.let { DifficoltaSfida.valueOf(it) }

    // Enum converters for Badge
    @TypeConverter
    fun fromTipoBadge(value: TipoBadge?): String? = value?.name

    @TypeConverter
    fun toTipoBadge(value: String?): TipoBadge? = value?.let { TipoBadge.valueOf(it) }

    @TypeConverter
    fun fromRaritaBadge(value: RaritaBadge?): String? = value?.name

    @TypeConverter
    fun toRaritaBadge(value: String?): RaritaBadge? = value?.let { RaritaBadge.valueOf(it) }

    // Enum converters for Obiettivo
    @TypeConverter
    fun fromTipoObiettivo(value: TipoObiettivo?): String? = value?.name

    @TypeConverter
    fun toTipoObiettivo(value: String?): TipoObiettivo? = value?.let { TipoObiettivo.valueOf(it) }

    @TypeConverter
    fun fromPeriodoObiettivo(value: PeriodoObiettivo?): String? = value?.name

    @TypeConverter
    fun toPeriodoObiettivo(value: String?): PeriodoObiettivo? =
        value?.let { PeriodoObiettivo.valueOf(it) }

    @TypeConverter
    fun fromDifficoltaObiettivo(value: DifficoltaObiettivo?): String? = value?.name

    @TypeConverter
    fun toDifficoltaObiettivo(value: String?): DifficoltaObiettivo? =
        value?.let { DifficoltaObiettivo.valueOf(it) }

    @TypeConverter
    fun fromTipoNotificaObiettivo(value: TipoNotificaObiettivo?): String? = value?.name

    @TypeConverter
    fun toTipoNotificaObiettivo(value: String?): TipoNotificaObiettivo? =
        value?.let { TipoNotificaObiettivo.valueOf(it) }

    // Enum converters for Trofeo
    @TypeConverter
    fun fromTipoTrofeo(value: TipoTrofeo?): String? = value?.name

    @TypeConverter
    fun toTipoTrofeo(value: String?): TipoTrofeo? = value?.let { TipoTrofeo.valueOf(it) }

    @TypeConverter
    fun fromRaritaTrofeo(value: RaritaTrofeo?): String? = value?.name

    @TypeConverter
    fun toRaritaTrofeo(value: String?): RaritaTrofeo? = value?.let { RaritaTrofeo.valueOf(it) }

    @TypeConverter
    fun fromCategoriaTrofeo(value: CategoriaTrofeo?): String? = value?.name

    @TypeConverter
    fun toCategoriaTrofeo(value: String?): CategoriaTrofeo? =
        value?.let { CategoriaTrofeo.valueOf(it) }

    @TypeConverter
    fun fromLivelloTrofeo(value: LivelloTrofeo?): String? = value?.name

    @TypeConverter
    fun toLivelloTrofeo(value: String?): LivelloTrofeo? = value?.let { LivelloTrofeo.valueOf(it) }

    @TypeConverter
    fun fromCategoriaBadge(value: CategoriaBadge?): String? = value?.name

    @TypeConverter
    fun toCategoriaBadge(value: String?): CategoriaBadge? =
        value?.let { CategoriaBadge.valueOf(it) }

    // List converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    // CondizioniOttenimento converter
    @TypeConverter
    fun fromCondizioniOttenimento(value: CondizioniOttenimento?): String? {
        if (value == null) return null
        // Semplificata: converte solo i campi essenziali
        return "${value.tipo.name}|${value.valore}|${value.unitaMisura ?: ""}|${value.sportSpecifico ?: ""}|${value.periodoTempo ?: ""}"
    }

    @TypeConverter
    fun toCondizioniOttenimento(value: String?): CondizioniOttenimento? {
        if (value.isNullOrEmpty()) return null
        val parts = value.split("|")
        if (parts.size < 5) return null

        return CondizioniOttenimento(
            tipo = TipoCondizione.valueOf(parts[0]),
            valore = parts[1].toDoubleOrNull() ?: 0.0,
            unitaMisura = parts[2].takeIf { it.isNotEmpty() },
            sportSpecifico = parts[3].takeIf { it.isNotEmpty() },
            periodoTempo = parts[4].takeIf { it.isNotEmpty() }
        )
    }

    // TipoCondizione converter
    @TypeConverter
    fun fromTipoCondizione(value: TipoCondizione?): String? = value?.name

    @TypeConverter
    fun toTipoCondizione(value: String?): TipoCondizione? =
        value?.let { TipoCondizione.valueOf(it) }
}
