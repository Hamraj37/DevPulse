package com.hamraj37.devpulse.ui

import android.app.Application
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val isLoading: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TelemetryRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadStaticTelemetry()
        observeDynamicTelemetry()
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setToolsPageOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isToolsPageOpen = isOpen) }
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
        viewModelScope.launch {
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
                    selectedCameraId = defaultCameraId
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
}
