package org.lepotager.executivefunction.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import org.lepotager.executivefunction.model.TaskStatus

class FocusCompletionPolicyTest {
    @Test fun smallStepsAlwaysRequireExplicitParentCompletion() {
        assertEquals(TaskStatus.READY, FocusCompletionPolicy.taskStatus(true))
        assertEquals(TaskStatus.COMPLETED, FocusCompletionPolicy.taskStatus(false))
    }
}
