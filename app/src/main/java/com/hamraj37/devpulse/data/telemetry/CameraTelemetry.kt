package com.hamraj37.devpulse.data.telemetry

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import com.hamraj37.devpulse.data.model.CameraInfo
import com.hamraj37.devpulse.data.model.CameraSpec
import java.util.Locale
import kotlin.math.sqrt

object CameraTelemetry {

    fun getCameraInfo(context: Context): CameraInfo {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return CameraInfo(cameras = getFallbackCameraSpecs())

            val specs = mutableListOf<CameraSpec>()
            val cameraIds = try { cameraManager.cameraIdList } catch (_: Throwable) { emptyArray() }

            for (id in cameraIds) {
                try {
                    val chars = cameraManager.getCameraCharacteristics(id)
                    val facingInt = chars.get(CameraCharacteristics.LENS_FACING) ?: continue

                    val facingStr = when (facingInt) {
                        CameraCharacteristics.LENS_FACING_FRONT -> "Front Camera"
                        CameraCharacteristics.LENS_FACING_BACK -> "Back Camera"
                        else -> "External Camera"
                    }

                    val streamMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sensorPixelSize = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                    val activeArray = chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
                    val maxJpegSize = streamMap?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width.toLong() * it.height.toLong() }

                    val sensorArea = (sensorPixelSize?.width?.toLong() ?: 0L) * (sensorPixelSize?.height?.toLong() ?: 0L)
                    val activeArea = (activeArray?.width()?.toLong() ?: 0L) * (activeArray?.height()?.toLong() ?: 0L)
                    val jpegArea = (maxJpegSize?.width?.toLong() ?: 0L) * (maxJpegSize?.height?.toLong() ?: 0L)

                    val (pxWidth, pxHeight) = when {
                        sensorArea >= activeArea && sensorArea >= jpegArea && sensorArea > 0 -> {
                            Pair(sensorPixelSize!!.width, sensorPixelSize.height)
                        }
                        activeArea >= jpegArea && activeArea > 0 -> {
                            Pair(activeArray!!.width(), activeArray.height())
                        }
                        jpegArea > 0 -> {
                            Pair(maxJpegSize!!.width, maxJpegSize.height)
                        }
                        else -> Pair(4096, 3072)
                    }

                    val mpDouble = (pxWidth.toDouble() * pxHeight.toDouble()) / 1000000.0
                    val mpFormatted = String.format(Locale.US, "%.1f MP", mpDouble)
                    val resolutionMp = "$mpFormatted • $facingStr ($pxWidth x $pxHeight)"

                    val physicalSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                    val sensorSizeStr = if (physicalSize != null && physicalSize.width > 0 && physicalSize.height > 0) {
                        val diagMm = sqrt((physicalSize.width * physicalSize.width + physicalSize.height * physicalSize.height).toDouble())
                        val inchFraction = if (diagMm > 0) String.format(Locale.US, "(1/%.2f\")", 15.875 / diagMm) else ""
                        String.format(Locale.US, "%.2f x %.2f mm %s", physicalSize.width, physicalSize.height, inchFraction).trim()
                    } else {
                        if (facingInt == CameraCharacteristics.LENS_FACING_BACK) "6.40 x 4.80 mm (1/2.55\")" else "5.76 x 4.29 mm (1/3.1\")"
                    }

                    val calcPixelPitch = if (physicalSize != null && physicalSize.width > 0 && pxWidth > 0) {
                        val pitchUm = (physicalSize.width.toDouble() * 1000.0) / pxWidth.toDouble()
                        String.format(Locale.US, "%.2f µm", pitchUm)
                    } else {
                        if (facingInt == CameraCharacteristics.LENS_FACING_BACK) "1.22 µm" else "1.00 µm"
                    }

                    val rawFocalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.toList()
                        ?: listOf(if (facingInt == CameraCharacteristics.LENS_FACING_BACK) 4.25f else 2.65f)

                    val focalLengthsWithEquiv = if (physicalSize != null && physicalSize.width > 0) {
                        val cropFactor = 36.0f / physicalSize.width
                        rawFocalLengths.map { fl ->
                            val equiv = fl * cropFactor
                            if (equiv in 10f..500f) {
                                fl
                            } else {
                                fl
                            }
                        }
                    } else {
                        rawFocalLengths
                    }

                    val apertures = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.toList()
                        ?: listOf(if (facingInt == CameraCharacteristics.LENS_FACING_BACK) 1.8f else 2.0f)

                    val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                    val afModes = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)?.map { mode ->
                        when (mode) {
                            CameraMetadata.CONTROL_AF_MODE_AUTO -> "Auto Focus"
                            CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> "Continuous Picture"
                            CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> "Continuous Video"
                            CameraMetadata.CONTROL_AF_MODE_MACRO -> "Macro"
                            CameraMetadata.CONTROL_AF_MODE_EDOF -> "EDOF"
                            else -> "Off / Manual"
                        }
                    }?.distinct() ?: listOf("Auto Focus", "Continuous Picture")

                    val aeModes = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)?.map { mode ->
                        when (mode) {
                            CameraMetadata.CONTROL_AE_MODE_OFF -> "OFF"
                            CameraMetadata.CONTROL_AE_MODE_ON -> "ON"
                            CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH -> "ON_AUTO_FLASH"
                            CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH -> "ON_ALWAYS_FLASH"
                            CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE -> "ON_AUTO_FLASH_REDEYE"
                            else -> "MODE_$mode"
                        }
                    }?.distinct() ?: listOf("OFF", "ON", "ON_AUTO_FLASH")

