package org.lepotager.executivefunction.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusSession
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "fr")
class FocusTapToPauseTest {
    @get:Rule val compose = createComposeRule()

    @Test fun tappingClockInvokesPauseNotTaskCompletion() {
        val now = System.currentTimeMillis()
        val task = TaskItem("focus-test-task", "Lire", null, TaskStatus.IN_PROGRESS, now, now)
        val session = FocusSession("focus-test-session", task.id, FocusStatus.RUNNING,
            0L, now, null, now, now, 25 * 60_000L)
        var paused = 0
        var finished = 0
        compose.setContent {
            ExecutiveFunctionTheme {
                FocusScreen(
                    activeFocus = ActiveFocus(task, session),
                    overlayEnabled = false,
                    pauseSuggestionsEnabled = false,
                    pauseAfterMinutes = 25,
                    onToggleOverlay = {},
                    onQuickCapture = { _, _, _ -> },
                    onInterrupt = { paused++ },
                    onComplete = { finished++ },
                )
            }
        }
        compose.onNodeWithTag("focus-timer-tap-pause").assertHasClickAction().performClick()
        compose.runOnIdle {
            assertEquals(1, paused)
            assertEquals(0, finished)
        }
    }
}
