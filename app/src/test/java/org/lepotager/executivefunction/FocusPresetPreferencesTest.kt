package org.lepotager.executivefunction

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.lepotager.executivefunction.domain.FocusSessionPreset
import org.lepotager.executivefunction.domain.FocusTimerMode
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FocusPresetPreferencesTest {
    @Test fun migratedPresetIsPersistedAndUnaffectedByLaterOneOffTimerMode() {
        val preferences = RuntimeEnvironment.getApplication<Application>()
            .getSharedPreferences("focus-preset-migration-test", 0)
        preferences.edit().clear().commit()

        try {
            assertEquals(
                FocusSessionPreset.OPEN,
                FocusPresetPreferences.load(preferences, FocusTimerMode.STOPWATCH),
            )

            // A later custom session updates the legacy timer mode, but the global preset stays fixed.
            assertEquals(
                FocusSessionPreset.OPEN,
                FocusPresetPreferences.load(preferences, FocusTimerMode.COUNTDOWN),
            )
            assertEquals(
                FocusSessionPreset.OPEN.name,
                preferences.getString(FocusPresetPreferences.KEY, null),
            )
        } finally {
            preferences.edit().clear().commit()
        }
    }
}
