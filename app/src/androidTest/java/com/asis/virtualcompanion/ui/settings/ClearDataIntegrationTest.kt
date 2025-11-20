package com.asis.virtualcompanion.ui.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.ui.main.SettingsFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClearDataIntegrationTest {

    @Test
    fun testClearDataFlowCompletes() {
        // Launch the SettingsFragment
        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        scenario.onFragment { fragment ->
            // Verify that clear data button is displayed
            onView(withId(R.id.clear_data_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.settings_clear_all_data)))

            // Click on clear data button
            onView(withId(R.id.clear_data_button))
                .perform(click())

            // Verify that the confirmation dialog is displayed
            onView(withText(R.string.clear_data_confirmation_message))
                .check(matches(isDisplayed()))

            // Verify that the cancel button is displayed
            onView(withText(R.string.cancel))
                .check(matches(isDisplayed()))

            // Verify that the clear button is displayed
            onView(withText(R.string.clear))
                .check(matches(isDisplayed()))

            // Click on clear button to confirm
            onView(withText(R.string.clear))
                .perform(click())

            // Note: In a real integration test, we would verify that:
            // 1. The data is actually cleared from the database
            // 2. Files are deleted from cache
            // 3. Preferences are reset
            // 4. Success toast is shown
            // For this test, we mainly verify the UI flow works correctly
            
            // Verify that the button is still displayed after the action
            onView(withId(R.id.clear_data_button))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun testClearDataDialogCanBeCancelled() {
        // Launch the SettingsFragment
        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        scenario.onFragment { fragment ->
            // Click on clear data button
            onView(withId(R.id.clear_data_button))
                .perform(click())

            // Verify that the confirmation dialog is displayed
            onView(withText(R.string.clear_data_confirmation_message))
                .check(matches(isDisplayed()))

            // Click on cancel button
            onView(withText(R.string.cancel))
                .perform(click())

            // Verify that the dialog is dismissed (by checking that we can interact with the button again)
            onView(withId(R.id.clear_data_button))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
        }
    }

    @Test
    fun testPrivacyPolicyButtonNavigates() {
        // Launch the SettingsFragment
        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        scenario.onFragment { fragment ->
            // Verify that privacy policy button is displayed
            onView(withId(R.id.privacy_policy_button))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.settings_view_privacy_policy)))

            // Click on privacy policy button
            onView(withId(R.id.privacy_policy_button))
                .perform(click())

            // Note: In a real integration test, we would verify that navigation occurs
            // For this test, we mainly verify the button exists and is clickable
            // The actual navigation test would require setting up a NavController mock
        }
    }

    @Test
    fun testVoiceRetentionToggleIsDisplayed() {
        // Launch the SettingsFragment
        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        scenario.onFragment { fragment ->
            // Verify that voice retention toggle is displayed
            onView(withId(R.id.retain_voice_toggle))
                .check(matches(isDisplayed()))

            // Verify that the label is displayed
            onView(withText(R.string.settings_retain_voice_recordings))
                .check(matches(isDisplayed()))

            // Verify that the toggle is checked by default
            onView(withId(R.id.retain_voice_toggle))
                .check(matches(isChecked()))
        }
    }

    @Test
    fun testProcessOfflineToggleIsDisabled() {
        // Launch the SettingsFragment
        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        scenario.onFragment { fragment ->
            // Verify that process offline toggle is displayed but disabled
            onView(withId(R.id.process_audio_offline_toggle))
                .check(matches(isDisplayed()))
                .check(matches(isNotEnabled()))
                .check(matches(isChecked())) // Should be checked and disabled
        }
    }
}