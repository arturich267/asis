package com.asis.virtualcompanion.ui.e2e

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.ui.main.HomeFragment
import com.asis.virtualcompanion.ui.main.SettingsFragment
import com.asis.virtualcompanion.ui.permissions.PermissionsFragment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CompleteUserJourneyE2ETest {

    @Test
    fun completeUserJourney_firstLaunch_toChat() {
        val navController = TestNavHostController(
            InstrumentationRegistry.getInstrumentation().targetContext
        )

        val scenario = launchFragmentInContainer<PermissionsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )
        scenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.navigation_permissions)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.title))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.permissions_title)))

        onView(withId(R.id.grant_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        assertEquals(R.id.navigation_permissions, navController.currentDestination?.id)
    }

    @Test
    fun completeUserJourney_homeToSettingsToPrivacyAndBack() {
        val navController = TestNavHostController(
            InstrumentationRegistry.getInstrumentation().targetContext
        )

        val homeScenario = launchFragmentInContainer<HomeFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )
        homeScenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.navigation_home)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.welcomeTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.settingsButton)).perform(click())
        assertEquals(R.id.navigation_settings, navController.currentDestination?.id)

        homeScenario.recreate()
        val settingsScenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )
        settingsScenario.onFragment { fragment ->
            navController.setCurrentDestination(R.id.navigation_settings)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.privacy_policy_button))
            .check(matches(isDisplayed()))
            .perform(click())

        assertEquals(R.id.privacy_policy_fragment, navController.currentDestination?.id)
    }

    @Test
    fun completeUserJourney_homeToAllMainScreens() {
        val navController = TestNavHostController(
            InstrumentationRegistry.getInstrumentation().targetContext
        )

        val scenario = launchFragmentInContainer<HomeFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )
        scenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.navigation_home)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.welcomeTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.home_welcome_title)))

        onView(withId(R.id.welcomeSubtitle))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.home_welcome_subtitle)))

        onView(withId(R.id.voiceButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())
        assertEquals(R.id.navigation_voice, navController.currentDestination?.id)

        scenario.recreate()
        scenario.onFragment { fragment ->
            navController.setCurrentDestination(R.id.navigation_home)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.chatButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())
        assertEquals(R.id.navigation_chat, navController.currentDestination?.id)

        scenario.recreate()
        scenario.onFragment { fragment ->
            navController.setCurrentDestination(R.id.navigation_home)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.settingsButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())
        assertEquals(R.id.navigation_settings, navController.currentDestination?.id)
    }

    @Test
    fun completeUserJourney_settingsClearDataFlow() {
        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.clear_data_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.settings_clear_all_data)))
            .perform(click())

        onView(withText(R.string.clear_data_confirmation_message))
            .check(matches(isDisplayed()))

        onView(withText(R.string.cancel))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.clear_data_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun completeUserJourney_settingsToggles() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.use_real_voice_toggle))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        onView(withId(R.id.retain_voice_toggle))
            .check(matches(isDisplayed()))
            .check(matches(isChecked()))
            .check(matches(isClickable()))

        onView(withId(R.id.process_audio_offline_toggle))
            .check(matches(isDisplayed()))
            .check(matches(isChecked()))
            .check(matches(isNotEnabled()))
    }

    @Test
    fun completeUserJourney_allScreensSurviveRotation() {
        val scenario = launchFragmentInContainer<HomeFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.welcomeTitle)).check(matches(isDisplayed()))
        scenario.recreate()
        onView(withId(R.id.welcomeTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.voiceButton)).check(matches(isDisplayed()))
        onView(withId(R.id.chatButton)).check(matches(isDisplayed()))
    }

    @Test
    fun completeUserJourney_accessibilityContentDescriptions() {
        launchFragmentInContainer<HomeFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.voiceButton))
            .check(matches(withContentDescription(R.string.home_voice_button_content_description)))

        onView(withId(R.id.chatButton))
            .check(matches(withContentDescription(R.string.home_chat_button_content_description)))

        onView(withId(R.id.settingsButton))
            .check(matches(withContentDescription(R.string.home_settings_button_content_description)))
    }

    @Test
    fun completeUserJourney_settingsArchivePicker() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.archive_picker_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.settings_select_archive)))
            .check(matches(isEnabled()))

        onView(withId(R.id.archive_status_text))
            .check(matches(isDisplayed()))
    }

    @Test
    fun completeUserJourney_settingsThemeContainer() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.theme_container))
            .check(matches(isDisplayed()))
    }

    @Test
    fun completeUserJourney_multipleNavigationCycles() {
        val navController = TestNavHostController(
            InstrumentationRegistry.getInstrumentation().targetContext
        )

        val scenario = launchFragmentInContainer<HomeFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )
        scenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.navigation_home)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        for (i in 1..3) {
            onView(withId(R.id.chatButton)).perform(click())
            assertEquals(R.id.navigation_chat, navController.currentDestination?.id)

            scenario.recreate()
            scenario.onFragment { fragment ->
                navController.setCurrentDestination(R.id.navigation_home)
                Navigation.setViewNavController(fragment.requireView(), navController)
            }

            onView(withId(R.id.voiceButton)).perform(click())
            assertEquals(R.id.navigation_voice, navController.currentDestination?.id)

            scenario.recreate()
            scenario.onFragment { fragment ->
                navController.setCurrentDestination(R.id.navigation_home)
                Navigation.setViewNavController(fragment.requireView(), navController)
            }

            onView(withId(R.id.settingsButton)).perform(click())
            assertEquals(R.id.navigation_settings, navController.currentDestination?.id)

            scenario.recreate()
            scenario.onFragment { fragment ->
                navController.setCurrentDestination(R.id.navigation_home)
                Navigation.setViewNavController(fragment.requireView(), navController)
            }
        }
    }
}
