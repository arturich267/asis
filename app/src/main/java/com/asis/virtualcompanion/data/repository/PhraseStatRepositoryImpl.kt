package com.asis.virtualcompanion.data.repository

import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.dao.PhraseStatDao
import com.asis.virtualcompanion.data.database.entity.PhraseStatEntity
import com.asis.virtualcompanion.domain.repository.PhraseStatRepository
import kotlinx.coroutines.flow.Flow

class PhraseStatRepositoryImpl(
    private val phraseStatDao: PhraseStatDao
) : BaseRepositoryImpl(), PhraseStatRepository {
    
    override fun getAllPhraseStats(): Flow<List<PhraseStatEntity>> {
        return phraseStatDao.getAllPhraseStats()
    }
    
    override suspend fun getTopPhrases(limit: Int): Result<List<PhraseStatEntity>> {
        return safeCall {
            phraseStatDao.getTopPhrases(limit)
        }
    }
    
    override suspend fun getPhraseStatByPhrase(phrase: String): Result<PhraseStatEntity?> {
        return safeCall {
            phraseStatDao.getPhraseStatByPhrase(phrase)
        }
    }
    
    override suspend fun insertPhraseStat(phraseStat: PhraseStatEntity): Result<Long> {
        return safeCall {
            phraseStatDao.insert(phraseStat)
        }
    }
    
    override suspend fun insertOrUpdatePhraseStat(phraseStat: PhraseStatEntity): Result<Unit> {
        return safeCall {
            val existing = phraseStatDao.getPhraseStatByPhrase(phraseStat.phrase)
            if (existing != null) {
                phraseStatDao.update(phraseStat)
            } else {
                phraseStatDao.insert(phraseStat)
            }
        }
    }
    
    override suspend fun incrementPhraseCount(phrase: String): Result<Boolean> {
        return safeCall {
            val rowsUpdated = phraseStatDao.incrementPhraseCount(phrase)
            rowsUpdated > 0
        }
    }
    
    override suspend fun updatePhraseStat(phraseStat: PhraseStatEntity): Result<Unit> {
        return safeCall {
            phraseStatDao.update(phraseStat)
        }
    }
    
    override suspend fun deletePhraseStat(phraseStat: PhraseStatEntity): Result<Unit> {
        return safeCall {
            phraseStatDao.delete(phraseStat)
        }
    }
    
    override suspend fun deleteLowCountPhrases(minCount: Int): Result<Int> {
        return safeCall {
            phraseStatDao.deleteLowCountPhrases(minCount)
        }
    }
    
    override suspend fun getTotalPhraseCount(): Result<Int> {
        return safeCall {
            phraseStatDao.getTotalPhraseCount() ?: 0
        }
    }
}
