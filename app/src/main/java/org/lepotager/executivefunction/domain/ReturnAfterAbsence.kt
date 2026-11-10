package org.lepotager.executivefunction.domain

/** Product prompt only: never alters the persisted focus session or its elapsed time. */
object ReturnAfterAbsence {
    const val LONG_BREAK_MS: Long = 24 * 60 * 60 * 1_000L

    fun shouldAskWhatHelpsNow(interruptedAtMs: Long, nowMs: Long): Boolean =
        interruptedAtMs > 0L && nowMs >= interruptedAtMs &&
            nowMs - interruptedAtMs >= LONG_BREAK_MS
}
