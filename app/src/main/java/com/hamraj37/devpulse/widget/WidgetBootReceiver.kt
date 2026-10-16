package com.hamraj37.devpulse.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_USER_PRESENT -> {
                if (WidgetUpdateManager.hasActiveWidgets(context)) {
                    WidgetUpdateManager.startWidgetUpdateLoop(context)
                }
            }
        }
    }
}
