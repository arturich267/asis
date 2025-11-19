package com.asis.virtualcompanion.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.asis.virtualcompanion.data.model.VoiceInteractionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.voiceInteractionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "voice_interaction_preferences"
)

class VoiceInteractionPreferences(private val context: Context) {
    
    private object Keys {
        val VOICE_MODE = stringPreferencesKey("voice_mode")
        val USE_REAL_VOICE = booleanPreferencesKey("use_real_voice")
        val TTS_PITCH = floatPreferencesKey("tts_pitch")
        val TTS_SPEED = floatPreferencesKey("tts_speed")
        val TTS_LANGUAGE = stringPreferencesKey("tts_language")
    }
    
    val voiceMode: Flow<VoiceInteractionMode> = context.voiceInteractionDataStore.data
        .map { preferences ->
            val modeString = preferences[Keys.VOICE_MODE] ?: VoiceInteractionMode.RANDOM_MEME.name
            try {
                VoiceInteractionMode.valueOf(modeString)
            } catch (e: IllegalArgumentException) {
                VoiceInteractionMode.RANDOM_MEME
            }
        }
    
    val useRealVoice: Flow<Boolean> = context.voiceInteractionDataStore.data
        .map { preferences -> preferences[Keys.USE_REAL_VOICE] ?: false }
    
    val ttsPitch: Flow<Float> = context.voiceInteractionDataStore.data
        .map { preferences -> preferences[Keys.TTS_PITCH] ?: 1.0f }
    
    val ttsSpeed: Flow<Float> = context.voiceInteractionDataStore.data
        .map { preferences -> preferences[Keys.TTS_SPEED] ?: 1.0f }
    
    val ttsLanguage: Flow<String> = context.voiceInteractionDataStore.data
        .map { preferences -> preferences[Keys.TTS_LANGUAGE] ?: "en-US" }
    
    suspend fun setVoiceMode(mode: VoiceInteractionMode) {
        context.voiceInteractionDataStore.edit { preferences ->
            preferences[Keys.VOICE_MODE] = mode.name
        }
    }
    
    suspend fun setUseRealVoice(useRealVoice: Boolean) {
        context.voiceInteractionDataStore.edit { preferences ->
            preferences[Keys.USE_REAL_VOICE] = useRealVoice
        }
    }
    
    suspend fun setTTSPitch(pitch: Float) {
        context.voiceInteractionDataStore.edit { preferences ->
            preferences[Keys.TTS_PITCH] = pitch
        }
    }
    
    suspend fun setTTSSpeed(speed: Float) {
        context.voiceInteractionDataStore.edit { preferences ->
            preferences[Keys.TTS_SPEED] = speed
        }
    }
    
    suspend fun setTTSLanguage(language: String) {
        context.voiceInteractionDataStore.edit { preferences ->
            preferences[Keys.TTS_LANGUAGE] = language
        }
    }
}
