package com.asis.virtualcompanion.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.asis.virtualcompanion.data.database.entity.ThemePreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemePreferenceDao : BaseDao<ThemePreferenceEntity> {
    
    @Query("SELECT * FROM theme_preferences")
    fun getAllPreferences(): Flow<List<ThemePreferenceEntity>>
    
    @Query("SELECT * FROM theme_preferences WHERE preference_key = :key")
    suspend fun getPreferenceByKey(key: String): ThemePreferenceEntity?
    
    @Query("SELECT * FROM theme_preferences WHERE preference_key = :key")
    fun getPreferenceByKeyFlow(key: String): Flow<ThemePreferenceEntity?>
    
    @Query("DELETE FROM theme_preferences WHERE preference_key = :key")
    suspend fun deletePreferenceByKey(key: String): Int
    
    @Query("DELETE FROM theme_preferences")
    suspend fun deleteAllPreferences(): Int
    
    @Query("SELECT COUNT(*) FROM theme_preferences")
    suspend fun getThemePreferenceCount(): Int
}
