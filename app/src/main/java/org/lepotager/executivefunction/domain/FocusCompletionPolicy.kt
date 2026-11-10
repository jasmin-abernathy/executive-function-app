package org.lepotager.executivefunction.domain

import org.lepotager.executivefunction.model.TaskStatus

/** Finishing time is not confirmation that a parent with small steps is done. */
object FocusCompletionPolicy {
    fun taskStatus(parentHasSteps: Boolean): TaskStatus =
        if (parentHasSteps) TaskStatus.READY else TaskStatus.COMPLETED
}
