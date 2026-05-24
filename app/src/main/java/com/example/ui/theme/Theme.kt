package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MatchaColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = White,
    primaryContainer = MintCard,
    onPrimaryContainer = DeepGreen,
    secondary = GreenAccent,
    onSecondary = DeepGreen,
    secondaryContainer = MintLight,
    onSecondaryContainer = TextPrimary,
    background = MintLight,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = MintLight,
    onSurfaceVariant = MutedText,
    outline = BorderColor,
    error = AlertText,
    onError = White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // We force the beautiful Light Matcha theme as requested
    dynamicColor: Boolean = false, // Disable dynamic colors to maintain precise Matcha aesthetics
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MatchaColorScheme,
        typography = Typography,
        content = content
    )
}
