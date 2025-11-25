package com.asis.virtualcompanion.work.analytics

import com.asis.virtualcompanion.data.model.*
import java.util.*
import kotlin.math.min

class NLPAnalyticsProcessor {
    
    private val sentimentKeywords = mapOf(
        "positive" to listOf(
            "happy", "good", "great", "awesome", "fantastic", "love", "excellent", "amazing", "wonderful", "perfect",
            "nice", "cool", "beautiful", "thank", "thanks", "appreciate", "glad", "pleased", "excited", "blessed",
            "😊", "😄", "😁", "😃", "👍", "❤️", "💕", "🎉", "🥳", "✨"
        ),
        "negative" to listOf(
            "sad", "bad", "terrible", "awful", "hate", "horrible", "worst", "disgusting", "pathetic", "annoying",
            "angry", "mad", "upset", "frustrated", "disappointed", "worried", "scared", "afraid", "sorry", "regret",
            "😢", "😭", "😡", "😠", "👎", "💔", "😞", "😔", "😪", "😿"
        ),
        "neutral" to listOf(
            "okay", "ok", "alright", "fine", "sure", "maybe", "perhaps", "probably", "actually", "literally",
            "basically", "technically", "supposedly", "apparently", "suppose", "guess", "think", "feel", "seem", "look"
        )
    )
    
    private val phrasePatterns = listOf(
        // Common phrases
        Regex("\\b(how are you|what's up|good morning|good night|thank you|you're welcome|see you|take care)\\b", RegexOption.IGNORE_CASE),
        // Emotional expressions
        Regex("\\b(i love you|i miss you|i'm sorry|excuse me|congratulations|happy birthday)\\b", RegexOption.IGNORE_CASE),
        // Question patterns
        Regex("\\b(what|when|where|who|why|how)\\s+(is|are|was|were|do|does|did|can|could|will|would|should)\\b", RegexOption.IGNORE_CASE),
        // Common responses
        Regex("\\b(no problem|don't worry|it's okay|that's fine|sounds good|got it|understood)\\b", RegexOption.IGNORE_CASE),
        // Time expressions
        Regex("\\b(tomorrow|yesterday|today|tonight|morning|afternoon|evening|night|weekend|holiday)\\b", RegexOption.IGNORE_CASE)
    )
    
    private val stopWords = setOf(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he", "in", "is", "it", "its",
        "of", "on", "that", "the", "to", "was", "will", "with", "i", "you", "we", "they", "this", "that",
        "these", "those", "am", "is", "are", "was", "were", "being", "been", "have", "has", "had", "do",
        "does", "did", "but", "or", "if", "because", "while", "until", "though", "although", "so", "than"
    )
    
    fun analyzeMessages(messages: List<WhatsAppMessage>): NLPAnalysisResult {
        val phraseStats = extractPhraseFrequencies(messages)
        val sentimentStats = analyzeSentiment(messages)
        val communicationPatterns = analyzeCommunicationPatterns(messages)
        
        return NLPAnalysisResult(
            phraseStats = phraseStats,
            sentimentStats = sentimentStats,
            communicationPatterns = communicationPatterns
        )
    }
    
    private fun extractPhraseFrequencies(messages: List<WhatsAppMessage>): List<PhraseFrequency> {
        val phraseCounts = mutableMapOf<String, Int>()
        val phraseEmojis = mutableMapOf<String, MutableList<String>>()
        
        for (message in messages.filter { it.messageType == MessageType.TEXT }) {
            val text = message.text.lowercase(Locale.getDefault())
            
            // Extract common phrases using patterns
            for (pattern in phrasePatterns) {
                val matches = pattern.findAll(text)
                for (match in matches) {
                    val phrase = match.value.lowercase(Locale.getDefault())
                    phraseCounts[phrase] = phraseCounts.getOrDefault(phrase, 0) + 1
                    
                    // Extract emojis from the message
                    val emojis = extractEmojis(message.text)
                    if (emojis.isNotEmpty()) {
                        phraseEmojis.getOrPut(phrase) { mutableListOf() }.addAll(emojis)
                    }
                }
            }
            
            // Extract n-grams (2-3 word phrases)
            val words = text.split(Regex("\\s+")).filter { it.isNotBlank() && !stopWords.contains(it.lowercase()) }
            extractNGrams(words, 2).forEach { ngram ->
                phraseCounts[ngram] = phraseCounts.getOrDefault(ngram, 0) + 1
            }
            extractNGrams(words, 3).forEach { ngram ->
                phraseCounts[ngram] = phraseCounts.getOrDefault(ngram, 0) + 1
            }
        }
        
        return phraseCounts.map { (phrase, count) ->
            PhraseFrequency(
                phrase = phrase,
                count = count,
                emojiHints = phraseEmojis[phrase]?.distinct() ?: emptyList()
            )
        }.sortedByDescending { it.count }.take(100) // Top 100 phrases
    }
    
