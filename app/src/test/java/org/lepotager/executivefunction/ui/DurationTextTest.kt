package org.lepotager.executivefunction.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationTextTest {
    @Test fun staysInMinutesBelowOneHour() {
        assertEquals("45 min", DurationText.minutes(45))
    }

    @Test fun convertsWholeHours() {
        assertEquals("1 h", DurationText.minutes(60))
        assertEquals("2 h", DurationText.minutes(120))
    }

    @Test fun keepsRemainingMinutes() {
        assertEquals("1 h 15 min", DurationText.minutes(75))
        assertEquals("2 h 5 min", DurationText.minutes(125))
    }
}
