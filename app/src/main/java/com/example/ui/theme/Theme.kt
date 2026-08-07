package com.example.ui.theme

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
    primary = AppleBlue,
    onPrimary = AppleBackgroundDark,
    primaryContainer = AppleSurfaceVariantDark,
    onPrimaryContainer = AppleTextPrimaryDark,
    secondary = AppleTeal,
    onSecondary = AppleBackgroundDark,
    background = AppleBackgroundDark,
    onBackground = AppleTextPrimaryDark,
    surface = AppleSurfaceDark,
    onSurface = AppleTextPrimaryDark,
    surfaceVariant = AppleGroupedDark,
    onSurfaceVariant = AppleTextSecondaryDark,
    outline = AppleGlassBorder,
    outlineVariant = AppleGlassBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AppleBlueLight,
    onPrimary = AppleSurfaceLight,
    primaryContainer = AppleSurfaceVariantLight,
    onPrimaryContainer = AppleTextPrimaryLight,
    secondary = AppleTeal,
    onSecondary = AppleSurfaceLight,
    background = AppleBackgroundLight,
    onBackground = AppleTextPrimaryLight,
    surface = AppleSurfaceLight,
    onSurface = AppleTextPrimaryLight,
    surfaceVariant = AppleSurfaceVariantLight,
    onSurfaceVariant = AppleTextSecondaryLight,
    outline = AppleGlassBorderLight,
    outlineVariant = AppleGlassBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
