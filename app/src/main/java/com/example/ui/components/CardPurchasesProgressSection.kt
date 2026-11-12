package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.CardPurchaseProgress
import com.example.util.CurrencyFormatter

@Composable
fun CardPurchasesProgressSection(
    purchases: List<CardPurchaseProgress>,
    isPrivacyMode: Boolean = false,
    onDeleteExpense: (Long) -> Unit,
    emptyMessage: String = "Nenhuma compra parcelada cadastrada",
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    var expenseToDelete by remember { mutableStateOf<CardPurchaseProgress?>(null) }

    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            containerColor = liquid.cardSurface,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Excluir Compra", color = liquid.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente remover a compra '${expenseToDelete?.expense?.title}' e todas as suas parcelas?", color = liquid.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.expense?.id?.let { onDeleteExpense(it) }
                        expenseToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancelar", color = liquid.textSecondary)
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Compras Parceladas no Cartão",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = liquid.textPrimary
            )
            Text(
                text = "${purchases.size} ${if (purchases.size == 1) "compra" else "compras"}",
                style = MaterialTheme.typography.labelMedium,
                color = liquid.textSecondary
            )
        }

        if (purchases.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = liquid.cardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = liquid.textMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = liquid.textSecondary
                    )
                    Text(
                        text = "Toque no botão + abaixo para registrar uma compra feita no seu cartão.",
                        style = MaterialTheme.typography.bodySmall,
                        color = liquid.textMuted
                    )
                }
            }
        } else {
            purchases.forEach { item ->
                CardPurchaseProgressItem(
                    item = item,
                    isPrivacyMode = isPrivacyMode,
                    onDelete = { expenseToDelete = item }
                )
            }
        }
    }
}

@Composable
fun CardPurchaseProgressItem(
    item: CardPurchaseProgress,
    isPrivacyMode: Boolean = false,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val isFinished = item.remainingInstallmentsCount <= 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (liquid.isDark) 4.dp else 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (liquid.isDark) Color.Black.copy(alpha = 0.4f) else Color(0x1F0F172A)
            )
            .testTag("card_purchase_progress_${item.expense.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = liquid.cardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title, Person Pill and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.expense.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = liquid.textPrimary
                    )

                    // Person badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(liquid.circleButtonBg)
                            .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.personName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = liquid.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir compra",
                        tint = liquid.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total and Installment Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Valor Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = liquid.textSecondary
                    )
                    Text(
                        text = CurrencyFormatter.format(item.expense.totalAmount, isPrivacyMode),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = liquid.textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Valor da Parcela",
                        style = MaterialTheme.typography.labelSmall,
                        color = liquid.textSecondary
                    )
                    Text(
                        text = "${item.totalInstallmentsCount}x de ${CurrencyFormatter.format(item.installmentAmount, isPrivacyMode)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = liquid.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isFinished) liquid.accentPaid else liquid.pillPrimaryBg,
                trackColor = liquid.cardSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Installments remaining information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.paidInstallmentsCount} de ${item.totalInstallmentsCount} pagas",
                    style = MaterialTheme.typography.bodySmall,
                    color = liquid.textMuted
                )

                if (isFinished) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = liquid.accentPaid,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Totalmente Quitada!",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = liquid.accentPaid
                        )
                    }
                } else {
                    Text(
                        text = "Restam ${item.remainingInstallmentsCount} parcelas (${CurrencyFormatter.format(item.remainingAmount, isPrivacyMode)})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = liquid.accentPending
                    )
                }
            }
        }
    }
}
