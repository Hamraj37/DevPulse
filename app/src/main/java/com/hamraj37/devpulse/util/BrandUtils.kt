package com.hamraj37.devpulse.util

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.hamraj37.devpulse.R

data class BrandInfo(
    val id: String,
    val name: String,
    @param:DrawableRes val logoResId: Int,
    val brandColor: Color,
    val customOs: String,
    val aliases: List<String>
)

object BrandUtils {
    val ALL_BRANDS: List<BrandInfo> = listOf(
        BrandInfo("google", "Google", R.drawable.ic_brand_google, Color(0xFF4285F4), "Pixel UI", listOf("google", "pixel")),
        BrandInfo("samsung", "Samsung", R.drawable.ic_brand_samsung, Color(0xFF1428A0), "One UI", listOf("samsung", "sec")),
        BrandInfo("xiaomi", "Xiaomi", R.drawable.ic_brand_xiaomi, Color(0xFFFF6900), "HyperOS / MIUI", listOf("xiaomi", "redmi", "blackshark")),
        BrandInfo("poco", "POCO", R.drawable.ic_brand_poco, Color(0xFFFFC107), "POCO HyperOS", listOf("poco")),
        BrandInfo("oneplus", "OnePlus", R.drawable.ic_brand_oneplus, Color(0xFFF30214), "OxygenOS / ColorOS", listOf("oneplus", "1plus")),
        BrandInfo("oppo", "OPPO", R.drawable.ic_brand_oppo, Color(0xFF008A00), "ColorOS", listOf("oppo", "oplus")),
        BrandInfo("vivo", "vivo", R.drawable.ic_brand_vivo, Color(0xFF0050FF), "OriginOS / Funtouch OS", listOf("vivo")),
        BrandInfo("iqoo", "iQOO", R.drawable.ic_brand_iqoo, Color(0xFFFFB800), "OriginOS / Funtouch OS", listOf("iqoo")),
        BrandInfo("realme", "realme", R.drawable.ic_brand_realme, Color(0xFFFFC800), "Realme UI", listOf("realme")),
        BrandInfo("motorola", "Motorola", R.drawable.ic_brand_motorola, Color(0xFF0088CC), "My UX / Hello UI", listOf("motorola", "moto")),
        BrandInfo("nothing", "Nothing", R.drawable.ic_brand_nothing, Color(0xFFD32F2F), "Nothing OS", listOf("nothing")),
        BrandInfo("sony", "Sony", R.drawable.ic_brand_sony, Color(0xFF1E88E5), "Xperia UI", listOf("sony", "xperia")),
        BrandInfo("huawei", "HUAWEI", R.drawable.ic_brand_huawei, Color(0xFFCF0A2C), "EMUI / HarmonyOS", listOf("huawei")),
        BrandInfo("honor", "HONOR", R.drawable.ic_brand_honor, Color(0xFF00A2E8), "MagicOS", listOf("honor")),
        BrandInfo("asus", "ASUS", R.drawable.ic_brand_asus, Color(0xFF00539B), "ZenUI / ROG UI", listOf("asus", "rog")),
        BrandInfo("lenovo", "Lenovo", R.drawable.ic_brand_lenovo, Color(0xFFE2231A), "ZUI", listOf("lenovo")),
        BrandInfo("lg", "LG", R.drawable.ic_brand_lg, Color(0xFFA50034), "LG UX", listOf("lg", "lge")),
        BrandInfo("nokia", "Nokia", R.drawable.ic_brand_nokia, Color(0xFF124191), "Pure Android", listOf("nokia", "hmd")),
        BrandInfo("zte", "ZTE", R.drawable.ic_brand_zte, Color(0xFF0066B3), "MyOS / RedMagic OS", listOf("zte", "nubia", "redmagic")),
        BrandInfo("infinix", "Infinix", R.drawable.ic_brand_infinix, Color(0xFF7CB342), "XOS", listOf("infinix")),
        BrandInfo("tecno", "TECNO", R.drawable.ic_brand_tecno, Color(0xFF00529C), "HiOS", listOf("tecno")),
        BrandInfo("apple", "Apple", R.drawable.ic_brand_apple, Color(0xFF888888), "iOS", listOf("apple")),
        BrandInfo("htc", "HTC", R.drawable.ic_brand_htc, Color(0xFF85B200), "Sense UI", listOf("htc")),
        BrandInfo("fairphone", "Fairphone", R.drawable.ic_brand_fairphone, Color(0xFF00A3E0), "Fairphone OS", listOf("fairphone")),
        BrandInfo("sharp", "SHARP", R.drawable.ic_brand_sharp, Color(0xFFE53935), "AQUOS UI", listOf("sharp", "aquos")),
        BrandInfo("razer", "Razer", R.drawable.ic_brand_razer, Color(0xFF00FF00), "Razer UI", listOf("razer")),
        BrandInfo("android", "Android", R.drawable.ic_brand_android, Color(0xFF3DDC84), "Stock Android", listOf("android", "generic", "unknown"))
    )

    val GENERIC_BRAND: BrandInfo = BrandInfo("android", "Android Device", R.drawable.ic_brand_android, Color(0xFF3DDC84), "Android", listOf("generic"))

    fun getBrandInfo(manufacturer: String?, brand: String?): BrandInfo {
        val query = "${manufacturer.orEmpty()} ${brand.orEmpty()}".lowercase().trim()
        if (query.isBlank()) return GENERIC_BRAND

        return ALL_BRANDS.firstOrNull { brandInfo ->
            brandInfo.aliases.any { alias -> query.contains(alias) }
        } ?: GENERIC_BRAND
    }
}
