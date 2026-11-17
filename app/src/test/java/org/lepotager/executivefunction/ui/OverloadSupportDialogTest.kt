package org.lepotager.executivefunction.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.ui.theme.ExecutiveFunctionTheme
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "fr")
class OverloadSupportDialogTest {
    @get:Rule val compose = createComposeRule()

    @Test fun offersQuietCardWithoutChangingFocus() {
        var interrupted = 0
        var dismissed = 0
        compose.setContent {
            ExecutiveFunctionTheme {
                OverloadSupportDialog(
                    hasRunningFocus = true,
                    onKeepPlace = { interrupted++ },
                    onDismiss = { dismissed++ },
                )
            }
        }
        compose.onNodeWithText("Réduire les sollicitations").performClick()
        compose.onNodeWithText("1. Si possible, éloigne-toi quelques instants du bruit ou de la lumière.",
        ).assertExists()
        compose.onNodeWithText("Retour").performClick()
        compose.onNodeWithText("Fermer").performClick()
        compose.runOnIdle {
            assertEquals(0, interrupted)
            assertTrue(dismissed > 0)
        }
    }

    @Test fun pauseIsOnlyOfferedDuringRunningFocusAndCallsExistingFlow() {
        var interrupted = 0
        var dismissed = 0
        compose.setContent {
            ExecutiveFunctionTheme {
                OverloadSupportDialog(
                    hasRunningFocus = true,
                    onKeepPlace = { interrupted++ },
                    onDismiss = { dismissed++ },
                )
            }
        }
        compose.onNodeWithText("Mettre le focus en pause et garder ma place").performClick()
        compose.runOnIdle {
            assertEquals(1, interrupted)
            assertEquals(1, dismissed)
        }
    }
}
