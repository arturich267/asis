package com.asis.virtualcompanion.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.*
import com.asis.virtualcompanion.data.preferences.VoiceInteractionPreferences
import com.asis.virtualcompanion.di.AppModule
import com.asis.virtualcompanion.domain.MemeGenerator
import com.asis.virtualcompanion.domain.service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

data class VoiceUiState(
    val recordingState: AudioRecordingState = AudioRecordingState(),
    val playbackInfo: AudioPlaybackInfo = AudioPlaybackInfo(),
    val interactionMode: VoiceInteractionMode = VoiceInteractionMode.RANDOM_MEME,
    val useRealVoiceSnippets: Boolean = false,
    val isProcessing: Boolean = false,
    val processingMessage: String = "",
    val errorMessage: String? = null,
    val lastMemeResponse: MemeResponse? = null
)

class VoiceViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _uiState = MutableLiveData<VoiceUiState>()
    val uiState: LiveData<VoiceUiState> = _uiState
    
    private val audioRecordingService = AudioRecordingService()
    private val emotionAnalysisService = EmotionAnalysisService()
    private val ttsService = TextToSpeechService(application)
    private val ffmpegService = FFmpegAudioService()
    private val mediaPlayerController = MediaPlayerController(application)
    private val memeGenerator = AppModule.provideMemeGenerator(application)
    private val voiceRepository = AppModule.provideVoiceRepository(application)
    private val preferences = VoiceInteractionPreferences(application)
    
    private val tempAudioDir = File(application.cacheDir, "voice_temp").apply { mkdirs() }
    private var recordingFile: File? = null
    private var amplitudeJob: Job? = null
    private var playbackJob: Job? = null
    
    init {
        _uiState.value = VoiceUiState()
        loadPreferences()
        observePlaybackState()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            preferences.voiceMode.collect { mode ->
                updateState { copy(interactionMode = mode) }
            }
        }
        viewModelScope.launch {
            preferences.useRealVoice.collect { useRealVoice ->
                updateState { copy(useRealVoiceSnippets = useRealVoice) }
            }
        }
        viewModelScope.launch {
            val ttsPitch = preferences.ttsPitch.first()
            val ttsSpeed = preferences.ttsSpeed.first()
            val ttsLanguage = preferences.ttsLanguage.first()
            ttsService.updateConfig(TTSConfig(ttsPitch, ttsSpeed, ttsLanguage))
        }
    }
    
    private fun observePlaybackState() {
        playbackJob = viewModelScope.launch {
            mediaPlayerController.playbackState().collect { playbackInfo ->
                val state = when {
                    playbackInfo.isPlaying -> AudioPlaybackState.PLAYING
                    playbackInfo.error != null -> AudioPlaybackState.ERROR
                    playbackInfo.currentPosition >= playbackInfo.duration && playbackInfo.duration > 0 -> AudioPlaybackState.COMPLETED
                    playbackInfo.duration > 0 -> AudioPlaybackState.PAUSED
                    else -> AudioPlaybackState.IDLE
                }
                
                updateState {
                    copy(
                        playbackInfo = AudioPlaybackInfo(
                            state = state,
                            currentPositionMs = playbackInfo.currentPosition,
                            durationMs = playbackInfo.duration,
                            error = playbackInfo.error
                        )
                    )
                }
            }
        }
    }
    
    fun startRecording() {
        viewModelScope.launch {
            try {
                recordingFile = File(tempAudioDir, "recording_${System.currentTimeMillis()}.pcm")
                val result = withContext(Dispatchers.IO) {
                    audioRecordingService.startRecording(recordingFile!!)
                }
                
                if (result is Result.Success) {
                    updateState {
                        copy(
                            recordingState = AudioRecordingState(isRecording = true),
                            errorMessage = null
                        )
                    }
                    startAmplitudeMonitoring()
                } else if (result is Result.Error) {
                    updateState {
                        copy(errorMessage = "Failed to start recording: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                updateState {
                    copy(errorMessage = "Failed to start recording: ${e.message}")
                }
            }
        }
    }
    
    private fun startAmplitudeMonitoring() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            while (audioRecordingService.isCurrentlyRecording()) {
                val amplitude = withContext(Dispatchers.IO) {
                    audioRecordingService.getMaxAmplitude()
                }
                val currentState = _uiState.value?.recordingState ?: AudioRecordingState()
                updateState {
                    copy(
                        recordingState = currentState.copy(
                            amplitudePercent = amplitude,
                            durationMs = currentState.durationMs + 100
                        )
                    )
                }
                delay(100)
            }
        }
    }
    
    fun stopRecording() {
        viewModelScope.launch {
            amplitudeJob?.cancel()
            
            val result = withContext(Dispatchers.IO) {
                audioRecordingService.stopRecording()
            }
            
            updateState {
                copy(recordingState = AudioRecordingState(isRecording = false))
            }
            
            if (result is Result.Success) {
                processRecordedAudio()
            } else if (result is Result.Error) {
                updateState {
                    copy(errorMessage = "Failed to stop recording: ${result.exception.message}")
                }
            }
        }
    }
    
    private fun processRecordedAudio() {
        viewModelScope.launch {
            updateState {
                copy(
                    isProcessing = true,
                    processingMessage = "Converting audio...",
                    errorMessage = null
                )
            }
            
            val pcmFile = recordingFile ?: run {
                updateState {
                    copy(
                        isProcessing = false,
                        errorMessage = "No recording file found"
                    )
                }
                return@launch
            }
            
            val wavFile = File(tempAudioDir, "recording_${System.currentTimeMillis()}.wav")
            val convertResult = audioRecordingService.convertPcmToWav(pcmFile, wavFile)
            
            if (convertResult is Result.Error) {
                updateState {
                    copy(
                        isProcessing = false,
                        errorMessage = "Failed to convert audio: ${convertResult.exception.message}"
                    )
                }
                return@launch
            }
            
            analyzeEmotionAndGenerateResponse(wavFile)
        }
    }
    
    private fun analyzeEmotionAndGenerateResponse(audioFile: File) {
        viewModelScope.launch {
            updateState {
                copy(processingMessage = "Analyzing emotion...")
            }
            
            val emotionResult = emotionAnalysisService.analyzeAudio(audioFile)
            
            val emotion = if (emotionResult is Result.Success) {
                emotionResult.data.emotion
            } else {
                "neutral"
            }
            
            updateState {
                copy(processingMessage = "Generating meme response...")
            }
            
            val mode = _uiState.value?.interactionMode ?: VoiceInteractionMode.RANDOM_MEME
            val memeMode = when (mode) {
                VoiceInteractionMode.RANDOM_MEME -> MemeMode.RANDOM
                VoiceInteractionMode.EMOTION_RESPONSE -> MemeMode.RESPOND_TO_EMOTION
            }
            
            val config = MemeGenerationConfig(
                mode = memeMode,
                includeAudio = true,
                detectedEmotion = emotion
            )
            
            val memeResult = memeGenerator.generateMemeResponse(config)
            
            if (memeResult is Result.Error) {
                updateState {
                    copy(
                        isProcessing = false,
                        errorMessage = "Failed to generate response: ${memeResult.exception.message}"
                    )
                }
                // Delete user audio file if voice retention is disabled
                if (!getVoiceRetentionSetting()) {
                    audioFile.delete()
                }
                return@launch
            }
            
            val memeResponse = (memeResult as Result.Success).data
            
            updateState {
                copy(
                    processingMessage = "Synthesizing speech...",
                    lastMemeResponse = memeResponse
                )
            }
            
            synthesizeAndPlay(memeResponse, audioFile)
        }
    }
    
    private fun synthesizeAndPlay(memeResponse: MemeResponse, userAudioFile: File? = null) {
        viewModelScope.launch {
            val ttsResult = ttsService.synthesizeToFile(
                memeResponse.text,
                tempAudioDir,
                "tts_${System.currentTimeMillis()}"
            )
            
            if (ttsResult is Result.Error) {
                updateState {
                    copy(
                        isProcessing = false,
                        errorMessage = "Failed to synthesize speech: ${ttsResult.exception.message}"
                    )
                }
                return@launch
            }
            
            val audioFile = (ttsResult as Result.Success).data
            
            val useRealVoice = preferences.useRealVoice.first()
            val finalAudioFile = if (useRealVoice) {
                tryMixWithRealVoice(audioFile, memeResponse)
            } else {
                audioFile
            }
            
            updateState {
                copy(
                    isProcessing = false,
                    processingMessage = "",
                    playbackInfo = AudioPlaybackInfo(
                        state = AudioPlaybackState.LOADING,
                        audioFile = finalAudioFile
                    )
                )
            }
            
            val prepareResult = withContext(Dispatchers.IO) {
                mediaPlayerController.prepare(finalAudioFile)
            }
            
            if (prepareResult is Result.Success) {
                play()
            } else {
                updateState {
                    copy(
                        playbackInfo = AudioPlaybackInfo(
                            state = AudioPlaybackState.ERROR,
                            error = "Failed to prepare audio for playback"
                        )
                    )
                }
            }
        }
        
        // Delete user audio file if voice retention is disabled
        userAudioFile?.let { audioFile ->
            if (!getVoiceRetentionSetting()) {
                withContext(Dispatchers.IO) {
                    audioFile.delete()
                }
            }
        }
    }
    
    private suspend fun tryMixWithRealVoice(ttsFile: File, memeResponse: MemeResponse): File {
        val voiceMetaResult = voiceRepository.getVoiceMetaByEmotion(
            memeResponse.template.timeContext.name.lowercase()
        )
        
        return withContext(Dispatchers.IO) {
            try {
                voiceMetaResult.first().firstOrNull()?.let { voiceMeta ->
                    val voiceFile = File(voiceMeta.filePath)
                    if (voiceFile.exists()) {
                        val mixedFile = File(tempAudioDir, "mixed_${System.currentTimeMillis()}.wav")
                        val mixResult = ffmpegService.mixAudio(ttsFile, voiceFile, mixedFile, 0.7f)
                        if (mixResult is Result.Success) {
                            return@withContext mixResult.data
                        }
                    }
                }
            } catch (e: Exception) {
                // Fall back to TTS only
            }
            ttsFile
        }
    }
    
    fun toggleInteractionMode() {
        viewModelScope.launch {
            val currentMode = _uiState.value?.interactionMode ?: VoiceInteractionMode.RANDOM_MEME
            val newMode = when (currentMode) {
                VoiceInteractionMode.RANDOM_MEME -> VoiceInteractionMode.EMOTION_RESPONSE
                VoiceInteractionMode.EMOTION_RESPONSE -> VoiceInteractionMode.RANDOM_MEME
            }
            preferences.setVoiceMode(newMode)
            updateState { copy(interactionMode = newMode) }
        }
    }

    fun setUseRealVoice(useRealVoice: Boolean) {
        viewModelScope.launch {
            preferences.setUseRealVoice(useRealVoice)
            updateState { copy(useRealVoiceSnippets = useRealVoice) }
        }
    }

    
    fun play() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mediaPlayerController.play()
            }
        }
    }
    
    fun pause() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mediaPlayerController.pause()
            }
        }
    }
    
    fun seekTo(position: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mediaPlayerController.seekTo(position)
            }
        }
    }
    
    fun replay() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mediaPlayerController.replay()
            }
        }
    }
    
    fun clearError() {
        updateState { copy(errorMessage = null) }
    }
    
    private fun updateState(update: VoiceUiState.() -> VoiceUiState) {
        _uiState.value = (_uiState.value ?: VoiceUiState()).update()
    }
    
    override fun onCleared() {
        super.onCleared()
        amplitudeJob?.cancel()
        playbackJob?.cancel()
        audioRecordingService.cleanup()
        mediaPlayerController.release()
        ttsService.shutdown()
        cleanupTempFiles()
    }
    
    private fun cleanupTempFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tempAudioDir.listFiles()?.forEach { file ->
                    val age = System.currentTimeMillis() - file.lastModified()
                    if (age > 3600000) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    private fun getVoiceRetentionSetting(): Boolean {
        return runBlocking {
            try {
                // For now, return true as default
                // In a real implementation, this would read from preferences
                true
            } catch (e: Exception) {
                true // Default to retaining voice recordings
            }
        }
    }
}
