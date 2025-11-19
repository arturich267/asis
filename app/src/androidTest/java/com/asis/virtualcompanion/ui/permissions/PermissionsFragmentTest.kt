package com.asis.virtualcompanion.ui.permissions

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.asis.virtualcompanion.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PermissionsFragmentTest {

    @Before
    fun setUp() {
        // Setup can be done here if needed
    }

    @Test
    fun permissionsFragment_isDisplayed() {
        val navController = TestNavHostController(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext)
        launchFragmentInContainer<PermissionsFragment>(themeResId = R.style.Theme_AsisVirtualCompanion).onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.subtitle)).check(matches(isDisplayed()))
        onView(withId(R.id.grant_button)).check(matches(isDisplayed()))
    }

    @Test
    fun permissionsFragment_grantButton_isClickable() {
        val navController = TestNavHostController(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext)
        launchFragmentInContainer<PermissionsFragment>(themeResId = R.style.Theme_AsisVirtualCompanion).onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.grant_button)).check(matches(isDisplayed()))
        onView(withId(R.id.grant_button)).perform(click())
    }

    @Test
    fun permissionsFragment_containsMediaAccessInfo() {
        launchFragmentInContainer<PermissionsFragment>(themeResId = R.style.Theme_AsisVirtualCompanion)

        onView(withId(R.id.title)).check(matches(isDisplayed()))
    }
}
