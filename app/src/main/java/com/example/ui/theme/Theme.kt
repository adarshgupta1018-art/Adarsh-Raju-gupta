package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsportsColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = EsportsBlack,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = GoldMetallic,
    onSecondary = EsportsBlack,
    secondaryContainer = EsportsSurfaceElevated,
    onSecondaryContainer = TextGold,
    tertiary = GoldLight,
    onTertiary = EsportsBlack,
    background = EsportsBlack,
    onBackground = TextWhite,
    surface = EsportsSurface,
    onSurface = TextWhite,
    surfaceVariant = EsportsSurfaceVariant,
    onSurfaceVariant = TextGray,
    outline = Color(0x33FFD700),
    outlineVariant = Color(0x1AFFD700),
    error = RedRejected,
    onError = TextWhite,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = EsportsColorScheme,
        typography = Typography,
        content = content
    )
}

