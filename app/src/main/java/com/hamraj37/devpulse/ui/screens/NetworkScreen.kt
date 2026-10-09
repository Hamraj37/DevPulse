package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import android.net.TrafficStats
import android.text.format.Formatter
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.data.model.NetworkInfo
import com.hamraj37.devpulse.ui.components.LiveSparklineChart
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import com.hamraj37.devpulse.ui.theme.OliveActiveBadge

@Composable
fun NetworkScreen(
    networkInfo: NetworkInfo,
    onNavigateToDataUsage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPublicIpDialog by remember { mutableStateOf(false) }
    var showUsageDialog by remember { mutableStateOf(false) }
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    val isCellular = networkInfo.activeConnectionType == "CELLULAR" || networkInfo.isCellularDataActive
    val isDisconnected = networkInfo.activeConnectionType == "DISCONNECTED" || !networkInfo.isConnected

    val bannerIcon = when {
        isCellular -> Icons.Rounded.CellTower
        isDisconnected -> Icons.Rounded.WifiOff
        else -> Icons.Rounded.Wifi
    }

    val bannerTitle = when {
        isCellular -> {
            val op = networkInfo.networkOperatorName
            if (op.contains("Mobile Data")) op else "$op - Mobile Data"
        }
        isDisconnected -> "No Active Network"
        else -> networkInfo.ssid
    }

    val badgeText = when {
        isCellular -> {
            if (networkInfo.networkType.contains("5G") || networkInfo.wifiBadge == "5G") "5G" else "4G LTE"
        }
        isDisconnected -> "Offline"
        else -> networkInfo.wifiBadge
    }

    val badgeColor = when {
        isDisconnected -> MaterialTheme.colorScheme.error
        else -> OliveActiveBadge
    }

    val bannerSubtitle = when {
        isDisconnected -> "Please connect to Wi-Fi or Mobile Data"
        else -> networkInfo.ipAddress
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Network Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = bannerIcon,
                                contentDescription = "Active Network",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = bannerTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor
                            ) {
                                Text(
                                    text = badgeText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = bannerSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-Time Speed Section (Download / Upload)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Download Speed Tile
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = "Download Speed",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = stringResource(R.string.network_download),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = networkInfo.downloadSpeed,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Upload Speed Tile
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Upload,
                                        contentDescription = "Upload Speed",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = stringResource(R.string.network_upload),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = networkInfo.uploadSpeed,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Live Sparkline Graph for Real-time Network Activity
                if (!isDisconnected) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.network_live_traffic),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.network_realtime),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiveSparklineChart(
                            history = networkInfo.speedHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            lineColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons ("Usage", "Public IP")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToDataUsage,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DataUsage,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.network_usage), fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = { showPublicIpDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.network_public_ip), fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        if (networkInfo.ssid.contains("Location Service Disabled") || networkInfo.ssid.contains("Location Permission Required")) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WifiOff,
                        contentDescription = "Location Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (networkInfo.ssid.contains("Disabled")) "Location Service Disabled" else "Location Permission Required",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Android 8.1+ requires Location services (GPS) and permissions to access Wi-Fi SSID and BSSID details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 2. Network Details Card below
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = bannerIcon,
                        contentDescription = "Network Details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isCellular -> "Detailed Mobile Data & Network Specs"
                            isDisconnected -> "Network Connection Status"
                            else -> "Detailed Wi-Fi & Network Specs"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = dividerColor)

                val networkSpecs = when {
                    isCellular -> listOf(
                        "Connection Type" to "Mobile Data",
                        "Real-time Download" to networkInfo.downloadSpeed,
                        "Real-time Upload" to networkInfo.uploadSpeed,
                        "Network Operator" to networkInfo.networkOperatorName,
                        "Network Type" to networkInfo.networkType,
                        "Cellular IPv4" to networkInfo.ipAddress,
                        "Cellular IPv6" to networkInfo.ipv6Address,
                        "Data State" to networkInfo.dataState,
                        "Interface" to networkInfo.interfaceName,
                        "Roaming" to networkInfo.roamingState,
                        "Country / MCC" to networkInfo.countryMcc,
                        "MCC / MNC" to networkInfo.mccMnc,
                        "Mobile Signal" to networkInfo.mobileSignal,
                        "Public IP Location" to networkInfo.location
                    )
                    isDisconnected -> listOf(
                        "Status" to "Disconnected",
                        "Active Connection" to "None",
                        "Wi-Fi" to "Not Connected",
                        "Mobile Data" to "Not Connected",
                        "Interface" to "None",
                        "Recommendation" to "Enable Wi-Fi or Mobile Data in Settings"
                    )
                    else -> listOf(
                        "SSID / Network" to networkInfo.ssid,
                        "Real-time Download" to networkInfo.downloadSpeed,
                        "Real-time Upload" to networkInfo.uploadSpeed,
                        "BSSID" to networkInfo.bssid,
                        "IP Address" to networkInfo.ipAddress,
                        "IPv6 Address" to networkInfo.ipv6Address,
                        "Gateway" to networkInfo.gateway,
                        "Subnet Mask" to networkInfo.subnetMask,
                        "DNS 1" to networkInfo.dns1,
                        "Lease Duration" to networkInfo.leaseDuration,
                        "Interface" to networkInfo.interfaceName,
                        "Link Speed" to networkInfo.linkSpeed,
                        "Channel" to networkInfo.channel,
                        "Frequency" to networkInfo.frequency,
                        "WiFi Standard" to networkInfo.wifiStandard,
                        "Security Type" to networkInfo.securityType,
                        "Public IP Location" to networkInfo.location
                    )
                }

                networkSpecs.forEachIndexed { index, (label, value) ->
                    NetworkItemRow(label, value)
                    if (index < networkSpecs.lastIndex) {
                        HorizontalDivider(color = dividerColor)
                    }
                }
            }
        }
    }

    if (showUsageDialog) {
        val totalRx = TrafficStats.getTotalRxBytes()
        val totalTx = TrafficStats.getTotalTxBytes()
        val mobileRx = TrafficStats.getMobileRxBytes()
        val mobileTx = TrafficStats.getMobileTxBytes()

        val wifiRx = if (totalRx != TrafficStats.UNSUPPORTED.toLong() && mobileRx != TrafficStats.UNSUPPORTED.toLong()) maxOf(0L, totalRx - mobileRx) else 0L
        val wifiTx = if (totalTx != TrafficStats.UNSUPPORTED.toLong() && mobileTx != TrafficStats.UNSUPPORTED.toLong()) maxOf(0L, totalTx - mobileTx) else 0L

        val formattedMobileRx = Formatter.formatFileSize(context, if (mobileRx != TrafficStats.UNSUPPORTED.toLong()) mobileRx else 0L)
        val formattedMobileTx = Formatter.formatFileSize(context, if (mobileTx != TrafficStats.UNSUPPORTED.toLong()) mobileTx else 0L)
        val formattedWifiRx = Formatter.formatFileSize(context, wifiRx)
        val formattedWifiTx = Formatter.formatFileSize(context, wifiTx)

        AlertDialog(
            onDismissRequest = { showUsageDialog = false },
            title = { Text(text = "Data Usage Summary", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Mobile Received: $formattedMobileRx",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCellular) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "Mobile Sent: $formattedMobileTx",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCellular) FontWeight.Bold else FontWeight.Normal
                    )
                    HorizontalDivider(color = dividerColor)
                    Text(
                        text = "Wi-Fi Received: $formattedWifiRx",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (!isCellular && !isDisconnected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "Wi-Fi Sent: $formattedWifiTx",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (!isCellular && !isDisconnected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showUsageDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPublicIpDialog) {
        val carrierOrIsp = if (isCellular) networkInfo.networkOperatorName else "Reliance Jio Infocomm Ltd"
        AlertDialog(
            onDismissRequest = { showPublicIpDialog = false },
            title = { Text(text = "Public IP Address", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Detected Public IP: ${networkInfo.publicIp}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "ISP / Carrier: $carrierOrIsp", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Location: ${networkInfo.location}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPublicIpDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun NetworkItemRow(
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
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.3f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NetworkScreenPreview() {
    DevPulseTheme {
        NetworkScreen(
            networkInfo = NetworkInfo()
        )
    }
}
