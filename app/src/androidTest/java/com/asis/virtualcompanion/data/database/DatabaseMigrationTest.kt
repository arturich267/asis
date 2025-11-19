package com.asis.virtualcompanion.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.common.Constants
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    
    private var database: AsisDatabase? = null
    
    @After
    fun teardown() {
        database?.close()
    }
    
    @Test
    fun databaseCreationTest() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java
        ).allowMainThreadQueries().build()
        
        assertNotNull(database)
        assertTrue(database!!.isOpen)
    }
    
    @Test
    fun allDaosAreAccessible() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java
        ).allowMainThreadQueries().build()
        
        assertNotNull(database!!.messageDao())
        assertNotNull(database!!.phraseStatDao())
        assertNotNull(database!!.voiceMetaDao())
        assertNotNull(database!!.chatMessageDao())
        assertNotNull(database!!.themePreferenceDao())
    }
    
    @Test
    fun databaseVersionIsCorrect() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java
        ).allowMainThreadQueries().build()
        
        val openHelper = database!!.openHelper
        val databaseVersion = openHelper.writableDatabase.version
        assertEquals(Constants.DATABASE_VERSION, databaseVersion)
    }
    
    @Test
    fun databaseWithFallbackToDestructiveMigration() {
        database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AsisDatabase::class.java,
            "test_database"
        )
        .fallbackToDestructiveMigration()
        .allowMainThreadQueries()
        .build()
        
        assertNotNull(database)
        assertTrue(database!!.isOpen)
        
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .deleteDatabase("test_database")
    }
}
