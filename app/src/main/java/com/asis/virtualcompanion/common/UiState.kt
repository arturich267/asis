package com.asis.virtualcompanion.common

/**
 * Sealed class representing different UI states
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
}

/**
 * Extension functions to easily convert to UiState
 */
fun <T> T.toSuccess(): UiState<T> = UiState.Success(this)
fun String.toError(exception: Throwable? = null): UiState<Nothing> = UiState.Error(this, exception)