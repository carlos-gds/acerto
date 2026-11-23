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
// VISUAL ACERTO "NOIR" (2026-11-22)
// Cabeçalho escuro com brilho azul-esverdeado, folhas brancas por cima,
// cartões amarelo / verde-água / azul e verde-limão no sucesso.
// Cores medidas quadro a quadro na referência de vídeo escolhida pelo usuário.
// ==========================================
object AcertoPalette {
    /** Fundo do cabeçalho escuro (sempre escuro, nos dois temas). */
    val Header = Color(0xFF1C2021)
    /** Brilho no canto superior direito do cabeçalho. */
    val HeaderGlow = Color(0xFF2E5560)
    /** Bloco cinza sobre o cabeçalho ("Recebido"). */
    val HeaderTile = Color(0xFF2E3133)
    /** Pílula clara sobre o escuro (ação principal do cabeçalho). */
    val PillLight = Color(0xFFEDEDED)
    /** Pílula escura sobre o escuro (ação secundária). */
    val PillDark = Color(0xFF2C3134)
    val TextOnHeader = Color(0xFFF5F5F5)
    val TextOnHeaderMuted = Color(0xFF8E9396)
    val Yellow = Color(0xFFFBF049)
    val Mint = Color(0xFF32FFC6)
    val Blue = Color(0xFF24A9EE)
    /** Tela de sucesso. */
    val Lime = Color(0xFF2BF831)
    val OnLime = Color(0xFF0B2A0C)
    /** Cores dos cartões empilhados, nessa ordem. */
    val Cards = listOf(Yellow, Mint, Blue, Color(0xFFFF9F43), Color(0xFFB794F6), Color(0xFFFF6B9A))
    /** Barra inferior flutuante. */
    val NavBar = Color(0xFF26292B)
    val NavSelected = Color(0xFFF2F2F2)
}

private val Ink = Color(0xFF16181A)
private val InkMuted = Color(0xFF63666B)
private val Paper = Color(0xFFFFFFFF)
private val PaperFill = Color(0xFFF4F4F5)
private val PaperFill2 = Color(0xFFEAEAEC)
private val NightSurface = Color(0xFF26292C)
private val NightFill = Color(0xFF303437)

private fun solid(color: Color) = Brush.verticalGradient(listOf(color, color))
private val NoBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))

val LiquidLightTokens = LiquidGlassColors(
    isDark = false,
    background = Paper,
    backgroundGradient = solid(Paper),
    headerSurface = Paper,
    cardSurface = PaperFill,
    cardSurfaceVariant = PaperFill2,
    cardBorder = Color(0x14000000),
    cardBorderSubtle = Color(0x0F000000),
    textPrimary = Ink,
    textSecondary = InkMuted,
    textMuted = Color(0xFF5F6267),
    pillPrimaryBg = Ink,
    pillPrimaryText = Color.White,
    pillSecondaryBg = PaperFill,
    pillSecondaryText = Ink,
    circleButtonBg = PaperFill,
    circleButtonBorder = Color(0x00000000),
    accentPaid = Color(0xFF15803D),
    accentPaidContainer = Color(0xFFDCFCE7),
    accentPending = Color(0xFFB45309),
    accentPendingContainer = Color(0xFFFEF3C7),
    eyeActive = InkMuted,
    accent = Ink,
    accentGradient = solid(Ink),
    heroGradient = solid(PaperFill),
    heroText = Ink,
    specularBorder = solid(Color(0x0F000000)),
    glassHighlight = NoBrush,
    dangerGradient = solid(Color(0xFFDC2626)),
    bottomFade = NoBrush,
    sheetSurface = Paper,
    scrim = Color(0x66000000),
    shadowAmbient = Color.Transparent,
    shadowSpot = Color.Transparent
)

val LiquidDarkTokens = LiquidGlassColors(
    isDark = true,
    background = AcertoPalette.Header,
    backgroundGradient = solid(AcertoPalette.Header),
    headerSurface = NightSurface,
    cardSurface = NightSurface,
    cardSurfaceVariant = NightFill,
    cardBorder = Color(0x1AFFFFFF),
    cardBorderSubtle = Color(0x12FFFFFF),
    textPrimary = Color(0xFFF2F2F2),
    textSecondary = Color(0xFF9A9EA1),
    textMuted = Color(0xFF9A9EA2),
    pillPrimaryBg = AcertoPalette.PillLight,
    pillPrimaryText = Ink,
    pillSecondaryBg = NightFill,
    pillSecondaryText = Color(0xFFF2F2F2),
    circleButtonBg = NightFill,
    circleButtonBorder = Color(0x00000000),
    accentPaid = Color(0xFF4ADE80),
    accentPaidContainer = Color(0x264ADE80),
    accentPending = Color(0xFFFBBF24),
    accentPendingContainer = Color(0x26FBBF24),
    eyeActive = Color(0xFF9A9EA1),
    accent = Color(0xFFF2F2F2),
    accentGradient = solid(AcertoPalette.PillLight),
    heroGradient = solid(NightSurface),
    heroText = Color(0xFFF2F2F2),
    specularBorder = solid(Color(0x12FFFFFF)),
    glassHighlight = NoBrush,
    dangerGradient = solid(Color(0xFFEF4444)),
    bottomFade = NoBrush,
    sheetSurface = NightSurface,
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
