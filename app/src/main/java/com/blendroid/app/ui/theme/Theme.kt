package com.blendroid.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF121318)
val DarkSurface = Color(0xFF1E2028)
val DarkSurfaceVariant = Color(0xFF282A36)
val DarkCardBorder = Color(0xFF383B4B)

val PrimaryIndigo = Color(0xFF6366F1)
val PrimaryIndigoLight = Color(0xFF818CF8)
val AccentCyan = Color(0xFF06B6D4)

val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

val StatusGreen = Color(0xFF10B981)
val StatusAmber = Color(0xFFF59E0B)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    secondary = AccentCyan,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun BlendroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
