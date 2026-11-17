package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// LIQUID GLASS DESIGN SYSTEM TOKENS
// Referência: protótipo Figma "Liquid Color Glass UI" (tema Obsidian).
//
// Sem blur real (Android 7+ e sem biblioteca): o "vidro" é simulado com
// superfícies quase opacas já compostas sobre o fundo escuro, um brilho
// em gradiente no topo e uma borda especular. Superfícies usadas como
// fundo de diálogo precisam continuar legíveis sobre o scrim, por isso
// não são translúcidas como no Figma (lá o desfoque garante a leitura).
// ==========================================

// --- Acento "Liquid Color" (Figma: #627AF0, gradiente #506BE6 -> #6B82F5) ---
val LiquidAccent = Color(0xFF627AF0)
val LiquidAccentStrong = Color(0xFF506BE6)
val LiquidAccentEnd = Color(0xFF6B82F5)
val LiquidAccentSoft = Color(0xFFB4C0FD)
val LiquidAccentBrand = Color(0xFF8485FF)

// --- Destaque quente (Figma: botão "Clean Junk" #F42525 -> #FF7D45) ---
val LiquidDangerStart = Color(0xFFF42525)
val LiquidDangerEnd = Color(0xFFFF7D45)

// --- Liquid Dark / Obsidian ---
val LiquidDarkBackground = Color(0xFF0E0E10)
val LiquidDarkBackgroundEnd = Color(0xFF0E0E0E)
/** Brilho azulado do topo (Figma: rgba(90,115,235,0.10) sobre o fundo). */
val LiquidDarkBackgroundGlow = Color(0xFF16182A)
/** Vidro de card: branco 8% translúcido (Figma 4%–12%); o fundo obsidian aparece por trás. */
val LiquidDarkCardSurface = Color(0x14FFFFFF)
/** Vidro de campo/linha: branco 10% translúcido. */
val LiquidDarkCardSurfaceVariant = Color(0x1AFFFFFF)
val LiquidDarkBorder = Color(0x2EFFFFFF) // especular, branco 18%
val LiquidDarkBorderSubtle = Color(0x14FFFFFF) // branco 8%

val LiquidDarkTextPrimary = Color(0xFFF5F6FA)
val LiquidDarkTextSecondary = Color(0xFFB8BAC2) // branco ~70%
val LiquidDarkTextMuted = Color(0xFF8E909A) // branco ~50%, >= 4,5:1

val LiquidDarkPillDark = LiquidAccentStrong // botão principal (Figma "Create")
val LiquidDarkPillDarkText = Color(0xFFFFFFFF)
val LiquidDarkPillGlass = Color(0x1FFFFFFF) // pílula de vidro, branco 12%
val LiquidDarkPillGlassText = LiquidDarkTextPrimary
val LiquidDarkCircleBtn = Color(0x1FFFFFFF)

// --- Liquid Light (derivado: o Figma só define o tema escuro) ---
val LiquidLightBackground = Color(0xFFF2F3F8)
val LiquidLightBackgroundEnd = Color(0xFFE7EAF2)
val LiquidLightBackgroundGlow = Color(0xFFE3E7FA)
val LiquidLightCardSurface = Color(0xB3FFFFFF) // branco fosco 70%
val LiquidLightCardSurfaceVariant = Color(0x99FFFFFF) // branco fosco 60%
val LiquidLightBorder = Color(0x1F0F172A) // antes branco puro (invisível)
val LiquidLightBorderSubtle = Color(0x140F172A)

val LiquidLightTextPrimary = Color(0xFF0F172A)
val LiquidLightTextSecondary = Color(0xFF475569)
val LiquidLightTextMuted = Color(0xFF5B6576) // antes #94A3B8 (abaixo de 4,5:1)

val LiquidLightPillDark = Color(0xFF4F67E0)
val LiquidLightPillDarkText = Color(0xFFFFFFFF)
val LiquidLightPillGlass = Color(0xBFFFFFFF)
val LiquidLightPillGlassText = LiquidLightTextPrimary
val LiquidLightCircleBtn = Color(0xCCFFFFFF)

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
    val eyeActive: Color,
    // --- Tokens Liquid Glass (Figma) ---
    /** Acento para ícones, links e destaques sobre o fundo. */
    val accent: Color,
    /** Gradiente do botão principal (Figma "Create"). */
    val accentGradient: Brush,
    /** Gradiente do card de destaque (Figma "Available Space"). */
    val heroGradient: Brush,
    /** Texto sobre o card de destaque. */
    val heroText: Color,
    /** Borda especular: mais clara no canto superior esquerdo. */
    val specularBorder: Brush,
    /** Brilho interno do vidro, do topo para baixo. */
    val glassHighlight: Brush,
    /** Gradiente quente de alerta (Figma "Clean Junk"). */
    val dangerGradient: Brush,
    /** Fade inferior atrás de barras flutuantes. */
    val bottomFade: Brush,
    val sheetSurface: Color,
    val scrim: Color,
    val shadowAmbient: Color,
    val shadowSpot: Color
)

