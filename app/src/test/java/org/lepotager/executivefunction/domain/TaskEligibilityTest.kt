package org.lepotager.executivefunction.domain

import org.junit.Assert.*
import org.junit.Test

class TaskEligibilityTest {
    @Test fun unknownDurationDoesNotExcludeATask() {
        assertTrue(TaskEligibility.accepts(0,null,null,5,"",""))
    }
    @Test fun energyAndAvailableTimeBothMatter() {
        assertFalse(TaskEligibility.accepts(3,1,60_000,5,"",""))
        assertFalse(TaskEligibility.accepts(1,1,360_000,5,"",""))
        assertTrue(TaskEligibility.accepts(1,1,300_000,5,"",""))
    }
    @Test fun contextIsOptionalAndCaseInsensitive() {
        assertTrue(TaskEligibility.accepts(0,null,null,null," Maison ","maison"))
        assertTrue(TaskEligibility.accepts(0,null,null,null,"","maison"))
        assertFalse(TaskEligibility.accepts(0,null,null,null,"bureau","maison"))
    }
}
