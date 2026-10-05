package com.hamraj37.devpulse.ui.screens

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamraj37.devpulse.data.model.ConnectivityInfo
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import com.hamraj37.devpulse.ui.theme.OliveActiveBadge
import com.hamraj37.devpulse.util.tr

@Composable
fun ConnectivityScreen(
    connectivityInfo: ConnectivityInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    val isBluetoothEnabled = remember(connectivityInfo) {
        try {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            btManager?.adapter?.isEnabled == true
        } catch (_: Throwable) {
            connectivityInfo.bluetoothEnabled
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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
            if (!isBluetoothEnabled) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bluetooth is Disabled".tr(context),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Please enable Bluetooth to view full hardware radio capabilities.".tr(context),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                    context.startActivity(intent)
                                } catch (_: Throwable) {
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Enable".tr(context), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                HorizontalDivider(color = dividerColor)
            }

            ConnectivityFeatureRow(
                label = "Bluetooth Radio",
                isSupported = connectivityInfo.bluetoothSupported,
                statusText = if (isBluetoothEnabled) connectivityInfo.bluetoothVersion else "Disabled"
            )
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
    val context = LocalContext.current
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
                        contentDescription = title.tr(context),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title.tr(context),
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
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.tr(context),
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
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label.tr(context),
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
fun ConnectivityScreenPreview() {
    DevPulseTheme {
        ConnectivityScreen(
            connectivityInfo = ConnectivityInfo()
        )
    }
}
