package com.asis.virtualcompanion.domain.service

import com.asis.virtualcompanion.common.Result
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class AudioRecordingServiceTest {

    private lateinit var service: AudioRecordingService

    @Before
    fun setup() {
        service = AudioRecordingService()
    }

    @Test
    fun `isCurrentlyRecording should return false initially`() {
        assertFalse(service.isCurrentlyRecording())
    }

    @Test
    fun `getMaxAmplitude should return 0 when not recording`() {
        val amplitude = service.getMaxAmplitude()
        assertEquals(0f, amplitude, 0.001f)
    }

    @Test
    fun `convertPcmToWav should fail with non-existent file`() = runBlocking {
        val pcmFile = File("nonexistent.pcm")
        val wavFile = File("output.wav")

        val result = service.convertPcmToWav(pcmFile, wavFile)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message?.contains("does not exist") == true)
    }

    @Test
    fun `convertPcmToWav should create WAV file with valid PCM input`() = runBlocking {
        val pcmFile = File.createTempFile("test", ".pcm").apply {
            writeBytes(ByteArray(1024) { it.toByte() })
        }
        val wavFile = File.createTempFile("test", ".wav")

        val result = service.convertPcmToWav(pcmFile, wavFile)

        assertTrue(result is Result.Success)
        assertTrue(wavFile.exists())
        assertTrue(wavFile.length() > pcmFile.length())

        pcmFile.delete()
        wavFile.delete()
    }

    @Test
    fun `cleanup should not throw exception`() {
        service.cleanup()
    }
}