// ==========================================
// PALETA ACERTO (2026-11): Obsidian + Ice Grey + Racing Red como assinatura.
// Seguindo o Apple HIG e o Taste Skill: superfícies sólidas, sem vidro,
// sem degradê e sem sombra em repouso. O vermelho é só marca (botão
// principal, aba ativa, ponto do nome); pendente é âmbar e pago é verde,
// para o vermelho nunca parecer dívida.
// ==========================================
val AcertoObsidian = Color(0xFF111217)
val AcertoIce = Color(0xFFEEF1F4)
val AcertoIceFill = Color(0xFFE3E7EC)
/** Vermelho de marca no claro: escurecido para texto branco passar AA (5,2:1). */
val AcertoRedLight = Color(0xFFC8152B)
/** Vermelho de marca no escuro: botão com texto branco ainda AA (4,7:1). */
val AcertoRedDark = Color(0xFFE0243A)
/** Vermelho para texto/ícone sobre o fundo escuro (4,8:1). */
val AcertoRedOnDark = Color(0xFFF0354B)
val AcertoDarkSurface = Color(0xFF1B1C22)
val AcertoDarkFill = Color(0xFF26272E)

private fun solid(color: Color) = Brush.verticalGradient(listOf(color, color))
private val NoBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))

val LiquidLightTokens = LiquidGlassColors(
    isDark = false,
    background = AcertoIce,
    backgroundGradient = solid(AcertoIce),
    headerSurface = Color.White,
    cardSurface = Color.White,
    cardSurfaceVariant = AcertoIceFill,
    cardBorder = Color(0x1A111217),
    cardBorderSubtle = Color(0x12111217),
    textPrimary = AcertoObsidian,
    textSecondary = Color(0xFF565C66),
    textMuted = Color(0xFF5E6470),
    pillPrimaryBg = AcertoRedLight,
    pillPrimaryText = Color.White,
    pillSecondaryBg = AcertoIceFill,
    pillSecondaryText = AcertoObsidian,
    circleButtonBg = AcertoIceFill,
    circleButtonBorder = Color(0x00000000),
    accentPaid = Color(0xFF1D7A45),
    accentPaidContainer = Color(0xFFDDF1E5),
    accentPending = Color(0xFF9A5B00),
    accentPendingContainer = Color(0xFFFBEBD0),
    eyeActive = Color(0xFF565C66),
    accent = AcertoObsidian,
    accentGradient = solid(AcertoRedLight),
    heroGradient = solid(Color.White),
    heroText = AcertoObsidian,
    specularBorder = solid(Color(0x12111217)),
    glassHighlight = NoBrush,
    dangerGradient = solid(AcertoRedLight),
    bottomFade = NoBrush,
    sheetSurface = Color.White,
    scrim = Color(0x66111217),
    shadowAmbient = Color.Transparent,
    shadowSpot = Color.Transparent
)

val LiquidDarkTokens = LiquidGlassColors(
    isDark = true,
    background = AcertoObsidian,
    backgroundGradient = solid(AcertoObsidian),
    headerSurface = AcertoDarkSurface,
    cardSurface = AcertoDarkSurface,
    cardSurfaceVariant = AcertoDarkFill,
    cardBorder = Color(0x1AEEF1F4),
    cardBorderSubtle = Color(0x12EEF1F4),
    textPrimary = AcertoIce,
    textSecondary = Color(0xFF9BA1AB),
    textMuted = Color(0xFF8B919B),
    pillPrimaryBg = AcertoRedDark,
    pillPrimaryText = Color.White,
    pillSecondaryBg = AcertoDarkFill,
    pillSecondaryText = AcertoIce,
    circleButtonBg = AcertoDarkFill,
    circleButtonBorder = Color(0x00000000),
    accentPaid = Color(0xFF4CC985),
    accentPaidContainer = Color(0x264CC985),
    accentPending = Color(0xFFF2B13C),
    accentPendingContainer = Color(0x26F2B13C),
    eyeActive = Color(0xFF9BA1AB),
    accent = AcertoIce,
    accentGradient = solid(AcertoRedDark),
    heroGradient = solid(AcertoDarkSurface),
    heroText = AcertoIce,
    specularBorder = solid(Color(0x12EEF1F4)),
    glassHighlight = NoBrush,
    dangerGradient = solid(AcertoRedDark),
    bottomFade = NoBrush,
    sheetSurface = AcertoDarkSurface,
    scrim = Color(0x99000000),
    shadowAmbient = Color.Transparent,
    shadowSpot = Color.Transparent
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
