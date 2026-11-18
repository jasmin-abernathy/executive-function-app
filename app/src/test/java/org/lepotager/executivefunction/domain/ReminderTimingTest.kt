package org.lepotager.executivefunction.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderTimingTest {
    @Test fun overdueReminderDoesNotLoseItsIntendedDueTime() {
        assertEquals(20_000L, ReminderTiming.nextDeliveryAt(10_000L, 19_000L))
        assertEquals(60_000L, ReminderTiming.nextDeliveryAt(60_000L, 19_000L))
        assertEquals(21_000L, ReminderTiming.nextDeliveryAt(20_000L, 20_000L))
    }
}
