package com.hamraj37.devpulse.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OliveActiveBadge,
    onPrimary = OliveOnPrimary,
    primaryContainer = OliveDarkSurfaceVariant,
    onPrimaryContainer = OlivePrimaryContainer,
    secondary = OliveSecondary,
    onSecondary = OliveOnSecondary,
    secondaryContainer = OliveDarkBanner,
    onSecondaryContainer = OliveSecondaryContainer,
    tertiary = OliveTertiary,
    background = OliveDarkBackground,
    surface = OliveDarkSurface,
    surfaceContainer = OliveDarkSurfaceVariant,
    surfaceContainerLow = OliveDarkSurfaceVariant,
    surfaceContainerHigh = OliveDarkBanner,
    surfaceContainerHighest = OliveDarkChipBg,
    surfaceVariant = OliveDarkSurfaceVariant,
    onSurface = Color(0xFFE2E3D8),
    onSurfaceVariant = Color(0xFFC4C8BA)
)

private val LightColorScheme = lightColorScheme(
    primary = OlivePrimary,
    onPrimary = OliveOnPrimary,
    primaryContainer = OlivePrimaryContainer,
    onPrimaryContainer = OliveOnPrimaryContainer,
    secondary = OliveSecondary,
    onSecondary = OliveOnSecondary,
    secondaryContainer = OliveSecondaryContainer,
    onSecondaryContainer = OliveOnSecondaryContainer,
    tertiary = OliveTertiary,
    background = OliveLightBackground,
    surface = OliveLightSurface,
    surfaceContainer = OliveLightCard,
    surfaceContainerLow = OliveLightCard,
    surfaceContainerHigh = OliveLightSurfaceVariant,
    surfaceContainerHighest = OliveLightChipBg,
    surfaceVariant = OliveLightSurfaceVariant,
    onSurface = Color(0xFF1A1D16),
    onSurfaceVariant = Color(0xFF44483D)
)

private data class ThemeAccents(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color
)

private fun getCustomColorScheme(darkTheme: Boolean, themeColor: String): ColorScheme {
    val accents = when (themeColor) {
        "Purple" -> if (darkTheme) {
            ThemeAccents(Color(0xFFCE93D8), Color(0xFF4A148C), Color(0xFF4A148C), Color(0xFFF3E5F5))
        } else {
            ThemeAccents(Color(0xFF7B1FA2), Color(0xFFFFFFFF), Color(0xFFE1BEE7), Color(0xFF4A148C))
        }
        "Green" -> if (darkTheme) {
            ThemeAccents(Color(0xFF81C784), Color(0xFF1B5E20), Color(0xFF1B5E20), Color(0xFFE8F5E9))
        } else {
            ThemeAccents(Color(0xFF2E7D32), Color(0xFFFFFFFF), Color(0xFFC8E6C9), Color(0xFF1B5E20))
        }
        "Orange" -> if (darkTheme) {
            ThemeAccents(Color(0xFFFFB74D), Color(0xFFBF360C), Color(0xFFBF360C), Color(0xFFFFF3E0))
        } else {
            ThemeAccents(Color(0xFFE65100), Color(0xFFFFFFFF), Color(0xFFFFE0B2), Color(0xFFBF360C))
        }
        "Teal" -> if (darkTheme) {
            ThemeAccents(Color(0xFF4DB6AC), Color(0xFF004D40), Color(0xFF004D40), Color(0xFFE0F2F1))
        } else {
            ThemeAccents(Color(0xFF00796B), Color(0xFFFFFFFF), Color(0xFFB2DFDB), Color(0xFF004D40))
        }
        else -> if (darkTheme) { // "Blue" default
            ThemeAccents(Color(0xFF64B5F6), Color(0xFF0D47A1), Color(0xFF0D47A1), Color(0xFFE3F2FD))
        } else {
            ThemeAccents(Color(0xFF1976D2), Color(0xFFFFFFFF), Color(0xFFBBDEFB), Color(0xFF0D47A1))
        }
    }

    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    return baseScheme.copy(
        primary = accents.primary,
        onPrimary = accents.onPrimary,
        primaryContainer = accents.primaryContainer,
        onPrimaryContainer = accents.onPrimaryContainer
    )
}

@Composable
fun DevPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeColor: String = "Blue",
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> getCustomColorScheme(darkTheme, themeColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
