package com.asis.virtualcompanion.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.common.UiState
import com.asis.virtualcompanion.common.toError
import com.asis.virtualcompanion.common.toSuccess
import com.asis.virtualcompanion.di.AppModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val coroutineModule = AppModule.provideCoroutineModule()
    private val appContext = application

    private val _uiState = MutableLiveData<UiState<String>>(UiState.Idle)
    val uiState: LiveData<UiState<String>> = _uiState

    init {
        loadInitialData()
    }

    /**
     * Load initial data for the main screen
     */
    private fun loadInitialData() {
        viewModelScope.launch(coroutineModule.io) {
            _uiState.postValue(UiState.Loading)

            // Simulate data loading
            delay(1000)

            try {
                val welcomeMessage = appContext.getString(R.string.main_welcome_message)
                _uiState.postValue(welcomeMessage.toSuccess())
            } catch (e: Exception) {
                val errorMessage = appContext.getString(R.string.main_loading_error)
                _uiState.postValue(errorMessage.toError(e))
            }
        }
    }

    /**
     * Refresh data
     */
    fun refreshData() {
        loadInitialData()
    }
}
