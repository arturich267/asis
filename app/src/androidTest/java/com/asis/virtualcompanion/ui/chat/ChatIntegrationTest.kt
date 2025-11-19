package com.asis.virtualcompanion.ui.chat

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.AsisDatabase
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import com.asis.virtualcompanion.data.database.entity.PhraseStatEntity
import com.asis.virtualcompanion.data.model.MemeGenerationConfig
import com.asis.virtualcompanion.data.model.MemeMode
import com.asis.virtualcompanion.data.repository.ChatMessageRepositoryImpl
import com.asis.virtualcompanion.data.repository.ConversationTopicRepositoryImpl
import com.asis.virtualcompanion.data.repository.PhraseStatRepositoryImpl
import com.asis.virtualcompanion.data.repository.SpeakerStyleRepositoryImpl
import com.asis.virtualcompanion.domain.MemeGenerator
import com.asis.virtualcompanion.domain.repository.ThemeRepository
import com.asis.virtualcompanion.domain.service.TensorFlowLiteStyleClassifier
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
class ChatIntegrationTest {

    private lateinit var database: AsisDatabase
    private lateinit var chatRepository: ChatMessageRepositoryImpl
    private lateinit var phraseStatRepository: PhraseStatRepositoryImpl
    private lateinit var memeGenerator: MemeGenerator
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AsisDatabase::class.java
        ).build()
        
        chatRepository = ChatMessageRepositoryImpl(database.chatMessageDao())
        phraseStatRepository = PhraseStatRepositoryImpl(database.phraseStatDao())
        
        val speakerStyleRepository = SpeakerStyleRepositoryImpl()
        val conversationTopicRepository = ConversationTopicRepositoryImpl()
        val themeRepository = ThemeRepository(
            context,
            com.asis.virtualcompanion.data.preferences.ThemePreferences(context)
        )
        val styleClassifier = TensorFlowLiteStyleClassifier(context)
        
        memeGenerator = MemeGenerator(
            phraseStatRepository,
            speakerStyleRepository,
            conversationTopicRepository,
            themeRepository,
            styleClassifier
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun endToEndChatFlow_sendsUserMessageAndGeneratesResponse() = runBlocking {
        seedPhraseStats()
        
        val userMessage = ChatMessageEntity(
            message = "Hello, how are you?",
            isFromUser = true,
            timestamp = Date()
        )
        
        val insertResult = chatRepository.insertChatMessage(userMessage)
        assertTrue(insertResult is Result.Success)
        
        val config = MemeGenerationConfig(
            mode = MemeMode.CONTEXT_AWARE,
            currentTime = System.currentTimeMillis(),
            includeAudio = false,
            variability = 0.7f,
            seed = 12345L
        )
        
        val responseResult = memeGenerator.generateMemeResponse(config)
        assertTrue(responseResult is Result.Success)
        
        val companionMessage = ChatMessageEntity(
            message = responseResult.data.text,
            isFromUser = false,
            timestamp = Date(),
            metadata = mapOf(
                "confidence" to responseResult.data.confidence.toString()
            )
        )
        
        chatRepository.insertChatMessage(companionMessage)
        
        val allMessages = chatRepository.getAllChatMessages().first()
        assertEquals(2, allMessages.size)
        assertEquals("Hello, how are you?", allMessages[0].message)
        assertTrue(allMessages[0].isFromUser)
        assertTrue(!allMessages[1].isFromUser)
    }

    @Test
    fun multipleMessagesInConversation_maintainOrder() = runBlocking {
        seedPhraseStats()
        
        val conversation = listOf(
            ChatMessageEntity(message = "Hi", isFromUser = true, timestamp = Date(System.currentTimeMillis() - 5000)),
            ChatMessageEntity(message = "Hey there!", isFromUser = false, timestamp = Date(System.currentTimeMillis() - 4000)),
            ChatMessageEntity(message = "How's it going?", isFromUser = true, timestamp = Date(System.currentTimeMillis() - 3000)),
            ChatMessageEntity(message = "Pretty good vibes!", isFromUser = false, timestamp = Date(System.currentTimeMillis() - 2000))
        )
        
        for (message in conversation) {
            chatRepository.insertChatMessage(message)
        }
        
        val messages = chatRepository.getAllChatMessages().first()
        assertEquals(4, messages.size)
        
        for (i in messages.indices) {
            assertEquals(conversation[i].message, messages[i].message)
            assertEquals(conversation[i].isFromUser, messages[i].isFromUser)
        }
    }

    @Test
    fun conversationPersistsAcrossDatabaseReopen() = runBlocking {
        val tempDb = Room.databaseBuilder(
            context,
            AsisDatabase::class.java,
            "test_chat_db"
        ).build()
        
        val tempRepo = ChatMessageRepositoryImpl(tempDb.chatMessageDao())
        
        val message = ChatMessageEntity(
            message = "Persistent message",
            isFromUser = true,
            timestamp = Date()
        )
        
        tempRepo.insertChatMessage(message)
        tempDb.close()
        
        val reopenedDb = Room.databaseBuilder(
            context,
            AsisDatabase::class.java,
            "test_chat_db"
        ).build()
        
        val reopenedRepo = ChatMessageRepositoryImpl(reopenedDb.chatMessageDao())
        val messages = reopenedRepo.getAllChatMessages().first()
        
        assertEquals(1, messages.size)
        assertEquals("Persistent message", messages[0].message)
        
        reopenedDb.close()
        context.deleteDatabase("test_chat_db")
    }

    private suspend fun seedPhraseStats() {
        val phrases = listOf(
            PhraseStatEntity(phrase = "hello there", count = 10, emojiHints = listOf("👋")),
            PhraseStatEntity(phrase = "how's it going", count = 8, emojiHints = listOf("😊")),
            PhraseStatEntity(phrase = "what's up", count = 6, emojiHints = listOf("✨")),
            PhraseStatEntity(phrase = "nice to see you", count = 5, emojiHints = listOf("😄")),
            PhraseStatEntity(phrase = "good vibes", count = 7, emojiHints = listOf("✌️"))
        )
        
        for (phrase in phrases) {
            phraseStatRepository.insertPhraseStat(phrase)
        }
    }
}
