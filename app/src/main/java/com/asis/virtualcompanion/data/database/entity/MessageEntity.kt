package com.asis.virtualcompanion.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["timestamp"], name = "idx_message_timestamp"),
        Index(value = ["sender"], name = "idx_message_sender")
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "sender")
    val sender: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Date,
    
    @ColumnInfo(name = "text")
    val text: String,
    
    @ColumnInfo(name = "sentiment")
    val sentiment: String? = null,
    
    @ColumnInfo(name = "context_tags")
    val contextTags: List<String> = emptyList()
)
