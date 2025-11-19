package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.SpeakerStyle
import com.asis.virtualcompanion.domain.repository.SpeakerStyleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class SpeakerStyleRepositoryImpl : SpeakerStyleRepository {
    
    private val speakerStylesFlow = MutableStateFlow(getDefaultSpeakerStyles())
    
    override fun getAllSpeakerStyles(): Flow<List<SpeakerStyle>> = speakerStylesFlow
    
    override suspend fun getSpeakerStyleById(id: String): Result<SpeakerStyle?> {
        return try {
            val styles = speakerStylesFlow.value
            val style = styles.find { it.id == id }
            Result.Success(style)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getSpeakerStyleByName(name: String): Result<SpeakerStyle?> {
        return try {
            val styles = speakerStylesFlow.value
            val style = styles.find { it.name.equals(name, ignoreCase = true) }
            Result.Success(style)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun insertSpeakerStyle(speakerStyle: SpeakerStyle): Result<Long> {
        return try {
            val currentStyles = speakerStylesFlow.value.toMutableList()
            currentStyles.add(speakerStyle)
            speakerStylesFlow.value = currentStyles
            Result.Success(System.currentTimeMillis())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateSpeakerStyle(speakerStyle: SpeakerStyle): Result<Unit> {
        return try {
            val currentStyles = speakerStylesFlow.value.toMutableList()
            val index = currentStyles.indexOfFirst { it.id == speakerStyle.id }
            if (index >= 0) {
                currentStyles[index] = speakerStyle
                speakerStylesFlow.value = currentStyles
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deleteSpeakerStyle(id: String): Result<Unit> {
        return try {
            val currentStyles = speakerStylesFlow.value.toMutableList()
            currentStyles.removeAll { it.id == id }
            speakerStylesFlow.value = currentStyles
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getStylesByCharacteristics(characteristics: List<String>): Result<List<SpeakerStyle>> {
        return try {
            val styles = speakerStylesFlow.value
            val matchingStyles = styles.filter { style ->
                characteristics.any { char -> 
                    style.characteristics.any { styleChar -> 
                        styleChar.equals(char, ignoreCase = true) 
                    } 
                }
            }
            Result.Success(matchingStyles)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private fun getDefaultSpeakerStyles(): List<SpeakerStyle> {
        return listOf(
            SpeakerStyle(
                id = "casual",
                name = "Casual",
                characteristics = listOf("relaxed", "friendly", "informal"),
                emojiPreferences = listOf("😎", "👌", "🤙", "✌️", "😊"),
                slangTerms = listOf("bro", "dude", "man", "yo", "sup", "what's up"),
                repetitionPatterns = listOf("you know", "like that", "for real", "no cap")
            ),
            SpeakerStyle(
                id = "energetic",
                name = "Energetic",
                characteristics = listOf("high-energy", "enthusiastic", "lively"),
                emojiPreferences = listOf("🔥", "⚡", "🎉", "💪", "🚀", "✨"),
                slangTerms = listOf("lit", "fire", "slay", "go off", "let's go", "pumped"),
                repetitionPatterns = listOf("let's gooo", "yessir", "bang bang", "vibes")
            ),
            SpeakerStyle(
                id = "sarcastic",
                name = "Sarcastic",
                characteristics = listOf("dry", "mocking", "ironic"),
                emojiPreferences = listOf("🙄", "😏", "🤔", "👀", "💅", "🤷"),
                slangTerms = listOf("sure", "right", "obviously", "clearly", "totally"),
                repetitionPatterns = listOf("if you say so", "wow, just wow", "how original")
            ),
            SpeakerStyle(
                id = "friendly",
                name = "Friendly",
                characteristics = listOf("warm", "inviting", "supportive"),
                emojiPreferences = listOf("😊", "🤗", "💙", "🌈", "☀️", "🌸"),
                slangTerms = listOf("awesome", "amazing", "wonderful", "great", "fantastic"),
                repetitionPatterns = listOf("you got this", "so proud", "love that", "keep going")
            ),
            SpeakerStyle(
                id = "playful",
                name = "Playful",
                characteristics = listOf("fun", "cheerful", "bouncy"),
                emojiPreferences = listOf("😄", "😂", "🤪", "😜", "🎮", "🎭"),
                slangTerms = listOf("lol", "lmao", "haha", "hehe", "funny", "silly"),
                repetitionPatterns = listOf("silly goose", "fun times", "play time", "game on")
            ),
            SpeakerStyle(
                id = "serious",
                name = "Serious",
                characteristics = listOf("formal", "measured", "thoughtful"),
                emojiPreferences = listOf("📝", "💭", "🤔", "📊", "🎯", "🔍"),
                slangTerms = listOf("indeed", "certainly", "precisely", "exactly", "absolutely"),
                repetitionPatterns = listOf("let me think", "quite interesting", "importantly")
            ),
            SpeakerStyle(
                id = "formal",
                name = "Formal",
                characteristics = listOf("professional", "polished", "respectful"),
                emojiPreferences = listOf("🤝", "📊", "📈", "💼", "🎯", "⚖️"),
                slangTerms = listOf("pleasure", "excellent", "outstanding", "remarkable", "impressive"),
                repetitionPatterns = listOf("it would seem", "one might say", "it appears that")
            )
        )
    }
}