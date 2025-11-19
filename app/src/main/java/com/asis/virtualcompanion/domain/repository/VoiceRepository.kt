package com.asis.virtualcompanion.domain.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.entity.VoiceMetaEntity
import kotlinx.coroutines.flow.Flow

interface VoiceRepository : BaseRepository {
    
    fun getAllVoiceMeta(): Flow<List<VoiceMetaEntity>>
    
    suspend fun getVoiceMetaByClipId(clipId: String): Result<VoiceMetaEntity?>
    
    fun getVoiceMetaByEmotion(emotion: String): Flow<List<VoiceMetaEntity>>
    
    fun getVoiceMetaByTopic(topic: String): Flow<List<VoiceMetaEntity>>
    
    suspend fun getVoiceMetaByEmotionAndTopic(emotion: String, topic: String): Result<List<VoiceMetaEntity>>
    
    suspend fun insertVoiceMeta(voiceMeta: VoiceMetaEntity): Result<Long>
    
    suspend fun insertVoiceMetaList(voiceMetaList: List<VoiceMetaEntity>): Result<List<Long>>
    
    suspend fun updateVoiceMeta(voiceMeta: VoiceMetaEntity): Result<Unit>
    
    suspend fun deleteVoiceMeta(voiceMeta: VoiceMetaEntity): Result<Unit>
    
    suspend fun deleteVoiceMetaByClipId(clipId: String): Result<Int>
    
    suspend fun getVoiceMetaCount(): Result<Int>
    
    suspend fun getTotalDuration(): Result<Long>
}
