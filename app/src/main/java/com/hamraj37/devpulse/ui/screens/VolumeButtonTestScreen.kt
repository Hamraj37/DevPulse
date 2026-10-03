package com.hamraj37.devpulse.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun VolumeButtonTestGraphic() {
    val focusRequester = remember { FocusRequester() }
    var volUpPressed by remember { mutableStateOf(false) }
    var volDownPressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {}
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.VolumeUp) {
                    volUpPressed = true
                    true
                } else if (keyEvent.key == Key.VolumeDown) {
                    volDownPressed = true
                    true
                } else {
                    false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Canvas(modifier = Modifier.size(220.dp, 180.dp)) {
                val w = size.width
                val h = size.height
                val cx = w * 0.32f
                val cy = h / 2f

                drawPath(
                    path = Path().apply {
                        moveTo(cx - 36.dp.toPx(), cy - 18.dp.toPx())
                        lineTo(cx - 10.dp.toPx(), cy - 18.dp.toPx())
                        lineTo(cx + 26.dp.toPx(), cy - 45.dp.toPx())
                        lineTo(cx + 26.dp.toPx(), cy + 45.dp.toPx())
                        lineTo(cx - 10.dp.toPx(), cy + 18.dp.toPx())
                        lineTo(cx - 36.dp.toPx(), cy + 18.dp.toPx())
                        close()
                    },
                    color = Color(0xFF78909C)
                )

                val rUp = 55.dp.toPx()
                drawArc(
                    color = if (volUpPressed) Color(0xFF0288D1) else Color(0xFF0288D1).copy(alpha = 0.3f),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx + 26.dp.toPx() - rUp / 2, cy - rUp),
                    size = Size(rUp * 2, rUp * 2),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )

                val rDown = 28.dp.toPx()
                drawArc(
                    color = if (volDownPressed) Color(0xFF29B6F6) else Color(0xFF29B6F6).copy(alpha = 0.3f),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx + 26.dp.toPx() - rDown / 2, cy - rDown),
                    size = Size(rDown * 2, rDown * 2),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Text(
                text = "Volume Up: ${if (volUpPressed) "Pressed ✅" else "Press Vol Up Key"} | Volume Down: ${if (volDownPressed) "Pressed ✅" else "Press Vol Down Key"}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (volUpPressed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable { volUpPressed = true }
                ) {
                    Text(
                        text = "Simulate Vol Up",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (volUpPressed) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (volDownPressed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable { volDownPressed = true }
                ) {
                    Text(
                        text = "Simulate Vol Down",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (volDownPressed) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun VolumeButtonTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardTestScreen(
        title = "Volume Keys",
        testId = "test_volume",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        VolumeButtonTestGraphic()
    }
}
