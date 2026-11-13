package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrbitCyanSoft,
    onPrimary = OrbitNavyDeep,
    primaryContainer = OrbitRoyalBlue,
    onPrimaryContainer = Color.White,
    secondary = OrbitRoyalBlueBright,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = OrbitEmerald,
    onTertiary = OrbitNavyDeep,
    background = OrbitNavyDeep,
    onBackground = Color(0xFFF8FAFC),
    surface = OrbitNavySurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = OrbitNavyCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = OrbitRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OrbitRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = OrbitCyanAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF083344),
    tertiary = OrbitEmerald,
    onTertiary = Color.White,
    background = OrbitLightBackground,
    onBackground = OrbitSlateText,
    surface = OrbitLightSurface,
    onSurface = OrbitSlateText,
    surfaceVariant = OrbitLightSurfaceVariant,
    onSurfaceVariant = OrbitSlateMuted,
    outline = Color(0xFFCBD5E1),
    error = OrbitRose,
    onError = Color.White
)

@Composable
fun CampusOrbitTheme(
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
