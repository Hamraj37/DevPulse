package com.hamraj37.devpulse.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Picture
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.TestStatus
import com.hamraj37.devpulse.data.telemetry.AppAndTestTelemetry
import com.hamraj37.devpulse.ui.MainUiState
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Color as AndroidColor

fun generateReportImageFile(context: Context, picture: Picture): Uri? {
    return try {
        val width = picture.width.coerceAtLeast(1)
        val height = picture.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(AndroidColor.WHITE)
        picture.draw(canvas)

        val file = File(context.cacheDir, "DevPulse_Device_Report.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun saveReportImageToGallery(context: Context, picture: Picture): Boolean {
    return try {
        val width = picture.width.coerceAtLeast(1)
        val height = picture.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(AndroidColor.WHITE)
        picture.draw(canvas)

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir
        val file = File(dir, "DevPulse_Report_${System.currentTimeMillis()}.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportScreen(
    uiState: MainUiState,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val reportPicture = remember { Picture() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = stringResource(R.string.export_title),
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recorded Printable / Shareable Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithCache {
                        val width = size.width.toInt()
                        val height = size.height.toInt()
                        onDrawWithContent {
                            val pictureCanvas = Canvas(
                                reportPicture.beginRecording(width, height)
                            )
                            draw(this, layoutDirection, pictureCanvas, size) {
                                this@onDrawWithContent.drawContent()
                            }
                            reportPicture.endRecording()
                            drawContent()
                        }
                    }
            ) {
                DeviceReportCard(uiState = uiState)
            }
        }

        // Bottom Fixed Action Bar with Save image & Share
        Surface(
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val success = saveReportImageToGallery(context, reportPicture)
                        if (success) {
                            Toast.makeText(context, context.getString(R.string.toast_report_image_saved), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.toast_failed_generate_report), Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.export_save_image),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Button(
                    onClick = {
                        val imageUri = generateReportImageFile(context, reportPicture)
                        if (imageUri != null) {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, imageUri)
                                type = "image/png"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.export_share))
                            context.startActivity(shareIntent)
                        } else {
                            Toast.makeText(context, context.getString(R.string.toast_failed_generate_report), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.export_share),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceReportCard(
    uiState: MainUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentFormattedTime = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()) }

    // Memory & Storage calculation
    val ramTotalGb = uiState.dashboardInfo.ramTotalBytes / (1024 * 1024 * 1024f)
    val ramUsedGb = uiState.dashboardInfo.ramUsedBytes / (1024 * 1024 * 1024f)
    val ramPct = if (ramTotalGb > 0) ((ramUsedGb / ramTotalGb) * 100).toInt() else 77

    val storageTotalGb = uiState.dashboardInfo.storageTotalBytes / (1024 * 1024 * 1024f)
    val storageUsedGb = uiState.dashboardInfo.storageUsedBytes / (1024 * 1024 * 1024f)
    val storagePct = if (storageTotalGb > 0) ((storageUsedGb / storageTotalGb) * 100).toInt() else 32

    val maxCpuGhzStr = String.format(
        Locale.US,
        "%.2f",
        if (uiState.cpuInfo.maxFrequencyMhz > 0) uiState.cpuInfo.maxFrequencyMhz / 1000f else 2.80f
    )

    val testsList = remember(uiState.testsList) {
        uiState.testsList.ifEmpty { AppAndTestTelemetry.getInitialTestItems(context) }
    }
    val passedCount = testsList.count { it.status == TestStatus.PASSED }
    val totalTestCount = testsList.size

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = stringResource(R.string.app_name),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.export_device_report_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.export_generated),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = currentFormattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Brand & Chipset Top Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Device Brand Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Smartphone,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = uiState.deviceInfo.manufacturer.uppercase().ifBlank { "DEVICE" },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = uiState.deviceInfo.model.ifBlank { "Unknown Model" },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val boardHardwareStr = listOf(uiState.deviceInfo.board, uiState.deviceInfo.hardware)
                            .filter { it.isNotBlank() }
                            .joinToString(" · ")
                            .ifBlank { "Hardware" }

                        Text(
                            text = boardHardwareStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Chipset Card
                val processorVendor = when {
                    uiState.cpuInfo.gpuVendor.contains("Qualcomm", ignoreCase = true) || uiState.cpuInfo.processorName.contains("Snapdragon", ignoreCase = true) -> "Qualcomm®"
                    uiState.cpuInfo.gpuVendor.contains("ARM", ignoreCase = true) || uiState.cpuInfo.processorName.contains("Dimensity", ignoreCase = true) -> "MediaTek®"
                    uiState.cpuInfo.gpuVendor.contains("Samsung", ignoreCase = true) || uiState.cpuInfo.processorName.contains("Exynos", ignoreCase = true) -> "Samsung®"
                    uiState.cpuInfo.gpuVendor.contains("Google", ignoreCase = true) || uiState.cpuInfo.processorName.contains("Tensor", ignoreCase = true) -> "Google®"
                    else -> uiState.cpuInfo.gpuVendor.ifBlank { "Processor" }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Memory,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = processorVendor,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = uiState.cpuInfo.processorName.ifBlank { "ARM Processor" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${uiState.cpuInfo.totalCores} cores · Max $maxCpuGhzStr GHz",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 3. OS Version Banner Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Android,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            val androidVerText = uiState.systemInfo.androidVersion.ifBlank { "14.0" }
                            val codeNameText = uiState.systemInfo.codeName
                            val osTitleText = if (codeNameText.isNotBlank()) "Android $androidVerText · $codeNameText" else "Android $androidVerText"

                            val customOsName = uiState.systemInfo.osName.ifBlank { "Android" }
                            val customOsVer = uiState.systemInfo.osVersion.ifBlank { uiState.systemInfo.buildNumber }
                            val osSubtitleText = when {
                                customOsVer.isNotBlank() && customOsVer != customOsName -> "$customOsName $customOsVer"
                                else -> customOsName
                            }

                            Text(
                                text = osTitleText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = osSubtitleText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "API ${uiState.systemInfo.apiLevel} · Ext 22",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Quick Metrics 2x2 Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Top Row: Battery & Internal Storage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricProgressTile(
                        icon = Icons.Rounded.BatteryChargingFull,
                        iconTint = Color(0xFF2E7D32),
                        title = "Battery · ${uiState.batteryInfo.status}",
                        value = "${uiState.batteryInfo.levelPercent}%",
                        subtitle = "${uiState.batteryInfo.capacitySystemMah} mAh · ${uiState.batteryInfo.temperatureCelsius}°C",
                        progress = uiState.batteryInfo.levelPercent / 100f,
                        progressColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )

                    MetricProgressTile(
                        icon = Icons.Rounded.SdStorage,
                        iconTint = Color(0xFFE65100),
                        title = "Internal Storage",
                        value = "$storagePct%",
                        subtitle = String.format(Locale.US, "%.1f GB / %.1f GB", storageUsedGb, storageTotalGb),
                        progress = storagePct / 100f,
                        progressColor = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bottom Row: RAM & Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricProgressTile(
                        icon = Icons.Rounded.Memory,
                        iconTint = Color(0xFF1565C0),
                        title = "RAM",
                        value = "$ramPct%",
                        subtitle = String.format(Locale.US, "%.2f GB / %.2f GB", ramUsedGb, ramTotalGb),
                        progress = ramPct / 100f,
                        progressColor = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f)
                    )

                    MetricTileWithoutProgress(
                        icon = Icons.Rounded.Smartphone,
                        iconTint = Color(0xFF2E7D32),
                        title = "Display",
                        value = "${uiState.displayInfo.resolutionWidthPx} x ${uiState.displayInfo.resolutionHeightPx}",
                        subtitle = "${uiState.displayInfo.physicalSizeInches}\" · ${uiState.displayInfo.refreshRate} · ${uiState.displayInfo.resolutionCategory}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 5. System Diagnostic Tests Card
            val colorScheme = MaterialTheme.colorScheme
            val notTestedBg = colorScheme.surfaceContainerHigh
            val notTestedTint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            val onSurfaceColor = colorScheme.onSurface
            val onVariantColor = colorScheme.onSurfaceVariant

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (passedCount > 0) "$passedCount/$totalTestCount tests passed" else "$totalTestCount System Tests",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (passedCount > 0) Color(0xFF2E7D32) else colorScheme.onSurface
                        )

                        Text(
                            text = "${uiState.sensorInfo.sensorCount} sensors · ${uiState.appInfo.totalApps} apps",
                            style = MaterialTheme.typography.labelSmall,
                            color = onVariantColor
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 2,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        testsList.forEach { test ->
                            val (badgeBg, tintColor, icon) = when (test.status) {
                                TestStatus.PASSED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Rounded.Check)
                                TestStatus.FAILED -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), Icons.Rounded.Close)
                                TestStatus.RUNNING -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Rounded.Refresh)
                                TestStatus.NOT_TESTED -> Triple(notTestedBg, notTestedTint, Icons.Rounded.Check)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(0.48f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = badgeBg,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tintColor,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = test.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (test.status) {
                                        TestStatus.PASSED -> onSurfaceColor
                                        TestStatus.FAILED -> Color(0xFFD32F2F)
                                        else -> onVariantColor
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 6. Disclaimer & App Branding Footer
            Text(
                text = stringResource(R.string.export_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = stringResource(R.string.app_name),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Column {
                            val appVersionName = remember {
                                try {
                                    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                                    pInfo.versionName ?: "1.0.7"
                                } catch (_: Throwable) {
                                    "1.0.7"
                                }
                            }

                            Text(
                                text = "DevPulse v$appVersionName",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.export_footer_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = stringResource(R.string.export_available_on),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricProgressTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val safeProgress = progress.let { if (it.isNaN() || it.isInfinite()) 0f else it.coerceIn(0f, 1f) }
            LinearProgressIndicator(
                progress = { safeProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }
}

@Composable
fun MetricTileWithoutProgress(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceReportCardPreview() {
    DevPulseTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DeviceReportCard(uiState = MainUiState())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExportScreenPreview() {
    DevPulseTheme {
        ExportScreen(uiState = MainUiState())
    }
}
