package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.nfc.NfcManager
import android.os.SystemClock
import android.provider.Settings
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
            title = context.getString(R.string.auto_check_last_restart_title),
            subtitle = if (days < 7) context.getString(R.string.auto_check_last_restart_ok) else context.getString(R.string.auto_check_last_restart_uptime, days),
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
            title = context.getString(R.string.auto_check_usb_debugging_title),
            subtitle = if (isUsbDebugging)
                context.getString(R.string.auto_check_usb_debugging_warn)
            else
                context.getString(R.string.auto_check_usb_debugging_ok),
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
            title = context.getString(R.string.auto_check_brightness_title),
            subtitle = if (isHighBrightness)
                context.getString(R.string.auto_check_brightness_warn)
            else
                context.getString(R.string.auto_check_brightness_ok),
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
            title = context.getString(R.string.auto_check_timeout_title),
            subtitle = if (timeoutMs <= 60000) context.getString(R.string.auto_check_timeout_ok) else context.getString(R.string.auto_check_timeout_sec, timeoutMs / 1000),
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
            title = context.getString(R.string.auto_check_lock_title),
            subtitle = if (isSecure) context.getString(R.string.auto_check_lock_ok) else context.getString(R.string.auto_check_lock_warn),
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
            title = context.getString(R.string.auto_check_nfc_title),
            subtitle = if (isNfcEnabled) context.getString(R.string.auto_check_nfc_on) else context.getString(R.string.auto_check_nfc_off),
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
    val color = if (isSad) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
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
                title = stringResource(R.string.tests_auto_diagnostics),
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Status Mascot Banner Card (matches DashboardScreen)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SadFaceIcon(isSad = hasSuggestions)

                    Text(
                        text = if (hasSuggestions) stringResource(R.string.tests_suggestions_available) else stringResource(R.string.tests_all_passed),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Auto Check Item Cards List
            checkItems.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.subtitle,
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
                                            text = stringResource(R.string.btn_check),
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
                            contentDescription = null,
                            tint = if (item.isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
