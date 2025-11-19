package com.asis.virtualcompanion.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.asis.virtualcompanion.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MessageDao : BaseDao<MessageEntity> {
    
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE sender = :sender ORDER BY timestamp DESC")
    fun getMessagesBySender(sender: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE timestamp >= :startDate ORDER BY timestamp DESC")
    fun getMessagesAfterDate(startDate: Date): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE sentiment = :sentiment ORDER BY timestamp DESC")
    fun getMessagesBySentiment(sentiment: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<MessageEntity>
    
    @Query("DELETE FROM messages WHERE timestamp < :beforeDate")
    suspend fun deleteMessagesBeforeDate(beforeDate: Date): Int
    
    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getMessageCount(): Int
}
