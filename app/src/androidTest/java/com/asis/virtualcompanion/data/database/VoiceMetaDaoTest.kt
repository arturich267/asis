package com.asis.virtualcompanion.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.data.database.dao.VoiceMetaDao
import com.asis.virtualcompanion.data.database.entity.VoiceMetaEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceMetaDaoTest {
    
    private lateinit var database: AsisDatabase
    private lateinit var voiceMetaDao: VoiceMetaDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java
        ).allowMainThreadQueries().build()
        
        voiceMetaDao = database.voiceMetaDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndGetVoiceMeta() = runTest {
        val voiceMeta = VoiceMetaEntity(
            clipId = "clip001",
            emotion = "happy",
            topic = "greeting",
            filePath = "/storage/voice/clip001.wav",
            duration = 5000
        )
        
        voiceMetaDao.insert(voiceMeta)
        
        val allVoiceMeta = voiceMetaDao.getAllVoiceMeta().first()
        assertEquals(1, allVoiceMeta.size)
        assertEquals("clip001", allVoiceMeta[0].clipId)
        assertEquals("happy", allVoiceMeta[0].emotion)
        assertEquals(5000, allVoiceMeta[0].duration)
    }
    
    @Test
    fun getVoiceMetaByClipId() = runTest {
        voiceMetaDao.insert(VoiceMetaEntity("clip001", "happy", "greeting", "/path/clip001", 5000))
        voiceMetaDao.insert(VoiceMetaEntity("clip002", "sad", "farewell", "/path/clip002", 4000))
        
        val voiceMeta = voiceMetaDao.getVoiceMetaByClipId("clip001")
        assertNotNull(voiceMeta)
        assertEquals("happy", voiceMeta?.emotion)
        
        val notFound = voiceMetaDao.getVoiceMetaByClipId("clip999")
        assertNull(notFound)
    }
    
    @Test
    fun getVoiceMetaByEmotion() = runTest {
        voiceMetaDao.insert(VoiceMetaEntity("clip001", "happy", "greeting", "/path/clip001", 5000))
        voiceMetaDao.insert(VoiceMetaEntity("clip002", "happy", "farewell", "/path/clip002", 4000))
        voiceMetaDao.insert(VoiceMetaEntity("clip003", "sad", "farewell", "/path/clip003", 3000))
        
        val happyVoices = voiceMetaDao.getVoiceMetaByEmotion("happy").first()
        assertEquals(2, happyVoices.size)
        
        val sadVoices = voiceMetaDao.getVoiceMetaByEmotion("sad").first()
        assertEquals(1, sadVoices.size)
    }
    
    @Test
    fun getVoiceMetaByTopic() = runTest {
        voiceMetaDao.insert(VoiceMetaEntity("clip001", "happy", "greeting", "/path/clip001", 5000))
        voiceMetaDao.insert(VoiceMetaEntity("clip002", "sad", "greeting", "/path/clip002", 4000))
        voiceMetaDao.insert(VoiceMetaEntity("clip003", "happy", "farewell", "/path/clip003", 3000))
        
        val greetingVoices = voiceMetaDao.getVoiceMetaByTopic("greeting").first()
        assertEquals(2, greetingVoices.size)
    }
    
    @Test
    fun getVoiceMetaByEmotionAndTopic() = runTest {
        voiceMetaDao.insert(VoiceMetaEntity("clip001", "happy", "greeting", "/path/clip001", 5000))
        voiceMetaDao.insert(VoiceMetaEntity("clip002", "happy", "greeting", "/path/clip002", 4000))
        voiceMetaDao.insert(VoiceMetaEntity("clip003", "happy", "farewell", "/path/clip003", 3000))
        voiceMetaDao.insert(VoiceMetaEntity("clip004", "sad", "greeting", "/path/clip004", 2000))
        
        val happyGreetings = voiceMetaDao.getVoiceMetaByEmotionAndTopic("happy", "greeting")
        assertEquals(2, happyGreetings.size)
    }
    
    @Test
    fun deleteVoiceMetaByClipId() = runTest {
        voiceMetaDao.insert(VoiceMetaEntity("clip001", "happy", "greeting", "/path/clip001", 5000))
        voiceMetaDao.insert(VoiceMetaEntity("clip002", "sad", "farewell", "/path/clip002", 4000))
        
        val deletedCount = voiceMetaDao.deleteVoiceMetaByClipId("clip001")
        assertEquals(1, deletedCount)
        
        val remaining = voiceMetaDao.getAllVoiceMeta().first()
        assertEquals(1, remaining.size)
        assertEquals("clip002", remaining[0].clipId)
    }
    
    @Test
    fun getVoiceMetaCount() = runTest {
        assertEquals(0, voiceMetaDao.getVoiceMetaCount())
        
        voiceMetaDao.insert(VoiceMetaEntity("clip001", "happy", "greeting", "/path/clip001", 5000))
        voiceMetaDao.insert(VoiceMetaEntity("clip002", "sad", "farewell", "/path/clip002", 4000))
        
        assertEquals(2, voiceMetaDao.getVoiceMetaCount())
    }
    
    @Test
    fun getTotalDuration() = runTest {
        assertNull(voiceMetaDao.getTotalDuration())
        
        voiceMetaDao.insert(VoiceMetaEntity("clip001", "happy", "greeting", "/path/clip001", 5000))
        voiceMetaDao.insert(VoiceMetaEntity("clip002", "sad", "farewell", "/path/clip002", 4000))
        
        val totalDuration = voiceMetaDao.getTotalDuration()
        assertEquals(9000L, totalDuration)
    }
}
