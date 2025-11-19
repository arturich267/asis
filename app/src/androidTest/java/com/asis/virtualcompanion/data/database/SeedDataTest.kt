package com.asis.virtualcompanion.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.data.database.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class SeedDataTest {
    
    private lateinit var database: AsisDatabase
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun seedMessageData() = runTest {
        val messages = listOf(
            MessageEntity(0, "user", Date(), "Hello!", "positive", listOf("greeting")),
            MessageEntity(0, "assistant", Date(), "Hi! How can I help you?", "positive", listOf("greeting")),
            MessageEntity(0, "user", Date(), "What's the weather like?", "neutral", listOf("question", "weather")),
            MessageEntity(0, "assistant", Date(), "It's sunny today!", "positive", listOf("weather", "information"))
        )
        
        val ids = database.messageDao().insertAll(messages)
        assertEquals(4, ids.size)
        
        val allMessages = database.messageDao().getAllMessages().first()
        assertEquals(4, allMessages.size)
        
        val userMessages = database.messageDao().getMessagesBySender("user").first()
        assertEquals(2, userMessages.size)
    }
    
    @Test
    fun seedPhraseStatData() = runTest {
        val phrases = listOf(
            PhraseStatEntity("hello", 25, listOf("👋", "😊")),
            PhraseStatEntity("thank you", 18, listOf("🙏", "😊")),
            PhraseStatEntity("goodbye", 15, listOf("👋", "😢")),
            PhraseStatEntity("yes", 30, listOf("✅", "👍")),
            PhraseStatEntity("no", 22, listOf("❌", "👎"))
        )
        
        database.phraseStatDao().insertAll(phrases)
        
        val topPhrases = database.phraseStatDao().getTopPhrases(3)
        assertEquals(3, topPhrases.size)
        assertEquals("yes", topPhrases[0].phrase)
        assertEquals(30, topPhrases[0].count)
    }
    
    @Test
    fun seedVoiceMetaData() = runTest {
        val voiceMeta = listOf(
            VoiceMetaEntity("clip001", "happy", "greeting", "/voice/clip001.wav", 3500),
            VoiceMetaEntity("clip002", "happy", "farewell", "/voice/clip002.wav", 3000),
            VoiceMetaEntity("clip003", "sad", "greeting", "/voice/clip003.wav", 4000),
            VoiceMetaEntity("clip004", "neutral", "question", "/voice/clip004.wav", 5500),
            VoiceMetaEntity("clip005", "excited", "information", "/voice/clip005.wav", 6000)
        )
        
        database.voiceMetaDao().insertAll(voiceMeta)
        
        val allVoices = database.voiceMetaDao().getAllVoiceMeta().first()
        assertEquals(5, allVoices.size)
        
        val happyVoices = database.voiceMetaDao().getVoiceMetaByEmotion("happy").first()
        assertEquals(2, happyVoices.size)
        
        val totalDuration = database.voiceMetaDao().getTotalDuration()
        assertEquals(22000L, totalDuration)
    }
    
    @Test
    fun seedChatMessageData() = runTest {
        val baseTime = System.currentTimeMillis()
        val chatMessages = listOf(
            ChatMessageEntity(0, "Hi there!", true, Date(baseTime), null),
            ChatMessageEntity(0, "Hello! How can I assist you today?", false, Date(baseTime + 1000), null),
            ChatMessageEntity(0, "I need help with something", true, Date(baseTime + 2000), mapOf("category" to "support")),
            ChatMessageEntity(0, "Of course! What do you need help with?", false, Date(baseTime + 3000), null),
            ChatMessageEntity(0, "Can you explain voice features?", true, Date(baseTime + 4000), mapOf("category" to "features")),
            ChatMessageEntity(0, "Voice features allow you to...", false, Date(baseTime + 5000), mapOf("response_type" to "explanation"))
        )
        
        database.chatMessageDao().insertBatch(chatMessages)
        
        val allMessages = database.chatMessageDao().getAllChatMessages().first()
        assertEquals(6, allMessages.size)
        
        val userMessages = database.chatMessageDao().getChatMessagesByType(true).first()
        assertEquals(3, userMessages.size)
        
        val assistantMessages = database.chatMessageDao().getChatMessagesByType(false).first()
        assertEquals(3, assistantMessages.size)
    }
    
    @Test
    fun seedThemePreferenceData() = runTest {
        val preferences = listOf(
            ThemePreferenceEntity("theme_id", "ocean", Date()),
            ThemePreferenceEntity("use_real_voice", "true", Date()),
            ThemePreferenceEntity("process_offline", "false", Date()),
            ThemePreferenceEntity("voice_speed", "1.0", Date())
        )
        
        database.themePreferenceDao().insertAll(preferences)
        
        val allPreferences = database.themePreferenceDao().getAllPreferences().first()
        assertEquals(4, allPreferences.size)
        
        val themePreference = database.themePreferenceDao().getPreferenceByKey("theme_id")
        assertNotNull(themePreference)
        assertEquals("ocean", themePreference?.preferenceValue)
    }
    
    @Test
    fun seedCompleteDatabase() = runTest {
        seedMessageData()
        seedPhraseStatData()
        seedVoiceMetaData()
        seedChatMessageData()
        seedThemePreferenceData()
        
        assertEquals(4, database.messageDao().getMessageCount())
        assertTrue(database.phraseStatDao().getTotalPhraseCount()!! > 0)
        assertEquals(5, database.voiceMetaDao().getVoiceMetaCount())
        assertEquals(6, database.chatMessageDao().getChatMessageCount())
    }
}
