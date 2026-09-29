package com.hamraj37.devpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.telemetry.BatteryTelemetry
import com.hamraj37.devpulse.data.telemetry.MemoryTelemetry
import java.util.Locale

class DevPulseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_devpulse)

            val memInfo = MemoryTelemetry.getMemoryInfo(context)
            val ramUsed = memInfo.ramUsedBytes
            val ramTotal = memInfo.ramTotalBytes
            val ramUsedGb = String.format(Locale.US, "%.2fGB", ramUsed / (1024.0 * 1024.0 * 1024.0))
            val ramPct = if (ramTotal > 0) ((ramUsed.toFloat() / ramTotal.toFloat()) * 100).toInt() else 68

            val storageUsed = memInfo.internalStorageUsedBytes
            val storageTotal = memInfo.internalStorageTotalBytes
            val storageUsedGb = String.format(Locale.US, "%.2fGB", storageUsed / (1024.0 * 1024.0 * 1024.0))
            val storagePct = if (storageTotal > 0) ((storageUsed.toFloat() / storageTotal.toFloat()) * 100).toInt() else 90

            val batteryInfo = BatteryTelemetry.getBatteryInfo(context)
            val batteryLevel = batteryInfo.levelPercent

            views.setTextViewText(R.id.widget_ram_text, "$ramUsedGb ($ramPct%)")
            views.setTextViewText(R.id.widget_storage_text, "$storageUsedGb ($storagePct%)")
            views.setTextViewText(R.id.widget_battery_text, "⚡ $batteryLevel%")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
