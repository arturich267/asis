package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.ConversationTopic
import com.asis.virtualcompanion.domain.repository.ConversationTopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class ConversationTopicRepositoryImpl : ConversationTopicRepository {
    
    private val topicsFlow = MutableStateFlow<List<ConversationTopic>>(emptyList())
    
    override fun getAllConversationTopics(): Flow<List<ConversationTopic>> = topicsFlow
    
    override suspend fun getRecentTopics(limit: Int): Result<List<ConversationTopic>> {
        return try {
            val allTopics = topicsFlow.value
            val recentTopics = allTopics
                .sortedByDescending { it.timestamp }
                .take(limit)
            Result.Success(recentTopics)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTopicById(id: String): Result<ConversationTopic?> {
        return try {
            val topics = topicsFlow.value
            val topic = topics.find { it.id == id }
            Result.Success(topic)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun insertTopic(topic: ConversationTopic): Result<Long> {
        return try {
            val currentTopics = topicsFlow.value.toMutableList()
            currentTopics.add(topic)
            topicsFlow.value = currentTopics
            Result.Success(System.currentTimeMillis())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateTopic(topic: ConversationTopic): Result<Unit> {
        return try {
            val currentTopics = topicsFlow.value.toMutableList()
            val index = currentTopics.indexOfFirst { it.id == topic.id }
            if (index >= 0) {
                currentTopics[index] = topic
                topicsFlow.value = currentTopics
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deleteTopic(id: String): Result<Unit> {
        return try {
            val currentTopics = topicsFlow.value.toMutableList()
            currentTopics.removeAll { it.id == id }
            topicsFlow.value = currentTopics
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTopicsByKeywords(keywords: List<String>): Result<List<ConversationTopic>> {
        return try {
            val topics = topicsFlow.value
            val matchingTopics = topics.filter { topic ->
                keywords.any { keyword ->
                    topic.topic.contains(keyword, ignoreCase = true) ||
                    topic.keywords.any { topicKeyword ->
                        topicKeyword.contains(keyword, ignoreCase = true)
                    }
                }
            }
            Result.Success(matchingTopics)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTopicsInTimeRange(startTime: Long, endTime: Long): Result<List<ConversationTopic>> {
        return try {
            val topics = topicsFlow.value
            val filteredTopics = topics.filter { topic ->
                topic.timestamp in startTime..endTime
            }
            Result.Success(filteredTopics)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun cleanupOldTopics(olderThan: Long): Result<Int> {
        return try {
            val currentTopics = topicsFlow.value
            val recentTopics = currentTopics.filter { it.timestamp >= olderThan }
            val removedCount = currentTopics.size - recentTopics.size
            topicsFlow.value = recentTopics
            Result.Success(removedCount)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    // Helper method to add sample topics for testing
    fun addSampleTopics() {
        val sampleTopics = listOf(
            ConversationTopic(
                id = "topic_1",
                topic = "Weekend plans",
                keywords = listOf("weekend", "plans", "fun", "activities"),
                timestamp = System.currentTimeMillis() - 3600000,
                relevanceScore = 0.9f
            ),
            ConversationTopic(
                id = "topic_2",
                topic = "Work stress",
                keywords = listOf("work", "stress", "deadline", "pressure"),
                timestamp = System.currentTimeMillis() - 7200000,
                relevanceScore = 0.8f
            ),
            ConversationTopic(
                id = "topic_3",
                topic = "New movie release",
                keywords = listOf("movie", "film", "cinema", "entertainment"),
                timestamp = System.currentTimeMillis() - 10800000,
                relevanceScore = 0.7f
            ),
            ConversationTopic(
                id = "topic_4",
                topic = "Fitness goals",
                keywords = listOf("fitness", "gym", "workout", "health"),
                timestamp = System.currentTimeMillis() - 14400000,
                relevanceScore = 0.6f
            ),
            ConversationTopic(
                id = "topic_5",
                topic = "Cooking recipes",
                keywords = listOf("cooking", "food", "recipes", "kitchen"),
                timestamp = System.currentTimeMillis() - 18000000,
                relevanceScore = 0.5f
            )
        )
        topicsFlow.value = sampleTopics
    }
}