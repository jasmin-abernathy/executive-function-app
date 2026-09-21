package org.lepotager.executivefunction.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusClockTextTest {
    @Test
    fun switchesToHoursAfterSixtyMinutes() {
        assertEquals("59:59", FocusClockText.format(59 * 60_000L + 59_000L))
        assertEquals("1:00:00", FocusClockText.format(60 * 60_000L))
        assertEquals("1:35:17", FocusClockText.format(95 * 60_000L + 17_000L))
    }
}
