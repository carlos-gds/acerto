package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LiquidDarkColorScheme = darkColorScheme(
    primary = LiquidAccentSoft,
    onPrimary = Color(0xFF111735),
    primaryContainer = Color(0xFF2B3368),
    onPrimaryContainer = Color(0xFFE0E5FF),
    inversePrimary = LiquidAccentStrong,
    secondary = LiquidAccentEmerald,
    onSecondary = Color.Black,
    secondaryContainer = LiquidDarkCardSurfaceVariant,
    onSecondaryContainer = LiquidDarkTextPrimary,
    tertiary = LiquidAccentAmber,
    onTertiary = Color.Black,
    background = LiquidDarkBackground,
    onBackground = LiquidDarkTextPrimary,
    surface = Color(0xFF18191E),
    onSurface = LiquidDarkTextPrimary,
    surfaceVariant = Color(0xFF1E1F25),
    onSurfaceVariant = LiquidDarkTextSecondary,
    surfaceTint = LiquidAccent,
    surfaceDim = LiquidDarkBackground,
    surfaceBright = Color(0xFF2A2B33),
    surfaceContainerLowest = Color(0xFF0B0B0D),
    surfaceContainerLow = Color(0xFF141519),
    surfaceContainer = Color(0xFF18191E),
    surfaceContainerHigh = Color(0xFF1E1F25),
    surfaceContainerHighest = Color(0xFF25262D),
    inverseSurface = LiquidDarkTextPrimary,
    inverseOnSurface = LiquidDarkBackground,
    outline = Color(0x40FFFFFF),
    outlineVariant = LiquidDarkBorderSubtle,
    scrim = Color.Black,
    error = Color(0xFFFF7A6B),
    onError = Color(0xFF3B0A06)
)

private val LiquidLightColorScheme = lightColorScheme(
    primary = LiquidLightPillDark,
    onPrimary = LiquidLightPillDarkText,
    primaryContainer = Color(0xFFE0E5FF),
    onPrimaryContainer = Color(0xFF111735),
    inversePrimary = LiquidAccentSoft,
    secondary = LiquidAccentEmerald,
    onSecondary = Color.White,
    secondaryContainer = LiquidLightCardSurfaceVariant,
    onSecondaryContainer = LiquidLightTextPrimary,
    tertiary = LiquidAccentAmber,
    onTertiary = Color.Black,
    background = LiquidLightBackground,
    onBackground = LiquidLightTextPrimary,
    surface = Color(0xFFFFFFFF),
    onSurface = LiquidLightTextPrimary,
    surfaceVariant = LiquidLightCardSurfaceVariant,
    onSurfaceVariant = LiquidLightTextSecondary,
    surfaceTint = LiquidLightPillDark,
    surfaceDim = Color(0xFFE2E5EE),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F8FC),
    surfaceContainer = Color(0xFFF2F3F8),
    surfaceContainerHigh = Color(0xFFEDEFF6),
    surfaceContainerHighest = Color(0xFFE7EAF2),
    inverseSurface = LiquidLightTextPrimary,
    inverseOnSurface = LiquidLightBackground,
    outline = Color(0x330F172A),
    outlineVariant = LiquidLightBorderSubtle,
    scrim = Color.Black,
    error = Color(0xFFDC2626),
    onError = Color.White
)

/** Formas do Material mapeadas para os raios do Figma. */
private val LiquidMaterialShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = LiquidShapes.field,
    medium = LiquidShapes.row,
    large = LiquidShapes.card,
    extraLarge = LiquidShapes.cardLarge
)

@Composable
fun QuemMeDeveTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) LiquidDarkColorScheme else LiquidLightColorScheme
    val liquidTokens = if (darkTheme) LiquidDarkTokens else LiquidLightTokens

    CompositionLocalProvider(
        LocalLiquidGlassColors provides liquidTokens,
        LocalLiquidSpacing provides LiquidSpacing(),
        LocalLiquidRadii provides LiquidRadii(),
        LocalLiquidElevation provides LiquidElevation(),
        LocalLiquidSizes provides LiquidSizes()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = LiquidMaterialShapes,
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
