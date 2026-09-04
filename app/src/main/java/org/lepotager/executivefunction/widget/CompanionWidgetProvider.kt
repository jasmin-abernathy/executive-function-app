package org.lepotager.executivefunction.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.lepotager.executivefunction.MainActivity
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.data.AppDatabase
import java.time.LocalDate

/**
 * Static home-screen companion.
 *
 * This intentionally does not implement aNeko-style overlays or animation.
 * The companion is rendered as a normal Android app widget, so it stays calm,
 * optional and launcher-friendly. Official illustrations will replace the
 * temporary artwork slot later.
 */
class CompanionWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        renderAsync(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        renderAsync(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    private fun renderAsync(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        if (appWidgetIds.isEmpty()) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val database = AppDatabase(context.applicationContext)
                val state = CompanionWidgetStateFactory.create(
                    activeFocus = database.activeFocus(),
                    openTasks = database.openTasks(),
                    dayOfYear = LocalDate.now().dayOfYear,
                )
                appWidgetIds.forEach { appWidgetId ->
                    renderWidget(context, appWidgetManager, appWidgetId, state)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun renderWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        state: CompanionWidgetState,
    ) {
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val useMediumLayout = minWidth >= MEDIUM_WIDTH_DP || minHeight >= MEDIUM_HEIGHT_DP
        val layout = if (useMediumLayout) {
            R.layout.widget_companion_medium
        } else {
            R.layout.widget_companion_small
        }

        val views = RemoteViews(context.packageName, layout)
        views.setOnClickPendingIntent(
            R.id.widget_root,
            activityIntent(context, ACTION_OPEN, REQUEST_OPEN),
        )
        views.setTextViewText(R.id.widget_scene_label, sceneLabel(context, state.scene))

        if (useMediumLayout) {
            views.setTextViewText(R.id.widget_context, contextLabel(context, state))
            views.setTextViewText(R.id.widget_primary_action, primaryActionLabel(context, state.status))
            views.setOnClickPendingIntent(
                R.id.widget_primary_action,
                activityIntent(
                    context,
                    if (state.status == CompanionWidgetStatus.RESUMABLE) ACTION_RESUME else ACTION_OPEN,
                    REQUEST_PRIMARY,
                ),
            )
            views.setOnClickPendingIntent(
                R.id.widget_capture_action,
                activityIntent(context, ACTION_CAPTURE, REQUEST_CAPTURE),
            )
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun sceneLabel(context: Context, scene: CompanionScene): String = when (scene) {
        CompanionScene.RESTING -> context.getString(R.string.widget_scene_resting)
        CompanionScene.CRAFTING -> context.getString(R.string.widget_scene_crafting)
        CompanionScene.SEWING -> context.getString(R.string.widget_scene_sewing)
        CompanionScene.OBSERVING -> context.getString(R.string.widget_scene_observing)
    }

    private fun contextLabel(context: Context, state: CompanionWidgetState): String = when (state.status) {
        CompanionWidgetStatus.FOCUSING -> context.getString(
            R.string.widget_status_focusing,
            state.taskTitle.orEmpty(),
        )
        CompanionWidgetStatus.RESUMABLE -> context.getString(
            R.string.widget_status_resumable,
            state.taskTitle.orEmpty(),
        )
        CompanionWidgetStatus.READY -> context.getString(
            R.string.widget_status_ready,
            state.taskTitle.orEmpty(),
        )
        CompanionWidgetStatus.QUIET -> context.getString(R.string.widget_status_quiet)
    }

    private fun primaryActionLabel(context: Context, status: CompanionWidgetStatus): String = when (status) {
        CompanionWidgetStatus.RESUMABLE -> context.getString(R.string.widget_resume_action)
        CompanionWidgetStatus.FOCUSING -> context.getString(R.string.widget_open_focus_action)
        CompanionWidgetStatus.READY,
        CompanionWidgetStatus.QUIET,
        -> context.getString(R.string.widget_open_action)
    }

    private fun activityIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            this.action = action
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_OPEN = "org.lepotager.executivefunction.widget.OPEN"
        const val ACTION_CAPTURE = "org.lepotager.executivefunction.widget.CAPTURE"
        const val ACTION_RESUME = "org.lepotager.executivefunction.widget.RESUME"

        private const val REQUEST_OPEN = 4100
        private const val REQUEST_PRIMARY = 4101
        private const val REQUEST_CAPTURE = 4102
        private const val MEDIUM_WIDTH_DP = 110
        private const val MEDIUM_HEIGHT_DP = 90

        /** Request a refresh after app data changes. */
        fun requestUpdate(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext)
            val provider = ComponentName(appContext, CompanionWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(provider)
            if (ids.isEmpty()) return

            val intent = Intent(appContext, CompanionWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            appContext.sendBroadcast(intent)
        }
    }
}
