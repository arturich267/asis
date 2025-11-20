package com.asis.virtualcompanion.ui.e2e

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.ui.main.HomeFragment
import com.asis.virtualcompanion.ui.permissions.PermissionsFragment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationE2ETest {

    @Test
    fun e2e_completeNavigationFlow_permissionsToHomeToSettingsToChat() {
        val navController = TestNavHostController(
            InstrumentationRegistry.getInstrumentation().targetContext
        )

        launchFragmentInContainer<PermissionsFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        ).onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.navigation_permissions)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.grant_button)).check(matches(isDisplayed()))
        assertEquals(navController.currentDestination?.id, R.id.navigation_permissions)
    }

    @Test
    fun e2e_homeScreen_navigationToAllDestinations() {
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

        onView(withId(R.id.welcomeTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.voiceButton)).check(matches(isDisplayed()))
        onView(withId(R.id.chatButton)).check(matches(isDisplayed()))
        onView(withId(R.id.settingsButton)).check(matches(isDisplayed()))

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
    }

    @Test
    fun e2e_homeScreen_allButtonsDisplayedAndClickable() {
        launchFragmentInContainer<HomeFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.voiceButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))

        onView(withId(R.id.chatButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))

        onView(withId(R.id.settingsButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
    }

    @Test
    fun e2e_homeScreen_contentDescriptionsForAccessibility() {
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
    fun e2e_homeScreen_surviveConfigurationChange() {
        val scenario = launchFragmentInContainer<HomeFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.welcomeTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.voiceButton)).check(matches(isDisplayed()))
        onView(withId(R.id.chatButton)).check(matches(isDisplayed()))

        scenario.recreate()

        onView(withId(R.id.welcomeTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.voiceButton)).check(matches(isDisplayed()))
        onView(withId(R.id.chatButton)).check(matches(isDisplayed()))
        onView(withId(R.id.settingsButton)).check(matches(isDisplayed()))
    }

    @Test
    fun e2e_homeScreen_verifyTextContent() {
        launchFragmentInContainer<HomeFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.welcomeTitle))
            .check(matches(withText(R.string.home_welcome_title)))

        onView(withId(R.id.welcomeSubtitle))
            .check(matches(withText(R.string.home_welcome_subtitle)))
    }
}
