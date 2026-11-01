package com.xauusd.analyst.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF0A1128)
val NavyCard = Color(0xFF111B3A)
val NavyLight = Color(0xFF1B2850)
val Outline = Color(0xFF2E3F73)
val Gold = Color(0xFFF5C242)
val BuyGreen = Color(0xFF2ECC71)
val SellRed = Color(0xFFE74C3C)
val WaitGrey = Color(0xFF8E9AAF)
val TextPrimary = Color(0xFFEAF0FF)
val TextMuted = Color(0xFF9AA7C7)

private val scheme = darkColorScheme(
    primary = Gold,
    onPrimary = Navy,
    secondary = Gold,
    background = Navy,
    onBackground = TextPrimary,
    surface = NavyCard,
    onSurface = TextPrimary,
    surfaceVariant = NavyLight,
    onSurfaceVariant = TextMuted,
    error = SellRed,
    outline = Outline
)

@Composable
fun XauTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
