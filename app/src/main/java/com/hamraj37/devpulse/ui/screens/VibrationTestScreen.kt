package com.hamraj37.devpulse.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun VibrationTestGraphic(context: Context) {
    LaunchedEffect(Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Throwable) {}
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(40.dp, 160.dp)) {
            val h = size.height
            listOf(20.dp.toPx(), 35.dp.toPx()).forEach { xOff ->
                drawLine(
                    color = Color(0xFFAB47BC),
                    start = Offset(xOff, h * 0.2f),
                    end = Offset(xOff, h * 0.8f),
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        DisplayTestGraphic()

        Canvas(modifier = Modifier.size(40.dp, 160.dp)) {
            val h = size.height
            listOf(5.dp.toPx(), 20.dp.toPx()).forEach { xOff ->
                drawLine(
                    color = Color(0xFFAB47BC),
                    start = Offset(xOff, h * 0.2f),
                    end = Offset(xOff, h * 0.8f),
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun VibrationTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StandardTestScreen(
        title = "Vibration Motor",
        testId = "test_vibration",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        VibrationTestGraphic(context)
    }
}
