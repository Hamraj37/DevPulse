package com.hamraj37.devpulse.data.model

// Common Key-Value item for UI presentation
data class TelemetryItem(
    val label: String,
    val value: String,
    val description: String? = null
)

// 1. Dashboard Info
data class DashboardInfo(
    val ramTotalBytes: Long = 0,
    val ramUsedBytes: Long = 0,
    val ramFreeBytes: Long = 0,
    val ramHistory: List<Float> = emptyList(), // History of RAM usage percentage (0..100)
    val cpuCoreFrequencies: List<Long> = emptyList(), // Current MHz per core
    val cpuCoreMaxFrequencies: List<Long> = emptyList(), // Max MHz per core
    val testsCompleted: Int = 0,
    val testsTotal: Int = 16,
    val displaySummary: String = "2414 x 1080 | 120 Hz",
    val displaySizeInches: Double = 6.7,
    val storageTotalBytes: Long = 0,
    val storageUsedBytes: Long = 0,
    val storageFreeBytes: Long = 0,
    val batteryChargingStatus: String = "Discharging",
    val batteryLevel: Int = 85,
    val batteryVoltage: Float = 4.15f,
    val batteryTemp: Float = 32.0f,
    val sensorCount: Int = 42,
    val appCount: Int = 128
)

// 2. Device Info
data class DeviceInfo(
    val deviceName: String = "",
    val model: String = "",
    val manufacturer: String = "",
    val deviceCode: String = "",
    val board: String = "",
    val hardware: String = "",
    val brand: String = "",
    val androidDeviceId: String = "",
    val buildFingerprint: String = "",
    val deviceType: String = "Smartphone", // Phone, Tablet, Foldable, Emulator
    val esimSupported: Boolean = true,
    val networkType: String = "5G NR / LTE Advanced",
    val networkOperator1: String = "Jio 5G",
    val networkOperator2: String = "Airtel"
)

// 3. System Info
data class SystemInfo(
    val androidVersion: String = "16.0",
    val codeName: String = "Baklava",
    val versionLetter: String = "W",
    val releaseDate: String = "June 11, 2025",
    val apiLevel: Int = 36,
    val securityPatch: String = "September 5, 2026",
    val bootloader: String = "v16.0-bootloader-rel",
    val buildNumber: String = "BP2A.250905.008",
    val basebandVersion: String = "g850-00021-231120-B-11119567",
    val javaVm: String = "ART 2.1.0",
    val kernelVersion: String = "6.1.75-android16-11",
    val language: String = "English (United States, en_US)",
    val timezone: String = "Asia/Kolkata (IST / GMT+05:30)",
    val openGlEsVersion: String = "OpenGL ES 3.2 (ANGLE 2.1.22238)",
    val rootManagementApps: String = "Not Rooted",
    val seLinux: String = "Enforcing",
    val googlePlayServices: String = "24.32.14 (190400-662541280)",
    val systemUptime: String = "18h 42m 15s",
    val vulkanVersion: String = "Vulkan 1.3.269",
    val trebleSupported: String = "Yes",
    val seamlessUpdates: String = "Yes (A/B)",
    val dynamicPartitions: String = "Yes",
    val drmInfo: DrmDetails = DrmDetails()
)

data class DrmDetails(
    val vendor: String = "com.google.android.widevine",
    val version: String = "18.0.0",
    val description: String = "Widevine Modular DRM",
    val algorithms: String = "AES/CBC/NoPadding, RSA/OAEP",
    val securityLevel: String = "L1",
    val maxHdcpLevel: String = "2.3"
)

// 4. CPU Info
data class CpuInfo(
    val processorName: String = "Qualcomm Snapdragon 8 Gen 2",
    val architecture: String = "arm64-v8a",
    val supportedAbis: List<String> = listOf("arm64-v8a", "armeabi-v7a", "armeabi"),
    val hardwareName: String = "qcom kalama",
    val cpuType: String = "Octa-Core (8 Cores)",
    val governor: String = "schedutil",
    val totalCores: Int = 8,
    val minFrequencyMhz: Long = 800,
    val maxFrequencyMhz: Long = 3200,
    val coreFrequencies: List<CpuCoreSpeed> = emptyList(),
    val gpuRenderer: String = "Adreno (TM) 740",
    val gpuVendor: String = "Qualcomm",
    val gpuVersion: String = "OpenGL ES 3.2 V@0615.0"
)

