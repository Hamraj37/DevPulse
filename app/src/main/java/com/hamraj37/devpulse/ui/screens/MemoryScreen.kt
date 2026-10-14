package com.hamraj37.devpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.MemoryInfo
import com.hamraj37.devpulse.data.model.StoragePartitionInfo
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import java.util.Locale

@Composable
fun MemoryScreen(
    memoryInfo: MemoryInfo,
    modifier: Modifier = Modifier,
) {
    val ramTotalGb = memoryInfo.ramTotalBytes / (1024 * 1024 * 1024f)
    val ramUsedGb = memoryInfo.ramUsedBytes / (1024 * 1024 * 1024f)
    val ramFreeGb = memoryInfo.ramAvailableBytes / (1024 * 1024 * 1024f)
    val ramPct = if (ramTotalGb > 0) ((ramUsedGb / ramTotalGb) * 100).toInt() else 45

    val sysTotalGb = memoryInfo.systemStorageTotalBytes / (1024 * 1024 * 1024f)
    val sysUsedGb = memoryInfo.systemStorageUsedBytes / (1024 * 1024 * 1024f)
    val sysFreeGb = memoryInfo.systemStorageFreeBytes / (1024 * 1024 * 1024f)
    val sysPct = if (sysTotalGb > 0) ((sysUsedGb / sysTotalGb) * 100).toInt() else 56

    val intTotalGb = memoryInfo.internalStorageTotalBytes / (1024 * 1024 * 1024f)
    val intUsedGb = memoryInfo.internalStorageUsedBytes / (1024 * 1024 * 1024f)
    val intFreeGb = memoryInfo.internalStorageFreeBytes / (1024 * 1024 * 1024f)
    val intPct = if (intTotalGb > 0) ((intUsedGb / intTotalGb) * 100).toInt() else 44

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. RAM Card
        MemoryStorageProgressCard(
            title = stringResource(R.string.tab_memory),
            path = stringResource(R.string.memory_ram_volatile),
            icon = Icons.Rounded.Memory,
            usedGb = ramUsedGb,
            freeGb = ramFreeGb,
            totalGb = ramTotalGb,
            pct = ramPct
        )

        // 2. zRAM Card
        ZramProgressCard(memoryInfo = memoryInfo)

        // 3. System Storage Card
        MemoryStorageProgressCard(
            title = stringResource(R.string.memory_system_storage),
            path = "/system",
            fileSystemType = memoryInfo.systemFileSystemType,
            icon = Icons.Rounded.Folder,
            usedGb = sysUsedGb,
            freeGb = sysFreeGb,
            totalGb = sysTotalGb,
            pct = sysPct
        )

        // 4. Internal Storage Card
        MemoryStorageProgressCard(
            title = stringResource(R.string.dashboard_internal_storage),
            path = "/data",
            fileSystemType = memoryInfo.internalFileSystemType,
            icon = Icons.Rounded.SdStorage,
            usedGb = intUsedGb,
            freeGb = intFreeGb,
            totalGb = intTotalGb,
            pct = intPct
        )

        // 5. Partition Types & Details Card
        if (memoryInfo.storagePartitions.isNotEmpty() || memoryInfo.allStoragePartitions.isNotEmpty()) {
            StoragePartitionsCard(memoryInfo = memoryInfo)
        }
    }
}

