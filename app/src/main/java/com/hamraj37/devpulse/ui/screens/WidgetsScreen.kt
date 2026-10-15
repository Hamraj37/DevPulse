package com.hamraj37.devpulse.ui.screens

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.ui.MainUiState
import com.hamraj37.devpulse.widget.DevPulseCompactWidgetProvider
import com.hamraj37.devpulse.widget.DevPulseDashboardWidgetProvider
import com.hamraj37.devpulse.widget.DevPulseMinimalWidgetProvider
import com.hamraj37.devpulse.widget.DevPulseStorageWidgetProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WidgetsScreen(
    uiState: MainUiState,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current

    fun requestPinWidget(providerClass: Class<*>, widgetName: String) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val myWidget = ComponentName(context, providerClass)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val successCallback = PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(context, providerClass),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    appWidgetManager.requestPinAppWidget(myWidget, null, successCallback)
                    Toast.makeText(context, context.getString(R.string.toast_widget_pin_request_sent), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_widget_long_press_home), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, context.getString(R.string.toast_widget_add_from_picker), Toast.LENGTH_LONG).show()
            }
        } catch (e: Throwable) {
            try {
                Toast.makeText(context, context.getString(R.string.toast_unable_request_pin, e.message ?: ""), Toast.LENGTH_SHORT).show()
            } catch (_: Throwable) {}
        }
    }

    // Live Telemetry Computations for Widget Preview
    val storageTotalBytes = (uiState.dashboardInfo.storageTotalBytes).coerceAtLeast(1L)
    val storageUsedBytes = (uiState.dashboardInfo.storageUsedBytes).coerceAtLeast(0L)
    val storageFreeBytes = (storageTotalBytes - storageUsedBytes).coerceAtLeast(0L)
    val storageFreeGbText = try {
        String.format(Locale.US, "%.2fG", storageFreeBytes / (1024.0 * 1024.0 * 1024.0))
    } catch (_: Throwable) { "0.00G" }
    val storageUsedPct = try {
        ((storageUsedBytes.toFloat() / storageTotalBytes.toFloat()) * 100).toInt().coerceIn(0, 100)
    } catch (_: Throwable) { 0 }
    val safeStorageProgress = (storageUsedPct / 100f).let { if (it.isNaN() || it.isInfinite()) 0f else it.coerceIn(0f, 1f) }

    val ramTotalBytes = (uiState.dashboardInfo.ramTotalBytes).coerceAtLeast(1L)
    val ramUsedBytes = (uiState.dashboardInfo.ramUsedBytes).coerceAtLeast(0L)
    val ramUsedPct = try {
        ((ramUsedBytes.toFloat() / ramTotalBytes.toFloat()) * 100).toInt().coerceIn(0, 100)
    } catch (_: Throwable) { 0 }
    val ramUsedGbText = try {
        String.format(Locale.US, "%.1fGB Used", ramUsedBytes / (1024.0 * 1024.0 * 1024.0))
    } catch (_: Throwable) { "0.0GB Used" }

    val batteryLevel = (uiState.batteryInfo.levelPercent).coerceIn(0, 100)
    val batteryTemp = uiState.batteryInfo.temperatureCelsius

    val formattedDate = remember {
        try {
            SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
        } catch (_: Throwable) { "Today" }
    }

    val (brandIconRes, modelName) = remember {
        try {
            val brand = Build.MANUFACTURER.lowercase()
            val brandIcon = when {
                brand.contains("oneplus") -> R.drawable.ic_brand_oneplus
                brand.contains("samsung") -> R.drawable.ic_brand_samsung
                brand.contains("google") -> R.drawable.ic_brand_google
                brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("mi ") -> R.drawable.ic_brand_xiaomi
                brand.contains("realme") -> R.drawable.ic_brand_realme
                brand.contains("oppo") -> R.drawable.ic_brand_oppo
                brand.contains("vivo") || brand.contains("iqoo") -> R.drawable.ic_brand_vivo
                brand.contains("motorola") || brand.contains("moto") -> R.drawable.ic_brand_motorola
                brand.contains("nothing") -> R.drawable.ic_brand_nothing
                brand.contains("asus") -> R.drawable.ic_brand_asus
                brand.contains("sony") -> R.drawable.ic_brand_sony
                else -> R.drawable.ic_brand_android
            }
            val rawModel = Build.MODEL
            val cleanModel = if (rawModel.startsWith(Build.MANUFACTURER, ignoreCase = true)) {
                rawModel.substring(Build.MANUFACTURER.length).trim()
            } else rawModel
            Pair(brandIcon, if (cleanModel.isNotBlank()) cleanModel else "Device")
        } catch (_: Throwable) {
            Pair(R.drawable.ic_brand_android, "Device")
        }
    }

    val (socVendor, socIconRes) = remember(uiState.cpuInfo) {
        try {
            val hw = uiState.cpuInfo.hardwareName ?: ""
            val proc = uiState.cpuInfo.processorName ?: ""
            val combined = "$hw $proc".lowercase()
            val vendor = when {
                combined.contains("snapdragon") || combined.contains("qualcomm") || combined.contains("qcom") || combined.contains("sm8") -> "Qualcomm®"
                combined.contains("mediatek") || combined.contains("mt") || combined.contains("dimensity") -> "MediaTek®"
                combined.contains("tensor") || combined.contains("google") || combined.contains("gs") -> "Google"
                combined.contains("exynos") || combined.contains("samsung") -> "Samsung"
                combined.contains("unisoc") || combined.contains("spreadtrum") -> "UNISOC"
                else -> "ARM®"
            }
            val icon = when {
                combined.contains("snapdragon") || combined.contains("qualcomm") || combined.contains("qcom") || combined.contains("sm8") -> R.drawable.ic_proc_snapdragon
                combined.contains("mediatek") || combined.contains("mt") || combined.contains("dimensity") -> R.drawable.ic_proc_mediatek
                combined.contains("tensor") || combined.contains("google") || combined.contains("gs") -> R.drawable.ic_proc_tensor
                combined.contains("exynos") || combined.contains("samsung") -> R.drawable.ic_proc_exynos
                combined.contains("unisoc") || combined.contains("spreadtrum") -> R.drawable.ic_proc_unisoc
                else -> R.drawable.ic_proc_arm
            }
            Pair(vendor, icon)
        } catch (_: Throwable) {
            Pair("ARM®", R.drawable.ic_proc_arm)
        }
    }

    val androidLogoRes = remember {
        try {
            val sdkInt = Build.VERSION.SDK_INT
            when (sdkInt) {
                Build.VERSION_CODES.Q -> R.drawable.android_10
                Build.VERSION_CODES.R -> R.drawable.android_11
                Build.VERSION_CODES.S, Build.VERSION_CODES.S_V2 -> R.drawable.android_12
                Build.VERSION_CODES.TIRAMISU -> R.drawable.android_13
                Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> R.drawable.android_14
                Build.VERSION_CODES.VANILLA_ICE_CREAM -> R.drawable.android_15
                36 -> R.drawable.android_16
                else -> if (sdkInt >= 37) R.drawable.android_17 else R.drawable.ic_brand_android
            }
        } catch (_: Throwable) {
            R.drawable.ic_brand_android
        }
    }

    val (wifiActive, dataActive) = remember(context) {
        var wifi = false
        var data = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val activeNetwork = cm.activeNetwork
                val caps = cm.getNetworkCapabilities(activeNetwork)
                if (caps != null) {
                    wifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    data = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                }
            }
        } catch (_: Throwable) {}
        Pair(wifi, data)
    }

    val bluetoothActive = remember(context) {
        try {
            BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
        } catch (_: Throwable) { false }
    }

    val locationActive = remember(context) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        } catch (_: Throwable) { false }
    }

    val isDark = isSystemInDarkTheme()
    val outerBg = if (isDark) Color(0xFF181A1D) else Color(0xFFEBECEE)
    val cardBg = if (isDark) Color(0xFF25282C) else Color.White
    val mintBg = if (isDark) Color(0xFF1A3224) else Color(0xFFEAF4ED)
    val activeGreen = if (isDark) Color(0xFF57C28B) else Color(0xFF3B7E5A)
    val inactiveGrey = if (isDark) Color(0xFF6C7278) else Color(0xFFA0A0A0)
    val trackColor = if (isDark) Color(0xFF3A3E42) else Color(0xFFE0E4E1)
    val textPrimary = if (isDark) Color(0xFFF0F2F5) else Color(0xFF1D1D1D)
    val textSecondary = if (isDark) Color(0xFFA0A5AB) else Color(0xFF666666)
    val textSocVendor = if (isDark) Color(0xFFB5D8C3) else Color(0xFF333333)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = stringResource(R.string.widgets_title),
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

            Text(
                text = stringResource(R.string.widgets_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Showcase Container
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. Control Dashboard Widget (4x2 Flagship)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = outerBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { requestPinWidget(DevPulseDashboardWidgetProvider::class.java, "Control Dashboard Widget") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.widget_dashboard_preview),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = activeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = activeGreen
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_pin),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Column 1: Status Icons & Battery
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = cardBg,
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_widget_wifi),
                                                    contentDescription = "Wi-Fi",
                                                    tint = if (wifiActive) activeGreen else inactiveGrey,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = cardBg,
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_widget_mobile_data),
                                                    contentDescription = "Mobile Data",
                                                    tint = if (dataActive) activeGreen else inactiveGrey,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = cardBg,
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_widget_bluetooth),
                                                    contentDescription = "Bluetooth",
                                                    tint = if (bluetoothActive) activeGreen else inactiveGrey,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = cardBg,
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_widget_location),
                                                    contentDescription = "Location",
                                                    tint = if (locationActive) activeGreen else inactiveGrey,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = cardBg,
                                        modifier = Modifier.fillMaxWidth().height(42.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(horizontal = 10.dp)
                                        ) {
                                            WidgetRingGauge(
                                                pct = batteryLevel,
                                                iconRes = R.drawable.ic_widget_battery_icon,
                                                activeColor = activeGreen,
                                                trackColor = trackColor,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Text(
                                                text = "$batteryLevel%",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary
                                            )
                                        }
                                    }
                                }

                                // Column 2: Date, SoC, OS
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = cardBg,
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                            Text(
                                                text = formattedDate,
                                                fontSize = 10.sp,
                                                color = textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = mintBg,
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = socIconRes),
                                                contentDescription = "SoC Logo",
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = (uiState.cpuInfo.processorName ?: "").ifEmpty { "Processor" },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = cardBg,
                                        modifier = Modifier.fillMaxWidth().height(42.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = androidLogoRes),
                                                contentDescription = "Android Logo",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "Android ${Build.VERSION.RELEASE}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary
                                            )
                                        }
                                    }
                                }

                                // Column 3: Storage Free & Model
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = cardBg,
                                        modifier = Modifier.fillMaxWidth().height(82.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Storage Free",
                                                    fontSize = 10.sp,
                                                    color = textSecondary
                                                )
                                                Text(
                                                    text = storageFreeGbText,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textPrimary
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { safeStorageProgress },
                                                color = activeGreen,
                                                trackColor = trackColor,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = cardBg,
                                        modifier = Modifier.fillMaxWidth().height(42.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = brandIconRes),
                                                contentDescription = "Brand Logo",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = modelName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Compact RAM & CPU Gauge Widget (2x2)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = outerBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { requestPinWidget(DevPulseCompactWidgetProvider::class.java, "Compact RAM & CPU Widget") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RAM & CPU Dual Gauge (2x2)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = activeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = activeGreen
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_pin),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // RAM Card
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = cardBg,
                                    modifier = Modifier.weight(1f).height(120.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Text("RAM Memory", fontSize = 11.sp, color = textSecondary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        WidgetRingGauge(
                                            pct = ramUsedPct,
                                            iconRes = R.drawable.ic_widget_ram,
                                            activeColor = activeGreen,
                                            trackColor = trackColor,
                                            modifier = Modifier.size(42.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("$ramUsedPct%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                        Text(ramUsedGbText, fontSize = 10.sp, color = textSecondary)
                                    }
                                }

                                // CPU Card
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = mintBg,
                                    modifier = Modifier.weight(1f).height(120.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Text("CPU Processor", fontSize = 11.sp, color = textSocVendor)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        WidgetRingGauge(
                                            pct = 45,
                                            iconRes = socIconRes,
                                            activeColor = activeGreen,
                                            trackColor = trackColor,
                                            modifier = Modifier.size(42.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("45%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                        Text((uiState.cpuInfo.processorName ?: "").ifEmpty { "Processor" }, fontSize = 10.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Storage & Battery Pill Widget (3x1)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = outerBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { requestPinWidget(DevPulseStorageWidgetProvider::class.java, "Storage & Battery Pill Widget") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Storage & Battery Bar (3x1)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = activeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = activeGreen
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_pin),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(68.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Storage
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = cardBg,
                                    modifier = Modifier.weight(1.2f).fillMaxSize()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Storage Free", fontSize = 10.sp, color = textSecondary)
                                        Text(storageFreeGbText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                        LinearProgressIndicator(
                                            progress = { safeStorageProgress },
                                            color = activeGreen,
                                            trackColor = trackColor,
                                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape)
                                        )
                                    }
                                }

                                // Battery
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = cardBg,
                                    modifier = Modifier.weight(1f).fillMaxSize()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        WidgetRingGauge(
                                            pct = batteryLevel,
                                            iconRes = R.drawable.ic_widget_battery_icon,
                                            activeColor = activeGreen,
                                            trackColor = trackColor,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("$batteryLevel%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }
                                }

                                // Temp
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = mintBg,
                                    modifier = Modifier.weight(1f).fillMaxSize()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(6.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = brandIconRes),
                                            contentDescription = "Brand",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("$batteryTemp ℃", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }
                                }
                            }
                        }
                    }

                    // 4. Minimal Status Chip Widget (2x1)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = outerBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { requestPinWidget(DevPulseMinimalWidgetProvider::class.java, "Minimal Status Chip Widget") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Minimal Status Chip (2x1)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = activeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = activeGreen
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_pin),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = cardBg,
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    WidgetRingGauge(
                                        pct = batteryLevel,
                                        iconRes = R.drawable.ic_widget_battery_icon,
                                        activeColor = activeGreen,
                                        trackColor = trackColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("$batteryLevel%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                        Text("$batteryTemp ℃ • $modelName", fontSize = 11.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Image(
                                        painter = painterResource(id = brandIconRes),
                                        contentDescription = "Brand",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    try {
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val myWidget = ComponentName(context, DevPulseDashboardWidgetProvider::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                val successCallback = PendingIntent.getBroadcast(
                                    context,
                                    0,
                                    Intent(context, DevPulseDashboardWidgetProvider::class.java),
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                appWidgetManager.requestPinAppWidget(myWidget, null, successCallback)
                                Toast.makeText(context, context.getString(R.string.toast_widget_pin_request_sent), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_widget_long_press_home), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.toast_widget_add_from_picker), Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Throwable) {
                        try {
                            Toast.makeText(context, context.getString(R.string.toast_unable_request_pin, e.message ?: ""), Toast.LENGTH_SHORT).show()
                        } catch (_: Throwable) {}
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.btn_add_widget_to_home))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun WidgetRingGauge(
    pct: Int,
    iconRes: Int,
    activeColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            val radius = ((size.minDimension - strokeWidth) / 2).coerceAtLeast(1f)
            val center = Offset(size.width / 2, size.height / 2)

            // Background Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress Arc
            val sweepAngle = 360f * (pct.coerceIn(0, 100) / 100f)
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = activeColor,
            modifier = Modifier.size(13.dp)
        )
    }
}
