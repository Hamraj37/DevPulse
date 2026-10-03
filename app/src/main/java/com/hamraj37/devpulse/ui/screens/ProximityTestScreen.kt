package com.hamraj37.devpulse.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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
fun ProximityTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StandardTestScreen(
        title = "Ear Proximity Sensor",
        testId = "test_proximity",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        EarProximityTestGraphic(context)
    }
}
