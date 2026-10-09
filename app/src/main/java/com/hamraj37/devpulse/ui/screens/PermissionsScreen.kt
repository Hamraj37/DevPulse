package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneCallback
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.ui.MainUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PermissionItemInfo(
    val name: String,
    val description: String,
    val permissionKey: String,
    val isSignature: Boolean,
    val icon: ImageVector,
    val allowedCount: Int = 0,
    val totalCount: Int = 0
)

@Composable
fun PermissionsScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Runtime, 1: Signature
    var isLoading by remember { mutableStateOf(true) }
    var permissionStats by remember { mutableStateOf<List<PermissionItemInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                }
            } catch (_: Throwable) {
                emptyList()
            }

            val reqCounts = mutableMapOf<String, Int>()
            val allowCounts = mutableMapOf<String, Int>()

            for (pkg in packages) {
                val reqs = pkg.requestedPermissions ?: continue
                val flags = pkg.requestedPermissionsFlags
                for (i in reqs.indices) {
                    val perm = reqs[i]
                    reqCounts[perm] = (reqCounts[perm] ?: 0) + 1
                    if (flags != null && i < flags.size) {
                        val isGranted = (flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                        if (isGranted) {
                            allowCounts[perm] = (allowCounts[perm] ?: 0) + 1
                        }
                    }
                }
            }

            val baseList = listOf(
                PermissionItemInfo("Post Notifications", "show notifications", if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else "android.permission.POST_NOTIFICATIONS", false, Icons.Rounded.Notifications),
                PermissionItemInfo("Camera", "take pictures and videos", Manifest.permission.CAMERA, false, Icons.Rounded.CameraAlt),
                PermissionItemInfo("Record Audio", "record audio", Manifest.permission.RECORD_AUDIO, false, Icons.Rounded.Mic),
                PermissionItemInfo("Read External Storage", "read the contents of your shared storage", Manifest.permission.READ_EXTERNAL_STORAGE, false, Icons.Rounded.Folder),
                PermissionItemInfo("Write External Storage", "modify or delete the contents of your shared storage", Manifest.permission.WRITE_EXTERNAL_STORAGE, false, Icons.Rounded.FolderZip),
                PermissionItemInfo("Access Coarse Location", "access approximate location only in the foreground", Manifest.permission.ACCESS_COARSE_LOCATION, false, Icons.Rounded.LocationOn),
                PermissionItemInfo("Access Fine Location", "access precise location only in the foreground", Manifest.permission.ACCESS_FINE_LOCATION, false, Icons.Rounded.MyLocation),
                PermissionItemInfo("Read Phone State", "read phone status and identity", Manifest.permission.READ_PHONE_STATE, false, Icons.Rounded.Phone),
                PermissionItemInfo("Read Media Images", "read image files from shared storage", if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else "android.permission.READ_MEDIA_IMAGES", false, Icons.Rounded.Image),
                PermissionItemInfo("Read Media Video", "read video files from shared storage", if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_VIDEO else "android.permission.READ_MEDIA_VIDEO", false, Icons.Rounded.Movie),
                PermissionItemInfo("Read Media Audio", "read audio files from shared storage", if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else "android.permission.READ_MEDIA_AUDIO", false, Icons.Rounded.LibraryMusic),
                PermissionItemInfo("Bluetooth Connect", "connect to paired Bluetooth devices", if (Build.VERSION.SDK_INT >= 31) Manifest.permission.BLUETOOTH_CONNECT else "android.permission.BLUETOOTH_CONNECT", false, Icons.Rounded.Bluetooth),
                PermissionItemInfo("Bluetooth Scan", "discover and pair nearby Bluetooth devices", if (Build.VERSION.SDK_INT >= 31) Manifest.permission.BLUETOOTH_SCAN else "android.permission.BLUETOOTH_SCAN", false, Icons.Rounded.BluetoothSearching),
                PermissionItemInfo("Get Accounts", "find accounts on the device", Manifest.permission.GET_ACCOUNTS, false, Icons.Rounded.AccountCircle),
                PermissionItemInfo("Read Contacts", "read your contacts", Manifest.permission.READ_CONTACTS, false, Icons.Rounded.Contacts),
                PermissionItemInfo("Write Contacts", "modify your contacts", Manifest.permission.WRITE_CONTACTS, false, Icons.Rounded.PersonAdd),
                PermissionItemInfo("Read Calendar", "read calendar events and details", Manifest.permission.READ_CALENDAR, false, Icons.Rounded.CalendarToday),
                PermissionItemInfo("Write Calendar", "add or modify calendar events", Manifest.permission.WRITE_CALENDAR, false, Icons.Rounded.Event),
                PermissionItemInfo("Call Phone", "directly call phone numbers", Manifest.permission.CALL_PHONE, false, Icons.Rounded.Call),
                PermissionItemInfo("Receive Sms", "receive text messages (SMS)", Manifest.permission.RECEIVE_SMS, false, Icons.Rounded.Sms),
                PermissionItemInfo("Read Sms", "read your text messages (SMS or MMS)", Manifest.permission.READ_SMS, false, Icons.Rounded.Message),
                PermissionItemInfo("Send Sms", "send SMS messages", Manifest.permission.SEND_SMS, false, Icons.Rounded.Send),
                PermissionItemInfo("Nearby Wifi Devices", "interact with nearby Wi-Fi devices", if (Build.VERSION.SDK_INT >= 33) Manifest.permission.NEARBY_WIFI_DEVICES else "android.permission.NEARBY_WIFI_DEVICES", false, Icons.Rounded.Wifi),
                PermissionItemInfo("Read Call Log", "read call log", Manifest.permission.READ_CALL_LOG, false, Icons.Rounded.PhoneCallback),
                PermissionItemInfo("Activity Recognition", "recognise physical activity", if (Build.VERSION.SDK_INT >= 29) Manifest.permission.ACTIVITY_RECOGNITION else "android.permission.ACTIVITY_RECOGNITION", false, Icons.Rounded.DirectionsRun),
                PermissionItemInfo("Access Background Location", "access location in the background", if (Build.VERSION.SDK_INT >= 29) Manifest.permission.ACCESS_BACKGROUND_LOCATION else "android.permission.ACCESS_BACKGROUND_LOCATION", false, Icons.Rounded.LocationSearching),

                // Signature / Special Permissions
                PermissionItemInfo("Display Over Apps", "draw over other apps", Manifest.permission.SYSTEM_ALERT_WINDOW, true, Icons.Rounded.Layers),
                PermissionItemInfo("Write Settings", "modify system settings", Manifest.permission.WRITE_SETTINGS, true, Icons.Rounded.Settings),
                PermissionItemInfo("Usage Stats", "track app usage stats", Manifest.permission.PACKAGE_USAGE_STATS, true, Icons.Rounded.BarChart),
                PermissionItemInfo("Install Packages", "install unknown apps", Manifest.permission.REQUEST_INSTALL_PACKAGES, true, Icons.Rounded.InstallMobile)
            )

            permissionStats = baseList.map { item ->
                val total = reqCounts[item.permissionKey] ?: 0
                val allowed = allowCounts[item.permissionKey] ?: 0
                item.copy(allowedCount = allowed, totalCount = total)
            }
            isLoading = false
        }
    }

    val filteredPermissions = remember(searchQuery, selectedTab, permissionStats) {
        permissionStats.filter { item ->
            val matchesTab = if (selectedTab == 0) !item.isSignature else item.isSignature
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = stringResource(R.string.permissions_title),
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.permissions_subtitle),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(R.string.permissions_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.permissions_search_placeholder)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        // Segmented Tab Switcher (Runtime | Signature)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabs = listOf(
                stringResource(R.string.permissions_tab_runtime),
                stringResource(R.string.permissions_tab_signature)
            )
            tabs.forEachIndexed { index, tabTitle ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabTitle,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider()

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else if (filteredPermissions.isEmpty()) {
                Text(
                    text = stringResource(R.string.permissions_none_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                filteredPermissions.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (item.totalCount > 0) "${item.allowedCount} ${stringResource(R.string.permissions_allowed_of)} ${item.totalCount}" else stringResource(R.string.permissions_not_requested),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