data class CpuCoreSpeed(
    val coreIndex: Int,
    val currentMhz: Long,
    val minMhz: Long,
    val maxMhz: Long
)

// 5. Battery Info
data class BatteryInfo(
    val currentMa: Float = 450f,
    val powerWatts: Float = 1.85f,
    val temperatureCelsius: Float = 32.5f,
    val usbStatus: String = "Discharging",
    val health: String = "Good",
    val levelPercent: Int = 85,
    val status: String = "Discharging",
    val powerSource: String = "Battery",
    val technology: String = "Li-ion",
    val voltageVolts: Float = 4.15f,
    val timeToChargeFormatted: String = "14 hrs 20 mins remaining",
    val chargeCycles: Int = 142,
    val capacityChargedMah: Int = 4250,
    val capacityEstimatedMah: Int = 5000,
    val capacitySystemMah: Int = 5000,
    val powerHistory: List<Float> = emptyList() // Voltage/Power history for live chart
)

// 6. Network Info
data class NetworkInfo(
    val activeConnectionType: String = "WIFI", // "WIFI", "CELLULAR", "DISCONNECTED"
    val isCellularDataActive: Boolean = false,
    val networkOperatorName: String = "Jio True5G",
    val networkType: String = "5G NR",
    val ssid: String = "Home_WiFi_5G",
    val bssid: String = "02:00:00:00:00:00",
    val ipAddress: String = "192.168.1.105",
    val ipv6Address: String = "fe80::1c32:46ff:fe8a:9201",
    val gateway: String = "192.168.1.1",
    val subnetMask: String = "255.255.255.0",
    val dns1: String = "8.8.8.8",
    val leaseDuration: String = "2H",
    val interfaceName: String = "wlan0",
    val linkSpeed: String = "108 Mbps",
    val channel: String = "CH 6",
    val frequency: String = "2437 MHz",
    val wifiStandard: String = "Wi-Fi 802.11n (Wi-Fi 4)",
    val securityType: String = "WPA/WPA2",
    val publicIp: String = "157.32.184.92",
    val location: String = "New Delhi, Delhi, India",
    val wifiBadge: String = "Wi-Fi 4",
    val isConnected: Boolean = true,
    val dataState: String = "Connected",
    val roamingState: String = "Not Roaming",
    val mccMnc: String = "405 / 854",
    val countryMcc: String = "India (405)",
    val mobileSignal: String = "Strong (-85 dBm)"
)

// 7. Connectivity Info
data class ConnectivityInfo(
    val wifiStandard: String = "Wi-Fi 6E (802.11ax)",
    val wifiDirectSupported: Boolean = true,
    val wifi5GhzSupported: Boolean = true,
    val wifi6GhzSupported: Boolean = true,
    val bluetoothSupported: Boolean = true,
    val bluetoothVersion: String = "Bluetooth 5.3",
    val multipleAdvertisementsSupported: Boolean = true,
    val offloadedFilteringSupported: Boolean = true,
    val offloadedScanBatchingSupported: Boolean = true,
    val bluetoothLeSupported: Boolean = true,
    val le2mPhySupported: Boolean = true,
    val leCodedPhySupported: Boolean = true,
    val leExtendedAdvertisingSupported: Boolean = true,
    val lePeriodicAdvertisingSupported: Boolean = true,
    val leAudioSupported: Boolean = true,
    val bluetoothFeatures: List<String> = listOf("Bluetooth 5.3", "LE Audio", "A2DP", "HID"),
    val nfcSupported: Boolean = true,
    val nfcEnabled: Boolean = true,
    val nfcStatus: String = "Supported & Enabled",
    val secureNfcSupported: Boolean = true,
    val uwbSupported: Boolean = true,
    val uwbStatus: String = "Supported",
    val usbHostSupported: Boolean = true,
    val usbAccessorySupported: Boolean = true,
    val usbDebuggingEnabled: Boolean = true,
    val usbStatus: String = "USB 3.2 Gen 2 (Type-C / OTG)"
)

