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
class DatabaseSchemaTest {
    
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
    fun verifyMessageEntitySchema() = runTest {
        val message = MessageEntity(
            id = 0,
            sender = "user",
            timestamp = Date(),
            text = "Test message",
            sentiment = "positive",
            contextTags = listOf("tag1", "tag2")
        )
        
        val id = database.messageDao().insert(message)
        val retrieved = database.messageDao().getRecentMessages(1).firstOrNull()
        
        assertNotNull(retrieved)
        assertEquals("user", retrieved?.sender)
        assertEquals("Test message", retrieved?.text)
        assertEquals("positive", retrieved?.sentiment)
        assertEquals(2, retrieved?.contextTags?.size)
    }
    
    @Test
    fun verifyPhraseStatEntitySchema() = runTest {
        val phraseStat = PhraseStatEntity(
            phrase = "hello",
            count = 10,
            emojiHints = listOf("👋", "😊")
        )
        
        database.phraseStatDao().insert(phraseStat)
        val retrieved = database.phraseStatDao().getPhraseStatByPhrase("hello")
        
        assertNotNull(retrieved)
        assertEquals("hello", retrieved?.phrase)
        assertEquals(10, retrieved?.count)
        assertEquals(2, retrieved?.emojiHints?.size)
    }
    
    @Test
    fun verifyVoiceMetaEntitySchema() = runTest {
        val voiceMeta = VoiceMetaEntity(
            clipId = "clip001",
            emotion = "happy",
            topic = "greeting",
            filePath = "/path/to/clip.wav",
            duration = 5000
        )
        
        database.voiceMetaDao().insert(voiceMeta)
        val retrieved = database.voiceMetaDao().getVoiceMetaByClipId("clip001")
        
        assertNotNull(retrieved)
        assertEquals("clip001", retrieved?.clipId)
        assertEquals("happy", retrieved?.emotion)
        assertEquals("greeting", retrieved?.topic)
        assertEquals("/path/to/clip.wav", retrieved?.filePath)
        assertEquals(5000L, retrieved?.duration)
    }
    
    @Test
    fun verifyChatMessageEntitySchema() = runTest {
        val chatMessage = ChatMessageEntity(
            id = 0,
            message = "Hello!",
            isFromUser = true,
            timestamp = Date(),
            metadata = mapOf("key1" to "value1", "key2" to "value2")
        )
        
        val id = database.chatMessageDao().insert(chatMessage)
        val retrieved = database.chatMessageDao().getAllChatMessages().first().firstOrNull()
        
        assertNotNull(retrieved)
        assertEquals("Hello!", retrieved?.message)
        assertTrue(retrieved?.isFromUser == true)
        assertEquals(2, retrieved?.metadata?.size)
    }
    
    @Test
    fun verifyThemePreferenceEntitySchema() = runTest {
        val preference = ThemePreferenceEntity(
            preferenceKey = "theme_id",
            preferenceValue = "ocean",
            updatedAt = Date()
        )
        
        database.themePreferenceDao().insert(preference)
        val retrieved = database.themePreferenceDao().getPreferenceByKey("theme_id")
        
        assertNotNull(retrieved)
        assertEquals("theme_id", retrieved?.preferenceKey)
        assertEquals("ocean", retrieved?.preferenceValue)
    }
    
    @Test
    fun verifyIndexesWork() = runTest {
        for (i in 1..100) {
            database.messageDao().insert(
                MessageEntity(0, "user$i", Date(), "Message $i", "positive", emptyList())
            )
        }
        
        val userMessages = database.messageDao().getMessagesBySender("user50").first()
        assertEquals(1, userMessages.size)
        
        val positiveMessages = database.messageDao().getMessagesBySentiment("positive").first()
        assertEquals(100, positiveMessages.size)
    }
    
    @Test
    fun verifyTypeConvertersWork() = runTest {
        val now = Date()
        val message = MessageEntity(
            id = 0,
            sender = "user",
            timestamp = now,
            text = "Test",
            sentiment = null,
            contextTags = listOf("tag1", "tag2", "tag3")
        )
        
        database.messageDao().insert(message)
        val retrieved = database.messageDao().getRecentMessages(1).firstOrNull()
        
        assertNotNull(retrieved)
        assertNotNull(retrieved?.timestamp)
        assertEquals(3, retrieved?.contextTags?.size)
        assertTrue(retrieved?.contextTags?.contains("tag1") == true)
        
        val chatMessage = ChatMessageEntity(
            id = 0,
            message = "Test",
            isFromUser = true,
            timestamp = now,
            metadata = mapOf("key" to "value")
        )
        
        database.chatMessageDao().insert(chatMessage)
        val retrievedChat = database.chatMessageDao().getAllChatMessages().first().firstOrNull()
        
        assertNotNull(retrievedChat)
        assertEquals("value", retrievedChat?.metadata?.get("key"))
    }
}
