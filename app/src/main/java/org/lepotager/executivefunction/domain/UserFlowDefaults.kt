package org.lepotager.executivefunction.domain

enum class HomeViewMode { ONE_NEXT, NOW_NEXT, LIST }

enum class FocusSessionPreset(val fallbackMinutes: Int?) {
    SHORT(10),
    NORMAL(25),
    LONG(50),
    OPEN(null),
}

object FocusStartDefaults {
    private const val MINUTE_MS = 60_000L

    fun targetDurationMs(
        preset: FocusSessionPreset,
        learnedTargetDurationMs: Long?,
        preferLearnedDuration: Boolean,
    ): Long? {
        require(learnedTargetDurationMs == null || learnedTargetDurationMs > 0)
        if (preset == FocusSessionPreset.OPEN) return null
        if (preferLearnedDuration && learnedTargetDurationMs != null) return learnedTargetDurationMs
        return requireNotNull(preset.fallbackMinutes).toLong() * MINUTE_MS
    }

    fun timerMode(preset: FocusSessionPreset): FocusTimerMode =
        if (preset == FocusSessionPreset.OPEN) FocusTimerMode.STOPWATCH else FocusTimerMode.COUNTDOWN
}
