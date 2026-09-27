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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.hamraj37.devpulse.data.model.AppTab
import com.hamraj37.devpulse.ui.components.DevPulseTabRow
import com.hamraj37.devpulse.ui.screens.AppsScreen
import com.hamraj37.devpulse.ui.screens.BatteryScreen
import com.hamraj37.devpulse.ui.screens.CameraScreen
import com.hamraj37.devpulse.ui.screens.ConnectivityScreen
import com.hamraj37.devpulse.ui.screens.CpuScreen
import com.hamraj37.devpulse.ui.screens.DashboardScreen
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.refreshTelemetry()
    }

    LaunchedEffect(Unit) {
        val ungranted = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (ungranted.isNotEmpty()) {
            permissionLauncher.launch(ungranted.toTypedArray())
        } else {
            viewModel.refreshTelemetry()
        }
    }

    val tabs = AppTab.entriesOrdered
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )
    var scrollJob by remember { mutableStateOf<Job?>(null) }

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

    BackHandler(enabled = uiState.selectedTab != AppTab.DASHBOARD) {
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
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
                                    text = uiState.deviceInfo.model.ifEmpty { "Pixel 8 Pro" },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refreshTelemetry() }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh Telemetry"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
            androidx.compose.runtime.key(currentTab) {
                TabContentScreen(
                    tab = currentTab,
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun TabContentScreen(
    tab: AppTab,
    uiState: MainUiState,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (tab) {
            AppTab.DASHBOARD -> DashboardScreen(
                dashboardInfo = uiState.dashboardInfo,
                testsList = uiState.testsList,
                onNavigateToTests = { viewModel.selectTab(AppTab.TESTS) }
            )
            AppTab.DEVICE -> DeviceScreen(uiState.deviceInfo)
            AppTab.SYSTEM -> SystemScreen(uiState.systemInfo)
            AppTab.CPU -> CpuScreen(uiState.cpuInfo)
            AppTab.BATTERY -> BatteryScreen(uiState.batteryInfo)
            AppTab.MEMORY -> MemoryScreen(uiState.memoryInfo)
            AppTab.NETWORK -> NetworkScreen(uiState.networkInfo)
            AppTab.CONNECTIVITY -> ConnectivityScreen(uiState.connectivityInfo)
            AppTab.DISPLAY -> DisplayScreen(uiState.displayInfo)
            AppTab.THERMAL -> ThermalScreen(uiState.thermalInfo)
            AppTab.SENSORS -> SensorsScreen(uiState.sensorInfo)
            AppTab.CAMERA -> CameraScreen(
                cameraInfo = uiState.cameraInfo,
                selectedCameraId = uiState.selectedCameraId,
                onSelectCamera = { viewModel.selectCamera(it) }
            )
            AppTab.APPS -> AppsScreen(
                appInfo = uiState.appInfo,
                searchQuery = uiState.appSearchQuery,
                categoryFilter = uiState.appCategoryFilter,
                onSearchQueryChange = { viewModel.setAppSearchQuery(it) },
                onCategoryFilterChange = { viewModel.setAppCategoryFilter(it) }
            )
            AppTab.TESTS -> TestsScreen(
                testsList = uiState.testsList,
                onUpdateTestStatus = { testId, status -> viewModel.updateTestStatus(testId, status) }
            )
        }
    }
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

@Preview
@Composable
fun MainScreenPreview() {
    DevPulseTheme {
        Surface {
            Text(text = "MainScreen Preview")
        }
    }
}
