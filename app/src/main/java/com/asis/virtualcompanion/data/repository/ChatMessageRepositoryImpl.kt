package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.dao.ChatMessageDao
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import com.asis.virtualcompanion.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class ChatMessageRepositoryImpl(
    private val chatMessageDao: ChatMessageDao
) : BaseRepositoryImpl(), ChatMessageRepository {
    
    override fun getAllChatMessages(): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getAllChatMessages()
    }
    
    override suspend fun getRecentChatMessages(limit: Int): Result<List<ChatMessageEntity>> {
        return safeCall {
            chatMessageDao.getRecentChatMessages(limit)
        }
    }
    
    override fun getChatMessagesByType(isFromUser: Boolean): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getChatMessagesByType(isFromUser)
    }
    
    override fun getChatMessagesAfterDate(startDate: Date): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getChatMessagesAfterDate(startDate)
    }
    
    override suspend fun insertChatMessage(message: ChatMessageEntity): Result<Long> {
        return safeCall {
            chatMessageDao.insert(message)
        }
    }
    
    override suspend fun insertChatMessages(messages: List<ChatMessageEntity>): Result<List<Long>> {
        return safeCall {
            chatMessageDao.insertBatch(messages)
            messages.map { 0L }
        }
    }
    
    override suspend fun updateChatMessage(message: ChatMessageEntity): Result<Unit> {
        return safeCall {
            chatMessageDao.update(message)
        }
    }
    
    override suspend fun deleteChatMessage(message: ChatMessageEntity): Result<Unit> {
        return safeCall {
            chatMessageDao.delete(message)
        }
    }
    
    override suspend fun deleteChatMessagesBeforeDate(beforeDate: Date): Result<Int> {
        return safeCall {
            chatMessageDao.deleteChatMessagesBeforeDate(beforeDate)
        }
    }
    
    override suspend fun deleteAllChatMessages(): Result<Int> {
        return safeCall {
            chatMessageDao.deleteAllChatMessages()
        }
    }
    
    override suspend fun getChatMessageCount(): Result<Int> {
        return safeCall {
            chatMessageDao.getChatMessageCount()
        }
    }
}
