package com.asis.virtualcompanion.work.voice

import android.media.MediaMetadataRetriever
import com.asis.virtualcompanion.data.model.VoiceNoteMetadata
import com.asis.virtualcompanion.data.model.SentimentResult
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class VoiceNoteProcessor(
    private val tflite: Interpreter?
) {
    
    private val metadataRetriever = MediaMetadataRetriever()
    
    fun extractMetadata(filePath: String, fileName: String, sender: String, timestamp: Long): VoiceNoteMetadata? {
        return try {
            metadataRetriever.setDataSource(filePath)
            
            val duration = extractDuration()
            val title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: fileName
            val artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: sender
            
            VoiceNoteMetadata(
                fileName = fileName,
                sender = sender,
                timestamp = java.util.Date(timestamp),
                duration = duration,
                filePath = filePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                metadataRetriever.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }
    
    fun classifyVoiceNote(filePath: String): SentimentResult? {
        val interpreter = tflite ?: return null
        
        return try {
            val audioFeatures = extractAudioFeatures(filePath) ?: return null
            val classification = runInference(interpreter, audioFeatures)
            
            classification
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun extractDuration(): Long {
        val durationStr = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr?.toLongOrNull() ?: 0L
    }
    
    private fun extractAudioFeatures(filePath: String): FloatArray? {
        return try {
            // This is a simplified feature extraction
            // In a real implementation, you would use audio processing libraries
            // to extract MFCCs, spectral features, etc.
            
            val file = File(filePath)
            if (!file.exists()) return null
            
            // For now, create a dummy feature vector
            // In production, this would be actual audio feature extraction
            val features = FloatArray(13) // 13 MFCC coefficients
            val random = java.util.Random(filePath.hashCode())
            
            for (i in features.indices) {
                features[i] = random.nextFloat() * 2.0f - 1.0f
            }
            
            features
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun runInference(interpreter: Interpreter, features: FloatArray): SentimentResult {
        // Prepare input buffer
        val inputBuffer = ByteBuffer.allocateDirect(4 * features.size)
        inputBuffer.order(ByteOrder.nativeOrder())
        
        for (feature in features) {
            inputBuffer.putFloat(feature)
        }
        
        // Prepare output buffer
        val outputShape = intArrayOf(1, 4) // Assuming 4 emotion classes
        val outputBuffer = Array(1) { FloatArray(4) }
        
        // Run inference
        interpreter.run(inputBuffer, outputBuffer)
        
        // Process results
        val probabilities = outputBuffer[0]
        val emotions = mapOf(
            "happy" to probabilities[0],
            "sad" to probabilities[1],
            "angry" to probabilities[2],
            "neutral" to probabilities[3]
        )
        
        val maxEmotion = emotions.maxByOrNull { it.value }?.key ?: "neutral"
        val maxConfidence = emotions[maxEmotion] ?: 0f
        
        return SentimentResult(
            sentiment = maxEmotion,
            confidence = maxConfidence,
            emotions = emotions
        )
    }
    
    fun getAudioFileInfo(filePath: String): AudioFileInfo? {
        return try {
            metadataRetriever.setDataSource(filePath)
            
            val duration = extractDuration()
            val title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val bitrate = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
            val sampleRate = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull()
            
            AudioFileInfo(
                filePath = filePath,
                duration = duration,
                title = title,
                artist = artist,
                album = album,
                bitrate = bitrate,
                sampleRate = sampleRate
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                metadataRetriever.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }
    
    fun isVoiceNoteFile(fileName: String): Boolean {
        val voiceNotePatterns = listOf(
            Regex("""PTT-\d{4}-\d{2}-\d{2}-WA\d+\.opus""", RegexOption.IGNORE_CASE),
            Regex("""voice_note_\d+\.opus""", RegexOption.IGNORE_CASE),
            Regex("""audio_\d+\.opus""", RegexOption.IGNORE_CASE),
            Regex(""".*voice.*\.opus""", RegexOption.IGNORE_CASE),
            Regex(""".*voice.*\.mp3""", RegexOption.IGNORE_CASE),
            Regex(""".*voice.*\.wav""", RegexOption.IGNORE_CASE)
        )
        
        return voiceNotePatterns.any { it.matches(fileName) }
    }
}

data class AudioFileInfo(
    val filePath: String,
    val duration: Long,
    val title: String?,
    val artist: String?,
    val album: String?,
    val bitrate: Int?,
    val sampleRate: Int?
)