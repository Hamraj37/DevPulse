package com.hamraj37.devpulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.ui.theme.DevPulseTheme

@Composable
fun CircularRamGauge(
    percentage: Int,
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    strokeWidth: Dp = 5.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val strokePx = strokeWidth.toPx()
            val waveAmplitude = 4.dp.toPx()
            val radius = (size.toPx() - strokePx * 2 - waveAmplitude * 2) / 2f
            val numWaves = 8
            val totalSteps = 360
            val startAngle = -Math.PI / 2 // -90 degrees (12 o'clock)

            // 1. Draw smooth circular wavy track (full 360 degrees)
            val trackPath = Path()
            for (step in 0..totalSteps) {
                val progress = step.toFloat() / totalSteps
                val angle = startAngle + progress * 2 * Math.PI
                val wavePhase = progress * 2 * Math.PI * numWaves
                val waveOffset = waveAmplitude * Math.sin(wavePhase).toFloat()
                val currentRadius = radius + waveOffset

                val x = center.x + currentRadius * Math.cos(angle).toFloat()
                val y = center.y + currentRadius * Math.sin(angle).toFloat()

                if (step == 0) {
                    trackPath.moveTo(x, y)
                } else {
                    trackPath.lineTo(x, y)
                }
            }
            trackPath.close()

            drawPath(
                path = trackPath,
                color = trackColor,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 2. Draw active wavy progress arc
            val activeFraction = percentage.coerceIn(0, 100) / 100f
            if (activeFraction > 0f) {
                val activeSteps = (totalSteps * activeFraction).toInt().coerceAtLeast(1)
                val activePath = Path()
                var endPoint: Offset? = null

                for (step in 0..activeSteps) {
                    val progress = step.toFloat() / totalSteps
                    val angle = startAngle + progress * 2 * Math.PI
                    val wavePhase = progress * 2 * Math.PI * numWaves
                    val waveOffset = waveAmplitude * Math.sin(wavePhase).toFloat()
                    val currentRadius = radius + waveOffset

                    val x = center.x + currentRadius * Math.cos(angle).toFloat()
                    val y = center.y + currentRadius * Math.sin(angle).toFloat()

                    if (step == 0) {
                        activePath.moveTo(x, y)
                    } else {
                        activePath.lineTo(x, y)
                    }

                    if (step == activeSteps) {
                        endPoint = Offset(x, y)
                    }
                }

                drawPath(
                    path = activePath,
                    color = activeColor,
                    style = Stroke(width = strokePx * 1.25f, cap = StrokeCap.Round)
                )

                // 3. Draw active tip accent dot
                endPoint?.let { tip ->
                    drawCircle(
                        color = activeColor,
                        radius = strokePx * 0.9f,
                        center = tip
                    )
                }
            }
        }

        // Text inside gauge: Big number + small %
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$percentage",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
fun LiveSparklineChart(
    history: List<Float>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(54.dp),
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillGradient: Brush = remember(lineColor) {
        Brush.verticalGradient(
            colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)
        )
    }
) {
    Canvas(modifier = modifier.padding(vertical = 4.dp)) {
        if (history.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val minVal = (history.minOrNull() ?: 0f).coerceAtMost(0f)
        val maxVal = (history.maxOrNull() ?: 100f).coerceAtLeast(100f)
        val range = if (maxVal - minVal > 0) maxVal - minVal else 1f

        val stepX = width / (history.size - 1).coerceAtLeast(1)
        val path = Path()
        val fillPath = Path()

        var lastX = 0f
        var lastY = 0f

        history.forEachIndexed { index, valPct ->
            val x = index * stepX
            val normalizedY = (valPct - minVal) / range
            val y = height - (normalizedY * height * 0.85f) - (height * 0.05f)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val cx = (lastX + x) / 2f
                val cy = (lastY + y) / 2f
                path.quadraticTo(lastX, lastY, cx, cy)
                fillPath.quadraticTo(lastX, lastY, cx, cy)
            }
            lastX = x
            lastY = y
        }

        if (history.size > 1) {
            path.lineTo(lastX, lastY)
            fillPath.lineTo(lastX, lastY)
        }

        fillPath.lineTo(lastX, height)
        fillPath.close()

        // Draw Area Fill
        drawPath(
            path = fillPath,
            brush = fillGradient
        )

        // Draw Sparkline Path
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw latest point dot
        val lastPoint = Offset(lastX, lastY)
        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = lastPoint
        )
        drawCircle(
            color = lineColor,
            radius = 2.dp.toPx(),
            center = lastPoint
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CircularRamGaugePreview() {
    DevPulseTheme {
        CircularRamGauge(percentage = 65)
    }
}

@Preview(showBackground = true)
@Composable
fun LiveSparklineChartPreview() {
    DevPulseTheme {
        LiveSparklineChart(
            history = listOf(10f, 25f, 18f, 40f, 35f, 60f, 52f, 80f),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        )
    }
}