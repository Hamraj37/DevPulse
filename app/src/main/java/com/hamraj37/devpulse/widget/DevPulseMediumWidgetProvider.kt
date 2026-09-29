package com.hamraj37.devpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.telemetry.MemoryTelemetry
import java.util.Locale

class DevPulseMediumWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_medium)
            val memInfo = MemoryTelemetry.getMemoryInfo(context)

            val storageUsed = memInfo.internalStorageUsedBytes
            val storageUsedGb = String.format(Locale.US, "%.2fGB Used", storageUsed / (1024.0 * 1024.0 * 1024.0))

            val ramUsed = memInfo.ramUsedBytes
            val ramUsedGb = String.format(Locale.US, "%.2fGB Used", ramUsed / (1024.0 * 1024.0 * 1024.0))

            views.setTextViewText(R.id.widget_medium_storage_text, storageUsedGb)
            views.setTextViewText(R.id.widget_medium_ram_text, ramUsedGb)

            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
