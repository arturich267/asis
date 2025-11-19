package com.asis.virtualcompanion.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.asis.virtualcompanion.data.database.entity.PhraseStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseStatDao : BaseDao<PhraseStatEntity> {
    
    @Query("SELECT * FROM phrase_stats ORDER BY count DESC")
    fun getAllPhraseStats(): Flow<List<PhraseStatEntity>>
    
    @Query("SELECT * FROM phrase_stats ORDER BY count DESC LIMIT :limit")
    suspend fun getTopPhrases(limit: Int): List<PhraseStatEntity>
    
    @Query("SELECT * FROM phrase_stats WHERE phrase = :phrase")
    suspend fun getPhraseStatByPhrase(phrase: String): PhraseStatEntity?
    
    @Query("UPDATE phrase_stats SET count = count + 1 WHERE phrase = :phrase")
    suspend fun incrementPhraseCount(phrase: String): Int
    
    @Query("DELETE FROM phrase_stats WHERE count < :minCount")
    suspend fun deleteLowCountPhrases(minCount: Int): Int
    
    @Query("SELECT SUM(count) FROM phrase_stats")
    suspend fun getTotalPhraseCount(): Int?
}
