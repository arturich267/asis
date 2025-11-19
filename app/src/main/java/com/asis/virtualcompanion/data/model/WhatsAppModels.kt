package com.asis.virtualcompanion.data.model

import java.util.Date

data class WhatsAppMessage(
    val sender: String,
    val timestamp: Date,
    val text: String,
    val messageType: MessageType,
    val isQuoted: Boolean = false,
    val quotedMessage: String? = null,
    val mediaFileName: String? = null,
    val voiceNoteDuration: Long? = null
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    VOICE_NOTE,
    DOCUMENT,
    STICKER,
    LOCATION,
    CONTACT,
    SYSTEM
}

data class WhatsAppChatExport(
    val messages: List<WhatsAppMessage>,
    val mediaFiles: List<String>,
    val voiceNotes: List<VoiceNoteMetadata>
)

data class VoiceNoteMetadata(
    val fileName: String,
    val sender: String,
    val timestamp: Date,
    val duration: Long,
    val filePath: String
)

data class PhraseFrequency(
    val phrase: String,
    val count: Int,
    val emojiHints: List<String> = emptyList()
)

data class SentimentResult(
    val sentiment: String,
    val confidence: Float,
    val emotions: Map<String, Float> = emptyMap()
)