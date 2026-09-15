package org.lepotager.executivefunction.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.domain.FocusTimerMode
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "fr")
class OnboardingAndTimerChoiceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun skippingIntroDoesNotEnableAdaptation() {
        var enabled = 0
        var finished = 0
        var skipped = 0
        compose.setContent {
            ExecutiveFunctionTheme {
                LocalAlgorithmIntro(
                    onEnableAdaptation = { enabled++ },
                    onFinish = { finished++ },
                    onSkip = { skipped++ },
                )
            }
        }

        compose.onNodeWithText("Passer").performClick()
        compose.runOnIdle {
            assertEquals(0, enabled)
            assertEquals(0, finished)
            assertEquals(1, skipped)
        }
    }

    @Test
    fun enablingSuggestionsIsAnExplicitThirdStepAction() {
        var enabled = 0
        var finished = 0
        compose.setContent {
            ExecutiveFunctionTheme {
                LocalAlgorithmIntro(
                    onEnableAdaptation = { enabled++ },
                    onFinish = { finished++ },
                    onSkip = {},
                )
            }
        }

        compose.onNodeWithText("Suivant").performClick()
        compose.onNodeWithText("Suivant").performClick()
        compose.onNodeWithText("Activer les suggestions").performClick()
        compose.runOnIdle {
            assertEquals(1, enabled)
            assertEquals(1, finished)
        }
    }

    @Test
    fun stopwatchCanIgnoreAnExistingLearnedReference() {
        var chosenMode: FocusTimerMode? = null
        var target: Long? = -1L
        compose.setContent {
            ExecutiveFunctionTheme {
                FocusStartDialog(
                    taskTitle = "Lire",
                    learnedTargetDurationMs = 14 * 60_000L,
                    initialMode = FocusTimerMode.COUNTDOWN,
                    onDismiss = {},
                    onConfirm = { mode, value -> chosenMode = mode; target = value },
                )
            }
        }

        compose.onNodeWithText("Chronomètre").performClick()
        compose.onNodeWithText("Commencer").assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals(FocusTimerMode.STOPWATCH, chosenMode)
            assertNull(target)
        }
    }

    @Test
    fun countdownWorksWithManualDurationWithoutLearningHistory() {
        var chosenMode: FocusTimerMode? = null
        var target: Long? = null
        compose.setContent {
            ExecutiveFunctionTheme {
                FocusStartDialog(
                    taskTitle = "Ranger",
                    learnedTargetDurationMs = null,
                    initialMode = FocusTimerMode.STOPWATCH,
                    onDismiss = {},
                    onConfirm = { mode, value -> chosenMode = mode; target = value },
                )
            }
        }

        compose.onNodeWithText("Minuteur").performClick()
        compose.onNodeWithTag("countdown-minutes").performTextInput("12")
        compose.onNodeWithText("Commencer").assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals(FocusTimerMode.COUNTDOWN, chosenMode)
            assertEquals(12 * 60_000L, target)
        }
    }
}
