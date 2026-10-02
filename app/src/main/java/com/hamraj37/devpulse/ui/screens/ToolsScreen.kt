package com.hamraj37.devpulse.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SsidChart
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.data.model.AppTab
import com.hamraj37.devpulse.ui.MainUiState
import com.hamraj37.devpulse.ui.theme.DevPulseTheme

enum class ToolType {
    FLOATING_MONITORS,
    PERMISSIONS,
    WIFI_ANALYZER,
    DATA_USAGE,
    SCREEN_TIME,
    WIDGETS,
    COMPASS,
    EXPORT,
    APP_ANALYZER,
    SETTINGS
}

data class ToolItemData(
    val type: ToolType,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBgColor: Color? = null
)

@Composable
fun ToolsScreen(
    uiState: MainUiState,
    onNavigateToTab: (AppTab) -> Unit,
    onBack: (() -> Unit)? = null,
    initialTool: ToolType? = null,
    onThemeModeChange: (String) -> Unit = {},
    onUseSystemColorsChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    var selectedTool by remember(initialTool) { mutableStateOf<ToolType?>(initialTool) }
    var showMenu by remember { mutableStateOf(false) }

    val toolsList = remember {
        listOf(
            ToolItemData(
                type = ToolType.FLOATING_MONITORS,
                title = "Floating Monitors",
                description = "Live system stats, floating on screen",
                icon = Icons.Rounded.SsidChart
            ),
            ToolItemData(
                type = ToolType.PERMISSIONS,
                title = "Permissions",
                description = "View the permissions requested by apps",
                icon = Icons.Rounded.Shield
            ),
            ToolItemData(
                type = ToolType.WIFI_ANALYZER,
                title = "Wi-Fi Analyzer",
                description = "Scan and analyze nearby Wi-Fi networks",
                icon = Icons.Rounded.Router
            ),
            ToolItemData(
                type = ToolType.DATA_USAGE,
                title = "Data Usage",
                description = "View Mobile and Wi-Fi Data Usage",
                icon = Icons.Rounded.SwapHoriz
            ),
            ToolItemData(
                type = ToolType.SCREEN_TIME,
                title = "Screen Time",
                description = "View daily screen time and app usage",
                icon = Icons.Rounded.Smartphone
            ),
            ToolItemData(
                type = ToolType.WIDGETS,
                title = "Widgets",
                description = "Preview of the widgets available",
                icon = Icons.Rounded.GridView
            ),
            ToolItemData(
                type = ToolType.APP_ANALYZER,
                title = "App Analyzer",
                description = "Analyze installed apps by installer, target & min SDK",
                icon = Icons.Rounded.BarChart
            ),
            ToolItemData(
                type = ToolType.COMPASS,
                title = "Compass",
                description = "Find your directions with compass",
                icon = Icons.Rounded.Explore
            ),
            ToolItemData(
                type = ToolType.EXPORT,
                title = "Export",
                description = "Export information to PDF or Text",
                icon = Icons.Rounded.PictureAsPdf
            )
        )
    }

    val handleToolBack: () -> Unit = {
        if (initialTool != null) {
            onBack?.invoke()
        } else {
            selectedTool = null
        }
    }

    if (selectedTool != null) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTool) {
                ToolType.FLOATING_MONITORS -> FloatingMonitorsScreen(
                    context = context,
                    onBack = handleToolBack
                )
                ToolType.PERMISSIONS -> PermissionsScreen(
                    uiState = uiState,
                    onBack = handleToolBack
                )
                ToolType.WIFI_ANALYZER -> WifiAnalyzerScreen(
                    uiState = uiState,
                    onNavigateToNetwork = {
                        selectedTool = null
                        onNavigateToTab(AppTab.NETWORK)
                    },
                    onBack = handleToolBack
                )
                ToolType.DATA_USAGE -> DataUsageScreen(
                    uiState = uiState,
                    onBack = handleToolBack
                )
                ToolType.SCREEN_TIME -> ScreenTimeScreen(
                    context = context,
                    onBack = handleToolBack
                )
                ToolType.WIDGETS -> WidgetsScreen(
                    uiState = uiState,
                    onBack = handleToolBack
                )
                ToolType.APP_ANALYZER -> AppAnalyzerScreen(
                    uiState = uiState,
                    onBack = handleToolBack
                )
                ToolType.COMPASS -> CompassScreen(
                    context = context,
                    onBack = handleToolBack
                )
                ToolType.EXPORT -> ExportScreen(
                    uiState = uiState,
                    context = context,
                    onBack = handleToolBack
                )
                ToolType.SETTINGS -> SettingsScreen(
                    uiState = uiState,
                    onThemeModeChange = onThemeModeChange,
                    onUseSystemColorsChange = onUseSystemColorsChange,
                    onBack = handleToolBack,
                    onNavigateToExport = { selectedTool = ToolType.EXPORT }
                )
                else -> {}
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Header
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "Tools",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Refresh Tools") },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, "Tools refreshed", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Full System Report") },
                                onClick = {
                                    showMenu = false
                                    selectedTool = ToolType.EXPORT
                                }
                            )
                        }
                    }
                }
            }

            // Tools List Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                toolsList.forEach { tool ->
                    ToolCardItem(
                        tool = tool,
                        onClick = { selectedTool = tool.type }
                    )
                }
            }
        }
    }
}

@Composable
fun ToolCardItem(
    tool: ToolItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.title,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ToolsScreenPreview() {
    DevPulseTheme {
        ToolsScreen(
            uiState = MainUiState(),
            onNavigateToTab = {}
        )
    }
}
