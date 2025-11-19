package com.asis.virtualcompanion.domain.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.TTSConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

class TextToSpeechService(private val context: Context) : TextToSpeech.OnInitListener {
    
    private var textToSpeech: TextToSpeech? = null
    private val initializationDeferred = CompletableDeferred<Boolean>()
    private var initialized = false
    private var ttsConfig = TTSConfig()
    
    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }
    
    override fun onInit(status: Int) {
        val initializedSuccessfully = status == TextToSpeech.SUCCESS
        initialized = initializedSuccessfully
        initializationDeferred.complete(initializedSuccessfully)
        if (!initializedSuccessfully) {
            Log.e("TTS", "Failed to initialize TextToSpeech")
        }
    }
    
    suspend fun ensureInitialized(): Boolean {
        return if (initialized) {
            true
        } else {
            initializationDeferred.await()
        }
    }
    
    fun updateConfig(config: TTSConfig) {
        ttsConfig = config
        textToSpeech?.apply {
            setPitch(config.pitch)
            setSpeechRate(config.speed)
            val locale = Locale.forLanguageTag(config.language)
            if (isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                language = locale
            }
        }
    }
    
    fun selectVoiceByName(voiceName: String) {
        textToSpeech?.voices?.firstOrNull { it.name.contains(voiceName, ignoreCase = true) }?.let {
            textToSpeech?.voice = it
        }
    }
    
    suspend fun synthesizeToFile(
        text: String,
        outputDirectory: File,
        fileName: String = "tts_${UUID.randomUUID()}"
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!ensureInitialized()) {
                return@withContext Result.Error(IllegalStateException("TextToSpeech not initialized"))
            }
            
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }
            
            val outputFile = File(outputDirectory, "$fileName.wav")
            
            val params = HashMap<String, String>().apply {
                put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, fileName)
            }
            
            val resultCode = textToSpeech?.synthesizeToFile(text, params, outputFile, fileName)
            if (resultCode == TextToSpeech.SUCCESS) {
                Result.Success(outputFile)
            } else {
                Result.Error(IllegalStateException("TTS synthesis failed with code $resultCode"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
    
    fun listAvailableVoices(): List<Voice> {
        return textToSpeech?.voices?.toList() ?: emptyList()
    }
    
    fun stop() {
        textToSpeech?.stop()
    }
    
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        initialized = false
    }
}
