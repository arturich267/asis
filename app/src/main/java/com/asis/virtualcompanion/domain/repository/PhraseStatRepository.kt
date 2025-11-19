package com.asis.virtualcompanion.domain.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.entity.PhraseStatEntity
import kotlinx.coroutines.flow.Flow

interface PhraseStatRepository : BaseRepository {
    
    fun getAllPhraseStats(): Flow<List<PhraseStatEntity>>
    
    suspend fun getTopPhrases(limit: Int): Result<List<PhraseStatEntity>>
    
    suspend fun getPhraseStatByPhrase(phrase: String): Result<PhraseStatEntity?>
    
    suspend fun insertPhraseStat(phraseStat: PhraseStatEntity): Result<Long>
    
    suspend fun insertOrUpdatePhraseStat(phraseStat: PhraseStatEntity): Result<Unit>
    
    suspend fun incrementPhraseCount(phrase: String): Result<Boolean>
    
    suspend fun updatePhraseStat(phraseStat: PhraseStatEntity): Result<Unit>
    
    suspend fun deletePhraseStat(phraseStat: PhraseStatEntity): Result<Unit>
    
    suspend fun deleteLowCountPhrases(minCount: Int): Result<Int>
    
    suspend fun getTotalPhraseCount(): Result<Int>
}
