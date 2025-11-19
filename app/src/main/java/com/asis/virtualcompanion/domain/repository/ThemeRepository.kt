package com.asis.virtualcompanion.domain.repository

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.data.model.Theme
import com.asis.virtualcompanion.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeRepository(
    private val context: Context,
    private val themePreferences: ThemePreferences
) : BaseRepository {

    private val themes = listOf(
        Theme(
            id = "default",
            name = "Default",
            backgroundRes = R.drawable.bg_home_default,
            primaryColorRes = R.color.primary,
            buttonStyleRes = R.style.Widget_Material3_Button_IconButton
        ),
        Theme(
            id = "ocean",
            name = "Ocean",
            backgroundRes = R.drawable.bg_home_default,
            primaryColorRes = R.color.primary,
            buttonStyleRes = R.style.Widget_Material3_Button_IconButton
        ),
        Theme(
            id = "forest",
            name = "Forest",
            backgroundRes = R.drawable.bg_home_default,
            primaryColorRes = R.color.primary,
            buttonStyleRes = R.style.Widget_Material3_Button_IconButton
        ),
        Theme(
            id = "sunset",
            name = "Sunset",
            backgroundRes = R.drawable.bg_home_default,
            primaryColorRes = R.color.primary,
            buttonStyleRes = R.style.Widget_Material3_Button_IconButton
        )
    )

    fun getAllThemes(): List<Theme> = themes

    fun getCurrentTheme(): LiveData<Theme> = themePreferences.themeIdFlow
        .map { themeId ->
            themes.find { it.id == themeId } ?: themes.first()
        }
        .asLiveData()

    suspend fun setCurrentTheme(themeId: String) {
        themePreferences.setThemeId(themeId)
    }

    fun getArchiveUri(): LiveData<String?> = themePreferences.archiveUriFlow.asLiveData()

    suspend fun setArchiveUri(uri: String?) {
        themePreferences.setArchiveUri(uri)
    }

    fun getUseRealVoice(): LiveData<Boolean> = themePreferences.useRealVoiceFlow.asLiveData()

    suspend fun setUseRealVoice(useRealVoice: Boolean) {
        themePreferences.setUseRealVoice(useRealVoice)
    }

    fun getProcessAudioOffline(): LiveData<Boolean> = themePreferences.processAudioOfflineFlow.asLiveData()

    suspend fun setProcessAudioOffline(processOffline: Boolean) {
        themePreferences.setProcessAudioOffline(processOffline)
    }
}
