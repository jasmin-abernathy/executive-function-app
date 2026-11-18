package org.lepotager.executivefunction.model

internal enum class CompanionPose {
    NEUTRAL,
    ACTIVE,
    RESTING,
}

/** Derives the pose from the focus session, independently of the user's energy check-in. */
internal fun AppSnapshot.companionPose(): CompanionPose =
    when (activeFocus?.session?.status) {
        FocusStatus.RUNNING -> CompanionPose.ACTIVE
        FocusStatus.INTERRUPTED -> CompanionPose.RESTING
        FocusStatus.POSTPONED, FocusStatus.COMPLETED, null -> CompanionPose.NEUTRAL
    }
