package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.dao.ChatMessageDao
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class ChatMessageRepositoryTest {
    
    private lateinit var chatMessageDao: ChatMessageDao
    private lateinit var repository: ChatMessageRepositoryImpl
    
    @Before
    fun setup() {
        chatMessageDao = mock()
        repository = ChatMessageRepositoryImpl(chatMessageDao)
    }
    
    @Test
    fun `getAllChatMessages returns flow from dao`() = runTest {
        val messages = listOf(
            ChatMessageEntity(1, "Hello", true, Date(), null)
        )
        val flow = flowOf(messages)
        whenever(chatMessageDao.getAllChatMessages()).thenReturn(flow)
        
        val result = repository.getAllChatMessages()
        
        assertEquals(flow, result)
        verify(chatMessageDao).getAllChatMessages()
    }
    
    @Test
    fun `insertChatMessage returns success result`() = runTest {
        val message = ChatMessageEntity(0, "Test message", true, Date(), null)
        val insertedId = 1L
        whenever(chatMessageDao.insert(message)).thenReturn(insertedId)
        
        val result = repository.insertChatMessage(message)
        
        assertTrue(result is Result.Success)
        assertEquals(insertedId, (result as Result.Success).data)
        verify(chatMessageDao).insert(message)
    }
    
    @Test
    fun `getRecentChatMessages returns success with data`() = runTest {
        val messages = listOf(
            ChatMessageEntity(1, "Message 1", true, Date(), null),
            ChatMessageEntity(2, "Message 2", false, Date(), null)
        )
        whenever(chatMessageDao.getRecentChatMessages(10)).thenReturn(messages)
        
        val result = repository.getRecentChatMessages(10)
        
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        verify(chatMessageDao).getRecentChatMessages(10)
    }
    
    @Test
    fun `deleteAllChatMessages returns success with count`() = runTest {
        val deletedCount = 10
        whenever(chatMessageDao.deleteAllChatMessages()).thenReturn(deletedCount)
        
        val result = repository.deleteAllChatMessages()
        
        assertTrue(result is Result.Success)
        assertEquals(deletedCount, (result as Result.Success).data)
        verify(chatMessageDao).deleteAllChatMessages()
    }
    
    @Test
    fun `getChatMessageCount returns success with count`() = runTest {
        val count = 15
        whenever(chatMessageDao.getChatMessageCount()).thenReturn(count)
        
        val result = repository.getChatMessageCount()
        
        assertTrue(result is Result.Success)
        assertEquals(count, (result as Result.Success).data)
        verify(chatMessageDao).getChatMessageCount()
    }
}
