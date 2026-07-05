package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = DarkNaturalOlive,
    secondary = DarkNaturalOrange,
    tertiary = DarkNaturalBannerBg,
    background = DarkNaturalBg,
    surface = Color(0xFF1C1C19),
    onPrimary = DarkNaturalBg,
    onSecondary = Color.White,
    onTertiary = DarkNaturalDarkGreenText,
    onBackground = DarkNaturalText,
    onSurface = DarkNaturalText,
    surfaceVariant = DarkNaturalHeaderBg,
    onSurfaceVariant = DarkNaturalNeutral,
    outline = DarkNaturalHeaderBorder
)

private val LightColorScheme = lightColorScheme(
    primary = NaturalOlive,
    secondary = NaturalOrange,
    tertiary = NaturalBannerBg,
    background = NaturalBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = NaturalOlive,
    onBackground = NaturalText,
    onSurface = NaturalText,
    surfaceVariant = NaturalHeaderBg,
    onSurfaceVariant = NaturalPlaceholder,
    outline = NaturalHeaderBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic system colors to preserve the exact theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
