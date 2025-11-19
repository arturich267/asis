package com.asis.virtualcompanion.domain

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.entity.PhraseStatEntity
import com.asis.virtualcompanion.data.model.*
import com.asis.virtualcompanion.domain.repository.ConversationTopicRepository
import com.asis.virtualcompanion.domain.repository.PhraseStatRepository
import com.asis.virtualcompanion.domain.repository.SpeakerStyleRepository
import com.asis.virtualcompanion.domain.repository.ThemeRepository
import com.asis.virtualcompanion.domain.service.TensorFlowLiteStyleClassifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemeGeneratorTest {
    
    @Mock
    private lateinit var phraseStatRepository: PhraseStatRepository
    
    @Mock
    private lateinit var speakerStyleRepository: SpeakerStyleRepository
    
    @Mock
    private lateinit var conversationTopicRepository: ConversationTopicRepository
    
    @Mock
    private lateinit var themeRepository: ThemeRepository
    
    @Mock
    private lateinit var styleClassifier: TensorFlowLiteStyleClassifier
    
    private lateinit var memeGenerator: MemeGenerator
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        memeGenerator = MemeGenerator(
            phraseStatRepository,
            speakerStyleRepository,
            conversationTopicRepository,
            themeRepository,
            styleClassifier
        )
    }
    
    @Test
    fun `generateMemeResponse with random mode returns valid response`() = runTest {
        // Given
        val topPhrases = listOf(
            PhraseStatEntity("hello world", 5, listOf("😊")),
            PhraseStatEntity("how are you", 3, listOf("👋")),
            PhraseStatEntity("good morning", 4, listOf("☀️"))
        )
        
        val speakerStyle = SpeakerStyle(
            id = "casual",
            name = "Casual",
            characteristics = listOf("relaxed"),
            emojiPreferences = listOf("😎", "👌"),
            slangTerms = listOf("bro", "dude"),
            repetitionPatterns = listOf("you know")
        )
        
        val config = MemeGenerationConfig(
            mode = MemeMode.RANDOM,
            seed = 12345L,
            variability = 0.3f
        )
        
        `when`(phraseStatRepository.getTopPhrases(20))
            .thenReturn(Result.Success(topPhrases))
        
        `when`(conversationTopicRepository.getRecentTopics(5))
            .thenReturn(Result.Success(emptyList()))
        
        `when`(speakerStyleRepository.getAllSpeakerStyles())
            .thenReturn(MutableStateFlow(listOf(speakerStyle)))
        
        `when`(speakerStyleRepository.getSpeakerStyleByName("Casual"))
            .thenReturn(Result.Success(speakerStyle))
        
        `when`(styleClassifier.classifyEmojiPack("casual", "hello world"))
            .thenReturn(Result.Success(listOf("😊", "✨", "👍")))
        
        // When
        val result = memeGenerator.generateMemeResponse(config)
        
        // Then
        assertTrue(result is Result.Success)
        val response = (result as Result.Success).data
        assertNotNull(response.text)
        assertNotNull(response.template)
        assertTrue(response.confidence > 0f)
        assertEquals("hello world", response.template.phrase)
        assertTrue(response.template.emojiReferences.isNotEmpty())
    }
    
    @Test
    fun `generateMemeResponse with emotion mode uses emotion classification`() = runTest {
        // Given
        val topPhrases = listOf(
            PhraseStatEntity("feeling great", 5, listOf("😄"))
        )
        
        val recentTopics = listOf(
            ConversationTopic(
                id = "topic1",
                topic = "happy moments",
                keywords = listOf("happy", "joy"),
                timestamp = System.currentTimeMillis()
            )
        )
        
        val config = MemeGenerationConfig(
            mode = MemeMode.RESPOND_TO_EMOTION,
            detectedEmotion = "happy",
            variability = 0.2f
        )
        
        `when`(phraseStatRepository.getTopPhrases(20))
            .thenReturn(Result.Success(topPhrases))
        
        `when`(conversationTopicRepository.getRecentTopics(5))
            .thenReturn(Result.Success(recentTopics))
        
        `when`(styleClassifier.classifyStyle("happy moments", "happy", any()))
            .thenReturn(Result.Success("energetic"))
        
        `when`(speakerStyleRepository.getSpeakerStyleByName("energetic"))
            .thenReturn(Result.Success(null))
        
        `when`(styleClassifier.classifyEmojiPack("energetic", "feeling great"))
            .thenReturn(Result.Success(listOf("🔥", "⚡", "🎉")))
        
        // When
        val result = memeGenerator.generateMemeResponse(config)
        
        // Then
        assertTrue(result is Result.Success)
        val response = (result as Result.Success).data
        assertEquals("feeling great", response.template.phrase)
        assertEquals("default_energetic", response.template.styleId)
        verify(styleClassifier).classifyStyle("happy moments", "happy", any())
    }
    
    @Test
    fun `generateMemeResponse with context mode uses time-based classification`() = runTest {
        // Given
        val topPhrases = listOf(
            PhraseStatEntity("late night thoughts", 2, listOf("🌙"))
        )
        
        val recentTopics = listOf(
            ConversationTopic(
                id = "topic1",
                topic = "night routine",
                keywords = listOf("night", "sleep"),
                timestamp = System.currentTimeMillis()
            )
        )
        
        val currentTime = System.currentTimeMillis()
        val config = MemeGenerationConfig(
            mode = MemeMode.CONTEXT_AWARE,
            currentTime = currentTime
        )
        
        `when`(phraseStatRepository.getTopPhrases(20))
            .thenReturn(Result.Success(topPhrases))
        
        `when`(conversationTopicRepository.getRecentTopics(5))
            .thenReturn(Result.Success(recentTopics))
        
        `when`(styleClassifier.classifyStyle("night routine", null, any()))
            .thenReturn(Result.Success("friendly"))
        
        `when`(speakerStyleRepository.getSpeakerStyleByName("friendly"))
            .thenReturn(Result.Success(null))
        
        `when`(styleClassifier.classifyEmojiPack("friendly", "late night thoughts"))
            .thenReturn(Result.Success(listOf("😊", "🤗", "💙")))
        
        // When
        val result = memeGenerator.generateMemeResponse(config)
        
        // Then
        assertTrue(result is Result.Success)
        val response = (result as Result.Success).data
        assertEquals("late night thoughts", response.template.phrase)
        verify(styleClassifier).classifyStyle("night routine", null, any())
    }
    
    @Test
    fun `generateMemeResponse with audio instructions includes audio data`() = runTest {
        // Given
        val topPhrases = listOf(
            PhraseStatEntity("excited about this", 3, listOf("🎉"))
        )
        
        val speakerStyle = SpeakerStyle(
            id = "energetic",
            name = "Energetic",
            characteristics = listOf("high-energy"),
            emojiPreferences = listOf("🔥", "⚡"),
            slangTerms = listOf("lit", "fire"),
            repetitionPatterns = listOf("let's go")
        )
        
        val config = MemeGenerationConfig(
            mode = MemeMode.RANDOM,
            includeAudio = true,
            variability = 0.1f
        )
        
        `when`(phraseStatRepository.getTopPhrases(20))
            .thenReturn(Result.Success(topPhrases))
        
        `when`(conversationTopicRepository.getRecentTopics(5))
            .thenReturn(Result.Success(emptyList()))
        
        `when`(speakerStyleRepository.getAllSpeakerStyles())
            .thenReturn(MutableStateFlow(listOf(speakerStyle)))
        
        `when`(speakerStyleRepository.getSpeakerStyleByName("Energetic"))
            .thenReturn(Result.Success(speakerStyle))
        
        `when`(styleClassifier.classifyEmojiPack("energetic", "excited about this"))
            .thenReturn(Result.Success(listOf("🎉", "🔥", "⚡")))
        
        // When
        val result = memeGenerator.generateMemeResponse(config)
        
        // Then
        assertTrue(result is Result.Success)
        val response = (result as Result.Success).data
        assertNotNull(response.audioInstructions)
        assertTrue(response.audioInstructions!!.contains("Tone:"))
        assertTrue(response.audioInstructions!!.contains("upbeat"))
    }
    
    @Test
    fun `generateMemeResponse detects repetition in phrases`() = runTest {
        // Given
        val topPhrases = listOf(
            PhraseStatEntity("again again and again", 4, listOf("🔄"))
        )
        
        val config = MemeGenerationConfig(
            mode = MemeMode.RANDOM,
            variability = 0.0f
        )
        
        `when`(phraseStatRepository.getTopPhrases(20))
            .thenReturn(Result.Success(topPhrases))
        
        `when`(conversationTopicRepository.getRecentTopics(5))
            .thenReturn(Result.Success(emptyList()))
        
        `when`(speakerStyleRepository.getAllSpeakerStyles())
            .thenReturn(MutableStateFlow(emptyList()))
        
        `when`(speakerStyleRepository.getSpeakerStyleByName(any()))
            .thenReturn(Result.Success(null))
        
        `when`(styleClassifier.classifyEmojiPack(any(), any()))
            .thenReturn(Result.Success(listOf("😊")))
        
        // When
        val result = memeGenerator.generateMemeResponse(config)
        
        // Then
        assertTrue(result is Result.Success)
        val response = (result as Result.Success).data
        assertTrue(response.template.memeSuffix.contains("again") || 
                  response.template.memeSuffix.contains("repeat"))
    }
    
    @Test
    fun `generateMemeResponse handles repository errors gracefully`() = runTest {
        // Given
        val config = MemeGenerationConfig(mode = MemeMode.RANDOM)
        
        `when`(phraseStatRepository.getTopPhrases(20))
            .thenReturn(Result.Error(Exception("Database error")))
        
        // When
        val result = memeGenerator.generateMemeResponse(config)
        
        // Then
        assertTrue(result is Result.Error)
        assertEquals("Database error", (result as Result.Error).exception.message)
    }
    
    @Test
    fun `generateMemeResponse with seeded RNG produces consistent results`() = runTest {
        // Given
        val topPhrases = listOf(
            PhraseStatEntity("test phrase", 1, listOf("🧪")),
            PhraseStatEntity("another phrase", 1, listOf("💭"))
        )
        
        val config1 = MemeGenerationConfig(
            mode = MemeMode.RANDOM,
            seed = 99999L,
            variability = 0.0f
        )
        
        val config2 = MemeGenerationConfig(
            mode = MemeMode.RANDOM,
            seed = 99999L,
            variability = 0.0f
        )
        
        `when`(phraseStatRepository.getTopPhrases(20))
            .thenReturn(Result.Success(topPhrases))
        
        `when`(conversationTopicRepository.getRecentTopics(5))
            .thenReturn(Result.Success(emptyList()))
        
        `when`(speakerStyleRepository.getAllSpeakerStyles())
            .thenReturn(MutableStateFlow(emptyList()))
        
        `when`(speakerStyleRepository.getSpeakerStyleByName(any()))
            .thenReturn(Result.Success(null))
        
        `when`(styleClassifier.classifyEmojiPack(any(), any()))
            .thenReturn(Result.Success(listOf("😊")))
        
        // When
        val result1 = memeGenerator.generateMemeResponse(config1)
        val result2 = memeGenerator.generateMemeResponse(config2)
        
        // Then
        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        val response1 = (result1 as Result.Success).data
        val response2 = (result2 as Result.Success).data
        
        // With same seed, results should be consistent
        assertEquals(response1.template.phrase, response2.template.phrase)
    }
    
    @Test
    fun `generateMemeResponseSync returns same result as async version`() = runTest {
        // Given
        val topPhrases = listOf(
            PhraseStatEntity("sync test", 2, listOf("⚡"))
        )
        
        val config = MemeGenerationConfig(
            mode = MemeMode.RANDOM,
            variability = 0.0f
        )
        
        `when`(phraseStatRepository.getTopPhrases(20))
            .thenReturn(Result.Success(topPhrases))
        
        `when`(conversationTopicRepository.getRecentTopics(5))
            .thenReturn(Result.Success(emptyList()))
        
        `when`(speakerStyleRepository.getAllSpeakerStyles())
            .thenReturn(MutableStateFlow(emptyList()))
        
        `when`(speakerStyleRepository.getSpeakerStyleByName(any()))
            .thenReturn(Result.Success(null))
        
        `when`(styleClassifier.classifyEmojiPack(any(), any()))
            .thenReturn(Result.Success(listOf("😊")))
        
        // When
        val asyncResult = memeGenerator.generateMemeResponse(config)
        val syncResult = memeGenerator.generateMemeResponseSync(config)
        
        // Then
        assertTrue(asyncResult is Result.Success)
        assertTrue(syncResult is Result.Success)
        val asyncResponse = (asyncResult as Result.Success).data
        val syncResponse = (syncResult as Result.Success).data
        
        assertEquals(asyncResponse.text, syncResponse.text)
        assertEquals(asyncResponse.template.phrase, syncResponse.template.phrase)
    }
    
    @Test
    fun `generateMemeResponse calculates confidence correctly`() = runTest {
        // Given
        val topPhrases = listOf(
            PhraseStatEntity("popular phrase", 10, listOf("🔥")),
            PhraseStatEntity("unpopular phrase", 1, listOf("👻"))
        )
        
        val recentTopics = listOf(
            ConversationTopic(
                id = "topic1",
                topic = "relevant topic",
                keywords = listOf("relevant"),
                timestamp = System.currentTimeMillis()
            )
        )
        
        val speakerStyle = SpeakerStyle(
            id = "custom",
            name = "Custom",
            characteristics = listOf("test"),
            emojiPreferences = listOf("🧪"),
            slangTerms = listOf("test"),
            repetitionPatterns = listOf("testing")
        )
        
        val config = MemeGenerationConfig(
            mode = MemeMode.RANDOM,
            variability = 0.0f
        )
        
        `when`(phraseStatRepository.getTopPhrases(20))
            .thenReturn(Result.Success(topPhrases))
        
        `when`(conversationTopicRepository.getRecentTopics(5))
            .thenReturn(Result.Success(recentTopics))
        
        `when`(speakerStyleRepository.getAllSpeakerStyles())
            .thenReturn(MutableStateFlow(listOf(speakerStyle)))
        
        `when`(speakerStyleRepository.getSpeakerStyleByName("Custom"))
            .thenReturn(Result.Success(speakerStyle))
        
        `when`(styleClassifier.classifyEmojiPack("custom", "popular phrase"))
            .thenReturn(Result.Success(listOf("🔥")))
        
        // When
        val result = memeGenerator.generateMemeResponse(config)
        
        // Then
        assertTrue(result is Result.Success)
        val response = (result as Result.Success).data
        
        // Confidence should be higher for popular phrases with topics and custom styles
        assertTrue(response.confidence > 0.6f)
        assertTrue(response.confidence <= 1.0f)
    }
}