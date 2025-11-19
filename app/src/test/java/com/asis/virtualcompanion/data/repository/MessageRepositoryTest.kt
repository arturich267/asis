package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.dao.MessageDao
import com.asis.virtualcompanion.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class MessageRepositoryTest {
    
    private lateinit var messageDao: MessageDao
    private lateinit var repository: MessageRepositoryImpl
    
    @Before
    fun setup() {
        messageDao = mock()
        repository = MessageRepositoryImpl(messageDao)
    }
    
    @Test
    fun `getAllMessages returns flow from dao`() = runTest {
        val messages = listOf(
            MessageEntity(1, "user", Date(), "Hello", "positive", emptyList())
        )
        val flow = flowOf(messages)
        whenever(messageDao.getAllMessages()).thenReturn(flow)
        
        val result = repository.getAllMessages()
        
        assertEquals(flow, result)
        verify(messageDao).getAllMessages()
    }
    
    @Test
    fun `insertMessage returns success result`() = runTest {
        val message = MessageEntity(0, "user", Date(), "Test message", null, emptyList())
        val insertedId = 1L
        whenever(messageDao.insert(message)).thenReturn(insertedId)
        
        val result = repository.insertMessage(message)
        
        assertTrue(result is Result.Success)
        assertEquals(insertedId, (result as Result.Success).data)
        verify(messageDao).insert(message)
    }
    
    @Test
    fun `insertMessage returns error on exception`() = runTest {
        val message = MessageEntity(0, "user", Date(), "Test message", null, emptyList())
        val exception = RuntimeException("Database error")
        whenever(messageDao.insert(message)).thenThrow(exception)
        
        val result = repository.insertMessage(message)
        
        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }
    
    @Test
    fun `getRecentMessages returns success with data`() = runTest {
        val messages = listOf(
            MessageEntity(1, "user", Date(), "Message 1", null, emptyList()),
            MessageEntity(2, "assistant", Date(), "Message 2", null, emptyList())
        )
        whenever(messageDao.getRecentMessages(10)).thenReturn(messages)
        
        val result = repository.getRecentMessages(10)
        
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        verify(messageDao).getRecentMessages(10)
    }
    
    @Test
    fun `deleteMessagesBeforeDate returns success with count`() = runTest {
        val date = Date()
        val deletedCount = 5
        whenever(messageDao.deleteMessagesBeforeDate(date)).thenReturn(deletedCount)
        
        val result = repository.deleteMessagesBeforeDate(date)
        
        assertTrue(result is Result.Success)
        assertEquals(deletedCount, (result as Result.Success).data)
        verify(messageDao).deleteMessagesBeforeDate(date)
    }
}