// 8. Display Info
data class DisplayInfo(
    val resolution: String = "2414 x 1080 Pixels (FHD+)",
    val density: String = "493 dpi (XXHDPI)",
    val fontScale: String = "1.0",
    val physicalSize: String = "5.9 inches",
    val refreshRate: String = "120.0 Hz",
    val supportedRefreshRates: List<String> = listOf("60.0 Hz", "90.0 Hz", "120.0 Hz"),
    val hdrSupported: String = "Supported",
    val hdrCapabilities: String = "HDR10, HLG, HDR10+",
    val wideColorGamut: String = "Supported",
    val brightnessLevel: String = "30%",
    val brightnessProgress: Float = 0.30f,
    val brightnessMode: String = "Adaptive",
    val screenTimeout: String = "30 Seconds",
    val orientation: String = "Portrait",
    val screenName: String = "Built-in screen",
    val resolutionWidthPx: Int = 2414,
    val resolutionHeightPx: Int = 1080,
    val resolutionCategory: String = "FHD+",
    val densityDpi: Int = 493,
    val densityBucket: String = "XXHDPI",
    val physicalSizeInches: Double = 5.9,
    val currentRefreshRate: Float = 120.0f,
    val currentBrightnessPercent: Int = 30,
    val screenTimeoutSeconds: Int = 30
)

// 9. Memory Info
data class MemoryInfo(
    val ramTotalBytes: Long = 12L * 1024 * 1024 * 1024,
    val ramUsedBytes: Long = 5L * 1024 * 1024 * 1024,
    val ramAvailableBytes: Long = 7L * 1024 * 1024 * 1024,
    val ramThresholdBytes: Long = 500L * 1024 * 1024,
    val isLowMemory: Boolean = false,
    val swapTotalBytes: Long = 3L * 1024 * 1024 * 1024,
    val swapUsedBytes: Long = 1L * 1024 * 1024 * 1024,
    val zramTotalBytes: Long = 2L * 1024 * 1024 * 1024,
    val zramUsedBytes: Long = 750L * 1024 * 1024,
    val zramOrigBytes: Long = 1L * 1024 * 1024 * 1024,
    val zramComprBytes: Long = 350L * 1024 * 1024,
    val systemStorageTotalBytes: Long = 32L * 1024 * 1024 * 1024,
    val systemStorageUsedBytes: Long = 18L * 1024 * 1024 * 1024,
    val systemStorageFreeBytes: Long = 14L * 1024 * 1024 * 1024,
    val internalStorageTotalBytes: Long = 256L * 1024 * 1024 * 1024,
    val internalStorageUsedBytes: Long = 112L * 1024 * 1024 * 1024,
    val internalStorageFreeBytes: Long = 144L * 1024 * 1024 * 1024
)

// 10. Camera Info
data class CameraInfo(
    val cameras: List<CameraSpec> = emptyList()
)

