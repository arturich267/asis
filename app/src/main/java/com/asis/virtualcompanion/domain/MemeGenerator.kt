package com.asis.virtualcompanion.domain

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.entity.PhraseStatEntity
import com.asis.virtualcompanion.data.model.*
import com.asis.virtualcompanion.domain.repository.ConversationTopicRepository
import com.asis.virtualcompanion.domain.repository.PhraseStatRepository
import com.asis.virtualcompanion.domain.repository.SpeakerStyleRepository
import com.asis.virtualcompanion.domain.repository.ThemeRepository
import com.asis.virtualcompanion.domain.service.TensorFlowLiteStyleClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.random.asJavaRandom

class MemeGenerator(
    private val phraseStatRepository: PhraseStatRepository,
    private val speakerStyleRepository: SpeakerStyleRepository,
    private val conversationTopicRepository: ConversationTopicRepository,
    private val themeRepository: ThemeRepository,
    private val styleClassifier: TensorFlowLiteStyleClassifier
) {
    
    private val random = Random(System.currentTimeMillis())
    
    suspend fun generateMemeResponse(
        config: MemeGenerationConfig
    ): Result<MemeResponse> = withContext(Dispatchers.IO) {
        try {
            // Set seed for reproducible randomness if provided
            config.seed?.let { seed ->
                Random(seed).asJavaRandom().let { javaRandom ->
                    this@MemeGenerator.random = Random(seed)
                }
            }
            
            // Get top phrases
            val topPhrasesResult = phraseStatRepository.getTopPhrases(20)
            if (topPhrasesResult is Result.Error) {
                return@withContext topPhrasesResult
            }
            val topPhrases = (topPhrasesResult as Result.Success).data
            
            // Get recent conversation topics
            val recentTopicsResult = conversationTopicRepository.getRecentTopics(5)
            val recentTopics = if (recentTopicsResult is Result.Success) {
                recentTopicsResult.data
            } else {
                emptyList()
            }
            
            // Determine style based on mode and context
            val style = when (config.mode) {
                MemeMode.RANDOM -> selectRandomStyle()
                MemeMode.RESPOND_TO_EMOTION -> {
                    val emotion = config.detectedEmotion ?: "neutral"
                    classifyStyleFromEmotion(emotion, recentTopics)
                }
                MemeMode.CONTEXT_AWARE -> {
                    classifyStyleFromContext(recentTopics, config.currentTime)
                }
            }
            
            // Get speaker style details
            val speakerStyleResult = speakerStyleRepository.getSpeakerStyleByName(style)
            val speakerStyle = if (speakerStyleResult is Result.Success) {
                speakerStyleResult.data
            } else {
                createDefaultSpeakerStyle(style)
            }
            
            // Generate template
            val template = generateTemplate(topPhrases, speakerStyle, config, recentTopics)
            
            // Generate final response
            val responseText = generateResponseText(template, config)
            
            // Generate audio instructions if needed
            val audioInstructions = if (config.includeAudio) {
                generateAudioInstructions(template, speakerStyle)
            } else {
                null
            }
            
            val response = MemeResponse(
                text = responseText,
                audioInstructions = audioInstructions,
                template = template,
                confidence = calculateConfidence(template, topPhrases, recentTopics)
            )
            
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun generateMemeResponseSync(
        config: MemeGenerationConfig
    ): Result<MemeResponse> {
        return generateMemeResponse(config)
    }
    
    private suspend fun selectRandomStyle(): String {
        val stylesResult = speakerStyleRepository.getAllSpeakerStyles()
        val styles = if (stylesResult is Result.Success) {
            stylesResult.value
        } else {
            listOf("casual", "energetic", "playful", "friendly")
        }
        return styles[random.nextInt(styles.size)].name
    }
    
    private suspend fun classifyStyleFromEmotion(
        emotion: String,
        recentTopics: List<ConversationTopic>
    ): String {
        val contextText = recentTopics.joinToString(" ") { it.topic }
        val classificationResult = styleClassifier.classifyStyle(contextText, emotion)
        return if (classificationResult is Result.Success) {
            classificationResult.data
        } else {
            "casual"
        }
    }
    
    private suspend fun classifyStyleFromContext(
        recentTopics: List<ConversationTopic>,
        currentTime: Long
    ): String {
        val contextText = recentTopics.joinToString(" ") { it.topic }
        val timeContext = mapOf(
            "timeOfDay" to currentTime,
            "topicCount" to recentTopics.size
        )
        val classificationResult = styleClassifier.classifyStyle(contextText, null, timeContext)
        return if (classificationResult is Result.Success) {
            classificationResult.data
        } else {
            getTimeBasedStyle(currentTime)
        }
    }
    
    private fun getTimeBasedStyle(currentTime: Long): String {
        val hour = (currentTime / 3600000) % 24
        return when (hour) {
            in 6..11 -> "energetic"
            in 12..17 -> "casual"
            in 18..22 -> "friendly"
            else -> "playful"
        }
    }
    
    private fun generateTemplate(
        topPhrases: List<PhraseStatEntity>,
        speakerStyle: SpeakerStyle?,
        config: MemeGenerationConfig,
        recentTopics: List<ConversationTopic>
    ): MemeTemplate {
        val selectedPhrase = topPhrases[random.nextInt(topPhrases.size)]
        
        // Detect meme-worthy constructs
        val hasRepetition = detectRepetition(selectedPhrase.phrase)
        val hasSlang = detectSlang(selectedPhrase.phrase, speakerStyle?.slangTerms ?: emptyList())
        val hasEmojiPattern = detectEmojiPattern(selectedPhrase.emojiHints)
        
        // Generate meme suffix based on detected patterns
        val memeSuffix = generateMemeSuffix(hasRepetition, hasSlang, hasEmojiPattern, speakerStyle)
        
        // Get emoji pack from classifier
        val emojiPackResult = styleClassifier.classifyEmojiPack(
            speakerStyle?.name ?: "casual",
            selectedPhrase.phrase
        )
        val emojiReferences = if (emojiPackResult is Result.Success) {
            emojiPackResult.data.take(random.nextInt(2, 4))
        } else {
            listOf("😊", "✨")
        }
        
        // Generate sticker references
        val stickerReferences = generateStickerReferences(speakerStyle, config)
        
        return MemeTemplate(
            id = "template_${System.currentTimeMillis()}_${random.nextInt(1000)}",
            phrase = selectedPhrase.phrase,
            memeSuffix = memeSuffix,
            emojiReferences = emojiReferences,
            stickerReferences = stickerReferences,
            styleId = speakerStyle?.id ?: "default",
            timeContext = getTimeContext(config.currentTime),
            variability = config.variability
        )
    }
    
    private fun detectRepetition(phrase: String): Boolean {
        val words = phrase.lowercase().split("\\s+".toRegex())
        val wordCounts = words.groupingBy { it }.eachCount()
        return wordCounts.any { it.value > 1 }
    }
    
    private fun detectSlang(phrase: String, slangTerms: List<String>): Boolean {
        val lowerPhrase = phrase.lowercase()
        return slangTerms.any { slang -> lowerPhrase.contains(slang.lowercase()) }
    }
    
    private fun detectEmojiPattern(emojiHints: List<String>): Boolean {
        return emojiHints.isNotEmpty() || emojiHints.any { it.contains("😀") || it.contains("😂") }
    }
    
    private fun generateMemeSuffix(
        hasRepetition: Boolean,
        hasSlang: Boolean,
        hasEmojiPattern: Boolean,
        speakerStyle: SpeakerStyle?
    ): String {
        val suffixes = mutableListOf<String>()
        
        if (hasRepetition) {
            suffixes.addAll(listOf("again and again", "on repeat", "broken record"))
        }
        
        if (hasSlang) {
            suffixes.addAll(listOf("fr fr", "no cap", "bet", "periodt"))
        }
        
        if (hasEmojiPattern) {
            suffixes.addAll(listOf("vibes only", "meme lord", "emoji master"))
        }
        
        // Add style-specific suffixes
        speakerStyle?.let { style ->
            suffixes.addAll(style.repetitionPatterns)
        }
        
        // Default suffixes if none detected
        if (suffixes.isEmpty()) {
            suffixes.addAll(listOf("just like that", "you know the vibes", "it is what it is"))
        }
        
        return suffixes[random.nextInt(suffixes.size)]
    }
    
    private fun generateStickerReferences(
        speakerStyle: SpeakerStyle?,
        config: MemeGenerationConfig
    ): List<String> {
        val baseStickers = listOf("happy_face", "thumbs_up", "celebration", "thinking", "cool")
        val styleStickers = speakerStyle?.emojiPreferences ?: emptyList()
        
        val allStickers = (baseStickers + styleStickers).distinct()
        return allStickers.shuffled(random).take(random.nextInt(1, 3))
    }
    
    private fun getTimeContext(currentTime: Long): TimeContext {
        val hour = (currentTime / 3600000) % 24
        return when (hour) {
            in 5..11 -> TimeContext.MORNING
            in 12..16 -> TimeContext.AFTERNOON
            in 17..20 -> TimeContext.EVENING
            in 21..23 -> TimeContext.NIGHT
            else -> TimeContext.LATE_NIGHT
        }
    }
    
    private fun generateResponseText(
        template: MemeTemplate,
        config: MemeGenerationConfig
    ): String {
        val baseText = "${template.phrase} ${template.memeSuffix}"
        
        // Add variability
        if (random.nextFloat() < template.variability) {
            val variations = listOf(
                "yo $baseText",
                "$baseText tbh",
                "real talk: $baseText",
                "$baseText for real",
                "can we talk about $baseText"
            )
            return variations[random.nextInt(variations.size)]
        }
        
        return baseText
    }
    
    private suspend fun generateAudioInstructions(
        template: MemeTemplate,
        speakerStyle: SpeakerStyle?
    ): String {
        val tone = when (speakerStyle?.name?.lowercase()) {
            "energetic" -> "upbeat, fast-paced, high energy"
            "friendly" -> "warm, gentle, inviting"
            "sarcastic" -> "dry, slightly mocking, pauses for effect"
            "casual" -> "relaxed, conversational, natural"
            "serious" -> "measured, calm, authoritative"
            "playful" -> "cheerful, bouncy, fun"
            "formal" -> "professional, clear, measured pace"
            else -> "neutral, clear, moderate pace"
        }
        
        val speed = when (template.timeContext) {
            TimeContext.MORNING -> "1.1x"
            TimeContext.AFTERNOON -> "1.0x"
            TimeContext.EVENING -> "0.9x"
            TimeContext.NIGHT -> "0.8x"
            TimeContext.LATE_NIGHT -> "0.7x"
            TimeContext.ANY -> "1.0x"
        }
        
        return "Tone: $tone, Speed: $speed, Emphasis: ${template.emojiReferences.joinToString(", ")}"
    }
    
    private fun calculateConfidence(
        template: MemeTemplate,
        topPhrases: List<PhraseStatEntity>,
        recentTopics: List<ConversationTopic>
    ): Float {
        var confidence = 0.5f // Base confidence
        
        // Boost confidence based on phrase popularity
        val phraseIndex = topPhrases.indexOfFirst { it.phrase == template.phrase }
        if (phraseIndex >= 0) {
            confidence += (20 - phraseIndex) * 0.02f
        }
        
        // Boost confidence based on topic relevance
        if (recentTopics.isNotEmpty()) {
            confidence += 0.1f
        }
        
        // Boost confidence based on style match
        if (template.styleId != "default") {
            confidence += 0.1f
        }
        
        return confidence.coerceIn(0.1f, 1.0f)
    }
    
    private fun createDefaultSpeakerStyle(styleName: String): SpeakerStyle {
        return SpeakerStyle(
            id = "default_$styleName",
            name = styleName,
            characteristics = listOf("default"),
            emojiPreferences = listOf("😊", "✨", "👍"),
            slangTerms = listOf("lol", "omg", "wow"),
            repetitionPatterns = listOf("you know", "like that", "for real")
        )
    }
}