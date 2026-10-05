package com.hamraj37.devpulse.ui.screens

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun FlashlightTestGraphic(context: Context) {
    DisposableEffect(Unit) {
        try {
            val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val camId = camMgr?.cameraIdList?.firstOrNull()
            if (camMgr != null && camId != null) {
                camMgr.setTorchMode(camId, true)
            }
        } catch (_: Throwable) {}

        onDispose {
            try {
                val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                val camId = camMgr?.cameraIdList?.firstOrNull()
                if (camMgr != null && camId != null) {
                    camMgr.setTorchMode(camId, false)
                }
            } catch (_: Throwable) {}
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            withTransform({
                rotate(45f, pivot = Offset(cx, cy))
            }) {
                drawPath(
                    path = Path().apply {
                        moveTo(cx - 30.dp.toPx(), cy - 40.dp.toPx())
                        lineTo(cx + 30.dp.toPx(), cy - 40.dp.toPx())
                        lineTo(cx + 60.dp.toPx(), cy - 100.dp.toPx())
                        lineTo(cx - 60.dp.toPx(), cy - 100.dp.toPx())
                        close()
                    },
                    color = primaryContainer
                )
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(cx - 30.dp.toPx(), cy - 40.dp.toPx()),
                    size = Size(60.dp.toPx(), 140.dp.toPx()),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
                drawRoundRect(
                    color = tertiary,
                    topLeft = Offset(cx - 8.dp.toPx(), cy + 10.dp.toPx()),
                    size = Size(16.dp.toPx(), 32.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun FlashlightTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StandardTestScreen(
        title = "Flashlight",
        testId = "test_flashlight",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        FlashlightTestGraphic(context)
    }
}
