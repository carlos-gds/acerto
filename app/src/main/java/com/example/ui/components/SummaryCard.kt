package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.viewmodel.MonthlySummary
import com.example.util.CurrencyFormatter

@Composable
fun SummaryCard(
    summary: MonthlySummary,
    isPrivacyMode: Boolean = false,
    onTogglePrivacy: () -> Unit = {},
    onAddCardPurchase: () -> Unit = {},
    onAddSharedBill: () -> Unit = {},
    onOptionsClick: () -> Unit = {},
    onOthersDebtClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current

    // Main Liquid Glass Hero Card (Ref. Liquid Glass Fintech App)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (liquid.isDark) 8.dp else 12.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = if (liquid.isDark) Color.Black.copy(alpha = 0.5f) else Color(0x260F172A)
            )
            .testTag("monthly_summary_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = liquid.cardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Label: ACCOUNT BALANCE / FATURA TOTAL DO MÊS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FATURA TOTAL DO MÊS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = liquid.textSecondary
                )

                // Eye Privacy Toggle Button (from Liquid Glass reference)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(liquid.circleButtonBg)
                        .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                        .clickable { onTogglePrivacy() }
                        .testTag("toggle_privacy_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPrivacyMode) "Mostrar valores" else "Ocultar valores",
                        tint = liquid.eyeActive,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Balance Amount (e.g. $21,530.86 in reference photo)
            val displayTotal = CurrencyFormatter.format(summary.totalInvoice, isPrivacyMode)
            Text(
                text = displayTotal,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = liquid.textPrimary,
                modifier = Modifier.testTag("total_invoice_text")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Action Pill Buttons (Send / Request / More from reference photo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Pill Button ("+ Compra")
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onAddCardPurchase() }
                        .testTag("quick_action_add_purchase"),
                    shape = RoundedCornerShape(50),
                    color = liquid.pillPrimaryBg,
                    border = if (liquid.isDark) androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorder) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = liquid.pillPrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Compra",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = liquid.pillPrimaryText
                        )
                    }
                }

                // Secondary Pill Button ("÷ Dividir")
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onAddSharedBill() }
                        .testTag("quick_action_add_shared"),
                    shape = RoundedCornerShape(50),
                    color = liquid.pillSecondaryBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Splitscreen,
                            contentDescription = null,
                            tint = liquid.pillSecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "÷ Dividir",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = liquid.pillSecondaryText
                        )
                    }
                }

                // Option Circle Button ("...")
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(liquid.circleButtonBg)
                        .border(1.dp, liquid.cardBorder, CircleShape)
                        .clickable { onOptionsClick() }
                        .testTag("quick_action_options"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Opções",
                        tint = liquid.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Two Sub-Metric Glass Panels: "Você Paga" vs "Outros Devem"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "Você paga" panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(liquid.cardSurfaceVariant)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "VOCÊ PAGA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = liquid.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val displayMyShare = CurrencyFormatter.format(summary.myShare, isPrivacyMode)
                        Text(
                            text = displayMyShare,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = liquid.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sua cota líquida",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = liquid.textMuted
                        )
                    }
                }

                // "Outros devem" panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(liquid.cardSurfaceVariant)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(18.dp))
                        .clickable { onOthersDebtClick() }
                        .padding(14.dp)
                        .testTag("others_debt_summary_panel")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OUTROS DEVEM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = liquid.textSecondary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Ver detalhamento das dívidas",
                                tint = liquid.pillPrimaryBg,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val displayOthersTotal = CurrencyFormatter.format(summary.othersTotal, isPrivacyMode)
                        Text(
                            text = displayOthersTotal,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = liquid.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pendente:",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = liquid.textMuted
                            )
                            val displayPending = CurrencyFormatter.format(summary.othersPending, isPrivacyMode)
                            Text(
                                text = displayPending,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (summary.othersPending > 0) liquid.accentPending else liquid.accentPaid
                            )
                        }
                    }
                }
            }

            // Progress bar if others have debts to reimburse
            if (summary.othersTotal > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                val progress = (summary.othersPaid / summary.othersTotal).toFloat().coerceIn(0f, 1f)
                val percent = (progress * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reembolso recebido",
                        style = MaterialTheme.typography.labelSmall,
                        color = liquid.textSecondary
                    )
                    Text(
                        text = "$percent% quitado",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (percent == 100) liquid.accentPaid else liquid.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = liquid.accentPaid,
                    trackColor = liquid.cardSurfaceVariant
                )
            }
        }
    }
}
