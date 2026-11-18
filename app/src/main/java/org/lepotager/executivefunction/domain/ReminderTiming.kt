package org.lepotager.executivefunction.domain

/** Re-arm overdue reminders with a short delay, without changing their saved due date. */
object ReminderTiming {
    fun nextDeliveryAt(dueAtMs: Long, nowMs: Long): Long =
        maxOf(dueAtMs, nowMs + 1_000L)
}
