package com.hamraj37.devpulse.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamraj37.devpulse.data.model.AppInfo
import com.hamraj37.devpulse.data.model.AppTab
import com.hamraj37.devpulse.data.model.BatteryInfo
import com.hamraj37.devpulse.data.model.CameraInfo
import com.hamraj37.devpulse.data.model.ConnectivityInfo
import com.hamraj37.devpulse.data.model.CpuInfo
import com.hamraj37.devpulse.data.model.DashboardInfo
import com.hamraj37.devpulse.data.model.DeviceInfo
import com.hamraj37.devpulse.data.model.DisplayInfo
import com.hamraj37.devpulse.data.model.MemoryInfo
import com.hamraj37.devpulse.data.model.NetworkInfo
import com.hamraj37.devpulse.data.model.SensorInfo
import com.hamraj37.devpulse.data.model.SystemInfo
import com.hamraj37.devpulse.data.model.TestItem
import com.hamraj37.devpulse.data.model.TestStatus
import com.hamraj37.devpulse.data.model.ThermalInfo
import com.hamraj37.devpulse.data.telemetry.AppCategoryFilter
import com.hamraj37.devpulse.data.telemetry.TelemetryRepository
import com.hamraj37.devpulse.ui.screens.ToolType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class GithubReleaseInfo(
    val tagName: String,
    val name: String,
    val body: String,
    val htmlUrl: String
)

