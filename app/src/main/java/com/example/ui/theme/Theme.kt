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
    primary = MusicPrimary,
    onPrimary = Color.White,
    primaryContainer = MusicAccent,
    onPrimaryContainer = Color.White,
    secondary = MusicSecondary,
    onSecondary = Color.White,
    tertiary = MusicTertiary,
    onTertiary = Color.Black,
    background = MusicBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = MusicSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MusicCard,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = StealthDanger
)

private val LightColorScheme = darkColorScheme(
    primary = MusicPrimary,
    onPrimary = Color.White,
    background = MusicBackground,
    onBackground = Color.White,
    surface = MusicSurface,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
