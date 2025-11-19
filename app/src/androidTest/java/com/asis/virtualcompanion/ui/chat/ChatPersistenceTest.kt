package com.asis.virtualcompanion.ui.chat

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.AsisDatabase
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import com.asis.virtualcompanion.data.repository.ChatMessageRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ChatPersistenceTest {

    private lateinit var database: AsisDatabase
    private lateinit var repository: ChatMessageRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AsisDatabase::class.java
        ).build()
        repository = ChatMessageRepositoryImpl(database.chatMessageDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveMessage() = runBlocking {
        val message = ChatMessageEntity(
            message = "Test message",
            isFromUser = true,
            timestamp = Date()
        )

        val insertResult = repository.insertChatMessage(message)
        assertTrue(insertResult is Result.Success)

        val messages = repository.getAllChatMessages().first()
        assertEquals(1, messages.size)
        assertEquals("Test message", messages[0].message)
        assertTrue(messages[0].isFromUser)
    }

    @Test
    fun insertMultipleMessages_maintainsOrder() = runBlocking {
        val messages = listOf(
            ChatMessageEntity(message = "First", isFromUser = true, timestamp = Date(System.currentTimeMillis() - 3000)),
            ChatMessageEntity(message = "Second", isFromUser = false, timestamp = Date(System.currentTimeMillis() - 2000)),
            ChatMessageEntity(message = "Third", isFromUser = true, timestamp = Date(System.currentTimeMillis() - 1000))
        )

        for (message in messages) {
            repository.insertChatMessage(message)
        }

        val retrieved = repository.getAllChatMessages().first()
        assertEquals(3, retrieved.size)
        assertEquals("First", retrieved[0].message)
        assertEquals("Second", retrieved[1].message)
        assertEquals("Third", retrieved[2].message)
    }

    @Test
    fun messagesWithMetadata_persistCorrectly() = runBlocking {
        val message = ChatMessageEntity(
            message = "Test with metadata",
            isFromUser = false,
            timestamp = Date(),
            metadata = mapOf(
                "confidence" to "0.85",
                "template_id" to "template_123"
            )
        )

        val insertResult = repository.insertChatMessage(message)
        assertTrue(insertResult is Result.Success)

        val messages = repository.getAllChatMessages().first()
        assertEquals(1, messages.size)
        assertEquals("0.85", messages[0].metadata?.get("confidence"))
        assertEquals("template_123", messages[0].metadata?.get("template_id"))
    }

    @Test
    fun deleteAllMessages_clearsDatabase() = runBlocking {
        val messages = listOf(
            ChatMessageEntity(message = "Message 1", isFromUser = true),
            ChatMessageEntity(message = "Message 2", isFromUser = false)
        )

        for (message in messages) {
            repository.insertChatMessage(message)
        }

        val countBeforeDelete = repository.getChatMessageCount()
        assertEquals(2, (countBeforeDelete as Result.Success).data)

        repository.deleteAllChatMessages()

        val countAfterDelete = repository.getChatMessageCount()
        assertEquals(0, (countAfterDelete as Result.Success).data)
    }

    @Test
    fun getRecentMessages_returnsCorrectLimit() = runBlocking {
        for (i in 1..10) {
            repository.insertChatMessage(
                ChatMessageEntity(
                    message = "Message $i",
                    isFromUser = i % 2 == 0,
                    timestamp = Date(System.currentTimeMillis() + i * 1000L)
                )
            )
        }

        val recentMessages = repository.getRecentChatMessages(5)
        assertTrue(recentMessages is Result.Success)
        assertEquals(5, recentMessages.data.size)
    }

    @Test
    fun filterMessagesByType_worksCorrectly() = runBlocking {
        val userMessages = listOf(
            ChatMessageEntity(message = "User 1", isFromUser = true),
            ChatMessageEntity(message = "User 2", isFromUser = true)
        )
        val companionMessages = listOf(
            ChatMessageEntity(message = "Companion 1", isFromUser = false),
            ChatMessageEntity(message = "Companion 2", isFromUser = false),
            ChatMessageEntity(message = "Companion 3", isFromUser = false)
        )

        (userMessages + companionMessages).forEach {
            repository.insertChatMessage(it)
        }

        val retrievedUserMessages = repository.getChatMessagesByType(isFromUser = true).first()
        assertEquals(2, retrievedUserMessages.size)

        val retrievedCompanionMessages = repository.getChatMessagesByType(isFromUser = false).first()
        assertEquals(3, retrievedCompanionMessages.size)
    }
}
