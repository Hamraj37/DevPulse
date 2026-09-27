package com.hamraj37.devpulse.data.model

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

enum class AppTab(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    DEVICE("Device", Icons.Rounded.PhoneAndroid),
    SYSTEM("System", Icons.Rounded.Android),
    CPU("CPU", Icons.Rounded.Memory),
    BATTERY("Battery", Icons.Rounded.BatteryChargingFull),
    NETWORK("Network", Icons.Rounded.Wifi),
    CONNECTIVITY("Connectivity", Icons.Rounded.Bluetooth),
    DISPLAY("Display", Icons.Rounded.DisplaySettings),
    MEMORY("Memory", Icons.Rounded.SdStorage),
    CAMERA("Camera", Icons.Rounded.PhotoCamera),
    THERMAL("Thermal", Icons.Rounded.Thermostat),
    SENSORS("Sensors", Icons.Rounded.Sensors),
    APPS("Apps", Icons.Rounded.Apps),
    TESTS("Tests", Icons.Rounded.CheckCircle);

    companion object {
        val entriesOrdered = entries.toTypedArray()
    }
}
