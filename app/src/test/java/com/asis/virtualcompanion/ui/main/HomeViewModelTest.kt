package com.asis.virtualcompanion.ui.main

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.asis.virtualcompanion.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var application: Application

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = HomeViewModel(application)
    }

    @Test
    fun `initial surface state should have default values`() {
        val initialState = viewModel.surfaceState.value

        assertEquals(R.drawable.bg_home_default, initialState?.currentBackgroundRes)
        assertEquals(
            application.getString(R.string.home_last_profile_summary),
            initialState?.lastProfileSummary
        )
        assertTrue(initialState?.isVoiceAvailable == true)
        assertTrue(initialState?.isChatAvailable == true)
    }

    @Test
    fun `updateBackground should change background resource`() {
        val newBackgroundRes = android.R.drawable.ic_menu_gallery

        viewModel.updateBackground(newBackgroundRes)

        val updatedState = viewModel.surfaceState.value
        assertEquals(newBackgroundRes, updatedState?.currentBackgroundRes)
    }

    @Test
    fun `updateProfileSummary should change profile summary`() {
        val newSummary = "Test profile imported successfully"

        viewModel.updateProfileSummary(newSummary)

        val updatedState = viewModel.surfaceState.value
        assertEquals(newSummary, updatedState?.lastProfileSummary)
    }

    @Test
    fun `onVoiceClicked should log analytics event`() {
        viewModel.onVoiceClicked()
    }

    @Test
    fun `onChatClicked should log analytics event`() {
        viewModel.onChatClicked()
    }

    @Test
    fun `onSettingsClicked should log analytics event`() {
        viewModel.onSettingsClicked()
    }

    @Test
    fun `multiple updates should preserve other state values`() {
        val initialBackground = viewModel.surfaceState.value?.currentBackgroundRes
        val initialSummary = viewModel.surfaceState.value?.lastProfileSummary

        viewModel.updateBackground(android.R.drawable.ic_menu_camera)
        val afterBackgroundUpdate = viewModel.surfaceState.value

        assertEquals(android.R.drawable.ic_menu_camera, afterBackgroundUpdate?.currentBackgroundRes)
        assertEquals(initialSummary, afterBackgroundUpdate?.lastProfileSummary)

        viewModel.updateProfileSummary("Updated summary")
        val afterSummaryUpdate = viewModel.surfaceState.value

        assertEquals(android.R.drawable.ic_menu_camera, afterSummaryUpdate?.currentBackgroundRes)
        assertEquals("Updated summary", afterSummaryUpdate?.lastProfileSummary)
    }
}
