package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalLiquidGlassColors

/**
 * Segmented liquid capsule selector for switching between Monthly Invoice and Card Installments tabs.
 */
@Composable
fun HomeTabSelector(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .background(liquid.cardSurfaceVariant)
            .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(50))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Tab 0: Fatura do Mês
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(
                    if (selectedTabIndex == 0) liquid.pillPrimaryBg
                    else Color.Transparent
                )
                .border(
                    width = 1.dp,
                    color = if (selectedTabIndex == 0) liquid.cardBorder else Color.Transparent,
                    shape = RoundedCornerShape(50)
                )
                .clickable { onTabSelected(0) }
                .testTag("tab_monthly_invoice"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = if (selectedTabIndex == 0) liquid.pillPrimaryText else liquid.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Fatura do Mês",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = if (selectedTabIndex == 0) liquid.pillPrimaryText else liquid.textSecondary
                )
            }
        }

        // Tab 1: Parcelas no Cartão
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(
                    if (selectedTabIndex == 1) liquid.pillPrimaryBg
                    else Color.Transparent
                )
                .border(
                    width = 1.dp,
                    color = if (selectedTabIndex == 1) liquid.cardBorder else Color.Transparent,
                    shape = RoundedCornerShape(50)
                )
                .clickable { onTabSelected(1) }
                .testTag("tab_card_installments"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = if (selectedTabIndex == 1) liquid.pillPrimaryText else liquid.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Parcelas Cartão",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = if (selectedTabIndex == 1) liquid.pillPrimaryText else liquid.textSecondary
                )
            }
        }
    }
}
