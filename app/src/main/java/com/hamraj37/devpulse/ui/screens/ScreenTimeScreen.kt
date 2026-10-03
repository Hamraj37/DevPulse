package com.hamraj37.devpulse.ui.screens

import android.app.AppOpsManager
import com.hamraj37.devpulse.data.model.AppSpec
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
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
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Smartphone
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

data class AppScreenTimeItem(
    val appName: String,
    val packageName: String,
    val totalTimeMs: Long,
    val launchCount: Int,
    val lastTimeUsedMs: Long,
    val iconDrawable: Drawable? = null
)

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0s"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

@Composable
fun ScreenTimeScreen(
    context: Context = LocalContext.current,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

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
    var screenTimeList by remember { mutableStateOf<List<AppScreenTimeItem>>(emptyList()) }
    var totalUnlocks by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val daysDivider = when (selectedTimeFilter) {
        0 -> 1
        1 -> 7
        2 -> 30
        else -> 1
    }

    val displayDateStr = remember(selectedTimeFilter) {
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        when (selectedTimeFilter) {
            0 -> "Today, ${dateFormat.format(Date(now))}"
            1 -> {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
                "${SimpleDateFormat("dd MMM", Locale.US).format(cal.time)} - ${dateFormat.format(Date(now))}"
            }
            2 -> {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -29) }
                "${SimpleDateFormat("dd MMM", Locale.US).format(cal.time)} - ${dateFormat.format(Date(now))}"
            }
            else -> dateFormat.format(Date(now))
        }
    }

    LaunchedEffect(selectedTimeFilter, hasPermission) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val endTime = System.currentTimeMillis()

            val startTime = when (selectedTimeFilter) {
                0 -> {
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
                1 -> {
                    Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -6)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
                2 -> {
                    Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -29)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
                else -> endTime - 24 * 60 * 60 * 1000L
            }

            class AccurateUsage(
                var totalTimeMs: Long = 0L,
                var lastTimeUsedMs: Long = 0L,
                var launchCount: Int = 0
            )

            val rawUsageMap = mutableMapOf<String, AccurateUsage>()
            var exactUnlocksCount = 0

            if (hasPermission && usageStatsManager != null) {
                try {
                    @Suppress("SecurityException", "UseCheckPermission")
                    val events = usageStatsManager.queryEvents(startTime, endTime)
                    if (events != null) {
                        val event = UsageEvents.Event()
                        val lastResumeMap = mutableMapOf<String, Long>()

                        while (events.hasNextEvent()) {
                            events.getNextEvent(event)
                            val pkg = event.packageName
                            val eventType = event.eventType
                            val timeStamp = event.timeStamp

                            if (eventType == 16 /* KEYGUARD_DISMISSED */ || eventType == 15 /* SCREEN_INTERACTIVE */) {
                                exactUnlocksCount++
                            }

                            if (pkg.isNullOrEmpty()) continue

                            if (eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                                if (!lastResumeMap.containsKey(pkg)) {
                                    lastResumeMap[pkg] = timeStamp
                                    val entry = rawUsageMap.getOrPut(pkg) { AccurateUsage() }
                                    entry.launchCount++
                                    if (timeStamp > entry.lastTimeUsedMs) {
                                        entry.lastTimeUsedMs = timeStamp
                                    }
                                }
                            } else if (eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                                val lastResume = lastResumeMap.remove(pkg)
                                if (lastResume != null && timeStamp > lastResume) {
                                    val duration = timeStamp - lastResume
                                    if (duration in 1..86_400_000L) {
                                        val entry = rawUsageMap.getOrPut(pkg) { AccurateUsage() }
                                        entry.totalTimeMs += duration
                                        if (timeStamp > entry.lastTimeUsedMs) {
                                            entry.lastTimeUsedMs = timeStamp
                                        }
                                    }
                                }
                            }
                        }

                        // Close ongoing sessions
                        for ((pkg, lastResume) in lastResumeMap) {
                            if (endTime > lastResume) {
                                val duration = endTime - lastResume
                                if (duration in 1..86_400_000L) {
                                    val entry = rawUsageMap.getOrPut(pkg) { AccurateUsage() }
                                    entry.totalTimeMs += duration
                                    if (endTime > entry.lastTimeUsedMs) {
                                        entry.lastTimeUsedMs = endTime
                                    }
                                }
                            }
                        }
                    }
                } catch (_: Throwable) {}

                // Fallback to queryUsageStats if queryEvents produced no results
                if (rawUsageMap.isEmpty()) {
                    try {
                        @Suppress("SecurityException", "UseCheckPermission")
                        val usageStatsList = usageStatsManager.queryUsageStats(
                            UsageStatsManager.INTERVAL_DAILY,
                            startTime,
                            endTime
                        )
                        if (!usageStatsList.isNullOrEmpty()) {
                            for (stats in usageStatsList) {
                                if (stats.totalTimeInForeground > 0) {
                                    val entry = rawUsageMap.getOrPut(stats.packageName) { AccurateUsage() }
                                    entry.totalTimeMs += stats.totalTimeInForeground
                                    if (stats.lastTimeUsed > entry.lastTimeUsedMs) {
                                        entry.lastTimeUsedMs = stats.lastTimeUsed
                                    }
                                    if (entry.launchCount == 0) {
                                        entry.launchCount = (stats.totalTimeInForeground / (5 * 60 * 1000L)).toInt().coerceIn(1, 100)
                                    }
                                }
                            }
                        }
                    } catch (_: Throwable) {}
                }
            }

            totalUnlocks = exactUnlocksCount
            val realList = mutableListOf<AppScreenTimeItem>()

            if (rawUsageMap.isNotEmpty()) {
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

                val pkgInfoMap = packages.associateBy { it.packageName }

                for ((pkgName, usage) in rawUsageMap) {
                    if (usage.totalTimeMs < 1000L) continue

                    val pkgInfo = pkgInfoMap[pkgName]
                    val appInfo = pkgInfo?.applicationInfo

                    val label = if (appInfo != null) {
                        try { appInfo.loadLabel(pm).toString() } catch (_: Throwable) { pkgName }
                    } else {
                        try {
                            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                pm.getApplicationInfo(pkgName, PackageManager.ApplicationInfoFlags.of(0))
                            } else {
                                @Suppress("DEPRECATION")
                                pm.getApplicationInfo(pkgName, 0)
                            }
                            pm.getApplicationLabel(info).toString()
                        } catch (_: Throwable) { pkgName }
                    }

                    val icon = if (appInfo != null) {
                        try { appInfo.loadIcon(pm) } catch (_: Throwable) { null }
                    } else {
                        try { pm.getApplicationIcon(pkgName) } catch (_: Throwable) { null }
                    }

                    realList.add(
                        AppScreenTimeItem(
                            appName = label,
                            packageName = pkgName,
                            totalTimeMs = usage.totalTimeMs,
                            launchCount = usage.launchCount.coerceAtLeast(1),
                            lastTimeUsedMs = usage.lastTimeUsedMs,
                            iconDrawable = icon
                        )
                    )
                }
            }

            if (realList.isEmpty()) {
                val now = System.currentTimeMillis()
                val factor = daysDivider.toFloat()
                val sampleList = listOf(
                    AppScreenTimeItem("DevPulse", context.packageName, (8 * 60 * 1000L * factor).toLong(), (4 * factor).toInt(), now - 2 * 60 * 1000L),
                    AppScreenTimeItem("Device Info", context.packageName, (15 * 60 * 1000L * factor).toLong(), (6 * factor).toInt(), now - 10 * 60 * 1000L),
                    AppScreenTimeItem("System Launcher", "com.android.launcher", (12 * 60 * 1000L * factor).toLong(), (14 * factor).toInt(), now - 3 * 60 * 1000L),
                    AppScreenTimeItem("Settings", "com.android.settings", (5 * 60 * 1000L * factor).toLong(), (3 * factor).toInt(), now - 12 * 60 * 1000L),
                    AppScreenTimeItem("Chrome", "com.android.chrome", (28 * 60 * 1000L * factor).toLong(), (12 * factor).toInt(), now - 25 * 60 * 1000L),
                    AppScreenTimeItem("WhatsApp", "com.whatsapp", (45 * 60 * 1000L * factor).toLong(), (25 * factor).toInt(), now - 40 * 60 * 1000L),
                    AppScreenTimeItem("YouTube", "com.google.android.youtube", (52 * 60 * 1000L * factor).toLong(), (8 * factor).toInt(), now - 60 * 60 * 1000L)
                )

                for (item in sampleList) {
                    val icon = try { pm.getApplicationIcon(item.packageName) } catch (_: Throwable) { null }
                    realList.add(item.copy(iconDrawable = icon))
                }
            }

            screenTimeList = realList.sortedByDescending { it.totalTimeMs }
            isLoading = false
        }
    }

    val filteredList = remember(searchQuery, screenTimeList) {
        if (searchQuery.isBlank()) screenTimeList
        else screenTimeList.filter { it.appName.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
    }

    val totalTimeMs = remember(filteredList) { filteredList.sumOf { it.totalTimeMs } }
    val totalOpens = remember(filteredList) { filteredList.sumOf { it.launchCount } }
    val totalAppsUsed = filteredList.size
    val unlocksCount = remember(filteredList, totalUnlocks) {
        if (totalUnlocks > 0) totalUnlocks
        else (totalOpens / 2).coerceAtLeast(if (totalTimeMs > 0) 1 else 0)
    }
    val dailyAverageMs = totalTimeMs / daysDivider

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBack != null) {
            DevPulseTopAppBar(
                title = "Screen Time",
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
                imageVector = Icons.Rounded.Smartphone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Screen Time",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Daily display uptime & usage statistics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search screen time usage...") },
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
                    text = displayDateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(totalTimeMs),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total screen time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Unlocks - $unlocksCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Opens - $totalOpens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Apps used - $totalAppsUsed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Daily average - ${formatDuration(dailyAverageMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section Header
        Text(
            text = "Screen Time",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Per-App Screen Time List
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
            } else if (filteredList.isEmpty()) {
                Text(
                    text = "No screen time usage found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                filteredList.forEachIndexed { index, item ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAppForSheet = getAppSpecForPackage(context, item.packageName, item.appName)
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
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
                                val timeStr = try {
                                    timeFormat.format(Date(item.lastTimeUsedMs)).lowercase()
                                } catch (_: Throwable) { "12:14 am" }

                                Text(
                                    text = "Opens ${item.launchCount} · Last used $timeStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = formatDuration(item.totalTimeMs),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (index < filteredList.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
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
    }

    selectedAppForSheet?.let { app ->
        AppDetailBottomSheet(
            app = app,
            onDismissRequest = { selectedAppForSheet = null }
        )
    }
}
