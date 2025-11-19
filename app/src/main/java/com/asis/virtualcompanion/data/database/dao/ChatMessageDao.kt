package com.asis.virtualcompanion.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ChatMessageDao : BaseDao<ChatMessageEntity> {
    
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentChatMessages(limit: Int): List<ChatMessageEntity>
    
    @Query("SELECT * FROM chat_messages WHERE is_from_user = :isFromUser ORDER BY timestamp ASC")
    fun getChatMessagesByType(isFromUser: Boolean): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages WHERE timestamp >= :startDate ORDER BY timestamp ASC")
    fun getChatMessagesAfterDate(startDate: Date): Flow<List<ChatMessageEntity>>
    
    @Query("DELETE FROM chat_messages WHERE timestamp < :beforeDate")
    suspend fun deleteChatMessagesBeforeDate(beforeDate: Date): Int
    
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllChatMessages(): Int
    
    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getChatMessageCount(): Int
    
    @Transaction
    suspend fun insertBatch(messages: List<ChatMessageEntity>) {
        insertAll(messages)
    }
}
