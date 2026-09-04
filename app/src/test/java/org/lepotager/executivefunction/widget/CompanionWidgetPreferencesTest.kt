package org.lepotager.executivefunction.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionWidgetPreferencesTest {
    @Test
    fun defaults_preserve_simple_widget_behavior() {
        val defaults = CompanionWidgetPreferencesData()

        assertEquals(CompanionTapAction.OPEN, defaults.compactTapAction)
        assertTrue(defaults.showTaskContext)
        assertTrue(defaults.showButtons)
        assertEquals(CompanionScenePreference.AUTO, defaults.scenePreference)
    }

    @Test
    fun automatic_scene_keeps_state_factory_choice() {
        val resolved = CompanionWidgetPreferences.resolveScene(
            CompanionScenePreference.AUTO,
            CompanionScene.SEWING,
        )

        assertEquals(CompanionScene.SEWING, resolved)
    }

    @Test
    fun fixed_scene_overrides_automatic_choice_without_productivity_logic() {
        val resolved = CompanionWidgetPreferences.resolveScene(
            CompanionScenePreference.RESTING,
            CompanionScene.CRAFTING,
        )

        assertEquals(CompanionScene.RESTING, resolved)
    }
}
