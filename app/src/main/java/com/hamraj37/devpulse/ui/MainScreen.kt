package com.hamraj37.devpulse.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.VerifiedUser
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.hamraj37.devpulse.ui.screens.ToolType
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.runtime.key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.AppTab
import com.hamraj37.devpulse.data.model.DashboardInfo
import com.hamraj37.devpulse.data.model.DeviceInfo
import com.hamraj37.devpulse.data.model.TestStatus
import com.hamraj37.devpulse.ui.components.DevPulseTabRow
import com.hamraj37.devpulse.ui.screens.AccelerometerTestScreen
import com.hamraj37.devpulse.ui.screens.AutomaticTestsScreen
import com.hamraj37.devpulse.ui.screens.BiometricTestScreen
import com.hamraj37.devpulse.ui.screens.BluetoothTestScreen
import com.hamraj37.devpulse.ui.screens.ChargingTestScreen
import com.hamraj37.devpulse.ui.screens.DisplayTestScreen
import com.hamraj37.devpulse.ui.screens.EarSpeakerTestScreen
import com.hamraj37.devpulse.ui.screens.FlashlightTestScreen
import com.hamraj37.devpulse.ui.screens.GpsTestScreen
import com.hamraj37.devpulse.ui.screens.LightSensorTestScreen
import com.hamraj37.devpulse.ui.screens.MicTestScreen
import com.hamraj37.devpulse.ui.screens.MultitouchTestScreen
import com.hamraj37.devpulse.ui.screens.ProximityTestScreen
import com.hamraj37.devpulse.ui.screens.SpeakerTestScreen
import com.hamraj37.devpulse.ui.screens.VibrationTestScreen
import com.hamraj37.devpulse.ui.screens.VolumeButtonTestScreen
import com.hamraj37.devpulse.ui.screens.AppsScreen
import com.hamraj37.devpulse.ui.screens.BatteryScreen
import com.hamraj37.devpulse.ui.screens.CameraScreen
import com.hamraj37.devpulse.ui.screens.ConnectivityScreen
import com.hamraj37.devpulse.ui.screens.CpuScreen
import com.hamraj37.devpulse.ui.screens.DashboardScreen
import com.hamraj37.devpulse.ui.screens.ToolsScreen
import com.hamraj37.devpulse.ui.screens.DeviceScreen
import com.hamraj37.devpulse.ui.screens.DisplayScreen
import com.hamraj37.devpulse.ui.screens.MemoryScreen
import com.hamraj37.devpulse.ui.screens.NetworkScreen
import com.hamraj37.devpulse.ui.screens.SensorsScreen
import com.hamraj37.devpulse.ui.screens.SystemScreen
import com.hamraj37.devpulse.ui.screens.TestsScreen
import com.hamraj37.devpulse.ui.screens.ThermalScreen
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import androidx.lifecycle.compose.collectAsStateWithLifecycle

