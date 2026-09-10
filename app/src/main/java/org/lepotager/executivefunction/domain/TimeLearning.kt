package org.lepotager.executivefunction.domain

/**
 * A small deterministic learning rule. Nothing leaves the device and no opaque
 * scoring is involved: after a few completed occurrences, the longest active
 * duration becomes the reference and a deliberately generous minute is added.
 */
object TimeLearning {
    const val MIN_COMPLETED_OCCURRENCES = 3
    private const val MINUTE_MS = 60_000L

    fun suggestedDurationMs(completedCount: Int, longestActiveDurationMs: Long?): Long? {
        if (completedCount < MIN_COMPLETED_OCCURRENCES) return null
        val longest = longestActiveDurationMs?.takeIf { it > 0 } ?: return null
        return normalizedDurationMs(longest)
    }

    /** Round up to the next whole minute, then add one full minute of margin. */
    fun normalizedDurationMs(activeDurationMs: Long): Long {
        require(activeDurationMs >= 0)
        val roundedUpMinutes = if (activeDurationMs == 0L) {
            0L
        } else {
            Math.addExact(activeDurationMs, MINUTE_MS - 1) / MINUTE_MS
        }
        return Math.multiplyExact(roundedUpMinutes + 1, MINUTE_MS)
    }
}
