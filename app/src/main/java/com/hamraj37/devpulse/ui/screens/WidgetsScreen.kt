package com.hamraj37.devpulse.ui.screens

import com.hamraj37.devpulse.R
import androidx.compose.ui.res.stringResource
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.Toast
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.ui.MainUiState
import com.hamraj37.devpulse.widget.DevPulseClockWidgetProvider
import com.hamraj37.devpulse.widget.DevPulseLargeWidgetProvider
import com.hamraj37.devpulse.widget.DevPulseMediumWidgetProvider
import com.hamraj37.devpulse.widget.DevPulseSmallWidgetProvider
import java.util.Locale

@Composable
fun WidgetsScreen(
    uiState: MainUiState,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current

    fun requestPinWidget(providerClass: Class<*>, widgetName: String) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val myWidget = ComponentName(context, providerClass)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val successCallback = PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(context, providerClass),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    appWidgetManager.requestPinAppWidget(myWidget, null, successCallback)
                    Toast.makeText(context, context.getString(R.string.toast_widget_pin_request_sent), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_widget_long_press_home), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, context.getString(R.string.toast_widget_add_from_picker), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.toast_unable_request_pin, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    val ramUsedBytes = uiState.dashboardInfo.ramUsedBytes.coerceAtLeast(0L)
    val ramTotalBytes = uiState.dashboardInfo.ramTotalBytes.coerceAtLeast(1L)
    val ramUsedGb = String.format(Locale.US, "%.2fGB", ramUsedBytes / (1024.0 * 1024.0 * 1024.0))
    val ramPercent = ((ramUsedBytes.toFloat() / ramTotalBytes.toFloat()) * 100).toInt().coerceIn(0, 100)

    val storageUsedBytes = uiState.dashboardInfo.storageUsedBytes.coerceAtLeast(0L)
    val storageTotalBytes = uiState.dashboardInfo.storageTotalBytes.coerceAtLeast(1L)
    val storageUsedGb = String.format(Locale.US, "%.2fGB", storageUsedBytes / (1024.0 * 1024.0 * 1024.0))
    val storagePercent = ((storageUsedBytes.toFloat() / storageTotalBytes.toFloat()) * 100).toInt().coerceIn(0, 100)

    val batteryTemp = uiState.batteryInfo.temperatureCelsius
    val batteryLevel = uiState.batteryInfo.levelPercent

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = "Home Screen Widgets",
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



        Text(
            text = "Preview of all the widgets available for you. Use your launcher to add widgets",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Showcase Container
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Clock Widget Preview
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requestPinWidget(DevPulseClockWidgetProvider::class.java, "Clock Widget") }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.widget_clock_preview), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(stringResource(R.string.btn_pin), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "10:42 AM",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 36.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Thursday, October 02",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. Large System Widget Preview
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requestPinWidget(DevPulseLargeWidgetProvider::class.java, "Large System Widget") }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.widget_large_preview), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(stringResource(R.string.btn_pin), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                        Text(stringResource(R.string.widget_title_large), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.dashboard_internal_storage), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$storageUsedGb " + stringResource(R.string.lbl_used), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            LinearProgressIndicator(progress = { storagePercent / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 2.dp).height(6.dp).clip(CircleShape))
                        }

                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.memory_ram_memory), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$ramUsedGb " + stringResource(R.string.lbl_used), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            LinearProgressIndicator(progress = { ramPercent / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 2.dp).height(6.dp).clip(CircleShape))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${stringResource(R.string.battery_temp_chip)}: ${batteryTemp} ℃", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text("${stringResource(R.string.tab_battery)}: $batteryLevel%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // 3. Medium Status Widget Preview
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requestPinWidget(DevPulseMediumWidgetProvider::class.java, "Medium Status Widget") }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.widget_medium_preview), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(stringResource(R.string.btn_pin), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                        Text(stringResource(R.string.widget_title_devpulse_status), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.widget_lbl_storage_used), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$storageUsedGb " + stringResource(R.string.lbl_used), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.widget_lbl_ram_used), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$ramUsedGb " + stringResource(R.string.lbl_used), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // 4. Small RAM Widget Preview
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requestPinWidget(DevPulseSmallWidgetProvider::class.java, "Small RAM Widget") }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.widget_small_preview), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(stringResource(R.string.btn_pin), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Text(stringResource(R.string.widget_lbl_ram_used), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = ramUsedGb,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                try {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val myWidget = ComponentName(context, DevPulseLargeWidgetProvider::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                            val successCallback = PendingIntent.getBroadcast(
                                context,
                                0,
                                Intent(context, DevPulseLargeWidgetProvider::class.java),
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            appWidgetManager.requestPinAppWidget(myWidget, null, successCallback)
                            Toast.makeText(context, context.getString(R.string.toast_widget_pin_request_sent), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.toast_widget_long_press_home), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.toast_widget_add_from_picker), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, context.getString(R.string.toast_unable_request_pin, e.message ?: ""), Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.btn_add_widget_to_home))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
