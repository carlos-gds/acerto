package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.model.Expense
import com.example.data.model.InstallmentItem
import com.example.data.model.Person
import com.example.util.Money
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSharedBillDialog(
    item: InstallmentItem,
    people: List<Person>,
    selectedYear: Int,
    selectedMonth: Int,
    onLoadExpense: suspend (Long) -> Expense?,
    onDismiss: () -> Unit,
    onConfirm: (
        expenseId: Long,
        newTitle: String,
        newTotalAmount: Double,
        newPerson: Person,
        newMyShare: Double,
        newOtherShare: Double,
        updateAllMonths: Boolean
    ) -> Unit
) {
    val liquid = LocalLiquidGlassColors.current

    var loadedExpense by remember { mutableStateOf<Expense?>(null) }
    var title by remember { mutableStateOf(item.expenseTitle) }
    var totalAmountText by remember { mutableStateOf("") }
    var isFiftyFifty by remember { mutableStateOf(true) }
    var customMyShareText by remember { mutableStateOf("") }
    var customOtherShareText by remember { mutableStateOf("") }
    var selectedPerson by remember {
        mutableStateOf(people.find { it.id == item.personId } ?: people.firstOrNull())
    }
    var updateAllMonths by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item.expenseId) {
        val exp = onLoadExpense(item.expenseId)
        loadedExpense = exp
        if (exp != null) {
            title = exp.title
            val total = Money.toReal(exp.totalAmount)
            totalAmountText = String.format(java.util.Locale.US, "%.2f", total).replace(".", ",")
            selectedPerson = people.find { it.id == exp.personId } ?: people.firstOrNull()

            val myShare = if (exp.myShareAmount > 0) Money.toReal(exp.myShareAmount) else (total / 2.0)
            val otherShare = if (exp.otherShareAmount > 0) Money.toReal(exp.otherShareAmount) else (total - myShare)

            val isHalfHalf = Math.abs(myShare - otherShare) < 0.05
            isFiftyFifty = isHalfHalf

            customMyShareText = String.format(java.util.Locale.US, "%.2f", myShare).replace(".", ",")
            customOtherShareText = String.format(java.util.Locale.US, "%.2f", otherShare).replace(".", ",")
        } else {
            // Fallback from item
            val estimatedTotal = Money.toReal(item.amount) * 2.0
            totalAmountText = String.format(java.util.Locale.US, "%.2f", estimatedTotal).replace(".", ",")
            customMyShareText = String.format(java.util.Locale.US, "%.2f", Money.toReal(item.amount)).replace(".", ",")
            customOtherShareText = String.format(java.util.Locale.US, "%.2f", Money.toReal(item.amount)).replace(".", ",")
            isFiftyFifty = true
        }
    }

    val numericTotal = totalAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0

    val myShareAmount: Double = if (isFiftyFifty) {
        numericTotal / 2.0
    } else {
        customMyShareText.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    val otherShareAmount: Double = if (isFiftyFifty) {
        numericTotal / 2.0
    } else {
        customOtherShareText.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = liquid.cardSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(liquid.pillPrimaryBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Splitscreen,
                        contentDescription = null,
                        tint = liquid.pillPrimaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Editar Conta Dividida",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = liquid.textPrimary
                    )
                    Text(
                        text = "Ajuste o valor total ou a sua parte da conta",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome da Conta") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_shared_bill_title_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = liquid.cardSurfaceVariant,
                        unfocusedContainerColor = liquid.cardSurfaceVariant,
                        focusedTextColor = liquid.textPrimary,
                        unfocusedTextColor = liquid.textPrimary
                    )
                )

                // Person selector
                Column {
                    Text(
                        text = "Dividir com:",
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
                                modifier = Modifier
                                    .clickable { selectedPerson = person }
                                    .testTag("select_person_${person.id}")
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

                // Total Amount
                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { input ->
                        totalAmountText = input
                        val newTot = input.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (isFiftyFifty) {
                            val half = newTot / 2.0
                            customMyShareText = String.format(java.util.Locale.US, "%.2f", half).replace(".", ",")
                            customOtherShareText = String.format(java.util.Locale.US, "%.2f", half).replace(".", ",")
                        }
                    },
                    label = { Text("Valor Total da Conta (R$)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_shared_bill_total_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = liquid.cardSurfaceVariant,
                        unfocusedContainerColor = liquid.cardSurfaceVariant,
                        focusedTextColor = liquid.textPrimary,
                        unfocusedTextColor = liquid.textPrimary
                    )
                )

                // Split Mode Chips
                Column {
                    Text(
                        text = "Divisão da Conta:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = liquid.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = isFiftyFifty,
                            onClick = {
                                isFiftyFifty = true
                                val half = numericTotal / 2.0
                                customMyShareText = String.format(java.util.Locale.US, "%.2f", half).replace(".", ",")
                                customOtherShareText = String.format(java.util.Locale.US, "%.2f", half).replace(".", ",")
                            },
                            label = { Text("50% / 50% (Meio a meio)") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = liquid.pillPrimaryBg,
                                selectedLabelColor = liquid.pillPrimaryText
                            ),
                            modifier = Modifier.testTag("split_mode_50_50")
                        )

                        FilterChip(
                            selected = !isFiftyFifty,
                            onClick = {
                                isFiftyFifty = false
                            },
                            label = { Text("Personalizado") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = liquid.pillPrimaryBg,
                                selectedLabelColor = liquid.pillPrimaryText
                            ),
                            modifier = Modifier.testTag("split_mode_custom")
                        )
                    }
                }

                // Share Inputs & Visualizer
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = liquid.cardSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isFiftyFifty) {
                            // 50/50 Display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Minha Parte (Você)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = liquid.textPrimary
                                    )
                                    Text(
                                        text = "50% da conta",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = liquid.textSecondary
                                    )
                                }
                                Text(
                                    text = CurrencyFormatter.format(myShareAmount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = liquid.pillPrimaryBg
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Parte de ${selectedPerson?.name ?: "Outro"}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = liquid.textPrimary
                                    )
                                    Text(
                                        text = "50% da conta",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = liquid.textSecondary
                                    )
                                }
                                Text(
                                    text = CurrencyFormatter.format(otherShareAmount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = liquid.accentPending
                                )
                            }
                        } else {
                            // Custom Inputs
                            Text(
                                text = "Defina os valores individuais:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = liquid.textSecondary
                            )

                            // My Share Input
                            OutlinedTextField(
                                value = customMyShareText,
                                onValueChange = { text ->
                                    customMyShareText = text
                                    val myVal = text.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    if (numericTotal > 0 && myVal <= numericTotal) {
                                        val otherVal = numericTotal - myVal
                                        customOtherShareText = String.format(java.util.Locale.US, "%.2f", otherVal).replace(".", ",")
                                    }
                                },
                                label = { Text("Minha Parte (R$)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_my_share_amount_input"),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = liquid.cardSurface,
                                    unfocusedContainerColor = liquid.cardSurface,
                                    focusedTextColor = liquid.textPrimary,
                                    unfocusedTextColor = liquid.textPrimary
                                )
                            )

                            // Other Share Input
                            OutlinedTextField(
                                value = customOtherShareText,
                                onValueChange = { text ->
                                    customOtherShareText = text
                                    val otherVal = text.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    if (numericTotal > 0 && otherVal <= numericTotal) {
                                        val myVal = numericTotal - otherVal
                                        customMyShareText = String.format(java.util.Locale.US, "%.2f", myVal).replace(".", ",")
                                    }
                                },
                                label = { Text("Parte de ${selectedPerson?.name ?: "Outro"} (R$)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_other_share_amount_input"),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = liquid.cardSurface,
                                    unfocusedContainerColor = liquid.cardSurface,
                                    focusedTextColor = liquid.textPrimary,
                                    unfocusedTextColor = liquid.textPrimary
                                )
                            )
                        }

                        // Proportional Visual Bar
                        val sumShares = myShareAmount + otherShareAmount
                        if (sumShares > 0) {
                            val myRatio = (myShareAmount / sumShares).toFloat().coerceIn(0f, 1f)
                            val otherRatio = 1f - myRatio

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(liquid.cardBorderSubtle)
                                ) {
                                    if (myRatio > 0.01f) {
                                        Box(
                                            modifier = Modifier
                                                .weight(myRatio)
                                                .height(10.dp)
                                                .background(liquid.pillPrimaryBg)
                                        )
                                    }
                                    if (otherRatio > 0.01f) {
                                        Box(
                                            modifier = Modifier
                                                .weight(otherRatio)
                                                .height(10.dp)
                                                .background(liquid.accentPending)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Você: ${(myRatio * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = liquid.textSecondary
                                    )
                                    Text(
                                        text = "${selectedPerson?.name ?: "Outro"}: ${(otherRatio * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = liquid.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Scope: All months vs. Only this month
                Column {
                    Text(
                        text = "Onde aplicar a alteração:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = liquid.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = liquid.cardSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (updateAllMonths) liquid.cardBorder else liquid.cardBorderSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { updateAllMonths = true }
                            .testTag("scope_all_months")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = updateAllMonths,
                                onClick = { updateAllMonths = true },
                                colors = RadioButtonDefaults.colors(selectedColor = liquid.pillPrimaryBg)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Todas as faturas desta conta",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = liquid.textPrimary
                                )
                                Text(
                                    text = "Atualiza o valor fixo recorrente para todos os meses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = liquid.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = liquid.cardSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (!updateAllMonths) liquid.cardBorder else liquid.cardBorderSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { updateAllMonths = false }
                            .testTag("scope_single_month")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !updateAllMonths,
                                onClick = { updateAllMonths = false },
                                colors = RadioButtonDefaults.colors(selectedColor = liquid.pillPrimaryBg)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Apenas na fatura de ${CurrencyFormatter.getMonthShortName(selectedMonth)}/$selectedYear",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = liquid.textPrimary
                                )
                                Text(
                                    text = "Ideal para faturas com variação pontual de consumo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = liquid.textSecondary
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
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
                        errorMessage = "Informe o nome da conta."
                        return@Button
                    }
                    if (numericTotal <= 0) {
                        errorMessage = "Informe um valor total maior que zero."
                        return@Button
                    }
                    if (selectedPerson == null) {
                        errorMessage = "Selecione com quem a conta é dividida."
                        return@Button
                    }
                    if (myShareAmount < 0 || otherShareAmount < 0) {
                        errorMessage = "Os valores das partes não podem ser negativos."
                        return@Button
                    }
                    if (myShareAmount == 0.0 && otherShareAmount == 0.0) {
                        errorMessage = "Defina ao menos uma parte com valor maior que zero."
                        return@Button
                    }

                    onConfirm(
                        item.expenseId,
                        title.trim(),
                        numericTotal,
                        selectedPerson!!,
                        myShareAmount,
                        otherShareAmount,
                        updateAllMonths
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = liquid.pillPrimaryBg),
                modifier = Modifier.testTag("save_edit_shared_bill_btn")
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
