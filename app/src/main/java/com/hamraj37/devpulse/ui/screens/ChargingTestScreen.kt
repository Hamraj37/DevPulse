package com.hamraj37.devpulse.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.hamraj37.devpulse.util.tr
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun ChargingTestGraphic(context: Context) {
    var batteryLevel by remember { mutableIntStateOf(72) }
    var isCharging by remember { mutableStateOf(false) }
    var tempC by remember { mutableFloatStateOf(39f) }
    var voltageMv by remember { mutableIntStateOf(4078) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent != null) {
                    val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (rawLevel >= 0 && scale > 0) {
                        batteryLevel = (rawLevel * 100) / scale
                    }
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                    val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                    if (rawTemp > 0) {
                        tempC = rawTemp / 10f
                    }
                    val rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                    if (rawVoltage > 0) {
                        voltageMv = rawVoltage
                    }
                }
            }
        }

        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val sticky = context.registerReceiver(receiver, filter)
            sticky?.let { intent ->
                receiver.onReceive(context, intent)
            }
        } catch (_: Throwable) {}

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Throwable) {}
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(100.dp, 150.dp)) {
            val w = size.width
            val h = size.height

            drawRoundRect(
                color = if (isCharging) Color(0xFF66BB6A) else Color(0xFFFF7043),
                topLeft = Offset(w / 2f - 16.dp.toPx(), 0f),
                size = Size(32.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            drawRoundRect(
                color = if (isCharging) Color(0xFF66BB6A) else Color(0xFFFF7043),
                topLeft = Offset(0f, 10.dp.toPx()),
                size = Size(w, h - 10.dp.toPx()),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
            )

            if (isCharging) {
                val boltPath = Path().apply {
                    moveTo(w * 0.55f, h * 0.25f)
                    lineTo(w * 0.35f, h * 0.52f)
                    lineTo(w * 0.52f, h * 0.52f)
                    lineTo(w * 0.45f, h * 0.80f)
                    lineTo(w * 0.65f, h * 0.48f)
                    lineTo(w * 0.48f, h * 0.48f)
                    close()
                }
                drawPath(path = boltPath, color = Color(0xFFFFD54F))
            }
        }

        Text(
            text = if (isCharging) "Charging".tr(LocalContext.current) + " (Power Connected)" else "Connect your charger".tr(LocalContext.current),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isCharging) Color(0xFF2E7D32) else Color(0xFFD32F2F)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("Level".tr(LocalContext.current), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$batteryLevel%", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("Temperature".tr(LocalContext.current), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${String.format(Locale.US, "%.1f", tempC)} °C", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("Voltage".tr(LocalContext.current), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$voltageMv mV", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChargingTestScreen(
    onBack: () -> Unit,
    onPass: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StandardTestScreen(
        title = "Battery Charging",
        testId = "test_charging",
        onBack = onBack,
        onPass = onPass,
        onFail = onFail,
        modifier = modifier
    ) {
        ChargingTestGraphic(context)
    }
}
