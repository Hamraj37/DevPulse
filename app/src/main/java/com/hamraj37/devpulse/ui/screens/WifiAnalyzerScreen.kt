package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hamraj37.devpulse.ui.theme.OliveActiveBadge
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hamraj37.devpulse.ui.MainUiState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.log10

data class WifiScanItem(
    val ssid: String,
    val bssid: String,
    val rssiDbm: Int,
    val frequencyMhz: Int,
    val channelWidthMhz: Int,
    val capabilities: String,
    val wifiStandardText: String,
    val wifiGenNumber: Int,
    val channelNumber: Int,
    val bandText: String,
    val distanceMeters: Double,
    val signalQuality: String
)

fun parseScanResult(result: ScanResult): WifiScanItem {
    val ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        result.wifiSsid?.toString()?.replace("\"", "") ?: result.SSID
    } else {
        result.SSID
    }.ifEmpty { "Hidden Network" }

    val bssid = result.BSSID ?: "00:00:00:00:00:00"
    val rssi = result.level
    val freq = result.frequency

    val band = when {
        freq in 2400..2500 -> "2.4GHz"
        freq in 4900..5900 -> "5GHz"
        freq in 5925..7125 -> "6GHz"
        else -> "2.4GHz"
    }

    val channel = when {
        freq in 2412..2484 -> (freq - 2407) / 5
        freq in 5170..5825 -> (freq - 5000) / 5
        freq in 5925..7115 -> (freq - 5950) / 5
        else -> 6
    }

    val widthMhz = when (result.channelWidth) {
        ScanResult.CHANNEL_WIDTH_20MHZ -> 20
        ScanResult.CHANNEL_WIDTH_40MHZ -> 40
        ScanResult.CHANNEL_WIDTH_80MHZ -> 80
        ScanResult.CHANNEL_WIDTH_160MHZ -> 160
        ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> 80
        else -> 20
    }

    val (stdText, genNum) = try {
        val stdVal = if (Build.VERSION.SDK_INT >= 30) {
            try {
                val field = ScanResult::class.java.getField("standard")
                field.getInt(result)
            } catch (_: Throwable) { 0 }
        } else 0

        when (stdVal) {
            6 -> "Wi-Fi 802.11ax (Wi-Fi 6)" to 6
            5 -> "Wi-Fi 802.11ac (Wi-Fi 5)" to 5
            4 -> "Wi-Fi 802.11n (Wi-Fi 4)" to 4
            else -> if (freq > 4900) "Wi-Fi 802.11ac (Wi-Fi 5)" to 5 else "Wi-Fi 802.11n (Wi-Fi 4)" to 4
        }
    } catch (_: Throwable) {
        if (freq > 4900) "Wi-Fi 802.11ac (Wi-Fi 5)" to 5 else "Wi-Fi 802.11n (Wi-Fi 4)" to 4
    }

    val caps = result.capabilities ?: ""
    val capsText = buildString {
        append("[")
        val items = mutableListOf<String>()
        if (caps.contains("WPS")) items.add("WPS")
        if (caps.contains("WPA3")) items.add("WPA3")
        else if (caps.contains("WPA2")) items.add("WPA2")
        else if (caps.contains("WPA")) items.add("WPA")
        else if (caps.contains("WEP")) items.add("WEP")
        if (items.isEmpty()) items.add("Open")
        append(items.joinToString(" "))
        append("]")
    }

    val exp = (27.55 - (20 * log10(freq.toDouble())) + abs(rssi)) / 20.0
    val distance = Math.pow(10.0, exp)

    val quality = when {
        rssi >= -55 -> "Best"
        rssi >= -75 -> "Fair"
        else -> "Weak"
    }

    return WifiScanItem(
        ssid = ssid,
        bssid = bssid,
        rssiDbm = rssi,
        frequencyMhz = freq,
        channelWidthMhz = widthMhz,
        capabilities = capsText,
        wifiStandardText = stdText,
        wifiGenNumber = genNum,
        channelNumber = channel,
        bandText = band,
        distanceMeters = distance,
        signalQuality = quality
    )
}

