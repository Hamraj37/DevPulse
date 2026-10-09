package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun CompassDialView(
    azimuthDegree: Float,
    modifier: Modifier = Modifier
) {
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuthDegree,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "azimuthRotation"
    )

    Box(
        modifier = modifier.size(310.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotatable Dial (Outer Ring + Ticks + Concentric Circles)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = -animatedAzimuth
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 30f

            // Outer Circle Ring
            drawCircle(
                color = Color(0xFF6B6E5F),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner Concentric Ring 1
            drawCircle(
                color = Color(0xFF6B6E5F),
                radius = radius * 0.75f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner Dashed Ring 2
            drawCircle(
                color = Color(0xFF6B6E5F),
                radius = radius * 0.55f,
                center = center,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            // Inner Solid Ring 3
            drawCircle(
                color = Color(0xFF6B6E5F),
                radius = radius * 0.32f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Ticks around outer edge
            for (i in 0 until 360 step 2) {
                val angleRad = Math.toRadians(i.toDouble() - 90)
                val isMajor30 = i % 30 == 0
                val isMedium10 = i % 10 == 0

                val tickLength = when {
                    isMajor30 -> 16.dp.toPx()
                    isMedium10 -> 10.dp.toPx()
                    else -> 6.dp.toPx()
                }

                val strokeWidth = when {
                    isMajor30 -> 2.dp.toPx()
                    isMedium10 -> 1.5.dp.toPx()
                    else -> 1.dp.toPx()
                }

                val startX = center.x + (radius - tickLength) * Math.cos(angleRad).toFloat()
                val startY = center.y + (radius - tickLength) * Math.sin(angleRad).toFloat()
                val endX = center.x + radius * Math.cos(angleRad).toFloat()
                val endY = center.y + radius * Math.sin(angleRad).toFloat()

                drawLine(
                    color = Color(0xFF6B6E5F),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth
                )
            }
        }

        // Cardinal & Intercardinal Text Labels rotating with dial
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = -animatedAzimuth
                }
        ) {
            val directions = listOf(
                "N" to 0f, "NE" to 45f, "E" to 90f, "SE" to 135f,
                "S" to 180f, "SW" to 225f, "W" to 270f, "NW" to 315f
            )

            directions.forEach { (label, angle) ->
                val angleRad = Math.toRadians(angle.toDouble() - 90)
                val offsetX = (130 * Math.cos(angleRad)).dp
                val offsetY = (130 * Math.sin(angleRad)).dp

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = offsetX, y = offsetY)
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontSize = if (label.length == 1) 16.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF53564A)
                        )
                    )
                }
            }
        }

        // Center Dual Pointer Needle (Red North / Blue South)
        Canvas(
            modifier = Modifier.size(230.dp)
        ) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val needleLength = size.width / 2f - 22f
            val needleWidth = 16f

            // Blue Pointer
            val northPath = Path().apply {
                moveTo(c.x - needleLength, c.y)
                lineTo(c.x, c.y - needleWidth)
                lineTo(c.x, c.y + needleWidth)
                close()
            }
            drawPath(
                path = northPath,
                color = Color(0xFF0288D1)
            )

            // Red Pointer
            val southPath = Path().apply {
                moveTo(c.x + needleLength, c.y)
                lineTo(c.x, c.y - needleWidth)
                lineTo(c.x, c.y + needleWidth)
                close()
            }
            drawPath(
                path = southPath,
                color = Color(0xFFD32F2F)
            )

            // Center Pivot Ring
            drawCircle(
                color = Color(0xFF53564A),
                radius = 16f,
                center = c
            )
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = 10f,
                center = c
            )
            drawCircle(
                color = Color(0xFF424242),
                radius = 5f,
                center = c
            )
        }
    }
}

@Composable
fun CompassScreen(
    context: Context = LocalContext.current,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    var azimuthDegree by remember { mutableFloatStateOf(0f) }
    var cardinalDirection by remember { mutableStateOf("N") }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var gravity: FloatArray? = null
        var geomagnetic: FloatArray? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    gravity = event.values.clone()
                }
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    geomagnetic = event.values.clone()
                }

                if (gravity != null && geomagnetic != null) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val azRad = orientation[0]
                        var deg = Math.toDegrees(azRad.toDouble()).toFloat()
                        if (deg < 0) deg += 360f

                        azimuthDegree = deg
                        cardinalDirection = when (deg) {
                            in 22.5f..67.5f -> "NE"
                            in 67.5f..112.5f -> "E"
                            in 112.5f..157.5f -> "SE"
                            in 157.5f..202.5f -> "S"
                            in 202.5f..247.5f -> "SW"
                            in 247.5f..292.5f -> "W"
                            in 292.5f..337.5f -> "NW"
                            else -> "N"
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null) {
            accelerometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = stringResource(R.string.compass),
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
        // Degree and Direction Heading Text
        Text(
            text = "${azimuthDegree.roundToInt()}° $cardinalDirection",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF53564A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Compass Dial Canvas View
        CompassDialView(azimuthDegree = azimuthDegree)

        Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
