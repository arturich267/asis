package com.asis.virtualcompanion.ui.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.ui.main.SettingsFragment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class SettingsFragmentTest {

    @Test
    fun settingsFragment_displaysAllUIElements() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.archive_picker_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.settings_select_archive)))

        onView(withId(R.id.archive_status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.use_real_voice_toggle))
            .check(matches(isDisplayed()))

        onView(withId(R.id.process_audio_offline_toggle))
            .check(matches(isDisplayed()))

        onView(withId(R.id.retain_voice_toggle))
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_policy_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.settings_view_privacy_policy)))

        onView(withId(R.id.clear_data_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.settings_clear_all_data)))
    }

    @Test
    fun settingsFragment_archivePickerButton_isClickable() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.archive_picker_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
    }

    @Test
    fun settingsFragment_togglesAreInteractive() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.use_real_voice_toggle))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        onView(withId(R.id.retain_voice_toggle))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        onView(withId(R.id.process_audio_offline_toggle))
            .check(matches(isDisplayed()))
            .check(matches(isNotEnabled()))
    }

    @Test
    fun settingsFragment_privacyPolicyButton_navigatesToPrivacyPolicy() {
        val navController = TestNavHostController(
            InstrumentationRegistry.getInstrumentation().targetContext
        )

        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )
        scenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.navigation_settings)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.privacy_policy_button)).perform(click())

        assertEquals(R.id.privacy_policy_fragment, navController.currentDestination?.id)
    }

    @Test
    fun settingsFragment_clearDataButton_showsDialog() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.clear_data_button)).perform(click())

        onView(withText(R.string.clear_data_confirmation_message))
            .check(matches(isDisplayed()))

        onView(withText(R.string.cancel))
            .check(matches(isDisplayed()))

        onView(withText(R.string.clear))
            .check(matches(isDisplayed()))
    }

    @Test
    fun settingsFragment_clearDataDialog_cancelWorks() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.clear_data_button)).perform(click())

        onView(withText(R.string.cancel)).perform(click())

        onView(withId(R.id.clear_data_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun settingsFragment_retainVoiceToggle_isCheckedByDefault() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.retain_voice_toggle))
            .check(matches(isChecked()))
    }

    @Test
    fun settingsFragment_processOfflineToggle_isDisabledAndChecked() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.process_audio_offline_toggle))
            .check(matches(isDisplayed()))
            .check(matches(isNotEnabled()))
            .check(matches(isChecked()))
    }

    @Test
    fun settingsFragment_themeContainer_isDisplayed() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.theme_container))
            .check(matches(isDisplayed()))
    }

    @Test
    fun settingsFragment_surviveConfigurationChange() {
        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.archive_picker_button)).check(matches(isDisplayed()))
        onView(withId(R.id.clear_data_button)).check(matches(isDisplayed()))

        scenario.recreate()

        onView(withId(R.id.archive_picker_button)).check(matches(isDisplayed()))
        onView(withId(R.id.clear_data_button)).check(matches(isDisplayed()))
        onView(withId(R.id.privacy_policy_button)).check(matches(isDisplayed()))
    }
}
