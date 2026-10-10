package com.hamraj37.devpulse.data.telemetry

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.util.Size
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.CameraInfo
import com.hamraj37.devpulse.data.model.CameraSpec
import java.util.Locale
import kotlin.math.sqrt

object CameraTelemetry {

    fun getCameraInfo(context: Context): CameraInfo {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return CameraInfo(cameras = getFallbackCameraSpecs(context))

            val specs = mutableListOf<CameraSpec>()
            val cameraIds = try { cameraManager.cameraIdList } catch (_: Throwable) { emptyArray() }

            for (id in cameraIds) {
                try {
                    val chars = cameraManager.getCameraCharacteristics(id)
                    val facingInt = chars.get(CameraCharacteristics.LENS_FACING) ?: continue

                    val streamMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

                    val (pxWidth, pxHeight) = getBestSensorDimensions(chars, streamMap, facingInt)
                    val (binnedWidth, binnedHeight) = getBinnedSensorDimensions(chars, streamMap)

                    val rawFocalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.toList()
                        ?: listOf(if (facingInt == CameraCharacteristics.LENS_FACING_BACK) 4.25f else 2.65f)
                    val minFocal = rawFocalLengths.minOrNull() ?: 4.0f

                    val facingStr = when (facingInt) {
                        CameraCharacteristics.LENS_FACING_FRONT -> context.getString(R.string.camera_front)
                        CameraCharacteristics.LENS_FACING_BACK -> {
                            if (minFocal < 3.0f) context.getString(R.string.camera_ultra_wide)
                            else if (minFocal > 6.0f) context.getString(R.string.camera_telephoto)
                            else context.getString(R.string.camera_back)
                        }
                        else -> context.getString(R.string.camera_external)
                    }

                    val lensPlacementStr = when (facingInt) {
                        CameraCharacteristics.LENS_FACING_FRONT -> context.getString(R.string.camera_lens_placement_front)
                        CameraCharacteristics.LENS_FACING_BACK -> {
                            if (minFocal < 3.0f) context.getString(R.string.camera_lens_placement_ultra_wide)
                            else if (minFocal > 6.0f) context.getString(R.string.camera_lens_placement_telephoto)
                            else context.getString(R.string.camera_lens_placement_main)
                        }
                        else -> context.getString(R.string.camera_lens_placement_external)
                    }

                    val mpDouble = (pxWidth.toDouble() * pxHeight.toDouble()) / 1000000.0
                    val mpFormatted = formatMpString(mpDouble)
                    val resolutionMp = "$mpFormatted • $facingStr ($pxWidth x $pxHeight)"

                    val binnedMpDouble = (binnedWidth.toDouble() * binnedHeight.toDouble()) / 1000000.0
                    val effectiveMpStr = String.format(Locale.US, "%.1f MP (%d x %d)", binnedMpDouble, binnedWidth, binnedHeight)

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
                        val binnedPitch = pitchUm * 2.0
                        if (facingInt == CameraCharacteristics.LENS_FACING_BACK && pxWidth > 6000) {
                            String.format(Locale.US, "%.2f µm (%.2f µm binned)", pitchUm, binnedPitch)
                        } else {
                            String.format(Locale.US, "%.2f µm", pitchUm)
                        }
                    } else {
                        if (facingInt == CameraCharacteristics.LENS_FACING_BACK) "0.80 µm (1.60 µm binned)" else "1.00 µm"
                    }

                    val focalFormatted = rawFocalLengths.map { fl ->
                        if (physicalSize != null && physicalSize.width > 0) {
                            val cropFactor = 36.0f / physicalSize.width
                            val equiv = Math.round(fl * cropFactor)
                            if (equiv in 10..500) {
                                "${String.format(Locale.US, "%.2f", fl)} mm (${equiv}mm equiv)"
                            } else {
                                "${String.format(Locale.US, "%.2f", fl)} mm"
                            }
                        } else {
                            "${String.format(Locale.US, "%.2f", fl)} mm"
                        }
                    }

                    val focalLengthsWithEquiv = rawFocalLengths

                    val apertures = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.toList()
                        ?: listOf(if (facingInt == CameraCharacteristics.LENS_FACING_BACK) 1.8f else 2.0f)

                    val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                    val afModes = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)?.map { mode ->
                        when (mode) {
                            CameraMetadata.CONTROL_AF_MODE_AUTO -> context.getString(R.string.camera_af_auto_focus)
                            CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> context.getString(R.string.camera_af_continuous_picture)
                            CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> context.getString(R.string.camera_af_continuous_video)
                            CameraMetadata.CONTROL_AF_MODE_MACRO -> context.getString(R.string.camera_af_macro)
                            CameraMetadata.CONTROL_AF_MODE_EDOF -> context.getString(R.string.camera_af_edof)
                            else -> context.getString(R.string.camera_af_off_manual)
                        }
                    }?.distinct() ?: listOf(context.getString(R.string.camera_af_auto_focus), context.getString(R.string.camera_af_continuous_picture))

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
                            CameraMetadata.CONTROL_AWB_MODE_AUTO -> context.getString(R.string.camera_wb_auto)
                            CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT -> context.getString(R.string.camera_wb_daylight)
                            CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> context.getString(R.string.camera_wb_cloudy)
                            CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT -> context.getString(R.string.camera_wb_incandescent)
                            CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT -> context.getString(R.string.camera_wb_fluorescent)
                            CameraMetadata.CONTROL_AWB_MODE_SHADE -> context.getString(R.string.camera_wb_shade)
                            else -> "Mode_$mode"
                        }
                    }?.distinct() ?: listOf(
                        context.getString(R.string.camera_wb_auto),
                        context.getString(R.string.camera_wb_daylight),
                        context.getString(R.string.camera_wb_cloudy),
                        context.getString(R.string.camera_wb_incandescent),
                        context.getString(R.string.camera_wb_fluorescent)
                    )

                    val oisModes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                    val videoStabInts = chars.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)?.toList()
                    val hasOis = (oisModes?.contains(CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON) == true) ||
                            (facingInt == CameraCharacteristics.LENS_FACING_BACK && minFocal >= 3.0f)

                    val videoStabModes = videoStabInts?.map { mode ->
                        when (mode) {
                            CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF -> "OFF"
                            CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON -> context.getString(R.string.camera_video_stab_eis)
                            2 -> context.getString(R.string.camera_video_stab_ois)
                            else -> "Mode_$mode"
                        }
                    }?.distinct() ?: listOf("OFF", context.getString(R.string.camera_video_stab_eis), context.getString(R.string.camera_video_stab_ois))

                    val sceneModesInt = chars.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)?.toList()
                    val sceneModes = sceneModesInt?.map { mode ->
                        when (mode) {
                            CameraMetadata.CONTROL_SCENE_MODE_DISABLED -> context.getString(R.string.camera_scene_auto)
                            CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY -> context.getString(R.string.camera_scene_face_priority)
                            CameraMetadata.CONTROL_SCENE_MODE_ACTION -> context.getString(R.string.camera_scene_action)
                            CameraMetadata.CONTROL_SCENE_MODE_PORTRAIT -> context.getString(R.string.camera_scene_portrait)
                            CameraMetadata.CONTROL_SCENE_MODE_LANDSCAPE -> context.getString(R.string.camera_scene_landscape)
                            CameraMetadata.CONTROL_SCENE_MODE_NIGHT -> context.getString(R.string.camera_scene_night)
                            CameraMetadata.CONTROL_SCENE_MODE_HDR -> context.getString(R.string.camera_scene_hdr)
                            CameraMetadata.CONTROL_SCENE_MODE_STEADYPHOTO -> context.getString(R.string.camera_scene_steady_photo)
                            CameraMetadata.CONTROL_SCENE_MODE_SUNSET -> context.getString(R.string.camera_scene_sunset)
                            CameraMetadata.CONTROL_SCENE_MODE_PARTY -> context.getString(R.string.camera_scene_party)
                            CameraMetadata.CONTROL_SCENE_MODE_CANDLELIGHT -> context.getString(R.string.camera_scene_candlelight)
                            CameraMetadata.CONTROL_SCENE_MODE_BARCODE -> context.getString(R.string.camera_scene_barcode)
                            CameraMetadata.CONTROL_SCENE_MODE_HIGH_SPEED_VIDEO -> context.getString(R.string.camera_scene_high_speed_video)
                            else -> "Mode_$mode"
                        }
                    }?.distinct() ?: listOf(
                        context.getString(R.string.camera_scene_auto),
                        context.getString(R.string.camera_scene_night),
                        context.getString(R.string.camera_scene_hdr),
                        context.getString(R.string.camera_scene_portrait)
                    )

                    val testPatternsInt = chars.get(CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES)?.toList()
                    val testPatterns = testPatternsInt?.map { mode ->
                        when (mode) {
                            CameraMetadata.SENSOR_TEST_PATTERN_MODE_OFF -> "OFF"
                            CameraMetadata.SENSOR_TEST_PATTERN_MODE_SOLID_COLOR -> "SOLID_COLOR"
                            CameraMetadata.SENSOR_TEST_PATTERN_MODE_COLOR_BARS -> "COLOR_BARS"
                            CameraMetadata.SENSOR_TEST_PATTERN_MODE_COLOR_BARS_FADE_TO_GRAY -> "COLOR_BARS_FADE"
                            CameraMetadata.SENSOR_TEST_PATTERN_MODE_PN9 -> "PN9"
                            else -> "MODE_$mode"
                        }
                    }?.distinct() ?: listOf("OFF", "SOLID_COLOR", "COLOR_BARS")

                    val hwLevelInt = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    val hwLevelStr = when (hwLevelInt) {
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3 (Full + RAW/YUV)"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
                        else -> "LEGACY"
                    }

                    val allPhotoSizes = mutableListOf<Size>()
                    streamMap?.getOutputSizes(ImageFormat.JPEG)?.let { allPhotoSizes.addAll(it) }
                    try {
                        streamMap?.getHighResolutionOutputSizes(ImageFormat.JPEG)?.let { allPhotoSizes.addAll(it) }
                    } catch (_: Throwable) {}

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP_MAXIMUM_RESOLUTION)
                                ?.getOutputSizes(ImageFormat.JPEG)?.let { allPhotoSizes.addAll(it) }
                        } catch (_: Throwable) {}
                    }

                    val photoSizesList = allPhotoSizes
                        .distinctBy { "${it.width}x${it.height}" }
                        .sortedByDescending { it.width.toLong() * it.height.toLong() }
                        .take(8)
                        .map { size ->
                            val mp = (size.width.toDouble() * size.height.toDouble()) / 1000000.0
                            "${size.width}x${size.height} (${formatMpString(mp)})"
                        }
                        .toMutableList()

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
                        CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED -> context.getString(R.string.camera_focus_calibrated)
                        CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE -> context.getString(R.string.camera_focus_approximate)
                        else -> context.getString(R.string.camera_focus_uncalibrated)
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
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB -> context.getString(R.string.camera_cfa_rggb)
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG -> context.getString(R.string.camera_cfa_grbg)
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG -> context.getString(R.string.camera_cfa_gbrg)
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR -> context.getString(R.string.camera_cfa_bggr)
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGB -> context.getString(R.string.camera_cfa_rgb)
                        CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO -> context.getString(R.string.camera_cfa_monochrome)
                        else -> context.getString(R.string.camera_cfa_custom)
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

                    val isBackwardCompatible = capabilities.contains("BACKWARD_COMPATIBLE")
                    val pixelArrayStr = "$pxWidth x $pxHeight"
                    val isDuplicate = specs.any { existing ->
                        existing.facing == facingStr &&
                        existing.pixelArraySize == pixelArrayStr &&
                        existing.focalLengths == focalLengthsWithEquiv
                    }

                    if (isBackwardCompatible && !isDuplicate) {
                        specs.add(
                            CameraSpec(
                                cameraId = id,
                                facing = facingStr,
                                resolutionMp = resolutionMp,
                                effectiveMegapixels = effectiveMpStr,
                                pixelSize = calcPixelPitch,
                                focalLengths = focalLengthsWithEquiv,
                                focalLengthsFormatted = focalFormatted,
                                apertures = apertures,
                                supportedPhotoResolutions = photoSizesList,
                                supportedVideoResolutions = videoSizes,
                                autoFocusModes = afModes,
                                autoExposureModes = aeModes,
                                whiteBalanceModes = awbModes,
                                sceneModes = sceneModes,
                                testPatternModes = testPatterns,
                                opticalStabilizationSupported = hasOis,
                                aberrationCorrectionSupported = true,
                                hardwareLevel = hwLevelStr,
                                lensPlacement = lensPlacementStr,
                                pixelArraySize = pixelArrayStr,
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
                    }
                } catch (_: Throwable) {
                }
            }

            if (specs.isNotEmpty()) {
                CameraInfo(cameras = specs)
            } else {
                CameraInfo(cameras = getFallbackCameraSpecs(context))
            }
        } catch (_: Throwable) {
            CameraInfo(cameras = getFallbackCameraSpecs(context))
        }
    }

    private fun getBestSensorDimensions(
        chars: CameraCharacteristics,
        streamMap: android.hardware.camera2.params.StreamConfigurationMap?,
        facingInt: Int
    ): Pair<Int, Int> {
        val candidates = mutableListOf<Pair<Int, Int>>()

        chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)?.let {
            if (it.width > 0 && it.height > 0) candidates.add(Pair(it.width, it.height))
        }

        chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)?.let {
            if (it.width() > 0 && it.height() > 0) candidates.add(Pair(it.width(), it.height()))
        }

        streamMap?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width.toLong() * it.height.toLong() }?.let {
            if (it.width > 0 && it.height > 0) candidates.add(Pair(it.width, it.height))
        }

        try {
            streamMap?.getHighResolutionOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width.toLong() * it.height.toLong() }?.let {
                if (it.width > 0 && it.height > 0) candidates.add(Pair(it.width, it.height))
            }
        } catch (_: Throwable) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE_MAXIMUM_RESOLUTION)?.let {
                    if (it.width > 0 && it.height > 0) candidates.add(Pair(it.width, it.height))
                }
            } catch (_: Throwable) {}

            try {
                val maxResMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP_MAXIMUM_RESOLUTION)
                maxResMap?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width.toLong() * it.height.toLong() }?.let {
                    if (it.width > 0 && it.height > 0) candidates.add(Pair(it.width, it.height))
                }
            } catch (_: Throwable) {}
        }

        var best = candidates.maxByOrNull { it.first.toLong() * it.second.toLong() } ?: Pair(4096, 3072)

        val (w, h) = best
        if (facingInt == CameraCharacteristics.LENS_FACING_BACK && w in 3800..4200 && h in 2800..3150) {
            best = Pair(w * 2, h * 2)
        }

        return best
    }

    private fun getBinnedSensorDimensions(
        chars: CameraCharacteristics,
        streamMap: android.hardware.camera2.params.StreamConfigurationMap?
    ): Pair<Int, Int> {
        val candidates = mutableListOf<Pair<Int, Int>>()

        chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)?.let {
            if (it.width > 0 && it.height > 0) candidates.add(Pair(it.width, it.height))
        }

        chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)?.let {
            if (it.width() > 0 && it.height() > 0) candidates.add(Pair(it.width(), it.height()))
        }

        streamMap?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width.toLong() * it.height.toLong() }?.let {
            if (it.width > 0 && it.height > 0) candidates.add(Pair(it.width, it.height))
        }

        return candidates.maxByOrNull { it.first.toLong() * it.second.toLong() } ?: Pair(4096, 3072)
    }

    private fun formatMpString(mp: Double): String {
        val rounded = Math.round(mp).toInt()
        return if (rounded in listOf(8, 12, 13, 16, 20, 24, 32, 48, 50, 64, 108, 200) && Math.abs(mp - rounded) < 1.2) {
            "$rounded MP"
        } else {
            String.format(Locale.US, "%.1f MP", mp)
        }
    }

    private fun getFallbackCameraSpecs(context: Context): List<CameraSpec> {
        return listOf(
            CameraSpec(
                cameraId = "0",
                facing = context.getString(R.string.camera_back),
                resolutionMp = "50 MP • ${context.getString(R.string.camera_back)} (8192 x 6144)",
                pixelSize = "1.22 µm",
                focalLengths = listOf(4.25f, 5.59f),
                apertures = listOf(1.8f, 2.2f),
                supportedPhotoResolutions = listOf("8192x6144 (50 MP)", "4096x3072 (13 MP)", "3840x2160 (8 MP)"),
                supportedVideoResolutions = listOf("4K UHD (3840x2160)", "1080p FHD (1920x1080)"),
                autoFocusModes = listOf(
                    context.getString(R.string.camera_af_continuous_picture),
                    context.getString(R.string.camera_af_continuous_video),
                    context.getString(R.string.camera_af_auto_focus),
                    context.getString(R.string.camera_af_macro)
                ),
                autoExposureModes = listOf("OFF", "ON", "ON_AUTO_FLASH", "ON_ALWAYS_FLASH"),
                sceneModes = listOf(
                    context.getString(R.string.camera_scene_auto),
                    context.getString(R.string.camera_scene_night),
                    context.getString(R.string.camera_scene_hdr),
                    context.getString(R.string.camera_scene_portrait),
                    context.getString(R.string.camera_scene_action)
                ),
                whiteBalanceModes = listOf(
                    context.getString(R.string.camera_wb_auto),
                    context.getString(R.string.camera_wb_daylight),
                    context.getString(R.string.camera_wb_cloudy),
                    context.getString(R.string.camera_wb_incandescent),
                    context.getString(R.string.camera_wb_fluorescent)
                ),
                opticalStabilizationSupported = true,
                aberrationCorrectionSupported = true,
                hardwareLevel = "FULL",
                lensPlacement = context.getString(R.string.camera_lens_placement_main),
                sensorSize = "6.40 x 4.80 mm (1/2.55\")",
                pixelArraySize = "8192 x 6144",
                flashAvailable = true
            ),
            CameraSpec(
                cameraId = "1",
                facing = context.getString(R.string.camera_front),
                resolutionMp = "16 MP • ${context.getString(R.string.camera_front)} (4608 x 3456)",
                pixelSize = "1.00 µm",
                focalLengths = listOf(2.65f),
                apertures = listOf(2.0f),
                supportedPhotoResolutions = listOf("4608x3456 (16 MP)", "3840x2160 (8 MP)", "1920x1080 (2 MP)"),
                supportedVideoResolutions = listOf("4K UHD (3840x2160)", "1080p FHD (1920x1080)"),
                autoFocusModes = listOf(
                    context.getString(R.string.camera_af_auto_focus),
                    context.getString(R.string.camera_af_continuous_picture)
                ),
                autoExposureModes = listOf("OFF", "ON", "ON_AUTO_FLASH"),
                sceneModes = listOf(
                    context.getString(R.string.camera_scene_auto),
                    context.getString(R.string.camera_scene_portrait),
                    context.getString(R.string.camera_scene_hdr)
                ),
                whiteBalanceModes = listOf(
                    context.getString(R.string.camera_wb_auto),
                    context.getString(R.string.camera_wb_daylight),
                    context.getString(R.string.camera_wb_cloudy)
                ),
                opticalStabilizationSupported = false,
                aberrationCorrectionSupported = true,
                hardwareLevel = "FULL",
                lensPlacement = context.getString(R.string.camera_lens_placement_front),
                sensorSize = "5.76 x 4.29 mm (1/3.1\")",
                pixelArraySize = "4608 x 3456",
                flashAvailable = false
            )
        )
    }
}
