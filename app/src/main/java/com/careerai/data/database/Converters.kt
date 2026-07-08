package com.careerai.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { Json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { Json.decodeFromString(it) }
    }
    
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { Json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return value?.let { Json.decodeFromString(it) }
    }
    
    @TypeConverter
    fun fromMap(value: Map<String, Any>?): String? {
        return value?.let { Json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toMap(value: String?): Map<String, Any>? {
        return value?.let { Json.decodeFromString(it) }
    }
}