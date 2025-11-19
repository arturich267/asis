package com.asis.virtualcompanion.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val THEME_PREFERENCES_NAME = "theme_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = THEME_PREFERENCES_NAME)

private val THEME_ID_KEY = stringPreferencesKey("theme_id")
private val ARCHIVE_URI_KEY = stringPreferencesKey("archive_uri")
private val USE_REAL_VOICE_KEY = booleanPreferencesKey("use_real_voice")
private val PROCESS_AUDIO_OFFLINE_KEY = booleanPreferencesKey("process_audio_offline")

class ThemePreferences(private val context: Context) {

    val themeIdFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_ID_KEY] ?: "default"
        }

    val archiveUriFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ARCHIVE_URI_KEY]
        }

    val useRealVoiceFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[USE_REAL_VOICE_KEY] ?: false
        }

    val processAudioOfflineFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PROCESS_AUDIO_OFFLINE_KEY] ?: false
        }

    suspend fun setThemeId(themeId: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_ID_KEY] = themeId
        }
    }

    suspend fun setArchiveUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[ARCHIVE_URI_KEY] = uri
            } else {
                preferences.remove(ARCHIVE_URI_KEY)
            }
        }
    }

    suspend fun setUseRealVoice(useRealVoice: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_REAL_VOICE_KEY] = useRealVoice
        }
    }

    suspend fun setProcessAudioOffline(processOffline: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PROCESS_AUDIO_OFFLINE_KEY] = processOffline
        }
    }
}
