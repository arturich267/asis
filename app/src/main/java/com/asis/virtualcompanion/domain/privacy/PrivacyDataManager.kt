package com.asis.virtualcompanion.domain.privacy

import android.content.Context
import com.asis.virtualcompanion.data.preferences.PermissionsPreferences
import com.asis.virtualcompanion.data.preferences.ThemePreferences
import com.asis.virtualcompanion.data.preferences.VoiceInteractionPreferences
import com.asis.virtualcompanion.di.AppModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PrivacyDataManager(context: Context) {

    private val appContext = context.applicationContext
    private val themePreferences = ThemePreferences(appContext)
    private val voicePreferences = VoiceInteractionPreferences(appContext)
    private val permissionsPreferences = PermissionsPreferences(appContext)

    suspend fun clearAllData(): PrivacyDataClearResult = withContext(Dispatchers.IO) {
        return@withContext try {
            AppModule.provideDatabase(appContext).clearAllTables()

            purgeDirectory(File(appContext.filesDir, "whatsapp_import"))
            purgeDirectory(File(appContext.cacheDir, "voice_temp"))
            purgeDirectory(File(appContext.cacheDir, "archives"))
            purgeDirectory(appContext.externalCacheDir)

            themePreferences.clearAll()
            voicePreferences.clearAll()
            permissionsPreferences.clear()

            releasePersistedUris()

            PrivacyDataClearResult(success = true)
        } catch (throwable: Throwable) {
            PrivacyDataClearResult(success = false, error = throwable)
        }
    }

    private fun purgeDirectory(directory: File?) {
        directory?.let {
            if (it.exists()) {
                it.deleteRecursively()
            }
        }
    }

    private fun releasePersistedUris() {
        val resolver = appContext.contentResolver
        resolver.persistedUriPermissions.forEach { permission ->
            try {
                resolver.releasePersistableUriPermission(permission.uri, permission.persistedModes)
            } catch (_: SecurityException) {
                // Ignore permission release failures
            }
        }
    }
}

data class PrivacyDataClearResult(
    val success: Boolean,
    val error: Throwable? = null
)
