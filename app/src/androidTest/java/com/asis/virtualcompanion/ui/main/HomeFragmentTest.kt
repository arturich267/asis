package com.asis.virtualcompanion.ui.main

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.asis.virtualcompanion.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeFragmentTest {

    private lateinit var navController: NavController

    @Before
    fun setup() {
        navController = mock(NavController::class.java)
    }

    @Test
    fun homeFragment_displaysCorrectUIElements() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<HomeFragment>()
        scenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Verify main UI elements are displayed
        onView(withId(R.id.settingsButton))
            .check(matches(isDisplayed()))
            .check(matches(withContentDescription(R.string.home_settings_button_content_description)))

        onView(withId(R.id.welcomeTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.home_welcome_title)))

        onView(withId(R.id.welcomeSubtitle))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.home_welcome_subtitle)))

        onView(withId(R.id.voiceButton))
            .check(matches(isDisplayed()))
            .check(matches(withContentDescription(R.string.home_voice_button_content_description)))

        onView(withId(R.id.chatButton))
            .check(matches(isDisplayed()))
            .check(matches(withContentDescription(R.string.home_chat_button_content_description)))
    }

    @Test
    fun settingsButton_navigatesToSettings() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<HomeFragment>()
        scenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Click settings button
        onView(withId(R.id.settingsButton)).perform(click())

        // Verify navigation to settings
        verify(navController).navigate(R.id.action_home_to_settings)
    }

    @Test
    fun voiceButton_navigatesToVoice() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<HomeFragment>()
        scenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Click voice button
        onView(withId(R.id.voiceButton)).perform(click())

        // Verify navigation to voice
        verify(navController).navigate(R.id.action_home_to_voice)
    }

    @Test
    fun chatButton_navigatesToChat() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<HomeFragment>()
        scenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Click chat button
        onView(withId(R.id.chatButton)).perform(click())

        // Verify navigation to chat
        verify(navController).navigate(R.id.action_home_to_chat)
    }

    @Test
    fun buttons_areEnabledByDefault() {
        // Launch the fragment
        launchFragmentInContainer<HomeFragment>()

        // Verify buttons are enabled by default
        onView(withId(R.id.voiceButton)).check(matches(isEnabled()))
        onView(withId(R.id.chatButton)).check(matches(isEnabled()))
        onView(withId(R.id.settingsButton)).check(matches(isEnabled()))
    }

    @Test
    fun profileSummary_hasCorrectInitialText() {
        // Launch the fragment
        launchFragmentInContainer<HomeFragment>()

        // Initially, profile summary should be hidden since there's no profile
        onView(withId(R.id.profileSummary)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun welcomeText_displaysCorrectContent() {
        // Launch the fragment
        launchFragmentInContainer<HomeFragment>()

        // Verify welcome text content
        onView(withId(R.id.welcomeTitle))
            .check(matches(withText("Welcome to Asis")))

        onView(withId(R.id.welcomeSubtitle))
            .check(matches(withText("Your Virtual Companion")))
    }

    @Test
    fun background_isApplied() {
        // Launch the fragment
        launchFragmentInContainer<HomeFragment>()

        // Verify the root view is displayed with background
        onView(withParent(withId(R.id.nav_host_fragment_activity_main)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun fragment_handlesConfigurationChanges() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<HomeFragment>()
        scenario.onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Verify initial state
        onView(withId(R.id.welcomeTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.voiceButton)).check(matches(isDisplayed()))
        onView(withId(R.id.chatButton)).check(matches(isDisplayed()))

        // Recreate fragment (simulates configuration change)
        scenario.recreate()

        // Verify UI elements are still displayed after recreation
        onView(withId(R.id.welcomeTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.voiceButton)).check(matches(isDisplayed()))
        onView(withId(R.id.chatButton)).check(matches(isDisplayed()))
        onView(withId(R.id.settingsButton)).check(matches(isDisplayed()))

        // Verify navigation still works after recreation
        onView(withId(R.id.voiceButton)).perform(click())
        verify(navController).navigate(R.id.action_home_to_voice)
    }
}