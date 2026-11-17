package com.example.ui.components.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.ui.theme.LiquidAccent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidElevation
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.theme.LocalLiquidSizes
import com.example.ui.theme.LocalLiquidSpacing
import com.example.ui.theme.QuemMeDeveTheme

/*
 * Componentes básicos do Liquid Glass (protótipo Figma "Liquid Color Glass UI").
 *
 * O vidro é simulado, sem blur real e sem biblioteca, para funcionar no Android 7+:
 * 1. sombra suave (Figma: 0 1 8 a 12% + 0 0 2 a 10%);
 * 2. superfície quase opaca, já composta sobre o fundo obsidian;
 * 3. brilho interno em gradiente a partir do topo;
 * 4. borda especular em gradiente, mais clara no canto superior esquerdo.
 *
 * Nesta etapa nenhuma tela usa estes componentes ainda.
 */

/**
 * Aplica o efeito de vidro simulado a qualquer elemento.
 *
 * @param surface cor de fundo; use um token de superfície do tema.
 * @param fill pincel opcional que substitui [surface] (ex.: gradiente de destaque).
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape,
    surface: Color = LocalLiquidGlassColors.current.cardSurface,
    fill: Brush? = null,
    elevation: Dp = LocalLiquidElevation.current.medium,
    border: Brush = LocalLiquidGlassColors.current.specularBorder,
    highlight: Brush? = LocalLiquidGlassColors.current.glassHighlight,
    /** Raio do blur real do conteúdo de trás (Android 12+). null = só vidro simulado. */
    blur: Dp? = null
): Modifier {
    val liquid = LocalLiquidGlassColors.current
    var result = this
    if (elevation > 0.dp) {
        result = result.shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = liquid.shadowAmbient,
            spotColor = liquid.shadowSpot
        )
    }
    result = result.clip(shape)
    if (blur != null) {
        // Android 12+: conteúdo de trás desfocado sob a tinta do vidro (no-op no Android antigo)
        result = result.liquidBackdropBlur(shape = shape, radius = blur)
    }
    result = if (fill != null) result.background(fill, shape) else result.background(surface, shape)
    if (highlight != null) {
        result = result.background(highlight, shape)
    }
    return result.border(width = 1.dp, brush = border, shape = shape)
}

/**
 * Fundo de tela Obsidian: gradiente vertical com o brilho azulado do topo
 * e um halo suave do acento, como no Figma.
 */
@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val liquid = LocalLiquidGlassColors.current
    // Visual Acerto: fundo sólido, sem halo nem degradê.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(liquid.background),
        content = content
    )
}

/** Superfície de vidro genérica, clicável quando [onClick] é informado. */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidShapes.row,
    surface: Color = LocalLiquidGlassColors.current.cardSurface,
    elevation: Dp = LocalLiquidElevation.current.low,
    contentPadding: PaddingValues = PaddingValues(LocalLiquidSpacing.current.cardPadding),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlass(shape = shape, surface = surface, elevation = elevation)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content
    )
}

/**
 * Card de vidro. Com [highlighted] = true usa o gradiente de destaque do Figma
 * (card "Available Space"); o texto deve usar `heroText`.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    shape: Shape = LiquidShapes.card,
    contentPadding: PaddingValues = PaddingValues(LocalLiquidSpacing.current.lg),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    Column(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                fill = if (highlighted) liquid.heroGradient else null,
                elevation = LocalLiquidElevation.current.medium
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content
    )
}

/**
 * Pílula de vidro (chips, abas, botões). Selecionada usa o gradiente do acento
 * com texto branco; não selecionada usa o vidro cinza.
 */
