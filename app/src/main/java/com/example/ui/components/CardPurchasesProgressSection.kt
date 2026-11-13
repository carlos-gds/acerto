package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.key
import com.example.ui.components.glass.liquidGlass
import com.example.ui.components.glass.rememberAppearTracker
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.components.glass.staggeredAppear
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidElevation
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.CardPurchaseProgress
import com.example.util.CurrencyFormatter
import com.example.ui.components.glass.liquidFilterChipColors
import com.example.ui.components.glass.liquidTextFieldColors
import com.example.ui.components.glass.LiquidDialogBlur
import com.example.ui.components.glass.liquidDialogSurface
import com.example.ui.components.glass.liquidDialogEnter

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
    val appearTracker = rememberAppearTracker()
    val reducedMotion = rememberReducedMotion()

    if (expenseToDelete != null) {
        AlertDialog(
            modifier = Modifier.liquidDialogEnter().border(1.dp, liquid.specularBorder, LiquidShapes.cardLarge),
            onDismissRequest = { expenseToDelete = null },
            containerColor = liquidDialogSurface(),
            shape = LiquidShapes.cardLarge,
            title = {
                LiquidDialogBlur()
                Text("Excluir Compra", color = liquid.textPrimary, fontWeight = FontWeight.Bold) },
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
                style = MaterialTheme.typography.titleMedium,
                color = liquid.textPrimary
            )
            Text(
                text = "${purchases.size} ${if (purchases.size == 1) "compra" else "compras"}",
                style = MaterialTheme.typography.labelMedium,
                color = liquid.textSecondary
            )
        }

        if (purchases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(shape = LiquidShapes.card, elevation = LocalLiquidElevation.current.low)
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
            // Entrada progressiva dos cards de compra (uma vez por compra)
            purchases.forEachIndexed { index, item ->
                key(item.expense.id) {
                    CardPurchaseProgressItem(
                        item = item,
                        isPrivacyMode = isPrivacyMode,
                        onDelete = { expenseToDelete = item },
                        modifier = Modifier.staggeredAppear(
                            "card_purchase_${item.expense.id}", index, appearTracker, !reducedMotion
                        )
                    )
                }
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = LiquidShapes.card, elevation = LocalLiquidElevation.current.medium)
            .testTag("card_purchase_progress_${item.expense.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon tile, Title, Person Pill and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bloco de ícone com o acento (Figma: tile 56 dp, raio 10)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(LiquidShapes.iconTile)
                        .background(
                            if (isFinished) liquid.accentPaidContainer else liquid.accent.copy(alpha = 0.16f)
                        )
                        .border(1.dp, liquid.cardBorderSubtle, LiquidShapes.iconTile),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFinished) Icons.Default.CheckCircle else Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = if (isFinished) liquid.accentPaid else liquid.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.expense.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = liquid.textPrimary
                    )

                    // Person badge
                    Box(
                        modifier = Modifier
                            .clip(LiquidShapes.pill)
                            .background(liquid.pillSecondaryBg)
                            .border(1.dp, liquid.cardBorderSubtle, LiquidShapes.pill)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.personName,
                            style = MaterialTheme.typography.labelSmall,
                            color = liquid.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
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

            // Barra de progresso de vidro (Figma: trilho translúcido + preenchimento em gradiente)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(LiquidShapes.pill)
                    .background(liquid.cardSurfaceVariant)
                    .border(1.dp, liquid.cardBorderSubtle, LiquidShapes.pill)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(LiquidShapes.pill)
                        .then(
                            if (isFinished) Modifier.background(liquid.accentPaid)
                            else Modifier.background(liquid.accentGradient)
                        )
                )
            }

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
