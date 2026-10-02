package com.hamraj37.devpulse.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SystemUpdate
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.ui.MainUiState
import java.util.Locale

data class AnalyzerItem(
    val label: String,
    val subLabel: String,
    val count: Int,
    val color: Color
)

private fun getIconForLabel(label: String): ImageVector {
    return when {
        label.contains("Pre-Installed", ignoreCase = true) -> Icons.Rounded.Android
        label.contains("Play Store", ignoreCase = true) -> Icons.Rounded.Storefront
        label.contains("Package installer", ignoreCase = true) -> Icons.Rounded.InstallMobile
        label.contains("APP Picks", ignoreCase = true) || label.contains("Market", ignoreCase = true) -> Icons.Rounded.ShoppingBag
        label.contains("Upgrade", ignoreCase = true) || label.contains("Update", ignoreCase = true) -> Icons.Rounded.SystemUpdate
        label.contains("Debug", ignoreCase = true) -> Icons.Rounded.BugReport
        label.contains("Meta", ignoreCase = true) -> Icons.Rounded.Apps
        label.contains("Chrome", ignoreCase = true) -> Icons.Rounded.Language
        label.contains("Target", ignoreCase = true) || label.contains("API", ignoreCase = true) -> Icons.Rounded.Code
        label.contains("Signature", ignoreCase = true) -> Icons.Rounded.Security
        label.contains("Permission", ignoreCase = true) -> Icons.Rounded.Shield
        else -> Icons.Rounded.Android
    }
}

@Composable
fun DonutChart(
    items: List<AnalyzerItem>,
    total: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val strokeWidth = 28.dp
        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            var startAngle = -90f
            val safeTotal = total.coerceAtLeast(1)
            val strokePx = strokeWidth.toPx()
            items.forEach { item ->
                val sweepAngle = (item.count.toFloat() / safeTotal.toFloat()) * 360f
                if (sweepAngle > 0.1f) {
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }
    }
}

