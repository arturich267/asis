package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.dao.PhraseStatDao
import com.asis.virtualcompanion.data.database.entity.PhraseStatEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PhraseStatRepositoryTest {
    
    private lateinit var phraseStatDao: PhraseStatDao
    private lateinit var repository: PhraseStatRepositoryImpl
    
    @Before
    fun setup() {
        phraseStatDao = mock()
        repository = PhraseStatRepositoryImpl(phraseStatDao)
    }
    
    @Test
    fun `getTopPhrases returns success with data`() = runTest {
        val phrases = listOf(
            PhraseStatEntity("hello", 10, listOf("👋")),
            PhraseStatEntity("goodbye", 5, listOf("👋", "😢"))
        )
        whenever(phraseStatDao.getTopPhrases(10)).thenReturn(phrases)
        
        val result = repository.getTopPhrases(10)
        
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        verify(phraseStatDao).getTopPhrases(10)
    }
    
    @Test
    fun `insertPhraseStat returns success with id`() = runTest {
        val phrase = PhraseStatEntity("test", 1, emptyList())
        val insertedId = 1L
        whenever(phraseStatDao.insert(phrase)).thenReturn(insertedId)
        
        val result = repository.insertPhraseStat(phrase)
        
        assertTrue(result is Result.Success)
        assertEquals(insertedId, (result as Result.Success).data)
        verify(phraseStatDao).insert(phrase)
    }
    
    @Test
    fun `incrementPhraseCount returns success true when updated`() = runTest {
        val phrase = "hello"
        whenever(phraseStatDao.incrementPhraseCount(phrase)).thenReturn(1)
        
        val result = repository.incrementPhraseCount(phrase)
        
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data)
        verify(phraseStatDao).incrementPhraseCount(phrase)
    }
    
    @Test
    fun `incrementPhraseCount returns success false when not updated`() = runTest {
        val phrase = "nonexistent"
        whenever(phraseStatDao.incrementPhraseCount(phrase)).thenReturn(0)
        
        val result = repository.incrementPhraseCount(phrase)
        
        assertTrue(result is Result.Success)
        assertFalse((result as Result.Success).data)
    }
    
    @Test
    fun `insertOrUpdatePhraseStat updates existing phrase`() = runTest {
        val phrase = PhraseStatEntity("hello", 5, listOf("👋"))
        val existing = PhraseStatEntity("hello", 4, listOf("👋"))
        whenever(phraseStatDao.getPhraseStatByPhrase("hello")).thenReturn(existing)
        
        val result = repository.insertOrUpdatePhraseStat(phrase)
        
        assertTrue(result is Result.Success)
        verify(phraseStatDao).update(phrase)
    }
    
    @Test
    fun `insertOrUpdatePhraseStat inserts new phrase`() = runTest {
        val phrase = PhraseStatEntity("new", 1, emptyList())
        whenever(phraseStatDao.getPhraseStatByPhrase("new")).thenReturn(null)
        whenever(phraseStatDao.insert(phrase)).thenReturn(1L)
        
        val result = repository.insertOrUpdatePhraseStat(phrase)
        
        assertTrue(result is Result.Success)
        verify(phraseStatDao).insert(phrase)
    }
    
    @Test
    fun `getTotalPhraseCount returns zero when null`() = runTest {
        whenever(phraseStatDao.getTotalPhraseCount()).thenReturn(null)
        
        val result = repository.getTotalPhraseCount()
        
        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data)
    }
}
