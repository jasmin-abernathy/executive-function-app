package org.lepotager.executivefunction.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
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

    private fun baseline() = FirstRunSetupConfig(
        timerMode = FocusTimerMode.STOPWATCH,
        drawEnabled = true,
        pauseSuggestionsEnabled = true,
        pauseAfterMinutes = 25,
        checkInEnabled = true,
        adaptationEnabled = false,
        calmMode = false,
        autoMiniWindow = false,
    )

    private fun clickText(text: String) {
        compose.onNodeWithText(text).performScrollTo().performClick()
        compose.waitForIdle()
    }

    @Test
    fun skippingSetupDoesNotApplyAnySetting() {
        var applied = 0
        var skipped = 0
        compose.setContent {
            ExecutiveFunctionTheme {
                FirstRunSetupFlow(baseline(), { applied++ }, { skipped++ })
            }
        }

        clickText("Passer")
        compose.runOnIdle {
            assertEquals(0, applied)
            assertEquals(1, skipped)
        }
    }

    @Test
    fun choosingDifficultyOnlyShowsTheRelevantFollowUp() {
        compose.setContent {
            ExecutiveFunctionTheme {
                FirstRunSetupFlow(baseline(), {}, {})
            }
        }

        clickText("Suivant")
        clickText("Décider quoi faire en premier")
        clickText("Suivant")

        compose.onNodeWithText("Utiliser le dé quand choisir devient difficile")
            .performScrollTo()
            .assertExists()
        compose.onNodeWithText("Me proposer une pause après un moment").assertDoesNotExist()
    }

    @Test
    fun minimalInterventionReplacesContradictorySupportChoices() {
        var applied: FirstRunSetupConfig? = null
        compose.setContent {
            ExecutiveFunctionTheme {
                FirstRunSetupFlow(baseline(), { applied = it }, {})
            }
        }

        clickText("Suivant")
        clickText("Rester sur l’activité")
        clickText("Suivant")
        clickText("Me proposer une pause après un moment")
        clickText("Garder une petite fenêtre quand je quitte le focus")
        clickText("Le moins d’interventions possible")
        clickText("Suivant") // support -> focus defaults
        clickText("Suivant") // focus defaults -> automatic supports
        clickText("Suivant") // automatic supports -> review
        clickText("Appliquer ces réglages")

        compose.runOnIdle {
            assertEquals(false, applied?.pauseSuggestionsEnabled)
            assertEquals(false, applied?.checkInEnabled)
            assertEquals(false, applied?.autoMiniWindow)
            assertEquals(true, applied?.calmMode)
        }
    }

    @Test
    fun setupAppliesChosenTimerAndPauseInterval() {
        var applied: FirstRunSetupConfig? = null
        compose.setContent {
            ExecutiveFunctionTheme {
                FirstRunSetupFlow(baseline(), { applied = it }, {})
            }
        }

        clickText("Suivant")
        clickText("Garder la notion du temps")
        clickText("Suivant")
        clickText("Me proposer une pause après un moment")
        clickText("Suivant")
        clickText("Minuteur par défaut — je choisis une durée")
        clickText("45 min")
        clickText("Suivant")
        clickText("Suivant")
        clickText("Appliquer ces réglages")

        compose.runOnIdle {
            assertEquals(FocusTimerMode.COUNTDOWN, applied?.timerMode)
            assertEquals(true, applied?.pauseSuggestionsEnabled)
            assertEquals(45, applied?.pauseAfterMinutes)
        }
    }

    @Test
    fun setupAcceptsCustomPauseInterval() {
        var applied: FirstRunSetupConfig? = null
        compose.setContent {
            ExecutiveFunctionTheme {
                FirstRunSetupFlow(baseline(), { applied = it }, {})
            }
        }

        clickText("Suivant")
        clickText("Garder la notion du temps")
        clickText("Suivant")
        clickText("Me proposer une pause après un moment")
        clickText("Suivant")
        clickText("Autre durée")
        compose.onNodeWithTag("setup-custom-pause-minutes")
            .performScrollTo()
            .performTextReplacement("37")
        compose.waitForIdle()
        compose.onNodeWithText("Suivant").performScrollTo().assertIsEnabled().performClick()
        compose.waitForIdle()
        clickText("Suivant")
        clickText("Appliquer ces réglages")

        compose.runOnIdle {
            assertEquals(true, applied?.pauseSuggestionsEnabled)
            assertEquals(37, applied?.pauseAfterMinutes)
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
    @Test
    fun countdownDraftSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        var target: Long? = null
        restoration.setContent {
            ExecutiveFunctionTheme {
                FocusStartDialog("Lire", null, FocusTimerMode.STOPWATCH,
                    onDismiss = {}, onConfirm = { _, value -> target = value })
            }
        }
        compose.onNodeWithText("Minuteur").performClick()
        compose.onNodeWithTag("countdown-minutes").performTextInput("17")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("countdown-minutes").assertTextContains("17")
        compose.onNodeWithText("Commencer").performClick()
        compose.runOnIdle { assertEquals(1_020_000L, target) }
    }

    @Test
    fun quickNoteDraftSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        var saved: String? = null
        restoration.setContent {
            ExecutiveFunctionTheme { QuickNoteDialog({}, { saved = it }) }
        }
        compose.onNode(hasSetTextAction()).performTextInput("Reprendre au paragraphe 3")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNode(hasSetTextAction()).assertTextContains("Reprendre au paragraphe 3")
        compose.onNodeWithText("Enregistrer la note").performClick()
        compose.runOnIdle { assertEquals("Reprendre au paragraphe 3", saved) }
    }
}
