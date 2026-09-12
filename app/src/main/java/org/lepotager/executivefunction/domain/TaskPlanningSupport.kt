package org.lepotager.executivefunction.domain

/**
 * Stable task-theme keys used for planning. Built-in keys are deliberately
 * descriptive rather than psychological: the user chooses what a task belongs to.
 */
internal object TaskThemes {
    const val WORK = "work"
    const val PERSONAL = "personal"
    const val HOME = "home"
    const val ADMIN = "admin"
    const val HEALTH = "health"
    const val RELATIONSHIPS = "relationships"
    const val CREATIVE = "creative"
    private const val CUSTOM_PREFIX = "custom:"

    val builtIns: List<String> = listOf(
        WORK,
        PERSONAL,
        HOME,
        ADMIN,
        HEALTH,
        RELATIONSHIPS,
        CREATIVE,
    )

    fun custom(label: String): String {
        val clean = label.trim().replace(Regex("\\s+"), " ")
        require(clean.isNotEmpty())
        require(clean.length <= 60)
        return CUSTOM_PREFIX + clean
    }

    fun customLabel(value: String): String? =
        value.takeIf { it.startsWith(CUSTOM_PREFIX) }
            ?.removePrefix(CUSTOM_PREFIX)
            ?.takeIf { it.isNotBlank() }

    fun isValid(value: String): Boolean =
        value.isEmpty() || value in builtIns || customLabel(value) != null
}

/** Pure helpers shared by task settings and future transparent suggestions. */
internal object TaskPlanningSupport {
    const val FIVE_MINUTES_MS = 5 * 60_000L

    /** A manual family reference wins over the learned duration when present. */
    fun effectiveDurationMs(manualDurationMs: Long?, learnedDurationMs: Long?): Long? {
        require(manualDurationMs == null || manualDurationMs > 0)
        require(learnedDurationMs == null || learnedDurationMs > 0)
        return manualDurationMs ?: learnedDurationMs
    }

    /**
     * Latest comfortable start derived from a REAL deadline and a known duration.
     * This is information only: it must never manufacture or move a deadline.
     */
    fun lastComfortableStartAt(deadlineAt: Long?, durationMs: Long?): Long? {
        if (deadlineAt == null || durationMs == null) return null
        require(deadlineAt >= 0)
        require(durationMs > 0)
        return (deadlineAt - durationMs).coerceAtLeast(0L)
    }

    /** Optional nudge when the user explicitly asks for a five-minute start suggestion. */
    fun fiveMinuteSuggestionAt(now: Long, enabled: Boolean): Long? {
        require(now >= 0)
        return if (enabled) now + FIVE_MINUTES_MS else null
    }

    /** Blank context on either side is intentionally non-restrictive. */
    fun contextMatches(taskContext: String, currentContext: String): Boolean {
        val task = taskContext.trim()
        val current = currentContext.trim()
        return task.isEmpty() || current.isEmpty() || task.equals(current, ignoreCase = true)
    }

    /** Unknown duration never excludes a task. */
    fun fitsAvailableTime(durationMs: Long?, availableMinutes: Int?): Boolean {
        require(durationMs == null || durationMs > 0)
        require(availableMinutes == null || availableMinutes >= 0)
        if (durationMs == null || availableMinutes == null || availableMinutes == 0) return true
        return durationMs <= availableMinutes * 60_000L
    }

    fun plannedStartReached(plannedStartAt: Long?, now: Long): Boolean {
        require(now >= 0)
        return plannedStartAt == null || plannedStartAt <= now
    }

    fun deadlineOverdue(deadlineAt: Long?, now: Long): Boolean {
        require(now >= 0)
        return deadlineAt != null && deadlineAt < now
    }
}