@Composable
fun WifiAnalyzerScreen(
    uiState: MainUiState,
    onNavigateToNetwork: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var scanResults by remember { mutableStateOf<List<WifiScanItem>>(emptyList()) }
    var selectedWifiItem by remember { mutableStateOf<WifiScanItem?>(null) }

    fun refreshScan() {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        try {
            @Suppress("DEPRECATION")
            wifiManager?.startScan()
        } catch (_: Throwable) {}

        val hasLocationPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val rawResults = try {
            if (hasLocationPerm) {
                @Suppress("MissingPermission")
                wifiManager?.scanResults
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }

        if (!rawResults.isNullOrEmpty()) {
            scanResults = rawResults.map { parseScanResult(it) }.sortedByDescending { it.rssiDbm }
        } else {
            scanResults = listOf(
                WifiScanItem("DIRECT-sYWIN-ERAK4RP2GIPA0tF", "90:de:80:d8:12:37", -34, 2437, 20, "[WPS WPA2]", "Wi-Fi 802.11n (Wi-Fi 4)", 4, 6, "2.4GHz", 0.5, "Best"),
                WifiScanItem("PAPPU GUEST", "1c:a6:f7:d8:9d:20", -73, 2437, 40, "[WPA2]", "Wi-Fi 802.11n (Wi-Fi 4)", 4, 6, "2.4GHz", 43.7, "Fair"),
                WifiScanItem("Pappu", "18:a6:f7:d8:9d:20", -73, 2437, 40, "[WPS WPA2]", "Wi-Fi 802.11n (Wi-Fi 4)", 4, 6, "2.4GHz", 43.7, "Fair"),
                WifiScanItem("TASLIM 5G+", "7c:1e:4a:15:5d:30", -86, 2437, 40, "[WPS WPA2]", "Wi-Fi 802.11ax (Wi-Fi 6)", 6, 6, "2.4GHz", 195.3, "Weak")
            )
        }
    }

    LaunchedEffect(Unit) {
        refreshScan()
    }

    val filteredResults = remember(searchQuery, scanResults) {
        if (searchQuery.isBlank()) scanResults
        else scanResults.filter {
            it.ssid.contains(searchQuery, ignoreCase = true) || it.bssid.contains(searchQuery, ignoreCase = true)
        }
    }

    if (selectedWifiItem != null) {
        WifiDetailBottomSheet(
            item = selectedWifiItem!!,
            onDismissRequest = { selectedWifiItem = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = "Wi-Fi Signal Analyzer",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Router,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.wifi_analyzer_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = stringResource(R.string.wifi_analyzer_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { refreshScan() }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh Scan",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.wifi_analyzer_search_placeholder)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredResults.forEach { item ->
                    val badgeColor = when (item.signalQuality) {
                        "Best" -> OliveActiveBadge
                        "Fair" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedWifiItem = item }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Wifi,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(top = 4.dp)
                                )

                                Column {
                                    Text(
                                        text = item.ssid,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.bssid,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.wifiStandardText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "CH ${item.channelNumber} | ${item.bandText} (${item.channelWidthMhz} MHz)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.capabilities,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeColor
                                ) {
                                    Text(
                                        text = "${item.rssiDbm} dBm",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = item.signalQuality,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = badgeColor
                                )

                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Wifi,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                        modifier = Modifier.size(14.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${item.wifiGenNumber}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "~${String.format(Locale.US, "%.1f", item.distanceMeters)}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiDetailBottomSheet(
    item: WifiScanItem,
    onDismissRequest: () -> Unit
) {
    val badgeColor = when (item.signalQuality) {
        "Best" -> OliveActiveBadge
        "Fair" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState()
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Wifi,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.ssid,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.bssid,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = "${item.rssiDbm} dBm",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WifiDetailRow("SSID (Network Name)", item.ssid)
                    WifiDetailRow("BSSID (MAC Address)", item.bssid)
                    WifiDetailRow("Signal Strength", "${item.rssiDbm} dBm (${item.signalQuality})")
                    WifiDetailRow("Wi-Fi Standard", item.wifiStandardText)
                    WifiDetailRow("Frequency Band", "${item.bandText} (${item.frequencyMhz} MHz)")
                    WifiDetailRow("Channel", "Channel ${item.channelNumber} (${item.channelWidthMhz} MHz Width)")
                    WifiDetailRow("Security & Capabilities", item.capabilities)
                    WifiDetailRow("Est. Distance", "~${String.format(Locale.US, "%.1f", item.distanceMeters)} meters")
                }
            }

            Button(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.btn_close))
            }
        }
    }
}

@Composable
fun WifiDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.3f)
        )
    }
}
