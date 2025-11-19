package com.asis.virtualcompanion.domain.repository

import com.asis.virtualcompanion.common.Result

/**
 * Base repository interface with common operations
 */
interface BaseRepository {
    
    /**
     * Generic method to handle results
     */
    suspend fun <T> safeCall(action: suspend () -> T): Result<T> {
        return try {
            Result.Success(action())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}