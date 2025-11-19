package com.asis.virtualcompanion.domain.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface MessageRepository : BaseRepository {
    
    fun getAllMessages(): Flow<List<MessageEntity>>
    
    fun getMessagesBySender(sender: String): Flow<List<MessageEntity>>
    
    fun getMessagesAfterDate(startDate: Date): Flow<List<MessageEntity>>
    
    fun getMessagesBySentiment(sentiment: String): Flow<List<MessageEntity>>
    
    suspend fun getRecentMessages(limit: Int): Result<List<MessageEntity>>
    
    suspend fun insertMessage(message: MessageEntity): Result<Long>
    
    suspend fun insertMessages(messages: List<MessageEntity>): Result<List<Long>>
    
    suspend fun updateMessage(message: MessageEntity): Result<Unit>
    
    suspend fun deleteMessage(message: MessageEntity): Result<Unit>
    
    suspend fun deleteMessagesBeforeDate(beforeDate: Date): Result<Int>
    
    suspend fun getMessageCount(): Result<Int>
}
