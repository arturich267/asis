package com.asis.virtualcompanion.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.asis.virtualcompanion.data.database.entity.VoiceMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceMetaDao : BaseDao<VoiceMetaEntity> {
    
    @Query("SELECT * FROM voice_meta ORDER BY clip_id")
    fun getAllVoiceMeta(): Flow<List<VoiceMetaEntity>>
    
    @Query("SELECT * FROM voice_meta WHERE sender = :sender")
    fun getVoiceMetaBySender(sender: String): Flow<List<VoiceMetaEntity>>
    
    @Query("SELECT * FROM voice_meta WHERE clip_id = :clipId")
    suspend fun getVoiceMetaByClipId(clipId: String): VoiceMetaEntity?
    
    @Query("SELECT * FROM voice_meta WHERE emotion = :emotion")
    fun getVoiceMetaByEmotion(emotion: String): Flow<List<VoiceMetaEntity>>
    
    @Query("SELECT * FROM voice_meta WHERE topic = :topic")
    fun getVoiceMetaByTopic(topic: String): Flow<List<VoiceMetaEntity>>
    
    @Query("SELECT * FROM voice_meta WHERE emotion = :emotion AND topic = :topic")
    suspend fun getVoiceMetaByEmotionAndTopic(emotion: String, topic: String): List<VoiceMetaEntity>
    
    @Query("DELETE FROM voice_meta WHERE clip_id = :clipId")
    suspend fun deleteVoiceMetaByClipId(clipId: String): Int
    
    @Query("SELECT COUNT(*) FROM voice_meta")
    suspend fun getVoiceMetaCount(): Int
    
    @Query("SELECT SUM(duration) FROM voice_meta")
    suspend fun getTotalDuration(): Long?
}
