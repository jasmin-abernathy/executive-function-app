package org.lepotager.executivefunction.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimeLearningTest {
    @Test
    fun waitsForThreeCompletedOccurrences() {
        assertNull(TimeLearning.suggestedDurationMs(2, 12 * 60_000L))
    }

    @Test
    fun twelveMinutesAndOneSecondBecomeFourteenMinutes() {
        assertEquals(14 * 60_000L, TimeLearning.normalizedDurationMs(12 * 60_000L + 1_000L))
    }

    @Test
    fun twelveMinutesAndFiftyNineSecondsBecomeFourteenMinutes() {
        assertEquals(14 * 60_000L, TimeLearning.normalizedDurationMs(12 * 60_000L + 59_000L))
    }

    @Test
    fun exactThirteenMinutesBecomeFourteenMinutes() {
        assertEquals(14 * 60_000L, TimeLearning.normalizedDurationMs(13 * 60_000L))
    }

    @Test
    fun laterLongerOccurrenceRaisesTheSuggestion() {
        assertEquals(16 * 60_000L, TimeLearning.suggestedDurationMs(5, 14 * 60_000L + 2_000L))
    }
}
