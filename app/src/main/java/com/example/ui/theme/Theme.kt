package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LiquidDarkColorScheme = darkColorScheme(
    primary = LiquidDarkTextPrimary,
    onPrimary = LiquidDarkBackground,
    primaryContainer = LiquidDarkCardSurfaceVariant,
    onPrimaryContainer = LiquidDarkTextPrimary,
    secondary = LiquidAccentEmerald,
    onSecondary = Color.Black,
    secondaryContainer = LiquidDarkCardSurfaceVariant,
    onSecondaryContainer = LiquidDarkTextPrimary,
    tertiary = LiquidAccentAmber,
    background = LiquidDarkBackground,
    surface = LiquidDarkCardSurface,
    surfaceVariant = LiquidDarkCardSurfaceVariant,
    onBackground = LiquidDarkTextPrimary,
    onSurface = LiquidDarkTextPrimary,
    onSurfaceVariant = LiquidDarkTextSecondary,
    outline = LiquidDarkBorder,
    outlineVariant = LiquidDarkBorderSubtle,
    error = Color(0xFFF87171)
)

private val LiquidLightColorScheme = lightColorScheme(
    primary = LiquidLightPillDark,
    onPrimary = LiquidLightPillDarkText,
    primaryContainer = LiquidLightCardSurfaceVariant,
    onPrimaryContainer = LiquidLightTextPrimary,
    secondary = LiquidAccentEmerald,
    onSecondary = Color.White,
    secondaryContainer = LiquidLightCardSurfaceVariant,
    onSecondaryContainer = LiquidLightTextPrimary,
    tertiary = LiquidAccentAmber,
    background = LiquidLightBackground,
    surface = LiquidLightCardSurface,
    surfaceVariant = LiquidLightCardSurfaceVariant,
    onBackground = LiquidLightTextPrimary,
    onSurface = LiquidLightTextPrimary,
    onSurfaceVariant = LiquidLightTextSecondary,
    outline = LiquidLightBorder,
    outlineVariant = LiquidLightBorderSubtle,
    error = Color(0xFFDC2626)
)

@Composable
fun QuemMeDeveTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) LiquidDarkColorScheme else LiquidLightColorScheme
    val liquidTokens = if (darkTheme) LiquidDarkTokens else LiquidLightTokens

    CompositionLocalProvider(LocalLiquidGlassColors provides liquidTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Backward-compatible alias for the app theme.
 */
@Deprecated(
    message = "Use QuemMeDeveTheme instead",
    replaceWith = ReplaceWith("QuemMeDeveTheme(darkTheme, content)")
)
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) = QuemMeDeveTheme(darkTheme = darkTheme, content = content)


