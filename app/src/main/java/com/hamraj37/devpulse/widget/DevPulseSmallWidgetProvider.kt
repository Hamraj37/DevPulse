package com.hamraj37.devpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.telemetry.MemoryTelemetry

class DevPulseSmallWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_small)
            val memInfo = MemoryTelemetry.getMemoryInfo(context)
            val ramUsedGb = context.getString(R.string.widget_used_gb_short_format, memInfo.ramUsedBytes / (1024.0 * 1024.0 * 1024.0))

            views.setTextViewText(R.id.widget_small_title, context.getString(R.string.widget_lbl_ram_used))
            views.setTextViewText(R.id.widget_small_value, ramUsedGb)

            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
