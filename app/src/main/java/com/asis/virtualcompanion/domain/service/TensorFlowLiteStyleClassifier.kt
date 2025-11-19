package com.asis.virtualcompanion.domain.service

import android.content.Context
import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.SpeakerStyle
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.max

class TensorFlowLiteStyleClassifier(
    private val context: Context
) {
    
    private var interpreter: Interpreter? = null
    private val labels = listOf("casual", "energetic", "sarcastic", "friendly", "formal", "playful", "serious")
    private val random = Random()
    
    companion object {
        private const val MODEL_FILE = "meme_style_classifier.tflite"
        private const val INPUT_SIZE = 512
        private const val OUTPUT_SIZE = 7
    }
    
    fun initialize(): Result<Boolean> {
        return try {
            val modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)
            val options = Interpreter.Options()
            interpreter = Interpreter(modelBuffer, options)
            Result.Success(true)
        } catch (e: Exception) {
            // Fallback to mock implementation if model not found
            Result.Success(false)
        }
    }
    
    suspend fun classifyStyle(
        inputText: String,
        detectedEmotion: String? = null,
        currentContext: Map<String, Any> = emptyMap()
    ): Result<String> {
        return try {
            val style = if (interpreter != null) {
                classifyWithModel(inputText, detectedEmotion, currentContext)
            } else {
                classifyWithFallback(inputText, detectedEmotion, currentContext)
            }
            Result.Success(style)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private fun classifyWithModel(
        inputText: String,
        detectedEmotion: String?,
        currentContext: Map<String, Any>
    ): String {
        val inputBuffer = preprocessInput(inputText, detectedEmotion, currentContext)
        val outputBuffer = Array(1) { FloatArray(OUTPUT_SIZE) }
        
        interpreter?.run(inputBuffer, outputBuffer)
        
        val probabilities = outputBuffer[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        return labels[maxIndex]
    }
    
    private fun classifyWithFallback(
        inputText: String,
        detectedEmotion: String?,
        currentContext: Map<String, Any>
    ): String {
        val text = inputText.lowercase()
        
        // Simple rule-based classification as fallback
        return when {
            detectedEmotion?.lowercase() in listOf("happy", "excited", "joyful") -> "energetic"
            detectedEmotion?.lowercase() in listOf("sad", "down", "blue") -> "friendly"
            text.contains("?") && text.length < 50 -> "casual"
            text.contains("lol", "lmao", "haha") -> "playful"
            text.contains("really", "seriously", "actually") -> "serious"
            text.contains("wow", "omg", "amazing") -> "energetic"
            text.length > 100 -> "formal"
            text.contains("bro", "dude", "man") -> "casual"
            else -> labels[random.nextInt(labels.size)]
        }
    }
    
    private fun preprocessInput(
        inputText: String,
        detectedEmotion: String?,
        currentContext: Map<String, Any>
    ): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE)
        buffer.order(ByteOrder.nativeOrder())
        
        // Simple text encoding (in real implementation, use proper tokenization)
        val textBytes = inputText.toByteArray()
        val emotionBytes = detectedEmotion?.toByteArray() ?: ByteArray(0)
        
        var index = 0
        // Encode text
        for (i in 0 until minOf(textBytes.size, INPUT_SIZE / 2)) {
            buffer.putFloat(textBytes[i].toFloat())
            index++
        }
        
        // Pad remaining text space
        while (index < INPUT_SIZE / 2) {
            buffer.putFloat(0f)
            index++
        }
        
        // Encode emotion and context
        for (i in 0 until minOf(emotionBytes.size, INPUT_SIZE / 4)) {
            buffer.putFloat(emotionBytes[i].toFloat())
            index++
        }
        
        // Add context features
        val timeOfDay = (currentContext["timeOfDay"] as? Long ?: System.currentTimeMillis()) % 86400000
        buffer.putFloat((timeOfDay / 86400000f).coerceIn(0f, 1f))
        index++
        
        // Fill remaining space
        while (index < INPUT_SIZE) {
            buffer.putFloat(0f)
            index++
        }
        
        buffer.rewind()
        return buffer
    }
    
    suspend fun classifyEmojiPack(
        style: String,
        inputText: String
    ): Result<List<String>> {
        return try {
            val emojiPack = when (style.lowercase()) {
                "energetic" -> listOf("🔥", "⚡", "🎉", "💪", "🚀", "✨", "🌟")
                "playful" -> listOf("😄", "😂", "🤪", "😜", "🎮", "🎭", "🎨")
                "friendly" -> listOf("😊", "🤗", "💙", "🌈", "☀️", "🌸", "🍀")
                "sarcastic" -> listOf("🙄", "😏", "🤔", "👀", "💅", "🎭", "🤷")
                "casual" -> listOf("😎", "👌", "🤙", "✌️", "🤝", "🍻", "🎯")
                "serious" -> listOf("📝", "💭", "🤔", "📊", "🎯", "🔍", "⚖️")
                "formal" -> listOf("🤝", "📊", "📈", "💼", "🎯", "⚖️", "🏆")
                else -> listOf("😊", "✨", "👍", "💙", "🌟", "🎉", "🚀")
            }
            Result.Success(emojiPack)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}