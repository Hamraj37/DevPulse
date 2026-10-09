package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhotoCamera
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.hamraj37.devpulse.data.model.CameraInfo
import com.hamraj37.devpulse.data.model.CameraSpec
import com.hamraj37.devpulse.ui.theme.DevPulseTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraScreen(
    cameraInfo: CameraInfo,
    selectedCameraId: String,
    onSelectCamera: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val context = LocalContext.current

    val cameras = cameraInfo.cameras.ifEmpty {
        listOf(
            CameraSpec(
                cameraId = "0",
                facing = "Back Camera",
                resolutionMp = "13 MP - Back 4096 x 3072"
            ),
            CameraSpec(
                cameraId = "1",
                facing = "Front Camera",
                resolutionMp = "16 MP - Front 4608 x 3456"
            )
        )
    }

    val activeSpec = cameras.find { it.cameraId == selectedCameraId } ?: cameras.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Camera Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            cameras.forEach { spec ->
                val isSelected = spec.cameraId == activeSpec.cameraId
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectCamera(spec.cameraId) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.PhotoCamera,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = spec.resolutionMp,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // 2. Disclaimer Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.camera_please_read_hdr),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.camera_megapixels_disclaimer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 3. Comprehensive Camera Specs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${activeSpec.facing} " + stringResource(R.string.camera_specifications),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Camera ID: ${activeSpec.cameraId} • Level: ${activeSpec.hardwareLevel.substringBefore(" ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = activeSpec.pixelSize,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                // Section 1: Lens & Optics
                CameraSpecSectionHeader(stringResource(R.string.camera_sec_lens_optics))
                CameraSpecRow(stringResource(R.string.camera_lens_placement), activeSpec.lensPlacement)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_focal_lengths), activeSpec.focalLengthsFormatted.ifEmpty { activeSpec.focalLengths.map { "${it} mm" } }.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_apertures), activeSpec.apertures.joinToString(", ") { "f/$it" })
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_ois), if (activeSpec.opticalStabilizationSupported) "Supported (Hardware OIS)" else stringResource(R.string.lbl_not_supported))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_focus_distance_calibration), activeSpec.focusDistanceCalibration)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_filter_densities), activeSpec.filterDensities.joinToString(", "))
                HorizontalDivider(color = dividerColor)

                // Section 2: Sensor & Pixel Structure
                CameraSpecSectionHeader(stringResource(R.string.camera_sec_sensor_specs))
                CameraSpecRow(stringResource(R.string.camera_sensor_size), activeSpec.sensorSize)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_sensor_resolution), activeSpec.resolutionMp.substringBefore(" •"))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_effective_megapixels), activeSpec.effectiveMegapixels)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_pixel_array_size), activeSpec.pixelArraySize)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_color_filter), activeSpec.colorFilterArrangement)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_orientation), activeSpec.orientation)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_timestamp_source), activeSpec.timestampSource)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_hot_pixel_modes), activeSpec.hotPixelModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)

                // Section 3: Exposure, Focus & White Balance
                CameraSpecSectionHeader(stringResource(R.string.camera_sec_exposure_color))
                CameraSpecRow(stringResource(R.string.camera_ae_modes), activeSpec.autoExposureModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_compensation_step), activeSpec.compensationStep)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_af_modes), activeSpec.autoFocusModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_awb_modes), activeSpec.whiteBalanceModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_ae_af_awb_regions), activeSpec.maxAeAfAwbRegions)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_aberration_modes), activeSpec.aberrationModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_antibanding_modes), activeSpec.antibandingModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_edge_modes), activeSpec.edgeModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)

                // Section 4: Video, Flash & Features
                CameraSpecSectionHeader(stringResource(R.string.camera_sec_video_flash))
                CameraSpecRow(stringResource(R.string.camera_flash_available), if (activeSpec.flashAvailable) "Yes (Hardware Torch/Flash)" else "No Flash")
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_video_stabilization), activeSpec.videoStabilizationModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_face_detection), activeSpec.faceDetectionModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_scene_modes), activeSpec.sceneModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_effects_available), activeSpec.effects.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_test_pattern_modes), activeSpec.testPatternModes.joinToString(", "))
                HorizontalDivider(color = dividerColor)

                // Section 5: Capabilities & Resolutions
                CameraSpecSectionHeader(stringResource(R.string.camera_sec_capabilities_resolutions))
                CameraSpecRow(stringResource(R.string.camera_dynamic_range_profiles), activeSpec.dynamicRangeProfiles.joinToString(", "))
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_max_output_streams), activeSpec.maxOutputStreams)
                HorizontalDivider(color = dividerColor)
                CameraSpecRow(stringResource(R.string.camera_thumbnail_sizes), activeSpec.thumbnailSizes.joinToString(", "))
                HorizontalDivider(color = dividerColor)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.camera_capabilities),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        activeSpec.cameraCapabilities.forEach { cap ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = cap,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.camera_resolutions),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (activeSpec.supportedPhotoResolutions + activeSpec.supportedVideoResolutions).forEach { res ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = res,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraSpecSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CameraSpecRow(
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
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    DevPulseTheme {
        CameraScreen(
            cameraInfo = CameraInfo(
                cameras = listOf(
                    CameraSpec(
                        cameraId = "0",
                        facing = "Back Camera",
                        resolutionMp = "50 MP - Back 8192 x 6144"
                    ),
                    CameraSpec(
                        cameraId = "1",
                        facing = "Front Camera",
                        resolutionMp = "12 MP - Front 4000 x 3000"
                    )
                )
            ),
            selectedCameraId = "0",
            onSelectCamera = {}
        )
    }
}
