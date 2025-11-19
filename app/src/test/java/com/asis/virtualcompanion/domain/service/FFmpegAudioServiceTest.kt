package com.asis.virtualcompanion.domain.service

import com.asis.virtualcompanion.common.Result
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.io.File

class FFmpegAudioServiceTest {

    private lateinit var service: FFmpegAudioService

    @Before
    fun setup() {
        service = FFmpegAudioService()
    }

    @Test
    fun `trimAudio should fail when input file does not exist`() = runBlocking {
        val inputFile = File("nonexistent.wav")
        val outputFile = File("output.wav")

        val result = service.trimAudio(inputFile, outputFile, 0, 5000)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `mixAudio should fail when input files do not exist`() = runBlocking {
        val input1 = File("nonexistent1.wav")
        val input2 = File("nonexistent2.wav")
        val output = File("output.wav")

        val result = service.mixAudio(input1, input2, output, 0.5f)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `concatenateAudio should fail with empty input list`() = runBlocking {
        val outputFile = File("output.wav")

        val result = service.concatenateAudio(emptyList(), outputFile)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message?.contains("No input files") == true)
    }

    @Test
    fun `concatenateAudio should fail when any input file does not exist`() = runBlocking {
        val files = listOf(
            File("exists.wav"),
            File("nonexistent.wav")
        )
        val outputFile = File("output.wav")

        val result = service.concatenateAudio(files, outputFile)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `convertAudio should fail when input file does not exist`() = runBlocking {
        val inputFile = File("nonexistent.mp3")
        val outputFile = File("output.wav")

        val result = service.convertAudio(inputFile, outputFile)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `getAudioDuration should fail when file does not exist`() = runBlocking {
        val file = File("nonexistent.wav")

        val result = service.getAudioDuration(file)

        assertTrue(result is Result.Error)
    }
}
