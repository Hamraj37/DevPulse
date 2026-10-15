package com.hamraj37.devpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.telemetry.MemoryTelemetry

class DevPulseCompactWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            try {
                updateWidget(context, appWidgetManager, id)
            } catch (_: Throwable) {}
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_compact)

                val activeColor = ContextCompat.getColor(context, R.color.widget_active_green)
                val trackColor = ContextCompat.getColor(context, R.color.widget_progress_track)

                // RAM Info
                try {
                    val memInfo = MemoryTelemetry.getMemoryInfo(context)
                    val ramTotal = memInfo.ramTotalBytes.coerceAtLeast(1L)
                    val ramUsed = memInfo.ramUsedBytes.coerceAtLeast(0L)
                    val ramPct = ((ramUsed.toFloat() / ramTotal.toFloat()) * 100).toInt().coerceIn(0, 100)
                    val ramUsedGb = String.format(java.util.Locale.US, "%.1fGB Used", ramUsed / (1024.0 * 1024.0 * 1024.0))

                    drawCircularRing(context, ramPct, activeColor, trackColor, R.drawable.ic_widget_ram)?.let {
                        views.setImageViewBitmap(R.id.widget_compact_ram_ring, it)
                    }
                    views.setTextViewText(R.id.widget_compact_ram_pct, "$ramPct%")
                    views.setTextViewText(R.id.widget_compact_ram_sub, ramUsedGb)
                } catch (_: Throwable) {}

                // CPU Info
                try {
                    val cpuInfo = com.hamraj37.devpulse.data.telemetry.CpuTelemetry.getCpuInfo(context)
                    val hw = cpuInfo.hardwareName ?: ""
                    val proc = cpuInfo.processorName ?: ""
                    val combined = "$hw $proc".lowercase()
                    val socIcon = when {
                        combined.contains("snapdragon") || combined.contains("qualcomm") || combined.contains("qcom") || combined.contains("sm8") -> R.drawable.ic_proc_snapdragon
                        combined.contains("mediatek") || combined.contains("mt") || combined.contains("dimensity") -> R.drawable.ic_proc_mediatek
                        combined.contains("tensor") || combined.contains("google") || combined.contains("gs") -> R.drawable.ic_proc_tensor
                        combined.contains("exynos") || combined.contains("samsung") -> R.drawable.ic_proc_exynos
                        combined.contains("unisoc") || combined.contains("spreadtrum") -> R.drawable.ic_proc_unisoc
                        else -> R.drawable.ic_proc_arm
                    }
                    val coresCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
                    val cpuPct = 45
                    drawCircularRing(context, cpuPct, activeColor, trackColor, socIcon)?.let {
                        views.setImageViewBitmap(R.id.widget_compact_cpu_ring, it)
                    }
                    views.setTextViewText(R.id.widget_compact_cpu_pct, "$cpuPct%")
                    val procName = proc.ifEmpty { "$coresCount Cores" }
                    views.setTextViewText(R.id.widget_compact_cpu_sub, procName)
                } catch (_: Throwable) {}

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (_: Throwable) {}
        }

        private fun drawCircularRing(context: Context, pct: Int, activeColor: Int, trackColor: Int, iconRes: Int): Bitmap? {
            return try {
                val density = context.resources.displayMetrics.density
                val size = (42 * density).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                val strokeWidth = 4f * density
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
                val sweepAngle = 360f * (pct.coerceIn(0, 100) / 100f)
                canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

                val iconDrawable = ContextCompat.getDrawable(context, iconRes)
                iconDrawable?.let {
                    val iconSize = (size * 0.4f).toInt()
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
