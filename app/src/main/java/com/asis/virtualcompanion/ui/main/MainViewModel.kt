package com.asis.virtualcompanion.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asis.virtualcompanion.common.UiState
import com.asis.virtualcompanion.di.AppModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen
 */
class MainViewModel : ViewModel() {
    
    private val coroutineModule = AppModule.provideCoroutineModule()
    
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
                // Placeholder for actual data loading
                val welcomeMessage = "Welcome to Asis Virtual Companion!"
                _uiState.postValue(welcomeMessage.toSuccess())
            } catch (e: Exception) {
                _uiState.postValue("Failed to load data".toError(e))
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