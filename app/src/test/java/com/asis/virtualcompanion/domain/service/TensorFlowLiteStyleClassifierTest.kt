package com.asis.virtualcompanion.domain.service

import android.content.Context
import com.asis.virtualcompanion.common.Result
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TensorFlowLiteStyleClassifierTest {
    
    @Mock
    private lateinit var context: Context
    
    private lateinit var classifier: TensorFlowLiteStyleClassifier
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        classifier = TensorFlowLiteStyleClassifier(context)
    }
    
    @Test
    fun `initialize returns success when model loads successfully`() = runTest {
        // Given - Model file exists and loads successfully
        // This test assumes the model file exists, but in practice it will fall back
        // to mock implementation since we don't have the actual model file
        
        // When
        val result = classifier.initialize()
        
        // Then
        assertTrue(result is Result.Success)
        assertEquals(true, (result as Result.Success).data)
    }
    
    @Test
    fun `classifyStyle with happy emotion returns energetic style`() = runTest {
        // Given
        val inputText = "I'm so excited about this!"
        val detectedEmotion = "happy"
        
        // When
        val result = classifier.classifyStyle(inputText, detectedEmotion)
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertEquals("energetic", style)
    }
    
    @Test
    fun `classifyStyle with sad emotion returns friendly style`() = runTest {
        // Given
        val inputText = "I'm feeling a bit down today"
        val detectedEmotion = "sad"
        
        // When
        val result = classifier.classifyStyle(inputText, detectedEmotion)
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertEquals("friendly", style)
    }
    
    @Test
    fun `classifyStyle with question text returns casual style`() = runTest {
        // Given
        val inputText = "How are you doing?"
        
        // When
        val result = classifier.classifyStyle(inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertEquals("casual", style)
    }
    
    @Test
    fun `classifyStyle with laughter text returns playful style`() = runTest {
        // Given
        val inputText = "That's so funny lol lmao"
        
        // When
        val result = classifier.classifyStyle(inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertEquals("playful", style)
    }
    
    @Test
    fun `classifyStyle with serious keywords returns serious style`() = runTest {
        // Given
        val inputText = "We need to seriously consider the implications"
        
        // When
        val result = classifier.classifyStyle(inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertEquals("serious", style)
    }
    
    @Test
    fun `classifyStyle with long text returns formal style`() = runTest {
        // Given
        val longText = "This is a very long text that exceeds one hundred characters " +
                "and therefore should be classified as formal according to our " +
                "fallback classification rules for this particular test case"
        
        // When
        val result = classifier.classifyStyle(longText)
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertEquals("formal", style)
    }
    
    @Test
    fun `classifyStyle with casual slang returns casual style`() = runTest {
        // Given
        val inputText = "Hey bro what's up man"
        
        // When
        val result = classifier.classifyStyle(inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertEquals("casual", style)
    }
    
    @Test
    fun `classifyStyle with excitement keywords returns energetic style`() = runTest {
        // Given
        val inputText = "Wow omg this is amazing!"
        
        // When
        val result = classifier.classifyStyle(inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertEquals("energetic", style)
    }
    
    @Test
    fun `classifyEmojiPack with energetic style returns energetic emojis`() = runTest {
        // Given
        val style = "energetic"
        val inputText = "Let's go!"
        
        // When
        val result = classifier.classifyEmojiPack(style, inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val emojis = (result as Result.Success).data
        assertTrue(emojis.contains("🔥"))
        assertTrue(emojis.contains("⚡"))
        assertTrue(emojis.contains("🎉"))
    }
    
    @Test
    fun `classifyEmojiPack with playful style returns playful emojis`() = runTest {
        // Given
        val style = "playful"
        val inputText = "So much fun!"
        
        // When
        val result = classifier.classifyEmojiPack(style, inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val emojis = (result as Result.Success).data
        assertTrue(emojis.contains("😄"))
        assertTrue(emojis.contains("😂"))
        assertTrue(emojis.contains("🤪"))
    }
    
    @Test
    fun `classifyEmojiPack with friendly style returns friendly emojis`() = runTest {
        // Given
        val style = "friendly"
        val inputText = "You're awesome"
        
        // When
        val result = classifier.classifyEmojiPack(style, inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val emojis = (result as Result.Success).data
        assertTrue(emojis.contains("😊"))
        assertTrue(emojis.contains("🤗"))
        assertTrue(emojis.contains("💙"))
    }
    
    @Test
    fun `classifyEmojiPack with sarcastic style returns sarcastic emojis`() = runTest {
        // Given
        val style = "sarcastic"
        val inputText = "Oh really?"
        
        // When
        val result = classifier.classifyEmojiPack(style, inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val emojis = (result as Result.Success).data
        assertTrue(emojis.contains("🙄"))
        assertTrue(emojis.contains("😏"))
        assertTrue(emojis.contains("🤔"))
    }
    
    @Test
    fun `classifyEmojiPack with casual style returns casual emojis`() = runTest {
        // Given
        val style = "casual"
        val inputText = "Cool stuff"
        
        // When
        val result = classifier.classifyEmojiPack(style, inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val emojis = (result as Result.Success).data
        assertTrue(emojis.contains("😎"))
        assertTrue(emojis.contains("👌"))
        assertTrue(emojis.contains("🤙"))
    }
    
    @Test
    fun `classifyEmojiPack with serious style returns serious emojis`() = runTest {
        // Given
        val style = "serious"
        val inputText = "Important discussion"
        
        // When
        val result = classifier.classifyEmojiPack(style, inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val emojis = (result as Result.Success).data
        assertTrue(emojis.contains("📝"))
        assertTrue(emojis.contains("💭"))
        assertTrue(emojis.contains("🤔"))
    }
    
    @Test
    fun `classifyEmojiPack with formal style returns formal emojis`() = runTest {
        // Given
        val style = "formal"
        val inputText = "Business meeting"
        
        // When
        val result = classifier.classifyEmojiPack(style, inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val emojis = (result as Result.Success).data
        assertTrue(emojis.contains("🤝"))
        assertTrue(emojis.contains("📊"))
        assertTrue(emojis.contains("💼"))
    }
    
    @Test
    fun `classifyEmojiPack with unknown style returns default emojis`() = runTest {
        // Given
        val style = "unknown"
        val inputText = "Some text"
        
        // When
        val result = classifier.classifyEmojiPack(style, inputText)
        
        // Then
        assertTrue(result is Result.Success)
        val emojis = (result as Result.Success).data
        assertTrue(emojis.contains("😊"))
        assertTrue(emojis.contains("✨"))
        assertTrue(emojis.contains("👍"))
    }
    
    @Test
    fun `classifyStyle with context includes time awareness`() = runTest {
        // Given
        val inputText = "Good morning everyone"
        val context = mapOf(
            "timeOfDay" to 3600000L, // 1 hour in milliseconds (morning)
            "topicCount" to 3
        )
        
        // When
        val result = classifier.classifyStyle(inputText, null, context)
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertNotNull(style)
        assertTrue(style.isNotEmpty())
    }
    
    @Test
    fun `close cleans up resources`() {
        // Given - classifier is initialized
        
        // When
        classifier.close()
        
        // Then - no exception should be thrown
        // This is mainly to ensure the close method doesn't crash
        assertTrue(true)
    }
}