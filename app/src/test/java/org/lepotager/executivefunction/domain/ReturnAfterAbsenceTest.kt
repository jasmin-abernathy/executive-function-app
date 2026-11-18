package org.lepotager.executivefunction.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReturnAfterAbsenceTest {
    @Test fun longBreakChangesOnlyTheGreeting() {
        val interruptedAt = 1_000_000L
        assertFalse(ReturnAfterAbsence.shouldAskWhatHelpsNow(interruptedAt, interruptedAt + 60_000))
        assertFalse(ReturnAfterAbsence.shouldAskWhatHelpsNow(interruptedAt, interruptedAt + ReturnAfterAbsence.LONG_BREAK_MS - 1))
        assertTrue(ReturnAfterAbsence.shouldAskWhatHelpsNow(interruptedAt, interruptedAt + ReturnAfterAbsence.LONG_BREAK_MS))
        assertFalse(ReturnAfterAbsence.shouldAskWhatHelpsNow(interruptedAt, interruptedAt - 1))
        assertFalse(ReturnAfterAbsence.shouldAskWhatHelpsNow(0, interruptedAt))
    }
}
