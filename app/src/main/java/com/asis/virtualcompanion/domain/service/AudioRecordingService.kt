package com.asis.virtualcompanion.domain.service

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.asis.virtualcompanion.common.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class AudioRecordingService {
    
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_MULTIPLIER = 2
    }
    
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    
    fun startRecording(outputFile: File): Result<Unit> {
        try {
            if (isRecording) {
                return Result.Error(IllegalStateException("Already recording"))
            }
            
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            ) * BUFFER_SIZE_MULTIPLIER
            
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                return Result.Error(IllegalStateException("Unable to get audio buffer size"))
            }
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return Result.Error(IllegalStateException("AudioRecord not initialized"))
            }
            
            audioRecord?.startRecording()
            isRecording = true
            
            recordingThread = Thread {
                writeAudioDataToFile(outputFile, bufferSize)
            }.apply { start() }
            
            return Result.Success(Unit)
        } catch (e: SecurityException) {
            return Result.Error(e)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }
    
    private fun writeAudioDataToFile(outputFile: File, bufferSize: Int) {
        val buffer = ShortArray(bufferSize / 2)
        FileOutputStream(outputFile).use { outputStream ->
            while (isRecording) {
                val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readResult > 0) {
                    val byteBuffer = ByteArray(readResult * 2)
                    for (i in 0 until readResult) {
                        byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                        byteBuffer[i * 2 + 1] = (buffer[i].toInt() shr 8 and 0xFF).toByte()
                    }
                    outputStream.write(byteBuffer)
                }
            }
        }
    }
    
    fun stopRecording(): Result<Unit> {
        return try {
            isRecording = false
            recordingThread?.join(1000)
            audioRecord?.apply {
                stop()
                release()
            }
            audioRecord = null
            recordingThread = null
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    fun getMaxAmplitude(): Float {
        val audioRecord = this.audioRecord ?: return 0f
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED || !isRecording) {
            return 0f
        }
        
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )
        val buffer = ShortArray(bufferSize / 2)
        val readResult = audioRecord.read(buffer, 0, buffer.size)
        
        if (readResult <= 0) return 0f
        
        var maxAmplitude = 0
        for (i in 0 until readResult) {
            val amplitude = abs(buffer[i].toInt())
            if (amplitude > maxAmplitude) {
                maxAmplitude = amplitude
            }
        }
        
        return (maxAmplitude.toFloat() / Short.MAX_VALUE.toFloat()) * 100f
    }
    
    fun isCurrentlyRecording(): Boolean = isRecording
    
    suspend fun convertPcmToWav(
        pcmFile: File,
        wavFile: File,
        sampleRate: Int = SAMPLE_RATE,
        channels: Int = 1,
        bitDepth: Int = 16
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!pcmFile.exists()) {
                return@withContext Result.Error(IllegalArgumentException("PCM file does not exist"))
            }
            
            val pcmData = pcmFile.readBytes()
            val wavHeader = createWavHeader(pcmData.size, sampleRate, channels, bitDepth)
            
            FileOutputStream(wavFile).use { outputStream ->
                outputStream.write(wavHeader)
                outputStream.write(pcmData)
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private fun createWavHeader(
        dataSize: Int,
        sampleRate: Int,
        channels: Int,
        bitDepth: Int
    ): ByteArray {
        val byteRate = sampleRate * channels * bitDepth / 8
        val header = ByteArray(44)
        
        // RIFF chunk descriptor
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        
        val fileSize = dataSize + 36
        header[4] = (fileSize and 0xff).toByte()
        header[5] = (fileSize shr 8 and 0xff).toByte()
        header[6] = (fileSize shr 16 and 0xff).toByte()
        header[7] = (fileSize shr 24 and 0xff).toByte()
        
        // WAVE format
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        
        // fmt subchunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        
        header[16] = 16 // Subchunk1Size for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0
        
        header[20] = 1 // AudioFormat (1 = PCM)
        header[21] = 0
        
        header[22] = channels.toByte()
        header[23] = 0
        
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        
        val blockAlign = channels * bitDepth / 8
        header[32] = blockAlign.toByte()
        header[33] = 0
        
        header[34] = bitDepth.toByte()
        header[35] = 0
        
        // data subchunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        
        header[40] = (dataSize and 0xff).toByte()
        header[41] = (dataSize shr 8 and 0xff).toByte()
        header[42] = (dataSize shr 16 and 0xff).toByte()
        header[43] = (dataSize shr 24 and 0xff).toByte()
        
        return header
    }
    
    fun cleanup() {
        if (isRecording) {
            stopRecording()
        }
    }
}
