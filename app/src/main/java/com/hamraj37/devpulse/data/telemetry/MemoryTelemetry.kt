package com.hamraj37.devpulse.data.telemetry

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.hamraj37.devpulse.data.model.MemoryInfo
import com.hamraj37.devpulse.data.model.StoragePartitionInfo
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
            val zramStats = getZramInfo()

            val (importantPartitions, allPartitions) = getAllAndImportantStoragePartitions()
            val dataPartition = importantPartitions.firstOrNull { it.mountPoint == "/data" }
            val internalFs = dataPartition?.fileSystemType ?: "f2fs"

            val sysPartition = importantPartitions.firstOrNull { (it.mountPoint == "/system") || (it.mountPoint == "/") }
            val systemFs = sysPartition?.fileSystemType ?: "erofs"

            MemoryInfo(
                ramTotalBytes = ramTotal,
                ramUsedBytes = ramUsed,
                ramAvailableBytes = ramAvail,
                ramThresholdBytes = memInfo.threshold,
                isLowMemory = memInfo.lowMemory,
                swapTotalBytes = swapStats.first,
                swapUsedBytes = swapStats.second,
                zramTotalBytes = zramStats.first,
                zramUsedBytes = zramStats.third,
                zramOrigBytes = zramStats.second,
                zramComprBytes = zramStats.third,
                systemStorageTotalBytes = systemStorageStats.first,
                systemStorageUsedBytes = (systemStorageStats.first - systemStorageStats.second).coerceAtLeast(0L),
                systemStorageFreeBytes = systemStorageStats.second,
                systemFileSystemType = systemFs,
                internalStorageTotalBytes = internalStorageStats.first,
                internalStorageUsedBytes = (internalStorageStats.first - internalStorageStats.second).coerceAtLeast(0L),
                internalStorageFreeBytes = internalStorageStats.second,
                internalFileSystemType = internalFs,
                storagePartitions = importantPartitions,
                allStoragePartitions = allPartitions,
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

    private fun getZramInfo(): Triple<Long, Long, Long> {
        var diskSize = 0L
        var origSize = 0L
        var comprSize = 0L

        try {
            val zramDir = File("/sys/block/zram0")
            if (zramDir.exists()) {
                diskSize = File(zramDir, "disksize").takeIf { it.canRead() }?.readText()?.trim()?.toLongOrNull() ?: 0L
                origSize = File(zramDir, "orig_data_size").takeIf { it.canRead() }?.readText()?.trim()?.toLongOrNull() ?: 0L
                comprSize = File(zramDir, "compr_data_size").takeIf { it.canRead() }?.readText()?.trim()?.toLongOrNull() ?: 0L

                val mmStatFile = File(zramDir, "mm_stat")
                if (mmStatFile.exists() && mmStatFile.canRead()) {
                    val content = mmStatFile.readText().trim()
                    val parts = content.split("\\s+".toRegex())
                    if (parts.size >= 2) {
                        origSize = parts[0].toLongOrNull() ?: origSize
                        comprSize = parts[1].toLongOrNull() ?: comprSize
                    }
                }
            }
        } catch (_: Throwable) {}

        val swap = getSwapInfoFromMemInfo()
        val total = if (diskSize > 0) diskSize else if (swap.first > 0) swap.first else 3L * 1024 * 1024 * 1024
        val orig = if (origSize > 0) origSize else if (swap.second > 0) swap.second else 1L * 1024 * 1024 * 1024
        val compr = if (comprSize > 0) comprSize else (orig / 2.3f).toLong().coerceAtLeast(1L)

        return Triple(total, orig, compr)
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

    private fun getAllAndImportantStoragePartitions(): Pair<List<StoragePartitionInfo>, List<StoragePartitionInfo>> {
        val importantPartitions = mutableListOf<StoragePartitionInfo>()
        val allPartitions = mutableListOf<StoragePartitionInfo>()
        val mountsFile = File("/proc/mounts")

        if (!mountsFile.exists() || !mountsFile.canRead()) {
            val defaults = MemoryInfo().storagePartitions
            return Pair(defaults, defaults)
        }

        val seenImportant = mutableSetOf<String>()
        val seenAll = mutableSetOf<String>()

        val importantMounts = setOf(
            "/data", "/system", "/vendor", "/product",
            "/system_ext", "/odm", "/metadata", "/storage/emulated"
        )

        val virtualFsTypes = setOf(
            "proc", "sysfs", "cgroup", "cgroup2", "debugfs",
            "tracefs", "bpf", "devpts", "selinuxfs", "binder", "pstore",
            "functionfs", "configfs"
        )

        try {
            mountsFile.useLines { lines ->
                for (line in lines) {
                    val parts = line.trim().split("\\s+".toRegex())
                    if (parts.size >= 4) {
                        val device = parts[0]
                        val mountPoint = parts[1]
                        val fsType = parts[2]
                        val options = parts[3].split(",")
                        val isRo = options.contains("ro")

                        if (virtualFsTypes.contains(fsType)) {
                            continue
                        }

                        val file = File(mountPoint)
                        val (total, free) = getStorageStats(file)
                        val used = (total - free).coerceAtLeast(0L)

                        val isRemovableSd = mountPoint.startsWith("/storage/") &&
                                mountPoint != "/storage/emulated" &&
                                mountPoint.split("/").size == 3 &&
                                !mountPoint.contains("self")

                        val isRootSystem = mountPoint == "/" && !seenImportant.contains("/system")

                        val friendlyName = when {
                            mountPoint == "/data" -> "Internal Data (/data)"
                            mountPoint == "/system" -> "System Partition (/system)"
                            mountPoint == "/vendor" -> "Vendor Partition (/vendor)"
                            mountPoint == "/product" -> "Product Partition (/product)"
                            mountPoint == "/system_ext" -> "System Extensions (/system_ext)"
                            mountPoint == "/odm" -> "ODM Partition (/odm)"
                            mountPoint == "/metadata" -> "Metadata Partition (/metadata)"
                            mountPoint == "/storage/emulated" -> "Emulated Storage (/storage)"
                            mountPoint == "/" -> "Root System (/)"
                            isRemovableSd -> "External SD Card ($mountPoint)"
                            else -> "$mountPoint Partition"
                        }

                        val partitionInfo = StoragePartitionInfo(
                            name = friendlyName,
                            mountPoint = mountPoint,
                            fileSystemType = fsType,
                            devicePath = device,
                            isReadOnly = isRo,
                            totalBytes = total,
                            usedBytes = used,
                            freeBytes = free
                        )

                        if (!seenAll.contains(mountPoint)) {
                            seenAll.add(mountPoint)
                            allPartitions.add(partitionInfo)
                        }

                        val isImportantNoisyCheck = !mountPoint.startsWith("/apex") &&
                                !mountPoint.startsWith("/mnt/pass_through") &&
                                !mountPoint.startsWith("/mnt/runtime") &&
                                !mountPoint.startsWith("/mnt/user") &&
                                !mountPoint.startsWith("/mnt/installer") &&
                                !mountPoint.startsWith("/storage/self") &&
                                !mountPoint.startsWith("/storage/emulated/")

                        val isImportant = (importantMounts.contains(mountPoint) || isRemovableSd || isRootSystem) && isImportantNoisyCheck

                        if (isImportant && !seenImportant.contains(mountPoint)) {
                            seenImportant.add(mountPoint)
                            importantPartitions.add(partitionInfo)
                        }
                    }
                }
            }
        } catch (_: Throwable) { }

        if (seenImportant.contains("/system")) {
            importantPartitions.removeAll { it.mountPoint == "/" }
        }

        val finalImportant = importantPartitions.ifEmpty { MemoryInfo().storagePartitions }
        val finalAll = allPartitions.ifEmpty { finalImportant }

        return Pair(finalImportant, finalAll)
    }
}
