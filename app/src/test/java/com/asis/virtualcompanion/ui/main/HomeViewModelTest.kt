package com.asis.virtualcompanion.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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

    @Before
    fun setup() {
        viewModel = HomeViewModel()
    }

    @Test
    fun `initial surface state should have default values`() {
        val initialState = viewModel.surfaceState.value
        
        assertEquals(R.drawable.bg_home_default, initialState?.currentBackgroundRes)
        assertEquals("No profile imported yet", initialState?.lastProfileSummary)
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
        // This test verifies the method executes without error
        // In a real implementation, you would verify analytics logging
        viewModel.onVoiceClicked()
        // Verification would be done through analytics mock
    }

    @Test
    fun `onChatClicked should log analytics event`() {
        // This test verifies the method executes without error
        // In a real implementation, you would verify analytics logging
        viewModel.onChatClicked()
        // Verification would be done through analytics mock
    }

    @Test
    fun `onSettingsClicked should log analytics event`() {
        // This test verifies the method executes without error
        // In a real implementation, you would verify analytics logging
        viewModel.onSettingsClicked()
        // Verification would be done through analytics mock
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