package com.asis.virtualcompanion.di

import android.content.Context
import androidx.room.Room
import com.asis.virtualcompanion.data.database.AsisDatabase
import com.asis.virtualcompanion.common.Constants
import com.asis.virtualcompanion.data.repository.*
import com.asis.virtualcompanion.domain.repository.*

/**
 * Simple dependency injection container
 * In a real app, you might want to use Hilt or Dagger
 */
object AppModule {
    
    private var _database: AsisDatabase? = null
    private var _coroutineModule: CoroutineModule? = null
    
    fun provideDatabase(context: Context): AsisDatabase {
        return _database ?: synchronized(this) {
            _database ?: buildDatabase(context).also { _database = it }
        }
    }
    
    private fun buildDatabase(context: Context): AsisDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AsisDatabase::class.java,
            Constants.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    fun provideCoroutineModule(): CoroutineModule {
        return _coroutineModule ?: synchronized(this) {
            _coroutineModule ?: CoroutineModule().also { _coroutineModule = it }
        }
    }
    
    fun provideMessageRepository(context: Context): MessageRepository {
        val database = provideDatabase(context)
        return MessageRepositoryImpl(database.messageDao())
    }
    
    fun providePhraseStatRepository(context: Context): PhraseStatRepository {
        val database = provideDatabase(context)
        return PhraseStatRepositoryImpl(database.phraseStatDao())
    }
    
    fun provideVoiceRepository(context: Context): VoiceRepository {
        val database = provideDatabase(context)
        return VoiceRepositoryImpl(database.voiceMetaDao())
    }
    
    fun provideChatMessageRepository(context: Context): ChatMessageRepository {
        val database = provideDatabase(context)
        return ChatMessageRepositoryImpl(database.chatMessageDao())
    }
}