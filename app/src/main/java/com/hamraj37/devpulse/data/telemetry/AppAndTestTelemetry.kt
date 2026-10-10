package com.hamraj37.devpulse.data.telemetry

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.AppInfo
import com.hamraj37.devpulse.data.model.AppSpec
import com.hamraj37.devpulse.data.model.TestItem
import com.hamraj37.devpulse.data.model.TestStatus
import java.io.File

enum class AppCategoryFilter(val displayName: String) {
    ALL("All"),
    USER("User"),
    SYSTEM("System"),
    ANALYZE("Analyze");

    companion object {
        val DEFAULT = USER
    }
}

object AppAndTestTelemetry {

    @Volatile
    private var cachedAppInfo: AppInfo? = null
    @Volatile
    private var lastFetchTimeMs: Long = 0L

    fun getInstallSourceLabel(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val installerPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val info = pm.getInstallSourceInfo(packageName)
                info.installingPackageName ?: info.initiatingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }

            when (installerPackage) {
                "com.android.vending" -> "Google Play Store"
                "com.google.android.packageinstaller", "com.android.packageinstaller" -> context.getString(R.string.install_source_package_installer)
                "com.amazon.venezia" -> context.getString(R.string.install_source_amazon)
                "com.sec.android.app.samsungapps" -> context.getString(R.string.install_source_samsung)
                "com.huawei.appmarket" -> context.getString(R.string.install_source_huawei)
                "com.xiaomi.mipicks" -> context.getString(R.string.install_source_xiaomi)
                "com.oppo.market" -> context.getString(R.string.install_source_oppo)
                "com.vivo.appstore" -> context.getString(R.string.install_source_vivo)
                "adb" -> context.getString(R.string.install_source_adb)
                null -> {
                    try {
                        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
                        } else {
                            @Suppress("DEPRECATION")
                            pm.getApplicationInfo(packageName, 0)
                        }
                        if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                            context.getString(R.string.install_source_system_app)
                        } else {
                            context.getString(R.string.install_source_side_loaded_unknown)
                        }
                    } catch (_: Exception) {
                        context.getString(R.string.install_source_side_loaded_unknown)
                    }
                }
                else -> {
                    try {
                        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pm.getApplicationInfo(installerPackage, PackageManager.ApplicationInfoFlags.of(0))
                        } else {
                            @Suppress("DEPRECATION")
                            pm.getApplicationInfo(installerPackage, 0)
                        }
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (_: Exception) {
                        installerPackage
                    }
                }
            }
        } catch (_: Exception) {
            "Unknown"
        }
    }

    fun getAppInfo(context: Context): AppInfo {
        val now = System.currentTimeMillis()
        val cached = cachedAppInfo
        if (cached != null && (now - lastFetchTimeMs < 30_000L)) {
            return cached
        }

        return try {
            val pm = context.packageManager
            val appSpecs = mutableListOf<AppSpec>()

            var userCount = 0
            var systemCount = 0

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

            for (pkg in packages) {
                try {
                    val appInfo = pkg.applicationInfo
                    val isSystem = (appInfo != null) &&
                            ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0)

                    if (isSystem) systemCount++ else userCount++

                    val label = try { appInfo?.loadLabel(pm)?.toString() ?: pkg.packageName } catch (_: Throwable) { pkg.packageName }
                    val apkPath = appInfo?.sourceDir
                    val apkSize = try { if (apkPath != null) File(apkPath).length() else 0L } catch (_: Throwable) { 0L }

                    val vCode = try {
                        pkg.longVersionCode
                    } catch (_: Throwable) {
                        1L
                    }

                    val source = getInstallSourceLabel(context, pkg.packageName)

                    val targetSdk = appInfo?.targetSdkVersion ?: 34
                    val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try { appInfo?.minSdkVersion ?: 21 } catch (_: Throwable) { 21 }
                    } else 21

                    val categoryInt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try { appInfo?.category ?: ApplicationInfo.CATEGORY_UNDEFINED } catch (_: Throwable) { ApplicationInfo.CATEGORY_UNDEFINED }
                    } else ApplicationInfo.CATEGORY_UNDEFINED

                    val categoryStr = when (categoryInt) {
                        ApplicationInfo.CATEGORY_GAME -> context.getString(R.string.app_category_games)
                        ApplicationInfo.CATEGORY_AUDIO -> context.getString(R.string.app_category_audio)
                        ApplicationInfo.CATEGORY_VIDEO -> context.getString(R.string.app_category_video)
                        ApplicationInfo.CATEGORY_IMAGE -> context.getString(R.string.app_category_photography)
                        ApplicationInfo.CATEGORY_SOCIAL -> context.getString(R.string.app_category_social)
                        ApplicationInfo.CATEGORY_NEWS -> context.getString(R.string.app_category_news)
                        ApplicationInfo.CATEGORY_MAPS -> context.getString(R.string.app_category_maps)
                        ApplicationInfo.CATEGORY_PRODUCTIVITY -> context.getString(R.string.app_category_productivity)
                        ApplicationInfo.CATEGORY_ACCESSIBILITY -> context.getString(R.string.app_category_accessibility)
                        else -> if (isSystem) context.getString(R.string.app_category_system_tools) else context.getString(R.string.app_category_other)
                    }

                    appSpecs.add(
                        AppSpec(
                            appName = label,
                            packageName = pkg.packageName,
                            versionName = pkg.versionName ?: "1.0",
                            versionCode = vCode,
                            appSizeBytes = apkSize,
                            isSystemApp = isSystem,
                            installedTimeMs = pkg.firstInstallTime,
                            updatedTimeMs = pkg.lastUpdateTime,
                            installSource = source,
                            targetSdk = targetSdk,
                            minSdk = minSdk,
                            appCategory = categoryStr
                        )
                    )
                } catch (_: Throwable) {
                }
            }

            if (appSpecs.isEmpty()) {
                userCount = 18
                systemCount = 110
                appSpecs.add(
                    AppSpec(
                        appName = "DevPulse Device Info",
                        packageName = context.packageName,
                        versionName = "1.0.0",
                        versionCode = 1,
                        appSizeBytes = 12 * 1024 * 1024,
                        isSystemApp = false,
                        installedTimeMs = System.currentTimeMillis() - 86400000,
                        updatedTimeMs = System.currentTimeMillis(),
                        installSource = "Side-loaded / Local Build"
                    )
                )
            }

            val result = AppInfo(
                totalApps = userCount + systemCount,
                userAppsCount = userCount,
                systemAppsCount = systemCount,
                appsList = appSpecs.sortedBy { it.appName.lowercase() }
            )
            cachedAppInfo = result
            lastFetchTimeMs = now
            result
        } catch (_: Throwable) {
            cachedAppInfo ?: AppInfo()
        }
    }

    fun getInitialTestItems(context: Context? = null): List<TestItem> {
        fun getString(resId: Int, defaultString: String): String {
            return context?.getString(resId) ?: defaultString
        }

        return listOf(
            // Automatic Section
            TestItem(
                id = "test_automatic",
                title = getString(R.string.test_item_auto_title, "Automatic Tests"),
                category = "Automatic",
                description = getString(R.string.test_item_auto_desc, "Runs automated background diagnostic checks on radio, battery, and system modules."),
                status = TestStatus.NOT_TESTED
            ),

            // Interactive Section (14 Tests)
            TestItem(
                id = "test_display",
                title = getString(R.string.test_item_display_title, "Display Test"),
                category = "Interactive",
                description = getString(R.string.test_item_display_desc, "Color screens fill for dead pixel check (Red, Green, Blue, White, Black)."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_touch",
                title = getString(R.string.test_item_touch_title, "Multitouch Test"),
                category = "Interactive",
                description = getString(R.string.test_item_touch_desc, "Touch points visualization canvas with multi-finger input tracking."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_flashlight",
                title = getString(R.string.test_item_flashlight_title, "Flashlight Test"),
                category = "Interactive",
                description = getString(R.string.test_item_flashlight_desc, "Toggle camera flashlight LED torch light."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_speaker",
                title = getString(R.string.test_item_speaker_title, "Loudspeaker Test"),
                category = "Interactive",
                description = getString(R.string.test_item_speaker_desc, "Stereo audio tone playback check."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_earspeaker",
                title = getString(R.string.test_item_earspeaker_title, "Ear Speaker Test"),
                category = "Interactive",
                description = getString(R.string.test_item_earspeaker_desc, "Earpiece call speaker audio playback check."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_mic",
                title = getString(R.string.test_item_mic_title, "Microphone Test"),
                category = "Interactive",
                description = getString(R.string.test_item_mic_desc, "Mic level recorder check with live audio spectrum meter."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_proximity",
                title = getString(R.string.test_item_proximity_title, "Ear Proximity Test"),
                category = "Interactive",
                description = getString(R.string.test_item_proximity_desc, "Proximity sensor distance reading check."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_light",
                title = getString(R.string.test_item_light_title, "Light Sensor Test"),
                category = "Interactive",
                description = getString(R.string.test_item_light_desc, "Ambient light lux level sensor change check."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_accel",
                title = getString(R.string.test_item_accel_title, "Accelerometer Test"),
                category = "Interactive",
                description = getString(R.string.test_item_accel_desc, "Tilt sphere/box physics animation canvas."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_charging",
                title = getString(R.string.test_item_charging_title, "Charging Test"),
                category = "Interactive",
                description = getString(R.string.test_item_charging_desc, "USB & AC charger plug detection check."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_vibration",
                title = getString(R.string.test_item_vibration_title, "Vibration Test"),
                category = "Interactive",
                description = getString(R.string.test_item_vibration_desc, "Haptic feedback vibration motor pattern trigger."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_bluetooth",
                title = getString(R.string.test_item_bluetooth_title, "Bluetooth Test"),
                category = "Interactive",
                description = getString(R.string.test_item_bluetooth_desc, "Bluetooth radio scan state check."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_fingerprint",
                title = getString(R.string.test_item_fingerprint_title, "Fingerprint Test"),
                category = "Interactive",
                description = getString(R.string.test_item_fingerprint_desc, "Biometric prompt and hardware sensor check."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_gps",
                title = getString(R.string.test_item_gps_title, "GPS / Location Test"),
                category = "Interactive",
                description = getString(R.string.test_item_gps_desc, "GPS satellite & network location provider diagnostic check."),
                status = TestStatus.NOT_TESTED
            ),
            TestItem(
                id = "test_volume",
                title = getString(R.string.test_item_volume_title, "Volume Up / Down Button Test"),
                category = "Interactive",
                description = getString(R.string.test_item_volume_desc, "Key event listener test for hardware volume controls."),
                status = TestStatus.NOT_TESTED
            )
        )
    }
}
