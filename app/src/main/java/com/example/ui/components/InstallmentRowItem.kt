package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.components.glass.glow
import com.example.ui.components.glass.pressScale
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstallmentRowItem(
    item: InstallmentItem,
    isPrivacyMode: Boolean = false,
    onTogglePayment: (InstallmentItem) -> Unit,
    onEditItem: (InstallmentItem) -> Unit,
    onDeleteItem: (InstallmentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    var showOptionsMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(LiquidShapes.row)
            .combinedClickable(
                onClick = { /* normal click */ },
                onLongClick = { showOptionsMenu = true }
            )
            .testTag("installment_item_${item.id}"),
        shape = LiquidShapes.row,
        color = if (item.isPaid) {
            liquid.cardSurfaceVariant.copy(alpha = liquid.cardSurfaceVariant.alpha * 0.55f)
        } else {
            liquid.cardSurfaceVariant
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isPaid) liquid.cardBorderSubtle else liquid.cardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bloco de ícone com o acento (Figma: tile translúcido do acento, raio 10)
            val tileColor by animateColorAsState(
                if (item.isPaid) liquid.accentPaidContainer else liquid.accent.copy(alpha = 0.16f),
                label = "installment_tile"
            )
            val tileIconColor by animateColorAsState(
                if (item.isPaid) liquid.accentPaid else liquid.accent,
                label = "installment_tile_icon"
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    // Glow discreto: verde quando pago, periwinkle quando em aberto
                    .glow(tileIconColor, radius = 30.dp) { 0.4f }
                    .clip(LiquidShapes.iconTile)
                    .background(tileColor)
                    .border(1.dp, liquid.cardBorderSubtle, LiquidShapes.iconTile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.expenseType == ExpenseType.CARD_PURCHASE) {
                        Icons.Default.CreditCard
                    } else {
                        Icons.Default.Splitscreen
                    },
                    contentDescription = null,
                    tint = tileIconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.expenseTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        textDecoration = if (item.isPaid) TextDecoration.LineThrough else null
                    ),
                    color = if (item.isPaid) liquid.textMuted
                    else liquid.textPrimary
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.expenseType == ExpenseType.CARD_PURCHASE) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(liquid.cardSurface)
                                .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Parcela ${item.installmentNumber}/${item.totalInstallments}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = liquid.textSecondary
                            )
                        }

                        val remaining = item.totalInstallments - item.installmentNumber
                        if (remaining > 0) {
                            Text(
                                text = "Restam $remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = liquid.textMuted,
                                maxLines = 1,
                                softWrap = false
                            )
                        } else {
                            Text(
                                text = "Última parcela",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = liquid.accentPaid
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(liquid.cardSurface)
                                .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Conta Dividida",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = liquid.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Amount and payment status chip
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = CurrencyFormatter.format(item.amount, isPrivacyMode),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textDecoration = if (item.isPaid) TextDecoration.LineThrough else null
                    ),
                    color = if (item.isPaid) liquid.textMuted
                    else liquid.textPrimary
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Payment Toggle Chip
                // Transição animada pendente <-> pago: cor, fundo, largura e glow
                val statusColor by animateColorAsState(
                    if (item.isPaid) liquid.accentPaid else liquid.accentPending,
                    label = "installment_status_color"
                )
                val statusContainer by animateColorAsState(
                    if (item.isPaid) liquid.accentPaidContainer else liquid.accentPendingContainer,
                    label = "installment_status_container"
                )
                val toggleInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .pressScale(toggleInteraction, pressedScale = 0.9f)
                        .glow(statusColor, radius = 30.dp) { 0.35f }
                        .clip(LiquidShapes.pill)
                        .background(statusContainer)
                        .border(1.dp, statusColor.copy(alpha = 0.45f), LiquidShapes.pill)
                        .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 500f))
                        .clickable(
                            interactionSource = toggleInteraction,
                            indication = LocalIndication.current
                        ) { onTogglePayment(item) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("toggle_paid_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (item.isPaid) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = if (item.isPaid) "Pago" else "Pendente",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = statusColor
                        )
                    }
                }
            }

            // 3-dots Options Menu
            Box {
                IconButton(
                    onClick = { showOptionsMenu = true },
                    modifier = Modifier
                        .testTag("options_item_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opções do item",
                        tint = liquid.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                    modifier = Modifier
                        .background(liquid.sheetSurface)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar", color = liquid.textPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = liquid.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showOptionsMenu = false
                            onEditItem(item)
                        },
                        modifier = Modifier.testTag("menu_edit_${item.id}")
                    )

                    DropdownMenuItem(
                        text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showOptionsMenu = false
                            onDeleteItem(item)
                        },
                        modifier = Modifier.testTag("menu_delete_${item.id}")
                    )
                }
            }
        }
    }
}

