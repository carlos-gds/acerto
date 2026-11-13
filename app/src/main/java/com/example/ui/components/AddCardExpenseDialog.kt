package com.example.ui.components

import com.example.util.MoneyInput
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Person
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.CurrencyFormatter
import com.example.ui.components.glass.liquidFilterChipColors
import com.example.ui.components.glass.liquidTextFieldColors
import com.example.ui.theme.LiquidShapes
import com.example.ui.components.glass.LiquidDialogBlur
import com.example.ui.components.glass.liquidDialogSurface
import com.example.ui.components.glass.liquidDialogEnter
import com.example.ui.components.glass.liquidFieldMotion
import com.example.ui.components.glass.shakeOnChange

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCardExpenseDialog(
    people: List<Person>,
    currentYear: Int,
    currentMonth: Int,
    onDismiss: () -> Unit,
    onAddPerson: () -> Unit,
    onConfirm: (
        title: String,
        totalAmount: Double,
        person: Person,
        totalInstallments: Int,
        startYear: Int,
        startMonth: Int,
        notes: String,
        markPastAsPaid: Boolean
    ) -> Unit
) {
    val liquid = LocalLiquidGlassColors.current

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedPerson by remember { mutableStateOf(people.firstOrNull()) }

    LaunchedEffect(people) {
        if (selectedPerson == null || people.none { it.id == selectedPerson?.id }) {
            selectedPerson = people.firstOrNull()
        }
    }
    var installments by remember { mutableIntStateOf(1) }
    var startMonth by remember { mutableIntStateOf(currentMonth) }
    var startYear by remember { mutableIntStateOf(currentYear) }
    var markPastAsPaid by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isRetroactive = (startYear * 12 + startMonth) < (currentYear * 12 + currentMonth)
    val amountValue = MoneyInput.parse(amountText) ?: 0.0
    val installmentValue = if (installments > 0 && amountValue > 0) amountValue / installments else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = liquidDialogSurface(),
        shape = LiquidShapes.cardLarge,
        modifier = Modifier.liquidDialogEnter().border(1.dp, liquid.specularBorder, LiquidShapes.cardLarge),
        title = {
            LiquidDialogBlur()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(liquid.accentGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = liquid.pillPrimaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Compra no Meu Cartão",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = liquid.textPrimary
                    )
                    Text(
                        text = "Alguém comprou usando seu cartão de crédito",
                        style = MaterialTheme.typography.labelSmall,
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Description of what was bought
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("O que a pessoa comprou? *", color = liquid.textSecondary) },
                    placeholder = { Text("Ex: Tênis Nike, Celular, Airfryer", color = liquid.textMuted) },
                    modifier = Modifier.liquidFieldMotion(title)
                        .fillMaxWidth()
                        .testTag("input_expense_title"),
                    singleLine = true,
                    colors = liquidTextFieldColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                // Person selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(liquid.cardSurfaceVariant)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quem comprou? *",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = liquid.textPrimary
                        )
                        TextButton(
                            onClick = onAddPerson,
                            modifier = Modifier.testTag("add_new_person_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = liquid.textPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Pessoa", fontSize = 12.sp, color = liquid.textPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (people.isEmpty()) {
                        Text(
                            text = "Nenhuma pessoa cadastrada. Cadastre uma acima.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            people.forEach { person ->
                                val isSelected = selectedPerson?.id == person.id
                                val personColor = try {
                                    Color(android.graphics.Color.parseColor(person.colorHex))
                                } catch (_: Exception) {
                                    liquid.accentPaid
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedPerson = person }
                                        .testTag("select_person_${person.id}"),
                                    color = if (isSelected) personColor.copy(alpha = 0.18f) else liquid.cardSurface,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelected) personColor else liquid.cardBorderSubtle
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        PersonAvatarBadge(
                                            name = person.name,
                                            colorHex = person.colorHex,
                                            avatar = person.avatar,
                                            size = 22.dp
                                        )
                                        Text(
                                            text = person.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = liquid.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Valor Total (R$) *", color = liquid.textSecondary) },
                    placeholder = { Text("Ex: 300,00", color = liquid.textMuted) },
                    modifier = Modifier.liquidFieldMotion(amountText)
                        .fillMaxWidth()
                        .testTag("input_expense_amount"),
                    singleLine = true,
                    colors = liquidTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Installments (Parcelas)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(liquid.cardSurfaceVariant)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Número de Parcelas",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = liquid.textPrimary
                        )
                        Text(
                            text = "${installments}x de ${CurrencyFormatter.format(installmentValue)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = liquid.accentPaid
                            )
                        )
                    }

                    Slider(
                        value = installments.toFloat(),
                        onValueChange = { installments = it.toInt() },
                        valueRange = 1f..24f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = liquid.pillPrimaryBg,
                            activeTrackColor = liquid.pillPrimaryBg,
                            inactiveTrackColor = liquid.cardBorderSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slider_installments")
                    )

                    // Quick buttons for common installments (1x, 2x, 3x, 6x, 10x, 12x)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(1, 2, 3, 6, 10, 12).forEach { num ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { installments = num },
                                color = if (installments == num) liquid.pillPrimaryBg else liquid.cardSurface,
                                border = BorderStroke(1.dp, if (installments == num) liquid.pillPrimaryBg else liquid.cardBorderSubtle),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${num}x",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (installments == num) liquid.pillPrimaryText else liquid.textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Start Month and Year (Lançamento Retroativo)
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
                            text = "Mês/Ano de Início:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = liquid.textPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (startMonth == 1) {
                                        startMonth = 12
                                        startYear -= 1
                                    } else {
                                        startMonth -= 1
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Mês anterior",
                                    tint = liquid.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${CurrencyFormatter.getMonthName(startMonth)} / $startYear",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = liquid.textPrimary
                            )

                            IconButton(
                                onClick = {
                                    if (startMonth == 12) {
                                        startMonth = 1
                                        startYear += 1
                                    } else {
                                        startMonth += 1
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Próximo mês",
                                    tint = liquid.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (isRetroactive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(liquid.cardSurface)
                                .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(10.dp))
                                .clickable { markPastAsPaid = !markPastAsPaid }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = markPastAsPaid,
                                onCheckedChange = { markPastAsPaid = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = liquid.pillPrimaryBg,
                                    checkmarkColor = liquid.pillPrimaryText,
                                    uncheckedColor = liquid.textMuted
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Marcar parcelas anteriores a ${CurrencyFormatter.getMonthShortName(currentMonth)}/$currentYear como já pagas",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = liquid.textSecondary
                            )
                        }
                    }
                }

                // Notes (Optional)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observação (opcional)", color = liquid.textSecondary) },
                    placeholder = { Text("Ex: Compra do aniversário", color = liquid.textMuted) },
                    modifier = Modifier.liquidFieldMotion(notes).fillMaxWidth(),
                    singleLine = true,
                    colors = liquidTextFieldColors()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.shakeOnChange(errorMessage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Informe o que a pessoa comprou."
                        return@Button
                    }
                    if (selectedPerson == null) {
                        errorMessage = "Selecione a pessoa que realizou a compra."
                        return@Button
                    }
                    if (amountValue <= 0.0) {
                        errorMessage = "Informe um valor maior que zero."
                        return@Button
                    }
                    onConfirm(
                        title.trim(),
                        amountValue,
                        selectedPerson!!,
                        installments,
                        startYear,
                        startMonth,
                        notes.trim(),
                        markPastAsPaid
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = liquid.pillPrimaryBg,
                    contentColor = liquid.pillPrimaryText
                ),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.testTag("confirm_add_expense_button")
            ) {
                Text("Salvar Compra", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = liquid.textSecondary)
            }
        }
    )
}
