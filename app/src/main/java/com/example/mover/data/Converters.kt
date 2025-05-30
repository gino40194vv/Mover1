package com.example.mover.data

import androidx.room.TypeConverter
import java.util.Date
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.mover.database.entities.MilestoneSfida
import com.example.mover.database.entities.CondizioniOttenimento
import com.example.mover.database.entities.ValoreStorico

class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // List converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun fromDoubleList(value: List<Double>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDoubleList(value: String?): List<Double> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<Double>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromMapStringAny(value: Map<String, Any>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMapStringAny(value: String?): Map<String, Any> {
        if (value.isNullOrEmpty()) return emptyMap()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    // Complex object converters
    @TypeConverter
    fun fromMilestoneSfidaList(value: List<MilestoneSfida>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMilestoneSfidaList(value: String?): List<MilestoneSfida> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<MilestoneSfida>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromCondizioniOttenimento(value: CondizioniOttenimento?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCondizioniOttenimento(value: String?): CondizioniOttenimento? {
        if (value.isNullOrEmpty()) return null
        return gson.fromJson(value, CondizioniOttenimento::class.java)
    }

    @TypeConverter
    fun fromValoreStoricoList(value: List<ValoreStorico>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toValoreStoricoList(value: String?): List<ValoreStorico> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<ValoreStorico>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
