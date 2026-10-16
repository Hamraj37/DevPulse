package com.hamraj37.devpulse.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdateManager {

    private val managerScope = CoroutineScope(Dispatchers.Default)

    fun hasActiveWidgets(context: Context): Boolean {
        return try {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return false

            val dashboardCount = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DevPulseDashboardWidgetProvider::class.java)
            ).size

            val compactCount = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DevPulseCompactWidgetProvider::class.java)
            ).size

            val storageCount = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DevPulseStorageWidgetProvider::class.java)
            ).size

            val minimalCount = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DevPulseMinimalWidgetProvider::class.java)
            ).size

            (dashboardCount + compactCount + storageCount + minimalCount) > 0
        } catch (_: Throwable) {
            false
        }
    }

    fun updateAllWidgets(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

            val dashboardIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DevPulseDashboardWidgetProvider::class.java)
            )
            for (id in dashboardIds) {
                try {
                    DevPulseDashboardWidgetProvider.updateWidget(context, appWidgetManager, id)
                } catch (_: Throwable) {}
            }

            val compactIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DevPulseCompactWidgetProvider::class.java)
            )
            for (id in compactIds) {
                try {
                    DevPulseCompactWidgetProvider.updateWidget(context, appWidgetManager, id)
                } catch (_: Throwable) {}
            }

            val storageIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DevPulseStorageWidgetProvider::class.java)
            )
            for (id in storageIds) {
                try {
                    DevPulseStorageWidgetProvider.updateWidget(context, appWidgetManager, id)
                } catch (_: Throwable) {}
            }

            val minimalIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DevPulseMinimalWidgetProvider::class.java)
            )
            for (id in minimalIds) {
                try {
                    DevPulseMinimalWidgetProvider.updateWidget(context, appWidgetManager, id)
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    fun startWidgetUpdateLoop(context: Context) {
        managerScope.launch {
            if (hasActiveWidgets(context)) {
                updateAllWidgets(context)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun stopWidgetUpdateLoop(context: Context) {
        // No-op: background foreground service is no longer required for widget updates
    }
}
