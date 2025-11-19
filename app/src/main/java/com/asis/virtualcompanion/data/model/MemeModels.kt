package com.asis.virtualcompanion.data.model

data class SpeakerStyle(
    val id: String,
    val name: String,
    val characteristics: List<String>,
    val emojiPreferences: List<String>,
    val slangTerms: List<String>,
    val repetitionPatterns: List<String>
)

data class ConversationTopic(
    val id: String,
    val topic: String,
    val keywords: List<String>,
    val timestamp: Long,
    val relevanceScore: Float = 1.0f
)

data class MemeTemplate(
    val id: String,
    val phrase: String,
    val memeSuffix: String,
    val emojiReferences: List<String>,
    val stickerReferences: List<String>,
    val styleId: String,
    val timeContext: TimeContext,
    val variability: Float = 0.3f
)

enum class TimeContext {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    LATE_NIGHT,
    ANY
}

enum class MemeMode {
    RANDOM,
    RESPOND_TO_EMOTION,
    CONTEXT_AWARE
}

data class MemeGenerationConfig(
    val mode: MemeMode,
    val seed: Long? = null,
    val variability: Float = 0.3f,
    val includeAudio: Boolean = false,
    val detectedEmotion: String? = null,
    val currentTime: Long = System.currentTimeMillis()
)

data class MemeResponse(
    val text: String,
    val audioInstructions: String? = null,
    val template: MemeTemplate,
    val confidence: Float,
    val generationTime: Long = System.currentTimeMillis()
)