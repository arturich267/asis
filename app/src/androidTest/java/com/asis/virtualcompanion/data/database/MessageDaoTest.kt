package com.asis.virtualcompanion.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.data.database.dao.MessageDao
import com.asis.virtualcompanion.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class MessageDaoTest {
    
    private lateinit var database: AsisDatabase
    private lateinit var messageDao: MessageDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java
        ).allowMainThreadQueries().build()
        
        messageDao = database.messageDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndGetMessage() = runTest {
        val message = MessageEntity(
            id = 0,
            sender = "user",
            timestamp = Date(),
            text = "Hello world",
            sentiment = "positive",
            contextTags = listOf("greeting", "test")
        )
        
        val id = messageDao.insert(message)
        assertTrue(id > 0)
        
        val messages = messageDao.getAllMessages().first()
        assertEquals(1, messages.size)
        assertEquals("Hello world", messages[0].text)
        assertEquals("user", messages[0].sender)
        assertEquals(2, messages[0].contextTags.size)
    }
    
    @Test
    fun getMessagesBySender() = runTest {
        messageDao.insert(MessageEntity(0, "user", Date(), "User message", null, emptyList()))
        messageDao.insert(MessageEntity(0, "assistant", Date(), "Assistant message", null, emptyList()))
        messageDao.insert(MessageEntity(0, "user", Date(), "Another user message", null, emptyList()))
        
        val userMessages = messageDao.getMessagesBySender("user").first()
        assertEquals(2, userMessages.size)
        
        val assistantMessages = messageDao.getMessagesBySender("assistant").first()
        assertEquals(1, assistantMessages.size)
    }
    
    @Test
    fun getRecentMessages() = runTest {
        for (i in 1..15) {
            messageDao.insert(MessageEntity(0, "user", Date(), "Message $i", null, emptyList()))
        }
        
        val recentMessages = messageDao.getRecentMessages(10)
        assertEquals(10, recentMessages.size)
    }
    
    @Test
    fun deleteMessagesBeforeDate() = runTest {
        val now = Date()
        val yesterday = Date(now.time - 24 * 60 * 60 * 1000)
        val tomorrow = Date(now.time + 24 * 60 * 60 * 1000)
        
        messageDao.insert(MessageEntity(0, "user", yesterday, "Old message", null, emptyList()))
        messageDao.insert(MessageEntity(0, "user", tomorrow, "New message", null, emptyList()))
        
        val deletedCount = messageDao.deleteMessagesBeforeDate(now)
        assertEquals(1, deletedCount)
        
        val remainingMessages = messageDao.getAllMessages().first()
        assertEquals(1, remainingMessages.size)
        assertEquals("New message", remainingMessages[0].text)
    }
    
    @Test
    fun getMessageCount() = runTest {
        assertEquals(0, messageDao.getMessageCount())
        
        messageDao.insert(MessageEntity(0, "user", Date(), "Message 1", null, emptyList()))
        messageDao.insert(MessageEntity(0, "user", Date(), "Message 2", null, emptyList()))
        
        assertEquals(2, messageDao.getMessageCount())
    }
    
    @Test
    fun updateMessage() = runTest {
        val message = MessageEntity(0, "user", Date(), "Original text", null, emptyList())
        val id = messageDao.insert(message)
        
        val updatedMessage = message.copy(id = id, text = "Updated text", sentiment = "positive")
        messageDao.update(updatedMessage)
        
        val messages = messageDao.getAllMessages().first()
        assertEquals(1, messages.size)
        assertEquals("Updated text", messages[0].text)
        assertEquals("positive", messages[0].sentiment)
    }
}
