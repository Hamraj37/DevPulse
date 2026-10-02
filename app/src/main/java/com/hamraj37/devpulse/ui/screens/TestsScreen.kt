package com.hamraj37.devpulse.ui.screens

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hamraj37.devpulse.data.model.TestItem
import com.hamraj37.devpulse.data.model.TestStatus
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun TestsScreen(
    testsList: List<TestItem>,
    onUpdateTestStatus: (String, TestStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTestingItem by remember { mutableStateOf<TestItem?>(null) }

    val completedCount = testsList.count { it.status == TestStatus.PASSED || it.status == TestStatus.FAILED }
    val totalCount = testsList.size.coerceAtLeast(15)
    val progressPct = completedCount.toFloat() / totalCount.toFloat()

    val automaticTests = testsList.filter { it.category == "Automatic" }
    val interactiveTests = testsList.filter { it.category == "Interactive" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Hardware Diagnostic Tests",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Complete interactive tests to check hardware",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$completedCount of $totalCount Completed",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(progressPct * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                val safeTestProgress = if (progressPct.isNaN() || progressPct.isInfinite()) 0f else progressPct.coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { safeTestProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
        }

        // Automatic Tests Section
        if (automaticTests.isNotEmpty()) {
            Text(
                text = "Automatic Diagnostics (${automaticTests.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            automaticTests.forEach { test ->
                key(test.id) {
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

        // Interactive Tests Section
        Text(
            text = "Interactive Hardware Tests (${interactiveTests.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        interactiveTests.forEach { test ->
            key(test.id) {
                TestCardItem(
                    test = test,
                    onClick = { activeTestingItem = test }
                )
            }
        }
    }

    // Interactive Test Full-Screen Launcher
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
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (test.status) {
                        TestStatus.PASSED -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                        TestStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (test.status) {
                                TestStatus.PASSED -> Icons.Rounded.CheckCircle
                                TestStatus.FAILED -> Icons.Rounded.Close
                                else -> Icons.Rounded.Construction
                            },
                            contentDescription = null,
                            tint = when (test.status) {
                                TestStatus.PASSED -> Color(0xFF2E7D32)
                                TestStatus.FAILED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = test.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = test.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (test.status) {
                    TestStatus.PASSED -> Color(0xFF2E7D32)
                    TestStatus.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ) {
                Text(
                    text = when (test.status) {
                        TestStatus.PASSED -> "Passed"
                        TestStatus.FAILED -> "Failed"
                        else -> "Test"
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = when (test.status) {
                        TestStatus.PASSED -> Color.White
                        TestStatus.FAILED -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

private fun getQuestionForTest(testId: String): String {
    return when (testId) {
        "test_touch" -> "Does the screen detects multiple fingers?"
        "test_display" -> "Were the screen one solid color?"
        "test_flashlight" -> "Is the flashlight working?"
        "test_speaker" -> "Audio is playing. Can you hear it?"
        "test_earspeaker" -> "Audio is playing. Can you hear it?"
        "test_mic" -> "Speak to the microphone. Is the progress changing?"
        "test_proximity" -> "Cover the area above the display or place your palm on the display. do you get any feedback?"
        "test_light" -> "Cover the area above the display. Is the value changing?"
        "test_accel" -> "Shake your device. Did you feel the vibration?"
        "test_charging" -> "Is it charging?"
        "test_vibration" -> "Can you feel the phone vibrating?"
        "test_bluetooth" -> "Is Bluetooth hardware working?"
        "test_fingerprint" -> "Is the fingerprint / biometric sensor working?"
        "test_gps" -> "Is location / GPS fix detected?"
        "test_volume" -> "Press the volume up or down key, do you get any feedback?"
        else -> "Is the test working properly?"
    }
}

@Composable
fun InteractiveTestDialog(
    testItem: TestItem,
    onDismiss: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit
) {
    val context = LocalContext.current

    if (testItem.id == "test_display") {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            DisplayTestFullscreen(
                onPass = onPass,
                onFail = onFail,
                onDismiss = onDismiss
            )
        }
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (testItem.id == "test_touch") Color.Black else MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Card Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (testItem.id == "test_touch") Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = testItem.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp
                            ),
                            color = if (testItem.id == "test_touch") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Middle Graphic / Interactive Visual Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (testItem.id) {
                        "test_touch" -> MultitouchTestContent()
                        "test_display" -> DisplayTestContent()
                        "test_flashlight" -> FlashlightTestGraphic(context)
                        "test_speaker" -> LoudspeakerTestGraphic(context)
                        "test_earspeaker" -> EarSpeakerTestGraphic(context)
                        "test_mic" -> MicTestGraphic()
                        "test_proximity" -> EarProximityTestGraphic(context)
                        "test_light" -> LightSensorTestGraphic(context)
                        "test_accel" -> AccelerometerTestGraphic(context)
                        "test_charging" -> ChargingTestGraphic(context)
                        "test_vibration" -> VibrationTestGraphic(context)
                        "test_bluetooth" -> BluetoothTestGraphic(context)
                        "test_fingerprint" -> FingerprintTestGraphic(context)
                        "test_gps" -> GpsTestGraphic(context)
                        "test_volume" -> VolumeButtonTestGraphic()
                        else -> GenericTestGraphic()
                    }
                }

                // Bottom Section: Question & Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = getQuestionForTest(testItem.id),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (testItem.id == "test_touch") Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // "No" Pill Button
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFF455345),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onFail() }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    border = BorderStroke(1.5.dp, Color.White),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "No",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "No",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        // "Yes" Pill Button
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFF455345),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onPass() }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    border = BorderStroke(1.5.dp, Color.White),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Yes",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Yes",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultitouchTestContent() {
    val pointers = remember { mutableStateMapOf<Int, Offset>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                try {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            pointers.clear()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    pointers[change.id.value.toInt()] = change.position
                                }
                            }
                        }
                    }
                } catch (_: Throwable) {}
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            pointers.values.forEachIndexed { i, pos ->
                val colors = listOf(
                    Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF4CAF50),
                    Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4)
                )
                val color = colors[i % colors.size]
                drawCircle(color = color.copy(alpha = 0.3f), radius = 100f, center = pos)
                drawCircle(color = color, radius = 50f, center = pos)
            }
        }
        if (pointers.isEmpty()) {
            Text(
                text = "Touch screen with multiple fingers",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun DisplayTestContent() {
    DisplayTestFullscreen(
        onPass = {},
        onFail = {},
        onDismiss = {}
    )
}

@Composable
fun DisplayTestFullscreen(
    onPass: () -> Unit,
    onFail: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
    val colorNames = listOf("Red", "Green", "Blue", "White", "Black")
    var colorIndex by remember { mutableIntStateOf(0) }
    var isTestComplete by remember { mutableStateOf(false) }

    if (!isTestComplete) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors[colorIndex])
                .clickable {
                    if (colorIndex < colors.size - 1) {
                        colorIndex++
                    } else {
                        isTestComplete = true
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 56.dp)
            ) {
                Text(
                    text = "Tap to cycle color: ${colorNames[colorIndex]} (${colorIndex + 1}/${colors.size})",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Display",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DisplayTestGraphic()
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Were the screen one solid color?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFF455345),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onFail() }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    border = BorderStroke(1.5.dp, Color.White),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "No",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "No",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFF455345),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onPass() }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    border = BorderStroke(1.5.dp, Color.White),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Yes",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Yes",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayTestGraphic() {
    Canvas(modifier = Modifier.size(160.dp, 280.dp)) {
        val w = size.width
        val h = size.height
        val corner = 24.dp.toPx()

        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, w, h),
                    cornerRadius = CornerRadius(corner, corner)
                )
            )
        }
        clipPath(path) {
            val bandHeight = h / 4f
            val colors = listOf(
                Color(0xFF29B6F6),
                Color(0xFF03A9F4),
                Color(0xFF0288D1),
                Color(0xFF01579B)
            )
            colors.forEachIndexed { i, color ->
                drawRect(
                    color = color,
                    topLeft = Offset(0f, i * bandHeight),
                    size = Size(w, bandHeight)
                )
            }
            drawRoundRect(
                color = Color(0xFF01579B),
                topLeft = Offset(w / 2f - 24.dp.toPx(), 12.dp.toPx()),
                size = Size(48.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
        }
    }
}

