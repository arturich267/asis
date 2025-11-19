package com.asis.virtualcompanion.data.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * Base DAO with common operations
 */
interface BaseDao<T> {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<T>): List<Long>
    
    @Update
    suspend fun update(entity: T)
    
    @Delete
    suspend fun delete(entity: T)
}