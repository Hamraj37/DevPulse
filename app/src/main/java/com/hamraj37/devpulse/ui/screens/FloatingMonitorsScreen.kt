package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.SsidChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hamraj37.devpulse.service.FloatingMonitorService

@Composable
fun FloatingMonitorsScreen(
    context: Context = LocalContext.current,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    var fpsOverlay by remember { mutableStateOf(FloatingMonitorService.fpsEnabled) }
    var cpuOverlay by remember { mutableStateOf(FloatingMonitorService.cpuEnabled) }
    var ramOverlay by remember { mutableStateOf(FloatingMonitorService.ramEnabled) }
    var batteryOverlay by remember { mutableStateOf(FloatingMonitorService.batteryEnabled) }

    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
                fpsOverlay = FloatingMonitorService.fpsEnabled
                cpuOverlay = FloatingMonitorService.cpuEnabled
                ramOverlay = FloatingMonitorService.ramEnabled
                batteryOverlay = FloatingMonitorService.batteryEnabled
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun updateServiceState(cpu: Boolean, ram: Boolean, battery: Boolean, fps: Boolean) {
        if (!cpu && !ram && !battery && !fps) {
            FloatingMonitorService.stopService(context)
        } else {
            FloatingMonitorService.startService(context, cpu, ram, battery, fps)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = stringResource(R.string.floating_monitors_title),
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
                imageVector = Icons.Rounded.SsidChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.floating_monitors_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(R.string.floating_monitors_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        if (!hasOverlayPermission) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.floating_monitors_perm_hdr),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = stringResource(R.string.floating_monitors_perm_msg),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    context.startActivity(intent)
                                } catch (e2: Exception) {
                                    Toast.makeText(context, context.getString(R.string.toast_unable_open_overlay_settings), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.btn_grant_permission))
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ToggleRow(
                    title = stringResource(R.string.floating_fps_title),
                    subtitle = stringResource(R.string.floating_fps_desc),
                    checked = fpsOverlay,
                    onCheckedChange = {
                        if (!hasOverlayPermission) {
                            Toast.makeText(context, context.getString(R.string.toast_overlay_permission_required), Toast.LENGTH_SHORT).show()
                        } else {
                            fpsOverlay = it
                            updateServiceState(cpuOverlay, ramOverlay, batteryOverlay, it)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ToggleRow(
                    title = stringResource(R.string.floating_cpu_title),
                    subtitle = stringResource(R.string.floating_cpu_desc),
                    checked = cpuOverlay,
                    onCheckedChange = {
                        if (!hasOverlayPermission) {
                            Toast.makeText(context, context.getString(R.string.toast_overlay_permission_required), Toast.LENGTH_SHORT).show()
                        } else {
                            cpuOverlay = it
                            updateServiceState(it, ramOverlay, batteryOverlay, fpsOverlay)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ToggleRow(
                    title = stringResource(R.string.floating_ram_title),
                    subtitle = stringResource(R.string.floating_ram_desc),
                    checked = ramOverlay,
                    onCheckedChange = {
                        if (!hasOverlayPermission) {
                            Toast.makeText(context, context.getString(R.string.toast_overlay_permission_required), Toast.LENGTH_SHORT).show()
                        } else {
                            ramOverlay = it
                            updateServiceState(cpuOverlay, it, batteryOverlay, fpsOverlay)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ToggleRow(
                    title = stringResource(R.string.floating_battery_title),
                    subtitle = stringResource(R.string.floating_battery_desc),
                    checked = batteryOverlay,
                    onCheckedChange = {
                        if (!hasOverlayPermission) {
                            Toast.makeText(context, context.getString(R.string.toast_overlay_permission_required), Toast.LENGTH_SHORT).show()
                        } else {
                            batteryOverlay = it
                            updateServiceState(cpuOverlay, ramOverlay, it, fpsOverlay)
                        }
                    }
                )
            }
        }

        // Live Preview Badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.floating_monitors_preview),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "60 FPS | CPU 24% | RAM 4.2 GB | 32.5°C",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = stringResource(R.string.lbl_live),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
