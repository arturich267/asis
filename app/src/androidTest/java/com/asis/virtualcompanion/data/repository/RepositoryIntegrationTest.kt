package com.asis.virtualcompanion.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.AsisDatabase
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
class RepositoryIntegrationTest {
    
    private lateinit var database: AsisDatabase
    private lateinit var messageRepository: MessageRepositoryImpl
    private lateinit var phraseStatRepository: PhraseStatRepositoryImpl
    private lateinit var voiceRepository: VoiceRepositoryImpl
    private lateinit var chatMessageRepository: ChatMessageRepositoryImpl
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java
        ).allowMainThreadQueries().build()
        
        messageRepository = MessageRepositoryImpl(database.messageDao())
        phraseStatRepository = PhraseStatRepositoryImpl(database.phraseStatDao())
        voiceRepository = VoiceRepositoryImpl(database.voiceMetaDao())
        chatMessageRepository = ChatMessageRepositoryImpl(database.chatMessageDao())
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun messageRepository_fullWorkflow() = runTest {
        val message = MessageEntity(0, "user", Date(), "Hello", "positive", listOf("greeting"))
        
        val insertResult = messageRepository.insertMessage(message)
        assertTrue(insertResult is Result.Success)
        
        val countResult = messageRepository.getMessageCount()
        assertTrue(countResult is Result.Success)
        assertEquals(1, (countResult as Result.Success).data)
        
        val messages = messageRepository.getAllMessages().first()
        assertEquals(1, messages.size)
        assertEquals("Hello", messages[0].text)
    }
    
    @Test
    fun phraseStatRepository_fullWorkflow() = runTest {
        val phrase = PhraseStatEntity("hello", 5, listOf("👋"))
        
        val insertResult = phraseStatRepository.insertPhraseStat(phrase)
        assertTrue(insertResult is Result.Success)
        
        val incrementResult = phraseStatRepository.incrementPhraseCount("hello")
        assertTrue(incrementResult is Result.Success)
        assertTrue((incrementResult as Result.Success).data)
        
        val topPhrasesResult = phraseStatRepository.getTopPhrases(10)
        assertTrue(topPhrasesResult is Result.Success)
        val topPhrases = (topPhrasesResult as Result.Success).data
        assertEquals(1, topPhrases.size)
        assertEquals(6, topPhrases[0].count)
    }
    
    @Test
    fun voiceRepository_fullWorkflow() = runTest {
        val voiceMeta = VoiceMetaEntity("clip001", "happy", "greeting", "/path/clip001", 5000)
        
        val insertResult = voiceRepository.insertVoiceMeta(voiceMeta)
        assertTrue(insertResult is Result.Success)
        
        val getResult = voiceRepository.getVoiceMetaByClipId("clip001")
        assertTrue(getResult is Result.Success)
        assertNotNull((getResult as Result.Success).data)
        
        val countResult = voiceRepository.getVoiceMetaCount()
        assertTrue(countResult is Result.Success)
        assertEquals(1, (countResult as Result.Success).data)
        
        val durationResult = voiceRepository.getTotalDuration()
        assertTrue(durationResult is Result.Success)
        assertEquals(5000L, (durationResult as Result.Success).data)
    }
    
    @Test
    fun chatMessageRepository_fullWorkflow() = runTest {
        val message = ChatMessageEntity(0, "Hello!", true, Date(), null)
        
        val insertResult = chatMessageRepository.insertChatMessage(message)
        assertTrue(insertResult is Result.Success)
        
        val countResult = chatMessageRepository.getChatMessageCount()
        assertTrue(countResult is Result.Success)
        assertEquals(1, (countResult as Result.Success).data)
        
        val messages = chatMessageRepository.getAllChatMessages().first()
        assertEquals(1, messages.size)
        assertEquals("Hello!", messages[0].message)
    }
    
    @Test
    fun repositories_batchOperations() = runTest {
        val messages = listOf(
            MessageEntity(0, "user", Date(), "Message 1", null, emptyList()),
            MessageEntity(0, "user", Date(), "Message 2", null, emptyList())
        )
        val result = messageRepository.insertMessages(messages)
        assertTrue(result is Result.Success)
        
        val voiceMetaList = listOf(
            VoiceMetaEntity("clip1", "happy", "greeting", "/path/clip1", 1000),
            VoiceMetaEntity("clip2", "sad", "farewell", "/path/clip2", 2000)
        )
        val voiceResult = voiceRepository.insertVoiceMetaList(voiceMetaList)
        assertTrue(voiceResult is Result.Success)
        
        val chatMessages = listOf(
            ChatMessageEntity(0, "Chat 1", true, Date(), null),
            ChatMessageEntity(0, "Chat 2", false, Date(), null)
        )
        val chatResult = chatMessageRepository.insertChatMessages(chatMessages)
        assertTrue(chatResult is Result.Success)
    }
    
    @Test
    fun repositories_flowObservation() = runTest {
        messageRepository.insertMessage(MessageEntity(0, "user", Date(), "Test", null, emptyList()))
        
        val messagesFlow = messageRepository.getAllMessages()
        val messages = messagesFlow.first()
        assertEquals(1, messages.size)
        
        phraseStatRepository.insertPhraseStat(PhraseStatEntity("test", 1, emptyList()))
        
        val phrasesFlow = phraseStatRepository.getAllPhraseStats()
        val phrases = phrasesFlow.first()
        assertEquals(1, phrases.size)
    }
    
    @Test
    fun repositories_errorHandling() = runTest {
        database.close()
        
        val insertResult = messageRepository.insertMessage(
            MessageEntity(0, "user", Date(), "Test", null, emptyList())
        )
        assertTrue(insertResult is Result.Error)
    }
}
