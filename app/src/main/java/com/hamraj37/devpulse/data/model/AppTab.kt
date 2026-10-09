package com.hamraj37.devpulse.data.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import com.hamraj37.devpulse.R

enum class AppTab(
    val title: String,
    @get:StringRes val titleResId: Int,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", R.string.tab_dashboard, Icons.Rounded.Dashboard),
    DEVICE("Device", R.string.tab_device, Icons.Rounded.PhoneAndroid),
    SYSTEM("System", R.string.tab_system, Icons.Rounded.Android),
    CPU("CPU", R.string.tab_cpu, Icons.Rounded.Memory),
    BATTERY("Battery", R.string.tab_battery, Icons.Rounded.BatteryChargingFull),
    NETWORK("Network", R.string.tab_network, Icons.Rounded.Wifi),
    CONNECTIVITY("Connectivity", R.string.tab_connectivity, Icons.Rounded.Bluetooth),
    DISPLAY("Display", R.string.tab_display, Icons.Rounded.DisplaySettings),
    MEMORY("Memory", R.string.tab_memory, Icons.Rounded.SdStorage),
    CAMERA("Camera", R.string.tab_camera, Icons.Rounded.PhotoCamera),
    THERMAL("Thermal", R.string.tab_thermal, Icons.Rounded.Thermostat),
    SENSORS("Sensors", R.string.tab_sensors, Icons.Rounded.Sensors),
    APPS("Apps", R.string.tab_apps, Icons.Rounded.Apps),
    TESTS("Tests", R.string.tab_tests, Icons.Rounded.CheckCircle);

    companion object {
        val entriesOrdered = entries.toTypedArray()
    }
}
