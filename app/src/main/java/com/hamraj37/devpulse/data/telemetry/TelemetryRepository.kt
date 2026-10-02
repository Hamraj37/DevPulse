package com.hamraj37.devpulse.data.telemetry

import android.content.Context
import com.hamraj37.devpulse.data.model.AppInfo
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
import java.util.Collections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class TelemetryRepository(private val context: Context) {

    private val ramHistoryList = Collections.synchronizedList(mutableListOf<Float>())
    private val prefs = context.getSharedPreferences("devpulse_test_prefs", Context.MODE_PRIVATE)

    private val _testsState = MutableStateFlow<List<TestItem>>(
        AppAndTestTelemetry.getInitialTestItems().map { item ->
            val savedName = prefs.getString("test_status_${item.id}", null)
            val savedStatus = try {
                if (savedName != null) TestStatus.valueOf(savedName) else item.status
            } catch (_: Throwable) {
                item.status
            }
            item.copy(status = savedStatus)
        }
    )
    val testsFlow: StateFlow<List<TestItem>> = _testsState.asStateFlow()

    fun updateTestStatus(testId: String, status: TestStatus) {
        prefs.edit().putString("test_status_$testId", status.name).apply()
        _testsState.update { currentList ->
            currentList.map { item ->
                if (item.id == testId) item.copy(status = status) else item
            }
        }
    }

    // 1. Dashboard Flow (updates every 1000ms)
    fun getDashboardFlow(): Flow<DashboardInfo> = flow {
        while (true) {
            try {
                val memory = MemoryTelemetry.getMemoryInfo(context)
                val cpuFreqs = CpuTelemetry.getCurrentCoreFrequencies()
                val battery = BatteryTelemetry.getBatteryInfo(context)
                val display = DisplayTelemetry.getDisplayInfo(context)
                val sensors = ThermalAndSensorTelemetry.getSensorInfo(context)
                val apps = AppAndTestTelemetry.getAppInfo(context)

                val ramUsedPercentRaw = if (memory.ramTotalBytes > 0) {
                    (memory.ramUsedBytes.toFloat() / memory.ramTotalBytes.toFloat()) * 100f
                } else 45f
                val ramUsedPercent = if (ramUsedPercentRaw.isNaN() || ramUsedPercentRaw.isInfinite()) 45f else ramUsedPercentRaw

                val snapshot = synchronized(ramHistoryList) {
                    ramHistoryList.add(ramUsedPercent)
                    if (ramHistoryList.size > 20) {
                        ramHistoryList.removeAt(0)
                    }
                    ramHistoryList.toList()
                }

                val currentTests = _testsState.value
                val completedCount = currentTests.count { it.status == TestStatus.PASSED || it.status == TestStatus.FAILED }

                val dashboardInfo = DashboardInfo(
                    ramTotalBytes = memory.ramTotalBytes,
                    ramUsedBytes = memory.ramUsedBytes,
                    ramFreeBytes = memory.ramAvailableBytes,
                    ramHistory = snapshot,
                    cpuCoreFrequencies = cpuFreqs,
                    cpuCoreMaxFrequencies = cpuFreqs.map { 2840L },
                    testsCompleted = completedCount,
                    testsTotal = currentTests.size.coerceAtLeast(16),
                    displaySummary = "${display.resolutionWidthPx} x ${display.resolutionHeightPx}",
                    displaySizeInches = display.physicalSizeInches.takeIf { it > 0 } ?: 6.7,
                    storageTotalBytes = memory.internalStorageTotalBytes,
                    storageUsedBytes = memory.internalStorageUsedBytes,
                    storageFreeBytes = memory.internalStorageFreeBytes,
                    batteryChargingStatus = battery.status,
                    batteryLevel = battery.levelPercent,
                    batteryHealthPercent = battery.healthPercent,
                    batteryVoltage = battery.voltageVolts,
                    batteryTemp = battery.temperatureCelsius,
                    sensorCount = sensors.sensorCount,
                    appCount = apps.totalApps.coerceAtLeast(128)
                )

                emit(dashboardInfo)
            } catch (_: Throwable) {
                emit(DashboardInfo())
            }
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    // 2. Device and System Flow (updates every 3000ms)
    fun getDeviceAndSystemFlow(): Flow<Pair<DeviceInfo, SystemInfo>> = flow {
        while (true) {
            try {
                val device = DeviceAndSystemTelemetry.getDeviceInfo(context)
                val system = DeviceAndSystemTelemetry.getSystemInfo(context)
                emit(Pair(device, system))
            } catch (_: Throwable) {
                emit(Pair(DeviceInfo(), SystemInfo()))
            }
            delay(3000)
        }
    }.flowOn(Dispatchers.IO)

    // 3. CPU Info Flow (updates every 1000ms with live per-core MHz frequencies & GPU status)
    fun getCpuInfoFlow(): Flow<CpuInfo> = flow {
        while (true) {
            try {
                emit(CpuTelemetry.getCpuInfo())
            } catch (_: Throwable) {
                emit(CpuInfo())
            }
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    // 4. Battery Info Flow (updates every 1500ms with live current mA, voltage mV, temperature °C, power W, and sparkline history)
    fun getBatteryInfoFlow(): Flow<BatteryInfo> = flow {
        while (true) {
            try {
                emit(BatteryTelemetry.getBatteryInfo(context))
            } catch (_: Throwable) {
                emit(BatteryInfo())
            }
            delay(1500)
        }
    }.flowOn(Dispatchers.IO)

    // 5. Network Info Flow (updates every 1000ms with live IP, real-time speed, link speed, signal strength)
    fun getNetworkInfoFlow(): Flow<NetworkInfo> = flow {
        while (true) {
            try {
                emit(NetworkAndConnectivityTelemetry.getNetworkInfo(context))
            } catch (_: Throwable) {
                emit(NetworkInfo())
            }
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    // 6. Display Info Flow (updates every 2000ms with live brightness %, orientation, refresh rate)
    fun getDisplayInfoFlow(): Flow<DisplayInfo> = flow {
        while (true) {
            try {
                emit(DisplayTelemetry.getDisplayInfo(context))
            } catch (_: Throwable) {
                emit(DisplayInfo())
            }
            delay(2000)
        }
    }.flowOn(Dispatchers.IO)

    // 7. Memory Info Flow (updates every 1500ms with live RAM used/free %, Internal storage used/free %, System storage used/free %)
    fun getMemoryInfoFlow(): Flow<MemoryInfo> = flow {
        while (true) {
            try {
                emit(MemoryTelemetry.getMemoryInfo(context))
            } catch (_: Throwable) {
                emit(MemoryInfo())
            }
            delay(1500)
        }
    }.flowOn(Dispatchers.IO)

    // 8. Camera Info Flow (updates every 3000ms)
    fun getCameraInfoFlow(): Flow<CameraInfo> = flow {
        while (true) {
            try {
                emit(CameraTelemetry.getCameraInfo(context))
            } catch (_: Throwable) {
                emit(CameraInfo())
            }
            delay(3000)
        }
    }.flowOn(Dispatchers.IO)

    // 9. Thermal Flow (updates every 1500ms with live temperature in °C across all thermal zones)
    fun getThermalFlow(): Flow<ThermalInfo> = flow {
        while (true) {
            try {
                emit(ThermalAndSensorTelemetry.getThermalInfo(context))
            } catch (_: Throwable) {
                emit(ThermalInfo())
            }
            delay(1500)
        }
    }.flowOn(Dispatchers.IO)

    // 10. Sensor List Flow (updates sensor count and active sensor streaming every 3000ms)
    fun getSensorListFlow(): Flow<SensorInfo> = flow {
        while (true) {
            try {
                emit(ThermalAndSensorTelemetry.getSensorInfo(context))
            } catch (_: Throwable) {
                emit(SensorInfo())
            }
            delay(3000)
        }
    }.flowOn(Dispatchers.IO)

    // 11. App List Flow (updates app counts and storage size every 3000ms)
    fun getAppListFlow(): Flow<AppInfo> = flow {
        while (true) {
            try {
                emit(AppAndTestTelemetry.getAppInfo(context))
            } catch (_: Throwable) {
                emit(AppInfo())
            }
            delay(3000)
        }
    }.flowOn(Dispatchers.IO)

    // 12. Diagnostic Tests Flow (updates test pass/fail completion state)
    fun getDiagnosticTestsFlow(): Flow<List<TestItem>> = _testsState.asStateFlow()

    // 13. Connectivity Info Flow (updates connectivity info every 3000ms)
    fun getConnectivityInfoFlow(): Flow<ConnectivityInfo> = flow {
        while (true) {
            try {
                emit(NetworkAndConnectivityTelemetry.getConnectivityInfo(context))
            } catch (_: Throwable) {
                emit(ConnectivityInfo())
            }
            delay(3000)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        try { DeviceAndSystemTelemetry.getDeviceInfo(context) } catch (_: Throwable) { DeviceInfo() }
    }

    suspend fun getSystemInfo(): SystemInfo = withContext(Dispatchers.IO) {
        try { DeviceAndSystemTelemetry.getSystemInfo(context) } catch (_: Throwable) { SystemInfo() }
    }

    suspend fun getNetworkInfo(): NetworkInfo = withContext(Dispatchers.IO) {
        try { NetworkAndConnectivityTelemetry.getNetworkInfo(context) } catch (_: Throwable) { NetworkInfo() }
    }

    suspend fun getConnectivityInfo(): ConnectivityInfo = withContext(Dispatchers.IO) {
        try { NetworkAndConnectivityTelemetry.getConnectivityInfo(context) } catch (_: Throwable) { ConnectivityInfo() }
    }

    suspend fun getDisplayInfo(): DisplayInfo = withContext(Dispatchers.IO) {
        try { DisplayTelemetry.getDisplayInfo(context) } catch (_: Throwable) { DisplayInfo() }
    }

    suspend fun getCameraInfo(): CameraInfo = withContext(Dispatchers.IO) {
        try { CameraTelemetry.getCameraInfo(context) } catch (_: Throwable) { CameraInfo() }
    }

    suspend fun getSensorInfo(): SensorInfo = withContext(Dispatchers.IO) {
        try { ThermalAndSensorTelemetry.getSensorInfo(context) } catch (_: Throwable) { SensorInfo() }
    }

    suspend fun getAppInfo(): AppInfo = withContext(Dispatchers.IO) {
        try { AppAndTestTelemetry.getAppInfo(context) } catch (_: Throwable) { AppInfo() }
    }

    fun getTests(): List<TestItem> {
        return _testsState.value
    }
}
