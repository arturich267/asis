package com.asis.virtualcompanion.data.model

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class VoiceInteractionModelsTest {

    @Test
    fun `AudioRecordingState should initialize with default values`() {
        val state = AudioRecordingState()

        assertFalse(state.isRecording)
        assertNull(state.audioFile)
        assertEquals(0L, state.durationMs)
        assertEquals(0f, state.amplitudePercent, 0.001f)
    }

    @Test
    fun `AudioRecordingState should accept custom values`() {
        val file = File("test.pcm")
        val state = AudioRecordingState(
            isRecording = true,
            audioFile = file,
            durationMs = 5000,
            amplitudePercent = 75.5f
        )

        assertTrue(state.isRecording)
        assertEquals(file, state.audioFile)
        assertEquals(5000L, state.durationMs)
        assertEquals(75.5f, state.amplitudePercent, 0.001f)
    }

    @Test
    fun `EmotionAnalysisResult should store emotion data`() {
        val scores = mapOf("happy" to 0.8f, "sad" to 0.1f, "neutral" to 0.1f)
        val result = EmotionAnalysisResult(
            emotion = "happy",
            confidence = 0.8f,
            emotionScores = scores
        )

        assertEquals("happy", result.emotion)
        assertEquals(0.8f, result.confidence, 0.001f)
        assertEquals(3, result.emotionScores.size)
        assertEquals(0.8f, result.emotionScores["happy"], 0.001f)
    }

    @Test
    fun `VoiceInteractionMode should have two modes`() {
        val modes = VoiceInteractionMode.values()

        assertEquals(2, modes.size)
        assertTrue(modes.contains(VoiceInteractionMode.RANDOM_MEME))
        assertTrue(modes.contains(VoiceInteractionMode.EMOTION_RESPONSE))
    }

    @Test
    fun `AudioPlaybackState should have all required states`() {
        val states = AudioPlaybackState.values()

        assertEquals(6, states.size)
        assertTrue(states.contains(AudioPlaybackState.IDLE))
        assertTrue(states.contains(AudioPlaybackState.LOADING))
        assertTrue(states.contains(AudioPlaybackState.PLAYING))
        assertTrue(states.contains(AudioPlaybackState.PAUSED))
        assertTrue(states.contains(AudioPlaybackState.COMPLETED))
        assertTrue(states.contains(AudioPlaybackState.ERROR))
    }

    @Test
    fun `AudioPlaybackInfo should initialize with IDLE state`() {
        val info = AudioPlaybackInfo()

        assertEquals(AudioPlaybackState.IDLE, info.state)
        assertNull(info.audioFile)
        assertEquals(0, info.currentPositionMs)
        assertEquals(0, info.durationMs)
        assertNull(info.error)
    }

    @Test
    fun `TTSConfig should have default values`() {
        val config = TTSConfig()

        assertEquals(1.0f, config.pitch, 0.001f)
        assertEquals(1.0f, config.speed, 0.001f)
        assertEquals("en-US", config.language)
    }

    @Test
    fun `TTSConfig should accept custom values`() {
        val config = TTSConfig(
            pitch = 1.5f,
            speed = 0.8f,
            language = "es-ES"
        )

        assertEquals(1.5f, config.pitch, 0.001f)
        assertEquals(0.8f, config.speed, 0.001f)
        assertEquals("es-ES", config.language)
    }

    @Test
    fun `AudioMixingConfig should store mixing parameters`() {
        val bgFile = File("background.wav")
        val ttsFile = File("tts.wav")
        val outputFile = File("output.wav")

        val config = AudioMixingConfig(
            backgroundVoiceFile = bgFile,
            ttsFile = ttsFile,
            outputFile = outputFile,
            mixRatio = 0.7f
        )

        assertEquals(bgFile, config.backgroundVoiceFile)
        assertEquals(ttsFile, config.ttsFile)
        assertEquals(outputFile, config.outputFile)
        assertEquals(0.7f, config.mixRatio, 0.001f)
    }

    @Test
    fun `VoiceResponseResult should store response data`() {
        val audioFile = File("response.wav")
        val memeResponse = MemeResponse(
            text = "Test response",
            template = MemeTemplate(
                id = "test",
                phrase = "test phrase",
                memeSuffix = "no cap",
                emojiReferences = listOf("😊"),
                stickerReferences = emptyList(),
                styleId = "casual",
                timeContext = TimeContext.AFTERNOON
            ),
            confidence = 0.85f
        )

        val result = VoiceResponseResult(
            audioFile = audioFile,
            memeResponse = memeResponse,
            usedTTS = true,
            mixedWithRealVoice = false
        )

        assertEquals(audioFile, result.audioFile)
        assertEquals("Test response", result.memeResponse.text)
        assertTrue(result.usedTTS)
        assertFalse(result.mixedWithRealVoice)
    }
}
