package com.hamraj37.devpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.telemetry.BatteryTelemetry
import com.hamraj37.devpulse.data.telemetry.MemoryTelemetry
import java.util.Locale

class DevPulseStorageWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            try {
                updateWidget(context, appWidgetManager, id)
            } catch (_: Throwable) {}
        }
        WidgetUpdateManager.startWidgetUpdateLoop(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateManager.startWidgetUpdateLoop(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        if (!WidgetUpdateManager.hasActiveWidgets(context)) {
            WidgetUpdateManager.stopWidgetUpdateLoop(context)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_storage_battery)

                val activeColor = ContextCompat.getColor(context, R.color.widget_active_green)
                val trackColor = ContextCompat.getColor(context, R.color.widget_progress_track)

                // Storage
                try {
                    val memInfo = MemoryTelemetry.getMemoryInfo(context)
                    val storageTotal = memInfo.internalStorageTotalBytes.coerceAtLeast(1L)
                    val storageUsed = memInfo.internalStorageUsedBytes.coerceAtLeast(0L)
                    val storageFree = (storageTotal - storageUsed).coerceAtLeast(0L)
                    val storageFreeText = String.format(Locale.US, "%.2fG", storageFree / (1024.0 * 1024.0 * 1024.0))
                    val storageUsedPct = ((storageUsed.toFloat() / storageTotal.toFloat()) * 100).toInt().coerceIn(0, 100)

                    views.setTextViewText(R.id.widget_sb_storage_free, storageFreeText)
                    views.setProgressBar(R.id.widget_sb_storage_progress, 100, storageUsedPct, false)
                } catch (_: Throwable) {}

                // Battery
                try {
                    val battery = BatteryTelemetry.getBatteryInfo(context)
                    val batteryLevel = battery.levelPercent.coerceIn(0, 100)
                    drawBatteryRing(context, batteryLevel, activeColor, trackColor)?.let {
                        views.setImageViewBitmap(R.id.widget_sb_battery_ring, it)
                    }
                    views.setTextViewText(R.id.widget_sb_battery_pct, "$batteryLevel%")

                    // Temp & Brand
                    val brandIconRes = when {
                        Build.MANUFACTURER.contains("oneplus", ignoreCase = true) -> R.drawable.ic_brand_oneplus
                        Build.MANUFACTURER.contains("samsung", ignoreCase = true) -> R.drawable.ic_brand_samsung
                        Build.MANUFACTURER.contains("google", ignoreCase = true) -> R.drawable.ic_brand_google
                        Build.MANUFACTURER.contains("xiaomi", ignoreCase = true) -> R.drawable.ic_brand_xiaomi
                        else -> R.drawable.ic_brand_android
                    }
                    views.setImageViewResource(R.id.widget_sb_brand_icon, brandIconRes)
                    views.setTextViewText(R.id.widget_sb_temp_text, String.format(Locale.US, "%.1f ℃", battery.temperatureCelsius))
                } catch (_: Throwable) {}

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (_: Throwable) {}
        }

        private fun drawBatteryRing(context: Context, levelPercent: Int, activeColor: Int, trackColor: Int): Bitmap? {
            return try {
                val density = context.resources.displayMetrics.density
                val size = (30 * density).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                val strokeWidth = 3.5f * density
                val rect = RectF(strokeWidth / 2, strokeWidth / 2, size - strokeWidth / 2, size - strokeWidth / 2)

                val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth
                    color = trackColor
                }
                canvas.drawOval(rect, trackPaint)

                val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth
                    strokeCap = Paint.Cap.ROUND
                    color = activeColor
                }
                val sweepAngle = 360f * (levelPercent / 100f)
                canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

                val iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_widget_battery_icon)
                iconDrawable?.let {
                    val iconSize = (size * 0.45f).toInt()
                    val left = (size - iconSize) / 2
                    val top = (size - iconSize) / 2
                    it.setBounds(left, top, left + iconSize, top + iconSize)
                    it.setTint(activeColor)
                    it.draw(canvas)
                }

                bitmap
            } catch (_: Throwable) {
                null
            }
        }
    }
}
