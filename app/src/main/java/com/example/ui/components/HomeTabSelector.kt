package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.liquidGlass
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidElevation
import com.example.ui.theme.LocalLiquidGlassColors

/**
 * Segmented liquid capsule selector for switching between Monthly Invoice and Card Installments tabs.
 *
 * Estilo Liquid Glass (Figma): cápsula de vidro com a aba ativa em pílula
 * preenchida pelo gradiente do acento.
 */
@Composable
fun HomeTabSelector(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .liquidGlass(
                shape = LiquidShapes.pill,
                surface = LocalLiquidGlassColors.current.pillSecondaryBg,
                elevation = LocalLiquidElevation.current.low
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabPill(
            label = "Fatura do Mês",
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            selected = selectedTabIndex == 0,
            testTag = "tab_monthly_invoice",
            onClick = { onTabSelected(0) }
        )
        TabPill(
            label = "Parcelas Cartão",
            icon = Icons.Default.CreditCard,
            selected = selectedTabIndex == 1,
            testTag = "tab_card_installments",
            onClick = { onTabSelected(1) }
        )
    }
}

@Composable
private fun RowScope.TabPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val contentColor = if (selected) liquid.pillPrimaryText else liquid.textSecondary

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(LiquidShapes.pill)
            .then(
                if (selected) {
                    Modifier
                        .background(liquid.accentGradient)
                        .border(1.dp, liquid.specularBorder, LiquidShapes.pill)
                } else {
                    Modifier
                }
            )
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}
