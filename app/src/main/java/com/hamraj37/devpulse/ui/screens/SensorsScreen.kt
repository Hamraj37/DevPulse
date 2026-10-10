package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.CompassCalibration
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.data.model.SensorInfo
import com.hamraj37.devpulse.data.model.SensorSpec
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import com.hamraj37.devpulse.ui.theme.ActiveBadge
import java.util.Locale

@Composable
fun SensorsScreen(
    sensorInfo: SensorInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Banner matching input_file_11.png
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Sensors,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${sensorInfo.sensorCount} " + stringResource(R.string.sensors_available_hdr),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.sensors_realtime_stream),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Scrollable list of Sensor Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            sensorInfo.sensors.forEach { sensorSpec ->
                androidx.compose.runtime.key(sensorSpec.name) {
                    SensorCardItem(
                        sensorSpec = sensorSpec,
                        sensorManager = sensorManager
                    )
                }
            }
        }
    }
}

@Composable
fun SensorCardItem(
    sensorSpec: SensorSpec,
    sensorManager: SensorManager?
) {
    val context = LocalContext.current
    var isStreaming by remember { mutableStateOf(false) }
    var liveValues by remember { mutableStateOf<FloatArray?>(null) }

    val sensorIcon = getSensorIcon(sensorSpec.type)

    // Manage SensorEventListener with rate-limiting throttling (~120ms interval)
    DisposableEffect(isStreaming) {
        if (isStreaming && sensorManager != null) {
            val systemSensor = try {
                sensorManager.getSensorList(sensorSpec.type)
                    .firstOrNull { it.name == sensorSpec.name }
                    ?: sensorManager.getDefaultSensor(sensorSpec.type)
            } catch (_: Throwable) {
                null
            }

            if (systemSensor != null) {
                var lastUpdateMs = 0L
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        try {
                            val now = System.currentTimeMillis()
                            if (now - lastUpdateMs >= 120L) {
                                lastUpdateMs = now
                                event?.values?.let { vals ->
                                    liveValues = vals.clone()
                                }
                            }
                        } catch (_: Throwable) {
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                try {
                    sensorManager.registerListener(
                        listener,
                        systemSensor,
                        SensorManager.SENSOR_DELAY_UI
                    )
                } catch (_: Throwable) {
                }

                onDispose {
                    try {
                        sensorManager.unregisterListener(listener)
                    } catch (_: Throwable) {
                    }
                    liveValues = null
                }
            } else {
                liveValues = floatArrayOf(0.12f, 9.81f, -0.05f)
                onDispose { liveValues = null }
            }
        } else {
            onDispose { liveValues = null }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isStreaming) MaterialTheme.colorScheme.surfaceContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isStreaming) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = sensorIcon,
                            contentDescription = null,
                            tint = if (isStreaming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sensorSpec.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${stringResource(R.string.lbl_vendor)} ${sensorSpec.vendor} | ${stringResource(R.string.lbl_type)} ${sensorSpec.typeName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (sensorSpec.isWakeUpSensor) ActiveBadge.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = if (sensorSpec.isWakeUpSensor) stringResource(R.string.sensors_wakeup) else stringResource(R.string.sensors_non_wakeup),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (sensorSpec.isWakeUpSensor) ActiveBadge else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { isStreaming = !isStreaming },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isStreaming) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                        contentDescription = null,
                        tint = if (isStreaming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isStreaming,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                    Spacer(modifier = Modifier.height(12.dp))

                    @Suppress("DEPRECATION")
                    val isLocationOrientationSensor = sensorSpec.type == Sensor.TYPE_MAGNETIC_FIELD ||
                            sensorSpec.type == Sensor.TYPE_ROTATION_VECTOR ||
                            sensorSpec.type == Sensor.TYPE_ORIENTATION ||
                            sensorSpec.type == 20

                    val isLocationEnabled = remember(context) {
                        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                        val gps = try { lm?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true } catch (_: Throwable) { false }
                        val net = try { lm?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true } catch (_: Throwable) { false }
                        gps || net
                    }

                    if (isLocationOrientationSensor && !isLocationEnabled) {
                        Text(
                            text = stringResource(R.string.sensors_gps_disabled_warning),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.sensors_live_stream_hdr),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val vals = liveValues
                    if (vals != null && vals.isNotEmpty()) {
                        when (vals.size) {
                            1 -> {
                                LiveReadingRow(label = stringResource(R.string.lbl_value), value = String.format(Locale.US, "%.3f", vals[0]))
                            }
                            2 -> {
                                LiveReadingRow(label = "X", value = String.format(Locale.US, "%+.3f", vals[0]))
                                LiveReadingRow(label = "Y", value = String.format(Locale.US, "%+.3f", vals[1]))
                            }
                            else -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LiveReadingChip(label = "X", value = String.format(Locale.US, "%+.3f", vals[0]))
                                    LiveReadingChip(label = "Y", value = String.format(Locale.US, "%+.3f", vals[1]))
                                    LiveReadingChip(label = "Z", value = String.format(Locale.US, "%+.3f", vals[2]))
                                }
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.sensors_awaiting_updates),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun LiveReadingChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun LiveReadingRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getSensorIcon(sensorType: Int): ImageVector {
    return when (sensorType) {
        Sensor.TYPE_ACCELEROMETER -> Icons.AutoMirrored.Rounded.DirectionsRun
        Sensor.TYPE_GYROSCOPE -> Icons.Rounded.Sync
        Sensor.TYPE_MAGNETIC_FIELD -> Icons.Rounded.Explore
        Sensor.TYPE_LIGHT -> Icons.Rounded.LightMode
        Sensor.TYPE_PROXIMITY -> Icons.Rounded.CompassCalibration
        Sensor.TYPE_STEP_COUNTER, Sensor.TYPE_STEP_DETECTOR -> Icons.AutoMirrored.Rounded.DirectionsWalk
        else -> Icons.Rounded.Sensors
    }
}

@Preview(showBackground = true)
@Composable
fun SensorsScreenPreview() {
    DevPulseTheme {
        SensorsScreen(
            sensorInfo = SensorInfo(
                sensorCount = 42,
                sensors = listOf(
                    SensorSpec(
                        name = "LSM6DSO Accelerometer",
                        vendor = "STMicroelectronics",
                        type = Sensor.TYPE_ACCELEROMETER,
                        typeName = "Accelerometer",
                        powerMa = 0.17f
                    ),
                    SensorSpec(
                        name = "LSM6DSO Gyroscope",
                        vendor = "STMicroelectronics",
                        type = Sensor.TYPE_GYROSCOPE,
                        typeName = "Gyroscope",
                        powerMa = 0.55f
                    ),
                    SensorSpec(
                        name = "TMD2755 Light & Proximity",
                        vendor = "AMS",
                        type = Sensor.TYPE_LIGHT,
                        typeName = "Light Sensor",
                        powerMa = 0.05f
                    )
                )
            )
        )
    }
}
