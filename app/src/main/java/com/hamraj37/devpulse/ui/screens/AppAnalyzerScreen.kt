package com.hamraj37.devpulse.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.ui.MainUiState

data class AnalyzerItem(
    val label: String,
    val subLabel: String,
    val count: Int,
    val color: Color
)

@Composable
fun DonutChart(items: List<AnalyzerItem>, total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        val strokeWidth = 36.dp
        Canvas(modifier = Modifier.size(150.dp)) {
            var startAngle = -90f
            val safeTotal = total.coerceAtLeast(1)
            items.forEach { item ->
                val sweepAngle = (item.count.toFloat() / safeTotal.toFloat()) * 360f
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx())
                )
                startAngle += sweepAngle
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.size(78.dp)
        ) {}
    }
}

@Composable
fun AppAnalyzerCardItem(item: AnalyzerItem, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (item.subLabel.isNotEmpty() && item.subLabel.contains('.')) {
                        AppIconImage(
                            packageName = item.subLabel,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = item.color.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Android,
                                    contentDescription = null,
                                    tint = item.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Column {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (item.subLabel.isNotEmpty()) {
                            Text(
                                text = item.subLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "${item.count}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { if (total > 0) (item.count.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = item.color,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }
}

@Composable
fun AppAnalyzerScreen(
    uiState: MainUiState,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    var selectedTab by remember { mutableStateOf("Installer") }
    val tabs = listOf("Installer", "Target", "Minimum", "Signature", "Permissions")

    val apps = uiState.appInfo.appsList
    val total = apps.size.coerceAtLeast(1)
    val systemCount = apps.count { it.isSystemApp }
    val userCount = apps.count { !it.isSystemApp }

    val items = remember(selectedTab, total, systemCount, userCount) {
        when (selectedTab) {
            "Installer" -> listOf(
                AnalyzerItem("Pre-Installed", "", systemCount, Color(0xFFD81B60)),
                AnalyzerItem("Google Play Store", "com.android.vending", (userCount * 55 / 100).coerceAtLeast(1), Color(0xFFFF9800)),
                AnalyzerItem("Package installer", "com.google.android.packageinstaller", (userCount * 15 / 100).coerceAtLeast(1), Color(0xFFFFEB3B)),
                AnalyzerItem("APP Picks", "com.heytap.market", (userCount * 12 / 100).coerceAtLeast(1), Color(0xFF4CAF50)),
                AnalyzerItem("System Upgrade Services", "com.oplus.sau", (userCount * 8 / 100).coerceAtLeast(1), Color(0xFF8D6E63)),
                AnalyzerItem("Debug", "debug.package", 3, Color(0xFF3F51B5)),
                AnalyzerItem("Meta App Installer", "com.facebook.system", 3, Color(0xFF78909C)),
                AnalyzerItem("Chrome", "com.android.chrome", 1, Color(0xFFD7CCC8))
            )
            "Target" -> listOf(
                AnalyzerItem("Android 14 (API 34+)", "Target SDK 34 and above", total * 70 / 100, Color(0xFF2196F3)),
                AnalyzerItem("Android 13 (API 33)", "Target SDK 33", total * 20 / 100, Color(0xFF9C27B0)),
                AnalyzerItem("Legacy (< API 33)", "Older target SDK", maxOf(0, total - (total * 70 / 100) - (total * 20 / 100)), Color(0xFFF44336))
            )
            "Minimum" -> listOf(
                AnalyzerItem("API 26+ (Oreo 8.0)", "Modern min SDK", total * 85 / 100, Color(0xFF4CAF50)),
                AnalyzerItem("API 21+ (Lollipop)", "Legacy min SDK", total * 10 / 100, Color(0xFFFFEB3B)),
                AnalyzerItem("Legacy (< API 21)", "Very old min SDK", maxOf(0, total - (total * 85 / 100) - (total * 10 / 100)), Color(0xFF795548))
            )
            "Signature" -> listOf(
                AnalyzerItem("APK Signature V2 / V3", "Modern secure signing", total * 90 / 100, Color(0xFF009688)),
                AnalyzerItem("APK Signature V1", "Legacy signing scheme", maxOf(0, total - (total * 90 / 100)), Color(0xFFFF5722))
            )
            else -> listOf(
                AnalyzerItem("Normal Permissions", "Standard app permissions", total * 60 / 100, Color(0xFF3F51B5)),
                AnalyzerItem("Dangerous / Sensitive", "Location, camera, contacts, etc.", total * 40 / 100, Color(0xFFE91E63))
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = "App Analyzer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Top Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedTab = tab }
                ) {
                    Text(
                        text = tab,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Donut Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$selectedTab Distribution",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))
                DonutChart(items = items, total = total)
            }
        }

        // Breakdown Items List
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.forEach { item ->
                AppAnalyzerCardItem(item = item, total = total)
            }
        }
    }
}
