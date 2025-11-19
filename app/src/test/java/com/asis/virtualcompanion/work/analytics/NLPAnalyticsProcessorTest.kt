package com.asis.virtualcompanion.work.analytics

import com.asis.virtualcompanion.data.model.*
import org.junit.Test
import org.junit.Assert.*
import java.util.*

class NLPAnalyticsProcessorTest {

    private val processor = NLPAnalyticsProcessor()

    @Test
    fun `should analyze basic sentiment`() {
        val messages = listOf(
            WhatsAppMessage("Alice", Date(), "I am so happy today!", MessageType.TEXT),
            WhatsAppMessage("Bob", Date(), "This is terrible news", MessageType.TEXT),
            WhatsAppMessage("Alice", Date(), "It's okay, I guess", MessageType.TEXT)
        )

        val result = processor.analyzeMessages(messages)

        assertNotNull(result.sentimentStats)
        assertTrue(result.sentimentStats.totalMessages == 3)
        assertTrue(result.sentimentStats.sentimentDistribution.containsKey("positive"))
        assertTrue(result.sentimentStats.sentimentDistribution.containsKey("negative"))
        assertTrue(result.sentimentStats.sentimentDistribution.containsKey("neutral"))
    }

    @Test
    fun `should extract phrase frequencies`() {
        val messages = listOf(
            WhatsAppMessage("Alice", Date(), "how are you doing today?", MessageType.TEXT),
            WhatsAppMessage("Bob", Date(), "how are you feeling?", MessageType.TEXT),
            WhatsAppMessage("Alice", Date(), "good morning everyone", MessageType.TEXT),
            WhatsAppMessage("Bob", Date(), "good morning to you too", MessageType.TEXT)
        )

        val result = processor.analyzeMessages(messages)

        assertNotNull(result.phraseStats)
        assertTrue(result.phraseStats.isNotEmpty())
        
        val howAreYouPhrase = result.phraseStats.find { it.phrase.contains("how are you") }
        assertNotNull(howAreYouPhrase)
        assertTrue(howAreYouPhrase!!.count >= 2)
        
        val goodMorningPhrase = result.phraseStats.find { it.phrase.contains("good morning") }
        assertNotNull(goodMorningPhrase)
        assertTrue(goodMorningPhrase!!.count >= 2)
    }

    @Test
    fun `should analyze communication patterns`() {
        val messages = listOf(
            WhatsAppMessage("Alice", Date(), "Hello", MessageType.TEXT),
            WhatsAppMessage("Alice", Date(), "How are you?", MessageType.TEXT),
            WhatsAppMessage("Bob", Date(), "I'm fine", MessageType.TEXT),
            WhatsAppMessage("Alice", Date(), "Great to hear", MessageType.TEXT),
            WhatsAppMessage("Alice", Date(), "<image: omitted>", MessageType.IMAGE),
            WhatsAppMessage("Bob", Date(), "<Media omitted>", MessageType.VOICE_NOTE)
        )

        val result = processor.analyzeMessages(messages)

        assertNotNull(result.communicationPatterns)
        
        val senderStats = result.communicationPatterns.senderStats
        assertEquals(2, senderStats.size)
        
        val aliceStats = senderStats.find { it.sender == "Alice" }
        assertNotNull(aliceStats)
        assertEquals(4, aliceStats!!.messageCount)
        
        val bobStats = senderStats.find { it.sender == "Bob" }
        assertNotNull(bobStats)
        assertEquals(2, bobStats!!.messageCount)
        
        val messageTypeStats = result.communicationPatterns.messageTypeDistribution
        assertTrue(messageTypeStats.containsKey(MessageType.TEXT))
        assertTrue(messageTypeStats.containsKey(MessageType.IMAGE))
        assertTrue(messageTypeStats.containsKey(MessageType.VOICE_NOTE))
    }

    @Test
    fun `should extract emojis from messages`() {
        val messages = listOf(
            WhatsAppMessage("Alice", Date(), "I'm so happy! 😊😄", MessageType.TEXT),
            WhatsAppMessage("Bob", Date(), "That's sad 😢", MessageType.TEXT),
            WhatsAppMessage("Alice", Date(), "Great job! 👍🎉", MessageType.TEXT)
        )

        val result = processor.analyzeMessages(messages)

        assertNotNull(result.phraseStats)
        
        // Find phrases with emoji hints
        val phrasesWithEmojis = result.phraseStats.filter { it.emojiHints.isNotEmpty() }
        assertTrue(phrasesWithEmojis.isNotEmpty())
        
        // Check that emojis were extracted
        val allEmojis = phrasesWithEmojis.flatMap { it.emojiHints }
        assertTrue(allEmojis.contains("😊"))
        assertTrue(allEmojis.contains("😄"))
        assertTrue(allEmojis.contains("😢"))
    }

    @Test
    fun `should handle empty message list`() {
        val messages = emptyList<WhatsAppMessage>()

        val result = processor.analyzeMessages(messages)

        assertNotNull(result)
        assertEquals(0, result.sentimentStats.totalMessages)
        assertTrue(result.phraseStats.isEmpty())
        assertTrue(result.communicationPatterns.senderStats.isEmpty())
    }

    @Test
    fun `should calculate average sentiment score`() {
        val messages = listOf(
            WhatsAppMessage("Alice", Date(), "happy happy", MessageType.TEXT),
            WhatsAppMessage("Bob", Date(), "sad sad", MessageType.TEXT),
            WhatsAppMessage("Alice", Date(), "neutral", MessageType.TEXT)
        )

        val result = processor.analyzeMessages(messages)

        assertNotNull(result.sentimentStats)
        val score = result.sentimentStats.averageSentimentScore
        assertTrue(score >= -1.0f && score <= 1.0f)
    }
}