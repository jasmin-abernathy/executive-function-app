package org.lepotager.executivefunction.widget

import android.content.Context

enum class CompanionTapAction {
    OPEN,
    RESUME,
    CAPTURE,
}

enum class CompanionScenePreference {
    AUTO,
    RESTING,
    CRAFTING,
    SEWING,
    OBSERVING,
}

data class CompanionWidgetPreferencesData(
    val compactTapAction: CompanionTapAction = CompanionTapAction.OPEN,
    val showTaskContext: Boolean = true,
    val showButtons: Boolean = true,
    val scenePreference: CompanionScenePreference = CompanionScenePreference.AUTO,
)

/** Per-widget preferences kept locally and independent from companion artwork. */
object CompanionWidgetPreferences {
    private const val FILE_NAME = "companion_widget_preferences"

    fun load(context: Context, appWidgetId: Int?): CompanionWidgetPreferencesData {
        val prefs = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val prefix = prefix(appWidgetId)
        return CompanionWidgetPreferencesData(
            compactTapAction = enumValueOrDefault(
                prefs.getString("${prefix}compact_tap", null),
                CompanionTapAction.OPEN,
            ),
            showTaskContext = prefs.getBoolean("${prefix}show_task_context", true),
            showButtons = prefs.getBoolean("${prefix}show_buttons", true),
            scenePreference = enumValueOrDefault(
                prefs.getString("${prefix}scene", null),
                CompanionScenePreference.AUTO,
            ),
        )
    }

    fun save(
        context: Context,
        appWidgetId: Int,
        data: CompanionWidgetPreferencesData,
    ) {
        val prefix = prefix(appWidgetId)
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("${prefix}compact_tap", data.compactTapAction.name)
            .putBoolean("${prefix}show_task_context", data.showTaskContext)
            .putBoolean("${prefix}show_buttons", data.showButtons)
            .putString("${prefix}scene", data.scenePreference.name)
            .apply()
    }

    fun delete(context: Context, appWidgetId: Int) {
        val prefs = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val prefix = prefix(appWidgetId)
        prefs.edit().apply {
            prefs.all.keys
                .filter { it.startsWith(prefix) }
                .forEach(::remove)
        }.apply()
    }

    fun resolveScene(
        preference: CompanionScenePreference,
        automaticScene: CompanionScene,
    ): CompanionScene = when (preference) {
        CompanionScenePreference.AUTO -> automaticScene
        CompanionScenePreference.RESTING -> CompanionScene.RESTING
        CompanionScenePreference.CRAFTING -> CompanionScene.CRAFTING
        CompanionScenePreference.SEWING -> CompanionScene.SEWING
        CompanionScenePreference.OBSERVING -> CompanionScene.OBSERVING
    }

    private fun prefix(appWidgetId: Int?): String = "widget_${appWidgetId ?: 0}_"

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T =
        value?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } } ?: fallback
}
