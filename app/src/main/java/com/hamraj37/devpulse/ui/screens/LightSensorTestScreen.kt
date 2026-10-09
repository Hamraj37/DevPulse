package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
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
    var lux by remember { mutableFloatStateOf(120f) }
    var hasHardwareSensor by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        var listener: SensorEventListener? = null
        var sensorManager: SensorManager? = null
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

            if (lightSensor != null) {
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

                sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
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

    val lightCategory = when {
        lux < 10f -> "Dark / Sensor Covered"
        lux < 100f -> "Dim Indoor Light"
        lux < 500f -> "Normal Indoor Lighting"
        else -> "Bright Direct Light"
    }

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val outline = MaterialTheme.colorScheme.outline
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHighest

    val sunGlowColor = when {
        lux < 10f -> outline
        lux < 100f -> secondary
        lux < 500f -> primary
        else -> tertiary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.clickable {
            lux = when {
                lux < 10f -> 150f
                lux < 500f -> 800f
                else -> 2f
            }
        }
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = sunGlowColor,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 14.dp.toPx())
            )

            drawRoundRect(
                color = surfaceContainer,
                topLeft = Offset(16.dp.toPx(), 16.dp.toPx()),
                size = Size(w - 32.dp.toPx(), h - 32.dp.toPx()),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            val cx = w / 2f
            val cy = h / 2f
            val size = 48.dp.toPx()

            withTransform({
                rotate(0f, Offset(cx, cy))
            }) {
                drawRoundRect(
                    color = sunGlowColor.copy(alpha = 0.8f),
                    topLeft = Offset(cx - size, cy - size),
                    size = Size(size * 2, size * 2),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            withTransform({
                rotate(45f, Offset(cx, cy))
            }) {
                drawRoundRect(
                    color = sunGlowColor,
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
                fontSize = 36.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = lightCategory,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        if (!hasHardwareSensor) {
            Text(
                text = "(Tap graphic to cycle simulated lux)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
        title = stringResource(R.string.test_item_light_title),
        testId = "test_light",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        LightSensorTestGraphic(context)
    }
}
