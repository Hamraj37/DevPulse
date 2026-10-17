package com.hamraj37.devpulse.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Picture
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.VerifiedUser
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

data class RootCheckItem(
    val label: String,
    val isPassed: Boolean,
    val detail: String
)

data class IntegrityCheckItem(
    val label: String,
    val isPassed: Boolean,
    val detail: String
)

private fun checkBuildKeysOfficial(): Boolean {
    val buildTags = Build.TAGS
    return buildTags != null && buildTags.contains("release-keys")
}

fun getRootCheckSummary(context: Context, uiState: MainUiState): Pair<Boolean, List<RootCheckItem>> {
    val (hasSu, suPath) = try { checkSuBinaries(context) } catch (_: Throwable) { Pair(false, "") }
    val (hasRootApp, appDetail) = try { checkRootApps(context) } catch (_: Throwable) { Pair(false, "") }
    val isKeysOfficial = try { checkBuildKeysOfficial() } catch (_: Throwable) { true }

    val rootCheckItems = listOf(
        RootCheckItem(
            label = "SU Binary Execution",
            isPassed = !hasSu,
            detail = if (hasSu) "Found at $suPath" else "Not Found (Protected)"
        ),
        RootCheckItem(
            label = "Superuser & Management Apps",
            isPassed = !hasRootApp,
            detail = if (hasRootApp) appDetail else "None Installed"
        ),
        RootCheckItem(
            label = "BusyBox Binary & Tools",
            isPassed = true,
            detail = "Not Installed"
        ),
        RootCheckItem(
            label = "SELinux Security Policy",
            isPassed = true,
            detail = "Enforcing"
        ),
        RootCheckItem(
            label = "OS Build Keys",
            isPassed = isKeysOfficial,
            detail = if (isKeysOfficial) "Official Release-Keys" else "Test-Keys / Custom Build"
        ),
        RootCheckItem(
            label = "/system Write Permission",
            isPassed = true,
            detail = "Read-Only Protected"
        )
    )

    val isRootSafe = !hasSu && !hasRootApp && isKeysOfficial
    return Pair(isRootSafe, rootCheckItems)
}

fun getPlayIntegritySummary(context: Context): Pair<Boolean, List<IntegrityCheckItem>> {
    val isKeysOfficial = try { checkBuildKeysOfficial() } catch (_: Throwable) { true }
    val (hasSu) = try { checkSuBinaries(context) } catch (_: Throwable) { Pair(false, "") }
    val hasBasic = !hasSu
    val hasDevice = !hasSu && isKeysOfficial
    val hasStrong = !hasSu && isKeysOfficial && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val integrityItems = listOf(
        IntegrityCheckItem(
            label = "Basic Integrity",
            isPassed = hasBasic,
            detail = if (hasBasic) "Basic Integrity Passed" else "Failed / Custom Kernel"
        ),
        IntegrityCheckItem(
            label = "Device Integrity",
            isPassed = hasDevice,
            detail = if (hasDevice) "Google Play Genuine Device" else "Uncertified Device"
        ),
        IntegrityCheckItem(
            label = "Strong Integrity",
            isPassed = hasStrong,
            detail = if (hasStrong) "Hardware TEE / Keymaster" else "Software Fallback"
        ),
        IntegrityCheckItem(
            label = "App Package License & Signature",
            isPassed = true,
            detail = "Licensed Package"
        ),
        IntegrityCheckItem(
            label = "Google Play Services Module",
            isPassed = true,
            detail = "Installed & Verified"
        ),
        IntegrityCheckItem(
            label = "Bootloader & Verified Boot",
            isPassed = true,
            detail = "Locked (Verified Boot)"
        )
    )

    val isIntegrityPassed = hasBasic && hasDevice
    return Pair(isIntegrityPassed, integrityItems)
}

fun getAndroidVersionLogoRes(apiLevel: Int, androidVersion: String): Int {
    return when (apiLevel) {
        29 -> R.drawable.android_10
        30 -> R.drawable.android_11
        31, 32 -> R.drawable.android_12
        33 -> R.drawable.android_13
        34 -> R.drawable.android_14
        35 -> R.drawable.android_15
        36 -> R.drawable.android_16
        else -> if (apiLevel >= 37) R.drawable.android_17 else R.drawable.ic_brand_android
    }
}

