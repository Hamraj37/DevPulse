package com.hamraj37.devpulse.data.telemetry

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.BatteryInfo
import java.io.File
import java.util.Collections
import kotlin.math.abs

object BatteryTelemetry {

    private val powerHistoryList = Collections.synchronizedList(mutableListOf<Float>())

    fun getBatteryInfo(context: Context): BatteryInfo {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = try {
                context.registerReceiver(null, intentFilter)
            } catch (_: Throwable) {
                null
            }

            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 85

            val statusInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                    statusInt == BatteryManager.BATTERY_STATUS_FULL

            val statusStr = when (statusInt) {
                BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.battery_status_charging)
                BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.battery_status_discharging)
                BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.battery_status_full)
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.battery_status_not_charging)
                else -> if (isCharging) context.getString(R.string.battery_status_charging) else context.getString(R.string.battery_status_discharging)
            }

            val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            val powerSource = when {
                chargePlug == BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.battery_power_source_ac)
                chargePlug == BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.battery_power_source_usb)
                chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.battery_power_source_wireless)
                chargePlug == 4 -> context.getString(R.string.battery_power_source_dock)
                else -> if (isCharging) context.getString(R.string.battery_power_source_connected) else context.getString(R.string.battery_power_source_battery)
            }

            val healthInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
            val healthStr = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.battery_health_good)
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.battery_health_overheat)
                BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.battery_health_dead)
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.battery_health_over_voltage)
                BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.battery_health_cold)
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.battery_health_unspecified_failure)
                else -> context.getString(R.string.battery_health_good)
            }

            val tech = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() } ?: context.getString(R.string.battery_tech_li_ion)
            val voltageRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: 4150
            val voltageVolts = if (voltageRaw > 0) voltageRaw / 1000f else 4.15f

            val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: 320
            val tempCelsius = if (tempRaw != -1) tempRaw / 10f else 32.0f

            val rawCurrent = try {
                bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
            } catch (_: Throwable) {
                0
            }

            val currentAbs = abs(rawCurrent)
            val currentMa: Float = when {
                // Nanoamperes (> 10 million)
                currentAbs > 10_000_000 -> currentAbs / 1_000_000f
                // Microamperes (Standard Android HAL > 10,000 uA, e.g. 450,000 uA = 450 mA)
                currentAbs > 10_000 -> currentAbs / 1_000f
                // Milliamperes (OEM drivers like Samsung/Xiaomi returning mA between 1 and 10,000)
                currentAbs in 1..10_000 -> currentAbs.toFloat()
                else -> 0f
            }

            val powerWatts = voltageVolts * (currentMa / 1000f)

            synchronized(powerHistoryList) {
                powerHistoryList.add(powerWatts)
                if (powerHistoryList.size > 20) {
                    powerHistoryList.removeAt(0)
                }
            }

            var chargeCycles = -1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val cycleCount = batteryStatus?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
                if (cycleCount > 0) chargeCycles = cycleCount
            }

            val capacitySystemMah = getBatteryCapacity(context, batteryStatus)
            val healthPercent = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> if (chargeCycles > 0) (100 - (chargeCycles / 60)).coerceIn(80, 100) else 100
                BatteryManager.BATTERY_HEALTH_OVERHEAT, BatteryManager.BATTERY_HEALTH_COLD -> 90
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 85
                BatteryManager.BATTERY_HEALTH_DEAD, BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 50
                else -> 100
            }

            val chargeCounterRaw = try {
                bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: 0
            } catch (_: Throwable) {
                0
            }
            val chargeCounterAbs = abs(chargeCounterRaw)
            val chargedFromPropertyMah = when {
                chargeCounterAbs > 100000 -> chargeCounterAbs / 1000
                chargeCounterAbs in 100..30000 -> chargeCounterAbs
                else -> -1
            }

            val capacityEstimatedMah = if (chargedFromPropertyMah > 0 && batteryPct > 0) {
                val estimated = (chargedFromPropertyMah * 100f / batteryPct).toInt()
                if (estimated in 500..30000) estimated else (capacitySystemMah * (healthPercent / 100f)).toInt()
            } else {
                (capacitySystemMah * (healthPercent / 100f)).toInt()
            }

            val capacityChargedMah = if (chargedFromPropertyMah > 0) {
                chargedFromPropertyMah
            } else {
                (capacityEstimatedMah * (batteryPct / 100f)).toInt()
            }

            val timeToChargeFormatted = if (isCharging) {
                if (batteryPct >= 100) {
                    context.getString(R.string.battery_fully_charged)
                } else {
                    var hwRemainingMins = -1
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val timeRemainingMs = try { bm?.computeChargeTimeRemaining() ?: -1L } catch (_: Throwable) { -1L }
                        if (timeRemainingMs > 0) {
                            hwRemainingMins = (timeRemainingMs / (1000 * 60)).toInt()
                        }
                    }

                    val totalMins = if (hwRemainingMins > 0) {
                        hwRemainingMins
                    } else {
                        val remainingMah = (capacityEstimatedMah * (100 - batteryPct) / 100f).coerceAtLeast(100f)
                        val effectiveMa = if (currentMa in 100f..10000f) currentMa else 1500f
                        ((remainingMah / effectiveMa) * 60f).toInt().coerceIn(5, 600)
                    }

                    val hours = totalMins / 60
                    val mins = totalMins % 60
                    if (hours > 0 && mins > 0) {
                        context.getString(R.string.battery_time_until_full_hours_mins, hours, mins)
                    } else if (hours > 0) {
                        context.getString(R.string.battery_time_until_full_hours_mins, hours, 0)
                    } else {
                        context.getString(R.string.battery_time_until_full_mins, mins.coerceAtLeast(1))
                    }
                }
            } else {
                if (batteryPct <= 0) {
                    context.getString(R.string.battery_time_remaining_mins, 0)
                } else {
                    val availableMah = (capacityEstimatedMah * batteryPct / 100f).coerceAtLeast(50f)
                    val effectiveMa = if (currentMa in 100f..5000f) currentMa else 380f
                    val totalMins = ((availableMah / effectiveMa) * 60f).toInt().coerceIn(10, 2880)

                    val hours = totalMins / 60
                    val mins = totalMins % 60
                    if (hours > 0 && mins > 0) {
                        context.getString(R.string.battery_time_remaining_hours_mins, hours, mins)
                    } else if (hours > 0) {
                        context.getString(R.string.battery_time_remaining_hours, hours)
                    } else {
                        context.getString(R.string.battery_time_remaining_mins, mins.coerceAtLeast(1))
                    }
                }
            }

            val usbStatusStr = if (isCharging) {
                context.getString(R.string.battery_usb_charging_format, powerSource)
            } else {
                context.getString(R.string.battery_status_discharging)
            }

            BatteryInfo(
                currentMa = currentMa,
                powerWatts = powerWatts,
                temperatureCelsius = tempCelsius,
                usbStatus = usbStatusStr,
                health = healthStr,
                healthPercent = healthPercent,
                levelPercent = batteryPct,
                status = statusStr,
                powerSource = powerSource,
                technology = tech,
                voltageVolts = voltageVolts,
                timeToChargeFormatted = timeToChargeFormatted,
                chargeCycles = chargeCycles,
                capacityChargedMah = capacityChargedMah,
                capacityEstimatedMah = capacityEstimatedMah,
                capacitySystemMah = capacitySystemMah,
                powerHistory = synchronized(powerHistoryList) { powerHistoryList.toList() }
            )
        } catch (_: Throwable) {
            BatteryInfo()
        }
    }

    private fun getBatteryCapacity(context: Context, batteryStatus: Intent?): Int {
        // 1. Try PowerProfile via Reflection
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfile = try {
                powerProfileClass.getConstructor(Context::class.java).newInstance(context)
            } catch (_: Throwable) {
                try {
                    powerProfileClass.getConstructor(Context::class.java, Boolean::class.javaPrimitiveType).newInstance(context, false)
                } catch (_: Throwable) {
                    null
                }
            }
            if (powerProfile != null) {
                val capacity = try {
                    powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile) as? Double
                } catch (_: Throwable) {
                    try {
                        powerProfileClass.getMethod("getAveragePower", String::class.java).invoke(powerProfile, "battery.capacity") as? Double
                    } catch (_: Throwable) {
                        null
                    }
                }
                if (capacity != null && capacity > 0) {
                    val capInt = capacity.toInt()
                    if (capInt in 500..30000) {
                        return capInt
                    }
                }
            }
        } catch (_: Throwable) {
        }

        // 2. Try System sysfs files
        val sysfsPaths = listOf(
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/battery/energy_full_design",
            "/sys/class/power_supply/bms/charge_full_design",
            "/sys/class/power_supply/battery/charge_full",
            "/sys/class/power_supply/battery/capacity_nominal"
        )
        for (path in sysfsPaths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val text = file.readText().trim()
                    val value = text.toLongOrNull()
                    if (value != null && value > 0) {
                        val mah = if (value > 100000) (value / 1000).toInt() else value.toInt()
                        if (mah in 500..30000) {
                            return mah
                        }
                    }
                }
            } catch (_: Throwable) {
            }
        }

        // 3. Fallback: Estimate from charge counter and current battery level
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val chargeCounterRaw = try {
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: 0
        } catch (_: Throwable) {
            0
        }
        val chargeCounterAbs = abs(chargeCounterRaw)
        if (chargeCounterAbs > 0) {
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level > 0 && scale > 0) {
                val pct = level / scale.toFloat()
                if (pct > 0f) {
                    val currentMah = if (chargeCounterAbs > 100000) chargeCounterAbs / 1000f else chargeCounterAbs.toFloat()
                    val calculated = currentMah / pct
                    if (calculated in 500f..30000f) {
                        return calculated.toInt()
                    }
                }
            }
        }

        return 5000
    }
}
