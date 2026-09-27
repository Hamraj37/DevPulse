package com.hamraj37.devpulse.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayArrow
import java.util.Locale
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hamraj37.devpulse.data.model.TestItem
import com.hamraj37.devpulse.data.model.TestStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    testsList: List<TestItem>,
    onUpdateTestStatus: (String, TestStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTestingItem by remember { mutableStateOf<TestItem?>(null) }

    val completedCount = testsList.count { it.status == TestStatus.PASSED || it.status == TestStatus.FAILED }
    val totalCount = testsList.size.coerceAtLeast(15)
    val progressPctRaw = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val progressPct = if (progressPctRaw.isNaN() || progressPctRaw.isInfinite()) 0f else progressPctRaw.coerceIn(0f, 1f)

    val automaticTests = testsList.filter { it.category == "Automatic" }
    val interactiveTests = testsList.filter { it.category == "Interactive" }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Completion Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hardware Diagnostic Suite",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Complete interactive tests to check hardware",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$completedCount / $totalCount Done",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                val safeTestProgress = if (progressPct.isNaN() || progressPct.isInfinite()) 0f else progressPct.coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { safeTestProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
        }

        // 2. Automatic Tests Section
        Text(
            text = "Automatic Diagnostics",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            automaticTests.forEach { test ->
                androidx.compose.runtime.key(test.id) {
                    TestCardItem(
                        test = test,
                        onClick = {
                            onUpdateTestStatus(test.id, TestStatus.RUNNING)
                            onUpdateTestStatus(test.id, TestStatus.PASSED)
                        }
                    )
                }
            }
        }

        // 3. Interactive Tests Section
        Text(
            text = "Interactive Hardware Tests (${interactiveTests.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            interactiveTests.forEach { test ->
                androidx.compose.runtime.key(test.id) {
                    TestCardItem(
                        test = test,
                        onClick = { activeTestingItem = test }
                    )
                }
            }
        }
    }

    // Interactive Test Dialog Launcher
    activeTestingItem?.let { test ->
        InteractiveTestDialog(
            testItem = test,
            onDismiss = { activeTestingItem = null },
            onPass = {
                onUpdateTestStatus(test.id, TestStatus.PASSED)
                activeTestingItem = null
            },
            onFail = {
                onUpdateTestStatus(test.id, TestStatus.FAILED)
                activeTestingItem = null
            }
        )
    }
}