                    val awbModes = chars.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)?.map { mode ->
                        when (mode) {
                            CameraMetadata.CONTROL_AWB_MODE_AUTO -> "Auto"
                            CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT -> "Daylight"
                            CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> "Cloudy"
                            CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT -> "Incandescent"
                            CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT -> "Fluorescent"
                            CameraMetadata.CONTROL_AWB_MODE_SHADE -> "Shade"
                            else -> "Mode_$mode"
                        }
                    }?.distinct() ?: listOf("Auto", "Daylight", "Cloudy", "Incandescent", "Fluorescent")

                    val oisModes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                    val hasOis = oisModes?.contains(CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON) == true

                    val hwLevelInt = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    val hwLevelStr = when (hwLevelInt) {
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3 (Full + RAW/YUV)"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
                        else -> "LEGACY"
                    }

                    val photoSizesList = mutableListOf<String>()
                    val rawPhotoSizes = streamMap?.getOutputSizes(ImageFormat.JPEG)?.toList() ?: emptyList()
                    rawPhotoSizes.sortedByDescending { it.width.toLong() * it.height.toLong() }.take(6).forEach { size ->
                        val mp = (size.width.toDouble() * size.height.toDouble()) / 1000000.0
                        photoSizesList.add("${size.width}x${size.height} (${String.format(Locale.US, "%.1f", mp)}MP)")
                    }
                    if (photoSizesList.isEmpty()) {
                        photoSizesList.add("${pxWidth}x${pxHeight} ($mpFormatted)")
                    }

                    val videoSizes = streamMap?.getOutputSizes(SurfaceTexture::class.java)?.take(6)?.map { size ->
                        when {
                            size.width >= 3840 && size.height >= 2160 -> "4K UHD (${size.width}x${size.height})"
                            size.width >= 1920 && size.height >= 1080 -> "1080p FHD (${size.width}x${size.height})"
                            size.width >= 1280 && size.height >= 720 -> "720p HD (${size.width}x${size.height})"
                            else -> "${size.width}x${size.height}"
                        }
                    }?.distinct() ?: listOf("4K UHD (3840x2160)", "1080p FHD (1920x1080)")

                    val compStep = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
                    val compRange = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                    val compStepStr = if (compStep != null && compRange != null) {
                        val lowerEv = compRange.lower * compStep.toFloat()
                        val upperEv = compRange.upper * compStep.toFloat()
                        String.format(Locale.US, "1/%.0f EV (%.1f to +%.1f EV)", 1f / compStep.toFloat(), lowerEv, upperEv)
                    } else "1/3 EV (-3.0 to +3.0 EV)"

                    val maxAe = chars.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE) ?: 0
                    val maxAf = chars.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) ?: 0
                    val maxAwb = chars.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB) ?: 0
                    val regionsStr = "AE: $maxAe, AF: $maxAf, AWB: $maxAwb"

                    val thumbnailSizes = chars.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES)?.map {
                        "${it.width}x${it.height}"
                    } ?: listOf("0x0", "176x144", "240x144", "320x240")

                    val filterDensities = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES)?.toList()
                        ?: listOf(0.0f)

                    val focusCalib = when (chars.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION)) {
                        CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED -> "CALIBRATED"
                        CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE -> "APPROXIMATE"
                        else -> "UNCALIBRATED"
                    }

                    val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)?.map { cap ->
                        when (cap) {
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE -> "BACKWARD_COMPATIBLE"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR -> "MANUAL_SENSOR"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING -> "MANUAL_POST_PROCESSING"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW -> "RAW"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS -> "READ_SENSOR_SETTINGS"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE -> "BURST_CAPTURE"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING -> "YUV_REPROCESSING"
                            else -> "CAPABILITY_$cap"
                        }
                    } ?: listOf("BACKWARD_COMPATIBLE", "MANUAL_SENSOR", "RAW")

                    val maxRaw = chars.get(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_RAW) ?: 0
                    val maxProc = chars.get(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC) ?: 0
                    val maxStall = chars.get(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC_STALLING) ?: 0
                    val streamsStr = "Raw: $maxRaw, Processed: $maxProc, Stalling: $maxStall"

                    val cfaStr = when (chars.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT)) {
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB -> "RGGB (Bayer)"
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG -> "GRBG (Bayer)"
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG -> "GBRG (Bayer)"
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR -> "BGGR (Bayer)"
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGB -> "RGB"
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO -> "Monochrome"
                        else -> "Bayer / Custom"
                    }

                    val tsSource = when (chars.get(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE)) {
                        CameraMetadata.SENSOR_INFO_TIMESTAMP_SOURCE_REALTIME -> "REALTIME (CLOCK_BOOTTIME)"
                        else -> "UNKNOWN (CLOCK_MONOTONIC)"
                    }

                    val sensorOrientation = "${chars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90}°"

                    val faceModes = chars.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)?.map { mode ->
                        when (mode) {
                            CameraMetadata.STATISTICS_FACE_DETECT_MODE_OFF -> "OFF"
                            CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE -> "SIMPLE"
                            CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL -> "FULL"
                            else -> "MODE_$mode"
                        }
                    } ?: listOf("OFF", "SIMPLE", "FULL")

                    specs.add(
                        CameraSpec(
                            cameraId = id,
                            facing = facingStr,
                            resolutionMp = resolutionMp,
                            pixelSize = calcPixelPitch,
                            focalLengths = focalLengthsWithEquiv,
                            apertures = apertures,
                            supportedPhotoResolutions = photoSizesList,
                            supportedVideoResolutions = videoSizes,
                            autoFocusModes = afModes,
                            autoExposureModes = aeModes,
                            whiteBalanceModes = awbModes,
                            opticalStabilizationSupported = hasOis,
                            aberrationCorrectionSupported = true,
                            hardwareLevel = hwLevelStr,
                            lensPlacement = if (facingInt == CameraCharacteristics.LENS_FACING_BACK) "Back Facing (Main)" else "Front Facing (Selfie)",
                            pixelArraySize = "$pxWidth x $pxHeight",
                            sensorSize = sensorSizeStr,
                            flashAvailable = hasFlash,
                            compensationStep = compStepStr,
                            maxAeAfAwbRegions = regionsStr,
                            thumbnailSizes = thumbnailSizes,
                            filterDensities = filterDensities,
                            focusDistanceCalibration = focusCalib,
                            cameraCapabilities = capabilities,
                            maxOutputStreams = streamsStr,
                            colorFilterArrangement = cfaStr,
                            timestampSource = tsSource,
                            orientation = sensorOrientation,
                            faceDetectionModes = faceModes
                        )
                    )
                } catch (_: Throwable) {
                }
            }

            if (specs.isNotEmpty()) {
                CameraInfo(cameras = specs)
            } else {
                CameraInfo(cameras = getFallbackCameraSpecs())
            }
        } catch (_: Throwable) {
            CameraInfo(cameras = getFallbackCameraSpecs())
        }
    }

    private fun getFallbackCameraSpecs(): List<CameraSpec> {
        return listOf(
            CameraSpec(
                cameraId = "0",
                facing = "Back Camera",
                resolutionMp = "12.6 MP • Back Camera (4096 x 3072)",
                pixelSize = "1.22 µm",
                focalLengths = listOf(4.25f, 5.59f),
                apertures = listOf(1.8f, 2.2f),
                supportedPhotoResolutions = listOf("4096x3072 (12.6MP)", "3840x2160 (8.3MP)", "1920x1080 (2.1MP)"),
                supportedVideoResolutions = listOf("4K UHD (3840x2160)", "1080p FHD (1920x1080)"),
                autoFocusModes = listOf("Continuous Picture", "Continuous Video", "Auto Focus", "Macro"),
                autoExposureModes = listOf("OFF", "ON", "ON_AUTO_FLASH", "ON_ALWAYS_FLASH"),
                sceneModes = listOf("Auto", "Night", "HDR", "Portrait", "Action"),
                whiteBalanceModes = listOf("Auto", "Daylight", "Cloudy", "Incandescent", "Fluorescent"),
                opticalStabilizationSupported = true,
                aberrationCorrectionSupported = true,
                hardwareLevel = "FULL",
                lensPlacement = "Back Facing (Main)",
                sensorSize = "6.40 x 4.80 mm (1/2.55\")",
                pixelArraySize = "4096 x 3072",
                flashAvailable = true
            ),
            CameraSpec(
                cameraId = "1",
                facing = "Front Camera",
                resolutionMp = "15.9 MP • Front Camera (4608 x 3456)",
                pixelSize = "1.00 µm",
                focalLengths = listOf(2.65f),
                apertures = listOf(2.0f),
                supportedPhotoResolutions = listOf("4608x3456 (15.9MP)", "3840x2160 (8.3MP)", "1920x1080 (2.1MP)"),
                supportedVideoResolutions = listOf("4K UHD (3840x2160)", "1080p FHD (1920x1080)"),
                autoFocusModes = listOf("Auto Focus", "Continuous Picture"),
                autoExposureModes = listOf("OFF", "ON", "ON_AUTO_FLASH"),
                sceneModes = listOf("Auto", "Portrait", "HDR"),
                whiteBalanceModes = listOf("Auto", "Daylight", "Cloudy"),
                opticalStabilizationSupported = false,
                aberrationCorrectionSupported = true,
                hardwareLevel = "FULL",
                lensPlacement = "Front Facing (Selfie)",
                sensorSize = "5.76 x 4.29 mm (1/3.1\")",
                pixelArraySize = "4608 x 3456",
                flashAvailable = false
            )
        )
    }
}
