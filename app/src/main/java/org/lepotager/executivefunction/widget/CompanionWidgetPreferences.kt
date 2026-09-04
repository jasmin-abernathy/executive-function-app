package org.lepotager.executivefunction.widget

import android.content.Context

enum class CompanionTapAction {
    OPEN,
    RESUME,
    CAPTURE,
}

data class CompanionWidgetPreferencesData(
    val compactTapAction: CompanionTapAction = CompanionTapAction.OPEN,
    val showTaskContext: Boolean = true,
)

/**
 * Lightweight per-widget preferences stored locally on-device.
 * No account, cloud sync, analytics, or companion artwork is involved.
 */
object CompanionWidgetPreferences {
    private const val FILE_NAME = "companion-widget-preferences"

    fun load(context: Context, appWidgetId: Int?): CompanionWidgetPreferencesData {
        if (appWidgetId == null) return CompanionWidgetPreferencesData()
        val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val action = preferences.getString(actionKey(appWidgetId), null)
            ?.let { stored -> CompanionTapAction.entries.firstOrNull { it.name == stored } }
            ?: CompanionTapAction.OPEN

        return CompanionWidgetPreferencesData(
            compactTapAction = action,
            showTaskContext = preferences.getBoolean(contextKey(appWidgetId), true),
        )
    }

    fun save(
        context: Context,
        appWidgetId: Int,
        value: CompanionWidgetPreferencesData,
    ) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(actionKey(appWidgetId), value.compactTapAction.name)
            .putBoolean(contextKey(appWidgetId), value.showTaskContext)
            .apply()
    }

    fun delete(context: Context, appWidgetId: Int) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(actionKey(appWidgetId))
            .remove(contextKey(appWidgetId))
            .apply()
    }

    private fun actionKey(id: Int) = "compact_action_$id"
    private fun contextKey(id: Int) = "show_context_$id"
}
