package com.example.kubhubsystem_gp13_dam.local

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // Para LocalDateTime
    @TypeConverter
    fun fromLocalDateTime(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(dateString: String?): LocalDateTime? {
        return dateString?.let { LocalDateTime.parse(it, formatter) }
    }

    // Para List<Int> (seccionesIds en DocenteEntity)
    @TypeConverter
    fun fromStringToListInt(value: String?): List<Int> {
        return if (value == null || value.isEmpty()) {
            emptyList()
        } else {
            try {
                value.split(",").map { it.trim().toInt() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromListIntToString(list: List<Int>?): String {
        return if (list == null || list.isEmpty()) {
            ""
        } else {
            list.joinToString(",")
        }
    }

    // Para List<String> (por si necesitas en el futuro)
    @TypeConverter
    fun fromStringToListString(value: String?): List<String> {
        return if (value == null || value.isEmpty()) {
            emptyList()
        } else {
            value.split(",").map { it.trim() }
        }
    }

    @TypeConverter
    fun fromListStringToString(list: List<String>?): String {
        return if (list == null || list.isEmpty()) {
            ""
        } else {
            list.joinToString(",")
        }
    }
}