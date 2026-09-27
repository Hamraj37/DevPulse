package com.hamraj37.devpulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.ui.theme.OliveActiveBadge
import com.hamraj37.devpulse.ui.theme.OlivePrimary

@Composable
fun CircularRamGauge(
    percentage: Int,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp,
    activeColor: Color = OliveActiveBadge,
    trackColor: Color = Color(0xFF2C3227)
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            val strokeStyle = Stroke(width = strokePx, cap = StrokeCap.Round)

            // Background Track Arc (260 degrees sweep)
            drawArc(
                color = trackColor,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = strokeStyle
            )

            // Active Arc
            val sweep = (percentage.coerceIn(0, 100) / 100f) * 260f
            drawArc(
                color = activeColor,
                startAngle = 140f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = strokeStyle
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                ),
                color = Color.White
            )
            Text(
                text = "RAM",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.7f)
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
    lineColor: Color = OliveActiveBadge,
    fillGradient: Brush = remember {
        Brush.verticalGradient(
            colors = listOf(OliveActiveBadge.copy(alpha = 0.4f), Color.Transparent)
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
            color = Color.White,
            radius = 2.dp.toPx(),
            center = lastPoint
        )
    }
}
