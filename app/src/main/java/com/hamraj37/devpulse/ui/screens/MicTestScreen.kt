package com.hamraj37.devpulse.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

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

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Canvas(modifier = Modifier.size(160.dp, 200.dp)) {
            val cx = size.width / 2f
            val cy = size.height * 0.4f

            drawRoundRect(
                color = primary,
                topLeft = Offset(cx - 30.dp.toPx(), cy - 50.dp.toPx()),
                size = Size(60.dp.toPx(), 100.dp.toPx()),
                cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx())
            )

            listOf(-20.dp.toPx(), 0f, 20.dp.toPx()).forEach { yOffset ->
                drawLine(
                    color = secondary,
                    start = Offset(cx - 26.dp.toPx(), cy + yOffset),
                    end = Offset(cx + 26.dp.toPx(), cy + yOffset),
                    strokeWidth = 4.dp.toPx()
                )
            }

            drawArc(
                color = primary,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - 45.dp.toPx(), cy - 20.dp.toPx()),
                size = Size(90.dp.toPx(), 90.dp.toPx()),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )

            drawLine(
                color = primary,
                start = Offset(cx, cy + 70.dp.toPx()),
                end = Offset(cx, cy + 100.dp.toPx()),
                strokeWidth = 10.dp.toPx()
            )

            drawLine(
                color = primary,
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
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

@Composable
fun MicTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardTestScreen(
        title = "Microphone",
        testId = "test_mic",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        MicTestGraphic()
    }
}
