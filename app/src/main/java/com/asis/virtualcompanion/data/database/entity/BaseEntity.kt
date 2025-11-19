package com.asis.virtualcompanion.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Base entity with common fields
 */
abstract class BaseEntity {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
    
    @ColumnInfo(name = "created_at")
    var createdAt: Date = Date()
    
    @ColumnInfo(name = "updated_at")
    var updatedAt: Date = Date()
}