package com.hamraj37.devpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.PhoneAndroid
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.DeviceInfo
import com.hamraj37.devpulse.ui.theme.DevPulseTheme

@Composable
fun DeviceScreen(
    deviceInfo: DeviceInfo,
    modifier: Modifier = Modifier
) {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Basic Hardware Identity
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
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.device_hardware_identity),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = dividerColor)

                DeviceItemRow(stringResource(R.string.device_name), deviceInfo.deviceName)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_model), deviceInfo.model)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_manufacturer), deviceInfo.manufacturer)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_code), deviceInfo.deviceCode)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_board), deviceInfo.board)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_hardware), deviceInfo.hardware)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_brand), deviceInfo.brand)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_android_id), deviceInfo.androidDeviceId)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_build_fingerprint), deviceInfo.buildFingerprint)
            }
        }

        // Card 2: Device Type & Cellular Telephony Specs
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
                        imageVector = Icons.Rounded.CellTower,
                        contentDescription = "Network Specifications",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.device_network_type_hdr),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = dividerColor)

                DeviceItemRow(stringResource(R.string.device_type), deviceInfo.deviceType)
                HorizontalDivider(color = dividerColor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.device_esim_support),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (deviceInfo.esimSupported) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (deviceInfo.esimSupported) stringResource(R.string.lbl_supported) else stringResource(R.string.lbl_not_supported),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (deviceInfo.esimSupported) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                HorizontalDivider(color = dividerColor)

                DeviceItemRow(stringResource(R.string.device_network_type), deviceInfo.networkType)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_operator_1), deviceInfo.networkOperator1)
                HorizontalDivider(color = dividerColor)
                DeviceItemRow(stringResource(R.string.device_operator_2), deviceInfo.networkOperator2)
            }
        }
    }
}

@Composable
fun DeviceItemRow(
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
fun DeviceScreenPreview() {
    DevPulseTheme {
        DeviceScreen(
            deviceInfo = DeviceInfo(
                deviceName = "Pixel 8 Pro",
                model = "Pixel 8 Pro",
                manufacturer = "Google",
                brand = "google",
                board = "husky",
                hardware = "zuma"
            )
        )
    }
}
