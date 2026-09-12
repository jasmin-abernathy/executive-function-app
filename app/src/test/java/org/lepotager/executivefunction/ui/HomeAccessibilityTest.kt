package org.lepotager.executivefunction.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme
import org.lepotager.executivefunction.ui.theme.LocalCalmMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "fr")
class HomeAccessibilityTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun calmWidgetDrawHasClickableResultAndAccessibleHandleWithoutReordering() {
        var writes = 0
        var started: String? = null
        var tasks by mutableStateOf(emptyList<TaskItem>())
        compose.setContent {
            ExecutiveFunctionTheme {
                CompositionLocalProvider(LocalCalmMode provides true) {
                    HomeScreen(
                        tasks = tasks, drawRequest = 1,
                        drawEnabled = true, pauseSuggestionsEnabled = false, pauseAfterMinutes = 25,
                        onCapture = { _, _, _, _ -> }, onStart = { started = it },
                        onMoveTask = { _, _ -> }, onApplyTaskOrder = { _, done -> writes++; done(true) },
                        onSetTaskColor = { _, _ -> }, onSetDrawEnabled = {},
                        onSetPauseSuggestionsEnabled = {}, onSetPauseAfterMinutes = {},
                    )
                }
            }
        }
        compose.runOnIdle {
            tasks = listOf("a", "b").map { TaskItem(it, it, null, TaskStatus.READY, 0, 0) }
        }
        compose.waitForIdle()
        assertEquals(0, writes)
        val handle = "Réordonner a. Appui long puis glisser, ou utiliser Monter et Descendre."
        compose.onNodeWithTag("home-task-list").performScrollToNode(hasContentDescription(handle))
        val semantics = compose.onNodeWithContentDescription(handle).fetchSemanticsNode().config
        assertTrue(semantics[SemanticsActions.CustomActions].isNotEmpty())
        compose.onNodeWithTag("home-task-list").performScrollToNode(hasText("Le dé propose"))
        compose.onNodeWithTag("home-task-list").performScrollToNode(hasText("Relancer"))
        compose.onNodeWithText("Relancer").performClick()
        compose.waitForIdle()
        assertEquals(0, writes)
        compose.onNodeWithTag("home-task-list").performScrollToNode(hasText("Le dé propose"))
        compose.onNode(hasText("Le dé propose") and hasClickAction()).performClick()
        assertTrue(started in setOf("a", "b"))
        assertEquals(0, writes)
    }

    @Test
    fun explicitShufflePersistsBeforeOfferingToStart() {
        var savedOrder: List<String>? = null
        var started: String? = null
        var tasks by mutableStateOf(listOf("a", "b").map { TaskItem(it, it, null, TaskStatus.READY, 0, 0) })
        compose.setContent {
            ExecutiveFunctionTheme {
                CompositionLocalProvider(LocalCalmMode provides true) {
                    HomeScreen(
                        tasks = tasks,
                        drawEnabled = true, pauseSuggestionsEnabled = false, pauseAfterMinutes = 25,
                        onCapture = { _, _, _, _ -> }, onStart = { started = it },
                        onMoveTask = { _, _ -> }, onApplyTaskOrder = { ids, done ->
                            savedOrder = ids
                            tasks = ids.map { id -> tasks.first { it.id == id } }
                            done(true)
                        },
                        onSetTaskColor = { _, _ -> }, onSetDrawEnabled = {},
                        onSetPauseSuggestionsEnabled = {}, onSetPauseAfterMinutes = {},
                    )
                }
            }
        }
        val draw = "Mélanger l’ordre des tâches au dé"
        compose.onNodeWithTag("home-task-list").performScrollToNode(hasText(draw))
        compose.onNodeWithText(draw).performClick()
        compose.waitForIdle()
        assertEquals(setOf("a", "b"), savedOrder?.toSet())
        assertNull(started)
        compose.onNodeWithTag("home-task-list").performScrollToNode(hasText("Le dé propose"))
        compose.onNodeWithTag("home-task-list").performScrollToNode(hasText("Le dé propose"))
        compose.onNode(hasText("Le dé propose") and hasClickAction()).performClick()
        assertEquals(savedOrder?.first(), started)
    }
}
