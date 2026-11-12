package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// LIQUID GLASS DESIGN SYSTEM TOKENS
// ==========================================

// --- Liquid Light Palette (Ref. image: white/clean frosted glass fintech) ---
val LiquidLightBackground = Color(0xFFF1F3F6)
val LiquidLightBackgroundEnd = Color(0xFFE4E8EE)
val LiquidLightCardSurface = Color(0xFAFDFDFE)
val LiquidLightCardSurfaceVariant = Color(0xF0EDF1F7)
val LiquidLightBorder = Color(0xFFFFFFFF) // Crisp white specular border
val LiquidLightBorderSubtle = Color(0x1F0F172A)

val LiquidLightTextPrimary = Color(0xFF0F172A)
val LiquidLightTextSecondary = Color(0xFF475569)
val LiquidLightTextMuted = Color(0xFF94A3B8)

val LiquidLightPillDark = Color(0xFF18181B)
val LiquidLightPillDarkText = Color(0xFFFFFFFF)
val LiquidLightPillGlass = Color(0xFFFFFFFF)
val LiquidLightPillGlassText = Color(0xFF18181B)
val LiquidLightCircleBtn = Color(0xFFFFFFFF)

// --- Liquid Dark Palette (Ref. image: deep obsidian frosted glass fintech) ---
val LiquidDarkBackground = Color(0xFF0B0B0E)
val LiquidDarkBackgroundEnd = Color(0xFF14141A)
val LiquidDarkCardSurface = Color(0xE6181820)
val LiquidDarkCardSurfaceVariant = Color(0xD9242430)
val LiquidDarkBorder = Color(0x38FFFFFF) // Luminous glass specular border
val LiquidDarkBorderSubtle = Color(0x1AFFFFFF)

val LiquidDarkTextPrimary = Color(0xFFF8FAFC)
val LiquidDarkTextSecondary = Color(0xFF94A3B8)
val LiquidDarkTextMuted = Color(0xFF64748B)

val LiquidDarkPillDark = Color(0xFFFFFFFF)
val LiquidDarkPillDarkText = Color(0xFF0B0B0E)
val LiquidDarkPillGlass = Color(0x402C2C38)
val LiquidDarkPillGlassText = Color(0xFFF8FAFC)
val LiquidDarkCircleBtn = Color(0x38282834)

// --- Accents (Shared / Status) ---
val LiquidAccentEmerald = Color(0xFF10B981)
val LiquidAccentEmeraldLight = Color(0x2610B981)
val LiquidAccentAmber = Color(0xFFF59E0B)
val LiquidAccentAmberLight = Color(0x26F59E0B)
val LiquidAccentViolet = Color(0xFF8B5CF6)
val LiquidAccentBlue = Color(0xFF3B82F6)

@Immutable
data class LiquidGlassColors(
    val isDark: Boolean,
    val background: Color,
    val backgroundGradient: Brush,
    val headerSurface: Color,
    val cardSurface: Color,
    val cardSurfaceVariant: Color,
    val cardBorder: Color,
    val cardBorderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val pillPrimaryBg: Color,
    val pillPrimaryText: Color,
    val pillSecondaryBg: Color,
    val pillSecondaryText: Color,
    val circleButtonBg: Color,
    val circleButtonBorder: Color,
    val accentPaid: Color,
    val accentPaidContainer: Color,
    val accentPending: Color,
    val accentPendingContainer: Color,
    val eyeActive: Color
)

val LiquidLightTokens = LiquidGlassColors(
    isDark = false,
    background = LiquidLightBackground,
    backgroundGradient = Brush.verticalGradient(listOf(LiquidLightBackground, LiquidLightBackgroundEnd)),
    headerSurface = Color(0xF2FFFFFF),
    cardSurface = LiquidLightCardSurface,
    cardSurfaceVariant = LiquidLightCardSurfaceVariant,
    cardBorder = LiquidLightBorder,
    cardBorderSubtle = LiquidLightBorderSubtle,
    textPrimary = LiquidLightTextPrimary,
    textSecondary = LiquidLightTextSecondary,
    textMuted = LiquidLightTextMuted,
    pillPrimaryBg = LiquidLightPillDark,
    pillPrimaryText = LiquidLightPillDarkText,
    pillSecondaryBg = LiquidLightPillGlass,
    pillSecondaryText = LiquidLightPillGlassText,
    circleButtonBg = LiquidLightCircleBtn,
    circleButtonBorder = LiquidLightBorder,
    accentPaid = Color(0xFF059669),
    accentPaidContainer = Color(0xFFD1FAE5),
    accentPending = Color(0xFFD97706),
    accentPendingContainer = Color(0xFFFEF3C7),
    eyeActive = LiquidLightTextSecondary
)

