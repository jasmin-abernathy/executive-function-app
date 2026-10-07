package org.lepotager.executivefunction.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocusStartDefaultsTest {
    @Test fun fixedPresetIsUsedWithoutLearnedDuration() {
        assertEquals(25 * 60_000L, FocusStartDefaults.targetDurationMs(FocusSessionPreset.NORMAL, null, true))
    }

    @Test fun learnedDurationIsUsedOnlyWhenPreferenceIsEnabled() {
        assertEquals(17 * 60_000L, FocusStartDefaults.targetDurationMs(FocusSessionPreset.SHORT, 17 * 60_000L, true))
        assertEquals(10 * 60_000L, FocusStartDefaults.targetDurationMs(FocusSessionPreset.SHORT, 17 * 60_000L, false))
    }

    @Test fun openPresetHasNoTarget() {
        assertNull(FocusStartDefaults.targetDurationMs(FocusSessionPreset.OPEN, 17 * 60_000L, true))
        assertEquals(FocusTimerMode.STOPWATCH, FocusStartDefaults.timerMode(FocusSessionPreset.OPEN))
    }

    @Test fun fixedPresetsUseCountdown() {
        listOf(FocusSessionPreset.SHORT, FocusSessionPreset.NORMAL, FocusSessionPreset.LONG).forEach {
            assertEquals(FocusTimerMode.COUNTDOWN, FocusStartDefaults.timerMode(it))
        }
    }
}
