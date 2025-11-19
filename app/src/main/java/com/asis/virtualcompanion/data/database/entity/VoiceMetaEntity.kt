package com.asis.virtualcompanion.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "voice_meta",
    indices = [
        Index(value = ["emotion"], name = "idx_voice_emotion"),
        Index(value = ["topic"], name = "idx_voice_topic")
    ]
)
data class VoiceMetaEntity(
    @PrimaryKey
    @ColumnInfo(name = "clip_id")
    val clipId: String,
    
    @ColumnInfo(name = "emotion")
    val emotion: String? = null,
    
    @ColumnInfo(name = "topic")
    val topic: String? = null,
    
    @ColumnInfo(name = "file_path")
    val filePath: String,
    
    @ColumnInfo(name = "duration")
    val duration: Long = 0
)
