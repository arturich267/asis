package com.asis.virtualcompanion.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.di.AppModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository for clearing all user data from the application
 */
class DataClearRepository(private val context: Context) {

    private val dataStore: DataStore<Preferences> by lazy {
        preferencesDataStore(name = "data_clear_preferences")(context)
    }

    /**
     * Deletes all data from Room database tables
     */
    suspend fun deleteAll(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val database = AppModule.provideDatabase(context)
                
                // Clear all tables in the database
                database.clearAllTables()
                
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    /**
     * Deletes archive files and temporary files recursively
     */
    suspend fun deleteRecursively(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Clear cache directory
                context.cacheDir.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        file.deleteRecursively()
                    } else {
                        file.delete()
                    }
                }

                // Clear temporary voice files
                val tempVoiceDir = File(context.cacheDir, "voice_temp")
                if (tempVoiceDir.exists()) {
                    tempVoiceDir.deleteRecursively()
                }

                // Clear any other app-specific directories
                val filesDir = context.filesDir
                filesDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("temp_") || file.name.startsWith("cache_")) {
                        if (file.isDirectory) {
                            file.deleteRecursively()
                        } else {
                            file.delete()
                        }
                    }
                }

                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    /**
     * Clears all DataStore preferences
     */
    suspend fun clearPreferences(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                context.dataStore.edit { preferences ->
                    preferences.clear()
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    /**
     * Gets the current data clear status
     */
    suspend fun getDataClearStatus(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val database = AppModule.provideDatabase(context)
                
                // Check if database is empty by querying each table
                val messageCount = database.messageDao().getMessageCount()
                val phraseStatCount = database.phraseStatDao().getPhraseStatCount()
                val voiceMetaCount = database.voiceMetaDao().getVoiceMetaCount()
                val chatMessageCount = database.chatMessageDao().getChatMessageCount()
                val themePreferenceCount = database.themePreferenceDao().getThemePreferenceCount()
                
                // Check if cache is empty
                val cacheEmpty = context.cacheDir.listFiles()?.isEmpty() ?: true
                
                // Consider data cleared if all tables are empty and cache is empty
                messageCount == 0 && phraseStatCount == 0 && voiceMetaCount == 0 && 
                chatMessageCount == 0 && themePreferenceCount == 0 && cacheEmpty
            } catch (e: Exception) {
                false
            }
        }
    }
}