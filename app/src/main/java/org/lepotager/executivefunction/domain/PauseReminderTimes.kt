package org.lepotager.executivefunction.domain

/** Deadlines are measured in active-session time; interrupted time never counts. */
object PauseReminderTimes {
    fun first(minutes: Int): Long = minutes.coerceIn(5, 120) * 60_000L

    fun afterContinue(elapsedMs: Long, minutes: Int): Long =
        elapsedMs.coerceAtLeast(0) + first(minutes)

    fun afterSnooze(elapsedMs: Long): Long = elapsedMs.coerceAtLeast(0) + 600_000L
}
