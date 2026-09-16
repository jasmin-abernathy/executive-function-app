package org.lepotager.executivefunction.domain

/** How the current focus session should display time. The persisted session target remains the source of truth. */
enum class FocusTimerMode { STOPWATCH, COUNTDOWN }

/** Pure conversion helpers used by the start UI and tests. */
object FocusTimerChoice {
    const val MAX_MINUTES = 10_080
    private const val MINUTE_MS = 60_000L

    fun targetDurationMs(mode: FocusTimerMode, countdownMinutes: Int?): Long? = when (mode) {
        FocusTimerMode.STOPWATCH -> null
        FocusTimerMode.COUNTDOWN -> {
            val minutes = requireNotNull(countdownMinutes) { "A countdown needs a duration" }
            require(minutes in 1..MAX_MINUTES) { "Countdown duration is out of range" }
            Math.multiplyExact(minutes.toLong(), MINUTE_MS)
        }
    }

    fun suggestedMinutes(targetDurationMs: Long?): Int? {
        val target = targetDurationMs?.takeIf { it > 0 } ?: return null
        val roundedUp = Math.addExact(target, MINUTE_MS - 1) / MINUTE_MS
        return roundedUp.coerceAtMost(MAX_MINUTES.toLong()).toInt()
    }

    fun modeFor(targetDurationMs: Long?): FocusTimerMode =
        if (targetDurationMs == null) FocusTimerMode.STOPWATCH else FocusTimerMode.COUNTDOWN
}
