package com.asis.virtualcompanion.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.data.model.Theme
import com.asis.virtualcompanion.data.model.VoiceRetentionPolicy
import com.asis.virtualcompanion.data.preferences.VoiceInteractionPreferences
import com.asis.virtualcompanion.domain.privacy.PrivacyDataManager
import com.asis.virtualcompanion.domain.repository.ThemeRepository
import com.asis.virtualcompanion.work.ImportArchiveWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val themes: List<Theme> = emptyList(),
    val selectedThemeId: String = "default",
    val archiveUri: String? = null,
    val archiveParsingInProgress: Boolean = false,
    val archiveParsingProgress: Int = 0,
    val useRealVoice: Boolean = false,
    val voiceRetentionPolicy: VoiceRetentionPolicy = VoiceRetentionPolicy.DELETE_IMMEDIATELY,
    val processAudioOffline: Boolean = false,
    val isClearingData: Boolean = false,
    val error: String? = null
)

class SettingsViewModel(
    private val context: Context,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val appContext = context.applicationContext
    private val voicePreferences = VoiceInteractionPreferences(appContext)
    private val privacyDataManager = PrivacyDataManager(appContext)

    private val _uiState = MutableLiveData<SettingsUiState>()
    val uiState: LiveData<SettingsUiState> = _uiState

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val themes = themeRepository.getAllThemes()
            updateUiState { copy(themes = themes) }
        }

        themeRepository.getCurrentTheme().observeForever { theme ->
            updateUiState { copy(selectedThemeId = theme.id) }
        }

        themeRepository.getArchiveUri().observeForever { uri ->
            updateUiState { copy(archiveUri = uri) }
        }

        themeRepository.getProcessAudioOffline().observeForever { processOffline ->
            updateUiState { copy(processAudioOffline = processOffline) }
        }

        viewModelScope.launch {
            voicePreferences.useRealVoice.collect { useRealVoice ->
                updateUiState { copy(useRealVoice = useRealVoice) }
            }
        }

        viewModelScope.launch {
            voicePreferences.retentionPolicy.collect { policy ->
                updateUiState { copy(voiceRetentionPolicy = policy) }
            }
        }
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
        updateUiState { copy(archiveParsingInProgress = true) }

        val workRequest = OneTimeWorkRequestBuilder<ImportArchiveWorker>()
            .setInputData(workDataOf("archive_uri" to uri))
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(workRequest.id)
            .observeForever { workInfo ->
                if (workInfo != null) {
                    when {
                        workInfo.state.isFinished -> {
                            updateUiState {
                                copy(
                                    archiveParsingInProgress = false,
                                    archiveParsingProgress = 100
                                )
                            }
                        }
                        else -> {
                            val progress = workInfo.progress.getInt("progress", 0)
                            updateUiState { copy(archiveParsingProgress = progress) }
                        }
                    }
                }
            }
    }

    fun toggleUseRealVoice(enabled: Boolean) {
        viewModelScope.launch {
            voicePreferences.setUseRealVoice(enabled)
        }
    }

    fun updateVoiceRetentionPolicy(policy: VoiceRetentionPolicy) {
        viewModelScope.launch {
            voicePreferences.setRetentionPolicy(policy)
        }
    }

    fun toggleProcessAudioOffline(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.setProcessAudioOffline(enabled)
        }
    }

    fun clearAllData() {
        if (_uiState.value?.isClearingData == true) return

        viewModelScope.launch {
            updateUiState { copy(isClearingData = true, error = null) }
            val result = privacyDataManager.clearAllData()
            if (result.success) {
                _statusMessage.value = appContext.getString(R.string.settings_clear_data_success)
                updateUiState { copy(error = null) }
            } else {
                val message = result.error?.localizedMessage
                    ?: appContext.getString(R.string.settings_clear_data_error_generic)
                _statusMessage.value = message
                updateUiState { copy(error = message) }
            }
            updateUiState { copy(isClearingData = false) }
        }
    }

    fun openPrivacyPolicy() {
        _navigationEvent.value = NavigationEvent.PrivacyPolicy
    }

    fun requestArchiveSelection() {
        _navigationEvent.value = NavigationEvent.SelectArchive
    }

    fun revokeArchiveAccess() {
        val uriString = _uiState.value?.archiveUri ?: return
        viewModelScope.launch {
            releasePersistedUri(uriString)
            themeRepository.setArchiveUri(null)
            _statusMessage.value = appContext.getString(R.string.settings_external_access_revoked)
        }
    }

    fun onStatusMessageShown() {
        _statusMessage.value = null
    }

    private suspend fun releasePersistedUri(uriString: String) {
        val uri = Uri.parse(uriString)
        withContext(Dispatchers.IO) {
            try {
                appContext.contentResolver.releasePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Ignore if permission was already revoked
            }
        }
    }

    private fun updateUiState(transform: SettingsUiState.() -> SettingsUiState) {
        val currentState = _uiState.value ?: SettingsUiState()
        _uiState.value = currentState.transform()
    }

    sealed class NavigationEvent {
        object SelectArchive : NavigationEvent()
        object PrivacyPolicy : NavigationEvent()
    }
}
