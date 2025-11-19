package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.ConversationTopic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConversationTopicRepositoryImplTest {
    
    private lateinit var repository: ConversationTopicRepositoryImpl
    private val currentTime = System.currentTimeMillis()
    
    @Before
    fun setup() {
        repository = ConversationTopicRepositoryImpl()
        repository.addSampleTopics()
    }
    
    @Test
    fun `getAllConversationTopics returns all topics`() = runTest {
        // When
        val topics = repository.getAllConversationTopics().first()
        
        // Then
        assertEquals(5, topics.size)
        assertTrue(topics.any { it.topic == "Weekend plans" })
        assertTrue(topics.any { it.topic == "Work stress" })
        assertTrue(topics.any { it.topic == "New movie release" })
        assertTrue(topics.any { it.topic == "Fitness goals" })
        assertTrue(topics.any { it.topic == "Cooking recipes" })
    }
    
    @Test
    fun `getRecentTopics with limit returns correct number`() = runTest {
        // When
        val result = repository.getRecentTopics(3)
        
        // Then
        assertTrue(result is Result.Success)
        val topics = (result as Result.Success).data
        assertEquals(3, topics.size)
        
        // Should be sorted by timestamp (most recent first)
        assertTrue(topics[0].timestamp >= topics[1].timestamp)
        assertTrue(topics[1].timestamp >= topics[2].timestamp)
    }
    
    @Test
    fun `getRecentTopics with limit larger than available returns all`() = runTest {
        // When
        val result = repository.getRecentTopics(10)
        
        // Then
        assertTrue(result is Result.Success)
        val topics = (result as Result.Success).data
        assertEquals(5, topics.size)
    }
    
    @Test
    fun `getTopicById returns correct topic`() = runTest {
        // When
        val result = repository.getTopicById("topic_1")
        
        // Then
        assertTrue(result is Result.Success)
        val topic = (result as Result.Success).data
        assertNotNull(topic)
        assertEquals("topic_1", topic?.id)
        assertEquals("Weekend plans", topic?.topic)
        assertTrue(topic?.keywords?.contains("weekend") == true)
    }
    
    @Test
    fun `getTopicById with non-existent id returns null`() = runTest {
        // When
        val result = repository.getTopicById("non_existent")
        
        // Then
        assertTrue(result is Result.Success)
        val topic = (result as Result.Success).data
        assertEquals(null, topic)
    }
    
    @Test
    fun `insertTopic adds new topic`() = runTest {
        // Given
        val newTopic = ConversationTopic(
            id = "new_topic",
            topic = "Travel plans",
            keywords = listOf("travel", "vacation", "trip"),
            timestamp = currentTime,
            relevanceScore = 0.8f
        )
        
        // When
        val result = repository.insertTopic(newTopic)
        
        // Then
        assertTrue(result is Result.Success)
        val insertId = (result as Result.Success).data
        assertTrue(insertId > 0)
        
        // Verify it was added
        val topics = repository.getAllConversationTopics().first()
        assertEquals(6, topics.size)
        assertTrue(topics.any { it.id == "new_topic" })
    }
    
    @Test
    fun `updateTopic modifies existing topic`() = runTest {
        // Given
        val updatedTopic = ConversationTopic(
            id = "topic_2",
            topic = "Updated work stress",
            keywords = listOf("work", "stress", "updated"),
            timestamp = currentTime + 1000,
            relevanceScore = 0.9f
        )
        
        // When
        val result = repository.updateTopic(updatedTopic)
        
        // Then
        assertTrue(result is Result.Success)
        
        // Verify it was updated
        val getResult = repository.getTopicById("topic_2")
        assertTrue(getResult is Result.Success)
        val topic = (getResult as Result.Success).data
        assertEquals("Updated work stress", topic?.topic)
        assertTrue(topic?.keywords?.contains("updated") == true)
        assertEquals(0.9f, topic?.relevanceScore)
    }
    
    @Test
    fun `updateTopic with non-existent id does nothing`() = runTest {
        // Given
        val nonExistentTopic = ConversationTopic(
            id = "non_existent",
            topic = "Won't be added",
            keywords = listOf("test"),
            timestamp = currentTime,
            relevanceScore = 0.5f
        )
        
        // When
        val result = repository.updateTopic(nonExistentTopic)
        
        // Then
        assertTrue(result is Result.Success)
        
        // Verify no new topic was added
        val topics = repository.getAllConversationTopics().first()
        assertEquals(5, topics.size)
        assertTrue(topics.none { it.id == "non_existent" })
    }
    
    @Test
    fun `deleteTopic removes topic`() = runTest {
        // When
        val result = repository.deleteTopic("topic_3")
        
        // Then
        assertTrue(result is Result.Success)
        
        // Verify it was deleted
        val getResult = repository.getTopicById("topic_3")
        assertTrue(getResult is Result.Success)
        assertEquals(null, (getResult as Result.Success).data)
        
        // Verify total count decreased
        val topics = repository.getAllConversationTopics().first()
        assertEquals(4, topics.size)
    }
    
    @Test
    fun `deleteTopic with non-existent id does nothing`() = runTest {
        // When
        val result = repository.deleteTopic("non_existent")
        
        // Then
        assertTrue(result is Result.Success)
        
        // Verify no topics were removed
        val topics = repository.getAllConversationTopics().first()
        assertEquals(5, topics.size)
    }
    
    @Test
    fun `getTopicsByKeywords returns matching topics`() = runTest {
        // When
        val result = repository.getTopicsByKeywords(listOf("work", "stress"))
        
        // Then
        assertTrue(result is Result.Success)
        val topics = (result as Result.Success).data
        assertEquals(1, topics.size)
        assertEquals("topic_2", topics[0].id)
        assertEquals("Work stress", topics[0].topic)
    }
    
    @Test
    fun `getTopicsByKeywords with partial match returns topics`() = runTest {
        // When
        val result = repository.getTopicsByKeywords(listOf("fit"))
        
        // Then
        assertTrue(result is Result.Success)
        val topics = (result as Result.Success).data
        assertEquals(1, topics.size)
        assertEquals("topic_4", topics[0].id)
        assertEquals("Fitness goals", topics[0].topic)
    }
    
    @Test
    fun `getTopicsByKeywords with topic content match returns topics`() = runTest {
        // When
        val result = repository.getTopicsByKeywords(listOf("movie"))
        
        // Then
        assertTrue(result is Result.Success)
        val topics = (result as Result.Success).data
        assertEquals(1, topics.size)
        assertEquals("topic_3", topics[0].id)
    }
    
    @Test
    fun `getTopicsByKeywords with no matches returns empty list`() = runTest {
        // When
        val result = repository.getTopicsByKeywords(listOf("nonexistent"))
        
        // Then
        assertTrue(result is Result.Success)
        val topics = (result as Result.Success).data
        assertTrue(topics.isEmpty())
    }
    
    @Test
    fun `getTopicsInTimeRange returns correct topics`() = runTest {
        // Given - Create topics with specific timestamps
        val baseTime = currentTime - TimeUnit.HOURS.toMillis(2)
        
        repository.insertTopic(ConversationTopic(
            id = "time_test_1",
            topic = "Old topic",
            keywords = listOf("old"),
            timestamp = baseTime - TimeUnit.HOURS.toMillis(2),
            relevanceScore = 0.5f
        ))
        
        repository.insertTopic(ConversationTopic(
            id = "time_test_2",
            topic = "Recent topic",
            keywords = listOf("recent"),
            timestamp = baseTime + TimeUnit.MINUTES.toMillis(30),
            relevanceScore = 0.7f
        ))
        
        // When - Get topics in the last hour
        val startTime = baseTime
        val endTime = baseTime + TimeUnit.HOURS.toMillis(1)
        val result = repository.getTopicsInTimeRange(startTime, endTime)
        
        // Then
        assertTrue(result is Result.Success)
        val topics = (result as Result.Success).data
        assertEquals(1, topics.size)
        assertEquals("time_test_2", topics[0].id)
        assertEquals("Recent topic", topics[0].topic)
    }
    
    @Test
    fun `getTopicsInTimeRange with no matches returns empty list`() = runTest {
        // When
        val futureStart = currentTime + TimeUnit.DAYS.toMillis(1)
        val futureEnd = currentTime + TimeUnit.DAYS.toMillis(2)
        val result = repository.getTopicsInTimeRange(futureStart, futureEnd)
        
        // Then
        assertTrue(result is Result.Success)
        val topics = (result as Result.Success).data
        assertTrue(topics.isEmpty())
    }
    
    @Test
    fun `cleanupOldTopics removes old topics and returns count`() = runTest {
        // Given - Add an old topic
        val oldTimestamp = currentTime - TimeUnit.DAYS.toMillis(7)
        repository.insertTopic(ConversationTopic(
            id = "old_topic",
            topic = "Very old topic",
            keywords = listOf("old"),
            timestamp = oldTimestamp,
            relevanceScore = 0.1f
        ))
        
        // Verify it was added
        var topics = repository.getAllConversationTopics().first()
        assertEquals(6, topics.size)
        
        // When - Cleanup topics older than 1 day
        val cutoffTime = currentTime - TimeUnit.DAYS.toMillis(1)
        val result = repository.cleanupOldTopics(cutoffTime)
        
        // Then
        assertTrue(result is Result.Success)
        val removedCount = (result as Result.Success).data
        assertEquals(1, removedCount)
        
        // Verify old topic was removed
        topics = repository.getAllConversationTopics().first()
        assertEquals(5, topics.size)
        assertTrue(topics.none { it.id == "old_topic" })
    }
    
    @Test
    fun `cleanupOldTopics with no old topics returns zero`() = runTest {
        // When - Cleanup with very old cutoff
        val ancientCutoff = currentTime - TimeUnit.DAYS.toMillis(365)
        val result = repository.cleanupOldTopics(ancientCutoff)
        
        // Then
        assertTrue(result is Result.Success)
        val removedCount = (result as Result.Success).data
        assertEquals(0, removedCount)
        
        // Verify no topics were removed
        val topics = repository.getAllConversationTopics().first()
        assertEquals(5, topics.size)
    }
    
    @Test
    fun `sample topics have correct properties`() = runTest {
        // When
        val topics = repository.getAllConversationTopics().first()
        
        // Then
        val weekendTopic = topics.find { it.id == "topic_1" }
        assertNotNull(weekendTopic)
        assertEquals("Weekend plans", weekendTopic.topic)
        assertTrue(weekendTopic.keywords.contains("weekend"))
        assertTrue(weekendTopic.keywords.contains("plans"))
        assertEquals(0.9f, weekendTopic.relevanceScore)
        
        val workTopic = topics.find { it.id == "topic_2" }
        assertNotNull(workTopic)
        assertEquals("Work stress", workTopic.topic)
        assertTrue(workTopic.keywords.contains("work"))
        assertTrue(workTopic.keywords.contains("stress"))
        assertEquals(0.8f, workTopic.relevanceScore)
    }
}