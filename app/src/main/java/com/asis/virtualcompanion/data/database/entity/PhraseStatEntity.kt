package com.asis.virtualcompanion.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "phrase_stats",
    indices = [
        Index(value = ["count"], name = "idx_phrase_count")
    ]
)
data class PhraseStatEntity(
    @PrimaryKey
    @ColumnInfo(name = "phrase")
    val phrase: String,
    
    @ColumnInfo(name = "count")
    val count: Int = 0,
    
    @ColumnInfo(name = "emoji_hints")
    val emojiHints: List<String> = emptyList()
)
