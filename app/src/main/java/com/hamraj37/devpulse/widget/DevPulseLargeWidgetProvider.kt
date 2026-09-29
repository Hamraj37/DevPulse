package com.hamraj37.devpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.telemetry.BatteryTelemetry
import com.hamraj37.devpulse.data.telemetry.MemoryTelemetry
import java.util.Locale

class DevPulseLargeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_large)
            val memInfo = MemoryTelemetry.getMemoryInfo(context)

            val storageUsed = memInfo.internalStorageUsedBytes
            val storageTotal = memInfo.internalStorageTotalBytes
            val storageUsedGb = String.format(Locale.US, "%.2fGB Used", storageUsed / (1024.0 * 1024.0 * 1024.0))
            val storagePct = if (storageTotal > 0) ((storageUsed.toFloat() / storageTotal.toFloat()) * 100).toInt() else 60

            val ramUsed = memInfo.ramUsedBytes
            val ramTotal = memInfo.ramTotalBytes
            val ramUsedGb = String.format(Locale.US, "%.2fGB Used", ramUsed / (1024.0 * 1024.0 * 1024.0))
            val ramPct = if (ramTotal > 0) ((ramUsed.toFloat() / ramTotal.toFloat()) * 100).toInt() else 75

            val battery = BatteryTelemetry.getBatteryInfo(context)
            val temp = battery.temperatureCelsius

            views.setTextViewText(R.id.widget_large_storage_text, storageUsedGb)
            views.setProgressBar(R.id.widget_large_storage_progress, 100, storagePct, false)

            views.setTextViewText(R.id.widget_large_ram_text, ramUsedGb)
            views.setProgressBar(R.id.widget_large_ram_progress, 100, ramPct, false)

            views.setTextViewText(R.id.widget_large_temp_text, String.format(Locale.US, "Temp: %.1f ℃", temp))
            views.setTextViewText(R.id.widget_large_battery_text, "Battery: ${battery.levelPercent}%")

            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
