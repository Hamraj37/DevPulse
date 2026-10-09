package com.hamraj37.devpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Speed
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
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.CpuCoreSpeed
import com.hamraj37.devpulse.data.model.CpuInfo
import com.hamraj37.devpulse.ui.theme.DevPulseTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CpuScreen(
    cpuInfo: CpuInfo,
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
        // 1. Live Cores MHz Grid Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = "Core Frequencies",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.cpu_core_frequencies),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${cpuInfo.totalCores} " + stringResource(R.string.lbl_cores),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4
                ) {
                    val coreSpeeds = cpuInfo.coreFrequencies.ifEmpty {
                        listOf(
                            CpuCoreSpeed(0, 1800, 800, 3200),
                            CpuCoreSpeed(1, 1800, 800, 3200),
                            CpuCoreSpeed(2, 1800, 800, 3200),
                            CpuCoreSpeed(3, 1800, 800, 3200),
                            CpuCoreSpeed(4, 2400, 800, 3200),
                            CpuCoreSpeed(5, 2400, 800, 3200),
                            CpuCoreSpeed(6, 2400, 800, 3200),
                            CpuCoreSpeed(7, 3200, 800, 3200)
                        )
                    }

                    coreSpeeds.forEach { speed ->
                        androidx.compose.runtime.key(speed.coreIndex) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${stringResource(R.string.lbl_core)} ${speed.coreIndex}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${speed.currentMhz} MHz",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. CPU & GPU Specs Card
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
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = "CPU Specifications",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.cpu_processor_architecture),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = dividerColor)

                val cpuSpecs = listOf(
                    stringResource(R.string.cpu_processor) to cpuInfo.processorName,
                    stringResource(R.string.cpu_architecture) to cpuInfo.architecture,
                    stringResource(R.string.cpu_supported_abis) to cpuInfo.supportedAbis.joinToString(", "),
                    stringResource(R.string.cpu_hardware) to cpuInfo.hardwareName,
                    stringResource(R.string.cpu_type) to cpuInfo.cpuType,
                    stringResource(R.string.cpu_governor) to cpuInfo.governor,
                    stringResource(R.string.lbl_cores) to "${cpuInfo.totalCores}",
                    stringResource(R.string.cpu_frequency) to "${cpuInfo.minFrequencyMhz} MHz - ${cpuInfo.maxFrequencyMhz} MHz",
                    stringResource(R.string.gpu_renderer) to cpuInfo.gpuRenderer,
                    stringResource(R.string.gpu_vendor) to cpuInfo.gpuVendor,
                    stringResource(R.string.gpu_version) to cpuInfo.gpuVersion
                )

                cpuSpecs.forEachIndexed { index, (label, value) ->
                    CpuItemRow(label, value)
                    if (index < cpuSpecs.lastIndex) {
                        HorizontalDivider(color = dividerColor)
                    }
                }
            }
        }
    }
}

@Composable
fun CpuItemRow(
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
fun CpuScreenPreview() {
    DevPulseTheme {
        CpuScreen(cpuInfo = CpuInfo())
    }
}
