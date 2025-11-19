package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.SpeakerStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpeakerStyleRepositoryImplTest {
    
    private lateinit var repository: SpeakerStyleRepositoryImpl
    
    @Before
    fun setup() {
        repository = SpeakerStyleRepositoryImpl()
    }
    
    @Test
    fun `getAllSpeakerStyles returns default styles`() = runTest {
        // When
        val styles = repository.getAllSpeakerStyles().first()
        
        // Then
        assertTrue(styles.isNotEmpty())
        assertTrue(styles.any { it.name == "Casual" })
        assertTrue(styles.any { it.name == "Energetic" })
        assertTrue(styles.any { it.name == "Sarcastic" })
        assertTrue(styles.any { it.name == "Friendly" })
        assertTrue(styles.any { it.name == "Playful" })
        assertTrue(styles.any { it.name == "Serious" })
        assertTrue(styles.any { it.name == "Formal" })
    }
    
    @Test
    fun `getSpeakerStyleById returns correct style`() = runTest {
        // Given - Add a custom style
        val customStyle = SpeakerStyle(
            id = "test_style",
            name = "Test Style",
            characteristics = listOf("test"),
            emojiPreferences = listOf("🧪"),
            slangTerms = listOf("testing"),
            repetitionPatterns = listOf("test test")
        )
        repository.insertSpeakerStyle(customStyle)
        
        // When
        val result = repository.getSpeakerStyleById("test_style")
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertNotNull(style)
        assertEquals("test_style", style?.id)
        assertEquals("Test Style", style?.name)
    }
    
    @Test
    fun `getSpeakerStyleById with non-existent id returns null`() = runTest {
        // When
        val result = repository.getSpeakerStyleById("non_existent")
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertEquals(null, style)
    }
    
    @Test
    fun `getSpeakerStyleByName returns correct style`() = runTest {
        // When
        val result = repository.getSpeakerStyleByName("Casual")
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertNotNull(style)
        assertEquals("Casual", style?.name)
        assertEquals("casual", style?.id)
    }
    
    @Test
    fun `getSpeakerStyleByName with case insensitive search works`() = runTest {
        // When
        val result = repository.getSpeakerStyleByName("ENERGETIC")
        
        // Then
        assertTrue(result is Result.Success)
        val style = (result as Result.Success).data
        assertNotNull(style)
        assertEquals("Energetic", style?.name)
    }
    
    @Test
    fun `insertSpeakerStyle adds new style`() = runTest {
        // Given
        val newStyle = SpeakerStyle(
            id = "new_style",
            name = "New Style",
            characteristics = listOf("new"),
            emojiPreferences = listOf("✨"),
            slangTerms = listOf("newbie"),
            repetitionPatterns = listOf("new new")
        )
        
        // When
        val result = repository.insertSpeakerStyle(newStyle)
        
        // Then
        assertTrue(result is Result.Success)
        val insertId = (result as Result.Success).data
        assertTrue(insertId > 0)
        
        // Verify it was added
        val styles = repository.getAllSpeakerStyles().first()
        assertTrue(styles.any { it.id == "new_style" })
    }
    
    @Test
    fun `updateSpeakerStyle modifies existing style`() = runTest {
        // Given - Get existing style
        val getResult = repository.getSpeakerStyleByName("Friendly")
        assertTrue(getResult is Result.Success)
        val originalStyle = (getResult as Result.Success).data!!
        
        val updatedStyle = originalStyle.copy(
            name = "Updated Friendly",
            characteristics = listOf("updated", "warm")
        )
        
        // When
        val result = repository.updateSpeakerStyle(updatedStyle)
        
        // Then
        assertTrue(result is Result.Success)
        
        // Verify it was updated
        val getResult2 = repository.getSpeakerStyleById("friendly")
        assertTrue(getResult2 is Result.Success)
        val retrievedStyle = (getResult2 as Result.Success).data
        assertEquals("Updated Friendly", retrievedStyle?.name)
        assertTrue(retrievedStyle?.characteristics?.contains("updated") == true)
    }
    
    @Test
    fun `deleteSpeakerStyle removes style`() = runTest {
        // Given - Add a style to delete
        val tempStyle = SpeakerStyle(
            id = "temp_style",
            name = "Temp Style",
            characteristics = listOf("temp"),
            emojiPreferences = listOf("⏰"),
            slangTerms = listOf("temporary"),
            repetitionPatterns = listOf("temp temp")
        )
        repository.insertSpeakerStyle(tempStyle)
        
        // Verify it exists
        val getResult = repository.getSpeakerStyleById("temp_style")
        assertTrue(getResult is Result.Success)
        assertNotNull((getResult as Result.Success).data)
        
        // When
        val deleteResult = repository.deleteSpeakerStyle("temp_style")
        
        // Then
        assertTrue(deleteResult is Result.Success)
        
        // Verify it was deleted
        val getResult2 = repository.getSpeakerStyleById("temp_style")
        assertTrue(getResult2 is Result.Success)
        assertEquals(null, (getResult2 as Result.Success).data)
    }
    
    @Test
    fun `getStylesByCharacteristics returns matching styles`() = runTest {
        // When
        val result = repository.getStylesByCharacteristics(listOf("friendly", "warm"))
        
        // Then
        assertTrue(result is Result.Success)
        val styles = (result as Result.Success).data
        assertTrue(styles.isNotEmpty())
        assertTrue(styles.any { it.name == "Friendly" })
    }
    
    @Test
    fun `getStylesByCharacteristics with partial match returns styles`() = runTest {
        // When
        val result = repository.getStylesByCharacteristics(listOf("energy"))
        
        // Then
        assertTrue(result is Result.Success)
        val styles = (result as Result.Success).data
        assertTrue(styles.isNotEmpty())
        assertTrue(styles.any { it.name == "Energetic" })
    }
    
    @Test
    fun `getStylesByCharacteristics with no matches returns empty list`() = runTest {
        // When
        val result = repository.getStylesByCharacteristics(listOf("nonexistent"))
        
        // Then
        assertTrue(result is Result.Success)
        val styles = (result as Result.Success).data
        assertTrue(styles.isEmpty())
    }
    
    @Test
    fun `default styles have correct properties`() = runTest {
        // When
        val styles = repository.getAllSpeakerStyles().first()
        
        // Then - Verify casual style
        val casual = styles.find { it.id == "casual" }
        assertNotNull(casual)
        assertEquals("Casual", casual.name)
        assertTrue(casual.characteristics.contains("relaxed"))
        assertTrue(casual.emojiPreferences.contains("😎"))
        assertTrue(casual.slangTerms.contains("bro"))
        assertTrue(casual.repetitionPatterns.contains("you know"))
        
        // Verify energetic style
        val energetic = styles.find { it.id == "energetic" }
        assertNotNull(energetic)
        assertEquals("Energetic", energetic.name)
        assertTrue(energetic.characteristics.contains("high-energy"))
        assertTrue(energetic.emojiPreferences.contains("🔥"))
        assertTrue(energetic.slangTerms.contains("lit"))
        assertTrue(energetic.repetitionPatterns.contains("let's gooo"))
        
        // Verify sarcastic style
        val sarcastic = styles.find { it.id == "sarcastic" }
        assertNotNull(sarcastic)
        assertEquals("Sarcastic", sarcastic.name)
        assertTrue(sarcastic.characteristics.contains("dry"))
        assertTrue(sarcastic.emojiPreferences.contains("🙄"))
        assertTrue(sarcastic.slangTerms.contains("sure"))
        assertTrue(sarcastic.repetitionPatterns.contains("if you say so"))
    }
}