package com.asis.virtualcompanion.domain.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.model.SpeakerStyle
import kotlinx.coroutines.flow.Flow

interface SpeakerStyleRepository : BaseRepository {
    
    fun getAllSpeakerStyles(): Flow<List<SpeakerStyle>>
    
    suspend fun getSpeakerStyleById(id: String): Result<SpeakerStyle?>
    
    suspend fun getSpeakerStyleByName(name: String): Result<SpeakerStyle?>
    
    suspend fun insertSpeakerStyle(speakerStyle: SpeakerStyle): Result<Long>
    
    suspend fun updateSpeakerStyle(speakerStyle: SpeakerStyle): Result<Unit>
    
    suspend fun deleteSpeakerStyle(id: String): Result<Unit>
    
    suspend fun getStylesByCharacteristics(characteristics: List<String>): Result<List<SpeakerStyle>>
}