package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.ui.components.glass.liquidGlass
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidElevation
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.MonthlySummary
import com.example.util.CurrencyFormatter
import com.example.ui.components.glass.animatedMoney

// Cores sobre o card de destaque (gradiente azul): iguais nos dois temas.
// Cores do card de destaque derivadas do tema (visual Acerto: card sólido)
private val HeroTextSecondary: Color @Composable get() = LocalLiquidGlassColors.current.textSecondary
private val HeroTextMuted: Color @Composable get() = LocalLiquidGlassColors.current.textSecondary
private val HeroGlass: Color @Composable get() = LocalLiquidGlassColors.current.cardSurfaceVariant
private val HeroGlassBorder: Color @Composable get() = Color.Transparent
private val HeroPaid: Color @Composable get() = LocalLiquidGlassColors.current.accentPaid
private val HeroPending: Color @Composable get() = LocalLiquidGlassColors.current.accentPending
private val HeroOnWhite: Color @Composable get() = LocalLiquidGlassColors.current.pillPrimaryText

@Composable
fun SummaryCard(
    summary: MonthlySummary,
    isPrivacyMode: Boolean = false,
    onTogglePrivacy: () -> Unit = {},
    onAddCardPurchase: () -> Unit = {},
    onAddSharedBill: () -> Unit = {},
    onOptionsClick: () -> Unit = {},
    onOthersDebtClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    /** false quando a privacidade é controlada no cabeçalho da tela. */
    showPrivacyToggle: Boolean = true,
    /** false quando as ações de adicionar ficam no botão + (SpeedDial). */
    showQuickActions: Boolean = true
) {
    val liquid = LocalLiquidGlassColors.current

    // Card de destaque Liquid Glass (Figma: "Available Space", gradiente do acento).
    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = LiquidShapes.cardLarge,
                fill = liquid.heroGradient,
                elevation = LocalLiquidElevation.current.high
            )
            .testTag("monthly_summary_card")
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fatura total do mês",
                style = MaterialTheme.typography.titleMedium,
                color = liquid.heroText
            )

            if (showPrivacyToggle) {
                // Botão de privacidade (olho)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(HeroGlass)
                        .border(1.dp, HeroGlassBorder, CircleShape)
                        .clickable { onTogglePrivacy() }
                        .testTag("toggle_privacy_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPrivacyMode) "Mostrar valores" else "Ocultar valores",
                        tint = liquid.heroText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val displayTotal = CurrencyFormatter.format(animatedMoney(summary.totalInvoice), isPrivacyMode)
        Text(
            text = displayTotal,
            style = MaterialTheme.typography.displaySmall,
            color = liquid.heroText,
            modifier = Modifier.testTag("total_invoice_text")
        )

        // Progresso do reembolso (Figma: barra de vidro com preenchimento branco)
        if (summary.othersTotal > 0) {
            Spacer(modifier = Modifier.height(14.dp))
            val progress = (summary.othersPaid / summary.othersTotal).toFloat().coerceIn(0f, 1f)
            val percent = (progress * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reembolso recebido",
                    style = MaterialTheme.typography.bodySmall,
                    color = HeroTextSecondary
                )
                Text(
                    text = "$percent% quitado",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (percent == 100) HeroPaid else liquid.heroText
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(LiquidShapes.pill)
                    .background(HeroGlass)
                    .border(1.dp, HeroGlassBorder, LiquidShapes.pill)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(LiquidShapes.pill)
                        .background(LocalLiquidGlassColors.current.accentPaid)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Painéis "Você paga" e "Outros devem" em vidro sobre o destaque
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(LiquidShapes.row)
                    .background(HeroGlass)
                    .border(1.dp, HeroGlassBorder, LiquidShapes.row)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Você paga",
                    style = MaterialTheme.typography.labelMedium,
                    color = HeroTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.format(animatedMoney(summary.myShare), isPrivacyMode),
                    style = MaterialTheme.typography.titleLarge,
                    color = liquid.heroText
                )
                Text(
                    text = "Sua cota líquida",
                    style = MaterialTheme.typography.labelSmall,
                    color = HeroTextMuted
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(LiquidShapes.row)
                    .background(HeroGlass)
                    .border(1.dp, HeroGlassBorder, LiquidShapes.row)
                    .clickable { onOthersDebtClick() }
                    .padding(12.dp)
                    .testTag("others_debt_summary_panel")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Outros devem",
                        style = MaterialTheme.typography.labelMedium,
                        color = HeroTextSecondary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ver detalhamento das dívidas",
                        tint = liquid.heroText,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.format(animatedMoney(summary.othersTotal), isPrivacyMode),
                    style = MaterialTheme.typography.titleLarge,
                    color = liquid.heroText
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pendente:",
                        style = MaterialTheme.typography.labelSmall,
                        color = HeroTextMuted
                    )
                    Text(
                        text = CurrencyFormatter.format(animatedMoney(summary.othersPending), isPrivacyMode),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.othersPending > 0) HeroPending else HeroPaid
                    )
                }
            }
        }

        if (showQuickActions) {
            Spacer(modifier = Modifier.height(16.dp))

            // Ações rápidas em pílulas (Figma: pílula clara + pílulas de vidro)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(LiquidShapes.pill)
                        .background(LocalLiquidGlassColors.current.pillPrimaryBg)
                        .clickable { onAddCardPurchase() }
                        .testTag("quick_action_add_purchase")
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = HeroOnWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Compra",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = HeroOnWhite
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(LiquidShapes.pill)
                        .background(HeroGlass)
                        .border(1.dp, HeroGlassBorder, LiquidShapes.pill)
                        .clickable { onAddSharedBill() }
                        .testTag("quick_action_add_shared")
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Splitscreen,
                        contentDescription = null,
                        tint = liquid.heroText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "÷ Dividir",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = liquid.heroText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(HeroGlass)
                        .border(1.dp, HeroGlassBorder, CircleShape)
                        .clickable { onOptionsClick() }
                        .testTag("quick_action_options"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Opções",
                        tint = liquid.heroText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
