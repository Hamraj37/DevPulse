package com.hamraj37.devpulse.ui.screens

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
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SettingsSystemDaydream
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamraj37.devpulse.data.model.SystemInfo
import com.hamraj37.devpulse.ui.theme.DevPulseTheme

@Composable
fun SystemScreen(
    systemInfo: SystemInfo,
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
        // 1. Header Banner Card (Android OS Badge & Release Details)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Android,
                            contentDescription = "Android OS Mascot",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Android ${systemInfo.androidVersion} - ${systemInfo.versionLetter}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = systemInfo.codeName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Released : ${systemInfo.releaseDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // 2. System Specifications Card
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
                        imageVector = Icons.Rounded.SettingsSystemDaydream,
                        contentDescription = "System Specifications",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "System Architecture & Runtime",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = dividerColor)

                val systemSpecs = listOf(
                    "OS Name" to systemInfo.osName,
                    "OS Version" to systemInfo.osVersion,
                    "Android Version" to systemInfo.androidVersion,
                    "Code Name" to systemInfo.codeName,
                    "API Level" to systemInfo.apiLevel.toString(),
                    "Security Patch Level" to systemInfo.securityPatch,
                    "Bootloader" to systemInfo.bootloader,
                    "Build Number" to systemInfo.buildNumber,
                    "Baseband" to systemInfo.basebandVersion,
                    "Java VM" to systemInfo.javaVm,
                    "Kernel" to systemInfo.kernelVersion,
                    "Language" to systemInfo.language,
                    "Timezone" to systemInfo.timezone,
                    "OpenGL ES" to systemInfo.openGlEsVersion,
                    "Root Management Apps" to systemInfo.rootManagementApps,
                    "SELinux" to systemInfo.seLinux,
                    "Google Play Services" to systemInfo.googlePlayServices,
                    "System Uptime" to systemInfo.systemUptime,
                    "Vulkan" to systemInfo.vulkanVersion,
                    "Treble" to systemInfo.trebleSupported,
                    "Seamless Updates" to systemInfo.seamlessUpdates,
                    "Dynamic Partitions" to systemInfo.dynamicPartitions
                )

                systemSpecs.forEachIndexed { index, (label, value) ->
                    SystemItemRow(label, value)
                    if (index < systemSpecs.lastIndex) {
                        HorizontalDivider(color = dividerColor)
                    }
                }
            }
        }

        // 3. DRM Section Card
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
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "DRM",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DRM Specifications",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = dividerColor)

                val drm = systemInfo.drmInfo
                val drmSpecs = listOf(
                    "Vendor" to drm.vendor,
                    "Version" to drm.version,
                    "Description" to drm.description,
                    "Algorithms" to drm.algorithms,
                    "Security Level" to drm.securityLevel,
                    "Max HDCP Level" to drm.maxHdcpLevel
                )

                drmSpecs.forEachIndexed { index, (label, value) ->
                    SystemItemRow(label, value)
                    if (index < drmSpecs.lastIndex) {
                        HorizontalDivider(color = dividerColor)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemItemRow(
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
fun SystemScreenPreview() {
    DevPulseTheme {
        SystemScreen(
            systemInfo = SystemInfo()
        )
    }
}
