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
        // Add entities here as they are created
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AsisDatabase : RoomDatabase() {
    
    // Add DAOs here as they are created
    // abstract fun userDao(): UserDao
    // abstract fun conversationDao(): ConversationDao
}