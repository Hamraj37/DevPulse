package com.hamraj37.devpulse.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
    surfaceContainerLow = OliveDarkSurfaceVariant,
    surfaceContainerHigh = OliveDarkBanner
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
    surfaceContainerLow = OliveLightCard,
    surfaceContainerHigh = OliveLightSurfaceVariant
)

@Composable
fun DevPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
