package com.hamraj37.devpulse.data.telemetry

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.hamraj37.devpulse.data.model.MemoryInfo
import java.io.File

object MemoryTelemetry {

    fun getMemoryInfo(context: Context): MemoryInfo {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            if (am != null) {
                try { am.getMemoryInfo(memInfo) } catch (_: Throwable) {}
            }

            val ramTotal = if (memInfo.totalMem > 0) memInfo.totalMem else 4L * 1024 * 1024 * 1024
            val ramAvail = memInfo.availMem
            val ramUsed = if (ramTotal > ramAvail) ramTotal - ramAvail else 0L

            val internalDataDir = try { Environment.getDataDirectory() } catch (_: Throwable) { File("/data") }
            val internalStorageStats = getStorageStats(internalDataDir)

            val rootDir = try { Environment.getRootDirectory() } catch (_: Throwable) { File("/system") }
            var systemStorageStats = getStorageStats(rootDir)
            if (systemStorageStats.first == 0L) {
                systemStorageStats = getStorageStats(File("/system"))
            }
            if (systemStorageStats.first == 0L) {
                systemStorageStats = getStorageStats(File("/"))
            }

            val swapStats = getSwapInfoFromMemInfo()

            MemoryInfo(
                ramTotalBytes = ramTotal,
                ramUsedBytes = ramUsed,
                ramAvailableBytes = ramAvail,
                ramThresholdBytes = memInfo.threshold,
                isLowMemory = memInfo.lowMemory,
                swapTotalBytes = swapStats.first,
                swapUsedBytes = swapStats.second,
                systemStorageTotalBytes = systemStorageStats.first,
                systemStorageUsedBytes = (systemStorageStats.first - systemStorageStats.second).coerceAtLeast(0L),
                systemStorageFreeBytes = systemStorageStats.second,
                internalStorageTotalBytes = internalStorageStats.first,
                internalStorageUsedBytes = (internalStorageStats.first - internalStorageStats.second).coerceAtLeast(0L),
                internalStorageFreeBytes = internalStorageStats.second,
            )
        } catch (_: Throwable) {
            MemoryInfo()
        }
    }

    private fun getStorageStats(dir: File): Pair<Long, Long> {
        return try {
            if (dir.exists()) {
                val stat = StatFs(dir.path)
                val total = stat.totalBytes
                val free = stat.availableBytes
                Pair(total, free)
            } else {
                Pair(0L, 0L)
            }
        } catch (_: Throwable) {
            Pair(0L, 0L)
        }
    }

    private fun getSwapInfoFromMemInfo(): Pair<Long, Long> {
        return try {
            val memInfoFile = File("/proc/meminfo")
            if (memInfoFile.exists()) {
                var swapTotalKb = 0L
                var swapFreeKb = 0L
                memInfoFile.useLines { lines ->
                    for (line in lines) {
                        if (line.startsWith("SwapTotal:")) {
                            swapTotalKb = parseKbValue(line)
                        } else if (line.startsWith("SwapFree:")) {
                            swapFreeKb = parseKbValue(line)
                        }
                    }
                }
                val swapTotalBytes = swapTotalKb * 1024L
                val swapFreeBytes = swapFreeKb * 1024L
                val swapUsedBytes = (swapTotalBytes - swapFreeBytes).coerceAtLeast(0L)
                Pair(swapTotalBytes, swapUsedBytes)
            } else {
                Pair(0L, 0L)
            }
        } catch (_: Throwable) {
            Pair(0L, 0L)
        }
    }

    private fun parseKbValue(line: String): Long {
        return try {
            val parts = line.split("\\s+".toRegex())
            if (parts.size >= 2) {
                parts[1].toLongOrNull() ?: 0L
            } else 0L
        } catch (_: Throwable) {
            0L
        }
    }
}
