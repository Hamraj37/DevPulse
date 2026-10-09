package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MultitouchTestContent() {
    val pointers = remember { mutableStateMapOf<Int, Offset>() }

    val c1 = MaterialTheme.colorScheme.primary
    val c2 = MaterialTheme.colorScheme.secondary
    val c3 = MaterialTheme.colorScheme.tertiary
    val c4 = MaterialTheme.colorScheme.primaryContainer
    val c5 = MaterialTheme.colorScheme.secondaryContainer
    val c6 = MaterialTheme.colorScheme.tertiaryContainer
    val colors = listOf(c1, c2, c3, c4, c5, c6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                try {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            pointers.clear()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    pointers[change.id.value.toInt()] = change.position
                                }
                            }
                        }
                    }
                } catch (_: Throwable) {}
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            pointers.values.forEachIndexed { i, pos ->
                val color = colors[i % colors.size]
                drawCircle(color = color.copy(alpha = 0.3f), radius = 100f, center = pos)
                drawCircle(color = color, radius = 50f, center = pos)
            }
        }
        if (pointers.isEmpty()) {
            Text(
                text = stringResource(R.string.test_multitouch_prompt),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun MultitouchTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardTestScreen(
        title = stringResource(R.string.test_item_touch_title),
        testId = "test_touch",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier,
        isDarkBackground = true
    ) {
        MultitouchTestContent()
    }
}
