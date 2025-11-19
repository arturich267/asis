package com.asis.virtualcompanion.data.model

import java.io.File

data class AudioRecordingState(
    val isRecording: Boolean = false,
    val audioFile: File? = null,
    val durationMs: Long = 0,
    val amplitudePercent: Float = 0f
)

data class EmotionAnalysisResult(
    val emotion: String,
    val confidence: Float,
    val emotionScores: Map<String, Float> = emptyMap()
)

enum class VoiceInteractionMode {
    RANDOM_MEME,
    EMOTION_RESPONSE
}

enum class AudioPlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class AudioPlaybackInfo(
    val state: AudioPlaybackState = AudioPlaybackState.IDLE,
    val audioFile: File? = null,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val error: String? = null
)

data class TTSConfig(
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f,
    val language: String = "en-US"
)

data class AudioMixingConfig(
    val backgroundVoiceFile: File? = null,
    val ttsFile: File,
    val outputFile: File,
    val mixRatio: Float = 0.5f
)

data class VoiceResponseResult(
    val audioFile: File,
    val memeResponse: MemeResponse,
    val usedTTS: Boolean = true,
    val mixedWithRealVoice: Boolean = false
)
