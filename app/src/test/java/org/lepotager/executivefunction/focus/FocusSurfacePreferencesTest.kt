package org.lepotager.executivefunction.focus

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusSurfacePreferencesTest {
    @Test
    fun intrusive_options_are_off_by_default() {
        val defaults = FocusSurfacePreferencesData()

        assertFalse(defaults.keepScreenOn)
        assertFalse(defaults.autoEnterPictureInPicture)
        assertTrue(defaults.notificationActions)
    }
}