val LiquidDarkTokens = LiquidGlassColors(
    isDark = true,
    background = LiquidDarkBackground,
    backgroundGradient = Brush.verticalGradient(listOf(LiquidDarkBackground, LiquidDarkBackgroundEnd)),
    headerSurface = Color(0xF2121218),
    cardSurface = LiquidDarkCardSurface,
    cardSurfaceVariant = LiquidDarkCardSurfaceVariant,
    cardBorder = LiquidDarkBorder,
    cardBorderSubtle = LiquidDarkBorderSubtle,
    textPrimary = LiquidDarkTextPrimary,
    textSecondary = LiquidDarkTextSecondary,
    textMuted = LiquidDarkTextMuted,
    pillPrimaryBg = LiquidDarkPillDark,
    pillPrimaryText = LiquidDarkPillDarkText,
    pillSecondaryBg = LiquidDarkPillGlass,
    pillSecondaryText = LiquidDarkPillGlassText,
    circleButtonBg = LiquidDarkCircleBtn,
    circleButtonBorder = LiquidDarkBorder,
    accentPaid = Color(0xFF34D399),
    accentPaidContainer = Color(0x2610B981),
    accentPending = Color(0xFFFBBF24),
    accentPendingContainer = Color(0x26F59E0B),
    eyeActive = LiquidDarkTextSecondary
)

val LocalLiquidGlassColors = staticCompositionLocalOf { LiquidDarkTokens }

// Legacy aliases for backward compatibility
val SophisticatedBackground get() = LiquidDarkBackground
val SophisticatedHeader get() = LiquidDarkCardSurface
val SophisticatedCard get() = LiquidDarkCardSurface
val SophisticatedCardVariant get() = LiquidDarkCardSurfaceVariant
val SophisticatedCardOthers get() = LiquidDarkCardSurfaceVariant
val SophisticatedAvatarBg get() = LiquidDarkCircleBtn
val SophisticatedNavBg get() = LiquidDarkBackground
val SophisticatedBorder get() = LiquidDarkBorder
val SophisticatedPrimary get() = Color(0xFFD0BCFF)
val SophisticatedOnPrimary get() = Color(0xFF381E72)
val SophisticatedPrimaryContainer get() = Color(0xFF381E72)
val SophisticatedOnPrimaryContainer get() = Color(0xFFEADDFF)
val SophisticatedSecondary get() = Color(0xFFCCC2DC)
val SophisticatedOnSecondary get() = Color(0xFF332D41)
val SophisticatedSecondaryContainer get() = Color(0xFF4A4458)
val SophisticatedOnSecondaryContainer get() = Color(0xFFE8DEF8)
val SophisticatedTextPrimary get() = LiquidDarkTextPrimary
val SophisticatedTextSecondary get() = LiquidDarkTextSecondary
val SophisticatedTextMuted get() = LiquidDarkTextMuted
val SophisticatedPaid get() = Color(0xFF81C784)
val SophisticatedPaidContainer get() = Color(0xFF1B382B)
val SophisticatedPending get() = Color(0xFFFFB74D)
val SophisticatedPendingContainer get() = Color(0xFF3E2D18)
val SophisticatedError get() = Color(0xFFF2B8B5)
val EmeraldPrimary get() = SophisticatedPrimary
val EmeraldDark get() = SophisticatedOnPrimary
val EmeraldLight get() = SophisticatedPrimary
val EmeraldContainer get() = SophisticatedPrimaryContainer
val OnEmeraldContainer get() = SophisticatedOnPrimaryContainer
val AmberPending get() = SophisticatedPending


