package com.hamraj37.devpulse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.hamraj37.devpulse.R

class DevPulseClockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_clock)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