@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val interaction = remember { MutableInteractionSource() }
    // Seleção animada: o gradiente do acento entra/sai em cross-fade (fase de desenho).
    val selection = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = LiquidMotion.selection,
        label = "pill_selection"
    )
    Row(
        modifier = modifier
            .pressScale(interaction)
            .glow(LiquidAccent, radius = 36.dp) { selection.value * 0.6f }
            .liquidGlass(
                shape = LiquidShapes.pill,
                surface = liquid.pillSecondaryBg,
                elevation = LocalLiquidElevation.current.low
            )
            .drawBehind {
                val a = selection.value
                if (a > 0.01f) drawRect(brush = liquid.accentGradient, alpha = a)
            }
            .then(
                if (onClick != null) {
                    Modifier.selectable(
                        selected = selected,
                        interactionSource = interaction,
                        indication = LocalIndication.current,
                        role = Role.Button,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/** Botão circular de vidro (Figma: 46 dp, ícone de 20 dp). */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = LocalLiquidSizes.current.iconButton,
    tint: Color = LocalLiquidGlassColors.current.textPrimary,
    accent: Boolean = false
) {
    val liquid = LocalLiquidGlassColors.current
    val interaction = remember { MutableInteractionSource() }
    // Ação principal (accent): gradiente + glow periwinkle, animados ao alternar.
    val accentProgress = animateFloatAsState(
        targetValue = if (accent) 1f else 0f,
        animationSpec = LiquidMotion.selection,
        label = "icon_button_accent"
    )
    val iconTint by animateColorAsState(
        targetValue = if (accent) liquid.pillPrimaryText else tint,
        label = "icon_button_tint"
    )
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .pressScale(interaction, pressedScale = 0.9f)
            .glow(LiquidAccent, radius = size * 0.85f) { accentProgress.value }
            .liquidGlass(
                shape = LiquidShapes.pill,
                surface = liquid.circleButtonBg,
                elevation = LocalLiquidElevation.current.low
            )
            .drawBehind {
                val a = accentProgress.value
                if (a > 0.01f) drawRect(brush = liquid.accentGradient, alpha = a)
            }
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(LocalLiquidSizes.current.icon)
        )
    }
}

/**
 * Cores de campo de texto de vidro (Figma: campo branco 6% com borda clara;
 * foco no acento). Use em qualquer OutlinedTextField do app.
 */
@Composable
fun liquidTextFieldColors(): TextFieldColors {
    val liquid = LocalLiquidGlassColors.current
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = liquid.textPrimary,
        unfocusedTextColor = liquid.textPrimary,
        disabledTextColor = liquid.textMuted,
        focusedContainerColor = liquid.cardSurfaceVariant,
        unfocusedContainerColor = liquid.cardSurfaceVariant,
        disabledContainerColor = liquid.cardSurfaceVariant.copy(alpha = liquid.cardSurfaceVariant.alpha * 0.5f),
        focusedBorderColor = liquid.accent,
        unfocusedBorderColor = liquid.cardBorder,
        disabledBorderColor = liquid.cardBorderSubtle,
        focusedLabelColor = liquid.accent,
        unfocusedLabelColor = liquid.textSecondary,
        focusedPlaceholderColor = liquid.textMuted,
        unfocusedPlaceholderColor = liquid.textMuted,
        focusedLeadingIconColor = liquid.accent,
        unfocusedLeadingIconColor = liquid.textSecondary,
        focusedTrailingIconColor = liquid.accent,
        unfocusedTrailingIconColor = liquid.textSecondary,
        focusedPrefixColor = liquid.textSecondary,
        unfocusedPrefixColor = liquid.textSecondary,
        focusedSuffixColor = liquid.textSecondary,
        unfocusedSuffixColor = liquid.textSecondary,
        cursorColor = liquid.accent,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorContainerColor = liquid.cardSurfaceVariant
    )
}

/** Cores de FilterChip de vidro: selecionado no acento, normal em vidro cinza. */
@Composable
fun liquidFilterChipColors(): SelectableChipColors {
    val liquid = LocalLiquidGlassColors.current
    return FilterChipDefaults.filterChipColors(
        containerColor = liquid.pillSecondaryBg,
        labelColor = liquid.textSecondary,
        iconColor = liquid.textSecondary,
        selectedContainerColor = liquid.pillPrimaryBg,
        selectedLabelColor = liquid.pillPrimaryText,
        selectedLeadingIconColor = liquid.pillPrimaryText,
        selectedTrailingIconColor = liquid.pillPrimaryText
    )
}

/** Divisória fina com o tom da borda do vidro. */
@Composable
fun GlassDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalLiquidGlassColors.current.cardBorderSubtle)
    )
}

// ---------- Previews ----------

@Composable
private fun GlassPreviewContent() {
    val liquid = LocalLiquidGlassColors.current
    LiquidBackground {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassSurface(modifier = Modifier.fillMaxWidth(), shape = LiquidShapes.field) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Pesquisar pessoa ou compra",
                        color = liquid.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.Search, null, tint = liquid.textPrimary)
                }
            }
            GlassCard(modifier = Modifier.fillMaxWidth(), highlighted = true) {
                Text("Fatura do mês", color = liquid.heroText, style = MaterialTheme.typography.titleMedium)
                Text(
                    "R$ 1.250,00",
                    color = liquid.heroText,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Carlos", color = liquid.textPrimary, style = MaterialTheme.typography.titleSmall)
                Text("2 itens neste mês", color = liquid.textMuted, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassPill(selected = true, onClick = {}) {
                    Text("Todas", color = liquid.pillPrimaryText, style = MaterialTheme.typography.labelLarge)
                }
                GlassPill(onClick = {}) {
                    Text("Pendentes", color = liquid.pillSecondaryText, style = MaterialTheme.typography.labelLarge)
                }
                GlassIconButton(icon = Icons.Default.Add, contentDescription = "Adicionar", onClick = {}, accent = true)
            }
            GlassDivider()
        }
    }
}

@Preview(name = "Liquid Glass escuro", widthDp = 390, heightDp = 520)
@Composable
private fun GlassComponentsDarkPreview() {
    QuemMeDeveTheme(darkTheme = true) { GlassPreviewContent() }
}

@Preview(name = "Liquid Glass claro", widthDp = 390, heightDp = 520)
@Composable
private fun GlassComponentsLightPreview() {
    QuemMeDeveTheme(darkTheme = false) { GlassPreviewContent() }
}
