package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.PersonMonthlyGroup
import com.example.util.CurrencyFormatter

@Composable
fun PersonDebtCard(
    group: PersonMonthlyGroup,
    selectedMonth: Int,
    selectedYear: Int,
    isPrivacyMode: Boolean = false,
    isExpanded: Boolean = true,
    onToggleExpanded: () -> Unit = {},
    onTogglePayment: (InstallmentItem) -> Unit,
    onEditItem: (InstallmentItem) -> Unit,
    onDeleteItem: (InstallmentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val context = LocalContext.current

    val isAllPaid = group.pendingAmount <= 0.001

    fun shareWhatsAppSummary() {
        val shareText = com.example.util.DebtMessageFormatter.format(
            group = group,
            scope = com.example.util.DebtMessageFormatter.DebtScope.SELECTED_MONTH,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear
        )
        com.example.util.ShareIntentHelper.sharePlainText(
            context = context,
            text = shareText,
            chooserTitle = "Enviar cobrança para ${group.personName}"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (liquid.isDark) 4.dp else 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (liquid.isDark) Color.Black.copy(alpha = 0.4f) else Color(0x1F0F172A)
            )
            .testTag("person_debt_card_${group.personId}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = liquid.cardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Person Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() }
                    .testTag("person_header_row_${group.personId}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Initials Circle
                    val initials = group.personName
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString("")
                        .ifEmpty { "P" }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(liquid.circleButtonBg)
                            .border(1.dp, liquid.cardBorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = liquid.textPrimary
                        )
                    }

                    Column {
                        Text(
                            text = group.personName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = liquid.textPrimary
                        )
                        Text(
                            text = "${group.items.size} ${if (group.items.size == 1) "item" else "itens"} neste mês",
                            style = MaterialTheme.typography.labelSmall,
                            color = liquid.textMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyFormatter.format(group.totalAmount, isPrivacyMode),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = liquid.textPrimary
                        )

                        // Status badge
                        if (isAllPaid) {
                            Text(
                                text = "Quitado",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = liquid.accentPaid
                            )
                        } else {
                            Text(
                                text = "Falta: ${CurrencyFormatter.format(group.pendingAmount, isPrivacyMode)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = liquid.accentPending
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Recolher detalhes de ${group.personName}" else "Expandir detalhes de ${group.personName}",
                        tint = liquid.textSecondary,
                        modifier = Modifier.testTag("toggle_expand_person_${group.personId}")
                    )
                }
            }

            // Expanded Item List
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    group.items.forEach { item ->
                        InstallmentRowItem(
                            item = item,
                            isPrivacyMode = isPrivacyMode,
                            onTogglePayment = onTogglePayment,
                            onEditItem = onEditItem,
                            onDeleteItem = onDeleteItem
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // WhatsApp share button
                    FilledTonalButton(
                        onClick = { shareWhatsAppSummary() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_whatsapp_${group.personId}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = liquid.cardSurfaceVariant,
                            contentColor = liquid.textPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = liquid.textPrimary
                            )
                            Text(
                                text = "Cobrar / Enviar Resumo no WhatsApp",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = liquid.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
