package com.asis.virtualcompanion.domain.service

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.asis.virtualcompanion.common.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FFmpegAudioService {
    
    companion object {
        private const val TAG = "FFmpegAudioService"
    }
    
    suspend fun trimAudio(
        inputFile: File,
        outputFile: File,
        startTimeMs: Long,
        durationMs: Long
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!inputFile.exists()) {
                return@withContext Result.Error(IllegalArgumentException("Input file does not exist"))
            }
            
            val startSeconds = startTimeMs / 1000.0
            val durationSeconds = durationMs / 1000.0
            
            val command = "-i \"${inputFile.absolutePath}\" " +
                    "-ss $startSeconds " +
                    "-t $durationSeconds " +
                    "-c copy \"${outputFile.absolutePath}\""
            
            val session = FFmpegKit.execute(command)
            val returnCode = session.returnCode
            
            if (ReturnCode.isSuccess(returnCode)) {
                if (outputFile.exists()) {
                    Result.Success(outputFile)
                } else {
                    Result.Error(IllegalStateException("Output file was not created"))
                }
            } else {
                val error = session.failStackTrace ?: "Unknown error"
                Log.e(TAG, "FFmpeg trim failed: $error")
                Result.Error(IllegalStateException("FFmpeg trim failed: $error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun mixAudio(
        input1File: File,
        input2File: File,
        outputFile: File,
        mixRatio: Float = 0.5f
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!input1File.exists() || !input2File.exists()) {
                return@withContext Result.Error(IllegalArgumentException("Input files do not exist"))
            }
            
            val volume1 = mixRatio
            val volume2 = 1.0f - mixRatio
            
            val command = "-i \"${input1File.absolutePath}\" " +
                    "-i \"${input2File.absolutePath}\" " +
                    "-filter_complex \"[0:a]volume=$volume1[a1];[1:a]volume=$volume2[a2];[a1][a2]amix=inputs=2:duration=longest\" " +
                    "-c:a aac -b:a 192k \"${outputFile.absolutePath}\""
            
            val session = FFmpegKit.execute(command)
            val returnCode = session.returnCode
            
            if (ReturnCode.isSuccess(returnCode)) {
                if (outputFile.exists()) {
                    Result.Success(outputFile)
                } else {
                    Result.Error(IllegalStateException("Output file was not created"))
                }
            } else {
                val error = session.failStackTrace ?: "Unknown error"
                Log.e(TAG, "FFmpeg mix failed: $error")
                Result.Error(IllegalStateException("FFmpeg mix failed: $error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun concatenateAudio(
        inputFiles: List<File>,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (inputFiles.isEmpty()) {
                return@withContext Result.Error(IllegalArgumentException("No input files provided"))
            }
            
            if (inputFiles.any { !it.exists() }) {
                return@withContext Result.Error(IllegalArgumentException("Some input files do not exist"))
            }
            
            val concatListFile = File(outputFile.parent, "concat_list_${System.currentTimeMillis()}.txt")
            concatListFile.writeText(inputFiles.joinToString("\n") { "file '${it.absolutePath}'" })
            
            val command = "-f concat -safe 0 -i \"${concatListFile.absolutePath}\" " +
                    "-c copy \"${outputFile.absolutePath}\""
            
            val session = FFmpegKit.execute(command)
            val returnCode = session.returnCode
            
            concatListFile.delete()
            
            if (ReturnCode.isSuccess(returnCode)) {
                if (outputFile.exists()) {
                    Result.Success(outputFile)
                } else {
                    Result.Error(IllegalStateException("Output file was not created"))
                }
            } else {
                val error = session.failStackTrace ?: "Unknown error"
                Log.e(TAG, "FFmpeg concatenate failed: $error")
                Result.Error(IllegalStateException("FFmpeg concatenate failed: $error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun convertAudio(
        inputFile: File,
        outputFile: File,
        outputFormat: String = "wav",
        sampleRate: Int = 44100,
        channels: Int = 1
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!inputFile.exists()) {
                return@withContext Result.Error(IllegalArgumentException("Input file does not exist"))
            }
            
            val command = "-i \"${inputFile.absolutePath}\" " +
                    "-ar $sampleRate " +
                    "-ac $channels " +
                    "-f $outputFormat \"${outputFile.absolutePath}\""
            
            val session = FFmpegKit.execute(command)
            val returnCode = session.returnCode
            
            if (ReturnCode.isSuccess(returnCode)) {
                if (outputFile.exists()) {
                    Result.Success(outputFile)
                } else {
                    Result.Error(IllegalStateException("Output file was not created"))
                }
            } else {
                val error = session.failStackTrace ?: "Unknown error"
                Log.e(TAG, "FFmpeg convert failed: $error")
                Result.Error(IllegalStateException("FFmpeg convert failed: $error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getAudioDuration(file: File): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                return@withContext Result.Error(IllegalArgumentException("File does not exist"))
            }
            
            val command = "-i \"${file.absolutePath}\" -show_entries format=duration -v quiet -of csv=p=0"
            val session = FFmpegKit.execute(command)
            
            if (ReturnCode.isSuccess(session.returnCode)) {
                val output = session.output?.trim()
                val durationSeconds = output?.toDoubleOrNull() ?: 0.0
                Result.Success((durationSeconds * 1000).toLong())
            } else {
                Result.Error(IllegalStateException("Failed to get audio duration"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