@Composable
fun MemoryStorageProgressCard(
    title: String,
    path: String,
    icon: ImageVector,
    usedGb: Float,
    freeGb: Float,
    totalGb: Float,
    pct: Int,
    fileSystemType: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (!fileSystemType.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = fileSystemType.uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        Text(
                            text = path,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.fmt_pct, pct),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            val safeMemProgress = (pct / 100f).let { if (it.isNaN() || it.isInfinite()) 0f else it.coerceIn(0f, 1f) }
            LinearProgressIndicator(
                progress = { safeMemProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val usedText = stringResource(R.string.fmt_memory_used_of_total, usedGb, totalGb)
                val freeText = stringResource(R.string.fmt_memory_free, freeGb)
                Text(
                    text = usedText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = freeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ZramProgressCard(
    memoryInfo: MemoryInfo
) {
    val zramOrigGb = memoryInfo.zramOrigBytes / (1024 * 1024 * 1024f)
    val zramComprGb = memoryInfo.zramComprBytes / (1024 * 1024 * 1024f)
    val zramPct = if (memoryInfo.zramTotalBytes > 0) {
        ((memoryInfo.zramOrigBytes.toFloat() / memoryInfo.zramTotalBytes.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else 35
    val compressionRatio = if (memoryInfo.zramComprBytes > 0) {
        memoryInfo.zramOrigBytes.toFloat() / memoryInfo.zramComprBytes.toFloat()
    } else 2.3f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.memory_zram_swap),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.memory_compression_saved, String.format(Locale.US, "%.1f", compressionRatio)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.fmt_pct, zramPct),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            val safeZramProgress = (zramPct / 100f).let { if (it.isNaN() || it.isInfinite()) 0f else it.coerceIn(0f, 1f) }
            LinearProgressIndicator(
                progress = { safeZramProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.memory_gb_uncompressed, String.format(Locale.US, "%.2f", zramOrigGb)),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.memory_gb_in_ram, String.format(Locale.US, "%.2f", zramComprGb)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StoragePartitionsCard(
    memoryInfo: MemoryInfo
) {
    var showAllPartitions by remember { mutableStateOf(false) }
    val displayPartitions = if (showAllPartitions && memoryInfo.allStoragePartitions.isNotEmpty()) {
        memoryInfo.allStoragePartitions
    } else {
        memoryInfo.storagePartitions
    }

    val totalAllCount = if (memoryInfo.allStoragePartitions.isNotEmpty()) memoryInfo.allStoragePartitions.size else memoryInfo.storagePartitions.size
    val totalMainCount = memoryInfo.storagePartitions.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.memory_storage_partitions),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (totalAllCount > totalMainCount) {
                    Surface(
                        onClick = { showAllPartitions = !showAllPartitions },
                        shape = RoundedCornerShape(10.dp),
                        color = if (showAllPartitions) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        val buttonText = if (showAllPartitions) {
                            stringResource(R.string.memory_partition_show_main, totalMainCount)
                        } else {
                            stringResource(R.string.memory_partition_show_all, totalAllCount)
                        }
                        Text(
                            text = buttonText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (showAllPartitions) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.memory_partition_count, displayPartitions.size),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            displayPartitions.forEachIndexed { index, partition ->
                StoragePartitionRow(partition = partition)
                if (index < displayPartitions.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun StoragePartitionRow(
    partition: StoragePartitionInfo
) {
    val totalGb = partition.totalBytes / (1024 * 1024 * 1024f)
    val usedGb = partition.usedBytes / (1024 * 1024 * 1024f)
    val freeGb = partition.freeBytes / (1024 * 1024 * 1024f)
    val pct = if (totalGb > 0) ((usedGb / totalGb) * 100).toInt().coerceIn(0, 100) else 0

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = partition.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.memory_partition_type_chip, partition.fileSystemType.uppercase()),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                val accessText = if (partition.isReadOnly) {
                    stringResource(R.string.memory_partition_ro)
                } else {
                    stringResource(R.string.memory_partition_rw)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (partition.isReadOnly) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = accessText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (partition.isReadOnly) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.memory_partition_mount, partition.mountPoint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (partition.devicePath.isNotBlank() && (partition.devicePath != partition.mountPoint)) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.memory_partition_device, partition.devicePath),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }

        if (totalGb > 0) {
            val safeProgress = (pct / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { safeProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.fmt_memory_used_of_total, usedGb, totalGb),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.fmt_memory_free, freeGb),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemoryScreenPreview() {
    DevPulseTheme {
        MemoryScreen(memoryInfo = MemoryInfo())
    }
}
