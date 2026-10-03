package com.hamraj37.devpulse.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DisplayTestGraphic() {
    Canvas(modifier = Modifier.size(160.dp, 280.dp)) {
        val w = size.width
        val h = size.height
        val corner = 24.dp.toPx()

        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, w, h),
                    cornerRadius = CornerRadius(corner, corner)
                )
            )
        }
        clipPath(path) {
            val bandHeight = h / 4f
            val colors = listOf(
                Color(0xFF29B6F6),
                Color(0xFF03A9F4),
                Color(0xFF0288D1),
                Color(0xFF01579B)
            )
            colors.forEachIndexed { i, color ->
                drawRect(
                    color = color,
                    topLeft = Offset(0f, i * bandHeight),
                    size = Size(w, bandHeight)
                )
            }
            drawRoundRect(
                color = Color(0xFF01579B),
                topLeft = Offset(w / 2f - 24.dp.toPx(), 12.dp.toPx()),
                size = Size(48.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
        }
    }
}

@Composable
fun DisplayTestFullscreen(
    onPass: () -> Unit,
    onFail: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
    val colorNames = listOf("Red", "Green", "Blue", "White", "Black")
    var colorIndex by remember { mutableIntStateOf(0) }
    var isTestComplete by remember { mutableStateOf(false) }

    if (!isTestComplete) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors[colorIndex])
                .clickable {
                    if (colorIndex < colors.size - 1) {
                        colorIndex++
                    } else {
                        isTestComplete = true
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 56.dp)
            ) {
                Text(
                    text = "Tap to cycle color: ${colorNames[colorIndex]} (${colorIndex + 1}/${colors.size})",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    } else {
        StandardTestScreen(
            title = "Display",
            testId = "test_display",
            onBack = onDismiss,
            onPass = onPass,
            onFail = onFail
        ) {
            DisplayTestGraphic()
        }
    }
}

@Composable
fun DisplayTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    DisplayTestFullscreen(
        onPass = onPass,
        onFail = onFail,
        onDismiss = onBack
    )
}
