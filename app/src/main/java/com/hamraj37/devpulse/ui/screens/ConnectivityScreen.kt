package com.hamraj37.devpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hamraj37.devpulse.data.model.ConnectivityInfo
import com.hamraj37.devpulse.ui.theme.OliveActiveBadge

@Composable
fun ConnectivityScreen(
    connectivityInfo: ConnectivityInfo,
    modifier: Modifier = Modifier
) {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Wi-Fi Capabilities Section
        ConnectivitySectionCard(
            title = "Wi-Fi Capabilities",
            icon = Icons.Rounded.Wifi,
            dividerColor = dividerColor
        ) {
            ConnectivityItemRow("WiFi Standard", connectivityInfo.wifiStandard)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("WiFi Direct", connectivityInfo.wifiDirectSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("5GHz Band", connectivityInfo.wifi5GhzSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("6GHz Band (Wi-Fi 6E)", connectivityInfo.wifi6GhzSupported)
        }

        // 2. Bluetooth Capabilities Section
        ConnectivitySectionCard(
            title = "Bluetooth Capabilities",
            icon = Icons.Rounded.Bluetooth,
            dividerColor = dividerColor
        ) {
            ConnectivityFeatureRow("Bluetooth", connectivityInfo.bluetoothSupported, statusText = connectivityInfo.bluetoothVersion)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("Multiple Advertisements", connectivityInfo.multipleAdvertisementsSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("Offloaded Filtering", connectivityInfo.offloadedFilteringSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("Offloaded Scan Batching", connectivityInfo.offloadedScanBatchingSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("Bluetooth LE", connectivityInfo.bluetoothLeSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("LE 2M PHY", connectivityInfo.le2mPhySupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("LE Coded PHY", connectivityInfo.leCodedPhySupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("LE Extended Advertising", connectivityInfo.leExtendedAdvertisingSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("LE Periodic Advertising", connectivityInfo.lePeriodicAdvertisingSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("LE Audio", connectivityInfo.leAudioSupported)
        }

        // 3. NFC Capabilities Section
        ConnectivitySectionCard(
            title = "NFC Capabilities",
            icon = Icons.Rounded.Nfc,
            dividerColor = dividerColor
        ) {
            ConnectivityFeatureRow("NFC", connectivityInfo.nfcSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityItemRow("Status", connectivityInfo.nfcStatus)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("Secure NFC", connectivityInfo.secureNfcSupported)
        }

        // 4. Ultra Wide Band (UWB) Section
        ConnectivitySectionCard(
            title = "Ultra Wide Band (UWB)",
            icon = Icons.Rounded.Radar,
            dividerColor = dividerColor
        ) {
            ConnectivityFeatureRow("UWB Hardware", connectivityInfo.uwbSupported, statusText = connectivityInfo.uwbStatus)
        }

        // 5. USB Capabilities Section
        ConnectivitySectionCard(
            title = "USB Capabilities",
            icon = Icons.Rounded.Usb,
            dividerColor = dividerColor
        ) {
            ConnectivityFeatureRow("USB Host", connectivityInfo.usbHostSupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("USB Accessory", connectivityInfo.usbAccessorySupported)
            HorizontalDivider(color = dividerColor)
            ConnectivityFeatureRow("USB Debugging", connectivityInfo.usbDebuggingEnabled, statusText = connectivityInfo.usbStatus)
        }
    }
}

@Composable
fun ConnectivitySectionCard(
    title: String,
    icon: ImageVector,
    dividerColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
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
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = dividerColor)

            content()
        }
    }
}

@Composable
fun ConnectivityFeatureRow(
    label: String,
    isSupported: Boolean,
    statusText: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (statusText != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSupported) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSupported) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            } else {
                Icon(
                    imageVector = if (isSupported) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = if (isSupported) "Supported" else "Not Supported",
                    tint = if (isSupported) OliveActiveBadge else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isSupported) "Supported" else "Not Supported",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSupported) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun ConnectivityItemRow(
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
