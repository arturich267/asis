package com.asis.virtualcompanion.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.Date
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class ChatMessageAdapterTest {

    private lateinit var adapter: ChatMessageAdapter

    @Mock
    private lateinit var parent: ViewGroup

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        adapter = ChatMessageAdapter()
    }

    @Test
    fun `getItemViewType returns correct type for user message`() {
        val messages = listOf(
            ChatMessageEntity(id = 1, message = "User message", isFromUser = true)
        )
        adapter.submitList(messages)

        val viewType = adapter.getItemViewType(0)
        assertEquals(1, viewType) // VIEW_TYPE_USER
    }

    @Test
    fun `getItemViewType returns correct type for companion message`() {
        val messages = listOf(
            ChatMessageEntity(id = 1, message = "Companion message", isFromUser = false)
        )
        adapter.submitList(messages)

        val viewType = adapter.getItemViewType(0)
        assertEquals(2, viewType) // VIEW_TYPE_COMPANION
    }

    @Test
    fun `submitList updates adapter correctly`() {
        val messages = listOf(
            ChatMessageEntity(id = 1, message = "Message 1", isFromUser = true),
            ChatMessageEntity(id = 2, message = "Message 2", isFromUser = false),
            ChatMessageEntity(id = 3, message = "Message 3", isFromUser = true)
        )
        
        adapter.submitList(messages)
        
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `DiffCallback identifies same items correctly`() {
        val diffCallback = ChatMessageDiffCallback()
        
        val message1 = ChatMessageEntity(id = 1, message = "Test", isFromUser = true)
        val message2 = ChatMessageEntity(id = 1, message = "Test Modified", isFromUser = true)
        val message3 = ChatMessageEntity(id = 2, message = "Test", isFromUser = true)
        
        assertEquals(true, diffCallback.areItemsTheSame(message1, message2))
        assertEquals(false, diffCallback.areItemsTheSame(message1, message3))
    }

    @Test
    fun `DiffCallback identifies same contents correctly`() {
        val diffCallback = ChatMessageDiffCallback()
        
        val timestamp = Date()
        val message1 = ChatMessageEntity(id = 1, message = "Test", isFromUser = true, timestamp = timestamp)
        val message2 = ChatMessageEntity(id = 1, message = "Test", isFromUser = true, timestamp = timestamp)
        val message3 = ChatMessageEntity(id = 1, message = "Test Modified", isFromUser = true, timestamp = timestamp)
        
        assertEquals(true, diffCallback.areContentsTheSame(message1, message2))
        assertEquals(false, diffCallback.areContentsTheSame(message1, message3))
    }

    @Test
    fun `empty list shows zero items`() {
        adapter.submitList(emptyList())
        assertEquals(0, adapter.itemCount)
    }
}
