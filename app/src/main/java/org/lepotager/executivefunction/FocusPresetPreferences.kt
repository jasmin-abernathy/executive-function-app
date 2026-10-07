package org.lepotager.executivefunction

import android.content.SharedPreferences
import org.lepotager.executivefunction.domain.FocusSessionPreset
import org.lepotager.executivefunction.domain.FocusTimerMode

/** Reads the global focus preset and materializes the one-time migration from the legacy timer mode. */
internal object FocusPresetPreferences {
    const val KEY = "focus_session_preset"

    fun load(
        preferences: SharedPreferences,
        legacyTimerMode: FocusTimerMode?,
    ): FocusSessionPreset {
        val saved = preferences.getString(KEY, null)?.let {
            runCatching { FocusSessionPreset.valueOf(it) }.getOrNull()
        }
        if (saved != null) return saved

        val migrated = if (legacyTimerMode == FocusTimerMode.STOPWATCH) {
            FocusSessionPreset.OPEN
        } else {
            FocusSessionPreset.NORMAL
        }
        preferences.edit().putString(KEY, migrated.name).apply()
        return migrated
    }
}
