package com.blackbox.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = OnNeonCyanDark,
    primaryContainer = NeonCyanDim,
    onPrimaryContainer = OnNeonCyanDark,
    secondary = NeonMagenta,
    onSecondary = OnNeonMagentaDark,
    secondaryContainer = NeonMagentaDim,
    onSecondaryContainer = OnNeonMagentaDark,
    tertiary = ElectricViolet,
    onTertiary = OnElectricVioletDark,
    tertiaryContainer = ElectricVioletDim,
    onTertiaryContainer = OnElectricVioletDark,
    background = CyberBackgroundDark,
    onBackground = CyberOnSurfaceDark,
    surface = CyberSurfaceDark,
    onSurface = CyberOnSurfaceDark,
    surfaceVariant = CyberSurfaceVariantDark,
    onSurfaceVariant = CyberOnSurfaceVariantDark,
    error = CyberError,
    onError = OnErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = NeonCyanDim,
    onPrimary = OnNeonCyanLight,
    primaryContainer = NeonCyan,
    onPrimaryContainer = OnBrightContainerLight,
    secondary = NeonMagentaDim,
    onSecondary = OnNeonMagentaLight,
    secondaryContainer = NeonMagenta,
    onSecondaryContainer = OnBrightContainerLight,
    tertiary = ElectricVioletDim,
    onTertiary = OnElectricVioletLight,
    tertiaryContainer = ElectricViolet,
    onTertiaryContainer = OnBrightContainerLight,
    background = CyberBackgroundLight,
    onBackground = CyberOnSurfaceLight,
    surface = CyberSurfaceLight,
    onSurface = CyberOnSurfaceLight,
    surfaceVariant = CyberSurfaceVariantLight,
    onSurfaceVariant = CyberOnSurfaceVariantLight,
    error = CyberError,
    onError = OnErrorLight
)

@Composable
fun BlackboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}