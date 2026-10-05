package com.hamraj37.devpulse.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal fun playSystemRingtone(context: Context, isEarpiece: Boolean): Ringtone? {
    return try {
        val ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        if (ringtone != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val usage = if (isEarpiece) AudioAttributes.USAGE_VOICE_COMMUNICATION else AudioAttributes.USAGE_MEDIA
                val contentType = if (isEarpiece) AudioAttributes.CONTENT_TYPE_SPEECH else AudioAttributes.CONTENT_TYPE_MUSIC
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(contentType)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                ringtone.streamType = if (isEarpiece) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC
            }
            ringtone.play()
        }
        ringtone
    } catch (_: Throwable) {
        null
    }
}

@Composable
fun LoudspeakerTestGraphic(context: Context) {
    var activeRingtone by remember { mutableStateOf<Ringtone?>(null) }

    DisposableEffect(context) {
        activeRingtone = playSystemRingtone(context, isEarpiece = false)

        onDispose {
            try {
                if (activeRingtone?.isPlaying == true) {
                    activeRingtone?.stop()
                }
            } catch (_: Throwable) {}
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(180.dp, 220.dp)) {
            val w = size.width
            val h = size.height
            val corner = 20.dp.toPx()

            drawRoundRect(
                color = primary,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(corner, corner)
            )

            val screwRadius = 5.dp.toPx()
            val margin = 14.dp.toPx()
            listOf(
                Offset(margin, margin),
                Offset(w - margin, margin),
                Offset(margin, h - margin),
                Offset(w - margin, h - margin)
            ).forEach {
                drawCircle(color = secondary, radius = screwRadius, center = it)
            }

            val topCenter = Offset(w / 2f, h * 0.3f)
            drawCircle(color = secondary, radius = 28.dp.toPx(), center = topCenter)
            drawCircle(color = surfaceContainer, radius = 18.dp.toPx(), center = topCenter)
            drawCircle(color = onSurface, radius = 8.dp.toPx(), center = topCenter)

            val bottomCenter = Offset(w / 2f, h * 0.7f)
            drawCircle(color = secondary, radius = 48.dp.toPx(), center = bottomCenter)
            drawCircle(color = surfaceContainer, radius = 32.dp.toPx(), center = bottomCenter)
            drawCircle(color = onSurface, radius = 14.dp.toPx(), center = bottomCenter)
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.clickable {
                try {
                    if (activeRingtone?.isPlaying == true) {
                        activeRingtone?.stop()
                    }
                    activeRingtone = playSystemRingtone(context, isEarpiece = false)
                } catch (_: Throwable) {}
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Play System Ringtone Again",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun SpeakerTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StandardTestScreen(
        title = "Loudspeaker",
        testId = "test_speaker",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        LoudspeakerTestGraphic(context)
    }
}
