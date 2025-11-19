package com.asis.virtualcompanion.work.parser

import com.asis.virtualcompanion.data.model.*
import org.junit.Test
import org.junit.Assert.*
import java.util.*

class WhatsAppChatParserTest {

    private val parser = WhatsAppChatParser()

    @Test
    fun `should parse US date format message`() {
        val chatText = """
            12/31/23, 11:59 PM - John Doe: Happy New Year!
            1/1/24, 12:00 AM - Jane Smith: Happy New Year to you too!
        """.trimIndent()

        val result = parser.parseChat(chatText)

        assertEquals(2, result.messages.size)
        
        val firstMessage = result.messages[0]
        assertEquals("John Doe", firstMessage.sender)
        assertEquals("Happy New Year!", firstMessage.text)
        assertEquals(MessageType.TEXT, firstMessage.messageType)
        
        val secondMessage = result.messages[1]
        assertEquals("Jane Smith", secondMessage.sender)
        assertEquals("Happy New Year to you too!", secondMessage.text)
    }

    @Test
    fun `should parse European date format message`() {
        val chatText = """
            31/12/23, 23:59 - John Doe: Happy New Year!
        """.trimIndent()

        val result = parser.parseChat(chatText)

        assertEquals(1, result.messages.size)
        
        val message = result.messages[0]
        assertEquals("John Doe", message.sender)
        assertEquals("Happy New Year!", message.text)
    }

    @Test
    fun `should detect media messages`() {
        val chatText = """
            12/31/23, 11:59 PM - John Doe: <image: omitted>
            12/31/23, 11:60 PM - Jane Smith: <video: omitted>
            12/31/23, 11:61 PM - Bob: <Media omitted>
        """.trimIndent()

        val result = parser.parseChat(chatText)

        assertEquals(3, result.messages.size)
        assertEquals(MessageType.IMAGE, result.messages[0].messageType)
        assertEquals(MessageType.VIDEO, result.messages[1].messageType)
        assertEquals(MessageType.VOICE_NOTE, result.messages[2].messageType)
    }

    @Test
    fun `should detect quoted messages`() {
        val chatText = """
            12/31/23, 11:59 PM - John Doe: "Happy New Year!"
            12/31/23, 11:60 PM - Jane Smith: > Happy New Year!
        """.trimIndent()

        val result = parser.parseChat(chatText)

        assertEquals(2, result.messages.size)
        assertTrue(result.messages[0].isQuoted)
        assertTrue(result.messages[1].isQuoted)
    }

    @Test
    fun `should extract voice note metadata`() {
        val chatText = """
            12/31/23, 11:59 PM - John Doe: PTT-2023-12-31-WA0001.opus
        """.trimIndent()

        val result = parser.parseChat(chatText)

        assertEquals(1, result.messages.size)
        assertEquals(1, result.voiceNotes.size)
        
        val voiceNote = result.voiceNotes[0]
        assertEquals("PTT-2023-12-31-WA0001.opus", voiceNote.fileName)
        assertEquals("John Doe", voiceNote.sender)
    }

    @Test
    fun `should handle multiline messages`() {
        val chatText = """
            12/31/23, 11:59 PM - John Doe: This is a multiline message
            that spans multiple lines
            and should be treated as one message.
            12/31/23, 11:60 PM - Jane Smith: Single line message
        """.trimIndent()

        val result = parser.parseChat(chatText)

        assertEquals(2, result.messages.size)
        
        val multilineMessage = result.messages[0]
        assertTrue(multilineMessage.text.contains("This is a multiline message"))
        assertTrue(multilineMessage.text.contains("that spans multiple lines"))
        assertTrue(multilineMessage.text.contains("and should be treated as one message"))
    }

    @Test
    fun `should handle system messages`() {
        val chatText = """
            12/31/23, 11:59 PM - Messages and calls are end-to-end encrypted. No one outside of this chat, not even WhatsApp, can read or listen to them. Tap to learn more.
            12/31/23, 11:60 PM - John Doe: Normal message
        """.trimIndent()

        val result = parser.parseChat(chatText)

        assertEquals(2, result.messages.size)
        
        val systemMessage = result.messages[0]
        assertEquals("System", systemMessage.sender)
        assertEquals(MessageType.SYSTEM, systemMessage.messageType)
        
        val normalMessage = result.messages[1]
        assertEquals("John Doe", normalMessage.sender)
        assertEquals(MessageType.TEXT, normalMessage.messageType)
    }
}