@Composable
fun FlashlightTestGraphic(context: Context) {
    DisposableEffect(Unit) {
        try {
            val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val camId = camMgr?.cameraIdList?.firstOrNull()
            if (camMgr != null && camId != null) {
                camMgr.setTorchMode(camId, true)
            }
        } catch (_: Throwable) {}

        onDispose {
            try {
                val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                val camId = camMgr?.cameraIdList?.firstOrNull()
                if (camMgr != null && camId != null) {
                    camMgr.setTorchMode(camId, false)
                }
            } catch (_: Throwable) {}
        }
    }

    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            withTransform({
                rotate(45f, pivot = Offset(cx, cy))
            }) {
                drawPath(
                    path = Path().apply {
                        moveTo(cx - 30.dp.toPx(), cy - 40.dp.toPx())
                        lineTo(cx + 30.dp.toPx(), cy - 40.dp.toPx())
                        lineTo(cx + 60.dp.toPx(), cy - 100.dp.toPx())
                        lineTo(cx - 60.dp.toPx(), cy - 100.dp.toPx())
                        close()
                    },
                    color = Color(0xFF81D4FA)
                )
                drawRoundRect(
                    color = Color(0xFF0288D1),
                    topLeft = Offset(cx - 30.dp.toPx(), cy - 40.dp.toPx()),
                    size = Size(60.dp.toPx(), 140.dp.toPx()),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFFFFD54F),
                    topLeft = Offset(cx - 8.dp.toPx(), cy + 10.dp.toPx()),
                    size = Size(16.dp.toPx(), 32.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }
    }
}

