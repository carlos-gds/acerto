package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.components.glass.glow
import com.example.ui.components.glass.pressScale
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import androidx.compose.ui.semantics.Role
import com.example.ui.components.glass.liquidGlass
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidElevation
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.PersonMonthlyGroup
import com.example.util.CurrencyFormatter
import com.example.ui.components.glass.liquidFilterChipColors
import com.example.ui.components.glass.liquidTextFieldColors
import com.example.ui.components.glass.LiquidDialogBlur
import com.example.ui.components.glass.liquidDialogSurface
import com.example.ui.components.glass.liquidDialogEnter
import com.example.ui.components.glass.animatedMoney

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
    val chevronRotation = animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "person_chevron"
    )

    var showPrivacyShareConfirmation by remember { mutableStateOf(false) }

    fun performShare() {
        val shareText = com.example.util.DebtMessageFormatter.format(
            group = group,
            scope = com.example.util.DebtMessageFormatter.DebtScope.SELECTED_MONTH,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            pixKey = com.example.util.PixKeyStore.get(context)
        )
        com.example.util.ShareIntentHelper.sharePlainText(
            context = context,
            text = shareText,
            chooserTitle = "Enviar cobrança para ${group.personName}"
        )
    }

    // Com o modo privacidade ativo, o texto compartilhado contém valores reais:
    // pede confirmação antes, igual à tela de detalhes.
    fun shareWhatsAppSummary() {
        if (isPrivacyMode) {
            showPrivacyShareConfirmation = true
        } else {
            performShare()
        }
    }

    if (showPrivacyShareConfirmation) {
        AlertDialog(
            modifier = Modifier.liquidDialogEnter().border(1.dp, liquid.specularBorder, LiquidShapes.cardLarge),
            onDismissRequest = { showPrivacyShareConfirmation = false },
            title = {
                LiquidDialogBlur()
                Text(
                    text = "Modo Privacidade Ativo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = liquid.textPrimary
                )
            },
            text = {
                Text(
                    text = "O texto a ser compartilhado conterá os valores financeiros reais (não mascarados) das pendências de ${group.personName}. Deseja continuar com o compartilhamento?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPrivacyShareConfirmation = false
                        performShare()
                    },
                    modifier = Modifier.testTag("confirm_privacy_share_btn")
                ) {
                    Text(
                        text = "Continuar e Compartilhar",
                        fontWeight = FontWeight.Bold,
                        color = liquid.accentPaid
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPrivacyShareConfirmation = false },
                    modifier = Modifier.testTag("cancel_privacy_share_btn")
                ) {
                    Text(text = "Cancelar", color = liquid.textSecondary)
                }
            },
            containerColor = liquidDialogSurface(),
            shape = RoundedCornerShape(20.dp)
        )
    }

    val personColor = remember(group.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(group.colorHex))
        } catch (_: Exception) {
            liquid.accent
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = LiquidShapes.card, elevation = LocalLiquidElevation.current.medium)
            .testTag("person_debt_card_${group.personId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

                    // Avatar de vidro na cor da pessoa (Figma: avatar circular de 46 dp com borda clara)
                    PersonAvatarBadge(
                        name = group.personName,
                        colorHex = group.colorHex,
                        avatar = group.avatar,
                        size = 46.dp
                    )

                    Column {
                        Text(
                            text = group.personName,
                            style = MaterialTheme.typography.titleMedium,
                            color = liquid.textPrimary
                        )
                        Text(
                            text = "${group.items.size} ${if (group.items.size == 1) "item" else "itens"} neste mês",
                            style = MaterialTheme.typography.bodySmall,
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
                            text = CurrencyFormatter.format(animatedMoney(group.totalAmount), isPrivacyMode),
                            style = MaterialTheme.typography.titleLarge,
                            color = liquid.textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Selo de status (quitado / pendente)
                        // Transição animada pendente <-> quitado (cor + texto), com glow discreto
                        val statusColor by animateColorAsState(
                            if (isAllPaid) liquid.accentPaid else liquid.accentPending,
                            label = "person_status_color"
                        )
                        val statusContainer by animateColorAsState(
                            if (isAllPaid) liquid.accentPaidContainer else liquid.accentPendingContainer,
                            label = "person_status_container"
                        )
                        Box(
                            modifier = Modifier
                                .glow(statusColor, radius = 34.dp) { 0.35f }
                                .clip(LiquidShapes.pill)
                                .background(statusContainer)
                                .border(1.dp, statusColor.copy(alpha = 0.45f), LiquidShapes.pill)
                                .animateContentSize()
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isAllPaid) {
                                    "Quitado"
                                } else {
                                    "Falta: ${CurrencyFormatter.format(animatedMoney(group.pendingAmount), isPrivacyMode)}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Recolher detalhes de ${group.personName}" else "Expandir detalhes de ${group.personName}",
                        tint = liquid.textSecondary,
                        modifier = Modifier
                            .graphicsLayer { rotationZ = chevronRotation.value }
                            .testTag("toggle_expand_person_${group.personId}")
                    )
                }
            }

            // Expanded Item List
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(spring(dampingRatio = 0.85f, stiffness = 380f)) + fadeIn(tween(220, delayMillis = 60)),
                exit = shrinkVertically(spring(dampingRatio = 0.9f, stiffness = 420f)) + fadeOut(tween(140))
            ) {
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
                    // Botão de cobrar em pílula com o gradiente do acento (Figma: botão "Create")
                    val shareInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(LiquidShapes.field)
                            .background(liquid.accentGradient)
                            .border(1.dp, liquid.specularBorder, LiquidShapes.field)
                            .pressScale(shareInteraction, pressedScale = 0.97f)
                            .clickable(
                                interactionSource = shareInteraction,
                                indication = LocalIndication.current,
                                role = Role.Button
                            ) { shareWhatsAppSummary() }
                            .testTag("share_whatsapp_${group.personId}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = liquid.pillPrimaryText
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Cobrar / Enviar Resumo no WhatsApp",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = liquid.pillPrimaryText
                        )
                    }
                }
            }
        }
    }
}
