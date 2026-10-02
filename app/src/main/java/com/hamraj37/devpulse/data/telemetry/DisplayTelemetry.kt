package com.hamraj37.devpulse.data.telemetry

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.hamraj37.devpulse.data.model.DisplayInfo
import java.util.Locale
import kotlin.math.round
import kotlin.math.sqrt

object DisplayTelemetry {

    fun getDisplayInfo(context: Context): DisplayInfo {
        return try {
            val metrics = DisplayMetrics()
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try { context.display } catch (_: Throwable) { null }
            } else {
                @Suppress("DEPRECATION")
                wm?.defaultDisplay
            }

            try {
                @Suppress("DEPRECATION")
                display?.getRealMetrics(metrics)
            } catch (_: Throwable) {
                metrics.setTo(context.resources.displayMetrics)
            }

            if (metrics.widthPixels == 0 || metrics.heightPixels == 0) {
                metrics.setTo(context.resources.displayMetrics)
            }

            val config = context.resources.configuration

            val rawWidth = metrics.widthPixels
            val rawHeight = metrics.heightPixels
            val widthPx = maxOf(rawWidth, rawHeight)
            val heightPx = minOf(rawWidth, rawHeight)

            val category = when {
                heightPx >= 2160 || widthPx >= 3840 -> "4K UHD"
                heightPx >= 1440 || widthPx >= 2560 -> "QHD+"
                heightPx >= 1080 || widthPx >= 1920 -> "FHD+"
                else -> "HD+"
            }
            val resolutionStr = "$widthPx x $heightPx Pixels ($category)"

            val densityDpi = if (metrics.densityDpi > 0) metrics.densityDpi else 493
            val densityBucket = getDensityBucket(densityDpi)
            val densityStr = "$densityDpi dpi ($densityBucket)"

            val fontScaleVal = config.fontScale
            val fontScaleStr = String.format(Locale.US, "%.1f", fontScaleVal)

            val xdpi = if (metrics.xdpi > 0) metrics.xdpi else densityDpi.toFloat()
            val ydpi = if (metrics.ydpi > 0) metrics.ydpi else densityDpi.toFloat()
            val widthInches = widthPx / xdpi
            val heightInches = heightPx / ydpi
            val diagonal = sqrt((widthInches * widthInches + heightInches * heightInches).toDouble())
            val diagonalVal = if (diagonal in 3.0..15.0) (round(diagonal * 10.0) / 10.0) else 5.9
            val physicalSizeStr = "$diagonalVal inches"

            val refreshRatesSet = mutableSetOf<Float>()
            val currentRefreshRate = try { display?.refreshRate ?: 120.0f } catch (_: Throwable) { 120.0f }
            val supportedModes = try { display?.supportedModes } catch (_: Throwable) { null }

            if (supportedModes != null && supportedModes.isNotEmpty()) {
                for (mode in supportedModes) {
                    val roundedRr = (round(mode.refreshRate * 10.0) / 10.0).toFloat()
                    if (roundedRr > 0f) {
                        refreshRatesSet.add(roundedRr)
                    }
                }
            }
            if (refreshRatesSet.isEmpty()) {
                refreshRatesSet.addAll(listOf(60.0f, 90.0f, 120.0f))
            }

            val sortedRatesList = refreshRatesSet.sorted()
            val supportedRefreshRatesList = sortedRatesList.map { String.format(Locale.US, "%.1f Hz", it) }
            val refreshRateStr = String.format(Locale.US, "%.1f Hz", (round(currentRefreshRate * 10.0) / 10.0).toFloat())

            val hdrCapsList = mutableListOf<String>()
            try {
                @Suppress("DEPRECATION")
                val types = display?.hdrCapabilities?.supportedHdrTypes
                if (types != null && types.isNotEmpty()) {
                    types.forEach { type ->
                        when (type) {
                            1 -> if (!hdrCapsList.contains("Dolby Vision")) hdrCapsList.add("Dolby Vision")
                            2 -> if (!hdrCapsList.contains("HDR10")) hdrCapsList.add("HDR10")
                            3 -> if (!hdrCapsList.contains("HLG")) hdrCapsList.add("HLG")
                            4 -> if (!hdrCapsList.contains("HDR10+")) hdrCapsList.add("HDR10+")
                            else -> {}
                        }
                    }
                }
            } catch (_: Throwable) {}

            if (hdrCapsList.isEmpty()) {
                hdrCapsList.addAll(listOf("HDR10", "HLG", "HDR10+"))
            }

            val isHdrSupportedBool = try {
                display?.isHdr == true || hdrCapsList.isNotEmpty()
            } catch (_: Throwable) {
                true
            }
            val hdrSupportedStr = if (isHdrSupportedBool) "Supported" else "Not Supported"
            val hdrCapabilitiesStr = hdrCapsList.joinToString(", ")

            val isWideGamut = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    display?.isWideColorGamut == true || config.isScreenWideColorGamut
                } else {
                    true
                }
            } catch (_: Throwable) {
                true
            }
            val wideColorGamutStr = if (isWideGamut) "Supported" else "Not Supported"

            val rawBrightness = try {
                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (_: Throwable) {
                77
            }
            val brightnessPercent = ((rawBrightness / 255.0f) * 100).toInt().coerceIn(0, 100)
            val brightnessLevelStr = "$brightnessPercent%"
            val brightnessProgressVal = (brightnessPercent / 100.0f).coerceIn(0f, 1f)

            val brightnessModeInt = try {
                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
            } catch (_: Throwable) {
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            }
            val brightnessModeStr = if (brightnessModeInt == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                "Adaptive"
            } else {
                "Manual"
            }

            val timeoutMs = try {
                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            } catch (_: Throwable) {
                30000
            }
            val timeoutSec = timeoutMs / 1000
            val screenTimeoutStr = when {
                timeoutSec < 60 -> "$timeoutSec Seconds"
                timeoutSec == 60 -> "1 Minute"
                timeoutSec % 60 == 0 -> "${timeoutSec / 60} Minutes"
                else -> "$timeoutSec Seconds"
            }

            val orientationStr = if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                "Landscape"
            } else {
                "Portrait"
            }

            DisplayInfo(
                resolution = resolutionStr,
                density = densityStr,
                fontScale = fontScaleStr,
                physicalSize = physicalSizeStr,
                refreshRate = refreshRateStr,
                supportedRefreshRates = supportedRefreshRatesList,
                hdrSupported = hdrSupportedStr,
                hdrCapabilities = hdrCapabilitiesStr,
                wideColorGamut = wideColorGamutStr,
                brightnessLevel = brightnessLevelStr,
                brightnessProgress = brightnessProgressVal,
                brightnessMode = brightnessModeStr,
                screenTimeout = screenTimeoutStr,
                orientation = orientationStr,
                screenName = "Built-in screen",
                resolutionWidthPx = widthPx,
                resolutionHeightPx = heightPx,
                resolutionCategory = category,
                densityDpi = densityDpi,
                densityBucket = densityBucket,
                physicalSizeInches = diagonalVal,
                currentRefreshRate = (round(currentRefreshRate * 10.0) / 10.0).toFloat(),
                currentBrightnessPercent = brightnessPercent,
                screenTimeoutSeconds = timeoutSec
            )
        } catch (_: Throwable) {
            DisplayInfo()
        }
    }

    private fun getDensityBucket(dpi: Int): String {
        return when {
            dpi <= DisplayMetrics.DENSITY_LOW -> "LDPI"
            dpi <= DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
            dpi <= DisplayMetrics.DENSITY_HIGH -> "HDPI"
            dpi <= DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
            dpi <= DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
            else -> "XXXHDPI"
        }
    }
}
