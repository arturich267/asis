package com.asis.virtualcompanion.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.data.database.dao.ChatMessageDao
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ChatMessageDaoTest {
    
    private lateinit var database: AsisDatabase
    private lateinit var chatMessageDao: ChatMessageDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java
        ).allowMainThreadQueries().build()
        
        chatMessageDao = database.chatMessageDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndGetChatMessage() = runTest {
        val message = ChatMessageEntity(
            id = 0,
            message = "Hello, how are you?",
            isFromUser = true,
            timestamp = Date(),
            metadata = mapOf("sentiment" to "positive")
        )
        
        val id = chatMessageDao.insert(message)
        assertTrue(id > 0)
        
        val messages = chatMessageDao.getAllChatMessages().first()
        assertEquals(1, messages.size)
        assertEquals("Hello, how are you?", messages[0].message)
        assertTrue(messages[0].isFromUser)
        assertEquals("positive", messages[0].metadata?.get("sentiment"))
    }
    
    @Test
    fun getChatMessagesByType() = runTest {
        chatMessageDao.insert(ChatMessageEntity(0, "User message 1", true, Date(), null))
        chatMessageDao.insert(ChatMessageEntity(0, "Assistant message", false, Date(), null))
        chatMessageDao.insert(ChatMessageEntity(0, "User message 2", true, Date(), null))
        
        val userMessages = chatMessageDao.getChatMessagesByType(true).first()
        assertEquals(2, userMessages.size)
        
        val assistantMessages = chatMessageDao.getChatMessagesByType(false).first()
        assertEquals(1, assistantMessages.size)
    }
    
    @Test
    fun getRecentChatMessages() = runTest {
        for (i in 1..15) {
            chatMessageDao.insert(ChatMessageEntity(0, "Message $i", true, Date(), null))
            Thread.sleep(10)
        }
        
        val recentMessages = chatMessageDao.getRecentChatMessages(5)
        assertEquals(5, recentMessages.size)
    }
    
    @Test
    fun insertBatch() = runTest {
        val messages = listOf(
            ChatMessageEntity(0, "Message 1", true, Date(), null),
            ChatMessageEntity(0, "Message 2", false, Date(), null),
            ChatMessageEntity(0, "Message 3", true, Date(), null)
        )
        
        chatMessageDao.insertBatch(messages)
        
        val allMessages = chatMessageDao.getAllChatMessages().first()
        assertEquals(3, allMessages.size)
    }
    
    @Test
    fun deleteChatMessagesBeforeDate() = runTest {
        val now = Date()
        val yesterday = Date(now.time - 24 * 60 * 60 * 1000)
        val tomorrow = Date(now.time + 24 * 60 * 60 * 1000)
        
        chatMessageDao.insert(ChatMessageEntity(0, "Old message", true, yesterday, null))
        chatMessageDao.insert(ChatMessageEntity(0, "New message", true, tomorrow, null))
        
        val deletedCount = chatMessageDao.deleteChatMessagesBeforeDate(now)
        assertEquals(1, deletedCount)
        
        val remainingMessages = chatMessageDao.getAllChatMessages().first()
        assertEquals(1, remainingMessages.size)
        assertEquals("New message", remainingMessages[0].message)
    }
    
    @Test
    fun deleteAllChatMessages() = runTest {
        chatMessageDao.insert(ChatMessageEntity(0, "Message 1", true, Date(), null))
        chatMessageDao.insert(ChatMessageEntity(0, "Message 2", false, Date(), null))
        
        val deletedCount = chatMessageDao.deleteAllChatMessages()
        assertEquals(2, deletedCount)
        
        val messages = chatMessageDao.getAllChatMessages().first()
        assertEquals(0, messages.size)
    }
    
    @Test
    fun getChatMessageCount() = runTest {
        assertEquals(0, chatMessageDao.getChatMessageCount())
        
        chatMessageDao.insert(ChatMessageEntity(0, "Message 1", true, Date(), null))
        chatMessageDao.insert(ChatMessageEntity(0, "Message 2", false, Date(), null))
        
        assertEquals(2, chatMessageDao.getChatMessageCount())
    }
    
    @Test
    fun updateChatMessage() = runTest {
        val message = ChatMessageEntity(0, "Original message", true, Date(), null)
        val id = chatMessageDao.insert(message)
        
        val updatedMessage = message.copy(
            id = id,
            message = "Updated message",
            metadata = mapOf("edited" to "true")
        )
        chatMessageDao.update(updatedMessage)
        
        val messages = chatMessageDao.getAllChatMessages().first()
        assertEquals(1, messages.size)
        assertEquals("Updated message", messages[0].message)
        assertEquals("true", messages[0].metadata?.get("edited"))
    }
}
