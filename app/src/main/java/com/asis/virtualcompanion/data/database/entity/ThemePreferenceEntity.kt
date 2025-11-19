package com.asis.virtualcompanion.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "theme_preferences")
data class ThemePreferenceEntity(
    @PrimaryKey
    @ColumnInfo(name = "preference_key")
    val preferenceKey: String,
    
    @ColumnInfo(name = "preference_value")
    val preferenceValue: String,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)
