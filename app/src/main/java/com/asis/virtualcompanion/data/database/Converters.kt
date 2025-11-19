package com.asis.virtualcompanion.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Type converters for Room database
 */
class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(value, listType)
        }
    }
    
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return if (value == null) null else {
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson(value, mapType)
        }
    }
}