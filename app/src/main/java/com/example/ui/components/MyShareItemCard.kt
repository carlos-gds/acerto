package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.InstallmentItem
import com.example.ui.components.glass.liquidGlass
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidElevation
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.CurrencyFormatter

/**
 * Card representing the user's own share ("Minha Parte") in a shared bill.
 */
@Composable
fun MyShareItemCard(
    item: InstallmentItem,
    isPrivacyMode: Boolean,
    onEdit: (InstallmentItem) -> Unit,
    onDelete: (InstallmentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    var showOptionsMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = LiquidShapes.row, elevation = LocalLiquidElevation.current.low)
            .clickable { onEdit(item) }
            .testTag("my_share_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(LiquidShapes.iconTile)
                        .background(liquid.accent.copy(alpha = 0.16f))
                        .border(1.dp, liquid.cardBorderSubtle, LiquidShapes.iconTile),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Splitscreen,
                        contentDescription = null,
                        tint = liquid.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = item.expenseTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = liquid.textPrimary
                    )
                    Text(
                        text = "Sua cota mensal • Toque para editar",
                        style = MaterialTheme.typography.bodySmall,
                        color = liquid.textMuted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val displayShare = CurrencyFormatter.format(item.amount, isPrivacyMode)
                Text(
                    text = displayShare,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = liquid.accent
                )

                // Quick Edit button pill
                Surface(
                    shape = LiquidShapes.pill,
                    color = liquid.pillSecondaryBg,
                    border = BorderStroke(1.dp, liquid.cardBorder),
                    modifier = Modifier
                        .clickable { onEdit(item) }
                        .testTag("edit_my_share_btn_${item.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Minha Parte",
                            tint = liquid.textPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Editar",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = liquid.textPrimary
                        )
                    }
                }

                // 3-dots Menu for Edit / Delete
                Box {
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier
                            .testTag("options_my_share_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = liquid.textSecondary,
                            modifier = Modifier.size(16.dp)
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
                            text = { Text("Editar Minha Parte", color = liquid.textPrimary) },
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
                                onEdit(item)
                            },
                            modifier = Modifier.testTag("menu_edit_my_share_${item.id}")
                        )

                        DropdownMenuItem(
                            text = { Text("Excluir Conta", color = MaterialTheme.colorScheme.error) },
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
                                onDelete(item)
                            },
                            modifier = Modifier.testTag("menu_delete_my_share_${item.id}")
                        )
                    }
                }
            }
        }
    }
}
