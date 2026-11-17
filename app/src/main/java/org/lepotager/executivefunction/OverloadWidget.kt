package org.lepotager.executivefunction

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** A small, static, user-chosen shortcut: no check-in data or messages on the launcher. */
class OverloadWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.overload_widget)
            val intent = Intent(context, MainActivity::class.java)
                .putExtra("quick_action", "overload")
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pending = PendingIntent.getActivity(
                context, 4204, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            views.setOnClickPendingIntent(R.id.overload_widget_button, pending)
            manager.updateAppWidget(id, views)
        }
    }
}
