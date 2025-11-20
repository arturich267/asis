package com.asis.virtualcompanion.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.di.AppModule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

val Context.testDataStore: DataStore<Preferences> by preferencesDataStore(name = "test_data_clear_preferences")

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DataClearRepositoryTest {

    private lateinit var context: Context
    private lateinit var dataClearRepository: DataClearRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dataClearRepository = DataClearRepository(context)
    }

    @Test
    fun testClearAllDataSucceeds() = runBlocking {
        // Setup: Add some test data to the database
        val database = AppModule.provideDatabase(context)
        
        // Insert test data
        val messageDao = database.messageDao()
        val testMessage = com.asis.virtualcompanion.data.database.entity.MessageEntity(
            id = 1,
            sender = "test_sender",
            content = "test_content",
            timestamp = System.currentTimeMillis(),
            sentiment = "neutral"
        )
        messageDao.insert(testMessage)
        
        // Verify data exists
        val initialCount = messageDao.getMessageCount()
        assertTrue(initialCount > 0, "Test data should exist before clearing")
        
        // Execute
        val result = dataClearRepository.deleteAll()
        
        // Verify
        assertTrue(result is Result.Success, "Clear all data should succeed")
        val finalCount = messageDao.getMessageCount()
        assertEquals(0, finalCount, "All data should be cleared")
    }

    @Test
    fun testClearAllDataRemovesFiles() = runBlocking {
        // Setup: Create test files in cache
        val testFile1 = File(context.cacheDir, "test_file1.txt")
        val testFile2 = File(context.cacheDir, "test_file2.txt")
        val testDir = File(context.cacheDir, "test_dir")
        testDir.mkdirs()
        val testFile3 = File(testDir, "test_file3.txt")
        
        testFile1.writeText("test content 1")
        testFile2.writeText("test content 2")
        testFile3.writeText("test content 3")
        
        // Verify files exist
        assertTrue(testFile1.exists(), "Test file 1 should exist")
        assertTrue(testFile2.exists(), "Test file 2 should exist")
        assertTrue(testFile3.exists(), "Test file 3 should exist")
        
        // Execute
        val result = dataClearRepository.deleteRecursively()
        
        // Verify
        assertTrue(result is Result.Success, "Delete recursively should succeed")
        assertFalse(testFile1.exists(), "Test file 1 should be deleted")
        assertFalse(testFile2.exists(), "Test file 2 should be deleted")
        assertFalse(testDir.exists(), "Test directory should be deleted")
    }

    @Test
    fun testDataClearStatusReturnsTrue() = runBlocking {
        // Setup: Clear all data first
        dataClearRepository.deleteAll()
        dataClearRepository.deleteRecursively()
        dataClearRepository.clearPreferences()
        
        // Execute
        val isDataCleared = dataClearRepository.getDataClearStatus()
        
        // Verify
        assertTrue(isDataCleared, "Data clear status should return true when all data is cleared")
    }

    @Test
    fun testDataClearStatusReturnsFalseWhenDataExists() = runBlocking {
        // Setup: Add some test data
        val database = AppModule.provideDatabase(context)
        val messageDao = database.messageDao()
        val testMessage = com.asis.virtualcompanion.data.database.entity.MessageEntity(
            id = 1,
            sender = "test_sender",
            content = "test_content",
            timestamp = System.currentTimeMillis(),
            sentiment = "neutral"
        )
        messageDao.insert(testMessage)
        
        // Execute
        val isDataCleared = dataClearRepository.getDataClearStatus()
        
        // Verify
        assertFalse(isDataCleared, "Data clear status should return false when data exists")
    }

    @Test
    fun testClearPreferencesSucceeds() = runBlocking {
        // Setup: Store some test preferences
        val dataStore = context.testDataStore
        val testKey = androidx.datastore.preferences.core.stringPreferencesKey("test_key")
        
        // Store test data
        dataStore.edit { preferences ->
            preferences[testKey] = "test_value"
        }
        
        // Verify data exists
        val testData = dataStore.data.first()[testKey]
        assertEquals("test_value", testData, "Test preference should exist")
        
        // Execute
        val result = dataClearRepository.clearPreferences()
        
        // Verify
        assertTrue(result is Result.Success, "Clear preferences should succeed")
        val clearedData = dataStore.data.first()[testKey]
        assertEquals(null, clearedData, "Preference should be cleared")
    }
}