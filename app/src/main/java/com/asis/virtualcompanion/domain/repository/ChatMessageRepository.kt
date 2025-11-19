package com.asis.virtualcompanion.domain.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface ChatMessageRepository : BaseRepository {
    
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>
    
    suspend fun getRecentChatMessages(limit: Int): Result<List<ChatMessageEntity>>
    
    fun getChatMessagesByType(isFromUser: Boolean): Flow<List<ChatMessageEntity>>
    
    fun getChatMessagesAfterDate(startDate: Date): Flow<List<ChatMessageEntity>>
    
    suspend fun insertChatMessage(message: ChatMessageEntity): Result<Long>
    
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>): Result<List<Long>>
    
    suspend fun updateChatMessage(message: ChatMessageEntity): Result<Unit>
    
    suspend fun deleteChatMessage(message: ChatMessageEntity): Result<Unit>
    
    suspend fun deleteChatMessagesBeforeDate(beforeDate: Date): Result<Int>
    
    suspend fun deleteAllChatMessages(): Result<Int>
    
    suspend fun getChatMessageCount(): Result<Int>
}
