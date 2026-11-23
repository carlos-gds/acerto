package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.LiquidBlurDefaults
import com.example.ui.components.glass.LiquidMotion
import com.example.ui.components.glass.LocalLiquidBackdrop
import com.example.ui.components.glass.glow
import com.example.ui.components.glass.liquidGlass
import com.example.ui.components.glass.pressScale
import com.example.ui.theme.LiquidAccent
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidElevation
import com.example.ui.theme.LocalLiquidGlassColors

/** Abas principais do app. */
enum class AppTab(val label: String, val icon: ImageVector, val testTag: String) {
    DEBTS("Pessoas", Icons.Default.People, "bottom_nav_debts"),
    HOME("Início", Icons.Default.Home, "bottom_nav_home"),
    CARD("Cartão", Icons.Default.CreditCard, "bottom_nav_card"),
    /** Fora da barra: abre tocando na foto de perfil da Início. */
    SETTINGS("Ajustes", Icons.Default.Settings, "bottom_nav_settings")
}

/**
 * Barra de navegação inferior Liquid Glass (Figma: pílula de vidro flutuante e
 * translúcida). O item ativo fica numa pílula periwinkle translúcida que desliza
 * com mola entre os itens. Compacta e centralizada; a cor acompanha o fundo da tela.
 */
@Composable
fun LiquidBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
    /** Cor da pílula: escura sobre telas claras; a cor do fundo sobre telas escuras. */
    containerColor: androidx.compose.ui.graphics.Color = com.example.ui.theme.AcertoPalette.NavBar,
    /** Posição da pílula (0 = Pessoas, 1 = Início, 2 = Cartão), contínua enquanto arrasta. */
    indicatorPosition: (() -> Float)? = null
) {
    val liquid = LocalLiquidGlassColors.current
    val tabs = listOf(AppTab.DEBTS, AppTab.HOME, AppTab.CARD)
    // Android 12+ com blur: vidro bem transparente sobre o conteúdo desfocado (como no Figma).
    // Fallback (Android 7–11 ou blur desligado): base densa para o conteúdo de trás não poluir a barra.
    val hasBackdrop = LocalLiquidBackdrop.current != null
    // Visual Acerto: barra sólida (sem vidro), na cor das superfícies.
    val barTint = liquid.headerSurface

    // Visual Noir: pílula escura flutuante, só ícones; o item ativo fica numa pílula clara
    BoxWithConstraints(
        // compacta e centralizada, como na referência
        modifier = modifier
            .width(72.dp * tabs.size + 12.dp)
            .height(60.dp)
            .clip(LiquidShapes.pill)
            .background(containerColor)
            .padding(6.dp)
            .testTag("liquid_bottom_bar")
    ) {
        val itemWidth = maxWidth / tabs.size
        val tapOffset by animateDpAsState(
            targetValue = itemWidth * tabs.indexOf(selectedTab).coerceAtLeast(0),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 420f),
            label = "bottom_bar_indicator"
        )
        Box(
            modifier = Modifier
                // segue a página enquanto arrasta; sem pager, anima pelo toque
                .offset {
                    val x = indicatorPosition?.let { pos -> itemWidth.toPx() * pos().coerceIn(0f, (tabs.size - 1).toFloat()) } ?: tapOffset.toPx()
                    androidx.compose.ui.unit.IntOffset(x.roundToIntCompat(), 0)
                }
                .width(itemWidth)
                .fillMaxHeight()
                .clip(LiquidShapes.pill)
                .background(com.example.ui.theme.AcertoPalette.NavSelected)
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .selectableGroup()
        ) {
            tabs.forEach { tab ->
                BottomBarItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    tab: AppTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val interaction = remember { MutableInteractionSource() }
    val progress = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = LiquidMotion.selection,
        label = "bottom_item_progress"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) androidx.compose.ui.graphics.Color(0xFF16181A) else androidx.compose.ui.graphics.Color(0xFF9A9EA1),
        label = "bottom_item_icon"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(LiquidShapes.pill)
            .pressScale(interaction, pressedScale = 0.9f)
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = LocalIndication.current,
                role = Role.Tab,
                onClick = onClick
            )
            .testTag(tab.testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    val sc = 1f + 0.08f * progress.value
                    scaleX = sc
                    scaleY = sc
                }
        )
    }
}

private fun Float.roundToIntCompat(): Int = kotlin.math.round(this).toInt()
