package com.example.ui.components

import com.example.util.MoneyInput
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.Person
import com.example.util.Money
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
fun EditExpenseDialog(
    item: InstallmentItem,
    people: List<Person>,
    onDismiss: () -> Unit,
    onConfirm: (
        expenseId: Long,
        newTitle: String,
        newTotalAmount: Double,
        newPerson: Person,
        newTotalInstallments: Int,
        startYear: Int,
        startMonth: Int
    ) -> Unit
) {
    val liquid = LocalLiquidGlassColors.current

    val initialTotalAmount = if (item.expenseType == ExpenseType.CARD_PURCHASE) {
        Money.toReal(item.amount * item.totalInstallments.toLong())
    } else {
        Money.toReal(item.amount)
    }

    // Calculate start month and year of original purchase
    val initialStartTotalMonths = (item.year * 12 + (item.month - 1)) - (item.installmentNumber - 1)
    val initialStartYear = initialStartTotalMonths / 12
    val initialStartMonth = (initialStartTotalMonths % 12) + 1

    var title by remember { mutableStateOf(item.expenseTitle) }
    var totalAmountText by remember { mutableStateOf(String.format(java.util.Locale.US, "%.2f", initialTotalAmount).replace(".", ",")) }
    var installments by remember { mutableIntStateOf(item.totalInstallments) }
    var selectedPerson by remember {
        mutableStateOf(people.find { it.id == item.personId } ?: people.firstOrNull())
    }
    var startMonth by remember { mutableIntStateOf(initialStartMonth) }
    var startYear by remember { mutableIntStateOf(initialStartYear) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        .background(liquid.accentGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = liquid.pillPrimaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Editar Dívida / Gasto",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = liquid.textPrimary
                    )
                    Text(
                        text = "Altere o valor, parcelas, pessoa ou título",
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
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Descrição / Título") },
                    modifier = Modifier.liquidFieldMotion(title)
                        .fillMaxWidth()
                        .testTag("edit_expense_title_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = liquidTextFieldColors()
                )

                // Total Amount
                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it },
                    label = { Text(if (item.expenseType == ExpenseType.CARD_PURCHASE) "Valor Total da Compra (R$)" else "Valor (R$)") },
                    modifier = Modifier.liquidFieldMotion(totalAmountText)
                        .fillMaxWidth()
                        .testTag("edit_expense_amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = liquidTextFieldColors()
                )

                // Person selector
                Column {
                    Text(
                        text = "Responsável pela dívida:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = liquid.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        people.forEach { person ->
                            val isSelected = selectedPerson?.id == person.id
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) liquid.pillPrimaryBg else liquid.cardSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) liquid.cardBorder else liquid.cardBorderSubtle
                                ),
                                modifier = Modifier.clickable { selectedPerson = person }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) liquid.pillPrimaryText else liquid.textSecondary
                                    )
                                    Text(
                                        text = person.name,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) liquid.pillPrimaryText else liquid.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Installments (if CARD_PURCHASE)
                if (item.expenseType == ExpenseType.CARD_PURCHASE) {
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
                                text = "Número de parcelas:",
                                style = MaterialTheme.typography.labelMedium,
                                color = liquid.textSecondary
                            )
                            Text(
                                text = "${installments}x de ${CurrencyFormatter.format(installmentAmount)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = liquid.accentPaid
                                )
                            )
                        }

                        var sliderVal by remember { mutableFloatStateOf(installments.toFloat()) }
                        Slider(
                            value = sliderVal,
                            onValueChange = {
                                sliderVal = it
                                installments = it.toInt().coerceIn(1, 24)
                            },
                            valueRange = 1f..24f,
                            steps = 22,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

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
                        errorMessage = "Informe o título da despesa."
                        return@Button
                    }
                    if (numericAmount <= 0) {
                        errorMessage = "Informe um valor maior que zero."
                        return@Button
                    }
                    if (selectedPerson == null) {
                        errorMessage = "Selecione uma pessoa."
                        return@Button
                    }
                    onConfirm(
                        item.expenseId,
                        title.trim(),
                        numericAmount,
                        selectedPerson!!,
                        installments,
                        startYear,
                        startMonth
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = liquid.pillPrimaryBg),
                modifier = Modifier.testTag("save_edit_expense_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = liquid.pillPrimaryText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salvar Alterações", color = liquid.pillPrimaryText)
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