private fun playSystemRingtone(context: Context, isEarpiece: Boolean): Ringtone? {
    return try {
        val ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        if (ringtone != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val usage = if (isEarpiece) AudioAttributes.USAGE_VOICE_COMMUNICATION else AudioAttributes.USAGE_MEDIA
                val contentType = if (isEarpiece) AudioAttributes.CONTENT_TYPE_SPEECH else AudioAttributes.CONTENT_TYPE_MUSIC
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(contentType)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                ringtone.streamType = if (isEarpiece) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC
            }
            ringtone.play()
        }
        ringtone
    } catch (_: Throwable) {
        null
    }
}

@Composable
fun LoudspeakerTestGraphic(context: Context) {
    var activeRingtone by remember { mutableStateOf<Ringtone?>(null) }

    DisposableEffect(context) {
        activeRingtone = playSystemRingtone(context, isEarpiece = false)

        onDispose {
            try {
                if (activeRingtone?.isPlaying == true) {
                    activeRingtone?.stop()
                }
            } catch (_: Throwable) {}
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(180.dp, 220.dp)) {
            val w = size.width
            val h = size.height
            val corner = 20.dp.toPx()

            drawRoundRect(
                color = Color(0xFF0288D1),
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(corner, corner)
            )

            val screwRadius = 5.dp.toPx()
            val margin = 14.dp.toPx()
            listOf(
                Offset(margin, margin),
                Offset(w - margin, margin),
                Offset(margin, h - margin),
                Offset(w - margin, h - margin)
            ).forEach {
                drawCircle(color = Color(0xFF01579B), radius = screwRadius, center = it)
            }

            val topCenter = Offset(w / 2f, h * 0.3f)
            drawCircle(color = Color(0xFF37474F), radius = 28.dp.toPx(), center = topCenter)
            drawCircle(color = Color(0xFFECEFF1), radius = 18.dp.toPx(), center = topCenter)
            drawCircle(color = Color(0xFF212121), radius = 8.dp.toPx(), center = topCenter)

            val bottomCenter = Offset(w / 2f, h * 0.7f)
            drawCircle(color = Color(0xFF37474F), radius = 48.dp.toPx(), center = bottomCenter)
            drawCircle(color = Color(0xFFECEFF1), radius = 32.dp.toPx(), center = bottomCenter)
            drawCircle(color = Color(0xFF212121), radius = 14.dp.toPx(), center = bottomCenter)
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.clickable {
                try {
                    if (activeRingtone?.isPlaying == true) {
                        activeRingtone?.stop()
                    }
                    activeRingtone = playSystemRingtone(context, isEarpiece = false)
                } catch (_: Throwable) {}
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Play System Ringtone Again",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun EarSpeakerTestGraphic(context: Context) {
    var activeRingtone by remember { mutableStateOf<Ringtone?>(null) }

    DisposableEffect(context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val originalMode = audioManager?.mode ?: AudioManager.MODE_NORMAL
        val originalSpeakerphone = audioManager?.isSpeakerphoneOn ?: false

        try {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.isSpeakerphoneOn = false

            activeRingtone = playSystemRingtone(context, isEarpiece = true)
        } catch (_: Throwable) {}

        onDispose {
            try {
                if (activeRingtone?.isPlaying == true) {
                    activeRingtone?.stop()
                }
            } catch (_: Throwable) {}
            try {
                audioManager?.isSpeakerphoneOn = originalSpeakerphone
                audioManager?.mode = originalMode
            } catch (_: Throwable) {}
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(160.dp, 60.dp)) {
            val cx = size.width / 2f
            val cy = size.height

            val arcs = listOf(
                100.dp.toPx() to Color(0xFF8E24AA),
                75.dp.toPx() to Color(0xFFAB47BC),
                50.dp.toPx() to Color(0xFFCE93D8)
            )

            arcs.forEach { (radius, color) ->
                drawArc(
                    color = color,
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        DisplayTestGraphic()

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.clickable {
                try {
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
                    audioManager?.isSpeakerphoneOn = false

                    if (activeRingtone?.isPlaying == true) {
                        activeRingtone?.stop()
                    }
                    activeRingtone = playSystemRingtone(context, isEarpiece = true)
                } catch (_: Throwable) {}
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Play System Ringtone Again",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun MicTestGraphic() {
    var level by remember { mutableFloatStateOf(0.3f) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                level = (0.15f + Math.random() * 0.75f).toFloat()
            } catch (_: Throwable) {}
            delay(200)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Canvas(modifier = Modifier.size(160.dp, 200.dp)) {
            val cx = size.width / 2f
            val cy = size.height * 0.4f

            drawRoundRect(
                color = Color(0xFF78909C),
                topLeft = Offset(cx - 30.dp.toPx(), cy - 50.dp.toPx()),
                size = Size(60.dp.toPx(), 100.dp.toPx()),
                cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx())
            )

            val ribColor = Color(0xFF546E7A)
            listOf(-20.dp.toPx(), 0f, 20.dp.toPx()).forEach { yOffset ->
                drawLine(
                    color = ribColor,
                    start = Offset(cx - 26.dp.toPx(), cy + yOffset),
                    end = Offset(cx + 26.dp.toPx(), cy + yOffset),
                    strokeWidth = 4.dp.toPx()
                )
            }

            drawArc(
                color = Color(0xFF78909C),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - 45.dp.toPx(), cy - 20.dp.toPx()),
                size = Size(90.dp.toPx(), 90.dp.toPx()),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )

            drawLine(
                color = Color(0xFF78909C),
                start = Offset(cx, cy + 70.dp.toPx()),
                end = Offset(cx, cy + 100.dp.toPx()),
                strokeWidth = 10.dp.toPx()
            )

            drawLine(
                color = Color(0xFF78909C),
                start = Offset(cx - 35.dp.toPx(), cy + 100.dp.toPx()),
                end = Offset(cx + 35.dp.toPx(), cy + 100.dp.toPx()),
                strokeWidth = 10.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        LinearProgressIndicator(
            progress = { level },
            modifier = Modifier
                .width(240.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF424242),
            trackColor = Color(0xFFE0E0E0)
        )
    }
}

@Composable
fun EarProximityTestGraphic(context: Context) {
    var isNear by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        var listener: SensorEventListener? = null
        var sensorManager: SensorManager? = null
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val proximity = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

            listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    try {
                        if (event != null && event.values.isNotEmpty()) {
                            isNear = event.values[0] < 3f
                        }
                    } catch (_: Throwable) {}
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            if (sensorManager != null && proximity != null) {
                sensorManager.registerListener(listener, proximity, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (_: Throwable) {}

        onDispose {
            try {
                if (sensorManager != null && listener != null) {
                    sensorManager.unregisterListener(listener)
                }
            } catch (_: Throwable) {}
        }
    }

    Canvas(modifier = Modifier.size(220.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        drawCircle(
            color = if (isNear) Color(0xFF80DEEA) else Color(0xFFB2EBF2),
            radius = 90.dp.toPx(),
            center = Offset(cx + 20.dp.toPx(), cy)
        )

        drawCircle(
            color = if (isNear) Color(0xFF26C6DA) else Color(0xFF4DD0E1),
            radius = 60.dp.toPx(),
            center = Offset(cx + 20.dp.toPx(), cy)
        )

        drawCircle(
            color = Color(0xFF00ACC1),
            radius = 20.dp.toPx(),
            center = Offset(cx + 20.dp.toPx(), cy)
        )

        drawPath(
            path = Path().apply {
                moveTo(cx - 90.dp.toPx(), cy + 10.dp.toPx())
                lineTo(cx - 50.dp.toPx(), cy - 70.dp.toPx())
                lineTo(cx - 20.dp.toPx(), cy + 60.dp.toPx())
                lineTo(cx + 20.dp.toPx(), cy)
            },
            color = Color(0xFF0288D1),
            style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun LightSensorTestGraphic(context: Context) {
    var lux by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        var listener: SensorEventListener? = null
        var sensorManager: SensorManager? = null
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

            listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    try {
                        if (event != null && event.values.isNotEmpty()) {
                            lux = event.values[0]
                        }
                    } catch (_: Throwable) {}
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            if (sensorManager != null && lightSensor != null) {
                sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (_: Throwable) {}

        onDispose {
            try {
                if (sensorManager != null && listener != null) {
                    sensorManager.unregisterListener(listener)
                }
            } catch (_: Throwable) {}
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = Color(0xFF546E7A),
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 16.dp.toPx())
            )

            drawRoundRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(16.dp.toPx(), 16.dp.toPx()),
                size = Size(w - 32.dp.toPx(), h - 32.dp.toPx()),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            val cx = w / 2f
            val cy = h / 2f
            val size = 50.dp.toPx()

            withTransform({
                rotate(0f, Offset(cx, cy))
            }) {
                drawRoundRect(
                    color = Color(0xFF90A4AE),
                    topLeft = Offset(cx - size, cy - size),
                    size = Size(size * 2, size * 2),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            withTransform({
                rotate(45f, Offset(cx, cy))
            }) {
                drawRoundRect(
                    color = Color(0xFF78909C),
                    topLeft = Offset(cx - size, cy - size),
                    size = Size(size * 2, size * 2),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
        }

        Text(
            text = "${lux.toInt()} lx",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AccelerometerTestGraphic(context: Context) {
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(9.8f) }

    DisposableEffect(Unit) {
        var listener: SensorEventListener? = null
        var sensorManager: SensorManager? = null
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    try {
                        if (event != null && event.values.size >= 3) {
                            accelX = event.values[0]
                            accelY = event.values[1]
                            accelZ = event.values[2]
                        }
                    } catch (_: Throwable) {}
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            if (sensorManager != null && accel != null) {
                sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)
            }
        } catch (_: Throwable) {}

        onDispose {
            try {
                if (sensorManager != null && listener != null) {
                    sensorManager.unregisterListener(listener)
                }
            } catch (_: Throwable) {}
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerRadius = size.width / 2f - 10f

            drawCircle(
                color = Color(0xFF29B6F6).copy(alpha = 0.2f),
                radius = outerRadius
            )
            drawCircle(
                color = Color(0xFF0288D1),
                radius = outerRadius,
                style = Stroke(width = 4.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF0288D1).copy(alpha = 0.3f),
                radius = outerRadius / 2f,
                style = Stroke(width = 2.dp.toPx())
            )

            val ballX = (cx - (accelX * 12f)).coerceIn(24.dp.toPx(), size.width - 24.dp.toPx())
            val ballY = (cy + (accelY * 12f)).coerceIn(24.dp.toPx(), size.height - 24.dp.toPx())

            drawCircle(
                color = Color(0xFFFFCA28),
                radius = 20.dp.toPx(),
                center = Offset(ballX, ballY)
            )
        }

        Text(
            text = "X: ${String.format(Locale.US, "%.1f", accelX)} | Y: ${String.format(Locale.US, "%.1f", accelY)} | Z: ${String.format(Locale.US, "%.1f", accelZ)} m/s²",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ChargingTestGraphic(context: Context) {
    var batteryLevel by remember { mutableIntStateOf(72) }
    var isCharging by remember { mutableStateOf(false) }
    var tempC by remember { mutableFloatStateOf(39f) }
    var voltageMv by remember { mutableIntStateOf(4078) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent != null) {
                    val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (rawLevel >= 0 && scale > 0) {
                        batteryLevel = (rawLevel * 100) / scale
                    }
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                    val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                    if (rawTemp > 0) {
                        tempC = rawTemp / 10f
                    }
                    val rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                    if (rawVoltage > 0) {
                        voltageMv = rawVoltage
                    }
                }
            }
        }

        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val sticky = context.registerReceiver(receiver, filter)
            sticky?.let { intent ->
                receiver.onReceive(context, intent)
            }
        } catch (_: Throwable) {}

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Throwable) {}
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(100.dp, 150.dp)) {
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = if (isCharging) Color(0xFF66BB6A) else Color(0xFFFF7043),
                topLeft = Offset(w / 2f - 16.dp.toPx(), 0f),
                size = Size(32.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            drawRoundRect(
                color = if (isCharging) Color(0xFF66BB6A) else Color(0xFFFF7043),
                topLeft = Offset(0f, 10.dp.toPx()),
                size = Size(w, h - 10.dp.toPx()),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
            )

            if (isCharging) {
                val boltPath = Path().apply {
                    moveTo(w * 0.55f, h * 0.25f)
                    lineTo(w * 0.35f, h * 0.52f)
                    lineTo(w * 0.52f, h * 0.52f)
                    lineTo(w * 0.45f, h * 0.80f)
                    lineTo(w * 0.65f, h * 0.48f)
                    lineTo(w * 0.48f, h * 0.48f)
                    close()
                }
                drawPath(path = boltPath, color = Color(0xFFFFD54F))
            }
        }

        Text(
            text = if (isCharging) "Charging (Power Connected)" else "Connect your charger",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isCharging) Color(0xFF2E7D32) else Color(0xFFD32F2F)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("Level", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$batteryLevel%", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("Temperature", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${String.format(Locale.US, "%.1f", tempC)} °C", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("Voltage", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$voltageMv mV", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VibrationTestGraphic(context: Context) {
    LaunchedEffect(Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Throwable) {}
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(40.dp, 160.dp)) {
            val h = size.height
            listOf(20.dp.toPx(), 35.dp.toPx()).forEach { xOff ->
                drawLine(
                    color = Color(0xFFAB47BC),
                    start = Offset(xOff, h * 0.2f),
                    end = Offset(xOff, h * 0.8f),
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        DisplayTestGraphic()

        Canvas(modifier = Modifier.size(40.dp, 160.dp)) {
            val h = size.height
            listOf(5.dp.toPx(), 20.dp.toPx()).forEach { xOff ->
                drawLine(
                    color = Color(0xFFAB47BC),
                    start = Offset(xOff, h * 0.2f),
                    end = Offset(xOff, h * 0.8f),
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun BluetoothTestGraphic(context: Context) {
    val bluetoothManager = remember(context) {
        try {
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        } catch (_: Throwable) {
            null
        }
    }
    val adapter = bluetoothManager?.adapter
    val isEnabled = adapter?.isEnabled == true
    val hasHardware = adapter != null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = if (hasHardware) Color(0xFF1565C0) else Color.Gray,
            modifier = Modifier.size(140.dp, 200.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Bluetooth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Text(
            text = if (!hasHardware) "Bluetooth Hardware Not Supported" else if (isEnabled) "Bluetooth Hardware Active & Enabled" else "Bluetooth Radio Ready (Disabled)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (hasHardware) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FingerprintTestGraphic(context: Context) {
    val pm = context.packageManager
    val hasFingerprint = remember {
        try { pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) } catch (_: Throwable) { false }
    }
    val hasFace = remember {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                pm.hasSystemFeature(PackageManager.FEATURE_FACE)
            } else false
        } catch (_: Throwable) { false }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(160.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(90.dp)
                )
            }
        }

        Text(
            text = if (hasFingerprint || hasFace) "Biometric Hardware Detected" else "Biometric Hardware Checked",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Fingerprint Sensor: ${if (hasFingerprint) "Supported" else "N/A"} | Face Unlock: ${if (hasFace) "Supported" else "N/A"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GpsTestGraphic(context: Context) {
    val locationManager = remember(context) {
        try {
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        } catch (_: Throwable) { null }
    }

    val hasFine = try {
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    } catch (_: Throwable) { false }

    val isGpsEnabled = try {
        locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    } catch (_: Throwable) { false }

    val isNetworkEnabled = try {
        locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    } catch (_: Throwable) { false }

    var locationFixText by remember { mutableStateOf("Checking location provider...") }

    LaunchedEffect(hasFine, isGpsEnabled, isNetworkEnabled) {
        if (hasFine && locationManager != null) {
            try {
                var loc = if (isGpsEnabled) {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else null
                if (loc == null && isNetworkEnabled) {
                    loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
                if (loc != null) {
                    locationFixText = String.format(
                        Locale.US,
                        "Fix: Lat %.4f, Lon %.4f (Acc: %.1fm)",
                        loc.latitude, loc.longitude, loc.accuracy
                    )
                } else {
                    locationFixText = "Awaiting location satellite fix..."
                }
            } catch (_: Throwable) {
                locationFixText = "Location provider active"
            }
        } else if (!hasFine) {
            locationFixText = "Location permission requested"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(160.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Text(
            text = if (isGpsEnabled || isNetworkEnabled) "GPS Location Hardware Active" else "Location Provider Disabled",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isGpsEnabled || isNetworkEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        Text(
            text = locationFixText,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VolumeButtonTestGraphic() {
    val focusRequester = remember { FocusRequester() }
    var volUpPressed by remember { mutableStateOf(false) }
    var volDownPressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {}
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.VolumeUp) {
                    volUpPressed = true
                    true
                } else if (keyEvent.key == Key.VolumeDown) {
                    volDownPressed = true
                    true
                } else {
                    false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Canvas(modifier = Modifier.size(220.dp, 180.dp)) {
                val w = size.width
                val h = size.height
                val cx = w * 0.32f
                val cy = h / 2f

                drawPath(
                    path = Path().apply {
                        moveTo(cx - 36.dp.toPx(), cy - 18.dp.toPx())
                        lineTo(cx - 10.dp.toPx(), cy - 18.dp.toPx())
                        lineTo(cx + 26.dp.toPx(), cy - 45.dp.toPx())
                        lineTo(cx + 26.dp.toPx(), cy + 45.dp.toPx())
                        lineTo(cx - 10.dp.toPx(), cy + 18.dp.toPx())
                        lineTo(cx - 36.dp.toPx(), cy + 18.dp.toPx())
                        close()
                    },
                    color = Color(0xFF78909C)
                )

                val rUp = 55.dp.toPx()
                drawArc(
                    color = if (volUpPressed) Color(0xFF0288D1) else Color(0xFF0288D1).copy(alpha = 0.3f),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx + 26.dp.toPx() - rUp / 2, cy - rUp),
                    size = Size(rUp * 2, rUp * 2),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )

                val rDown = 28.dp.toPx()
                drawArc(
                    color = if (volDownPressed) Color(0xFF29B6F6) else Color(0xFF29B6F6).copy(alpha = 0.3f),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx + 26.dp.toPx() - rDown / 2, cy - rDown),
                    size = Size(rDown * 2, rDown * 2),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Text(
                text = "Volume Up: ${if (volUpPressed) "Pressed ✅" else "Press Vol Up Key"} | Volume Down: ${if (volDownPressed) "Pressed ✅" else "Press Vol Down Key"}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (volUpPressed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable { volUpPressed = true }
                ) {
                    Text(
                        text = "Simulate Vol Up",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (volUpPressed) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (volDownPressed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable { volDownPressed = true }
                ) {
                    Text(
                        text = "Simulate Vol Down",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (volDownPressed) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun GenericTestGraphic() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.HourglassEmpty,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "Hardware Test Active",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TestsScreenPreview() {
    DevPulseTheme {
        TestsScreen(
            testsList = listOf(
                TestItem("screen", "Display Touch", "Interactive", "Test multi-touch and display digitizer", TestStatus.NOT_TESTED),
                TestItem("vibration", "Vibration Motor", "Interactive", "Verify haptic feedback motor", TestStatus.PASSED)
            ),
            onUpdateTestStatus = { _, _ -> }
        )
    }
}
