package com.hamraj37.devpulse.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.hamraj37.devpulse.ui.MainUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Color as AndroidColor

fun generatePdfReportFile(context: Context, reportContent: String): Uri? {
    return try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = AndroidColor.parseColor("#1A237E")
            textSize = 18f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 11f
        }

        canvas.drawText("DevPulse System Telemetry Report", 40f, 50f, titlePaint)

        var y = 85f
        val lines = reportContent.split("\n")
        for (line in lines) {
            if (y > 800f) break
            canvas.drawText(line, 40f, y, textPaint)
            y += 16f
        }

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "DevPulse_System_Report.pdf")
        file.outputStream().use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

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

fun generateTextReportFile(context: Context, reportContent: String): Uri? {
    return try {
        val file = File(context.cacheDir, "DevPulse_System_Report.txt")
        file.writeText(reportContent)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportScreen(
    uiState: MainUiState,
    context: Context = LocalContext.current,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    var reportType by remember { mutableStateOf("Text") } // "Text" or "PDF"

    val allCategories = remember {
        listOf(
            "Device", "System", "CPU", "Battery",
            "Network", "Connectivity", "Display", "Memory",
            "Camera", "Thermal", "Sensors", "Apps"
        )
    }

    val selectedCategories = remember {
        mutableStateListOf<String>().apply { addAll(allCategories) }
    }

    fun buildCustomReport(): String {
        return buildString {
            appendLine("=== DevPulse System Telemetry Report ===")
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
            appendLine("Generated: ${sdf.format(Date())}")
            appendLine()

            if (selectedCategories.contains("Device")) {
                appendLine("[Device Info]")
                appendLine("Manufacturer: ${uiState.deviceInfo.manufacturer}")
                appendLine("Model: ${uiState.deviceInfo.model}")
                appendLine("Brand: ${uiState.deviceInfo.brand}")
                appendLine("Board: ${uiState.deviceInfo.board}")
                appendLine("Hardware: ${uiState.deviceInfo.hardware}")
                appendLine()
            }

            if (selectedCategories.contains("System")) {
                appendLine("[System Info]")
                appendLine("Android Version: ${uiState.systemInfo.androidVersion} (API ${uiState.systemInfo.apiLevel})")
                appendLine("Security Patch: ${uiState.systemInfo.securityPatch}")
                appendLine("Build Number: ${uiState.systemInfo.buildNumber}")
                appendLine("Kernel Version: ${uiState.systemInfo.kernelVersion}")
                appendLine()
            }

            if (selectedCategories.contains("CPU")) {
                appendLine("[CPU Info]")
                appendLine("Processor: ${uiState.cpuInfo.processorName}")
                appendLine("Architecture: ${uiState.cpuInfo.architecture}")
                appendLine("Cores: ${uiState.cpuInfo.totalCores}")
                appendLine("Governor: ${uiState.cpuInfo.governor}")
                appendLine("GPU Renderer: ${uiState.cpuInfo.gpuRenderer}")
                appendLine()
            }

            if (selectedCategories.contains("Battery")) {
                appendLine("[Battery Info]")
                appendLine("Level: ${uiState.batteryInfo.levelPercent}%")
                appendLine("Health: ${uiState.batteryInfo.health}")
                appendLine("Status: ${uiState.batteryInfo.status}")
                appendLine("Temperature: ${uiState.batteryInfo.temperatureCelsius}°C")
                appendLine("Voltage: ${uiState.batteryInfo.voltageVolts} V")
                appendLine()
            }

            if (selectedCategories.contains("Network")) {
                appendLine("[Network Info]")
                appendLine("SSID: ${uiState.networkInfo.ssid}")
                appendLine("IP Address: ${uiState.networkInfo.ipAddress}")
                appendLine("Gateway: ${uiState.networkInfo.gateway}")
                appendLine("Link Speed: ${uiState.networkInfo.linkSpeed}")
                appendLine()
            }

            if (selectedCategories.contains("Connectivity")) {
                appendLine("[Connectivity Info]")
                appendLine("Connection Type: ${uiState.networkInfo.activeConnectionType}")
                appendLine("Wi-Fi Direct: ${uiState.connectivityInfo.wifiDirectSupported}")
                appendLine("Bluetooth: ${uiState.connectivityInfo.bluetoothSupported}")
                appendLine()
            }

            if (selectedCategories.contains("Display")) {
                appendLine("[Display Info]")
                appendLine("Resolution: ${uiState.displayInfo.resolution}")
                appendLine("Refresh Rate: ${uiState.displayInfo.refreshRate}")
                appendLine("Density: ${uiState.displayInfo.densityDpi} DPI")
                appendLine()
            }

            if (selectedCategories.contains("Memory")) {
                appendLine("[Memory & Storage]")
                appendLine("RAM Total: ${uiState.dashboardInfo.ramTotalBytes / (1024 * 1024 * 1024)} GB")
                appendLine("RAM Used: ${uiState.dashboardInfo.ramUsedBytes / (1024 * 1024 * 1024)} GB")
                appendLine()
            }

            if (selectedCategories.contains("Camera")) {
                appendLine("[Camera Info]")
                appendLine("Total Cameras: ${uiState.cameraInfo.cameras.size}")
                appendLine("Active ID: ${uiState.selectedCameraId}")
                appendLine()
            }

            if (selectedCategories.contains("Thermal")) {
                appendLine("[Thermal Info]")
                appendLine("Thermal Status: ${uiState.thermalInfo.overallStatus}")
                appendLine()
            }

            if (selectedCategories.contains("Sensors")) {
                appendLine("[Sensors Info]")
                appendLine("Total Sensors: ${uiState.sensorInfo.sensors.size}")
                appendLine()
            }

            if (selectedCategories.contains("Apps")) {
                appendLine("[Apps Summary]")
                appendLine("Total Apps: ${uiState.appInfo.totalApps}")
                appendLine("User Apps: ${uiState.appInfo.userAppsCount}")
                appendLine("System Apps: ${uiState.appInfo.systemAppsCount}")
                appendLine()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (onBack != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Top Banner Title Box
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Export",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Export Data Intro Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Export Data",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Save your device information to a PDF or Text document by customizing the information you need",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Report Type Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Report Type",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Select the type of the report you want to export",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val types = listOf("Text", "PDF")
                    types.forEach { typeName ->
                        val isSelected = reportType == typeName
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable { reportType = typeName }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = typeName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Categories Selection Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Select the categories of data you want to export",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allCategories.forEach { categoryName ->
                        val isSelected = selectedCategories.contains(categoryName)
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable {
                                if (isSelected) {
                                    if (selectedCategories.size > 1) {
                                        selectedCategories.remove(categoryName)
                                    }
                                } else {
                                    selectedCategories.add(categoryName)
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export Action Button at bottom
        Button(
            onClick = {
                val reportContent = buildCustomReport()
                val fileUri = if (reportType == "PDF") {
                    generatePdfReportFile(context, reportContent)
                } else {
                    generateTextReportFile(context, reportContent)
                }

                if (fileUri != null) {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        putExtra(Intent.EXTRA_TEXT, reportContent)
                        type = if (reportType == "PDF") "application/pdf" else "text/plain"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Export DevPulse $reportType Report")
                    context.startActivity(shareIntent)
                } else {
                    Toast.makeText(context, "Failed to generate report", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52564A)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = "Export",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Export",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
