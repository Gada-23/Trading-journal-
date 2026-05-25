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
    primary = FintechBlue,
    onPrimary = Color.White,
    primaryContainer = FintechBlueSecondary,
    onPrimaryContainer = Color.White,
    secondary = FintechSlate,
    onSecondary = Color.Black,
    background = ObsidianBg,
    onBackground = TextSilverBase,
    surface = ObsidianSurface,
    onSurface = TextSilverBase,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSilverBase,
    error = BearishCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for premium fintech feel
    dynamicColor: Boolean = false, // Disable dynamic colors to keep design premium, bespoke, and consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        // Fallback or Light scheme (still use obsidian surface accents for design consistency)
        darkColorScheme(
            primary = FintechBlue,
            secondary = FintechSlate,
            background = Color(0xFF131722),
            surface = ObsidianSurface,
            onBackground = TextSilverBase,
            onSurface = TextSilverBase
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
