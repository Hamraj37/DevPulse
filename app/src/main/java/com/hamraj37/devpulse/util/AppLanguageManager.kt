package com.hamraj37.devpulse.util

import android.content.Context

object AppLanguageManager {

    private val translations = mapOf(
        "hi" to mapOf(
            "Dashboard" to "डैशबोर्ड",
            "Device" to "डिवाइस",
            "System" to "सिस्टम",
            "CPU" to "सीपीयू",
            "Battery" to "बैटरी",
            "Memory" to "मेमोरी",
            "Network" to "नेटवर्क",
            "Connectivity" to "कनेक्टिविटी",
            "Display" to "डिस्प्ले",
            "Thermal" to "थर्मल",
            "Sensors" to "सेंसर",
            "Camera" to "कैमरा",
            "Apps" to "ऐप्स",
            "Tests" to "परीक्षण",
            "Tools" to "टूल्स",
            "Settings" to "सेटिंग्स",
            "App Settings" to "ऐप सेटिंग्स",
            "Play Integrity" to "प्ले इंटीग्रिटी",

            "Refresh Telemetry" to "टेलीमेट्री रिफ्रेश करें",
            "App Analyzer" to "ऐप विश्लेषक",
            "Wi-Fi Analyzer" to "वाई-फ़ाई विश्लेषक",
            "Permissions" to "अनुमतियां",
            "Data Usage" to "डेटा उपयोग",
            "Widgets" to "विजेट्स",
            "Export System Report" to "सिस्टम रिपोर्ट निर्यात करें",
            "About DevPulse" to "देवपल्स के बारे में",

            "App preferences & customization" to "ऐप प्राथमिकताएं और अनुकूलन",
            "Theme" to "थीम",
            "Use system colors" to "सिस्टम रंगों का उपयोग करें",
            "Match the colors from your wallpaper" to "अपने वॉलपेपर के रंगों का मिलान करें",
            "Theme color" to "थीम का रंग",
            "General" to "सामान्य",
            "App Language" to "ऐप भाषा",
            "Export Data" to "डेटा निर्यात करें",
            "Save your device information to a text file" to "अपने डिवाइस की जानकारी टेक्स्ट फ़ाइल में सहेजें",
            "Clear Data" to "डेटा साफ़ करें",
            "Clear app's data and preferences" to "ऐप का डेटा और प्राथमिकताएं साफ़ करें",
            "Support Us" to "हमारा समर्थन करें",
            "Donate" to "दान करें",
            "You can show your appreciation for my work by making a small donation" to "आप मेरे काम के लिए एक छोटा सा दान करके सराहना दिखा सकते हैं",
            "About" to "के बारे में",
            "Privacy Policy" to "गोपनीयता नीति",
            "App Version" to "ऐप का संस्करण",
            "System default" to "सिस्टम डिफ़ॉल्ट",
            "Choose App Language" to "ऐप भाषा चुनें",
            "Choose Theme" to "थीम चुनें",
            "Choose Theme Color" to "थीम का रंग चुनें",
            "Cancel" to "रद्द करें",
            "Clear" to "साफ़ करें",
            "Close" to "बंद करें"
        ),
        "es" to mapOf(
            "Dashboard" to "Panel",
            "Device" to "Dispositivo",
            "System" to "Sistema",
            "CPU" to "CPU",
            "Battery" to "Batería",
            "Memory" to "Memoria",
            "Network" to "Red",
            "Connectivity" to "Conectividad",
            "Display" to "Pantalla",
            "Thermal" to "Térmico",
            "Sensors" to "Sensores",
            "Camera" to "Cámara",
            "Apps" to "Aplicaciones",
            "Tests" to "Pruebas",
            "Tools" to "Herramientas",
            "Settings" to "Ajustes",
            "App Settings" to "Ajustes de la app",
            "Play Integrity" to "Play Integrity",

            "Refresh Telemetry" to "Actualizar telemetría",
            "App Analyzer" to "Analizador de aplicaciones",
            "Wi-Fi Analyzer" to "Analizador Wi-Fi",
            "Permissions" to "Permisos",
            "Data Usage" to "Uso de datos",
            "Widgets" to "Widgets",
            "Export System Report" to "Exportar informe del sistema",
            "About DevPulse" to "Acerca de DevPulse",

            "App preferences & customization" to "Preferencias y personalización de la aplicación",
            "Theme" to "Tema",
            "Use system colors" to "Usar colores del sistema",
            "Match the colors from your wallpaper" to "Coincide con los colores de tu fondo de pantalla",
            "Theme color" to "Color del tema",
            "General" to "General",
            "App Language" to "Idioma de la aplicación",
            "Export Data" to "Exportar datos",
            "Save your device information to a text file" to "Guarda la información de tu dispositivo en un archivo de texto",
            "Clear Data" to "Borrar datos",
            "Clear app's data and preferences" to "Borra los datos y preferencias de la aplicación",
            "Support Us" to "Apóyanos",
            "Donate" to "Donar",
            "You can show your appreciation for my work by making a small donation" to "Puedes mostrar tu agradecimiento haciendo una pequeña donación",
            "About" to "Acerca de",
            "Privacy Policy" to "Política de privacidad",
            "App Version" to "Versión de la aplicación",
            "System default" to "Predeterminado del sistema",
            "Choose App Language" to "Elegir idioma",
            "Choose Theme" to "Elegir tema",
            "Choose Theme Color" to "Elegir color del tema",
            "Cancel" to "Cancelar",
            "Clear" to "Borrar",
            "Close" to "Cerrar"
        )
    )

    fun translate(context: Context, text: String): String {
        if (text.isBlank()) return text
        val prefs = context.getSharedPreferences("devpulse_prefs", Context.MODE_PRIVATE)
        val code = prefs.getString("app_language_code", "") ?: ""
        if (code.isEmpty()) return text

        val langMap = translations[code.lowercase()] ?: return text
        val trimmed = text.trim()
        return langMap[trimmed] ?: langMap[text] ?: text
    }

    fun translateFormat(context: Context, formatText: String, vararg args: Any): String {
        val pattern = translate(context, formatText)
        return try {
            String.format(java.util.Locale.US, pattern, *args)
        } catch (_: Throwable) {
            pattern
        }
    }
}

fun String.tr(context: Context): String {
    return AppLanguageManager.translate(context, this)
}
