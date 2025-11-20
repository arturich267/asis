package com.asis.virtualcompanion.ui.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.containsString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asis.virtualcompanion.R
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class PrivacyPolicyFragmentTest {

    @Test
    fun testPrivacyPolicyFragmentDisplaysContent() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<PrivacyPolicyFragment>()

        scenario.onFragment { fragment ->
            // Verify that the title is displayed
            onView(withText(R.string.privacy_policy_title))
                .check(matches(isDisplayed()))

            // Verify that the content text view is displayed
            onView(withId(R.id.privacy_policy_text))
                .check(matches(isDisplayed()))

            // Verify that the content is not empty
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("Политика конфиденциальности"))))

            // Verify that the back button is displayed
            onView(withId(R.id.back_button))
                .check(matches(isDisplayed()))
                .check(matches(withContentDescription(R.string.privacy_policy_back_button_content_description)))
        }
    }

    @Test
    fun testBackButtonClosesFragment() {
        // Create a mock NavController
        val navController = mock(NavController::class.java)

        // Launch the fragment with NavController
        val scenario = launchFragmentInContainer<PrivacyPolicyFragment>()

        scenario.onFragment { fragment ->
            // Set the NavController on the fragment
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }

        // Click the back button
        onView(withId(R.id.back_button))
            .perform(click())

        // Verify that navigateUp was called
        verify(navController).navigateUp()
    }

    @Test
    fun testPrivacyPolicyContentIsScrollable() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<PrivacyPolicyFragment>()

        scenario.onFragment { fragment ->
            // Verify that the ScrollView is displayed
            onView(isAssignableFrom(android.widget.ScrollView::class.java))
                .check(matches(isDisplayed()))

            // Verify that the content is long enough to scroll
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("Эта политика конфиденциальности действительна"))))
        }
    }

    @Test
    fun testPrivacyPolicyContentHasRequiredSections() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<PrivacyPolicyFragment>()

        scenario.onFragment { fragment ->
            // Verify that all required sections are present
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("1. Сбор информации"))))
            
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("2. Использование информации"))))
            
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("3. Хранение данных"))))
            
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("4. Безопасность данных"))))
            
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("5. Ваши права"))))
            
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("6. Дети"))))
            
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("7. Изменения в политике"))))
            
            onView(withId(R.id.privacy_policy_text))
                .check(matches(withText(containsString("8. Связь с нами"))))
        }
    }
}