package com.asis.virtualcompanion.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["timestamp"], name = "idx_chat_timestamp"),
        Index(value = ["is_from_user"], name = "idx_chat_is_from_user")
    ]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "message")
    val message: String,
    
    @ColumnInfo(name = "is_from_user")
    val isFromUser: Boolean,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Date = Date(),
    
    @ColumnInfo(name = "metadata")
    val metadata: Map<String, String>? = null
)
