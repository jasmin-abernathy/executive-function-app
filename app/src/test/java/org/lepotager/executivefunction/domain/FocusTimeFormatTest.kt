package org.lepotager.executivefunction.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusTimeFormatTest {
    @Test fun displaysHoursAfterFiftyNineMinutes() {
        assertEquals("59:59", FocusTimeFormat.format(3_599_000))
        assertEquals("1:00:00", FocusTimeFormat.format(3_600_000))
        assertEquals("8:20:00", FocusTimeFormat.format(500 * 60_000L))
    }
}
