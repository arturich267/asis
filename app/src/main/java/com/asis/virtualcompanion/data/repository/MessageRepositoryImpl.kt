package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.dao.MessageDao
import com.asis.virtualcompanion.data.database.entity.MessageEntity
import com.asis.virtualcompanion.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class MessageRepositoryImpl(
    private val messageDao: MessageDao
) : BaseRepositoryImpl(), MessageRepository {
    
    override fun getAllMessages(): Flow<List<MessageEntity>> {
        return messageDao.getAllMessages()
    }
    
    override fun getMessagesBySender(sender: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesBySender(sender)
    }
    
    override fun getMessagesAfterDate(startDate: Date): Flow<List<MessageEntity>> {
        return messageDao.getMessagesAfterDate(startDate)
    }
    
    override fun getMessagesBySentiment(sentiment: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesBySentiment(sentiment)
    }
    
    override suspend fun getRecentMessages(limit: Int): Result<List<MessageEntity>> {
        return safeCall {
            messageDao.getRecentMessages(limit)
        }
    }
    
    override suspend fun insertMessage(message: MessageEntity): Result<Long> {
        return safeCall {
            messageDao.insert(message)
        }
    }
    
    override suspend fun insertMessages(messages: List<MessageEntity>): Result<List<Long>> {
        return safeCall {
            messageDao.insertAll(messages)
        }
    }
    
    override suspend fun updateMessage(message: MessageEntity): Result<Unit> {
        return safeCall {
            messageDao.update(message)
        }
    }
    
    override suspend fun deleteMessage(message: MessageEntity): Result<Unit> {
        return safeCall {
            messageDao.delete(message)
        }
    }
    
    override suspend fun deleteMessagesBeforeDate(beforeDate: Date): Result<Int> {
        return safeCall {
            messageDao.deleteMessagesBeforeDate(beforeDate)
        }
    }
    
    override suspend fun getMessageCount(): Result<Int> {
        return safeCall {
            messageDao.getMessageCount()
        }
    }
}
