package com.hamraj37.devpulse.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext



private val RedDarkColorScheme = darkColorScheme(
    primary = RedPrimaryDark,
    onPrimary = RedOnPrimaryDark,
    primaryContainer = RedPrimaryContainerDark,
    onPrimaryContainer = RedOnPrimaryContainerDark,
    secondary = RedSecondaryDark,
    onSecondary = Color(0xFF442926),
    secondaryContainer = RedSecondaryContainerDark,
    onSecondaryContainer = RedOnSecondaryContainerDark,
    tertiary = RedTertiaryDark,
    background = RedBackgroundDark,
    surface = RedSurfaceDark,
    surfaceContainer = RedSurfaceVariantDark,
    surfaceContainerLow = RedSurfaceVariantDark,
    surfaceContainerHigh = RedBannerDark,
    surfaceContainerHighest = RedChipBgDark,
    surfaceVariant = RedSurfaceVariantDark,
    onSurface = Color(0xFFF0DFDC),
    onSurfaceVariant = Color(0xFFD8C2BF)
)

private val RedLightColorScheme = lightColorScheme(
    primary = RedPrimaryLight,
    onPrimary = RedOnPrimaryLight,
    primaryContainer = RedPrimaryContainerLight,
    onPrimaryContainer = RedOnPrimaryContainerLight,
    secondary = RedSecondaryLight,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = RedSecondaryContainerLight,
    onSecondaryContainer = RedOnSecondaryContainerLight,
    tertiary = RedTertiaryLight,
    background = RedBackgroundLight,
    surface = RedSurfaceLight,
    surfaceContainer = RedSurfaceCardLight,
    surfaceContainerLow = RedSurfaceCardLight,
    surfaceContainerHigh = RedSurfaceVariantLight,
    surfaceContainerHighest = Color(0xFFF0DEDC),
    surfaceVariant = RedSurfaceVariantLight,
    onSurface = Color(0xFF231918),
    onSurfaceVariant = Color(0xFF534341)
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = BlueOnPrimaryDark,
    primaryContainer = BluePrimaryContainerDark,
    onPrimaryContainer = BlueOnPrimaryContainerDark,
    secondary = BlueSecondaryDark,
    onSecondary = Color(0xFF253140),
    secondaryContainer = BlueSecondaryContainerDark,
    onSecondaryContainer = BlueOnSecondaryContainerDark,
    tertiary = BlueTertiaryDark,
    background = BlueBackgroundDark,
    surface = BlueSurfaceDark,
    surfaceContainer = BlueSurfaceVariantDark,
    surfaceContainerLow = BlueSurfaceVariantDark,
    surfaceContainerHigh = BlueBannerDark,
    surfaceContainerHighest = BlueChipBgDark,
    surfaceVariant = BlueSurfaceVariantDark,
    onSurface = Color(0xFFE2E2E9),
    onSurfaceVariant = Color(0xFFC3C6CF)
)

private val BlueLightColorScheme = lightColorScheme(
    primary = BluePrimaryLight,
    onPrimary = BlueOnPrimaryLight,
    primaryContainer = BluePrimaryContainerLight,
    onPrimaryContainer = BlueOnPrimaryContainerLight,
    secondary = BlueSecondaryLight,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = BlueSecondaryContainerLight,
    onSecondaryContainer = BlueOnSecondaryContainerLight,
    tertiary = BlueTertiaryLight,
    background = BlueBackgroundLight,
    surface = BlueSurfaceLight,
    surfaceContainer = BlueSurfaceCardLight,
    surfaceContainerLow = BlueSurfaceCardLight,
    surfaceContainerHigh = BlueSurfaceVariantLight,
    surfaceContainerHighest = Color(0xFFDCE5F4),
    surfaceVariant = BlueSurfaceVariantLight,
    onSurface = Color(0xFF191C20),
    onSurfaceVariant = Color(0xFF43474E)
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = GreenPrimaryContainerDark,
    onPrimaryContainer = GreenOnPrimaryContainerDark,
    secondary = GreenSecondaryDark,
    onSecondary = Color(0xFF1F3529),
    secondaryContainer = GreenSecondaryContainerDark,
    onSecondaryContainer = GreenOnSecondaryContainerDark,
    tertiary = GreenTertiaryDark,
    background = GreenBackgroundDark,
    surface = GreenSurfaceDark,
    surfaceContainer = GreenSurfaceVariantDark,
    surfaceContainerLow = GreenSurfaceVariantDark,
    surfaceContainerHigh = GreenBannerDark,
    surfaceContainerHighest = GreenChipBgDark,
    surfaceVariant = GreenSurfaceVariantDark,
    onSurface = Color(0xFFE0E3DE),
    onSurfaceVariant = Color(0xFFC1C9C2)
)

