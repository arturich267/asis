package com.asis.virtualcompanion.domain.service

import com.asis.virtualcompanion.common.Result
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class EmotionAnalysisServiceTest {

    private lateinit var service: EmotionAnalysisService

    @Before
    fun setup() {
        service = EmotionAnalysisService()
    }

    @Test
    fun `analyzeAudio should fail when file does not exist`() = runBlocking {
        val nonExistentFile = File("nonexistent.wav")

        val result = service.analyzeAudio(nonExistentFile)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message?.contains("does not exist") == true)
    }

    @Test
    fun `analyzeText should succeed with non-empty text`() = runBlocking {
        val text = "I am so happy today!"

        val result = service.analyzeText(text)

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals("happy", data.emotion)
        assertTrue(data.confidence > 0f)
    }

    @Test
    fun `analyzeText should fail with empty text`() = runBlocking {
        val text = ""

        val result = service.analyzeText(text)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `analyzeText should detect sad emotion`() = runBlocking {
        val text = "I am so sad and crying"

        val result = service.analyzeText(text)

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals("sad", data.emotion)
    }

    @Test
    fun `analyzeText should detect excited emotion`() = runBlocking {
        val text = "Wow this is amazing! 🎉"

        val result = service.analyzeText(text)

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertTrue(data.emotion == "excited" || data.emotion == "surprised")
    }

    @Test
    fun `analyzeText should have emotion scores map`() = runBlocking {
        val text = "This is interesting"

        val result = service.analyzeText(text)

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertTrue(data.emotionScores.isNotEmpty())
        assertTrue(data.emotionScores.containsKey(data.emotion))
    }
}
