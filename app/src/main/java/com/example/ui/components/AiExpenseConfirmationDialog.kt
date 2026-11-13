package com.example.ui.components

import com.example.util.MoneyInput
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuickExpenseAiResponse
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.CurrencyFormatter
import com.example.ui.components.glass.liquidFilterChipColors
import com.example.ui.components.glass.liquidTextFieldColors
import com.example.ui.theme.LiquidShapes
import com.example.ui.components.glass.LiquidDialogBlur
import com.example.ui.components.glass.liquidDialogSurface
import com.example.ui.components.glass.liquidDialogEnter
import com.example.ui.components.glass.liquidFieldMotion

@Composable
fun AiExpenseConfirmationDialog(
    initialData: QuickExpenseAiResponse,
    currentYear: Int,
    currentMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (description: String, totalAmount: Double, installments: Int, targetPerson: String, startYear: Int, startMonth: Int, markPastAsPaid: Boolean) -> Unit
) {
    val liquid = LocalLiquidGlassColors.current

    var description by remember { mutableStateOf(initialData.description) }
    var totalAmountText by remember { mutableStateOf("%.2f".format(initialData.total_amount).replace(".", ",")) }
    var installments by remember { mutableIntStateOf(initialData.installments.coerceIn(1, 48)) }
    var targetPerson by remember { mutableStateOf(initialData.target_person) }
    var selectedMonth by remember { mutableIntStateOf(initialData.start_month ?: currentMonth) }
    var selectedYear by remember { mutableIntStateOf(initialData.start_year ?: currentYear) }
    var markPastAsPaid by remember { mutableStateOf(true) }

    val isRetroactive = (selectedYear * 12 + selectedMonth) < (currentYear * 12 + currentMonth)
    val numericAmount = MoneyInput.parse(totalAmountText) ?: 0.0
    val installmentAmount = if (installments > 0) numericAmount / installments else 0.0

    AlertDialog(
        modifier = Modifier.liquidDialogEnter().border(1.dp, liquid.specularBorder, LiquidShapes.cardLarge),
        onDismissRequest = onDismiss,
        containerColor = liquidDialogSurface(),
        shape = LiquidShapes.cardLarge,
        title = {
            LiquidDialogBlur()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            liquid.accentGradient
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "IA",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Confirmar Lançamento IA",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = liquid.textPrimary
                    )
                    Text(
                        text = "Valores identificados pelo Gemini",
                        fontSize = 11.sp,
                        color = liquid.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Título") },
                    modifier = Modifier.liquidFieldMotion(description)
                        .fillMaxWidth()
                        .testTag("ai_confirm_desc_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = liquidTextFieldColors()
                )

                // Total Amount field
                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it },
                    label = { Text("Valor Total (R$)") },
                    modifier = Modifier.liquidFieldMotion(totalAmountText)
                        .fillMaxWidth()
                        .testTag("ai_confirm_amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = liquidTextFieldColors()
                )

                // Target Person field
                OutlinedTextField(
                    value = targetPerson,
                    onValueChange = { targetPerson = it },
                    label = { Text("Pessoa / Contato") },
                    modifier = Modifier.liquidFieldMotion(targetPerson)
                        .fillMaxWidth()
                        .testTag("ai_confirm_person_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = liquid.accent
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = liquidTextFieldColors()
                )

                // Installment Slider & Counter
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(liquid.cardSurfaceVariant)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parcelas:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = liquid.textSecondary
                        )
                        Text(
                            text = "${installments}x de ${CurrencyFormatter.format(installmentAmount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = liquid.accent
                        )
                    }

                    var sliderPosition by remember { mutableFloatStateOf(installments.toFloat()) }
                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
                            installments = it.toInt().coerceIn(1, 24)
                        },
                        valueRange = 1f..24f,
                        steps = 22,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Initial month selector & Retroactive indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(liquid.cardSurfaceVariant)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Início das parcelas:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = liquid.textSecondary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    if (selectedMonth == 1) {
                                        selectedMonth = 12
                                        selectedYear -= 1
                                    } else {
                                        selectedMonth -= 1
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Mês anterior",
                                    tint = liquid.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "${CurrencyFormatter.getMonthShortName(selectedMonth)} / $selectedYear",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = liquid.accent
                            )

                            androidx.compose.material3.IconButton(
                                onClick = {
                                    if (selectedMonth == 12) {
                                        selectedMonth = 1
                                        selectedYear += 1
                                    } else {
                                        selectedMonth += 1
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Próximo mês",
                                    tint = liquid.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (isRetroactive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(liquid.accent.copy(alpha = 0.12f))
                                .clickable { markPastAsPaid = !markPastAsPaid }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = markPastAsPaid,
                                onCheckedChange = { markPastAsPaid = it },
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Marcar parcelas anteriores a ${CurrencyFormatter.getMonthShortName(currentMonth)}/$currentYear como já pagas",
                                fontSize = 11.sp,
                                color = liquid.textPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && numericAmount > 0 && targetPerson.isNotBlank()) {
                        onConfirm(
                            description.trim(),
                            numericAmount,
                            installments,
                            targetPerson.trim(),
                            selectedYear,
                            selectedMonth,
                            markPastAsPaid
                        )
                    }
                },
                enabled = description.isNotBlank() && numericAmount > 0 && targetPerson.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = liquid.accent
                ),
                modifier = Modifier.testTag("ai_confirm_save_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirmar e Salvar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancelar", color = liquid.textSecondary)
            }
        }
    )
}
