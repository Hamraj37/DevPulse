package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.data.model.AppSpec
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
fun AppAnalyzerCardItem(
    item: AnalyzerItem,
    total: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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

private fun getAndroidVersionNameForApi(apiLevel: Int): Pair<String, String> {
    return when (apiLevel) {
        36 -> Pair("Android 16 (API 36)", "Baklava")
        35 -> Pair("Android 15 (API 35)", "Vanilla Ice Cream")
        34 -> Pair("Android 14 (API 34)", "Upside Down Cake")
        33 -> Pair("Android 13 (API 33)", "Tiramisu")
        32 -> Pair("Android 12L (API 32)", "Snow Cone")
        31 -> Pair("Android 12 (API 31)", "Snow Cone")
        30 -> Pair("Android 11 (API 30)", "Red Velvet Cake")
        29 -> Pair("Android 10 (API 29)", "Quince Tart")
        28 -> Pair("Android 9 (API 28)", "Pie")
        27 -> Pair("Android 8.1 (API 27)", "Oreo")
        26 -> Pair("Android 8.0 (API 26)", "Oreo")
        25 -> Pair("Android 7.1 (API 25)", "Nougat")
        24 -> Pair("Android 7.0 (API 24)", "Nougat")
        23 -> Pair("Android 6.0 (API 23)", "Marshmallow")
        22 -> Pair("Android 5.1 (API 22)", "Lollipop")
        21 -> Pair("Android 5.0 (API 21)", "Lollipop")
        else -> Pair("Android API $apiLevel", "Target SDK $apiLevel")
    }
}

private fun getAndroidMinSdkNameForApi(apiLevel: Int): Pair<String, String> {
    return when (apiLevel) {
        36 -> Pair("API 36 (Android 16+)", "Baklava")
        35 -> Pair("API 35 (Android 15+)", "Vanilla Ice Cream")
        34 -> Pair("API 34 (Android 14+)", "Upside Down Cake")
        33 -> Pair("API 33 (Android 13+)", "Tiramisu")
        32 -> Pair("API 32 (Android 12L+)", "Snow Cone")
        31 -> Pair("API 31 (Android 12+)", "Snow Cone")
        30 -> Pair("API 30 (Android 11+)", "Red Velvet Cake")
        29 -> Pair("API 29 (Android 10+)", "Quince Tart")
        28 -> Pair("API 28 (Android 9+)", "Pie")
        27 -> Pair("API 27 (Android 8.1+)", "Oreo")
        26 -> Pair("API 26 (Android 8.0+)", "Oreo")
        25 -> Pair("API 25 (Android 7.1+)", "Nougat")
        24 -> Pair("API 24 (Android 7.0+)", "Nougat")
        23 -> Pair("API 23 (Android 6.0+)", "Marshmallow")
        22 -> Pair("API 22 (Android 5.1+)", "Lollipop")
        21 -> Pair("API 21 (Android 5.0+)", "Lollipop")
        else -> Pair("API $apiLevel", "Min SDK $apiLevel")
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

    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Categories") }
    val tabs = remember {
        listOf("Categories", "Installer", "Target SDK", "Minimum SDK", "App Type")
    }

    var selectedCategoryItem by remember { mutableStateOf<AnalyzerItem?>(null) }
    var selectedAppForDetail by remember { mutableStateOf<AppSpec?>(null) }

    val apps = uiState.appInfo.appsList

    val colors = remember {
        listOf(
            Color(0xFFC2185B), Color(0xFFF57C00), Color(0xFFFBC02D), Color(0xFF689F38),
            Color(0xFF1976D2), Color(0xFF7B1FA2), Color(0xFF0097A7), Color(0xFFE64A19),
            Color(0xFF388E3C), Color(0xFF546E7A), Color(0xFF8D6E63), Color(0xFF303F9F)
        )
    }

    val items = remember(selectedTab, apps) {
        if (apps.isNotEmpty()) {
            when (selectedTab) {
                "Categories" -> {
                    val groups = apps.groupBy { it.appCategory }
                    groups.entries.sortedByDescending { it.value.size }.mapIndexed { idx, entry ->
                        AnalyzerItem(
                            label = entry.key,
                            subLabel = "${entry.value.size} Applications",
                            count = entry.value.size,
                            color = colors[idx % colors.size]
                        )
                    }
                }
                "Installer" -> {
                    val groups = apps.groupBy { it.installSource.ifBlank { "Side-loaded / Local" } }
                    groups.entries.sortedByDescending { it.value.size }.mapIndexed { idx, entry ->
                        AnalyzerItem(
                            label = entry.key,
                            subLabel = if (entry.key.contains("Play", true)) "com.android.vending" else "",
                            count = entry.value.size,
                            color = colors[idx % colors.size]
                        )
                    }
                }
                "Target SDK" -> {
                    val groups = apps.groupBy { it.targetSdk }
                    groups.entries.sortedByDescending { it.key }.mapIndexed { idx, entry ->
                        val (title, code) = getAndroidVersionNameForApi(entry.key)
                        AnalyzerItem(
                            label = title,
                            subLabel = "${entry.value.size} Apps • $code",
                            count = entry.value.size,
                            color = colors[idx % colors.size]
                        )
                    }
                }
                "Minimum SDK" -> {
                    val groups = apps.groupBy { it.minSdk }
                    groups.entries.sortedByDescending { it.key }.mapIndexed { idx, entry ->
                        val (title, code) = getAndroidMinSdkNameForApi(entry.key)
                        AnalyzerItem(
                            label = title,
                            subLabel = "${entry.value.size} Apps • $code",
                            count = entry.value.size,
                            color = colors[idx % colors.size]
                        )
                    }
                }
                else -> {
                    val userApps = apps.count { !it.isSystemApp }
                    val systemApps = apps.count { it.isSystemApp }

                    listOf(
                        AnalyzerItem("User Applications", "Installed by user or store", userApps, Color(0xFFF57C00)),
                        AnalyzerItem("Pre-Installed System Apps", "Built-in system applications", systemApps, Color(0xFFC2185B))
                    )
                }
            }
        } else {
            listOf(
                AnalyzerItem("System & Tools", "Built-in system apps", 110, Color(0xFFC2185B)),
                AnalyzerItem("Social & Communication", "Chat, social & messaging", 24, Color(0xFFF57C00)),
                AnalyzerItem("Productivity", "Utilities & productivity", 18, Color(0xFF1976D2)),
                AnalyzerItem("Games", "Games & media apps", 12, Color(0xFF388E3C))
            )
        }
    }

    val sumTotal = items.sumOf { it.count }.coerceAtLeast(1)

    if (selectedCategoryItem != null) {
        CategoryAppListBottomSheet(
            categoryItem = selectedCategoryItem!!,
            allApps = apps,
            selectedTab = selectedTab,
            onSelectApp = { app ->
                selectedAppForDetail = app
            },
            onDismissRequest = { selectedCategoryItem = null }
        )
    }

    if (selectedAppForDetail != null) {
        AppDetailBottomSheet(
            app = selectedAppForDetail!!,
            onDismissRequest = { selectedAppForDetail = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = "App Analyzer",
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    AppAnalyzerCardItem(
                        item = item,
                        total = sumTotal,
                        onClick = { selectedCategoryItem = item }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAppListBottomSheet(
    categoryItem: AnalyzerItem,
    allApps: List<AppSpec>,
    selectedTab: String,
    onSelectApp: (AppSpec) -> Unit,
    onDismissRequest: () -> Unit
) {
    val categoryApps = remember(categoryItem, allApps, selectedTab) {
        when (selectedTab) {
            "Categories" -> allApps.filter { it.appCategory == categoryItem.label }
            "Installer" -> allApps.filter { it.installSource.ifBlank { "Side-loaded / Local" } == categoryItem.label }
            "Target SDK" -> {
                val api = categoryItem.label.substringAfter("API ").substringBefore(")").toIntOrNull()
                    ?: categoryItem.subLabel.substringAfter("Target SDK ").substringBefore(" ").toIntOrNull()
                if (api != null) {
                    allApps.filter { it.targetSdk == api }
                } else {
                    allApps
                }
            }
            "Minimum SDK" -> {
                val api = categoryItem.label.substringAfter("API ").substringBefore(" ").substringBefore(")").toIntOrNull()
                    ?: categoryItem.subLabel.substringAfter("Min SDK ").substringBefore(" ").toIntOrNull()
                if (api != null) {
                    allApps.filter { it.minSdk == api }
                } else {
                    allApps
                }
            }
            else -> when {
                categoryItem.label.contains("Pre-Installed", ignoreCase = true) || categoryItem.label.contains("System", ignoreCase = true) ->
                    allApps.filter { it.isSystemApp }
                else -> allApps.filter { !it.isSystemApp }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState()
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = categoryItem.color.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getIconForLabel(categoryItem.label),
                                contentDescription = null,
                                tint = categoryItem.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = categoryItem.label,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${categoryApps.size} " + stringResource(R.string.lbl_applications),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (categoryApps.isNotEmpty()) {
                    categoryApps.forEach { app ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectApp(app) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AppIconImage(
                                    packageName = app.packageName,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = formatApkSize(app.appSizeBytes),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No apps found in this category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
        }
    }
}
