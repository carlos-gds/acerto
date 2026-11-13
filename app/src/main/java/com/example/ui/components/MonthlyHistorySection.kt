package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.MonthlyHistorySummary
import com.example.util.CurrencyFormatter
import androidx.compose.foundation.border
import com.example.ui.components.glass.liquidFilterChipColors
import com.example.ui.components.glass.liquidTextFieldColors
import com.example.ui.theme.LiquidShapes

@Composable
fun MonthlyHistorySection(
    history: List<MonthlyHistorySummary>,
    isPrivacyMode: Boolean,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val visibleHistory = history.take(6)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LiquidShapes.cardLarge,
        colors = CardDefaults.cardColors(containerColor = liquid.cardSurface),
        border = BorderStroke(1.dp, liquid.specularBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Histórico mensal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = liquid.textPrimary
                )
                Text(
                    text = "Acompanhe o que foi pago e o que ainda está pendente",
                    style = MaterialTheme.typography.bodySmall,
                    color = liquid.textSecondary
                )
            }

            if (visibleHistory.isEmpty()) {
                Text(
                    text = "Ainda não há movimentações para mostrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textMuted
                )
            } else {
                visibleHistory.forEach { month ->
                    MonthlyHistoryRow(
                        summary = month,
                        isPrivacyMode = isPrivacyMode
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyHistoryRow(
    summary: MonthlyHistorySummary,
    isPrivacyMode: Boolean
) {
    val liquid = LocalLiquidGlassColors.current
    val periodLabel = "${CurrencyFormatter.getMonthShortName(summary.month)}/${summary.year}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = liquid.textPrimary
            )
            Text(
                text = "${summary.installmentCount} parcelas",
                style = MaterialTheme.typography.labelSmall,
                color = liquid.textMuted
            )
        }

        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(
                text = CurrencyFormatter.format(summary.totalAmount, isPrivacyMode),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = liquid.textPrimary
            )
            Text(
                text = "Pago: ${CurrencyFormatter.format(summary.paidAmount, isPrivacyMode)}  •  Pendente: ${CurrencyFormatter.format(summary.pendingAmount, isPrivacyMode)}",
                style = MaterialTheme.typography.labelSmall,
                color = if (summary.pendingAmount > 0) liquid.accentPending else liquid.accentPaid
            )
        }
    }
}