    private fun extractNGrams(words: List<String>, n: Int): List<String> {
        if (words.size < n) return emptyList()
        
        return (0..words.size - n).map { i ->
            words.subList(i, i + n).joinToString(" ")
        }
    }
    
    private fun extractEmojis(text: String): List<String> {
        val emojiPattern = Regex("[\\p{So}\\p{Sk}]")
        return emojiPattern.findAll(text).map { it.value }.toList()
    }
    
    private fun analyzeSentiment(messages: List<WhatsAppMessage>): SentimentAnalysisResult {
        val sentimentCounts = mutableMapOf<String, Int>()
        val totalMessages = messages.filter { it.messageType == MessageType.TEXT }.size
        
        for (message in messages.filter { it.messageType == MessageType.TEXT }) {
            val text = message.text.lowercase(Locale.getDefault())
            val sentiment = classifySentiment(text)
            sentimentCounts[sentiment] = sentimentCounts.getOrDefault(sentiment, 0) + 1
        }
        
        return SentimentAnalysisResult(
            totalMessages = totalMessages,
            sentimentDistribution = sentimentCounts,
            averageSentimentScore = calculateAverageSentiment(sentimentCounts, totalMessages)
        )
    }
    
    private fun classifySentiment(text: String): String {
        val positiveCount = sentimentKeywords["positive"]?.count { keyword -> text.contains(keyword, ignoreCase = true) } ?: 0
        val negativeCount = sentimentKeywords["negative"]?.count { keyword -> text.contains(keyword, ignoreCase = true) } ?: 0
        val neutralCount = sentimentKeywords["neutral"]?.count { keyword -> text.contains(keyword, ignoreCase = true) } ?: 0
        
        return when {
            positiveCount > negativeCount && positiveCount > neutralCount -> "positive"
            negativeCount > positiveCount && negativeCount > neutralCount -> "negative"
            else -> "neutral"
        }
    }
    
    private fun calculateAverageSentiment(sentimentCounts: Map<String, Int>, totalMessages: Int): Float {
        if (totalMessages == 0) return 0f
        
        val positiveScore = (sentimentCounts["positive"] ?: 0) * 1f
        val negativeScore = (sentimentCounts["negative"] ?: 0) * -1f
        val neutralScore = (sentimentCounts["neutral"] ?: 0) * 0f
        
        return (positiveScore + negativeScore + neutralScore) / totalMessages
    }
    
    private fun analyzeCommunicationPatterns(messages: List<WhatsAppMessage>): CommunicationPatterns {
        val senderStats = mutableMapOf<String, SenderStats>()
        val hourlyActivity = mutableMapOf<Int, Int>()
        val dailyActivity = mutableMapOf<Int, Int>()
        val messageTypeStats = mutableMapOf<MessageType, Int>()
        
        for (message in messages) {
            // Sender statistics
            val currentStats = senderStats.getOrPut(message.sender) {
                SenderStats(sender = message.sender, messageCount = 0, wordCount = 0)
            }
            val wordCountIncrement = message.text.split(Regex("\\s+")).size
            senderStats[message.sender] = currentStats.copy(
                messageCount = currentStats.messageCount + 1,
                wordCount = currentStats.wordCount + wordCountIncrement
            )
            
            // Time-based patterns
            val calendar = Calendar.getInstance()
            calendar.time = message.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val day = calendar.get(Calendar.DAY_OF_WEEK)
            
            hourlyActivity[hour] = hourlyActivity.getOrDefault(hour, 0) + 1
            dailyActivity[day] = dailyActivity.getOrDefault(day, 0) + 1
            
            // Message type statistics
            messageTypeStats[message.messageType] = messageTypeStats.getOrDefault(message.messageType, 0) + 1
        }
        
        return CommunicationPatterns(
            senderStats = senderStats.values.toList(),
            hourlyActivity = hourlyActivity,
            dailyActivity = dailyActivity,
            messageTypeDistribution = messageTypeStats
        )
    }
}

data class NLPAnalysisResult(
    val phraseStats: List<PhraseFrequency>,
    val sentimentStats: SentimentAnalysisResult,
    val communicationPatterns: CommunicationPatterns
)

data class SentimentAnalysisResult(
    val totalMessages: Int,
    val sentimentDistribution: Map<String, Int>,
    val averageSentimentScore: Float
)

data class CommunicationPatterns(
    val senderStats: List<SenderStats>,
    val hourlyActivity: Map<Int, Int>,
    val dailyActivity: Map<Int, Int>,
    val messageTypeDistribution: Map<MessageType, Int>
)

data class SenderStats(
    val sender: String,
    val messageCount: Int,
    val wordCount: Int
) {
    val averageWordsPerMessage: Float
        get() = if (messageCount > 0) wordCount.toFloat() / messageCount else 0f
}