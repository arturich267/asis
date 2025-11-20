package com.asis.virtualcompanion.ui.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.asis.virtualcompanion.data.model.Theme
import com.asis.virtualcompanion.data.preferences.ThemePreferences
import com.asis.virtualcompanion.data.repository.DataClearRepository
import com.asis.virtualcompanion.domain.repository.ThemeRepository
import com.asis.virtualcompanion.work.ImportArchiveWorker
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themes: List<Theme> = emptyList(),
    val selectedThemeId: String = "default",
    val archiveUri: String? = null,
    val archiveParsingInProgress: Boolean = false,
    val archiveParsingProgress: Int = 0,
    val useRealVoice: Boolean = false,
    val processAudioOffline: Boolean = false,
    val retainVoiceRecordings: Boolean = true,
    val error: String? = null
)

class SettingsViewModel(
    private val context: Context,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val dataClearRepository = DataClearRepository(context)

    private val _uiState = MutableLiveData<SettingsUiState>()
    val uiState: LiveData<SettingsUiState> = _uiState

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val themes = themeRepository.getAllThemes()
            val currentState = _uiState.value ?: SettingsUiState()
            _uiState.value = currentState.copy(themes = themes)
        }

        themeRepository.getCurrentTheme().observeForever { theme ->
            val currentState = _uiState.value ?: SettingsUiState()
            _uiState.value = currentState.copy(selectedThemeId = theme.id)
        }

        themeRepository.getArchiveUri().observeForever { uri ->
            val currentState = _uiState.value ?: SettingsUiState()
            _uiState.value = currentState.copy(archiveUri = uri)
        }

        themeRepository.getUseRealVoice().observeForever { useRealVoice ->
            val currentState = _uiState.value ?: SettingsUiState()
            _uiState.value = currentState.copy(useRealVoice = useRealVoice)
        }

        themeRepository.getProcessAudioOffline().observeForever { processOffline ->
            val currentState = _uiState.value ?: SettingsUiState()
            _uiState.value = currentState.copy(processAudioOffline = processOffline)
        }

        // Initialize voice retention preference
        val currentState = _uiState.value ?: SettingsUiState()
        _uiState.value = currentState.copy(retainVoiceRecordings = true)
    }

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            themeRepository.setCurrentTheme(themeId)
        }
    }

    fun selectArchive(uri: Uri) {
        val uriString = uri.toString()
        viewModelScope.launch {
            themeRepository.setArchiveUri(uriString)
            kickOffArchiveParsing(uriString)
        }
    }

    private fun kickOffArchiveParsing(uri: String) {
        val currentState = _uiState.value ?: SettingsUiState()
        _uiState.value = currentState.copy(archiveParsingInProgress = true)

        val workRequest = OneTimeWorkRequestBuilder<ImportArchiveWorker>()
            .setInputData(workDataOf("archive_uri" to uri))
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(workRequest.id)
            .observeForever { workInfo ->
                if (workInfo != null) {
                    when {
                        workInfo.state.isFinished -> {
                            val newState = currentState.copy(
                                archiveParsingInProgress = false,
                                archiveParsingProgress = 100
                            )
                            _uiState.value = newState
                        }
                        else -> {
                            val progress = workInfo.progress.getInt("progress", 0)
                            _uiState.value = currentState.copy(archiveParsingProgress = progress)
                        }
                    }
                }
            }
    }

    fun toggleUseRealVoice(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.setUseRealVoice(enabled)
        }
    }

    fun toggleProcessAudioOffline(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.setProcessAudioOffline(enabled)
        }
    }

    fun requestArchiveSelection() {
        _navigationEvent.value = NavigationEvent.SelectArchive
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                // Clear all data from database
                dataClearRepository.deleteAll()
                
                // Clear files and cache
                dataClearRepository.deleteRecursively()
                
                // Clear preferences
                dataClearRepository.clearPreferences()
                
                // Reset UI state
                val currentState = _uiState.value ?: SettingsUiState()
                _uiState.value = currentState.copy(
                    archiveUri = null,
                    archiveParsingInProgress = false,
                    archiveParsingProgress = 0,
                    error = null
                )
            } catch (e: Exception) {
                val currentState = _uiState.value ?: SettingsUiState()
                _uiState.value = currentState.copy(error = "Failed to clear data: ${e.message}")
            }
        }
    }

    fun setVoiceRetentionToggle(enabled: Boolean) {
        val currentState = _uiState.value ?: SettingsUiState()
        _uiState.value = currentState.copy(retainVoiceRecordings = enabled)
    }

    sealed class NavigationEvent {
        object SelectArchive : NavigationEvent()
    }
}
