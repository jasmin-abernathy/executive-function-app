package org.lepotager.executivefunction.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class CompanionWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CompanionWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            CompanionWidgetPreferences.delete(context, appWidgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }
}
