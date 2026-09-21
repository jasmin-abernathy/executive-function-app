package org.lepotager.executivefunction.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "fr")
class SupportOptionsInputTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun customDurationsSurvivePreferenceUpdatesAndRestoration() {
        var interval by mutableStateOf(25)
        var duration by mutableStateOf(10)
        var suggestions by mutableStateOf(true)
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            ExecutiveFunctionTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    SupportOptionsCard(
                        drawEnabled = false,
                        pauseSuggestionsEnabled = suggestions,
                        pauseAfterMinutes = interval,
                        pauseDurationMinutes = duration,
                        onSetDrawEnabled = {},
                        onSetPauseSuggestionsEnabled = { suggestions = it },
                        onSetPauseAfterMinutes = { interval = it },
                        onSetPauseDurationMinutes = { duration = it },
                    )
                }
            }
        }
        val breakField = compose.onNodeWithTag("home-custom-break-minutes")
        breakField.performScrollTo().performTextInput("5")
        compose.waitForIdle()
        breakField.assertTextContains("5")
        breakField.performTextInput("0")
        compose.waitForIdle()
        breakField.assertTextContains("50")
        compose.runOnIdle { assertEquals(50, duration) }

        val intervalField = compose.onNodeWithTag("home-custom-pause-minutes")
        intervalField.performScrollTo().performTextReplacement("60")
        compose.waitForIdle()
        intervalField.assertTextContains("60")
        compose.runOnIdle { assertEquals(60, interval) }
        intervalField.performTextReplacement("121")
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(60, interval) }

        restoration.emulateSavedInstanceStateRestore()
        intervalField.performScrollTo().assertTextContains("121")
        breakField.performScrollTo().assertTextContains("50")

        compose.runOnIdle { suggestions = false }
        intervalField.assertDoesNotExist()
        breakField.performScrollTo().performTextReplacement("12")
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(12, duration) }
    }
}
