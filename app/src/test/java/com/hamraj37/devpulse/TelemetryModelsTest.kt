package com.hamraj37.devpulse

import com.hamraj37.devpulse.data.model.AppTab
import com.hamraj37.devpulse.data.model.BatteryInfo
import com.hamraj37.devpulse.data.model.CameraSpec
import com.hamraj37.devpulse.data.model.CpuInfo
import com.hamraj37.devpulse.data.model.DeviceInfo
import com.hamraj37.devpulse.data.model.DisplayInfo
import com.hamraj37.devpulse.data.model.TestItem
import com.hamraj37.devpulse.data.model.TestStatus
import com.hamraj37.devpulse.data.telemetry.AppAndTestTelemetry
import com.hamraj37.devpulse.data.telemetry.CpuTelemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryModelsTest {

    @Test
    fun appTabsCount_isFourteen() {
        assertEquals(14, AppTab.entriesOrdered.size)
    }

    @Test
    fun cpuTelemetry_returnsValidCpuInfo() {
        val cpuInfo = CpuTelemetry.getCpuInfo()
        assertNotNull(cpuInfo)
        assertEquals(Runtime.getRuntime().availableProcessors(), cpuInfo.totalCores)
        assertNotNull(cpuInfo.supportedAbis)
    }

    @Test
    fun dataModels_instantiateWithDefaults() {
        val device = DeviceInfo()
        val cpu = CpuInfo()
        val battery = BatteryInfo()
        val display = DisplayInfo()
        val network = com.hamraj37.devpulse.data.model.NetworkInfo()

        assertNotNull(device)
        assertNotNull(cpu)
        assertNotNull(battery)
        assertNotNull(display)
        assertNotNull(network)
    }

    @Test
    fun batteryInfo_containsCapacityFields() {
        val battery = BatteryInfo()
        assertTrue(battery.capacitySystemMah > 0)
        assertTrue(battery.capacityEstimatedMah > 0)
        assertTrue(battery.capacityChargedMah > 0)
    }

    @Test
    fun displayInfo_containsAllRequiredFields() {
        val display = DisplayInfo()
        assertEquals("2414 x 1080 Pixels (FHD+)", display.resolution)
        assertEquals("493 dpi (XXHDPI)", display.density)
        assertEquals("1.0", display.fontScale)
        assertEquals("5.9 inches", display.physicalSize)
        assertEquals("120.0 Hz", display.refreshRate)
        assertEquals(listOf("60.0 Hz", "90.0 Hz", "120.0 Hz"), display.supportedRefreshRates)
        assertEquals("Supported", display.hdrSupported)
        assertEquals("HDR10, HLG, HDR10+", display.hdrCapabilities)
        assertEquals("Supported", display.wideColorGamut)
        assertEquals("30%", display.brightnessLevel)
        assertEquals(0.30f, display.brightnessProgress, 0.01f)
        assertEquals("Adaptive", display.brightnessMode)
        assertEquals("30 Seconds", display.screenTimeout)
        assertEquals("Portrait", display.orientation)
        assertEquals("Built-in screen", display.screenName)
    }

    @Test
    fun networkInfo_containsAllRequiredFields() {
        val networkInfo = com.hamraj37.devpulse.data.model.NetworkInfo()
        assertNotNull(networkInfo.ssid)
        assertNotNull(networkInfo.ipAddress)
        assertNotNull(networkInfo.ipv6Address)
        assertNotNull(networkInfo.gateway)
        assertNotNull(networkInfo.subnetMask)
        assertNotNull(networkInfo.dns1)
        assertNotNull(networkInfo.leaseDuration)
        assertNotNull(networkInfo.interfaceName)
        assertNotNull(networkInfo.linkSpeed)
        assertNotNull(networkInfo.channel)
        assertNotNull(networkInfo.frequency)
        assertNotNull(networkInfo.wifiStandard)
        assertNotNull(networkInfo.securityType)
        assertNotNull(networkInfo.publicIp)
        assertNotNull(networkInfo.location)

        assertEquals("WIFI", networkInfo.activeConnectionType)
        assertEquals(false, networkInfo.isCellularDataActive)
        assertEquals("Jio True5G", networkInfo.networkOperatorName)
        assertEquals("5G NR", networkInfo.networkType)
        assertEquals("Home_WiFi_5G", networkInfo.ssid)
        assertEquals("192.168.1.105", networkInfo.ipAddress)
        assertEquals("fe80::1c32:46ff:fe8a:9201", networkInfo.ipv6Address)
        assertEquals("192.168.1.1", networkInfo.gateway)
        assertEquals("255.255.255.0", networkInfo.subnetMask)
        assertEquals("8.8.8.8", networkInfo.dns1)
        assertEquals("2H", networkInfo.leaseDuration)
        assertEquals("wlan0", networkInfo.interfaceName)
        assertEquals("108 Mbps", networkInfo.linkSpeed)
        assertEquals("CH 6", networkInfo.channel)
        assertEquals("2437 MHz", networkInfo.frequency)
        assertEquals("Wi-Fi 802.11n (Wi-Fi 4)", networkInfo.wifiStandard)
        assertEquals("WPA/WPA2", networkInfo.securityType)
        assertEquals("157.32.184.92", networkInfo.publicIp)
        assertEquals("New Delhi, Delhi, India", networkInfo.location)
        assertEquals("0 KB/s", networkInfo.downloadSpeed)
        assertEquals("0 KB/s", networkInfo.uploadSpeed)
        assertEquals(0L, networkInfo.downloadSpeedBytesPerSec)
        assertEquals(0L, networkInfo.uploadSpeedBytesPerSec)
        assertNotNull(networkInfo.speedHistory)
    }

    @Test
    fun initialTestItems_hasSixteenTests() {
        val tests = AppAndTestTelemetry.getInitialTestItems()
        assertEquals(16, tests.size)
        assertTrue(tests.any { it.category == "Automatic" })
        assertEquals(15, tests.count { it.category == "Interactive" })
        assertTrue(tests.any { it.id == "test_gps" })
    }

    @Test
    fun systemInfo_containsLocationAndLocaleDetails() {
        val systemInfo = com.hamraj37.devpulse.data.model.SystemInfo()
        assertTrue(systemInfo.language.contains("English") || systemInfo.language.contains("("))
        assertTrue(systemInfo.timezone.contains("GMT") || systemInfo.timezone.contains("Asia") || systemInfo.timezone.contains("("))
    }

    @Test
    fun cameraSpec_containsAllParameters() {
        val spec = CameraSpec(
            cameraId = "0",
            facing = "Back Camera",
            resolutionMp = "13 MP - Back 4096 x 3072"
        )
        assertNotNull(spec.aberrationModes)
        assertNotNull(spec.antibandingModes)
        assertNotNull(spec.autoExposureModes)
        assertNotNull(spec.compensationStep)
        assertNotNull(spec.autoFocusModes)
        assertNotNull(spec.effects)
        assertNotNull(spec.sceneModes)
        assertNotNull(spec.videoStabilizationModes)
        assertNotNull(spec.whiteBalanceModes)
        assertNotNull(spec.maxAeAfAwbRegions)
        assertNotNull(spec.edgeModes)
        assertNotNull(spec.hotPixelModes)
        assertNotNull(spec.thumbnailSizes)
        assertNotNull(spec.lensPlacement)
        assertNotNull(spec.apertures)
        assertNotNull(spec.focalLengths)
        assertNotNull(spec.cameraCapabilities)
        assertNotNull(spec.dynamicRangeProfiles)
        assertNotNull(spec.maxOutputStreams)
        assertNotNull(spec.supportedPhotoResolutions)
        assertNotNull(spec.testPatternModes)
        assertNotNull(spec.colorFilterArrangement)
        assertNotNull(spec.sensorSize)
        assertNotNull(spec.pixelArraySize)
        assertNotNull(spec.timestampSource)
        assertNotNull(spec.orientation)
        assertNotNull(spec.faceDetectionModes)
    }

    @Test
    fun testStatus_canBeUpdated() {
        val test = TestItem("test_1", "Sample", "Interactive", "Desc")
        assertEquals(TestStatus.NOT_TESTED, test.status)
        val updated = test.copy(status = TestStatus.PASSED)
        assertEquals(TestStatus.PASSED, updated.status)
    }

    @Test
    fun requiredPermissions_containsExpectedBasePermissions() {
        val permissions = com.hamraj37.devpulse.ui.getRequiredPermissions()
        assertTrue(permissions.contains(android.Manifest.permission.CAMERA))
        assertTrue(permissions.contains(android.Manifest.permission.RECORD_AUDIO))
        assertTrue(permissions.contains(android.Manifest.permission.READ_PHONE_STATE))
        assertTrue(permissions.contains(android.Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(permissions.contains(android.Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @Test
    fun thermalHeadroom_isNotNaN() {
        val thermalInfo = com.hamraj37.devpulse.data.model.ThermalInfo()
        org.junit.Assert.assertFalse(thermalInfo.thermalHeadroom.isNaN())
        org.junit.Assert.assertFalse(thermalInfo.thermalHeadroom.isInfinite())
    }

    @Test
    fun appCategoryFilter_defaultsToUserCategory() {
        val uiState = com.hamraj37.devpulse.ui.MainUiState()
        assertEquals("User", uiState.appCategoryFilter)
        assertEquals(com.hamraj37.devpulse.data.telemetry.AppCategoryFilter.USER.displayName, uiState.appCategoryFilter)
    }

    @Test
    fun appCategoryFilterEnum_hasExpectedValues() {
        assertEquals("User", com.hamraj37.devpulse.data.telemetry.AppCategoryFilter.USER.displayName)
        assertEquals("All", com.hamraj37.devpulse.data.telemetry.AppCategoryFilter.ALL.displayName)
        assertEquals("System", com.hamraj37.devpulse.data.telemetry.AppCategoryFilter.SYSTEM.displayName)
        assertEquals("Analyze", com.hamraj37.devpulse.data.telemetry.AppCategoryFilter.ANALYZE.displayName)
        assertEquals(com.hamraj37.devpulse.data.telemetry.AppCategoryFilter.USER, com.hamraj37.devpulse.data.telemetry.AppCategoryFilter.DEFAULT)
    }
}
