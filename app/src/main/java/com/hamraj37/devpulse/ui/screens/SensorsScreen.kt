package com.hamraj37.devpulse.ui.screens

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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.CompassCalibration
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.LightMode
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.SensorInfo
import com.hamraj37.devpulse.data.model.SensorSpec
import com.hamraj37.devpulse.data.telemetry.ThermalAndSensorTelemetry
import com.hamraj37.devpulse.ui.theme.ActiveBadge
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
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

    var selectedSensorForTest by remember { mutableStateOf<SensorSpec?>(null) }

    selectedSensorForTest?.let { spec ->
        FullScreenSensorTestDialog(
            sensorSpec = spec,
            sensorManager = sensorManager,
            onDismiss = { selectedSensorForTest = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Banner
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

        // Scrollable list of Sensor Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            sensorInfo.sensors.forEach { sensorSpec ->
                androidx.compose.runtime.key(sensorSpec.name) {
                    SensorCardItem(
                        sensorSpec = sensorSpec,
                        sensorManager = sensorManager,
                        onOpenFullScreenTest = { selectedSensorForTest = sensorSpec }
                    )
                }
            }
        }
    }
}

@Composable
fun SensorCardItem(
    sensorSpec: SensorSpec,
    sensorManager: SensorManager?,
    onOpenFullScreenTest: () -> Unit
) {
    val context = LocalContext.current
    var isStreaming by remember { mutableStateOf(false) }
    var liveValues by remember { mutableStateOf<FloatArray?>(null) }

    val sensorIcon = getSensorIcon(sensorSpec.type)

    // Manage SensorEventListener
    DisposableEffect(isStreaming) {
        if (isStreaming && sensorManager != null) {
            val systemSensor = try {
                sensorManager.getSensorList(sensorSpec.type).firstOrNull {
                    it.name == sensorSpec.name ||
                            ThermalAndSensorTelemetry.cleanSensorName(it.name) == sensorSpec.name ||
                            (it.vendor == sensorSpec.vendor && it.type == sensorSpec.type)
                } ?: sensorManager.getDefaultSensor(sensorSpec.type)
            } catch (_: Throwable) {
                null
            }

            if (systemSensor != null) {
                var lastUpdateMs = 0L
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        try {
                            val vals = event?.values ?: return
                            if (vals.isEmpty()) return
                            val now = System.currentTimeMillis()
                            if (liveValues == null || now - lastUpdateMs >= 80L) {
                                lastUpdateMs = now
                                liveValues = vals.clone()
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
                onDispose { liveValues = null }
            }
        } else {
            onDispose { liveValues = null }
        }
    }

    Card(
        onClick = onOpenFullScreenTest,
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
                    val formattedName = remember(sensorSpec.name, sensorSpec.typeName) {
                        if (sensorSpec.name.isBlank() || sensorSpec.name.startsWith("Hardware Sensor (Type", ignoreCase = true)) {
                            sensorSpec.typeName
                        } else {
                            sensorSpec.name
                        }
                    }

                    Text(
                        text = formattedName,
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Tap for Fullscreen Test",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
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
fun FullScreenSensorTestDialog(
    sensorSpec: SensorSpec,
    sensorManager: SensorManager?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isStreaming by remember { mutableStateOf(true) }
    var liveValues by remember { mutableStateOf<FloatArray?>(null) }
    var eventCount by remember { mutableIntStateOf(0) }

    DisposableEffect(isStreaming, sensorSpec) {
        if (isStreaming && sensorManager != null) {
            val systemSensor = try {
                sensorManager.getSensorList(sensorSpec.type).firstOrNull {
                    it.name == sensorSpec.name ||
                            ThermalAndSensorTelemetry.cleanSensorName(it.name) == sensorSpec.name ||
                            (it.vendor == sensorSpec.vendor && it.type == sensorSpec.type)
                } ?: sensorManager.getDefaultSensor(sensorSpec.type)
            } catch (_: Throwable) {
                null
            }

            if (systemSensor != null) {
                var lastUpdateMs = 0L
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        try {
                            val vals = event?.values ?: return
                            if (vals.isEmpty()) return
                            val now = System.currentTimeMillis()
                            if (liveValues == null || now - lastUpdateMs >= 60L) {
                                lastUpdateMs = now
                                liveValues = vals.clone()
                                eventCount++
                            }
                        } catch (_: Throwable) {}
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                try {
                    sensorManager.registerListener(
                        listener,
                        systemSensor,
                        SensorManager.SENSOR_DELAY_UI
                    )
                } catch (_: Throwable) {}

                onDispose {
                    try {
                        sensorManager.unregisterListener(listener)
                    } catch (_: Throwable) {}
                }
            } else {
                onDispose {}
            }
        } else {
            onDispose {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sensorSpec.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${sensorSpec.vendor} • ${sensorSpec.typeName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visualizer Graphic Canvas
                    SensorVisualizerCard(
                        sensorSpec = sensorSpec,
                        liveValues = liveValues
                    )

                    // Live Stream Readouts
                    SensorLiveReadoutSection(
                        sensorSpec = sensorSpec,
                        liveValues = liveValues,
                        eventCount = eventCount
                    )

                    // Detailed Hardware Specs
                    SensorHardwareSpecsCard(sensorSpec = sensorSpec)
                }
            }
        }
    }
}

@Composable
fun SensorVisualizerCard(
    sensorSpec: SensorSpec,
    liveValues: FloatArray?
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val vals = liveValues ?: floatArrayOf(0f, 0f, 0f)

            when (sensorSpec.type) {
                Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GRAVITY, Sensor.TYPE_LINEAR_ACCELERATION, 35, 38, 40, 16, 39, 41 -> {
                    // Bubble level / 2D Motion Canvas
                    val x = if (vals.isNotEmpty()) vals[0] else 0f
                    val y = if (vals.size > 1) vals[1] else 0f

                    Canvas(modifier = Modifier.size(190.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val radius = size.width / 2f - 12f

                        drawCircle(
                            color = primaryColor.copy(alpha = 0.15f),
                            radius = radius
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = radius,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = secondaryColor.copy(alpha = 0.4f),
                            radius = radius / 2f,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawLine(
                            color = primaryColor.copy(alpha = 0.3f),
                            start = Offset(cx - 20.dp.toPx(), cy),
                            end = Offset(cx + 20.dp.toPx(), cy),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = primaryColor.copy(alpha = 0.3f),
                            start = Offset(cx, cy - 20.dp.toPx()),
                            end = Offset(cx, cy + 20.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )

                        val ballX = (cx - (x * 10f)).coerceIn(24.dp.toPx(), size.width - 24.dp.toPx())
                        val ballY = (cy + (y * 10f)).coerceIn(24.dp.toPx(), size.height - 24.dp.toPx())

                        drawCircle(
                            color = primaryColor,
                            radius = 18.dp.toPx(),
                            center = Offset(ballX, ballY)
                        )
                    }
                }
                Sensor.TYPE_LIGHT, Sensor.TYPE_PROXIMITY -> {
                    // Intensity Circle Canvas
                    val v0 = if (vals.isNotEmpty()) vals[0] else 0f
                    val maxVal = if (sensorSpec.type == Sensor.TYPE_LIGHT) 1000f else (sensorSpec.maxRange.takeIf { it > 0 } ?: 10f)
                    val fraction = (v0 / maxVal).coerceIn(0.1f, 1.0f)

                    Canvas(modifier = Modifier.size(170.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val baseRadius = (size.width / 2f - 10f) * fraction

                        drawCircle(
                            color = primaryColor.copy(alpha = 0.2f),
                            radius = size.width / 2f - 10f
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = baseRadius
                        )
                    }
                }
                else -> {
                    // Multi-axis Pulsing Pulse Ring Gauge
                    val v0 = if (vals.isNotEmpty()) vals[0] else 0f

                    Canvas(modifier = Modifier.size(170.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val radius = size.width / 2f - 15f

                        drawCircle(
                            color = primaryColor.copy(alpha = 0.15f),
                            radius = radius
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = radius,
                            style = Stroke(width = 4.dp.toPx())
                        )

                        val sweepX = ((v0 * 20f) % 360f)

                        drawArc(
                            color = secondaryColor,
                            startAngle = sweepX,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = Offset(cx - radius + 10f, cy - radius + 10f),
                            size = androidx.compose.ui.geometry.Size((radius - 10f) * 2, (radius - 10f) * 2),
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SensorLiveReadoutSection(
    sensorSpec: SensorSpec,
    liveValues: FloatArray?,
    eventCount: Int
) {
    val unit = getSensorUnit(sensorSpec.type)
    val vals = liveValues

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Stream Data",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ActiveBadge.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "LIVE STREAM ($eventCount samples)",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ActiveBadge
                )
            }
        }

        if (vals == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Awaiting Live Sensor Updates...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else if (vals.size >= 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AxisCard(
                    axisName = "X Axis",
                    value = String.format(Locale.US, "%+.3f", vals[0]),
                    unit = unit,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                AxisCard(
                    axisName = "Y Axis",
                    value = String.format(Locale.US, "%+.3f", vals[1]),
                    unit = unit,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                AxisCard(
                    axisName = "Z Axis",
                    value = String.format(Locale.US, "%+.3f", vals[2]),
                    unit = unit,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        } else if (vals.size == 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AxisCard(
                    axisName = "X / Val 1",
                    value = String.format(Locale.US, "%+.3f", vals[0]),
                    unit = unit,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                AxisCard(
                    axisName = "Y / Val 2",
                    value = String.format(Locale.US, "%+.3f", vals[1]),
                    unit = unit,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        } else if (vals.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Sensor Reading",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.2f", vals[0])} $unit".trim(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun AxisCard(
    axisName: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = axisName.take(1),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SensorHardwareSpecsCard(
    sensorSpec: SensorSpec
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Hardware Specifications",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)

            SpecRow("Vendor", sensorSpec.vendor)
            SpecRow("Sensor Type ID", "${sensorSpec.type} (${sensorSpec.typeName})")
            SpecRow("Power Consumption", "${sensorSpec.powerMa} mA")
            SpecRow("Maximum Range", "${sensorSpec.maxRange}")
            SpecRow("Resolution", "${sensorSpec.resolution}")
            SpecRow("Min Delay", "${sensorSpec.minDelayUs} µs")
            SpecRow("Wake-Up Sensor", if (sensorSpec.isWakeUpSensor) "Yes" else "No")
        }
    }
}

@Composable
fun SpecRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun getSensorUnit(sensorType: Int): String {
    return when (sensorType) {
        Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_GRAVITY, 35, 38, 40 -> "m/s²"
        Sensor.TYPE_GYROSCOPE, 16, 39, 41 -> "rad/s"
        Sensor.TYPE_MAGNETIC_FIELD, 14 -> "µT"
        Sensor.TYPE_LIGHT -> "lux"
        Sensor.TYPE_PRESSURE -> "hPa"
        Sensor.TYPE_PROXIMITY -> "cm"
        Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_TEMPERATURE -> "°C"
        Sensor.TYPE_RELATIVE_HUMIDITY -> "%"
        Sensor.TYPE_STEP_COUNTER -> "steps"
        Sensor.TYPE_HEART_RATE -> "bpm"
        else -> ""
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
        Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_GRAVITY, 35, 38, 40 -> Icons.AutoMirrored.Rounded.DirectionsRun
        Sensor.TYPE_GYROSCOPE, 16, 39, 41 -> Icons.Rounded.Sync
        Sensor.TYPE_MAGNETIC_FIELD, 14 -> Icons.Rounded.Explore
        Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_ORIENTATION, 15, 20, 28 -> Icons.Rounded.CompassCalibration
        Sensor.TYPE_LIGHT -> Icons.Rounded.LightMode
        Sensor.TYPE_PROXIMITY -> Icons.Rounded.CompassCalibration
        Sensor.TYPE_STEP_COUNTER, Sensor.TYPE_STEP_DETECTOR, Sensor.TYPE_MOTION_DETECT, Sensor.TYPE_STATIONARY_DETECT, 17 -> Icons.AutoMirrored.Rounded.DirectionsWalk
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
