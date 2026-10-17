package org.lepotager.executivefunction.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object TaskDeadline {
    fun warningDays(date: LocalDate, today: LocalDate): Int =
        if (ChronoUnit.DAYS.between(today, date) <= 14) 7 else 14

    fun intensity(daysLeft: Long, warningDays: Int): Float =
        if (daysLeft > warningDays) 0f else ((warningDays - daysLeft + 1f) / (warningDays + 1f)).coerceIn(0f, 1f)
}
