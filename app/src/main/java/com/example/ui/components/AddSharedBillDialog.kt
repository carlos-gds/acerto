package com.example.ui.components

import com.example.util.MoneyInput
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.BillParticipantShare
import com.example.data.model.MY_PERSON_ID
import com.example.data.model.Person
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.CurrencyFormatter
import kotlin.math.abs
import com.example.ui.components.glass.liquidFilterChipColors
import com.example.ui.components.glass.liquidTextFieldColors
import com.example.ui.theme.LiquidShapes
import com.example.ui.components.glass.LiquidDialogBlur
import com.example.ui.components.glass.liquidDialogSurface
import com.example.ui.components.glass.liquidDialogEnter
import com.example.ui.components.glass.liquidFieldMotion

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSharedBillDialog(
    people: List<Person>,
    currentYear: Int,
    currentMonth: Int,
    onDismiss: () -> Unit,
    onAddPerson: () -> Unit,
    onConfirm: (
        title: String,
        totalAmount: Double,
        participants: List<BillParticipantShare>,
        startYear: Int,
        startMonth: Int,
        isRecurring: Boolean,
        recurrenceMonthsCount: Int,
        notes: String
    ) -> Unit
) {
    val liquid = LocalLiquidGlassColors.current

    var title by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }

    // Participant selection: can include "Eu" or NOT include "Eu", and select 1 or more people
    var includeMe by remember { mutableStateOf(true) }
    val selectedPeopleIds = remember {
        mutableStateMapOf<Long, Boolean>().apply {
            people.firstOrNull()?.let { put(it.id, true) }
        }
    }

    LaunchedEffect(people) {
        // Ensure that if no person is selected and people exist, select the first one
        if (selectedPeopleIds.values.none { it } && people.isNotEmpty()) {
            selectedPeopleIds[people.first().id] = true
        }
    }

    var isEqualSplit by remember { mutableStateOf(true) }
    // Custom amounts per participant (MY_PERSON_ID for "Eu", person.id for others)
    val customShares = remember { mutableStateMapOf<Long, String>() }

    var startMonth by remember { mutableIntStateOf(currentMonth) }
    var startYear by remember { mutableIntStateOf(currentYear) }
    var isRecurring by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val totalAmount = MoneyInput.parse(totalAmountText) ?: 0.0

    val activePeople = people.filter { selectedPeopleIds[it.id] == true }
    val totalParticipantCount = (if (includeMe) 1 else 0) + activePeople.size

    val equalSharePerPerson = if (totalParticipantCount > 0 && totalAmount > 0.0) {
        totalAmount / totalParticipantCount.toDouble()
    } else {
        0.0
    }

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
                        imageVector = Icons.Default.Splitscreen,
                        contentDescription = null,
                        tint = liquid.pillPrimaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Dividir Conta",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = liquid.textPrimary
                    )
                    Text(
                        text = "Selecione com quem dividir e a forma de rateio",
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
                // Bill title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Nome da Conta *", color = liquid.textSecondary) },
                    placeholder = { Text("Ex: Internet Fibra, Conta de Luz, Aluguel", color = liquid.textMuted) },
                    modifier = Modifier.liquidFieldMotion(title)
                        .fillMaxWidth()
                        .testTag("input_bill_title"),
                    singleLine = true,
                    colors = liquidTextFieldColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                // Total amount
                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = {
                        totalAmountText = it
                        errorMessage = null
                    },
                    label = { Text("Valor Total da Conta (R$) *", color = liquid.textSecondary) },
                    placeholder = { Text("Ex: 120,00", color = liquid.textMuted) },
                    modifier = Modifier.liquidFieldMotion(totalAmountText)
                        .fillMaxWidth()
                        .testTag("input_bill_amount"),
                    singleLine = true,
                    colors = liquidTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Participant selection: With whom to split? (Not exclusively "Eu")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(liquid.cardSurfaceVariant)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Com quem vai dividir? *",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = liquid.textPrimary
                            )
                            Text(
                                text = "$totalParticipantCount participante(s) selecionado(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = liquid.textSecondary
                            )
                        }
                        TextButton(
                            onClick = onAddPerson,
                            modifier = Modifier.testTag("add_person_from_bill")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = liquid.textPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Pessoa", fontSize = 12.sp, color = liquid.textPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Option: Include "Eu (Você)" in the split
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                includeMe = !includeMe
                                errorMessage = null
                            }
                            .testTag("bill_include_me"),
                        color = if (includeMe) liquid.pillPrimaryBg.copy(alpha = 0.12f) else liquid.cardSurface,
                        border = BorderStroke(
                            1.dp,
                            if (includeMe) liquid.pillPrimaryBg else liquid.cardBorderSubtle
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (includeMe) liquid.pillPrimaryBg else liquid.circleButtonBg)
                                        .border(1.dp, liquid.cardBorderSubtle, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (includeMe) liquid.pillPrimaryText else liquid.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Eu (Você)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (includeMe) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = liquid.textPrimary
                                    )
                                    Text(
                                        text = if (includeMe) "Participa do rateio desta conta" else "Não participo (divisão entre terceiros)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = liquid.textSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(if (includeMe) liquid.pillPrimaryBg else Color.Transparent)
                                    .border(1.5.dp, if (includeMe) liquid.pillPrimaryBg else liquid.textMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (includeMe) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = liquid.pillPrimaryText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Registered people multi-selection
                    if (people.isEmpty()) {
                        Text(
                            text = "Nenhuma outra pessoa cadastrada. Toque em 'Nova Pessoa' para adicionar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = liquid.textMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            people.forEach { person ->
                                val isSelected = selectedPeopleIds[person.id] == true
                                val personColor = try {
                                    Color(android.graphics.Color.parseColor(person.colorHex))
                                } catch (_: Exception) {
                                    liquid.accentPaid
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedPeopleIds[person.id] = !isSelected
                                            errorMessage = null
                                        }
                                        .testTag("bill_person_${person.id}"),
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
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PersonAvatarBadge(
                                            name = person.name,
                                            colorHex = person.colorHex,
                                            avatar = person.avatar,
                                            size = 24.dp
                                        )

                                        Text(
                                            text = person.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = liquid.textPrimary
                                        )

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = personColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Split mode: Equal Split vs Custom
                Column {
                    Text(
                        text = "Forma de Divisão",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = liquid.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isEqualSplit,
                            onClick = { isEqualSplit = true },
                            label = {
                                Text(
                                    "Divisão Igual (${if (totalParticipantCount > 0) "${(100f / totalParticipantCount).toInt()}% cada" else "Partes iguais"})",
                                    fontWeight = if (isEqualSplit) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = liquidFilterChipColors(),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isEqualSplit,
                                borderColor = liquid.cardBorderSubtle,
                                selectedBorderColor = liquid.pillPrimaryBg
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isEqualSplit,
                            onClick = {
                                isEqualSplit = false
                                // Pre-fill custom values with equal split
                                if (equalSharePerPerson > 0) {
                                    val formatted = "%.2f".format(equalSharePerPerson).replace(".", ",")
                                    if (includeMe) customShares[MY_PERSON_ID] = formatted
                                    activePeople.forEach { customShares[it.id] = formatted }
                                }
                            },
                            label = {
                                Text(
                                    "Personalizado",
                                    fontWeight = if (!isEqualSplit) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = liquidFilterChipColors(),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = !isEqualSplit,
                                borderColor = liquid.cardBorderSubtle,
                                selectedBorderColor = liquid.pillPrimaryBg
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Custom split fields
                if (!isEqualSplit) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(liquid.cardSurfaceVariant)
                            .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Defina o valor de cada participante:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = liquid.textSecondary
                        )

                        if (includeMe) {
                            OutlinedTextField(
                                value = customShares[MY_PERSON_ID] ?: "",
                                onValueChange = { customShares[MY_PERSON_ID] = it },
                                label = { Text("Minha parte (Eu) - R$", color = liquid.textSecondary) },
                                modifier = Modifier.liquidFieldMotion(customShares[MY_PERSON_ID] ?: "").fillMaxWidth(),
                                singleLine = true,
                                colors = liquidTextFieldColors(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        activePeople.forEach { person ->
                            OutlinedTextField(
                                value = customShares[person.id] ?: "",
                                onValueChange = { customShares[person.id] = it },
                                label = { Text("Parte de ${person.name} - R$", color = liquid.textSecondary) },
                                modifier = Modifier.liquidFieldMotion(customShares[person.id] ?: "").fillMaxWidth(),
                                singleLine = true,
                                colors = liquidTextFieldColors(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }

                // Live split preview box
                if (totalAmount > 0 && totalParticipantCount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = liquid.cardSurfaceVariant),
                        border = BorderStroke(1.dp, liquid.cardBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Resumo da Divisão Calculada:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = liquid.textSecondary
                            )

                            if (isEqualSplit) {
                                if (includeMe) {
                                    Text(
                                        text = "• Minha parte (Eu): ${CurrencyFormatter.format(equalSharePerPerson)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = liquid.textPrimary
                                    )
                                }
                                activePeople.forEach { person ->
                                    Text(
                                        text = "• Parte de ${person.name}: ${CurrencyFormatter.format(equalSharePerPerson)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = liquid.accentPaid
                                    )
                                }
                            } else {
                                if (includeMe) {
                                    val myVal = customShares[MY_PERSON_ID]?.let { MoneyInput.parse(it) } ?: 0.0
                                    Text(
                                        text = "• Minha parte (Eu): ${CurrencyFormatter.format(myVal)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = liquid.textPrimary
                                    )
                                }
                                activePeople.forEach { person ->
                                    val pVal = customShares[person.id]?.let { MoneyInput.parse(it) } ?: 0.0
                                    Text(
                                        text = "• Parte de ${person.name}: ${CurrencyFormatter.format(pVal)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = liquid.accentPaid
                                    )
                                }
                            }
                        }
                    }
                }

                // Start Month and Year selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(liquid.cardSurfaceVariant)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mês de Início:",
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
                                    startYear--
                                } else {
                                    startMonth--
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
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
                                    startYear++
                                } else {
                                    startMonth++
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Próximo mês",
                                tint = liquid.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Recurring switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(liquid.cardSurfaceVariant)
                        .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repetir Mensalmente",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = liquid.textPrimary
                        )
                        Text(
                            text = if (isRecurring) "Conta fixa (repete por 12 meses)" else "Cobrança pontual (apenas este mês)",
                            style = MaterialTheme.typography.labelSmall,
                            color = liquid.textSecondary
                        )
                    }
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = liquid.pillPrimaryText,
                            checkedTrackColor = liquid.pillPrimaryBg,
                            uncheckedThumbColor = liquid.textMuted,
                            uncheckedTrackColor = liquid.cardSurface
                        )
                    )
                }

                // Optional notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (opcional)", color = liquid.textSecondary) },
                    placeholder = { Text("Ex: Vence dia 10 todo mês", color = liquid.textMuted) },
                    modifier = Modifier.liquidFieldMotion(notes).fillMaxWidth(),
                    singleLine = true,
                    colors = liquidTextFieldColors()
                )

                // Error message display
                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
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
                    if (totalAmount <= 0.0) {
                        errorMessage = "Informe um valor total válido para a conta."
                        return@Button
                    }
                    if (totalParticipantCount < 2) {
                        errorMessage = "Selecione pelo menos 2 participantes para dividir a conta."
                        return@Button
                    }

                    // Build participants list
                    val participants = mutableListOf<BillParticipantShare>()
                    if (isEqualSplit) {
                        if (includeMe) {
                            participants.add(
                                BillParticipantShare(
                                    personId = MY_PERSON_ID,
                                    personName = "Eu",
                                    amount = equalSharePerPerson,
                                    isMyShare = true
                                )
                            )
                        }
                        activePeople.forEach { person ->
                            participants.add(
                                BillParticipantShare(
                                    personId = person.id,
                                    personName = person.name,
                                    amount = equalSharePerPerson,
                                    isMyShare = false
                                )
                            )
                        }
                    } else {
                        var customSum = 0.0
                        if (includeMe) {
                            val myVal = customShares[MY_PERSON_ID]?.let { MoneyInput.parse(it) } ?: 0.0
                            if (myVal <= 0.0) {
                                errorMessage = "Informe um valor maior que zero para sua parte."
                                return@Button
                            }
                            customSum += myVal
                            participants.add(
                                BillParticipantShare(
                                    personId = MY_PERSON_ID,
                                    personName = "Eu",
                                    amount = myVal,
                                    isMyShare = true
                                )
                            )
                        }
                        for (person in activePeople) {
                            val pVal = customShares[person.id]?.let { MoneyInput.parse(it) } ?: 0.0
                            if (pVal <= 0.0) {
                                errorMessage = "Informe um valor válido para ${person.name}."
                                return@Button
                            }
                            customSum += pVal
                            participants.add(
                                BillParticipantShare(
                                    personId = person.id,
                                    personName = person.name,
                                    amount = pVal,
                                    isMyShare = false
                                )
                            )
                        }

                        if (abs(customSum - totalAmount) > 0.10) {
                            errorMessage = "A soma das partes (${CurrencyFormatter.format(customSum)}) não bate com o valor total (${CurrencyFormatter.format(totalAmount)})."
                            return@Button
                        }
                    }

                    onConfirm(
                        title.trim(),
                        totalAmount,
                        participants,
                        startYear,
                        startMonth,
                        isRecurring,
                        if (isRecurring) 12 else 1,
                        notes.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = liquid.pillPrimaryBg,
                    contentColor = liquid.pillPrimaryText
                ),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.testTag("confirm_add_shared_bill")
            ) {
                Text("Salvar Divisão", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = liquid.textSecondary)
            }
        }
    )
}