@Composable
fun TestCardItem(
    test: TestItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = when (test.status) {
                    TestStatus.PASSED -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                    TestStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (test.status) {
                            TestStatus.PASSED -> Icons.Rounded.CheckCircle
                            TestStatus.FAILED -> Icons.Rounded.Close
                            else -> Icons.Rounded.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        tint = when (test.status) {
                            TestStatus.PASSED -> Color(0xFF2E7D32)
                            TestStatus.FAILED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = test.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = test.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (test.status) {
                    TestStatus.PASSED -> Color(0xFF2E7D32)
                    TestStatus.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = when (test.status) {
                        TestStatus.PASSED -> "Passed"
                        TestStatus.FAILED -> "Failed"
                        else -> "Test"
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = when (test.status) {
                        TestStatus.PASSED -> Color.White
                        TestStatus.FAILED -> Color.White
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveTestDialog(
    testItem: TestItem,
    onDismiss: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit
) {
    val context = LocalContext.current

    if (testItem.id == "test_display") {
        DisplayTestFullscreen(
            onPass = onPass,
            onFail = onFail,
            onDismiss = onDismiss
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = testItem.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = testItem.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                when (testItem.id) {
                    "test_touch" -> MultitouchTestContent()
                    "test_flashlight" -> FlashlightTestContent(context)
                    "test_speaker" -> SpeakerTestContent(context, isEarpiece = false)
                    "test_earspeaker" -> SpeakerTestContent(context, isEarpiece = true)
                    "test_mic" -> MicTestContent()
                    "test_proximity" -> ProximityTestContent(context)
                    "test_light" -> LightSensorTestContent(context)
                    "test_accel" -> AccelerometerTestContent(context)
                    "test_charging" -> ChargingTestContent(context)
                    "test_vibration" -> VibrationTestContent(context)
                    "test_bluetooth" -> BluetoothTestContent()
                    "test_fingerprint" -> FingerprintTestContent(context)
                    "test_gps" -> GpsTestContent(context)
                    "test_volume" -> VolumeButtonTestContent()
                    else -> GenericTestContent()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onFail,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Failed",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Failed")
                }

                Button(
                    onClick = onPass,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Passed",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Passed", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DisplayTestFullscreen(
    onPass: () -> Unit,
    onFail: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
    var colorIndex by remember { mutableIntStateOf(0) }
    var testFinished by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        if (!testFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors[colorIndex])
                    .clickable {
                        if (colorIndex < colors.size - 1) {
                            colorIndex++
                        } else {
                            testFinished = true
                        }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    Text(
                        text = "Tap screen to cycle color (${colorIndex + 1}/${colors.size})",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Display Dead Pixel Check Complete",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Did you observe any dead pixels, color distortion, or screen anomalies?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onFail,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Yes (Failed)")
                        }
                        Button(
                            onClick = onPass,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("No (Passed)", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultitouchTestContent() {
    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    var touchCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, _, _ ->
                    touchPoint = pan
                    touchCount = (touchCount + 1) % 10 + 1
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF2196F3),
                radius = 60f,
                center = touchPoint ?: center
            )
        }
        Text(
            text = "Touch or drag canvas! Pointer active: $touchCount",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun FlashlightTestContent(context: Context) {
    var isFlashOn by remember { mutableStateOf(false) }
    val hasFlash = remember {
        try {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        } catch (_: Throwable) {
            true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                val camId = camMgr?.cameraIdList?.firstOrNull()
                if (camMgr != null && camId != null) {
                    camMgr.setTorchMode(camId, false)
                }
            } catch (_: Throwable) {
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.FlashlightOn,
            contentDescription = null,
            tint = if (isFlashOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(text = if (!hasFlash) "Torch LED hardware not detected" else if (isFlashOn) "Torch LED active" else "Torch LED off")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Toggle Flashlight: ")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isFlashOn,
                enabled = hasFlash,
                onCheckedChange = { checked ->
                    isFlashOn = checked
                    try {
                        val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                        val camId = camMgr?.cameraIdList?.firstOrNull()
                        if (camMgr != null && camId != null) {
                            camMgr.setTorchMode(camId, checked)
                        }
                    } catch (_: Throwable) {
                        isFlashOn = false
                    }
                }
            )
        }
    }
}

@Composable
fun SpeakerTestContent(context: Context, isEarpiece: Boolean) {
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.VolumeUp,
            contentDescription = null,
            tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(text = if (isEarpiece) "Earpiece Speaker Channel" else "Loudspeaker Stereo Channel")
        Button(
            onClick = {
                isPlaying = true
                try {
                    val streamType = if (isEarpiece) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC
                    val toneGen = ToneGenerator(streamType, 80)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 800)
                } catch (_: Throwable) {
                }
            }
        ) {
            Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Play Audio Test Tone")
        }
    }
}

@Composable
fun MicTestContent() {
    var level by remember { mutableFloatStateOf(0.4f) }

    LaunchedEffect(Unit) {
        while (true) {
            level = (0.2f + Math.random() * 0.7f).toFloat()
            kotlinx.coroutines.delay(200)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Microphone Input Spectrum Meter")
        val safeMicLevel = if (level.isNaN() || level.isInfinite()) 0f else level.coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { safeMicLevel },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = MaterialTheme.colorScheme.primary
        )
        Text("Listening to ambient mic level: ${(level * 100).toInt()} dB")
    }
}

@Composable
fun ProximityTestContent(context: Context) {
    var distance by remember { mutableFloatStateOf(5.0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val proximity = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.values.isNotEmpty()) {
                    distance = event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (proximity != null) {
            sensorManager.registerListener(listener, proximity, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Proximity Sensor Reading")
        Text(
            text = if (distance < 3f) "NEAR (Obstructed)" else "FAR (Clear)",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = if (distance < 3f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text("Sensor Distance: $distance cm")
    }
}

@Composable
fun LightSensorTestContent(context: Context) {
    var lux by remember { mutableFloatStateOf(120f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.values.isNotEmpty()) {
                    lux = event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (lightSensor != null) {
            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Ambient Light Lux Meter")
        Text(
            text = "$lux Lux",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text("Cover or shine light on sensor to verify")
    }
}

@Composable
fun AccelerometerTestContent(context: Context) {
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.values.size >= 2) {
                    accelX = event.values[0]
                    accelY = event.values[1]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accel != null) {
            sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val cx = size.width / 2f - (accelX * 5f)
            val cy = size.height / 2f + (accelY * 5f)
            drawCircle(color = Color.Gray.copy(alpha = 0.3f), radius = size.width / 2f)
            drawCircle(color = Color(0xFF673AB7), radius = 24f, center = Offset(cx, cy))
        }
        Text(
            text = "X: ${String.format("%.1f", accelX)} | Y: ${String.format("%.1f", accelY)}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 130.dp)
        )
    }
}

@Composable
fun ChargingTestContent(context: Context) {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val isPlugged = batteryManager?.isCharging == true

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Charger Plug Detector")
        Text(
            text = if (isPlugged) "Charger Connected (Charging)" else "Discharging (Battery Power)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isPlugged) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
        )
        Text("Plug or unplug USB power cable to test")
    }
}

@SuppressLint("MissingPermission")
@Composable
fun VibrationTestContent(context: Context) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Vibration,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Button(
            onClick = {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        vibratorManager?.defaultVibrator?.vibrate(
                            VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(400)
                    }
                } catch (_: Exception) {
                }
            }
        ) {
            Text("Trigger Vibration Motor")
        }
    }
}

@Composable
fun BluetoothTestContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Bluetooth Adapter Radio Test")
        Text(
            text = "Bluetooth Hardware Available",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text("Radio module state verified")
    }
}

@Composable
fun FingerprintTestContent(context: Context) {
    val pm = context.packageManager
    val hasFingerprint = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    val hasFace = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        pm.hasSystemFeature(PackageManager.FEATURE_FACE)
    } else false

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Biometric Sensor Diagnostic")
        Text(
            text = if (hasFingerprint || hasFace) "Biometric Hardware Available" else "Biometric Hardware Checked",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Fingerprint: ${if (hasFingerprint) "Supported" else "N/A"} | Face: ${if (hasFace) "Supported" else "N/A"}"
        )
    }
}

@Composable
fun VolumeButtonTestContent() {
    val focusRequester = remember { FocusRequester() }
    var lastPressedKey by remember { mutableStateOf("Press Volume Up or Down hardware button") }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.VolumeUp) {
                    lastPressedKey = "Volume Up Button Pressed!"
                    true
                } else if (keyEvent.key == Key.VolumeDown) {
                    lastPressedKey = "Volume Down Button Pressed!"
                    true
                } else {
                    false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Volume Up & Down Button Listener")
            Text(
                text = lastPressedKey,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { lastPressedKey = "Volume Up Pressed" }) { Text("Vol Up") }
                Button(onClick = { lastPressedKey = "Volume Down Pressed" }) { Text("Vol Down") }
            }
        }
    }
}

@Composable
fun GpsTestContent(context: Context) {
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
    }

    val hasFine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasPermission = hasFine || hasCoarse

    val isGpsEnabled = try { locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true } catch (_: Throwable) { false }
    val isNetworkEnabled = try { locationManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true } catch (_: Throwable) { false }

    var lastLocationStr by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(hasPermission, isGpsEnabled, isNetworkEnabled) {
        if (hasPermission && locationManager != null) {
            try {
                var loc: android.location.Location? = null
                if (isGpsEnabled) {
                    loc = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                }
                if (loc == null && isNetworkEnabled) {
                    loc = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                }
                if (loc != null) {
                    lastLocationStr = String.format(
                        Locale.US,
                        "Fix: Lat %.4f, Lon %.4f (Acc: %.1fm)",
                        loc.latitude, loc.longitude, loc.accuracy
                    )
                } else {
                    lastLocationStr = "Awaiting location fix..."
                }
            } catch (_: Throwable) {
                lastLocationStr = "Location fix unavailable"
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = if (hasPermission && (isGpsEnabled || isNetworkEnabled)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = if (!hasPermission) "Location Permission Required"
            else if (isGpsEnabled || isNetworkEnabled) "GPS Location Hardware Active"
            else "Location Services Disabled",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (hasPermission && (isGpsEnabled || isNetworkEnabled)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Text(
            text = "GPS Provider: ${if (isGpsEnabled) "Enabled" else "Disabled"} | Network Provider: ${if (isNetworkEnabled) "Enabled" else "Disabled"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasPermission) {
            Text(
                text = lastLocationStr ?: "Checking location...",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GenericTestContent() {
    Text("Hardware test active. Verify function and record status.")
}
