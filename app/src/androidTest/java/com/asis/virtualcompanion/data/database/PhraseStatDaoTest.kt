package com.asis.virtualcompanion.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.data.database.dao.PhraseStatDao
import com.asis.virtualcompanion.data.database.entity.PhraseStatEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhraseStatDaoTest {
    
    private lateinit var database: AsisDatabase
    private lateinit var phraseStatDao: PhraseStatDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java
        ).allowMainThreadQueries().build()
        
        phraseStatDao = database.phraseStatDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndGetPhraseStat() = runTest {
        val phraseStat = PhraseStatEntity(
            phrase = "hello",
            count = 5,
            emojiHints = listOf("👋", "😊")
        )
        
        phraseStatDao.insert(phraseStat)
        
        val phraseStats = phraseStatDao.getAllPhraseStats().first()
        assertEquals(1, phraseStats.size)
        assertEquals("hello", phraseStats[0].phrase)
        assertEquals(5, phraseStats[0].count)
        assertEquals(2, phraseStats[0].emojiHints.size)
    }
    
    @Test
    fun getTopPhrases() = runTest {
        phraseStatDao.insert(PhraseStatEntity("hello", 10, emptyList()))
        phraseStatDao.insert(PhraseStatEntity("goodbye", 5, emptyList()))
        phraseStatDao.insert(PhraseStatEntity("thanks", 8, emptyList()))
        phraseStatDao.insert(PhraseStatEntity("please", 3, emptyList()))
        
        val topPhrases = phraseStatDao.getTopPhrases(2)
        assertEquals(2, topPhrases.size)
        assertEquals("hello", topPhrases[0].phrase)
        assertEquals(10, topPhrases[0].count)
        assertEquals("thanks", topPhrases[1].phrase)
        assertEquals(8, topPhrases[1].count)
    }
    
    @Test
    fun getPhraseStatByPhrase() = runTest {
        phraseStatDao.insert(PhraseStatEntity("hello", 5, listOf("👋")))
        
        val phraseStat = phraseStatDao.getPhraseStatByPhrase("hello")
        assertNotNull(phraseStat)
        assertEquals("hello", phraseStat?.phrase)
        assertEquals(5, phraseStat?.count)
        
        val notFound = phraseStatDao.getPhraseStatByPhrase("nonexistent")
        assertNull(notFound)
    }
    
    @Test
    fun incrementPhraseCount() = runTest {
        phraseStatDao.insert(PhraseStatEntity("hello", 5, emptyList()))
        
        val rowsUpdated = phraseStatDao.incrementPhraseCount("hello")
        assertEquals(1, rowsUpdated)
        
        val phraseStat = phraseStatDao.getPhraseStatByPhrase("hello")
        assertEquals(6, phraseStat?.count)
        
        val notUpdated = phraseStatDao.incrementPhraseCount("nonexistent")
        assertEquals(0, notUpdated)
    }
    
    @Test
    fun deleteLowCountPhrases() = runTest {
        phraseStatDao.insert(PhraseStatEntity("hello", 10, emptyList()))
        phraseStatDao.insert(PhraseStatEntity("goodbye", 2, emptyList()))
        phraseStatDao.insert(PhraseStatEntity("thanks", 8, emptyList()))
        phraseStatDao.insert(PhraseStatEntity("please", 1, emptyList()))
        
        val deletedCount = phraseStatDao.deleteLowCountPhrases(5)
        assertEquals(2, deletedCount)
        
        val remaining = phraseStatDao.getAllPhraseStats().first()
        assertEquals(2, remaining.size)
    }
    
    @Test
    fun getTotalPhraseCount() = runTest {
        assertEquals(null, phraseStatDao.getTotalPhraseCount())
        
        phraseStatDao.insert(PhraseStatEntity("hello", 10, emptyList()))
        phraseStatDao.insert(PhraseStatEntity("goodbye", 5, emptyList()))
        
        val total = phraseStatDao.getTotalPhraseCount()
        assertEquals(15, total)
    }
}
