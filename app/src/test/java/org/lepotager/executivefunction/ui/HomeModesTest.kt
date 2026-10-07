package org.lepotager.executivefunction.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.domain.HomeViewMode
import org.lepotager.executivefunction.model.TaskItem
import org.lepotager.executivefunction.model.TaskStatus
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "fr")
class HomeModesTest {
    @get:Rule val compose = createComposeRule()

    private val tasks = listOf("Courant", "Suivante 1", "Suivante 2", "Quatrième")
        .mapIndexed { index, title ->
            TaskItem("task-$index", title, null, TaskStatus.READY, index.toLong(), index.toLong())
        }

    private fun render(onStart: (String) -> Unit = {}, onCustom: (String) -> Unit = {}) {
        compose.setContent {
            ExecutiveFunctionTheme {
                HomeScreen(
                    tasks = tasks,
                    drawEnabled = false,
                    pauseSuggestionsEnabled = true,
                    pauseAfterMinutes = 25,
                    onCapture = { _, _, _, done -> done() },
                    onStart = onStart,
                    homeViewMode = HomeViewMode.NOW_NEXT,
                    customStartAvailable = true,
                    onStartCustom = onCustom,
                    onMoveTask = { _, _ -> },
                    onApplyTaskOrder = { _, settled -> settled(true) },
                    onSetTaskColor = { _, _ -> },
                    onSetDrawEnabled = {},
                    onSetPauseSuggestionsEnabled = {},
                    onSetPauseAfterMinutes = {},
                )
            }
        }
    }

    private fun scrollToTodayPanel() {
        compose.onNodeWithTag("home-task-list").performScrollToNode(hasText("Maintenant"))
    }

    @Test fun nowNextShowsOnlyCurrentAndTwoAlternatives() {
        render()
        scrollToTodayPanel()
        compose.onNodeWithText("Maintenant").assertExists()
        compose.onNodeWithText("Ensuite").assertExists()
        compose.onNodeWithText("Courant").assertExists()
        compose.onNodeWithText("Suivante 1").assertExists()
        compose.onNodeWithText("Suivante 2").assertExists()
        compose.onNodeWithText("Quatrième").assertDoesNotExist()
    }

    @Test fun primaryAndCustomActionsUseTheirOwnCallbacks() {
        var started: String? = null
        var customized: String? = null
        render(onStart = { started = it }, onCustom = { customized = it })
        scrollToTodayPanel()
        compose.onNodeWithTag("home-primary-start").performScrollTo().performClick()
        compose.onNodeWithTag("home-custom-start").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals("task-0", started)
            assertEquals("task-0", customized)
        }
    }

    @Test fun difficultDayHidesAlternativesAndCanRestoreThem() {
        render()
        scrollToTodayPanel()
        compose.onNodeWithText("Aujourd’hui, ça coince").performScrollTo().performClick()
        compose.onNodeWithText("Suivante 1").assertDoesNotExist()
        compose.onNodeWithText("Revenir à la vue habituelle").performScrollTo().performClick()
        compose.onNodeWithText("Suivante 1").assertExists()
    }

    @Test fun difficultDayClosesAnAlreadyExpandedTaskList() {
        render()
        scrollToTodayPanel()
        compose.onNodeWithText("Voir toutes mes tâches").performScrollTo().performClick()
        compose.onNodeWithText("Quatrième").assertExists()
        compose.onNodeWithText("Aujourd’hui, ça coince").performScrollTo().performClick()
        compose.onNodeWithText("Quatrième").assertDoesNotExist()
        compose.onNodeWithText("Revenir à la vue habituelle").performScrollTo().performClick()
        compose.onNodeWithText("Suivante 1").assertExists()
        compose.onNodeWithText("Quatrième").assertDoesNotExist()
    }
}