data class CameraSpec(
    val cameraId: String,
    val facing: String, // Front / Back / External
    val resolutionMp: String,
    val pixelSize: String = "1.22 µm",
    val focalLengths: List<Float> = listOf(4.25f),
    val apertures: List<Float> = listOf(1.8f),
    val supportedPhotoResolutions: List<String> = listOf("4096x3072 (13MP)", "3840x2160 (8MP)", "1920x1080 (2MP)"),
    val supportedVideoResolutions: List<String> = listOf("4K UHD (3840x2160 @ 60fps)", "1080p FHD (1920x1080 @ 120fps)"),
    val autoFocusModes: List<String> = listOf("Auto Focus", "Continuous Picture", "Continuous Video"),
    val sceneModes: List<String> = listOf("Auto", "Night", "HDR", "Portrait"),
    val whiteBalanceModes: List<String> = listOf("Auto", "Daylight", "Cloudy", "Incandescent", "Fluorescent"),
    val opticalStabilizationSupported: Boolean = true,
    val aberrationCorrectionSupported: Boolean = true,
    val hardwareLevel: String = "FULL",

    // Additional 18 fields making 32 specs total
    val aberrationModes: List<String> = listOf("OFF", "FAST", "HIGH_QUALITY"),
    val antibandingModes: List<String> = listOf("OFF", "50HZ", "60HZ", "AUTO"),
    val autoExposureModes: List<String> = listOf("OFF", "ON", "ON_AUTO_FLASH", "ON_ALWAYS_FLASH"),
    val compensationStep: String = "1/3 EV (-3.0 to +3.0 EV)",
    val effects: List<String> = listOf("OFF", "MONO", "NEGATIVE", "SEPIA"),
    val videoStabilizationModes: List<String> = listOf("OFF", "ON", "PREVIEW_STABILIZATION"),
    val maxAeAfAwbRegions: String = "AE: 3, AF: 1, AWB: 1",
    val edgeModes: List<String> = listOf("OFF", "FAST", "HIGH_QUALITY"),
    val flashAvailable: Boolean = true,
    val hotPixelModes: List<String> = listOf("OFF", "FAST", "HIGH_QUALITY"),
    val thumbnailSizes: List<String> = listOf("0x0", "176x144", "240x144", "320x240"),
    val lensPlacement: String = "Back Facing",
    val filterDensities: List<Float> = listOf(0.0f),
    val focusDistanceCalibration: String = "CALIBRATED",
    val cameraCapabilities: List<String> = listOf("BACKWARD_COMPATIBLE", "MANUAL_SENSOR", "MANUAL_POST_PROCESSING", "RAW", "READ_SENSOR_SETTINGS"),
    val dynamicRangeProfiles: List<String> = listOf("STANDARD (8-bit)", "HLG10 (10-bit)", "HDR10"),
    val maxOutputStreams: String = "Raw: 1, Processed: 3, Stalling: 1",
    val testPatternModes: List<String> = listOf("OFF", "SOLID_COLOR", "COLOR_BARS"),
    val colorFilterArrangement: String = "RGGB (Bayer)",
    val sensorSize: String = "6.40 x 4.80 mm (1/2.55\")",
    val pixelArraySize: String = "4096 x 3072",
    val timestampSource: String = "UNKNOWN",
    val orientation: String = "90°",
    val faceDetectionModes: List<String> = listOf("OFF", "SIMPLE", "FULL")
)

// 11. Thermal Info
data class ThermalInfo(
    val overallStatus: String = "Normal",
    val thermalHeadroom: Float = 1.0f,
    val thermalZones: List<ThermalZone> = emptyList()
)

data class ThermalZone(
    val name: String,
    val tempCelsius: Float,
    val type: String
)

// 12. Sensors Info
data class SensorInfo(
    val sensorCount: Int = 42,
    val sensors: List<SensorSpec> = emptyList()
)

data class SensorSpec(
    val name: String,
    val vendor: String,
    val type: Int = 0,
    val typeName: String,
    val isWakeUpSensor: Boolean = false,
    val version: Int = 1,
    val powerMa: Float = 0.2f,
    val maxRange: Float = 100f,
    val resolution: Float = 0.01f,
    val minDelayUs: Int = 10000
)

// 13. Apps Info
data class AppInfo(
    val totalApps: Int = 128,
    val userAppsCount: Int = 48,
    val systemAppsCount: Int = 80,
    val appsList: List<AppSpec> = emptyList()
)

data class AppSpec(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val appSizeBytes: Long,
    val isSystemApp: Boolean,
    val installedTimeMs: Long,
    val updatedTimeMs: Long
)

// 14. Tests Info
data class TestItem(
    val id: String,
    val title: String,
    val category: String, // "Automatic" or "Interactive"
    val description: String,
    val status: TestStatus = TestStatus.NOT_TESTED
)

enum class TestStatus {
    NOT_TESTED,
    RUNNING,
    PASSED,
    FAILED
}
