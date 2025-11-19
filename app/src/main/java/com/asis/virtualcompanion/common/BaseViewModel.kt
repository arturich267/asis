package com.asis.virtualcompanion.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel with common functionality
 */
abstract class BaseViewModel(
    private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    /**
     * Launch a coroutine in viewModelScope with error handling
     */
    protected fun launchCoroutine(
        dispatcher: CoroutineDispatcher = defaultDispatcher,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                block()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Handle errors - can be overridden by subclasses
     */
    protected open fun handleError(error: Throwable) {
        // Log error or implement specific error handling
        error.printStackTrace()
    }
}

/**
 * Base ViewModel with UI state management
 */
abstract class BaseStateViewModel<T>(
    initialState: T,
    private val defaultDispatcher: CoroutineDispatcher
) : BaseViewModel(defaultDispatcher) {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<T> = _uiState.asStateFlow()

    protected fun updateState(newState: T) {
        _uiState.value = newState
    }

    protected fun updateState(update: (T) -> T) {
        _uiState.value = update(_uiState.value)
    }
}