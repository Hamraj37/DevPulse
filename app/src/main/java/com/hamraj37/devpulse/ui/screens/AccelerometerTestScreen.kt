package com.hamraj37.devpulse.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
