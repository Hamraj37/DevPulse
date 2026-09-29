package com.hamraj37.devpulse.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.hamraj37.devpulse.MainActivity
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.telemetry.BatteryTelemetry
import com.hamraj37.devpulse.data.telemetry.CpuTelemetry
import com.hamraj37.devpulse.data.telemetry.MemoryTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class FloatingMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "devpulse_floating_monitor"
        const val NOTIFICATION_ID = 9988
        const val ACTION_START = "com.hamraj37.devpulse.ACTION_START_FLOATING_MONITOR"
        const val ACTION_STOP = "com.hamraj37.devpulse.ACTION_STOP_FLOATING_MONITOR"

        const val EXTRA_CPU = "extra_cpu"
        const val EXTRA_RAM = "extra_ram"
        const val EXTRA_BATTERY = "extra_battery"
        const val EXTRA_FPS = "extra_fps"

        @Volatile
        var isServiceRunning = false
            private set

        @Volatile
        var cpuEnabled = false
            private set

        @Volatile
        var ramEnabled = false
            private set

        @Volatile
        var batteryEnabled = false
            private set

        @Volatile
        var fpsEnabled = false
            private set

        fun startService(
            context: Context,
            showCpu: Boolean,
            showRam: Boolean,
            showBattery: Boolean,
            showFps: Boolean
        ) {
            val intent = Intent(context, FloatingMonitorService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CPU, showCpu)
                putExtra(EXTRA_RAM, showRam)
                putExtra(EXTRA_BATTERY, showBattery)
                putExtra(EXTRA_FPS, showFps)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FloatingMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var windowManager: WindowManager? = null
    private var overlayView: LinearLayout? = null
    private var params: WindowManager.LayoutParams? = null

    private var fpsTextView: TextView? = null
    private var cpuTextView: TextView? = null
    private var ramTextView: TextView? = null
    private var batteryTextView: TextView? = null

    @Volatile
    private var currentFps = 60
    private var frameCount = 0
    private var lastFrameTimeNanos = 0L

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!fpsEnabled) return
            frameCount++
            if (lastFrameTimeNanos == 0L) {
                lastFrameTimeNanos = frameTimeNanos
            } else {
                val deltaMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
                if (deltaMs >= 1000) {
                    currentFps = ((frameCount * 1000L) / deltaMs).toInt().coerceIn(1, 144)
                    frameCount = 0
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
            if (fpsEnabled) {
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateMetrics()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val showCpu = intent?.getBooleanExtra(EXTRA_CPU, cpuEnabled) ?: cpuEnabled
        val showRam = intent?.getBooleanExtra(EXTRA_RAM, ramEnabled) ?: ramEnabled
        val showBattery = intent?.getBooleanExtra(EXTRA_BATTERY, batteryEnabled) ?: batteryEnabled
        val showFps = intent?.getBooleanExtra(EXTRA_FPS, fpsEnabled) ?: fpsEnabled

        val previousFpsEnabled = fpsEnabled

        cpuEnabled = showCpu
        ramEnabled = showRam
        batteryEnabled = showBattery
        fpsEnabled = showFps

        if (fpsEnabled && !previousFpsEnabled) {
            frameCount = 0
            lastFrameTimeNanos = 0L
            Choreographer.getInstance().postFrameCallback(frameCallback)
        } else if (!fpsEnabled && previousFpsEnabled) {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
        }

        if (!cpuEnabled && !ramEnabled && !batteryEnabled && !fpsEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isServiceRunning = true

        setupOverlayView()
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)

        return START_STICKY
    }

    private fun setupOverlayView() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        if (overlayView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 200
            }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val dpPaddingHorizontal = dpToPx(12)
                val dpPaddingVertical = dpToPx(8)
                setPadding(dpPaddingHorizontal, dpPaddingVertical, dpPaddingHorizontal, dpPaddingVertical)

                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(18).toFloat()
                    setColor(Color.parseColor("#EE12141C"))
                    setStroke(dpToPx(1), Color.parseColor("#4464B5F6"))
                }
            }

            // Drag handle / Title icon
            val titleText = TextView(this).apply {
                text = ""
                textSize = 12f
                setTextColor(Color.parseColor("#FF64B5F6"))
                setPadding(0, 0, dpToPx(8), 0)
            }
            container.addView(titleText)

            // FPS TextView
            fpsTextView = createMetricBadge("#E040FB").also { container.addView(it) }

            // CPU TextView
            cpuTextView = createMetricBadge("#00E5FF").also { container.addView(it) }

            // RAM TextView
            ramTextView = createMetricBadge("#00E676").also { container.addView(it) }

            // Battery TextView
            batteryTextView = createMetricBadge("#FFAB00").also { container.addView(it) }

            // Close button
            val closeBtn = TextView(this).apply {
                text = " ✕ "
                textSize = 14f
                setTextColor(Color.parseColor("#AAAAAA"))
                setPadding(dpToPx(6), 0, 0, 0)
                setOnClickListener {
                    stopSelf()
                }
            }
            container.addView(closeBtn)

            setupDragListener(container)

            overlayView = container
            try {
                windowManager?.addView(overlayView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createMetricBadge(colorHex: String): TextView {
        return TextView(this).apply {
            textSize = 11f
            setTextColor(Color.parseColor(colorHex))
            setPadding(dpToPx(4), 0, dpToPx(4), 0)
            visibility = View.GONE
        }
    }

    private fun setupDragListener(view: View) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    v.performClick()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params?.x = initialX + (event.rawX - initialTouchX).toInt()
                    params?.y = initialY + (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager?.updateViewLayout(overlayView, params)
                    } catch (_: Exception) {}
                    true
                }
                else -> false
            }
        }
    }

    private fun updateMetrics() {
        serviceScope.launch(Dispatchers.IO) {
            val cpuInfo = if (cpuEnabled) CpuTelemetry.getCpuInfo() else null
            val memInfo = if (ramEnabled) MemoryTelemetry.getMemoryInfo(applicationContext) else null
            val battInfo = if (batteryEnabled) BatteryTelemetry.getBatteryInfo(applicationContext) else null

            withContext(Dispatchers.Main) {
                if (fpsEnabled) {
                    fpsTextView?.text = "$currentFps FPS"
                    fpsTextView?.visibility = View.VISIBLE
                } else {
                    fpsTextView?.visibility = View.GONE
                }

                if (cpuEnabled) {
                    val maxFreq = cpuInfo?.coreFrequencies?.maxOfOrNull { it.currentMhz } ?: cpuInfo?.maxFrequencyMhz ?: 0
                    val totalCores = cpuInfo?.totalCores ?: 8
                    cpuTextView?.text = "CPU ${totalCores}C @ ${maxFreq}MHz"
                    cpuTextView?.visibility = View.VISIBLE
                } else {
                    cpuTextView?.visibility = View.GONE
                }

                if (ramEnabled) {
                    val ramUsedGb = String.format(Locale.US, "%.1f", (memInfo?.ramUsedBytes ?: 0) / (1024.0 * 1024.0 * 1024.0))
                    val ramTotalGb = String.format(Locale.US, "%.1f", (memInfo?.ramTotalBytes ?: 0) / (1024.0 * 1024.0 * 1024.0))
                    ramTextView?.text = "RAM ${ramUsedGb}/${ramTotalGb}GB"
                    ramTextView?.visibility = View.VISIBLE
                } else {
                    ramTextView?.visibility = View.GONE
                }

                if (batteryEnabled) {
                    val tempStr = String.format(Locale.US, "%.1f", battInfo?.temperatureCelsius ?: 30.0f)
                    val level = battInfo?.levelPercent ?: 0
                    batteryTextView?.text = "⚡ ${tempStr}°C ${level}%"
                    batteryTextView?.visibility = View.VISIBLE
                } else {
                    batteryTextView?.visibility = View.GONE
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Floating Monitor Overlay"
            val descriptionText = "Displays real-time system metrics overlay on screen"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DevPulse Floating Monitor")
            .setContentText("Live system performance overlays running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        if (fpsEnabled) {
            try {
                Choreographer.getInstance().removeFrameCallback(frameCallback)
            } catch (_: Exception) {}
        }
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (_: Exception) {}
            overlayView = null
        }
        isServiceRunning = false
        cpuEnabled = false
        ramEnabled = false
        batteryEnabled = false
        fpsEnabled = false
    }
}
