package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.dao.VoiceMetaDao
import com.asis.virtualcompanion.data.database.entity.VoiceMetaEntity
import com.asis.virtualcompanion.domain.repository.VoiceRepository
import kotlinx.coroutines.flow.Flow

class VoiceRepositoryImpl(
    private val voiceMetaDao: VoiceMetaDao
) : BaseRepositoryImpl(), VoiceRepository {
    
    override fun getAllVoiceMeta(): Flow<List<VoiceMetaEntity>> {
        return voiceMetaDao.getAllVoiceMeta()
    }
    
    override suspend fun getVoiceMetaByClipId(clipId: String): Result<VoiceMetaEntity?> {
        return safeCall {
            voiceMetaDao.getVoiceMetaByClipId(clipId)
        }
    }
    
    override fun getVoiceMetaByEmotion(emotion: String): Flow<List<VoiceMetaEntity>> {
        return voiceMetaDao.getVoiceMetaByEmotion(emotion)
    }
    
    override fun getVoiceMetaByTopic(topic: String): Flow<List<VoiceMetaEntity>> {
        return voiceMetaDao.getVoiceMetaByTopic(topic)
    }
    
    override suspend fun getVoiceMetaByEmotionAndTopic(
        emotion: String,
        topic: String
    ): Result<List<VoiceMetaEntity>> {
        return safeCall {
            voiceMetaDao.getVoiceMetaByEmotionAndTopic(emotion, topic)
        }
    }
    
    override suspend fun insertVoiceMeta(voiceMeta: VoiceMetaEntity): Result<Long> {
        return safeCall {
            voiceMetaDao.insert(voiceMeta)
        }
    }
    
    override suspend fun insertVoiceMetaList(voiceMetaList: List<VoiceMetaEntity>): Result<List<Long>> {
        return safeCall {
            voiceMetaDao.insertAll(voiceMetaList)
        }
    }
    
    override suspend fun updateVoiceMeta(voiceMeta: VoiceMetaEntity): Result<Unit> {
        return safeCall {
            voiceMetaDao.update(voiceMeta)
        }
    }
    
    override suspend fun deleteVoiceMeta(voiceMeta: VoiceMetaEntity): Result<Unit> {
        return safeCall {
            voiceMetaDao.delete(voiceMeta)
        }
    }
    
    override suspend fun deleteVoiceMetaByClipId(clipId: String): Result<Int> {
        return safeCall {
            voiceMetaDao.deleteVoiceMetaByClipId(clipId)
        }
    }
    
    override suspend fun getVoiceMetaCount(): Result<Int> {
        return safeCall {
            voiceMetaDao.getVoiceMetaCount()
        }
    }
    
    override suspend fun getTotalDuration(): Result<Long> {
        return safeCall {
            voiceMetaDao.getTotalDuration() ?: 0L
        }
    }
}
