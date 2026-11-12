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
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { /* normal click */ },
                onLongClick = { showOptionsMenu = true }
            )
            .testTag("installment_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        color = if (item.isPaid) {
            liquid.cardSurfaceVariant.copy(alpha = 0.5f)
        } else {
            liquid.cardSurfaceVariant
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isPaid) liquid.cardBorderSubtle.copy(alpha = 0.3f)
            else liquid.cardBorderSubtle
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left icon container
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(liquid.circleButtonBg)
                    .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.expenseType == ExpenseType.CARD_PURCHASE) {
                        Icons.Default.CreditCard
                    } else {
                        Icons.Default.Splitscreen
                    },
                    contentDescription = null,
                    tint = liquid.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.expenseTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
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
                                color = liquid.textMuted
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (item.isPaid) liquid.accentPaidContainer
                            else liquid.accentPendingContainer
                        )
                        .clickable { onTogglePayment(item) }
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
                                tint = liquid.accentPaid,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = if (item.isPaid) "Pago" else "Pendente",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (item.isPaid) liquid.accentPaid
                            else liquid.accentPending
                        )
                    }
                }
            }

            // 3-dots Options Menu
            Box {
                IconButton(
                    onClick = { showOptionsMenu = true },
                    modifier = Modifier
                        .size(32.dp)
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
                        .background(liquid.cardSurface)
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