data class MainUiState(
    val selectedTab: AppTab = AppTab.DASHBOARD,
    val dashboardInfo: DashboardInfo = DashboardInfo(),
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val systemInfo: SystemInfo = SystemInfo(),
    val cpuInfo: CpuInfo = CpuInfo(),
    val batteryInfo: BatteryInfo = BatteryInfo(),
    val networkInfo: NetworkInfo = NetworkInfo(),
    val connectivityInfo: ConnectivityInfo = ConnectivityInfo(),
    val displayInfo: DisplayInfo = DisplayInfo(),
    val memoryInfo: MemoryInfo = MemoryInfo(),
    val cameraInfo: CameraInfo = CameraInfo(),
    val thermalInfo: ThermalInfo = ThermalInfo(),
    val sensorInfo: SensorInfo = SensorInfo(),
    val appInfo: AppInfo = AppInfo(),
    val testsList: List<TestItem> = emptyList(),
    val selectedCameraId: String = "0",
    val appSearchQuery: String = "",
    val appCategoryFilter: String = AppCategoryFilter.USER.displayName, // "User" default
    val isToolsPageOpen: Boolean = false,
    val initialTool: ToolType? = null,
    val isLoading: Boolean = false,
    val themeMode: String = "System default", // "System default", "Light", "Dark"
    val updateInfo: GithubReleaseInfo? = null,
    val showUpdateDialog: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TelemetryRepository(application)
    private val prefs = application.getSharedPreferences("devpulse_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        val savedTheme = prefs.getString("theme_mode", "System default") ?: "System default"
        _uiState.update {
            it.copy(
                themeMode = savedTheme
            )
        }
        loadStaticTelemetry()
        observeDynamicTelemetry()
        checkForUpdate()
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = false) }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            try {
                val release = checkForGitHubUpdate()
                if (release != null) {
                    val currentVersion = try {
                        val pInfo = getApplication<Application>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
                        pInfo.versionName?.trim() ?: "1.0.0"
                    } catch (_: Throwable) {
                        "1.0.0"
                    }
                    val cleanTag = release.tagName.removePrefix("v").trim()
                    if (cleanTag.isNotEmpty() && cleanTag != currentVersion) {
                        _uiState.update { it.copy(updateInfo = release, showUpdateDialog = true) }
                    }
                }
            } catch (_: Throwable) {
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setToolsPageOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isToolsPageOpen = isOpen, initialTool = if (!isOpen) null else it.initialTool) }
    }

    fun openTool(toolType: ToolType? = null) {
        _uiState.update { it.copy(isToolsPageOpen = true, initialTool = toolType) }
    }

    fun selectCamera(cameraId: String) {
        _uiState.update { it.copy(selectedCameraId = cameraId) }
    }

    fun setAppSearchQuery(query: String) {
        _uiState.update { it.copy(appSearchQuery = query) }
    }

    fun setAppCategoryFilter(category: String) {
        _uiState.update { it.copy(appCategoryFilter = category) }
    }

    fun updateTestStatus(testId: String, status: TestStatus) {
        repository.updateTestStatus(testId, status)
    }

    fun refreshTelemetry() {
        loadStaticTelemetry()
    }

    fun loadStaticTelemetry() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val device = repository.getDeviceInfo()
            val system = repository.getSystemInfo()
            val network = repository.getNetworkInfo()
            val connectivity = repository.getConnectivityInfo()
            val display = repository.getDisplayInfo()
            val camera = repository.getCameraInfo()
            val sensors = repository.getSensorInfo()
            val apps = repository.getAppInfo()
            val tests = repository.getTests()

            val defaultCameraId = camera.cameras.firstOrNull()?.cameraId ?: "0"

            _uiState.update {
                it.copy(
                    deviceInfo = device,
                    systemInfo = system,
                    networkInfo = network,
                    connectivityInfo = connectivity,
                    displayInfo = display,
                    cameraInfo = camera,
                    sensorInfo = sensors,
                    appInfo = apps,
                    testsList = tests,
                    selectedCameraId = defaultCameraId,
                    isLoading = false
                )
            }
        }
    }

    private fun observeDynamicTelemetry() {
        viewModelScope.launch {
            repository.getDashboardFlow()
                .catch { emit(DashboardInfo()) }
                .collect { dashboard ->
                    _uiState.update { it.copy(dashboardInfo = dashboard) }
                }
        }

        viewModelScope.launch {
            repository.getDeviceAndSystemFlow()
                .catch { emit(Pair(DeviceInfo(), SystemInfo())) }
                .collect { (device, system) ->
                    _uiState.update { it.copy(deviceInfo = device, systemInfo = system) }
                }
        }

        viewModelScope.launch {
            repository.getCpuInfoFlow()
                .catch { emit(CpuInfo()) }
                .collect { cpu ->
                    _uiState.update { it.copy(cpuInfo = cpu) }
                }
        }

        viewModelScope.launch {
            repository.getBatteryInfoFlow()
                .catch { emit(BatteryInfo()) }
                .collect { battery ->
                    _uiState.update { it.copy(batteryInfo = battery) }
                }
        }

        viewModelScope.launch {
            repository.getNetworkInfoFlow()
                .catch { emit(NetworkInfo()) }
                .collect { network ->
                    _uiState.update { it.copy(networkInfo = network) }
                }
        }

        viewModelScope.launch {
            repository.getConnectivityInfoFlow()
                .catch { emit(ConnectivityInfo()) }
                .collect { connectivity ->
                    _uiState.update { it.copy(connectivityInfo = connectivity) }
                }
        }

        viewModelScope.launch {
            repository.getDisplayInfoFlow()
                .catch { emit(DisplayInfo()) }
                .collect { display ->
                    _uiState.update { it.copy(displayInfo = display) }
                }
        }

        viewModelScope.launch {
            repository.getMemoryInfoFlow()
                .catch { emit(MemoryInfo()) }
                .collect { memory ->
                    _uiState.update { it.copy(memoryInfo = memory) }
                }
        }

        viewModelScope.launch {
            repository.getCameraInfoFlow()
                .catch { emit(CameraInfo()) }
                .collect { camera ->
                    _uiState.update { it.copy(cameraInfo = camera) }
                }
        }

        viewModelScope.launch {
            repository.getThermalFlow()
                .catch { emit(ThermalInfo()) }
                .collect { thermal ->
                    _uiState.update { it.copy(thermalInfo = thermal) }
                }
        }

        viewModelScope.launch {
            repository.getSensorListFlow()
                .catch { emit(SensorInfo()) }
                .collect { sensor ->
                    _uiState.update { it.copy(sensorInfo = sensor) }
                }
        }

        viewModelScope.launch {
            repository.getAppListFlow()
                .catch { emit(AppInfo()) }
                .collect { app ->
                    _uiState.update { it.copy(appInfo = app) }
                }
        }

        viewModelScope.launch {
            repository.getDiagnosticTestsFlow()
                .catch { emit(emptyList()) }
                .collect { tests ->
                    _uiState.update { it.copy(testsList = tests) }
                }
        }
    }

    private suspend fun checkForGitHubUpdate(): GithubReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/Hamraj37/DevPulse/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "DevPulse-App")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val tagName = extractJsonField(response, "tag_name") ?: return@withContext null
                val name = extractJsonField(response, "name") ?: tagName
                val body = extractJsonField(response, "body") ?: "Bug fixes and performance improvements."
                val htmlUrl = extractJsonField(response, "html_url") ?: "https://github.com/Hamraj37/DevPulse/releases"
                return@withContext GithubReleaseInfo(tagName, name, body, htmlUrl)
            }
            null
        } catch (_: Throwable) {
            null
        }
    }

    private fun extractJsonField(json: String, field: String): String? {
        val pattern = "\"$field\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        val match = pattern.find(json)
        return match?.groupValues?.get(1)?.replace("\\n", "\n")
    }
}
