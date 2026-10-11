package org.lepotager.executivefunction.domain

import org.junit.Assert.*
import org.junit.Test

class LowEnergyModeTest {
    @Test fun lowMoodOrLowEnergyActivatesImmediately() {
        assertTrue(LowEnergyMode.active(1, 3, 100, 100))
        assertTrue(LowEnergyMode.active(3, 1, 100, 100))
        assertFalse(LowEnergyMode.active(2, 2, 100, 100))
    }
    @Test fun oldOrFutureCheckInDoesNotActivate() {
        assertFalse(LowEnergyMode.active(0, 0, 100, 99))
        assertTrue(LowEnergyMode.active(0, 0, 100, 100 + LowEnergyMode.VALID_FOR_MS - 1))
        assertFalse(LowEnergyMode.active(0, 0, 100, 100 + LowEnergyMode.VALID_FOR_MS))
    }
}
