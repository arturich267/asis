package com.asis.virtualcompanion.ui.chat

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.ui.main.ChatFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatFragmentTest {

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setGraph(R.navigation.mobile_navigation)
    }

    @Test
    fun chatFragment_displaysCorrectly() {
        val scenario = launchFragmentInContainer<ChatFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )
        
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.messagesRecyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.messageInput)).check(matches(isDisplayed()))
        onView(withId(R.id.sendButton)).check(matches(isDisplayed()))
    }

    @Test
    fun chatFragment_showsEmptyStateWhenNoMessages() {
        launchFragmentInContainer<ChatFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        Thread.sleep(500)
        
        onView(withId(R.id.emptyState)).check(matches(isDisplayed()))
    }

    @Test
    fun chatFragment_sendButtonIsClickable() {
        launchFragmentInContainer<ChatFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.messageInput))
            .perform(typeText("Test message"), closeSoftKeyboard())
        
        onView(withId(R.id.sendButton))
            .check(matches(isEnabled()))
            .perform(click())
    }

    @Test
    fun chatFragment_navigationBackWorks() {
        val scenario = launchFragmentInContainer<ChatFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )
        
        scenario.onFragment { fragment ->
            navController.setCurrentDestination(R.id.navigation_chat)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withContentDescription("Navigate up")).perform(click())
    }

    @Test
    fun chatFragment_inputFieldAcceptsText() {
        launchFragmentInContainer<ChatFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        val testMessage = "Hello, this is a test message"
        
        onView(withId(R.id.messageInput))
            .perform(typeText(testMessage), closeSoftKeyboard())
        
        onView(withId(R.id.messageInput))
            .check(matches(withText(testMessage)))
    }

    @Test
    fun chatFragment_retryButtonWorksOnError() {
        launchFragmentInContainer<ChatFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        Thread.sleep(500)
        
        onView(withId(R.id.retryButton)).perform(click())
    }

    @Test
    fun chatFragment_survivesOrientationChange() {
        val scenario = launchFragmentInContainer<ChatFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        val testMessage = "Orientation test"
        
        onView(withId(R.id.messageInput))
            .perform(typeText(testMessage), closeSoftKeyboard())
        
        scenario.recreate()
        
        Thread.sleep(300)
        
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.messagesRecyclerView)).check(matches(isDisplayed()))
    }
}
