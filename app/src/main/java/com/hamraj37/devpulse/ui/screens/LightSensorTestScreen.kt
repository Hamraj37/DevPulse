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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
fun LightSensorTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StandardTestScreen(
        title = "Ambient Light Sensor",
        testId = "test_light",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        LightSensorTestGraphic(context)
    }
}
