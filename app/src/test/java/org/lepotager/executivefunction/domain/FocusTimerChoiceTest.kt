package org.lepotager.executivefunction.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocusTimerChoiceTest {
    @Test
    fun stopwatchHasNoTargetEvenWhenMinutesAreProvided() {
        assertNull(FocusTimerChoice.targetDurationMs(FocusTimerMode.STOPWATCH, 25))
    }

    @Test
    fun countdownUsesChosenPositiveMinutes() {
        assertEquals(14 * 60_000L, FocusTimerChoice.targetDurationMs(FocusTimerMode.COUNTDOWN, 14))
    }

    @Test(expected = IllegalArgumentException::class)
    fun countdownRejectsZeroMinutes() {
        FocusTimerChoice.targetDurationMs(FocusTimerMode.COUNTDOWN, 0)
    }

    @Test
    fun learnedTargetIsRoundedForTheMinutesField() {
        assertEquals(14, FocusTimerChoice.suggestedMinutes(13 * 60_000L + 1))
        assertNull(FocusTimerChoice.suggestedMinutes(null))
    }

    @Test
    fun persistedTargetDeterminesTheSessionMode() {
        assertEquals(FocusTimerMode.STOPWATCH, FocusTimerChoice.modeFor(null))
        assertEquals(FocusTimerMode.COUNTDOWN, FocusTimerChoice.modeFor(1L))
    }
}
