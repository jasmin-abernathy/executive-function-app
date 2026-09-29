package org.lepotager.executivefunction.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PauseReminderTimesTest {
    @Test fun firstReminderIsRelativeToSessionStart() {
        assertEquals(25 * 60_000L, PauseReminderTimes.first(25))
        assertEquals(5 * 60_000L, PauseReminderTimes.first(0))
    }

    @Test fun continuingAfterFiveHundredMinutesSchedulesAnotherReminder() {
        val elapsed = 500 * 60_000L
        assertEquals(525 * 60_000L, PauseReminderTimes.afterContinue(elapsed, 25))
        assertEquals(510 * 60_000L, PauseReminderTimes.afterSnooze(elapsed))
    }
}
