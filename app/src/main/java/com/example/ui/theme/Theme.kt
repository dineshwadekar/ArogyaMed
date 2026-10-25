package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArogyaLightColorScheme = lightColorScheme(
    primary = PharmaTealPrimary,
    onPrimary = PharmaOnTeal,
    primaryContainer = PharmaTealLight,
    onPrimaryContainer = PharmaTealDark,
    secondary = PharmaTealDark,
    onSecondary = PharmaWhite,
    secondaryContainer = PharmaSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    background = PharmaWhite,
    onBackground = TextPrimary,
    surface = PharmaWhite,
    onSurface = TextPrimary,
    surfaceVariant = PharmaSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = PharmaBorder,
    outlineVariant = PharmaBorderLight,
    error = AccentRed,
    onError = PharmaWhite,
    errorContainer = AccentRedLight,
    onErrorContainer = AccentRed
)

private val ArogyaDarkColorScheme = darkColorScheme(
    primary = DarkTealPrimary,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = DarkTealContainer,
    onPrimaryContainer = DarkOnTealContainer,
    secondary = DarkTealPrimary,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderLight,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ArogyaDarkColorScheme else ArogyaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

