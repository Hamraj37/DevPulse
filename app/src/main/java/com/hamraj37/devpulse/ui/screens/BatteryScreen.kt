package com.hamraj37.devpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.BatteryInfo
import com.hamraj37.devpulse.ui.components.LiveSparklineChart
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import com.hamraj37.devpulse.ui.theme.ActiveBadge

@Composable
fun BatteryScreen(
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Banner Card (Battery Gauge Icon, Sparkline & Quick Specs)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.battery_power_state),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = batteryInfo.usbStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = ActiveBadge,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.fmt_pct, batteryInfo.levelPercent),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BatteryMetricChip(stringResource(R.string.battery_current), stringResource(R.string.fmt_ma, batteryInfo.currentMa))
                    BatteryMetricChip(stringResource(R.string.battery_temp_chip), stringResource(R.string.fmt_battery_temp, batteryInfo.temperatureCelsius))
                    BatteryMetricChip(stringResource(R.string.battery_power), stringResource(R.string.fmt_watts, batteryInfo.powerWatts))
                    BatteryMetricChip(stringResource(R.string.battery_health), stringResource(R.string.fmt_pct, batteryInfo.healthPercent))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.battery_live_power_draw),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                LiveSparklineChart(
                    history = batteryInfo.powerHistory.ifEmpty { listOf(1.8f, 1.9f, 1.85f, 1.92f, 1.88f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    lineColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 2. Battery Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.battery_health_specs),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = dividerColor)

                val batterySpecs = listOf(
                    stringResource(R.string.battery_health) to stringResource(R.string.fmt_battery_health_pct, batteryInfo.health, batteryInfo.healthPercent),
                    stringResource(R.string.battery_level) to stringResource(R.string.fmt_pct, batteryInfo.levelPercent),
                    stringResource(R.string.battery_status) to batteryInfo.status,
                    stringResource(R.string.battery_power_source) to batteryInfo.powerSource,
                    stringResource(R.string.battery_technology) to batteryInfo.technology,
                    stringResource(R.string.lbl_temperature) to stringResource(R.string.fmt_battery_temp, batteryInfo.temperatureCelsius),
                    stringResource(R.string.battery_current) to stringResource(R.string.fmt_ma, batteryInfo.currentMa),
                    stringResource(R.string.battery_power) to stringResource(R.string.fmt_watts, batteryInfo.powerWatts),
                    stringResource(R.string.lbl_voltage) to stringResource(R.string.fmt_voltage, batteryInfo.voltageVolts),
                    stringResource(R.string.battery_time_to_charge) to batteryInfo.timeToChargeFormatted,
                    stringResource(R.string.battery_charge_cycles) to (if (batteryInfo.chargeCycles > 0) batteryInfo.chargeCycles.toString() else stringResource(R.string.lbl_not_supported)),
                    stringResource(R.string.battery_capacity_charged) to stringResource(R.string.fmt_mah, batteryInfo.capacityChargedMah),
                    stringResource(R.string.battery_capacity_estimated) to stringResource(R.string.fmt_mah, batteryInfo.capacityEstimatedMah),
                    stringResource(R.string.battery_capacity_system) to stringResource(R.string.fmt_mah, batteryInfo.capacitySystemMah)
                )

                batterySpecs.forEachIndexed { index, (label, value) ->
                    BatteryItemRow(label, value)
                    if (index < batterySpecs.lastIndex) {
                        HorizontalDivider(color = dividerColor)
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryMetricChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BatteryItemRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.3f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BatteryScreenPreview() {
    DevPulseTheme {
        BatteryScreen(batteryInfo = BatteryInfo())
    }
}
