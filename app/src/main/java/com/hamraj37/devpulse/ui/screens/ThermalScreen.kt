package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.data.model.ThermalInfo
import com.hamraj37.devpulse.data.model.ThermalZone
import com.hamraj37.devpulse.ui.components.LiveSparklineChart
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import com.hamraj37.devpulse.ui.theme.ActiveBadge

import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.LinearProgressIndicator

@Composable
fun ThermalScreen(
    thermalInfo: ThermalInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Thermal Throttling & Headroom Card (Moved to top)
        ThermalThrottlingCard(thermalInfo = thermalInfo)

        // 2. CPU Temperature & Thermal Overview Banner (Dashboard card style with LiveSparklineChart)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            val cpuZone = thermalInfo.thermalZones.firstOrNull { it.type.contains("cpu", ignoreCase = true) || it.name.contains("cpu", ignoreCase = true) }
            val cpuTemp = cpuZone?.tempCelsius ?: thermalInfo.thermalZones.firstOrNull()?.tempCelsius ?: 36.5f

            val highLabel = stringResource(R.string.thermal_label_high)
            val warmLabel = stringResource(R.string.thermal_label_warm)
            val normalLabel = stringResource(R.string.thermal_label_normal)

            val (statusColor, statusText) = when {
                cpuTemp >= 48f -> Color(0xFFE53935) to highLabel
                cpuTemp >= 40f -> Color(0xFFFB8C00) to warmLabel
                else -> ActiveBadge to normalLabel
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.cpu_temperature_title),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.fmt_battery_temp, cpuTemp),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Thermostat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Badges Row using FlowRow for robust responsiveness
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge (driven by CPU temp)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = statusColor
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    // Zones Count Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.fmt_thermal_zones_count, thermalInfo.thermalZones.size),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(16.dp))

                val cpuTempHistory = remember {
                    mutableStateListOf<Float>().apply {
                        for (i in 0..15) {
                            val delta = ((i * 37) % 7 - 3) * 0.1f
                            add(cpuTemp + delta)
                        }
                    }
                }

                LaunchedEffect(cpuTemp) {
                    if (cpuTempHistory.isEmpty() || cpuTempHistory.last() != cpuTemp) {
                        if (cpuTempHistory.size > 20) cpuTempHistory.removeAt(0)
                        cpuTempHistory.add(cpuTemp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live CPU Temperature Trend",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Real-time",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LiveSparklineChart(
                    history = cpuTempHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    lineColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 3. Grid of Thermal Cards (2 columns, equal width)
        val zones = thermalInfo.thermalZones
        val chunkedZones = zones.chunked(2)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            chunkedZones.forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { zone ->
                        androidx.compose.runtime.key(zone.name) {
                            Box(modifier = Modifier.weight(1f)) {
                                ThermalCardItem(zone = zone)
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ThermalThrottlingCard(
    thermalInfo: ThermalInfo,
    modifier: Modifier = Modifier
) {
    val (statusLabel, statusColor) = when (thermalInfo.thermalStatusInt) {
        0 -> stringResource(R.string.thermal_throttling_none) to ActiveBadge
        1 -> stringResource(R.string.thermal_throttling_light) to Color(0xFFF59E0B)
        2 -> stringResource(R.string.thermal_throttling_moderate) to Color(0xFFFB8C00)
        3 -> stringResource(R.string.thermal_throttling_severe) to Color(0xFFE53935)
        4 -> stringResource(R.string.thermal_throttling_critical) to Color(0xFFD32F2F)
        else -> stringResource(R.string.thermal_throttling_emergency) to Color(0xFFB71C1C)
    }

    val headroomRatio = thermalInfo.thermalHeadroom.coerceIn(0f, 1f)
    val headroomPct = (headroomRatio * 100).toInt()

    val barColor = when {
        headroomRatio >= 0.95f || thermalInfo.thermalStatusInt >= 2 -> Color(0xFFE53935)
        headroomRatio >= 0.75f || thermalInfo.thermalStatusInt == 1 -> Color(0xFFFB8C00)
        else -> ActiveBadge
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = statusColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (thermalInfo.thermalStatusInt > 0) Icons.Rounded.Warning else Icons.Rounded.Speed,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.thermal_throttling_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.thermal_throttling_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Status Badge & Headroom percentage display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, CircleShape)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }

                Text(
                    text = "$headroomPct% Headroom",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Linear Progress Indicator for Headroom
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.thermal_headroom_capacity),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (headroomRatio >= 1.0f) "Threshold Reached" else "${100 - headroomPct}% Margin",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (headroomRatio >= 1.0f) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { headroomRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            // Info Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Performance Status Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Performance State",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (thermalInfo.thermalStatusInt == 0) "Optimal (Unthrottled)" else "Frequency Scaled",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (thermalInfo.thermalStatusInt == 0) ActiveBadge else Color(0xFFFB8C00)
                        )
                    }
                }

                // Overall System Thermal
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Overall System",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = thermalInfo.overallStatus,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThermalCardItem(
    zone: ThermalZone
) {
    val temp = zone.tempCelsius
    val highLabel = stringResource(R.string.thermal_label_high)
    val warmLabel = stringResource(R.string.thermal_label_warm)
    val normalLabel = stringResource(R.string.thermal_label_normal)

    val (tempColor, tempLabel) = when {
        temp >= 48f -> Color(0xFFE53935) to highLabel
        temp >= 40f -> Color(0xFFFB8C00) to warmLabel
        else -> ActiveBadge to normalLabel
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = tempColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Thermostat,
                            contentDescription = null,
                            tint = tempColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tempColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = tempLabel,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = tempColor,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = zone.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = zone.type,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.fmt_battery_temp, temp),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                ),
                color = tempColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThermalScreenPreview() {
    DevPulseTest()
}

@Composable
fun DevPulseTest() {
    DevPulseTheme {
        ThermalScreen(
            thermalInfo = ThermalInfo(
                overallStatus = "Normal",
                thermalHeadroom = 1.0f,
                thermalZones = listOf(
                    ThermalZone("CPU Thermal", 36.5f, "CPU"),
                    ThermalZone("GPU Thermal", 38.0f, "GPU"),
                    ThermalZone("Battery Temp", 32.5f, "Battery"),
                    ThermalZone("Modem", 42.0f, "Cellular")
                )
            )
        )
    }
}
