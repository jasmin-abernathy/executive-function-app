package org.lepotager.executivefunction

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** Static functional entry points. No background polling or companion assets. */
class ToolsWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context,manager: AppWidgetManager,ids: IntArray) {
        ids.forEach {id->
            val views=RemoteViews(context.packageName,R.layout.tools_widget)
            fun launch(action: String,request: Int)=PendingIntent.getActivity(context,request,Intent(context,MainActivity::class.java).putExtra("quick_action",action).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.widget_capture,launch("capture",4101))
            views.setOnClickPendingIntent(R.id.widget_draw,launch("draw",4102))
            views.setOnClickPendingIntent(R.id.widget_notes,PendingIntent.getActivity(context,4103,Intent(context,JournalActivity::class.java).putExtra("section","notes"),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            manager.updateAppWidget(id,views)
        }
    }
}
