package org.lepotager.executivefunction.focus

import android.content.Context

/**
 * Dormant/optional focus-surface switches.
 *
 * Keep intrusive behaviours off by default. The code is present so a later
 * settings screen can expose the options without changing the focus data model.
 */
data class FocusSurfacePreferencesData(
    val keepScreenOn: Boolean = false,
    val autoEnterPictureInPicture: Boolean = false,
    val notificationActions: Boolean = true,
)

class FocusSurfacePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE,
    )

    fun load(): FocusSurfacePreferencesData = FocusSurfacePreferencesData(
        keepScreenOn = preferences.getBoolean(KEY_KEEP_SCREEN_ON, false),
        autoEnterPictureInPicture = preferences.getBoolean(KEY_AUTO_PIP, false),
        notificationActions = preferences.getBoolean(KEY_NOTIFICATION_ACTIONS, true),
    )

    fun setKeepScreenOn(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
    }

    fun setAutoEnterPictureInPicture(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_PIP, enabled).apply()
    }

    fun setNotificationActions(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_ACTIONS, enabled).apply()
    }

    companion object {
        private const val FILE_NAME = "focus_surface_preferences"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_AUTO_PIP = "auto_enter_pip"
        private const val KEY_NOTIFICATION_ACTIONS = "notification_actions"
    }
}
