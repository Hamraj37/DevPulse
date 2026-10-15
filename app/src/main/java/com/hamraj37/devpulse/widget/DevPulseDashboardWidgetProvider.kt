package com.hamraj37.devpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.telemetry.BatteryTelemetry
import com.hamraj37.devpulse.data.telemetry.CpuTelemetry
import com.hamraj37.devpulse.data.telemetry.MemoryTelemetry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DevPulseDashboardWidgetProvider : AppWidgetProvider() {

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
                val views = RemoteViews(context.packageName, R.layout.widget_dashboard)

                val activeColor = ContextCompat.getColor(context, R.color.widget_active_green)
                val inactiveColor = ContextCompat.getColor(context, R.color.widget_inactive_grey)
                val trackColor = ContextCompat.getColor(context, R.color.widget_progress_track)

                // 1. Connectivity Statuses
                val (wifiActive, dataActive) = getNetworkStatus(context)
                val bluetoothActive = isBluetoothActive(context)
                val locationActive = isLocationActive(context)

                getTintedBitmap(context, R.drawable.ic_widget_wifi, if (wifiActive) activeColor else inactiveColor)?.let {
                    views.setImageViewBitmap(R.id.widget_wifi_icon, it)
                }
                getTintedBitmap(context, R.drawable.ic_widget_mobile_data, if (dataActive) activeColor else inactiveColor)?.let {
                    views.setImageViewBitmap(R.id.widget_data_icon, it)
                }
                getTintedBitmap(context, R.drawable.ic_widget_bluetooth, if (bluetoothActive) activeColor else inactiveColor)?.let {
                    views.setImageViewBitmap(R.id.widget_bluetooth_icon, it)
                }
                getTintedBitmap(context, R.drawable.ic_widget_location, if (locationActive) activeColor else inactiveColor)?.let {
                    views.setImageViewBitmap(R.id.widget_location_icon, it)
                }

                // 2. Date
                try {
                    val dateFormatted = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
                    views.setTextViewText(R.id.widget_date_text, dateFormatted)
                } catch (_: Throwable) {}

                // 3. Storage Free
                try {
                    val memInfo = MemoryTelemetry.getMemoryInfo(context)
                    val storageTotal = memInfo.internalStorageTotalBytes.coerceAtLeast(1L)
                    val storageUsed = memInfo.internalStorageUsedBytes.coerceAtLeast(0L)
                    val storageFree = (storageTotal - storageUsed).coerceAtLeast(0L)
                    val storageFreeGb = storageFree / (1024.0 * 1024.0 * 1024.0)
                    val storageFreeText = String.format(Locale.US, "%.2fG", storageFreeGb)
                    val storageUsedPct = ((storageUsed.toFloat() / storageTotal.toFloat()) * 100).toInt().coerceIn(0, 100)

                    views.setTextViewText(R.id.widget_storage_free_text, storageFreeText)
                    views.setProgressBar(R.id.widget_storage_progress, 100, storageUsedPct, false)
                } catch (_: Throwable) {}

                // 4. Processor / SoC Info
                try {
                    val cpuInfo = CpuTelemetry.getCpuInfo(context)
                    val (socIconRes, socModel) = parseSocDetails(cpuInfo.hardwareName, cpuInfo.processorName)
                    views.setImageViewResource(R.id.widget_soc_icon, socIconRes)
                    views.setTextViewText(R.id.widget_soc_model, socModel)
                } catch (_: Throwable) {}

                // 5. Battery Gauge & Ring
                try {
                    val battery = BatteryTelemetry.getBatteryInfo(context)
                    val batteryLevel = battery.levelPercent.coerceIn(0, 100)
                    drawBatteryRingBitmap(context, batteryLevel, activeColor, trackColor)?.let {
                        views.setImageViewBitmap(R.id.widget_battery_ring_img, it)
                    }
                    views.setTextViewText(R.id.widget_battery_pct_text, "$batteryLevel%")
                } catch (_: Throwable) {}

                // 6. Android Version & Logo
                try {
                    val sdkInt = Build.VERSION.SDK_INT
                    val androidLogoRes = when (sdkInt) {
                        Build.VERSION_CODES.Q -> R.drawable.android_10
                        Build.VERSION_CODES.R -> R.drawable.android_11
                        Build.VERSION_CODES.S, Build.VERSION_CODES.S_V2 -> R.drawable.android_12
                        Build.VERSION_CODES.TIRAMISU -> R.drawable.android_13
                        Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> R.drawable.android_14
                        Build.VERSION_CODES.VANILLA_ICE_CREAM -> R.drawable.android_15
                        36 -> R.drawable.android_16
                        else -> if (sdkInt >= 37) R.drawable.android_17 else R.drawable.ic_brand_android
                    }
                    views.setImageViewResource(R.id.widget_android_logo, androidLogoRes)
                    views.setTextViewText(R.id.widget_android_version, "Android ${Build.VERSION.RELEASE}")
                } catch (_: Throwable) {}

                // 7. Device Brand & Model
                try {
                    val (brandIconRes, modelName) = parseDeviceDetails()
                    views.setImageViewResource(R.id.widget_brand_icon, brandIconRes)
                    views.setTextViewText(R.id.widget_device_model, modelName)
                } catch (_: Throwable) {}

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (_: Throwable) {}
        }

        private fun getNetworkStatus(context: Context): Pair<Boolean, Boolean> {
            var wifi = false
            var data = false
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                if (cm != null) {
                    val activeNetwork = cm.activeNetwork
                    val caps = cm.getNetworkCapabilities(activeNetwork)
                    if (caps != null) {
                        wifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        data = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    }
                }
            } catch (_: Throwable) {}
            return Pair(wifi, data)
        }

        private fun isBluetoothActive(context: Context): Boolean {
            return try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                adapter?.isEnabled == true
            } catch (_: Throwable) {
                false
            }
        }

        private fun isLocationActive(context: Context): Boolean {
            return try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                        lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            } catch (_: Throwable) {
                false
            }
        }

        private fun getTintedBitmap(context: Context, resId: Int, color: Int): Bitmap? {
            return try {
                val drawable = ContextCompat.getDrawable(context, resId) ?: return null
                val density = context.resources.displayMetrics.density
                val size = (28 * density).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, size, size)
                drawable.setTint(color)
                drawable.draw(canvas)
                bitmap
            } catch (_: Throwable) {
                null
            }
        }

        private fun drawBatteryRingBitmap(context: Context, levelPercent: Int, ringColor: Int, trackColor: Int): Bitmap? {
            return try {
                val density = context.resources.displayMetrics.density
                val size = (32 * density).toInt().coerceAtLeast(1)
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
                    color = ringColor
                }
                val sweepAngle = 360f * (levelPercent / 100f)
                canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

                val iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_widget_battery_icon)
                iconDrawable?.let {
                    val iconSize = (size * 0.45f).toInt()
                    val left = (size - iconSize) / 2
                    val top = (size - iconSize) / 2
                    it.setBounds(left, top, left + iconSize, top + iconSize)
                    it.setTint(ringColor)
                    it.draw(canvas)
                }

                bitmap
            } catch (_: Throwable) {
                null
            }
        }

        private fun parseSocDetails(hardware: String, processorName: String): Pair<Int, String> {
            val combined = "$hardware $processorName".lowercase()

            val socIcon = when {
                combined.contains("snapdragon") || combined.contains("qualcomm") || combined.contains("qcom") || combined.contains("sm8") -> R.drawable.ic_proc_snapdragon
                combined.contains("mediatek") || combined.contains("mt") || combined.contains("dimensity") -> R.drawable.ic_proc_mediatek
                combined.contains("tensor") || combined.contains("google") || combined.contains("gs") -> R.drawable.ic_proc_tensor
                combined.contains("exynos") || combined.contains("samsung") -> R.drawable.ic_proc_exynos
                combined.contains("unisoc") || combined.contains("spreadtrum") -> R.drawable.ic_proc_unisoc
                else -> R.drawable.ic_proc_arm
            }

            val socModelName = processorName.ifBlank { hardware.ifBlank { "Processor" } }

            return Pair(socIcon, socModelName)
        }

        private fun parseDeviceDetails(): Pair<Int, String> {
            val brand = Build.MANUFACTURER.lowercase()
            val brandIcon = when {
                brand.contains("oneplus") -> R.drawable.ic_brand_oneplus
                brand.contains("samsung") -> R.drawable.ic_brand_samsung
                brand.contains("google") -> R.drawable.ic_brand_google
                brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("mi ") -> R.drawable.ic_brand_xiaomi
                brand.contains("realme") -> R.drawable.ic_brand_realme
                brand.contains("oppo") -> R.drawable.ic_brand_oppo
                brand.contains("vivo") || brand.contains("iqoo") -> R.drawable.ic_brand_vivo
                brand.contains("motorola") || brand.contains("moto") -> R.drawable.ic_brand_motorola
                brand.contains("nothing") -> R.drawable.ic_brand_nothing
                brand.contains("asus") -> R.drawable.ic_brand_asus
                brand.contains("sony") -> R.drawable.ic_brand_sony
                else -> R.drawable.ic_brand_android
            }

            val rawModel = Build.MODEL
            val modelName = when {
                rawModel.startsWith(Build.MANUFACTURER, ignoreCase = true) -> rawModel.substring(Build.MANUFACTURER.length).trim()
                else -> rawModel
            }

            return Pair(brandIcon, if (modelName.isNotBlank()) modelName else "Device")
        }
    }
}
