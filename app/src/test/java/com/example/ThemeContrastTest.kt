package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.LiquidDarkTokens
import com.example.ui.theme.LiquidGlassColors
import com.example.ui.theme.LiquidLightTokens
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Garante contraste WCAG mínimo dos tokens Liquid Glass nos dois temas,
 * para evitar regressões como texto branco sobre pílula branca no modo escuro.
 * Superfícies translúcidas são compostas sobre o fundo antes da medição.
 */
class ThemeContrastTest {

    private val themes = listOf("claro" to LiquidLightTokens, "escuro" to LiquidDarkTokens)

    private fun contrast(foreground: Color, background: Color): Double {
        val bg = background.compositeOver(Color.Black)
        val fg = foreground.compositeOver(bg)
        val l1 = fg.luminance().toDouble()
        val l2 = bg.luminance().toDouble()
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    private fun surfaces(t: LiquidGlassColors): List<Pair<String, Color>> = listOf(
        "background" to t.background,
        "cardSurface" to t.cardSurface.compositeOver(t.background),
        "cardSurfaceVariant" to t.cardSurfaceVariant.compositeOver(t.background),
        "headerSurface" to t.headerSurface.compositeOver(t.background),
        "sheetSurface" to t.sheetSurface.compositeOver(t.background)
    )

    private fun assertMin(min: Double, label: String, fg: Color, bg: Color) {
        val ratio = contrast(fg, bg)
        assertTrue("$label: contraste %.2f abaixo de %.1f".format(ratio, min), ratio >= min)
    }

    @Test
    fun textos_temContraste_AA_sobreTodasAsSuperficies() {
        for ((name, t) in themes) {
            for ((surfaceName, surface) in surfaces(t)) {
                assertMin(4.5, "[$name] textPrimary/$surfaceName", t.textPrimary, surface)
                assertMin(4.5, "[$name] textSecondary/$surfaceName", t.textSecondary, surface)
                assertMin(4.5, "[$name] textMuted/$surfaceName", t.textMuted, surface)
            }
        }
    }

    @Test
    fun estadosPagoEPendente_temContraste_AA_sobreCards() {
        for ((name, t) in themes) {
            val card = t.cardSurface.compositeOver(t.background)
            assertMin(4.5, "[$name] accentPaid", t.accentPaid, card)
            assertMin(4.5, "[$name] accentPending", t.accentPending, card)
        }
    }

    @Test
    fun pilulaPrincipal_textoLegivel() {
        for ((name, t) in themes) {
            assertMin(4.5, "[$name] pillPrimaryText/pillPrimaryBg", t.pillPrimaryText, t.pillPrimaryBg)
            val pill2 = t.pillSecondaryBg.compositeOver(t.background)
            assertMin(4.5, "[$name] pillSecondaryText/pillSecondaryBg", t.pillSecondaryText, pill2)
        }
    }

    @Test
    fun acento_visivelComoIcone_sobreCards() {
        for ((name, t) in themes) {
            val card = t.cardSurface.compositeOver(t.background)
            // Ícones e elementos gráficos: mínimo WCAG 3:1.
            assertMin(3.0, "[$name] accent", t.accent, card)
            assertMin(3.0, "[$name] textPrimary em circleButton", t.textPrimary, t.circleButtonBg.compositeOver(t.background))
        }
    }
}
