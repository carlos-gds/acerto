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
    HOME("Início", Icons.Default.Home, "bottom_nav_home"),
    CARD("Cartão", Icons.Default.CreditCard, "bottom_nav_card"),
    DEBTS("Pessoas", Icons.Default.People, "bottom_nav_debts"),
    SETTINGS("Ajustes", Icons.Default.Settings, "bottom_nav_settings")
}

/**
 * Barra de navegação inferior Liquid Glass (Figma: pílula de vidro flutuante e
 * translúcida). O item ativo fica numa pílula periwinkle translúcida que desliza
 * com mola entre os itens, com glow suave. O botão + (SpeedDial) fica fora, à direita.
 */
@Composable
fun LiquidBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val tabs = AppTab.entries
    // Android 12+ com blur: vidro bem transparente sobre o conteúdo desfocado (como no Figma).
    // Fallback (Android 7–11 ou blur desligado): base densa para o conteúdo de trás não poluir a barra.
    val hasBackdrop = LocalLiquidBackdrop.current != null
    // Visual Acerto: barra sólida (sem vidro), na cor das superfícies.
    val barTint = liquid.headerSurface

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .liquidGlass(
                shape = LiquidShapes.pill,
                surface = barTint,
                elevation = LocalLiquidElevation.current.high,
                blur = LiquidBlurDefaults.bar
            )
            .background(liquid.headerSurface, LiquidShapes.pill)
            .padding(5.dp)
            .testTag("liquid_bottom_bar")
    ) {
        val itemWidth = maxWidth / tabs.size
        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * selectedTab.ordinal,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
            label = "bottom_bar_indicator"
        )

        // Pílula ativa deslizante (periwinkle translúcido + borda especular + glow)
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(itemWidth)
                .fillMaxHeight()
                .clip(LiquidShapes.pill)
                .background(liquid.cardSurfaceVariant)
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
    val labelColor by animateColorAsState(
        targetValue = if (selected) (if (liquid.isDark) Color(0xFFF0354B) else liquid.pillPrimaryBg) else liquid.textSecondary,
        label = "bottom_item_label"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) {
            if (liquid.isDark) Color(0xFFF0354B) else liquid.pillPrimaryBg
        } else {
            liquid.textSecondary
        },
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(20.dp)
                    // Ícone ativo cresce levemente e ganha glow (fase de desenho)
                    .graphicsLayer {
                        val s = 1f + 0.12f * progress.value
                        scaleX = s
                        scaleY = s
                    }
                    .glow(LiquidAccent, radius = 18.dp) { progress.value * 0.7f }
            )
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize *
                        (1.3f / androidx.compose.ui.platform.LocalDensity.current.fontScale).coerceAtMost(1f)
                ),
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