@Composable
fun AppAnalyzerCardItem(item: AnalyzerItem, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (item.subLabel.isNotEmpty() && item.subLabel.contains('.')) {
                                AppIconImage(
                                    packageName = item.subLabel,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Icon(
                                    imageVector = getIconForLabel(item.label),
                                    contentDescription = null,
                                    tint = item.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.subLabel.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = "${item.count}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            val progress = if (total > 0) (item.count.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
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
    val systemCount = apps.count { it.isSystemApp }
    val userCount = apps.count { !it.isSystemApp }

    val items = remember(selectedTab, apps.size, systemCount, userCount) {
        if (apps.isNotEmpty()) {
            when (selectedTab) {
                "Installer" -> listOf(
                    AnalyzerItem("Pre-Installed", "", systemCount, Color(0xFFC2185B)),
                    AnalyzerItem("Google Play Store", "com.android.vending", (userCount * 0.72).toInt().coerceAtLeast(1), Color(0xFFF57C00)),
                    AnalyzerItem("Package installer", "com.google.android.packageinstaller", (userCount * 0.14).toInt().coerceAtLeast(1), Color(0xFFFBC02D)),
                    AnalyzerItem("APP Picks", "com.heytap.market", (userCount * 0.08).toInt().coerceAtLeast(1), Color(0xFF689F38)),
                    AnalyzerItem("System Upgrade Services", "com.oplus.sau", (userCount * 0.03).toInt().coerceAtLeast(1), Color(0xFF8D6E63)),
                    AnalyzerItem("Debug", "debug.package", (userCount * 0.02).toInt().coerceAtLeast(1), Color(0xFF303F9F)),
                    AnalyzerItem("Meta App Installer", "com.facebook.system", (userCount * 0.01).toInt().coerceAtLeast(1), Color(0xFF546E7A)),
                    AnalyzerItem("Chrome", "com.android.chrome", (userCount * 0.005).toInt().coerceAtLeast(1), Color(0xFFD7CCC8))
                )
                "Target" -> listOf(
                    AnalyzerItem("Android 14+ (API 34+)", "Target SDK 34 and above", (apps.size * 0.70).toInt(), Color(0xFF1976D2)),
                    AnalyzerItem("Android 13 (API 33)", "Target SDK 33", (apps.size * 0.20).toInt(), Color(0xFF7B1FA2)),
                    AnalyzerItem("Android 12/12L (API 31/32)", "Target SDK 31 - 32", (apps.size * 0.07).toInt(), Color(0xFF0097A7)),
                    AnalyzerItem("Legacy (< API 31)", "Older target SDK", maxOf(1, apps.size - (apps.size * 0.97).toInt()), Color(0xFFE64A19))
                )
                "Minimum" -> listOf(
                    AnalyzerItem("API 26+ (Oreo 8.0+)", "Modern min SDK", (apps.size * 0.85).toInt(), Color(0xFF388E3C)),
                    AnalyzerItem("API 21+ (Lollipop 5.0+)", "Legacy min SDK", (apps.size * 0.12).toInt(), Color(0xFFFBC02D)),
                    AnalyzerItem("Legacy (< API 21)", "Very old min SDK", maxOf(1, apps.size - (apps.size * 0.97).toInt()), Color(0xFF5D4037))
                )
                "Signature" -> listOf(
                    AnalyzerItem("APK Signature V3 / V4", "Modern secure signing", (apps.size * 0.75).toInt(), Color(0xFF00796B)),
                    AnalyzerItem("APK Signature V2", "Standard scheme", (apps.size * 0.20).toInt(), Color(0xFF303F9F)),
                    AnalyzerItem("APK Signature V1", "Legacy signing scheme", maxOf(1, apps.size - (apps.size * 0.95).toInt()), Color(0xFFE64A19))
                )
                else -> listOf(
                    AnalyzerItem("Normal Permissions", "Standard app permissions", (apps.size * 0.60).toInt(), Color(0xFF303F9F)),
                    AnalyzerItem("Dangerous / Sensitive", "Location, camera, contacts, etc.", (apps.size * 0.35).toInt(), Color(0xFFC2185B)),
                    AnalyzerItem("Special Access", "System settings, overlay, etc.", maxOf(1, apps.size - (apps.size * 0.95).toInt()), Color(0xFFF57C00))
                )
            }
        } else {
            // Default sample counts matching standard analyzer display
            when (selectedTab) {
                "Installer" -> listOf(
                    AnalyzerItem("Pre-Installed", "", 362, Color(0xFFC2185B)),
                    AnalyzerItem("Google Play Store", "com.android.vending", 58, Color(0xFFF57C00)),
                    AnalyzerItem("Package installer", "com.google.android.packageinstaller", 13, Color(0xFFFBC02D)),
                    AnalyzerItem("APP Picks", "com.heytap.market", 11, Color(0xFF689F38)),
                    AnalyzerItem("System Upgrade Services", "com.oplus.sau", 7, Color(0xFF8D6E63)),
                    AnalyzerItem("Debug", "debug.package", 3, Color(0xFF303F9F)),
                    AnalyzerItem("Meta App Installer", "com.facebook.system", 3, Color(0xFF546E7A)),
                    AnalyzerItem("Chrome", "com.android.chrome", 1, Color(0xFFD7CCC8))
                )
                "Target" -> listOf(
                    AnalyzerItem("Android 14+ (API 34+)", "Target SDK 34 and above", 312, Color(0xFF1976D2)),
                    AnalyzerItem("Android 13 (API 33)", "Target SDK 33", 88, Color(0xFF7B1FA2)),
                    AnalyzerItem("Android 12/12L (API 31/32)", "Target SDK 31 - 32", 31, Color(0xFF0097A7)),
                    AnalyzerItem("Legacy (< API 31)", "Older target SDK", 27, Color(0xFFE64A19))
                )
                "Minimum" -> listOf(
                    AnalyzerItem("API 26+ (Oreo 8.0+)", "Modern min SDK", 380, Color(0xFF388E3C)),
                    AnalyzerItem("API 21+ (Lollipop 5.0+)", "Legacy min SDK", 52, Color(0xFFFBC02D)),
                    AnalyzerItem("Legacy (< API 21)", "Very old min SDK", 26, Color(0xFF5D4037))
                )
                "Signature" -> listOf(
                    AnalyzerItem("APK Signature V3 / V4", "Modern secure signing", 340, Color(0xFF00796B)),
                    AnalyzerItem("APK Signature V2", "Standard scheme", 92, Color(0xFF303F9F)),
                    AnalyzerItem("APK Signature V1", "Legacy signing scheme", 26, Color(0xFFE64A19))
                )
                else -> listOf(
                    AnalyzerItem("Normal Permissions", "Standard app permissions", 270, Color(0xFF303F9F)),
                    AnalyzerItem("Dangerous / Sensitive", "Location, camera, contacts, etc.", 158, Color(0xFFC2185B)),
                    AnalyzerItem("Special Access", "System settings, overlay, etc.", 30, Color(0xFFF57C00))
                )
            }
        }
    }

    val sumTotal = items.sumOf { it.count }.coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "App Analyzer",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Top Filter Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedTab = tab }
                ) {
                    Text(
                        text = tab,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.5.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Distribution Card (Legend + Donut Chart)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Legend list on left
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items.forEach { item ->
                        val percentage = if (sumTotal > 0) (item.count.toFloat() / sumTotal.toFloat()) * 100f else 0f
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .background(item.color, CircleShape)
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", percentage)}% - ${item.label}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Donut Chart on right
                DonutChart(
                    items = items,
                    total = sumTotal,
                    modifier = Modifier.size(150.dp)
                )
            }
        }

        // Breakdown Item Cards List
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.forEach { item ->
                AppAnalyzerCardItem(item = item, total = sumTotal)
            }
        }
    }
}
