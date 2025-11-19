package com.asis.virtualcompanion.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "permissions_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

private val PERMISSIONS_GRANTED_KEY = booleanPreferencesKey("permissions_granted")

class PermissionsPreferences(private val context: Context) {

    val permissionsGrantedFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PERMISSIONS_GRANTED_KEY] ?: false
        }

    suspend fun setPermissionsGranted(granted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PERMISSIONS_GRANTED_KEY] = granted
        }
    }
    
    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
