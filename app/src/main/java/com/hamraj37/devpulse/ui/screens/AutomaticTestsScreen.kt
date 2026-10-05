package com.hamraj37.devpulse.ui.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.nfc.NfcManager
import android.os.SystemClock
import android.provider.Settings
import com.hamraj37.devpulse.util.tr
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AutoCheckItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val isPassed: Boolean,
    val icon: ImageVector,
    val actionIntent: Intent? = null
)

fun getAutoCheckItems(context: Context): List<AutoCheckItem> {
    val items = mutableListOf<AutoCheckItem>()

    // 1. Last Restart
    val uptimeMs = SystemClock.elapsedRealtime()
    val days = uptimeMs / (1000 * 60 * 60 * 24)
    items.add(
        AutoCheckItem(
            id = "last_restart",
            title = "Last Restart",
            subtitle = if (days < 7) "Your device was restarted recently" else "Device uptime: $days days",
            isPassed = true,
            icon = Icons.Rounded.Refresh
        )
    )

    // 2. USB Debugging
    val isUsbDebugging = try {
        Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
    } catch (_: Throwable) { false }

    items.add(
        AutoCheckItem(
            id = "usb_debugging",
            title = "USB Debugging",
            subtitle = if (isUsbDebugging)
                "USB Debugging is enabled. It is recommended to disable USB Debugging"
            else
                "USB Debugging is disabled",
            isPassed = !isUsbDebugging,
            icon = Icons.Rounded.Usb,
            actionIntent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        )
    )

    // 3. Screen Brightness
    val brightness = try {
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
    } catch (_: Throwable) { 128 }

    val isHighBrightness = brightness > 220
    items.add(
        AutoCheckItem(
            id = "screen_brightness",
            title = "Screen Brightness",
            subtitle = if (isHighBrightness)
                "Reduce your screen brightness to save battery"
            else
                "Screen brightness is optimized",
            isPassed = !isHighBrightness,
            icon = Icons.Rounded.BrightnessHigh,
            actionIntent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
        )
    )

    // 4. Screen Timeout
    val timeoutMs = try {
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 30000)
    } catch (_: Throwable) { 30000 }

    items.add(
        AutoCheckItem(
            id = "screen_timeout",
            title = "Screen Timeout",
            subtitle = if (timeoutMs <= 60000) "Screen timeout is on its best" else "Screen timeout is set to ${timeoutMs / 1000}s",
            isPassed = true,
            icon = Icons.Rounded.Smartphone
        )
    )

    // 5. Screen Lock
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    val isSecure = keyguardManager?.isDeviceSecure == true
    items.add(
        AutoCheckItem(
            id = "screen_lock",
            title = "Screen Lock",
            subtitle = if (isSecure) "Screen lock is configured successfully" else "Screen lock is not configured",
            isPassed = isSecure,
            icon = Icons.Rounded.Lock,
            actionIntent = if (!isSecure) Intent(Settings.ACTION_SECURITY_SETTINGS) else null
        )
    )

    // 6. NFC
    val nfcManager = context.getSystemService(Context.NFC_SERVICE) as? NfcManager
    val nfcAdapter = nfcManager?.defaultAdapter
    val isNfcEnabled = nfcAdapter?.isEnabled == true
    items.add(
        AutoCheckItem(
            id = "nfc",
            title = "NFC",
            subtitle = if (isNfcEnabled) "NFC is turned on" else "NFC is turned off",
            isPassed = true,
            icon = Icons.Rounded.Nfc
        )
    )

    return items
}

@Composable
fun SadFaceIcon(
    isSad: Boolean,
    modifier: Modifier = Modifier.size(80.dp)
) {
    val color = if (isSad) Color(0xFFFFA000) else Color(0xFF4CAF50)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = (minOf(w, h) / 2f) - 6.dp.toPx()
        val strokeWidth = 5.dp.toPx()

        drawCircle(
            color = color,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = strokeWidth)
        )

        val eyeRadius = 5.dp.toPx()
        val eyeOffsetY = cy - radius * 0.25f
        val eyeOffsetX = radius * 0.35f
        drawCircle(color = color, radius = eyeRadius, center = Offset(cx - eyeOffsetX, eyeOffsetY))
        drawCircle(color = color, radius = eyeRadius, center = Offset(cx + eyeOffsetX, eyeOffsetY))

        val mouthW = radius * 0.8f
        val mouthH = radius * 0.5f
        val mouthTopY = if (isSad) cy + radius * 0.2f else cy + radius * 0.05f
        val startAngle = if (isSad) 200f else 20f
        val sweepAngle = 140f

        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(cx - mouthW / 2f, mouthTopY),
            size = Size(mouthW, mouthH),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun AutomaticTestsScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    val checkItems = remember(context) { getAutoCheckItems(context) }
    val hasSuggestions = remember(checkItems) { checkItems.any { !it.isPassed } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = "Automatic Diagnostics",
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Mascot / Banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SadFaceIcon(isSad = hasSuggestions)

                Text(
                    text = if (hasSuggestions) "Suggestions are available".tr(context) else "All automatic tests passed".tr(context),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Auto Check Item Cards List
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                checkItems.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title.tr(context),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.subtitle.tr(context),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (!item.isPassed && item.actionIntent != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.clickable {
                                                try {
                                                    context.startActivity(item.actionIntent)
                                                } catch (_: Throwable) {}
                                            }
                                        ) {
                                            Text(
                                                text = "Check".tr(context),
                                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = if (item.isPassed) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                                contentDescription = if (item.isPassed) "Passed" else "Suggestion",
                                tint = if (item.isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
