package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun EarProximityTestGraphic(context: Context) {
    var isNear by remember { mutableStateOf(false) }
    var rawDistance by remember { mutableFloatStateOf(5.0f) }
    var hasHardwareSensor by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        var listener: SensorEventListener? = null
        var sensorManager: SensorManager? = null
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val proximity = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

            if (proximity != null) {
                val maxRange = proximity.maximumRange
                listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        try {
                            if (event != null && event.values.isNotEmpty()) {
                                val val0 = event.values[0]
                                rawDistance = val0
                                isNear = val0 == 0.0f || val0 < 3.0f || (maxRange > 0f && val0 < maxRange)
                            }
                        } catch (_: Throwable) {}
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                sensorManager.registerListener(listener, proximity, SensorManager.SENSOR_DELAY_UI)
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

    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.clickable {
            isNear = !isNear
            rawDistance = if (isNear) 0.0f else 5.0f
        }
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            val pulseColor1 = if (isNear) primary.copy(alpha = 0.2f) else primaryContainer.copy(alpha = 0.3f)
            val pulseColor2 = if (isNear) primary.copy(alpha = 0.5f) else primaryContainer.copy(alpha = 0.6f)
            val centerColor = if (isNear) primary else primaryContainer

            drawCircle(
                color = pulseColor1,
                radius = 90.dp.toPx(),
                center = Offset(cx, cy)
            )

            drawCircle(
                color = pulseColor2,
                radius = 60.dp.toPx(),
                center = Offset(cx, cy)
            )

            drawCircle(
                color = centerColor,
                radius = 24.dp.toPx(),
                center = Offset(cx, cy)
            )

            drawPath(
                path = Path().apply {
                    moveTo(cx - 70.dp.toPx(), cy + 10.dp.toPx())
                    lineTo(cx - 30.dp.toPx(), cy - 50.dp.toPx())
                    lineTo(cx, cy + 40.dp.toPx())
                    lineTo(cx + 40.dp.toPx(), cy - 10.dp.toPx())
                },
                color = if (isNear) onPrimary else onPrimaryContainer,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isNear) primary else primaryContainer
        ) {
            Text(
                text = if (isNear) "NEAR (Sensor Covered ✅)" else "FAR (Sensor Uncovered)",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isNear) onPrimary else onPrimaryContainer
            )
        }

        Text(
            text = "Distance Reading: ${String.format(Locale.US, "%.1f", rawDistance)} cm" +
                    if (!hasHardwareSensor) " (Tap graphic to simulate)" else "",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
