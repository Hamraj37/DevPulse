package com.hamraj37.devpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamraj37.devpulse.data.model.MemoryInfo
import com.hamraj37.devpulse.ui.theme.DevPulseTheme

@Composable
fun MemoryScreen(
    memoryInfo: MemoryInfo,
    modifier: Modifier = Modifier
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
            title = "Memory",
            path = "RAM (Volatile)",
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
            title = "System Storage",
            path = "/system",
            icon = Icons.Rounded.Folder,
            usedGb = sysUsedGb,
            freeGb = sysFreeGb,
            totalGb = sysTotalGb,
            pct = sysPct
        )

        // 4. Internal Storage Card
        MemoryStorageProgressCard(
            title = "Internal Storage",
            path = "/data",
            icon = Icons.Rounded.SdStorage,
            usedGb = intUsedGb,
            freeGb = intFreeGb,
            totalGb = intTotalGb,
            pct = intPct
        )
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
    pct: Int
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
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = path,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$pct%",
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format("%.2f", usedGb)} GB of ${String.format("%.2f", totalGb)} GB Used",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${String.format("%.2f", freeGb)} GB Free",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ZramProgressCard(
    memoryInfo: MemoryInfo
) {
    val zramTotalGb = memoryInfo.zramTotalBytes / (1024 * 1024 * 1024f)
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
                        contentDescription = "zRAM",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "zRAM (RAM Swap)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = String.format(Locale.US, "%.1fx Compression Saved", compressionRatio),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$zramPct%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            val safeProgress = (zramPct / 100f).let { if (it.isNaN() || it.isInfinite()) 0f else it.coerceIn(0f, 1f) }
            LinearProgressIndicator(
                progress = { safeProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(Locale.US, "%.2f GB Swapped (%.2f GB in RAM)", zramOrigGb, zramComprGb),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = String.format(Locale.US, "Total: %.2f GB", zramTotalGb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemoryScreenPreview() {
    DevPulseTheme {
        MemoryScreen(
            memoryInfo = MemoryInfo()
        )
    }
}
