package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.dao.VoiceMetaDao
import com.asis.virtualcompanion.data.database.entity.VoiceMetaEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class VoiceRepositoryTest {
    
    private lateinit var voiceMetaDao: VoiceMetaDao
    private lateinit var repository: VoiceRepositoryImpl
    
    @Before
    fun setup() {
        voiceMetaDao = mock()
        repository = VoiceRepositoryImpl(voiceMetaDao)
    }
    
    @Test
    fun `getAllVoiceMeta returns flow from dao`() = runTest {
        val voiceMeta = listOf(
            VoiceMetaEntity("clip1", "happy", "greeting", "/path/to/clip1", 5000)
        )
        val flow = flowOf(voiceMeta)
        whenever(voiceMetaDao.getAllVoiceMeta()).thenReturn(flow)
        
        val result = repository.getAllVoiceMeta()
        
        assertEquals(flow, result)
        verify(voiceMetaDao).getAllVoiceMeta()
    }
    
    @Test
    fun `getVoiceMetaByClipId returns success with data`() = runTest {
        val voiceMeta = VoiceMetaEntity("clip1", "happy", "greeting", "/path/to/clip1", 5000)
        whenever(voiceMetaDao.getVoiceMetaByClipId("clip1")).thenReturn(voiceMeta)
        
        val result = repository.getVoiceMetaByClipId("clip1")
        
        assertTrue(result is Result.Success)
        assertEquals(voiceMeta, (result as Result.Success).data)
        verify(voiceMetaDao).getVoiceMetaByClipId("clip1")
    }
    
    @Test
    fun `insertVoiceMeta returns success with id`() = runTest {
        val voiceMeta = VoiceMetaEntity("clip1", "happy", "greeting", "/path/to/clip1", 5000)
        val insertedId = 1L
        whenever(voiceMetaDao.insert(voiceMeta)).thenReturn(insertedId)
        
        val result = repository.insertVoiceMeta(voiceMeta)
        
        assertTrue(result is Result.Success)
        assertEquals(insertedId, (result as Result.Success).data)
        verify(voiceMetaDao).insert(voiceMeta)
    }
    
    @Test
    fun `getVoiceMetaByEmotionAndTopic returns success with list`() = runTest {
        val voiceMetaList = listOf(
            VoiceMetaEntity("clip1", "happy", "greeting", "/path/to/clip1", 5000),
            VoiceMetaEntity("clip2", "happy", "greeting", "/path/to/clip2", 4000)
        )
        whenever(voiceMetaDao.getVoiceMetaByEmotionAndTopic("happy", "greeting"))
            .thenReturn(voiceMetaList)
        
        val result = repository.getVoiceMetaByEmotionAndTopic("happy", "greeting")
        
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
    }
    
    @Test
    fun `getTotalDuration returns zero when null`() = runTest {
        whenever(voiceMetaDao.getTotalDuration()).thenReturn(null)
        
        val result = repository.getTotalDuration()
        
        assertTrue(result is Result.Success)
        assertEquals(0L, (result as Result.Success).data)
    }
    
    @Test
    fun `deleteVoiceMetaByClipId returns success with count`() = runTest {
        whenever(voiceMetaDao.deleteVoiceMetaByClipId("clip1")).thenReturn(1)
        
        val result = repository.deleteVoiceMetaByClipId("clip1")
        
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data)
    }
}