private val GreenLightColorScheme = lightColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = GreenOnPrimaryLight,
    primaryContainer = GreenPrimaryContainerLight,
    onPrimaryContainer = GreenOnPrimaryContainerLight,
    secondary = GreenSecondaryLight,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = GreenSecondaryContainerLight,
    onSecondaryContainer = GreenOnSecondaryContainerLight,
    tertiary = GreenTertiaryLight,
    background = GreenBackgroundLight,
    surface = GreenSurfaceLight,
    surfaceContainer = GreenSurfaceCardLight,
    surfaceContainerLow = GreenSurfaceCardLight,
    surfaceContainerHigh = GreenSurfaceVariantLight,
    surfaceContainerHighest = Color(0xFFD7E8DC),
    surfaceVariant = GreenSurfaceVariantLight,
    onSurface = Color(0xFF171D19),
    onSurfaceVariant = Color(0xFF414943)
)

private val YellowDarkColorScheme = darkColorScheme(
    primary = YellowPrimaryDark,
    onPrimary = YellowOnPrimaryDark,
    primaryContainer = YellowPrimaryContainerDark,
    onPrimaryContainer = YellowOnPrimaryContainerDark,
    secondary = YellowSecondaryDark,
    onSecondary = Color(0xFF382F15),
    secondaryContainer = YellowSecondaryContainerDark,
    onSecondaryContainer = YellowOnSecondaryContainerDark,
    tertiary = YellowTertiaryDark,
    background = YellowBackgroundDark,
    surface = YellowSurfaceDark,
    surfaceContainer = YellowSurfaceVariantDark,
    surfaceContainerLow = YellowSurfaceVariantDark,
    surfaceContainerHigh = YellowBannerDark,
    surfaceContainerHighest = YellowChipBgDark,
    surfaceVariant = YellowSurfaceVariantDark,
    onSurface = Color(0xFFE9E2D5),
    onSurfaceVariant = Color(0xFFCEC6B4)
)

private val YellowLightColorScheme = lightColorScheme(
    primary = YellowPrimaryLight,
    onPrimary = YellowOnPrimaryLight,
    primaryContainer = YellowPrimaryContainerLight,
    onPrimaryContainer = YellowOnPrimaryContainerLight,
    secondary = YellowSecondaryLight,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = YellowSecondaryContainerLight,
    onSecondaryContainer = YellowOnSecondaryContainerLight,
    tertiary = YellowTertiaryLight,
    background = YellowBackgroundLight,
    surface = YellowSurfaceLight,
    surfaceContainer = YellowSurfaceCardLight,
    surfaceContainerLow = YellowSurfaceCardLight,
    surfaceContainerHigh = YellowSurfaceVariantLight,
    surfaceContainerHighest = Color(0xFFEAE2CD),
    surfaceVariant = YellowSurfaceVariantLight,
    onSurface = Color(0xFF1E1B13),
    onSurfaceVariant = Color(0xFF4B4639)
)

@Composable
fun DevPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isMonetEnabled: Boolean = false,
    themePalette: String = "Red",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        isMonetEnabled && (themePalette == "Monet" || themePalette == "Dynamic" || themePalette == "Monet (Dynamic)") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themePalette == "Red" -> if (darkTheme) RedDarkColorScheme else RedLightColorScheme
        themePalette == "Blue" -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        themePalette == "Green" -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        themePalette == "Yellow" -> if (darkTheme) YellowDarkColorScheme else YellowLightColorScheme
        else -> if (darkTheme) RedDarkColorScheme else RedLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
