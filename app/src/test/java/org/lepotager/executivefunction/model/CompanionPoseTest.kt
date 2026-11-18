package org.lepotager.executivefunction.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CompanionPoseTest {
    @Test
    fun noSessionIsNeutralEvenInLowEnergyMode() {
        assertEquals(CompanionPose.NEUTRAL, AppSnapshot(lowEnergyUntil = Long.MAX_VALUE).companionPose())
    }

    @Test
    fun poseTracksSessionAcrossPauseResumeAndClose() {
        val task = TaskItem("task", "Write", null, TaskStatus.IN_PROGRESS, 0L, 0L)
        val session = FocusSession("focus", task.id, FocusStatus.RUNNING, 0L, 0L, null, 0L, 0L)
        val snapshot = AppSnapshot(activeFocus = ActiveFocus(task, session), loading = false)

        assertEquals(CompanionPose.ACTIVE, snapshot.companionPose())

        val paused = snapshot.copy(
            activeFocus = ActiveFocus(task, session.copy(status = FocusStatus.INTERRUPTED, segmentStartedAt = null)),
        )
        assertEquals(CompanionPose.RESTING, paused.companionPose())
        assertEquals(CompanionPose.ACTIVE, paused.copy(activeFocus = snapshot.activeFocus).companionPose())

        for (status in listOf(FocusStatus.POSTPONED, FocusStatus.COMPLETED)) {
            val closed = snapshot.copy(activeFocus = ActiveFocus(task, session.copy(status = status)))
            assertEquals(CompanionPose.NEUTRAL, closed.companionPose())
        }
    }
}
