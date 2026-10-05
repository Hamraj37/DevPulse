package com.hamraj37.devpulse.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AccelerometerTestGraphic(context: Context) {
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(9.8f) }
    var motionDetected by remember { mutableStateOf(false) }
    var hasHardwareSensor by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        var listener: SensorEventListener? = null
        var sensorManager: SensorManager? = null
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            if (accel != null) {
                listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        try {
                            if (event != null && event.values.size >= 3) {
                                val x = event.values[0]
                                val y = event.values[1]
                                val z = event.values[2]
                                accelX = x
                                accelY = y
                                accelZ = z

                                val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble())
                                if (Math.abs(magnitude - 9.8) > 2.5) {
                                    motionDetected = true
                                }
                            }
                        } catch (_: Throwable) {}
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)
            } else {
                hasHardwareSensor = false
            }
        } catch (_: Throwable) {
            hasHardwareSensor = false
        }

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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.clickable {
            accelX = if (accelX == 0f) 4.5f else 0f
            accelY = if (accelY == 0f) -3.2f else 0f
            motionDetected = true
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    @Suppress("DEPRECATION")
                    v?.vibrate(100)
                }
            } catch (_: Throwable) {}
        }
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary

        Canvas(modifier = Modifier.size(200.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerRadius = size.width / 2f - 10f

            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = outerRadius
            )
            drawCircle(
                color = primaryColor,
                radius = outerRadius,
                style = Stroke(width = 4.dp.toPx())
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.3f),
                radius = outerRadius / 2f,
                style = Stroke(width = 2.dp.toPx())
            )

            val ballX = (cx - (accelX * 12f)).coerceIn(24.dp.toPx(), size.width - 24.dp.toPx())
            val ballY = (cy + (accelY * 12f)).coerceIn(24.dp.toPx(), size.height - 24.dp.toPx())

            drawCircle(
                color = if (motionDetected) primaryColor else tertiaryColor,
                radius = 20.dp.toPx(),
                center = Offset(ballX, ballY)
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (motionDetected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = if (motionDetected) "Motion / Shake Detected! ✅" else "Shake or tilt your device",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (motionDetected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
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
fun AccelerometerTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StandardTestScreen(
        title = "Accelerometer Sensor",
        testId = "test_accel",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        AccelerometerTestGraphic(context)
    }
}
