package com.hamraj37.devpulse.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SsidChart
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.VerifiedUser
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.R
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
    PLAY_INTEGRITY,
    ROOT_CHECKER,
    SETTINGS,
    ABOUT
}

data class ToolItemData(
    val type: ToolType,
    @get:StringRes val titleRes: Int,
    @get:StringRes val descriptionRes: Int,
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
                titleRes = R.string.floating_monitors_title,
                descriptionRes = R.string.floating_monitors_subtitle,
                icon = Icons.Rounded.SsidChart
            ),
            ToolItemData(
                type = ToolType.PERMISSIONS,
                titleRes = R.string.permissions_title,
                descriptionRes = R.string.permissions_desc,
                icon = Icons.Rounded.Shield
            ),
            ToolItemData(
                type = ToolType.WIFI_ANALYZER,
                titleRes = R.string.wifi_analyzer_title,
                descriptionRes = R.string.wifi_analyzer_desc,
                icon = Icons.Rounded.Router
            ),
            ToolItemData(
                type = ToolType.DATA_USAGE,
                titleRes = R.string.data_usage_title,
                descriptionRes = R.string.data_usage_desc,
                icon = Icons.Rounded.SwapHoriz
            ),
            ToolItemData(
                type = ToolType.SCREEN_TIME,
                titleRes = R.string.screen_time_title,
                descriptionRes = R.string.screen_time_desc,
                icon = Icons.Rounded.Smartphone
            ),
            ToolItemData(
                type = ToolType.WIDGETS,
                titleRes = R.string.widgets_title,
                descriptionRes = R.string.widgets_desc,
                icon = Icons.Rounded.GridView
            ),
            ToolItemData(
                type = ToolType.COMPASS,
                titleRes = R.string.compass_title,
                descriptionRes = R.string.compass_desc,
                icon = Icons.Rounded.Explore
            ),
            ToolItemData(
                type = ToolType.APP_ANALYZER,
                titleRes = R.string.app_analyzer_title,
                descriptionRes = R.string.app_analyzer_desc,
                icon = Icons.Rounded.BarChart
            ),
            ToolItemData(
                type = ToolType.PLAY_INTEGRITY,
                titleRes = R.string.play_integrity_title,
                descriptionRes = R.string.play_integrity_desc,
                icon = Icons.Rounded.VerifiedUser
            ),
            ToolItemData(
                type = ToolType.ROOT_CHECKER,
                titleRes = R.string.root_checker_title,
                descriptionRes = R.string.root_checker_desc,
                icon = Icons.Rounded.AdminPanelSettings
            ),
            ToolItemData(
                type = ToolType.EXPORT,
                titleRes = R.string.btn_export,
                descriptionRes = R.string.export_desc,
                icon = Icons.Rounded.PictureAsPdf
            )
        )
    }

    val handleToolBack: () -> Unit = {
        if (initialTool != null && onBack != null) {
            onBack()
        } else {
            selectedTool = null
        }
    }

    when (selectedTool) {
        ToolType.FLOATING_MONITORS -> FloatingMonitorsScreen(onBack = handleToolBack)
        ToolType.PERMISSIONS -> PermissionsScreen(onBack = handleToolBack)
        ToolType.WIFI_ANALYZER -> WifiAnalyzerScreen(uiState = uiState, onBack = handleToolBack)
        ToolType.DATA_USAGE -> DataUsageScreen(uiState = uiState, onBack = handleToolBack)
        ToolType.SCREEN_TIME -> ScreenTimeScreen(onBack = handleToolBack)
        ToolType.WIDGETS -> WidgetsScreen(uiState = uiState, onBack = handleToolBack)
        ToolType.COMPASS -> CompassScreen(onBack = handleToolBack)
        ToolType.EXPORT -> ExportScreen(uiState = uiState, onBack = handleToolBack)
        ToolType.APP_ANALYZER -> AppAnalyzerScreen(uiState = uiState, onBack = handleToolBack)
        ToolType.PLAY_INTEGRITY -> PlayIntegrityScreen(onBack = handleToolBack)
        ToolType.ROOT_CHECKER -> RootCheckerScreen(uiState = uiState, onBack = handleToolBack)
        ToolType.SETTINGS -> SettingsScreen(
            uiState = uiState,
            onThemeModeChange = onThemeModeChange,
            onBack = handleToolBack,
            onNavigateToExport = { selectedTool = ToolType.EXPORT }
        )
        ToolType.ABOUT -> AboutScreen(onBack = handleToolBack)
        null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                // Top Action Header
                DevPulseTopAppBar(
                    title = stringResource(R.string.tools_hdr),
                    onBack = onBack,
                    actions = {
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
                                    text = { Text(stringResource(R.string.btn_refresh_tools)) },
                                    onClick = {
                                        showMenu = false
                                        Toast.makeText(context, context.getString(R.string.toast_tools_refreshed), Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.btn_export_full_report)) },
                                    onClick = {
                                        showMenu = false
                                        selectedTool = ToolType.EXPORT
                                    }
                                )
                            }
                        }
                    }
                )

                // Tools List Cards
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
}

@Composable
fun ToolCardItem(
    tool: ToolItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = stringResource(tool.titleRes)
    val desc = stringResource(tool.descriptionRes)
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
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = desc,
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
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
