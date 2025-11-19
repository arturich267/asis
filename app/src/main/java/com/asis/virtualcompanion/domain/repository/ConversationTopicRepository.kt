package com.asis.virtualcompanion.domain.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.ConversationTopic
import kotlinx.coroutines.flow.Flow

interface ConversationTopicRepository : BaseRepository {
    
    fun getAllConversationTopics(): Flow<List<ConversationTopic>>
    
    suspend fun getRecentTopics(limit: Int = 10): Result<List<ConversationTopic>>
    
    suspend fun getTopicById(id: String): Result<ConversationTopic?>
    
    suspend fun insertTopic(topic: ConversationTopic): Result<Long>
    
    suspend fun updateTopic(topic: ConversationTopic): Result<Unit>
    
    suspend fun deleteTopic(id: String): Result<Unit>
    
    suspend fun getTopicsByKeywords(keywords: List<String>): Result<List<ConversationTopic>>
    
    suspend fun getTopicsInTimeRange(startTime: Long, endTime: Long): Result<List<ConversationTopic>>
    
    suspend fun cleanupOldTopics(olderThan: Long): Result<Int>
}