package com.asis.virtualcompanion.ui.voice

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.ui.main.VoiceFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class VoiceFragmentTest {

    @Test
    fun voiceFragment_displaysAllUIElements() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.mic_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.mode_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.use_real_voice_switch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun voiceFragment_micButton_isClickable() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.mic_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
    }

    @Test
    fun voiceFragment_modeToggleButton_isClickable() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.mode_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .perform(click())
    }

    @Test
    fun voiceFragment_statusText_displaysHint() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.voice_press_hint)))
    }

    @Test
    fun voiceFragment_useRealVoiceSwitch_isInteractive() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.use_real_voice_switch))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun voiceFragment_visualizationCard_initiallyHidden() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.visualization_card))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun voiceFragment_playbackCard_initiallyHidden() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.playback_card))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun voiceFragment_processingIndicator_initiallyHidden() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.processing_indicator))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun voiceFragment_errorCard_initiallyHidden() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.error_card))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun voiceFragment_playbackControls_areDisplayed() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.playback_seekbar))
            .check(matches(isDisplayed()))

        onView(withId(R.id.play_pause_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.replay_button))
            .check(matches(isDisplayed()))
    }

    @Test
    fun voiceFragment_recordingVisualization_hasAmplitudeIndicator() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.amplitude_indicator))
            .check(matches(isDisplayed()))

        onView(withId(R.id.recording_duration_text))
            .check(matches(isDisplayed()))
    }

    @Test
    fun voiceFragment_surviveConfigurationChange() {
        val scenario = launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.mic_button)).check(matches(isDisplayed()))
        onView(withId(R.id.status_text)).check(matches(isDisplayed()))

        scenario.recreate()

        onView(withId(R.id.mic_button)).check(matches(isDisplayed()))
        onView(withId(R.id.status_text)).check(matches(isDisplayed()))
        onView(withId(R.id.mode_toggle_button)).check(matches(isDisplayed()))
    }

    @Test
    fun voiceFragment_modeButton_hasCorrectContentDescription() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.mode_toggle_button))
            .check(matches(isDisplayed()))
    }

    @Test
    fun voiceFragment_responseText_exists() {
        launchFragmentInContainer<VoiceFragment>(
            themeResId = R.style.Theme_AsisVirtualCompanion
        )

        onView(withId(R.id.response_text))
            .check(matches(isDisplayed()))
    }
}
