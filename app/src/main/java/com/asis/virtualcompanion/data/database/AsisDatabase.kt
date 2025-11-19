package com.asis.virtualcompanion.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.asis.virtualcompanion.common.Constants
import com.asis.virtualcompanion.data.database.dao.*
import com.asis.virtualcompanion.data.database.entity.*

/**
 * Main Room database for the application
 */
@Database(
    entities = [
        MessageEntity::class,
        PhraseStatEntity::class,
        VoiceMetaEntity::class,
        ChatMessageEntity::class,
        ThemePreferenceEntity::class
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AsisDatabase : RoomDatabase() {
    
    abstract fun messageDao(): MessageDao
    abstract fun phraseStatDao(): PhraseStatDao
    abstract fun voiceMetaDao(): VoiceMetaDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun themePreferenceDao(): ThemePreferenceDao
}