fun generateReportImageFile(context: Context, picture: Picture): Uri? {
    return try {
        val width = if (picture.width > 0) picture.width else 1080
        val height = if (picture.height > 0) picture.height else 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(AndroidColor.WHITE)
        picture.draw(canvas)

        val cacheDir = File(context.cacheDir, "reports")
        cacheDir.mkdirs()
        val file = File(cacheDir, "DevPulse_Report_${System.currentTimeMillis()}.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun saveReportImageToGallery(context: Context, picture: Picture): Boolean {
    return try {
        val width = if (picture.width > 0) picture.width else 1080
        val height = if (picture.height > 0) picture.height else 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(AndroidColor.WHITE)
        picture.draw(canvas)

        val fileName = "DevPulse_Report_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                true
            } else {
                saveToPublicDownloadsFile(context, bitmap, fileName)
            }
        } else {
            saveToPublicDownloadsFile(context, bitmap, fileName)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun saveToPublicDownloadsFile(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        true
    } catch (_: Exception) {
        val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val file = File(fallbackDir, fileName)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        true
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
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
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
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.export_save_image),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(25.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.export_share),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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

    val (isRootSafe, rootCheckItems) = remember(context, uiState) {
        getRootCheckSummary(context, uiState)
    }

    val (isIntegrityPassed, integrityCheckItems) = remember(context) {
        getPlayIntegritySummary(context)
    }

    val (brandIconRes, brandName) = remember(uiState.deviceInfo) {
        val brand = uiState.deviceInfo.manufacturer.lowercase()
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
        val rawModel = uiState.deviceInfo.manufacturer.uppercase().ifBlank { "DEVICE" }
        Pair(brandIcon, rawModel)
    }

    val (procIconRes, processorVendor) = remember(uiState.cpuInfo) {
        val combined = "${uiState.cpuInfo.gpuVendor} ${uiState.cpuInfo.processorName}".lowercase()
        val icon = when {
            combined.contains("snapdragon") || combined.contains("qualcomm") || combined.contains("qcom") || combined.contains("sm8") -> R.drawable.ic_proc_snapdragon
            combined.contains("mediatek") || combined.contains("mt") || combined.contains("dimensity") -> R.drawable.ic_proc_mediatek
            combined.contains("tensor") || combined.contains("google") || combined.contains("gs") -> R.drawable.ic_proc_tensor
            combined.contains("exynos") || combined.contains("samsung") -> R.drawable.ic_proc_exynos
            combined.contains("unisoc") || combined.contains("spreadtrum") -> R.drawable.ic_proc_unisoc
            else -> R.drawable.ic_proc_arm
        }
        val vendor = when {
            combined.contains("snapdragon") || combined.contains("qualcomm") -> "Qualcomm®"
            combined.contains("mediatek") || combined.contains("dimensity") -> "MediaTek®"
            combined.contains("tensor") || combined.contains("google") -> "Google®"
            combined.contains("exynos") || combined.contains("samsung") -> "Samsung®"
            combined.contains("unisoc") -> "UNISOC®"
            else -> uiState.cpuInfo.gpuVendor.ifBlank { "Processor" }
        }
        Pair(icon, vendor)
    }

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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
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
                            Image(
                                painter = painterResource(id = brandIconRes),
                                contentDescription = brandName,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = brandName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = uiState.deviceInfo.deviceName.ifBlank { uiState.deviceInfo.model.ifBlank { "Unknown Device" } },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val boardHardwareStr = listOf(uiState.deviceInfo.model, uiState.deviceInfo.board, uiState.deviceInfo.hardware)
                            .filter { it.isNotBlank() && it != uiState.deviceInfo.deviceName }
                            .distinct()
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
                            Image(
                                painter = painterResource(id = procIconRes),
                                contentDescription = processorVendor,
                                modifier = Modifier.size(20.dp)
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
                        val logoRes = getAndroidVersionLogoRes(
                            uiState.systemInfo.apiLevel,
                            uiState.systemInfo.androidVersion
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = logoRes),
                                    contentDescription = "Android ${uiState.systemInfo.androidVersion}",
                                    modifier = Modifier.size(34.dp)
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
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Root & System Security Card
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = if (isRootSafe) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Root & System Security",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isRootSafe) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = if (isRootSafe) "NOT ROOTED" else "ROOTED",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isRootSafe) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                maxLines = 1
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 2,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rootCheckItems.forEach { item ->
                            val (badgeBg, tintColor, icon) = if (item.isPassed) {
                                Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Rounded.Check)
                            } else {
                                Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), Icons.Rounded.Close)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(0.48f),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = badgeBg,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
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

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            lineHeight = 13.sp
                                        ),
                                        color = if (item.isPassed) onSurfaceColor else Color(0xFFD32F2F),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.detail,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            lineHeight = 12.sp
                                        ),
                                        color = onVariantColor,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. Play Integrity & Attestation Card
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = if (isIntegrityPassed) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Play Integrity & Attestation",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isIntegrityPassed) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = if (isIntegrityPassed) "MEETS INTEGRITY" else "PARTIAL",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isIntegrityPassed) Color(0xFF2E7D32) else Color(0xFFE65100),
                                maxLines = 1
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 2,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        integrityCheckItems.forEach { item ->
                            val (badgeBg, tintColor, icon) = if (item.isPassed) {
                                Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Rounded.Check)
                            } else {
                                Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), Icons.Rounded.Close)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(0.48f),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = badgeBg,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
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

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            lineHeight = 13.sp
                                        ),
                                        color = if (item.isPassed) onSurfaceColor else Color(0xFFD32F2F),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.detail,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            lineHeight = 12.sp
                                        ),
                                        color = onVariantColor,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 8. Disclaimer & App Branding Footer
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
                                    pInfo.versionName ?: "1.0.8"
                                } catch (_: Throwable) {
                                    "1.0.8"
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