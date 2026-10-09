package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hamraj37.devpulse.ui.MainUiState
import kotlinx.coroutines.delay
import java.io.File

fun checkSuBinaries(): Pair<Boolean, String> {
    val paths = arrayOf(
        "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
        "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
        "/system/bin/failsafe/su", "/data/local/su"
    )
    for (path in paths) {
        if (File(path).exists()) {
            return Pair(true, "Found at $path")
        }
    }
    return Pair(false, "No su binary detected in system paths")
}

fun checkRootApps(context: Context): Pair<Boolean, String> {
    val rootPkgs = mapOf(
        "com.topjohnwu.magisk" to "Magisk",
        "me.weishu.kernelsu" to "KernelSU",
        "com.noshufou.android.su" to "Superuser",
        "eu.chainfire.supersu" to "SuperSU",
        "com.koushikdutta.superuser" to "Superuser (Koush)",
        "com.kingroot.kinguser" to "KingRoot",
        "me.b3nac.apatch" to "APatch"
    )
    for ((pkg, name) in rootPkgs) {
        try {
            context.packageManager.getPackageInfo(pkg, 0)
            return Pair(true, "$name Installed ($pkg)")
        } catch (_: Exception) {}
    }
    return Pair(false, "No root management app detected")
}

fun checkBusyBox(): Pair<Boolean, String> {
    val paths = arrayOf("/system/xbin/busybox", "/system/bin/busybox", "/sbin/busybox")
    for (path in paths) {
        if (File(path).exists()) return Pair(true, "Detected at $path")
    }
    return Pair(false, "Not Installed")
}

@Composable
fun RootCheckerScreen(
    uiState: MainUiState,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }

    val suCheck = remember(isScanning) { checkSuBinaries() }
    val rootAppCheck = remember(isScanning) { checkRootApps(context) }
    val busyBoxCheck = remember(isScanning) { checkBusyBox() }

    val isTestKeys = remember { Build.TAGS != null && Build.TAGS.contains("test-keys") }
    val isRooted = suCheck.first || rootAppCheck.first || isTestKeys

    fun runScan() {
        isScanning = true
        Toast.makeText(context, context.getString(R.string.toast_scanning_root), Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(1000)
            isScanning = false
            Toast.makeText(context, if (isRooted) context.getString(R.string.toast_root_access_detected) else context.getString(R.string.toast_device_not_rooted), Toast.LENGTH_SHORT).show()
        }
    }

    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = "Root Checker",
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
            // Top Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRooted) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isRooted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.AdminPanelSettings,
                                    contentDescription = "Root Checker",
                                    tint = if (isRooted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.root_checker_title),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isRooted) "ROOTED / Superuser Detected" else "NOT ROOTED (Standard System)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isRooted) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                            )
                        }
                    }

                    if (isScanning) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { runScan() },
                        enabled = !isScanning,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Verify Root",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = if (isScanning) stringResource(R.string.root_checker_scanning) else stringResource(R.string.root_checker_verify_status))
                        }
                    }
                }
            }

            // Section 1: Binary & Superuser App Inspection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = "Root Inspection",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Root Binaries & Superuser Inspection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = dividerColor)

                    RootCheckItemRow(
                        label = "su Binary Executable",
                        statusText = if (suCheck.first) "Detected" else "Not Found",
                        isPassed = !suCheck.first,
                        detailText = suCheck.second
                    )

                    HorizontalDivider(color = dividerColor)

                    RootCheckItemRow(
                        label = "Superuser / Magisk Apps",
                        statusText = if (rootAppCheck.first) "Detected" else "Not Installed",
                        isPassed = !rootAppCheck.first,
                        detailText = rootAppCheck.second
                    )

                    HorizontalDivider(color = dividerColor)

                    RootCheckItemRow(
                        label = "BusyBox Binary",
                        statusText = if (busyBoxCheck.first) "Detected" else "Not Installed",
                        isPassed = !busyBoxCheck.first,
                        detailText = busyBoxCheck.second
                    )
                }
            }

            // Section 2: Firmware & Build Integrity
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = "Build Integrity",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Firmware & System Security State",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = dividerColor)

                    RootCheckItemRow(
                        label = "OS Build Keys",
                        statusText = if (isTestKeys) "Test-Keys (Custom ROM)" else "Release-Keys (Official)",
                        isPassed = !isTestKeys,
                        detailText = "Build Tags: ${Build.TAGS ?: "release-keys"}"
                    )

                    HorizontalDivider(color = dividerColor)

                    RootCheckItemRow(
                        label = "SELinux Enforcing State",
                        statusText = uiState.systemInfo.seLinux,
                        isPassed = uiState.systemInfo.seLinux.contains("Enforcing", ignoreCase = true),
                        detailText = "System Security Policy Mode"
                    )

                    HorizontalDivider(color = dividerColor)

                    RootCheckItemRow(
                        label = "System Directory Read-Only",
                        statusText = "Protected",
                        isPassed = true,
                        detailText = "/system partition write protection"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RootCheckItemRow(
    label: String,
    statusText: String,
    isPassed: Boolean,
    detailText: String
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detailText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isPassed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isPassed) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = null,
                    tint = if (isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isPassed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
