package com.asis.virtualcompanion.domain.service

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.EmotionAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

class EmotionAnalysisService {
    
    suspend fun analyzeAudio(audioFile: File): Result<EmotionAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            if (!audioFile.exists()) {
                return@withContext Result.Error(IllegalArgumentException("Audio file does not exist"))
            }
            
            delay(500)
            
            val emotions = listOf("happy", "sad", "excited", "calm", "angry", "neutral", "surprised")
            val emotion = emotions[Random.nextInt(emotions.size)]
            
            val emotionScores = emotions.associateWith {
                if (it == emotion) {
                    Random.nextFloat() * 0.3f + 0.7f
                } else {
                    Random.nextFloat() * 0.3f
                }
            }
            
            val result = EmotionAnalysisResult(
                emotion = emotion,
                confidence = emotionScores[emotion] ?: 0.5f,
                emotionScores = emotionScores
            )
            
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun analyzeText(text: String): Result<EmotionAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            if (text.isBlank()) {
                return@withContext Result.Error(IllegalArgumentException("Text is empty"))
            }
            
            delay(200)
            
            val lowerText = text.lowercase()
            val emotion = when {
                lowerText.contains("love") || lowerText.contains("😊") || lowerText.contains("❤") -> "happy"
                lowerText.contains("sad") || lowerText.contains("😢") || lowerText.contains("cry") -> "sad"
                lowerText.contains("wow") || lowerText.contains("amazing") || lowerText.contains("😮") -> "surprised"
                lowerText.contains("angry") || lowerText.contains("mad") || lowerText.contains("😠") -> "angry"
                lowerText.contains("excited") || lowerText.contains("🎉") || lowerText.contains("yay") -> "excited"
                lowerText.contains("calm") || lowerText.contains("peace") || lowerText.contains("😌") -> "calm"
                else -> "neutral"
            }
            
            val confidence = Random.nextFloat() * 0.2f + 0.7f
            
            val emotionScores = mapOf(
                emotion to confidence,
                "neutral" to (1f - confidence) * 0.5f,
                "happy" to Random.nextFloat() * 0.2f,
                "sad" to Random.nextFloat() * 0.2f,
                "excited" to Random.nextFloat() * 0.2f,
                "calm" to Random.nextFloat() * 0.2f,
                "angry" to Random.nextFloat() * 0.2f,
                "surprised" to Random.nextFloat() * 0.2f
            )
            
            val result = EmotionAnalysisResult(
                emotion = emotion,
                confidence = confidence,
                emotionScores = emotionScores
            )
            
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