fun getRequiredPermissions(): Array<String> {
    val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    return permissions.toTypedArray()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val requiredPermissions = remember { getRequiredPermissions() }
    val ungrantedPermissions = remember(context) {
        requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    var showPermissionDialog by remember { mutableStateOf(ungrantedPermissions.isNotEmpty()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.refreshTelemetry()
    }

    LaunchedEffect(Unit) {
        if (ungrantedPermissions.isEmpty()) {
            viewModel.refreshTelemetry()
        }
    }

    if (showPermissionDialog && ungrantedPermissions.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDialog = false
                viewModel.refreshTelemetry()
            },
            title = {
                Text(
                    text = stringResource(R.string.dialog_permissions_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.dialog_permissions_desc))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(ungrantedPermissions.toTypedArray())
                    }
                ) {
                    Text(text = stringResource(R.string.btn_grant_permission))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        viewModel.refreshTelemetry()
                    }
                ) {
                    Text(text = stringResource(R.string.btn_later))
                }
            }
        )
    }

    val tabs = AppTab.entriesOrdered
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )
    var scrollJob by remember { mutableStateOf<Job?>(null) }
    var showTopMenu by remember { mutableStateOf(false) }

    // Sync selected tab with pager state
    LaunchedEffect(pagerState.currentPage) {
        val newTab = tabs.getOrNull(pagerState.currentPage) ?: AppTab.DASHBOARD
        if (uiState.selectedTab != newTab) {
            viewModel.selectTab(newTab)
        }
    }

    // Sync pager state when tab is clicked or updated
    val selectedIndex = tabs.indexOf(uiState.selectedTab).coerceIn(0, (tabs.size - 1).coerceAtLeast(0))
    LaunchedEffect(uiState.selectedTab) {
        if (pagerState.currentPage != selectedIndex && selectedIndex in tabs.indices) {
            if (scrollJob?.isActive != true) {
                scrollJob?.cancel()
                scrollJob = coroutineScope.launch {
                    if (pagerState.isScrollInProgress) {
                        pagerState.scrollToPage(selectedIndex)
                    } else {
                        pagerState.animateScrollToPage(selectedIndex)
                    }
                }
            }
        }
    }

    if (uiState.showUpdateDialog) {
        uiState.updateInfo?.let { release ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(text = "Update Available (${release.tagName})")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "A new version of DevPulse is available on GitHub!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Changelog:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = release.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                            viewModel.dismissUpdateDialog()
                        }
                    ) {
                        Text(stringResource(R.string.btn_download_update))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissUpdateDialog() }
                    ) {
                        Text(stringResource(R.string.btn_later))
                    }
                }
            )
        }
    }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        val appVersion = remember {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "2.8"
            } catch (_: Throwable) {
                "2.8"
            }
        }

        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "DevPulse Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "DevPulse", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Version $appVersion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Real-time hardware telemetry, system diagnostics, and developer tools for Android.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Features:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• System, CPU, RAM & Battery Gauges\n• Floating Performance Overlays\n• Wi-Fi & Network Signal Analyzer\n• App Permission & Target SDK Inspector\n• Hardware Sensor & Camera Directory\n• Custom Telemetry Report Exporter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    if (showSettingsDialog) {
        var autoRefresh by remember { mutableStateOf(true) }
        var celsiusUnit by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(text = stringResource(R.string.btn_app_settings), fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Refresh Telemetry",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Continuously poll live CPU, RAM & Network stats",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoRefresh,
                            onCheckedChange = { autoRefresh = it }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Temperature Unit (°C)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Use Celsius for battery & thermal sensors",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = celsiusUnit,
                            onCheckedChange = { celsiusUnit = it }
                        )
                    }

                    HorizontalDivider()

                    Button(
                        onClick = {
                            showSettingsDialog = false
                            viewModel.openTool(ToolType.FLOATING_MONITORS)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.dialog_configure_floating_monitors))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(stringResource(R.string.btn_done))
                }
            }
        )
    }

    BackHandler(enabled = uiState.selectedTab != AppTab.DASHBOARD || uiState.isToolsPageOpen || uiState.activeTestId != null) {
        if (uiState.activeTestId != null) {
            viewModel.closeActiveTest()
        } else if (uiState.isToolsPageOpen) {
            viewModel.setToolsPageOpen(false)
        } else {
            viewModel.selectTab(AppTab.DASHBOARD)
            scrollJob?.cancel()
            scrollJob = coroutineScope.launch {
                if (pagerState.isScrollInProgress) {
                    pagerState.scrollToPage(0)
                } else {
                    pagerState.animateScrollToPage(0)
                }
            }
        }
    }

        if (uiState.activeTestId != null) {
            Surface(
                modifier = modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val testId = uiState.activeTestId!!
                when (testId) {
                    "automatic" -> AutomaticTestsScreen(onBack = { viewModel.closeActiveTest() })
                    "test_touch" -> MultitouchTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_touch", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_touch", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_display" -> DisplayTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_display", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_display", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_flashlight" -> FlashlightTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_flashlight", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_flashlight", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_speaker" -> SpeakerTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_speaker", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_speaker", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_earspeaker" -> EarSpeakerTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_earspeaker", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_earspeaker", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_mic" -> MicTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_mic", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_mic", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_proximity" -> ProximityTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_proximity", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_proximity", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_light" -> LightSensorTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_light", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_light", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_accel" -> AccelerometerTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_accel", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_accel", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_charging" -> ChargingTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_charging", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_charging", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_vibration" -> VibrationTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_vibration", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_vibration", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_bluetooth" -> BluetoothTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_bluetooth", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_bluetooth", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_fingerprint" -> BiometricTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_fingerprint", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_fingerprint", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_gps" -> GpsTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_gps", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_gps", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    "test_volume" -> VolumeButtonTestScreen(
                        onBack = { viewModel.closeActiveTest() },
                        onPass = { viewModel.updateTestStatus("test_volume", TestStatus.PASSED); viewModel.closeActiveTest() },
                        onFail = { viewModel.updateTestStatus("test_volume", TestStatus.FAILED); viewModel.closeActiveTest() }
                    )
                    else -> viewModel.closeActiveTest()
                }
            }
        } else if (uiState.isToolsPageOpen) {
            Surface(
                modifier = modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.background
            ) {
                ToolsScreen(
                    uiState = uiState,
                    initialTool = uiState.initialTool,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onThemePaletteChange = { viewModel.setThemePalette(it) },
                    onMonetToggle = { viewModel.setMonetEnabled(it) },
                    onBack = { viewModel.setToolsPageOpen(false) },
                    onNavigateToTab = { tab ->
                        viewModel.selectTab(tab)
                        viewModel.setToolsPageOpen(false)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                Column(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = "App Icon",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DevPulse",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = uiState.deviceInfo.deviceName.ifEmpty { uiState.deviceInfo.model.ifEmpty { "Pixel 8 Pro" } },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.setToolsPageOpen(true) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Build,
                                    contentDescription = "Tools Page"
                                )
                            }
                            Box {
                                IconButton(onClick = { showTopMenu = !showTopMenu }) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.5.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreVert,
                                            contentDescription = "Menu Options"
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showTopMenu,
                                    onDismissRequest = { showTopMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_refresh_telemetry)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.refreshTelemetry()
                                        }
                                    )

                                    HorizontalDivider()

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_app_analyzer)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.BarChart,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.APP_ANALYZER)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_wifi_analyzer)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Router,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.WIFI_ANALYZER)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_permissions)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Shield,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.PERMISSIONS)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_play_integrity)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.VerifiedUser,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.PLAY_INTEGRITY)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_root_checker)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.AdminPanelSettings,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.ROOT_CHECKER)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_data_usage)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.SwapHoriz,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.DATA_USAGE)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_widgets)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.GridView,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.WIDGETS)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_export_report)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.PictureAsPdf,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.EXPORT)
                                        }
                                    )

                                    HorizontalDivider()

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_settings)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Settings,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.SETTINGS)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_about)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Info,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showTopMenu = false
                                            viewModel.openTool(ToolType.ABOUT)
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    DevPulseTabRow(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = { tab ->
                            val index = tabs.indexOf(tab)
                            if (index in tabs.indices) {
                                viewModel.selectTab(tab)
                                scrollJob?.cancel()
                                scrollJob = coroutineScope.launch {
                                    if (pagerState.isScrollInProgress) {
                                        pagerState.scrollToPage(index)
                                    } else {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            }
                        }
                    )

                    if (uiState.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { pageIndex ->
                val currentTab = tabs[pageIndex]
                key(currentTab) {
                    TabContentScreen(
                        tab = currentTab,
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun TabContentScreen(
    tab: AppTab,
    uiState: MainUiState,
    onSelectTab: (AppTab) -> Unit = {},
    onSetToolsPageOpen: (Boolean) -> Unit = {},
    onOpenTool: (ToolType) -> Unit = {},
    onSelectCamera: (String) -> Unit = {},
    onAppSearchQueryChange: (String) -> Unit = {},
    onAppCategoryFilterChange: (String) -> Unit = {},
    onUpdateTestStatus: (String, TestStatus) -> Unit = { _, _ -> },
    onStartTest: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (tab) {
            AppTab.DASHBOARD -> DashboardScreen(
                dashboardInfo = uiState.dashboardInfo,
                testsList = uiState.testsList,
                onNavigateToTests = { onSelectTab(AppTab.TESTS) },
                onNavigateToDisplay = { onSelectTab(AppTab.DISPLAY) },
                onNavigateToBattery = { onSelectTab(AppTab.BATTERY) },
                onNavigateToTools = { onSetToolsPageOpen(true) },
                onNavigateToApps = { onSelectTab(AppTab.APPS) },
                onNavigateToSensors = { onSelectTab(AppTab.SENSORS) },
                onNavigateToAppAnalyzer = { onOpenTool(ToolType.APP_ANALYZER) },
                onNavigateToExport = { onOpenTool(ToolType.EXPORT) }
            )
            AppTab.DEVICE -> DeviceScreen(uiState.deviceInfo)
            AppTab.SYSTEM -> SystemScreen(uiState.systemInfo)
            AppTab.CPU -> CpuScreen(uiState.cpuInfo)
            AppTab.BATTERY -> BatteryScreen(uiState.batteryInfo)
            AppTab.MEMORY -> MemoryScreen(uiState.memoryInfo)
            AppTab.NETWORK -> NetworkScreen(
                networkInfo = uiState.networkInfo,
                onNavigateToDataUsage = { onOpenTool(ToolType.DATA_USAGE) }
            )
            AppTab.CONNECTIVITY -> ConnectivityScreen(uiState.connectivityInfo)
            AppTab.DISPLAY -> DisplayScreen(uiState.displayInfo)
            AppTab.THERMAL -> ThermalScreen(uiState.thermalInfo)
            AppTab.SENSORS -> SensorsScreen(uiState.sensorInfo)
            AppTab.CAMERA -> CameraScreen(
                cameraInfo = uiState.cameraInfo,
                selectedCameraId = uiState.selectedCameraId,
                onSelectCamera = onSelectCamera
            )
            AppTab.APPS -> AppsScreen(
                appInfo = uiState.appInfo,
                searchQuery = uiState.appSearchQuery,
                categoryFilter = uiState.appCategoryFilter,
                onSearchQueryChange = onAppSearchQueryChange,
                onCategoryFilterChange = onAppCategoryFilterChange
            )
            AppTab.TESTS -> TestsScreen(
                testsList = uiState.testsList,
                onUpdateTestStatus = onUpdateTestStatus,
                onStartTest = onStartTest
            )
        }
    }
}

@Composable
fun TabContentScreen(
    tab: AppTab,
    uiState: MainUiState,
    viewModel: MainViewModel
) {
    TabContentScreen(
        tab = tab,
        uiState = uiState,
        onSelectTab = { viewModel.selectTab(it) },
        onSetToolsPageOpen = { viewModel.setToolsPageOpen(it) },
        onOpenTool = { viewModel.openTool(it) },
        onSelectCamera = { viewModel.selectCamera(it) },
        onAppSearchQueryChange = { viewModel.setAppSearchQuery(it) },
        onAppCategoryFilterChange = { viewModel.setAppCategoryFilter(it) },
        onUpdateTestStatus = { testId, status -> viewModel.updateTestStatus(testId, status) },
        onStartTest = { viewModel.openTest(it) }
    )
}

fun getTabDescription(tab: AppTab): String {
    return when (tab) {
        AppTab.DASHBOARD -> "Real-time system gauges & hardware telemetry"
        AppTab.DEVICE -> "Device identity, model, board & build information"
        AppTab.SYSTEM -> "Android OS, kernel, security patch & DRM level"
        AppTab.CPU -> "Processor specs, core speeds & GPU hardware"
        AppTab.BATTERY -> "Battery health, current mA, voltage & power"
        AppTab.NETWORK -> "IP address, IPv6, Wi-Fi link speed & DNS"
        AppTab.CONNECTIVITY -> "Wi-Fi 6E, Bluetooth, NFC, UWB & USB capabilities"
        AppTab.DISPLAY -> "Resolution, density, refresh rates & HDR support"
        AppTab.MEMORY -> "RAM usage, internal storage & system partitions"
        AppTab.CAMERA -> "Lens apertures, resolutions, AF modes & OIS"
        AppTab.THERMAL -> "Thermal zones, temperatures & headroom state"
        AppTab.SENSORS -> "Hardware sensor inventory & power consumption"
        AppTab.APPS -> "Installed user and system application directory"
        AppTab.TESTS -> "Automated & interactive hardware test suite"
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DevPulseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TabContentScreen(
                tab = AppTab.DASHBOARD,
                uiState = MainUiState(
                    deviceInfo = DeviceInfo(deviceName = "Pixel 8 Pro", model = "Pixel 8 Pro"),
                    dashboardInfo = DashboardInfo(
                        ramTotalBytes = 12L * 1024 * 1024 * 1024,
                        ramUsedBytes = 5L * 1024 * 1024 * 1024,
                        ramHistory = listOf(30f, 45f, 40f, 55f, 50f, 65f, 60f),
                        storageTotalBytes = 256L * 1024 * 1024 * 1024,
                        storageUsedBytes = 100L * 1024 * 1024 * 1024,
                        batteryLevel = 85,
                        batteryChargingStatus = "Discharging"
                    )
                )
            )
        }
    }
}
