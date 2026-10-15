package com.hamraj37.devpulse.util

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.hamraj37.devpulse.R
import com.hamraj37.devpulse.data.model.CpuInfo

data class ProcessorBrand(
    val id: String,
    val name: String,
    @get:DrawableRes val logoResId: Int,
    val brandColor: Color,
    val manufacturer: String,
    val aliases: List<String>
)

object ProcessorUtils {

    val ALL_PROCESSOR_BRANDS: List<ProcessorBrand> = listOf(
        ProcessorBrand(
            id = "snapdragon",
            name = "Qualcomm Snapdragon",
            logoResId = R.drawable.ic_proc_snapdragon,
            brandColor = Color(0xFFE60012),
            manufacturer = "Qualcomm",
            aliases = listOf("snapdragon", "qualcomm", "qcom", "adreno", "sm8", "sm7", "sm6", "sm4", "msm", "apq")
        ),
        ProcessorBrand(
            id = "mediatek",
            name = "MediaTek Dimensity",
            logoResId = R.drawable.ic_proc_mediatek,
            brandColor = Color(0xFFFF6A00),
            manufacturer = "MediaTek",
            aliases = listOf("mediatek", "dimensity", "helio", "mtk", "mt6", "mt8", "mali-g7", "mali-g6")
        ),
        ProcessorBrand(
            id = "exynos",
            name = "Samsung Exynos",
            logoResId = R.drawable.ic_proc_exynos,
            brandColor = Color(0xFF1428A0),
            manufacturer = "Samsung",
            aliases = listOf("exynos", "samsung", "s5e", "xclipse")
        ),
        ProcessorBrand(
            id = "tensor",
            name = "Google Tensor",
            logoResId = R.drawable.ic_proc_tensor,
            brandColor = Color(0xFF4285F4),
            manufacturer = "Google",
            aliases = listOf("tensor", "gs101", "gs201", "zumapro", "ripper", "zuma")
        ),
        ProcessorBrand(
            id = "unisoc",
            name = "UNISOC",
            logoResId = R.drawable.ic_proc_unisoc,
            brandColor = Color(0xFF00A0E9),
            manufacturer = "UNISOC",
            aliases = listOf("unisoc", "spreadtrum", "sc9863", "t606", "t610", "t612", "t616", "t618", "t700")
        ),
        ProcessorBrand(
            id = "arm",
            name = "ARM Cortex",
            logoResId = R.drawable.ic_proc_arm,
            brandColor = Color(0xFF0091BD),
            manufacturer = "ARM",
            aliases = listOf("arm", "cortex", "armv8", "armv9", "mali")
        )
    )

    val GENERIC_PROCESSOR: ProcessorBrand = ProcessorBrand(
        id = "generic",
        name = "ARM Processor",
        logoResId = R.drawable.ic_proc_arm,
        brandColor = Color(0xFF0091BD),
        manufacturer = "ARM",
        aliases = listOf("generic")
    )

    fun getProcessorBrand(cpuInfo: CpuInfo): ProcessorBrand {
        val searchTarget = "${cpuInfo.processorName} ${cpuInfo.hardwareName} ${cpuInfo.gpuVendor} ${cpuInfo.gpuRenderer}".lowercase()

        return ALL_PROCESSOR_BRANDS.firstOrNull { brand ->
            brand.aliases.any { alias -> searchTarget.contains(alias) }
        } ?: GENERIC_PROCESSOR
    }
}
