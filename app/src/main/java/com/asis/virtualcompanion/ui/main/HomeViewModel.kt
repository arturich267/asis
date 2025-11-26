package com.asis.virtualcompanion.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.asis.virtualcompanion.R

/**
 * Data class representing the home screen surface state
 */
data class HomeSurfaceState(
    val currentBackgroundRes: Int = R.drawable.bg_home_default,
    val lastProfileSummary: String = "",
    val isVoiceAvailable: Boolean = true,
    val isChatAvailable: Boolean = true
)

/**
 * ViewModel for the Home screen managing surface state and interactions
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val defaultProfileSummary = application.getString(R.string.home_last_profile_summary)

    private val _surfaceState = MutableLiveData(
        HomeSurfaceState(lastProfileSummary = defaultProfileSummary)
    )
    val surfaceState: LiveData<HomeSurfaceState> = _surfaceState

    /**
     * Update the background resource
     */
    fun updateBackground(backgroundRes: Int) {
        val currentState = _surfaceState.value ?: return
        _surfaceState.value = currentState.copy(currentBackgroundRes = backgroundRes)
    }

    /**
     * Update the last profile summary
     */
    fun updateProfileSummary(summary: String) {
        val currentState = _surfaceState.value ?: return
        _surfaceState.value = currentState.copy(lastProfileSummary = summary)
    }

    /**
     * Handle voice button click
     */
    fun onVoiceClicked() {
        // Log analytics for voice interaction
        logAnalyticsEvent("voice_button_clicked")
    }

    /**
     * Handle chat button click
     */
    fun onChatClicked() {
        // Log analytics for chat interaction
        logAnalyticsEvent("chat_button_clicked")
    }

    /**
     * Handle settings button click
     */
    fun onSettingsClicked() {
        // Log analytics for settings interaction
        logAnalyticsEvent("settings_button_clicked")
    }

    /**
     * Stub for analytics logging
     */
    private fun logAnalyticsEvent(eventName: String) {
        // TODO: Implement actual analytics logging
        // For now, this serves as a stub for the requirement
        println("Analytics event: $eventName")
    }
}
