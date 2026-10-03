package com.hamraj37.devpulse.ui.screens

import android.app.AppOpsManager
import com.hamraj37.devpulse.data.model.AppSpec
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.hamraj37.devpulse.ui.MainUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AppDataUsageItem(
    val appName: String,
    val packageName: String,
    val wifiBytes: Long,
    val mobileBytes: Long,
    val iconDrawable: Drawable? = null
) {
    val totalBytes: Long get() = wifiBytes + mobileBytes
}

fun formatDataSize(bytes: Long): String {
    val b = bytes.coerceAtLeast(0L)
    return when {
        b >= 1024L * 1024L * 1024L -> String.format(Locale.US, "%.2f GB", b / (1024f * 1024f * 1024f))
        b >= 1024L * 1024L -> String.format(Locale.US, "%.2f MB", b / (1024f * 1024f))
        b >= 1024L -> String.format(Locale.US, "%.2f KB", b / 1024f)
        else -> "$b B"
    }
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

private data class NetworkBytes(val rxBytes: Long, val txBytes: Long) {
    val totalBytes: Long get() = rxBytes + txBytes
}

private fun queryUidNetworkBytesMap(
    networkStatsManager: NetworkStatsManager?,
    networkType: Int,
    startTime: Long,
    endTime: Long
): Map<Int, NetworkBytes> {
    val result = mutableMapOf<Int, NetworkBytes>()
    if (networkStatsManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return result
    try {
        @Suppress("DEPRECATION")
        val stats = networkStatsManager.querySummary(networkType, null, startTime, endTime)
        if (stats != null) {
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val uid = bucket.uid
                val rx = bucket.rxBytes.coerceAtLeast(0L)
                val tx = bucket.txBytes.coerceAtLeast(0L)
                if (rx > 0L || tx > 0L) {
                    val prev = result[uid] ?: NetworkBytes(0L, 0L)
                    result[uid] = NetworkBytes(prev.rxBytes + rx, prev.txBytes + tx)
                }
            }
            stats.close()
        }
    } catch (_: Throwable) {}
    return result
}

private fun queryDeviceSummaryBytes(
    networkStatsManager: NetworkStatsManager?,
    networkType: Int,
    startTime: Long,
    endTime: Long
): Pair<Long, Long> {
    if (networkStatsManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return Pair(0L, 0L)
    return try {
        @Suppress("DEPRECATION")
        val bucket = networkStatsManager.querySummaryForDevice(networkType, null, startTime, endTime)
        Pair(bucket.rxBytes.coerceAtLeast(0L), bucket.txBytes.coerceAtLeast(0L))
    } catch (_: Throwable) {
        Pair(0L, 0L)
    }
}

@Composable
fun DataUsageScreen(
    uiState: MainUiState,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = hasUsageStatsPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeFilter by remember { mutableStateOf(0) } // 0: Today, 1: Last 7 Days, 2: Last 30 Days
    var selectedAppForSheet by remember { mutableStateOf<AppSpec?>(null) }
    var appUsageList by remember { mutableStateOf<List<AppDataUsageItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var summaryWifiRx by remember { mutableLongStateOf(0L) }
    var summaryWifiTx by remember { mutableLongStateOf(0L) }
    var summaryMobileRx by remember { mutableLongStateOf(0L) }
    var summaryMobileTx by remember { mutableLongStateOf(0L) }

    val currentDateStr = remember {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        sdf.format(Date())
    }

    LaunchedEffect(selectedTimeFilter, hasPermission) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledPackages(0)
                }
            } catch (_: Throwable) {
                emptyList()
            }

            val hasPermission = hasUsageStatsPermission(context)
            val networkStatsManager = if (hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
            } else null

            val endTime = System.currentTimeMillis()
            val startTime = when (selectedTimeFilter) {
                0 -> {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
                1 -> endTime - 7 * 24 * 60 * 60 * 1000L
                2 -> endTime - 30 * 24 * 60 * 60 * 1000L
                else -> endTime - 24 * 60 * 60 * 1000L
            }

            @Suppress("DEPRECATION")
            val wifiMap = queryUidNetworkBytesMap(networkStatsManager, ConnectivityManager.TYPE_WIFI, startTime, endTime)
            @Suppress("DEPRECATION")
            val mobileMap = queryUidNetworkBytesMap(networkStatsManager, ConnectivityManager.TYPE_MOBILE, startTime, endTime)

            var wRx = wifiMap.values.sumOf { it.rxBytes }
            var wTx = wifiMap.values.sumOf { it.txBytes }
            var mRx = mobileMap.values.sumOf { it.rxBytes }
            var mTx = mobileMap.values.sumOf { it.txBytes }

            if (wRx == 0L && wTx == 0L && mRx == 0L && mTx == 0L && networkStatsManager != null) {
                val (wifiRxRes, wifiTxRes) = queryDeviceSummaryBytes(networkStatsManager, ConnectivityManager.TYPE_WIFI, startTime, endTime)
                val (mobRxRes, mobTxRes) = queryDeviceSummaryBytes(networkStatsManager, ConnectivityManager.TYPE_MOBILE, startTime, endTime)
                wRx = wifiRxRes
                wTx = wifiTxRes
                mRx = mobRxRes
                mTx = mobTxRes
            }

            // Fallback to TrafficStats if device network stats returned 0
            if (wRx == 0L && wTx == 0L && mRx == 0L && mTx == 0L) {
                val totRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
                val totTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
                val mobRx = TrafficStats.getMobileRxBytes().coerceAtLeast(0L)
                val mobTx = TrafficStats.getMobileTxBytes().coerceAtLeast(0L)

                mRx = mobRx
                mTx = mobTx
                wRx = maxOf(0L, totRx - mobRx)
                wTx = maxOf(0L, totTx - mobTx)
            }

            summaryWifiRx = wRx
            summaryWifiTx = wTx
            summaryMobileRx = mRx
            summaryMobileTx = mTx

            val realList = mutableListOf<AppDataUsageItem>()

            for (pkg in packages) {
                val appInfo = pkg.applicationInfo ?: continue
                val uid = appInfo.uid

                val label = try { appInfo.loadLabel(pm).toString() } catch (_: Throwable) { pkg.packageName }
                val icon = try { appInfo.loadIcon(pm) } catch (_: Throwable) { null }

                var wifi = wifiMap[uid]?.totalBytes ?: 0L
                var mobile = mobileMap[uid]?.totalBytes ?: 0L

                if (wifi == 0L && mobile == 0L) {
                    val rx = TrafficStats.getUidRxBytes(uid)
                    val tx = TrafficStats.getUidTxBytes(uid)
                    val appRx = if (rx > 0) rx else 0L
                    val appTx = if (tx > 0) tx else 0L
                    val rawAppTotal = appRx + appTx

                    if (rawAppTotal > 0) {
                        val totalSys = wRx + wTx + mRx + mTx
                        val mobRatio = if (totalSys > 0) (mRx + mTx).toDouble() / totalSys else 0.12

                        mobile = (rawAppTotal * mobRatio).toLong()
                        wifi = maxOf(0L, rawAppTotal - mobile)
                    }
                }

                if (wifi > 0L || mobile > 0L) {
                    realList.add(
                        AppDataUsageItem(
                            appName = label,
                            packageName = pkg.packageName,
                            wifiBytes = wifi,
                            mobileBytes = mobile,
                            iconDrawable = icon
                        )
                    )
                }
            }

            appUsageList = realList.sortedByDescending { it.totalBytes }
            isLoading = false
        }
    }

    val filteredApps = remember(searchQuery, appUsageList) {
        val nonZeroList = appUsageList.filter { it.totalBytes > 0L }
        if (searchQuery.isBlank()) nonZeroList
        else nonZeroList.filter { it.appName.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
    }

    val totalWifiBytes = remember(filteredApps, summaryWifiRx, summaryWifiTx) {
        val sumAppsWifi = filteredApps.sumOf { it.wifiBytes }
        val deviceWifi = summaryWifiRx + summaryWifiTx
        maxOf(sumAppsWifi, deviceWifi)
    }

    val totalMobileBytes = remember(filteredApps, summaryMobileRx, summaryMobileTx) {
        val sumAppsMobile = filteredApps.sumOf { it.mobileBytes }
        val deviceMobile = summaryMobileRx + summaryMobileTx
        maxOf(sumAppsMobile, deviceMobile)
    }

    val totalRxBytes = remember(summaryWifiRx, summaryMobileRx, filteredApps) {
        val deviceRx = summaryWifiRx + summaryMobileRx
        if (deviceRx > 0) deviceRx else ((totalWifiBytes + totalMobileBytes) * 0.78).toLong()
    }

    val totalTxBytes = remember(summaryWifiTx, summaryMobileTx, filteredApps) {
        val deviceTx = summaryWifiTx + summaryMobileTx
        if (deviceTx > 0) deviceTx else maxOf(0L, (totalWifiBytes + totalMobileBytes) - totalRxBytes)
    }

    val totalRxTxBytes = totalWifiBytes + totalMobileBytes

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (onBack != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Data Usage",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SwapHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Data Usage",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Mobile and Wi-Fi network traffic stats",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search app usage...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear")
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

        // Filter Pills: Today | Last 7 Days | Last 30 Days
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val filters = listOf("Today", "Last 7 Days", "Last 30 Days")
            filters.forEachIndexed { index, filterTitle ->
                val isSelected = selectedTimeFilter == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent)
                        .clickable { selectedTimeFilter = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filterTitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = currentDateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDataSize(totalRxTxBytes),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total data usage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Wi-Fi - ${formatDataSize(totalWifiBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Mobile - ${formatDataSize(totalMobileBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⬇ Download - ${formatDataSize(totalRxBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "⬆ Upload - ${formatDataSize(totalTxBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // App usage Header
        Text(
            text = "App usage",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Per-App Usage List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else if (filteredApps.isEmpty()) {
                Text(
                    text = "No app usage data found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                filteredApps.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedAppForSheet = getAppSpecForPackage(context, item.packageName, item.appName)
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App Icon
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (item.iconDrawable != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(item.iconDrawable),
                                        contentDescription = item.appName,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Android,
                                        contentDescription = item.appName,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.appName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Wi-Fi ${formatDataSize(item.wifiBytes)} · Mobile ${formatDataSize(item.mobileBytes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = formatDataSize(item.totalBytes),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (!hasPermission) {
            Button(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Opening System Settings...", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Open System Usage Access Settings")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    selectedAppForSheet?.let { app ->
        AppDetailBottomSheet(
            app = app,
            onDismissRequest = { selectedAppForSheet = null }
        )
    }
}
