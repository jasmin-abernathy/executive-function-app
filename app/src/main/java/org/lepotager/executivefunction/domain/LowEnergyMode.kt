package org.lepotager.executivefunction.domain

/** Explicit UI adaptation, not a health assessment. A new check-in replaces the previous one. */
object LowEnergyMode {
    const val VALID_FOR_MS = 4 * 60 * 60 * 1_000L
    fun active(mood: Int, energy: Int, recordedAt: Long, now: Long): Boolean =
        now - recordedAt in 0 until VALID_FOR_MS && (mood <= 1 || energy <= 1)
}
