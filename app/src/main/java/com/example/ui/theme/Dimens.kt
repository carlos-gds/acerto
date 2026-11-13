package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Espaçamentos do Liquid Glass. Valores do Figma: margem lateral 20,
 * padding interno 12, intervalo de 16 entre linhas de lista.
 */
@Immutable
data class LiquidSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    /** Margem lateral das telas. */
    val screenGutter: Dp = 20.dp,
    /** Padding interno de cards e linhas. */
    val cardPadding: Dp = 12.dp,
    /** Intervalo vertical entre itens de lista. */
    val listGap: Dp = 16.dp
)

/** Raios do Figma: 10 ícone, 12 campo/botão, 16 linha, 20 card, 24 card grande. */
@Immutable
data class LiquidRadii(
    val iconTile: Dp = 10.dp,
    val field: Dp = 12.dp,
    val row: Dp = 16.dp,
    val card: Dp = 20.dp,
    val cardLarge: Dp = 24.dp,
    val sheet: Dp = 28.dp
)

/**
 * Elevações. O Figma usa sombras suaves (0 1 8 a 12% + 0 0 2 a 10%):
 * low para linhas, medium para cards, high para elementos flutuantes.
 */
@Immutable
data class LiquidElevation(
    val none: Dp = 0.dp,
    val low: Dp = 2.dp,
    val medium: Dp = 4.dp,
    val high: Dp = 8.dp
)

/** Dimensões de componentes recorrentes no Figma. */
@Immutable
data class LiquidSizes(
    val iconButton: Dp = 46.dp,
    val iconTile: Dp = 56.dp,
    val searchField: Dp = 50.dp,
    val listRow: Dp = 80.dp,
    val bottomBar: Dp = 68.dp,
    val bottomBarItem: Dp = 56.dp,
    val fab: Dp = 68.dp,
    val icon: Dp = 20.dp,
    val iconLarge: Dp = 24.dp
)

/** Formas prontas a partir de [LiquidRadii]. */
object LiquidShapes {
    private val radii = LiquidRadii()
    val iconTile = RoundedCornerShape(radii.iconTile)
    val field = RoundedCornerShape(radii.field)
    val row = RoundedCornerShape(radii.row)
    val card = RoundedCornerShape(radii.card)
    val cardLarge = RoundedCornerShape(radii.cardLarge)
    val sheet = RoundedCornerShape(topStart = radii.sheet, topEnd = radii.sheet)
    val pill = RoundedCornerShape(percent = 50)
}

val LocalLiquidSpacing = staticCompositionLocalOf { LiquidSpacing() }
val LocalLiquidRadii = staticCompositionLocalOf { LiquidRadii() }
val LocalLiquidElevation = staticCompositionLocalOf { LiquidElevation() }
val LocalLiquidSizes = staticCompositionLocalOf { LiquidSizes() }
