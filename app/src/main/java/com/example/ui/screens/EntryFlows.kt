package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.ui.components.glass.pressScale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillParticipantShare
import com.example.data.model.MY_PERSON_ID
import com.example.data.model.Person
import com.example.ui.components.NoirRoundButton
import com.example.ui.components.PersonAvatarBadge
import com.example.ui.components.StatusBarIcons
import com.example.ui.components.glass.AcertoMotion
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.components.noirHeaderBackground
import com.example.ui.components.glass.shakeOnChange
import com.example.ui.theme.AcertoPalette
import com.example.ui.theme.LiquidDarkTokens
import com.example.ui.theme.LiquidLightTokens
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.Calculator
import com.example.util.CurrencyFormatter
import kotlin.math.abs

/*
 * Lançamentos em etapas, no estilo da referência:
 *  - Nova compra:   Quem comprou (grade escura) → Valor e parcelas (teclado) → Detalhes → sucesso
 *  - Dividir conta: Valor (teclado) → Com quem (grade escura) → Detalhes → sucesso
 * A folha branca sobe por cima da grade; entre as etapas da folha o conteúdo
 * desliza para o lado (para a frente ou para trás).
 */

private val Ink = Color(0xFF16181A)
private val InkMuted = Color(0xFF63666B)
private val Soft = Color(0xFFF2F2F3)
private val Line = Color(0xFFE4E4E7)

/* =========================== Nova compra =========================== */

private enum class PurchaseStep { WHO, AMOUNT, DETAILS, DONE }

@Composable
fun NewPurchaseFlow(
    people: List<Person>,
    currentYear: Int,
    currentMonth: Int,
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
    ) -> Unit,
    onClose: () -> Unit,
    /** Pessoa já escolhida: abre direto no valor. */
    initialPersonId: Long? = null
) {
    var step by rememberSaveable { mutableStateOf(if (initialPersonId != null) PurchaseStep.AMOUNT else PurchaseStep.WHO) }
    var personId by rememberSaveable { mutableStateOf(initialPersonId) }
    var expr by rememberSaveable { mutableStateOf("") }
    var installments by rememberSaveable { mutableIntStateOf(1) }
    var title by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var startYear by rememberSaveable { mutableIntStateOf(currentYear) }
    var startMonth by rememberSaveable { mutableIntStateOf(currentMonth) }
    var markPast by rememberSaveable { mutableStateOf(true) }
    val person = people.firstOrNull { it.id == personId }
    val total = Calculator.evaluate(expr) ?: 0.0

    BackHandler {
        step = when (step) {
            PurchaseStep.DETAILS -> PurchaseStep.AMOUNT
            PurchaseStep.AMOUNT -> PurchaseStep.WHO
            else -> { onClose(); step }
        }
    }

    FlowScaffold(
        sheetVisible = step == PurchaseStep.AMOUNT || step == PurchaseStep.DETAILS,
        done = step == PurchaseStep.DONE,
        background = {
            PeopleGrid(
                title = "Quem comprou?",
                subtitle = "Compra no seu cartão",
                people = people,
                isSelected = { it == personId },
                multi = false,
                onTap = { id -> personId = id; step = PurchaseStep.AMOUNT },
                onAddPerson = onAddPerson,
                leading = { NoirRoundButton(icon = Icons.Default.Close, contentDescription = "Fechar", onClick = onClose, modifier = Modifier.testTag("purchase_close")) },
                tagPrefix = "purchase_person_"
            )
        },
        sheetStep = step,
        sheet = { s ->
            when (s) {
                PurchaseStep.DETAILS -> PurchaseDetails(
                    person = person,
                    total = total,
                    installments = installments,
                    title = title, onTitle = { title = it },
                    notes = notes, onNotes = { notes = it },
                    startYear = startYear, startMonth = startMonth,
                    onMonth = { y, m -> startYear = y; startMonth = m },
                    currentYear = currentYear, currentMonth = currentMonth,
                    markPast = markPast, onMarkPast = { markPast = it },
                    onBack = { step = PurchaseStep.AMOUNT },
                    onSave = {
                        val p = person ?: return@PurchaseDetails
                        onConfirm(title.trim(), total, p, installments, startYear, startMonth, notes.trim(), markPast)
                        step = PurchaseStep.DONE
                    }
                )
                else -> AmountStep(
                    heading = person?.let { "Compra de ${it.name}" } ?: "Valor da compra",
                    person = person,
                    expr = expr,
                    onExpr = { expr = it },
                    onBack = { step = PurchaseStep.WHO },
                    tagPrefix = "purchase_",
                    extra = { value ->
                        InstallmentPicker(total = value, selected = installments, onSelect = { installments = it })
                    },
                    onNext = { step = PurchaseStep.DETAILS }
                )
            }
        },
        success = {
            SuccessScreen(
                amount = total,
                name = person?.name.orEmpty(),
                onClose = onClose,
                subtitle = if (installments > 1) "${installments}x · compra de ${person?.name.orEmpty()}" else "Compra de ${person?.name.orEmpty()}"
            )
        }
    )
}

@Composable
private fun InstallmentPicker(total: Double, selected: Int, onSelect: (Int) -> Unit) {
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (selected - 3).coerceAtLeast(0))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            when {
                total <= 0.0 -> "Em quantas vezes?"
                selected == 1 -> "À vista"
                else -> "${selected}x de ${CurrencyFormatter.format(total / selected)}"
            },
            style = MaterialTheme.typography.titleMedium,
            color = InkMuted,
            modifier = Modifier.testTag("purchase_installment_summary")
        )
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 10.dp).testTag("purchase_installments")
        ) {
            items((1..24).toList(), key = { it }) { n ->
                val on = n == selected
                val bg by androidx.compose.animation.animateColorAsState(if (on) Ink else Soft, tween(AcertoMotion.QUICK), label = "inst_bg")
                val fg by androidx.compose.animation.animateColorAsState(if (on) Color.White else Ink, tween(AcertoMotion.QUICK), label = "inst_fg")
                Box(
                    Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(bg)
                        .clickable(role = Role.RadioButton, onClickLabel = "${n} parcelas") {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(n)
                        }
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                        .testTag("purchase_installment_$n"),
                    contentAlignment = Alignment.Center
                ) { Text("${n}x", style = MaterialTheme.typography.titleSmall, color = fg) }
            }
        }
    }
}

@Composable
private fun PurchaseDetails(
    person: Person?,
    total: Double,
    installments: Int,
    title: String,
    onTitle: (String) -> Unit,
    notes: String,
    onNotes: (String) -> Unit,
    startYear: Int,
    startMonth: Int,
    onMonth: (Int, Int) -> Unit,
    currentYear: Int,
    currentMonth: Int,
    markPast: Boolean,
    onMarkPast: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val retro = startYear * 12 + startMonth < currentYear * 12 + currentMonth
    DetailsColumn(heading = "O que comprou?", onBack = onBack) {
        SummaryCard(
            person = person,
            title = person?.name ?: "",
            line = CurrencyFormatter.format(total) + if (installments > 1) " · ${installments}x de ${CurrencyFormatter.format(total / installments)}" else " · à vista",
            onClick = onBack
        )
        BigTextField(
            value = title,
            onValue = onTitle,
            placeholder = "Ex: Tênis, celular, mercado",
            tag = "purchase_title"
        )
        MonthStepper(label = "Primeira parcela", year = startYear, month = startMonth, onChange = onMonth)
        if (retro) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Soft)
                    .clickable(role = Role.Checkbox) { onMarkPast(!markPast) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Checkbox(
                    checked = markPast,
                    onCheckedChange = onMarkPast,
                    colors = CheckboxDefaults.colors(checkedColor = Ink, checkmarkColor = Color.White, uncheckedColor = InkMuted)
                )
                Text(
                    "Marcar as parcelas antes de ${CurrencyFormatter.getMonthShortName(currentMonth).lowercase()}/$currentYear como pagas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink
                )
            }
        }
        OutlinedTextField(
            value = notes,
            onValueChange = onNotes,
            label = { Text("Observação (opcional)") },
            singleLine = true,
            colors = sheetFieldColors(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        SaveSwipe(enabled = title.isNotBlank(), onSave = onSave)
    }
}

/* =========================== Dividir conta =========================== */

private enum class SplitStep { AMOUNT, WHO, DETAILS, DONE }

@Composable
fun SplitBillFlow(
    people: List<Person>,
    currentYear: Int,
    currentMonth: Int,
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
    ) -> Unit,
    onClose: () -> Unit
) {
    var step by rememberSaveable { mutableStateOf(SplitStep.AMOUNT) }
    var expr by rememberSaveable { mutableStateOf("") }
    var includeMe by rememberSaveable { mutableStateOf(true) }
    val selected = remember { mutableStateMapOf<Long, Boolean>().apply { people.firstOrNull()?.let { put(it.id, true) } } }
    var equal by rememberSaveable { mutableStateOf(true) }
    val custom = remember { mutableStateMapOf<Long, Long>() } // centavos por participante (MY_PERSON_ID = eu)
    var title by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var startYear by rememberSaveable { mutableIntStateOf(currentYear) }
    var startMonth by rememberSaveable { mutableIntStateOf(currentMonth) }
    var recurring by rememberSaveable { mutableStateOf(true) }

    val total = Calculator.evaluate(expr) ?: 0.0
    val totalCents = Math.round(total * 100)
    val active = people.filter { selected[it.id] == true }
    val participantIds = (if (includeMe) listOf(MY_PERSON_ID) else emptyList()) + active.map { it.id }
    val count = participantIds.size
    val customSum = participantIds.sumOf { custom[it] ?: 0L }
    val sharesOk = equal || (participantIds.all { (custom[it] ?: 0L) > 0L } && abs(customSum - totalCents) <= 10)
    val whoOk = count >= 2 && sharesOk

    // Ao trocar para "Personalizado", começa com a divisão igual (o resto vai para o último)
    fun prefillCustom() {
        if (count == 0 || totalCents <= 0) return
        val base = totalCents / count
        participantIds.forEachIndexed { i, id -> custom[id] = if (i == count - 1) totalCents - base * (count - 1) else base }
    }

    BackHandler {
        step = when (step) {
            SplitStep.DETAILS -> SplitStep.WHO
            SplitStep.WHO -> SplitStep.AMOUNT
            else -> { onClose(); step }
        }
    }

    FlowScaffold(
        sheetVisible = step == SplitStep.AMOUNT || step == SplitStep.DETAILS,
        done = step == SplitStep.DONE,
        background = {
            PeopleGrid(
                title = "Com quem dividir?",
                subtitle = if (total > 0) "Conta de ${CurrencyFormatter.format(total)}" else null,
                people = people,
                isSelected = { selected[it] == true },
                multi = true,
                onTap = { id -> selected[id] = selected[id] != true },
                onAddPerson = onAddPerson,
                me = MeTile(selected = includeMe, onToggle = { includeMe = !includeMe }),
                leading = {
                    NoirRoundButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", onClick = { step = SplitStep.AMOUNT })
                },
                tagPrefix = "split_person_",
                bottom = {
                    SplitPanel(
                        people = active,
                        includeMe = includeMe,
                        total = total,
                        equal = equal,
                        onEqual = { e -> equal = e; if (!e) prefillCustom() },
                        custom = custom,
                        onCustom = { id, cents -> custom[id] = cents },
                        count = count,
                        customSum = customSum,
                        totalCents = totalCents,
                        canContinue = whoOk,
                        onNext = { step = SplitStep.DETAILS }
                    )
                }
            )
        },
        sheetStep = step,
        sheet = { s ->
            when (s) {
                SplitStep.DETAILS -> SplitDetails(
                    total = total,
                    names = (if (includeMe) listOf("você") else emptyList()) + active.map { it.name },
                    equalShare = if (equal && count > 0) total / count else null,
                    firstPerson = active.firstOrNull(),
                    title = title, onTitle = { title = it },
                    notes = notes, onNotes = { notes = it },
                    startYear = startYear, startMonth = startMonth,
                    onMonth = { y, m -> startYear = y; startMonth = m },
                    recurring = recurring, onRecurring = { recurring = it },
                    onBack = { step = SplitStep.WHO },
                    onSave = {
                        val parts = mutableListOf<BillParticipantShare>()
                        val share = if (count > 0) total / count else 0.0
                        if (includeMe) parts += BillParticipantShare(MY_PERSON_ID, "Eu", if (equal) share else (custom[MY_PERSON_ID] ?: 0L) / 100.0, isMyShare = true)
                        active.forEach { p ->
                            parts += BillParticipantShare(p.id, p.name, if (equal) share else (custom[p.id] ?: 0L) / 100.0, isMyShare = false)
                        }
                        onConfirm(title.trim(), total, parts, startYear, startMonth, recurring, if (recurring) 12 else 1, notes.trim())
                        step = SplitStep.DONE
                    }
                )
                else -> AmountStep(
                    heading = "Dividir conta",
                    person = null,
                    expr = expr,
                    onExpr = { expr = it },
                    onBack = onClose,
                    backIsClose = true,
                    tagPrefix = "split_",
                    extra = { value ->
                        Text(
                            if (value > 0 && count >= 2 && equal) "${CurrencyFormatter.format(value / count)} para cada um, entre $count" else "Valor total da conta",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkMuted
                        )
                    },
                    onNext = { step = SplitStep.WHO }
                )
            }
        },
        success = {
            SuccessScreen(amount = total, name = "", onClose = onClose, subtitle = "Conta dividida entre $count")
        }
    )
}

/** Painel branco da etapa "Com quem": Igual / Personalizado e quanto cada um paga. */
@Composable
private fun SplitPanel(
    people: List<Person>,
    includeMe: Boolean,
    total: Double,
    equal: Boolean,
    onEqual: (Boolean) -> Unit,
    custom: Map<Long, Long>,
    onCustom: (Long, Long) -> Unit,
    count: Int,
    customSum: Long,
    totalCents: Long,
    canContinue: Boolean,
    onNext: () -> Unit
) {
    CompositionLocalProvider(LocalLiquidGlassColors provides LiquidLightTokens) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color.White)
                .imePadding()
                .navigationBarsPadding()
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("split_panel"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                ModeChip("Igual", selected = equal, onClick = { onEqual(true) }, modifier = Modifier.testTag("split_equal"))
                ModeChip("Personalizado", selected = !equal, onClick = { onEqual(false) }, modifier = Modifier.testTag("split_custom"))
            }
            val rows = (if (includeMe) listOf<Person?>(null) else emptyList()) + people
            if (count < 2) {
                Text(
                    "Escolha pelo menos 2 participantes (você conta).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            rows.forEach { p ->
                val id = p?.id ?: MY_PERSON_ID
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    if (p != null) PersonAvatarBadge(name = p.name, colorHex = p.colorHex, avatar = p.avatar, size = 36.dp)
                    else Box(Modifier.size(36.dp).clip(CircleShape).background(Soft), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(p?.name ?: "Eu", style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (equal) {
                        Text(
                            if (count > 0) CurrencyFormatter.format(total / count) else "—",
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkMuted
                        )
                    } else {
                        CentsField(
                            cents = custom[id] ?: 0L,
                            onCents = { onCustom(id, it) },
                            modifier = Modifier.width(132.dp).testTag("split_share_$id")
                        )
                    }
                }
            }
            if (!equal && count >= 2) {
                val diff = totalCents - customSum
                Text(
                    when {
                        abs(diff) <= 10 -> "Bate com o total ✓"
                        diff > 0 -> "Faltam R$ ${Calculator.formatCents(diff)}"
                        else -> "Passou R$ ${Calculator.formatCents(-diff)} do total"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (abs(diff) <= 10) Color(0xFF15803D) else Color(0xFFB45309),
                    modifier = Modifier.align(Alignment.End).testTag("split_diff")
                )
            }
            PrimaryPill(text = "Continuar", enabled = canContinue, onClick = onNext, tag = "split_who_next")
        }
    }
}

@Composable
private fun SplitDetails(
    total: Double,
    names: List<String>,
    equalShare: Double?,
    firstPerson: Person?,
    title: String,
    onTitle: (String) -> Unit,
    notes: String,
    onNotes: (String) -> Unit,
    startYear: Int,
    startMonth: Int,
    onMonth: (Int, Int) -> Unit,
    recurring: Boolean,
    onRecurring: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    DetailsColumn(heading = "Que conta é essa?", onBack = onBack) {
        SummaryCard(
            person = firstPerson,
            title = CurrencyFormatter.format(total),
            line = "Entre " + names.joinToString(", ").replaceLastComma() +
                (equalShare?.let { " · ${CurrencyFormatter.format(it)} cada" } ?: " · valores personalizados"),
            onClick = onBack
        )
        BigTextField(value = title, onValue = onTitle, placeholder = "Ex: Internet, luz, aluguel", tag = "split_title")
        MonthStepper(label = "Mês de início", year = startYear, month = startMonth, onChange = onMonth)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Soft)
                .clickable(role = Role.Switch) { onRecurring(!recurring) }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text("Repetir todo mês", style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(
                    if (recurring) "Conta fixa, repete por 12 meses" else "Só neste mês",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
            }
            Switch(
                checked = recurring,
                onCheckedChange = onRecurring,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, checkedTrackColor = Ink,
                    uncheckedThumbColor = InkMuted, uncheckedTrackColor = Line, uncheckedBorderColor = Line
                ),
                modifier = Modifier.testTag("split_recurring")
            )
        }
        OutlinedTextField(
            value = notes,
            onValueChange = onNotes,
            label = { Text("Observação (opcional)") },
            singleLine = true,
            colors = sheetFieldColors(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        SaveSwipe(enabled = title.isNotBlank(), onSave = onSave)
    }
}

private fun String.replaceLastComma(): String {
    val i = lastIndexOf(", ")
    return if (i < 0) this else substring(0, i) + " e " + substring(i + 2)
}

/* =========================== Peças comuns =========================== */

/**
 * Camadas do fluxo: a grade escura no fundo (recua e escurece quando a folha
 * sobe), a folha branca com as etapas e a tela verde de sucesso por cima.
 */
@Composable
private fun <S : Enum<S>> FlowScaffold(
    sheetVisible: Boolean,
    done: Boolean,
    background: @Composable () -> Unit,
    sheetStep: S,
    sheet: @Composable (S) -> Unit,
    success: @Composable () -> Unit
) {
    val reduced = rememberReducedMotion()
    val back by animateFloatAsState(
        targetValue = if (sheetVisible || done) 1f else 0f,
        animationSpec = tween(if (reduced) 0 else AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut),
        label = "flow_back"
    )
    Box(Modifier.fillMaxSize().background(Color.Black).testTag("entry_flow")) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val sc = 1f - 0.06f * back
                    scaleX = sc; scaleY = sc
                    translationY = 18f * back * density
                }
                .clip(RoundedCornerShape((28 * back).dp))
        ) {
            CompositionLocalProvider(LocalLiquidGlassColors provides LiquidDarkTokens) { background() }
            if (back > 0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f * back)))
        }

        AnimatedVisibility(
            visible = sheetVisible && !done,
            enter = if (reduced) fadeIn(tween(0)) else slideInVertically(spring(dampingRatio = 0.86f, stiffness = 380f)) { it } + fadeIn(tween(120)),
            exit = if (reduced) fadeOut(tween(0)) else slideOutVertically(tween(AcertoMotion.FAST, easing = AcertoMotion.SmoothOut)) { it } + fadeOut(tween(AcertoMotion.FAST))
        ) {
            StatusBarIcons(lightIcons = true)
            CompositionLocalProvider(LocalLiquidGlassColors provides LiquidLightTokens) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(Color.White)
                        .navigationBarsPadding()
                        .testTag("entry_sheet")
                ) {
                    Box(
                        Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp).width(36.dp).height(4.dp)
                            .clip(RoundedCornerShape(2.dp)).background(Color(0x22000000))
                    )
                    // Etapas da folha: deslizam para o lado, na direção do avanço
                    AnimatedContent(
                        targetState = sheetStep,
                        transitionSpec = {
                            if (reduced) {
                                ContentTransform(fadeIn(tween(0)), fadeOut(tween(0)))
                            } else {
                                val forward = targetState.ordinal > initialState.ordinal
                                val spec = tween<androidx.compose.ui.unit.IntOffset>(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut)
                                (slideInHorizontally(spec) { w -> if (forward) w / 3 else -w / 3 } + fadeIn(tween(AcertoMotion.FAST, delayMillis = 60))) togetherWith
                                    (slideOutHorizontally(spec) { w -> if (forward) -w / 3 else w / 3 } + fadeOut(tween(AcertoMotion.QUICK)))
                            }
                        },
                        label = "flow_sheet_step",
                        modifier = Modifier.fillMaxSize()
                    ) { s -> sheet(s) }
                }
            }
        }

        AnimatedVisibility(
            visible = done,
            enter = fadeIn(tween(AcertoMotion.FAST)) + scaleIn(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut), initialScale = 0.92f),
            exit = fadeOut(tween(AcertoMotion.QUICK))
        ) { success() }
    }
}

/** Topo da folha: voltar (ou fechar), título no centro e avatar opcional. */
@Composable
private fun SheetTopBar(heading: String, onBack: () -> Unit, person: Person? = null, backIsClose: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp)
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(Soft)
                .clickable(role = Role.Button, onClickLabel = if (backIsClose) "Fechar" else "Voltar", onClick = onBack)
                .testTag("flow_back"),
            contentAlignment = Alignment.Center
        ) {
            Icon(if (backIsClose) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (backIsClose) "Fechar" else "Voltar", tint = Ink, modifier = Modifier.size(22.dp))
        }
        Text(
            heading,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            if (person != null) PersonAvatarBadge(name = person.name, colorHex = person.colorHex, avatar = person.avatar, size = 38.dp)
        }
    }
}

/** Etapa de valor: número grande, conta "143,00 × 2", extra (parcelas) e teclado. */
@Composable
private fun AmountStep(
    heading: String,
    person: Person?,
    expr: String,
    onExpr: (String) -> Unit,
    onBack: () -> Unit,
    tagPrefix: String,
    extra: @Composable (Double) -> Unit,
    onNext: () -> Unit,
    backIsClose: Boolean = false
) {
    val haptics = LocalHapticFeedback.current
    var errorTick by remember { mutableIntStateOf(0) }
    val value = Calculator.evaluate(expr) ?: 0.0
    Column(Modifier.fillMaxSize()) {
        SheetTopBar(heading = heading, onBack = onBack, person = person, backIsClose = backIsClose)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Box(Modifier.shakeOnChange(errorTick).padding(horizontal = 20.dp)) {
                ExpressionText(expr = expr, color = Ink, tag = "${tagPrefix}amount")
            }
            if (Calculator.hasOperator(expr)) {
                Text(
                    "= ${CurrencyFormatter.format(value)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkMuted,
                    modifier = Modifier.padding(top = 2.dp).testTag("${tagPrefix}result")
                )
            }
            Spacer(Modifier.height(14.dp))
            extra(value)
        }
        OperatorRow(onOp = { op -> onExpr(Calculator.press(expr, op.toString())) }, tagPrefix = "${tagPrefix}op_")
        Keypad(
            onKey = { key ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                val next = Calculator.press(expr, key)
                if (next == expr && key != "⌫") errorTick++ else onExpr(next)
            },
            modifier = Modifier.padding(horizontal = 16.dp),
            tagPrefix = "${tagPrefix}key_"
        )
        PrimaryPill(
            text = "Continuar",
            enabled = value > 0.0,
            onClick = onNext,
            tag = "${tagPrefix}next",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)
        )
    }
}

/** Coluna rolável das etapas de detalhes (sobe junto com o teclado do celular). */
@Composable
private fun DetailsColumn(heading: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding()) {
        SheetTopBar(heading = heading, onBack = onBack)
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val minH = maxHeight
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = minH)
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                // no meio da tela (não tudo grudado em cima); sobe com o teclado
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                content = content
            )
        }
    }
}

/** Resumo do que já foi escolhido, no centro; tocar volta para a etapa anterior. */
@Composable
private fun SummaryCard(person: Person?, title: String, line: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClickLabel = "Alterar", onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag("flow_summary")
    ) {
        if (person != null) {
            PersonAvatarBadge(name = person.name, colorHex = person.colorHex, avatar = person.avatar, size = 60.dp)
            Spacer(Modifier.height(10.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(line, style = MaterialTheme.typography.bodyMedium, color = InkMuted, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
        Text(
            "Alterar",
            style = MaterialTheme.typography.labelLarge,
            color = Ink,
            modifier = Modifier.padding(top = 10.dp).clip(RoundedCornerShape(14.dp)).background(Soft).padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

/** Campo de texto grande, sem borda, que já abre o teclado. */
@Composable
private fun BigTextField(value: String, onValue: (String) -> Unit, placeholder: String, tag: String) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(AcertoMotion.SLOW.toLong()) // espera a folha assentar
        runCatching { focus.requestFocus() }
    }
    val style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.Center)
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        if (value.isEmpty()) Text(placeholder, style = style.copy(color = Color(0xFFB8BABD)), modifier = Modifier.fillMaxWidth())
        BasicTextField(
            value = value,
            onValueChange = onValue,
            textStyle = style,
            cursorBrush = SolidColor(Ink),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag(tag)
        )
    }
}

/** "Primeira parcela  ←  Novembro / 2026  →" */
@Composable
private fun MonthStepper(label: String, year: Int, month: Int, onChange: (Int, Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Soft).padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f).padding(start = 10.dp))
        StepArrow(Icons.AutoMirrored.Filled.ArrowBack, "Mês anterior") {
            if (month == 1) onChange(year - 1, 12) else onChange(year, month - 1)
        }
        androidx.compose.animation.AnimatedContent(
            targetState = year * 12 + month,
            transitionSpec = {
                val up = targetState > initialState
                (slideInHorizontally { w -> if (up) w / 2 else -w / 2 } + fadeIn()) togetherWith
                    (slideOutHorizontally { w -> if (up) -w / 2 else w / 2 } + fadeOut())
            },
            label = "month_stepper"
        ) { idx ->
            val y = (idx - 1) / 12
            val m = (idx - 1) % 12 + 1
            Text(
                "${CurrencyFormatter.getMonthShortName(m)} $y",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(84.dp)
            )
        }
        StepArrow(Icons.AutoMirrored.Filled.ArrowForward, "Próximo mês") {
            if (month == 12) onChange(year + 1, 1) else onChange(year, month + 1)
        }
    }
}

@Composable
private fun StepArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = label, tint = Ink, modifier = Modifier.size(20.dp)) }
}

/** "Deslize para a direita para salvar": aparece assim que o nome é digitado. */
@Composable
private fun SaveSwipe(enabled: Boolean, onSave: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    AnimatedVisibility(
        visible = enabled,
        enter = androidx.compose.animation.expandVertically(tween(AcertoMotion.FAST, easing = AcertoMotion.SmoothOut)) +
            fadeIn(tween(AcertoMotion.FAST, delayMillis = 60)) +
            slideInVertically(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut)) { it / 3 },
        exit = androidx.compose.animation.shrinkVertically(tween(AcertoMotion.FAST)) + fadeOut(tween(AcertoMotion.QUICK))
    ) {
        SwipeToConfirm(
            label = "Deslize para a direita para salvar",
            onConfirmed = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onSave()
            }
        )
    }
}

/** Botão principal escuro, largura total. */
@Composable
private fun PrimaryPill(text: String, enabled: Boolean, onClick: () -> Unit, tag: String, modifier: Modifier = Modifier) {
    val bg by androidx.compose.animation.animateColorAsState(if (enabled) Ink else Line, tween(AcertoMotion.FAST), label = "pill_bg")
    val fg by androidx.compose.animation.animateColorAsState(if (enabled) Color.White else Color(0xFF9A9DA1), tween(AcertoMotion.FAST), label = "pill_fg")
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .pressScale(interaction, pressedScale = 0.97f)
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) { Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = fg) }
}

/** Campo de valor com vírgula automática (digite 1500 → 15,00). */
@Composable
private fun CentsField(cents: Long, onCents: (Long) -> Unit, modifier: Modifier = Modifier) {
    val text = if (cents <= 0) "" else Calculator.formatCents(cents)
    OutlinedTextField(
        value = TextFieldValue(text, selection = TextRange(text.length)),
        onValueChange = { v ->
            val digits = v.text.filter { it.isDigit() }.trimStart('0').take(10)
            onCents(digits.toLongOrNull() ?: 0L)
        },
        prefix = { Text("R$ ", color = InkMuted) },
        placeholder = { Text("0,00", color = Color(0xFFB8BABD)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = sheetFieldColors(),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
        modifier = modifier
    )
}

@Composable
private fun sheetFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    focusedBorderColor = Ink,
    unfocusedBorderColor = Line,
    cursorColor = Ink,
    focusedLabelColor = Ink,
    unfocusedLabelColor = InkMuted,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedPrefixColor = InkMuted,
    unfocusedPrefixColor = InkMuted
)

/* ---------- Grade escura de pessoas ---------- */

private data class MeTile(val selected: Boolean, val onToggle: () -> Unit)

@Composable
private fun PeopleGrid(
    title: String,
    subtitle: String?,
    people: List<Person>,
    isSelected: (Long) -> Boolean,
    multi: Boolean,
    onTap: (Long) -> Unit,
    onAddPerson: () -> Unit,
    leading: @Composable () -> Unit,
    tagPrefix: String,
    me: MeTile? = null,
    bottom: (@Composable () -> Unit)? = null
) {
    StatusBarIcons(lightIcons = true, lightNavIcons = bottom == null)
    val reduced = rememberReducedMotion()
    Column(Modifier.fillMaxSize().noirHeaderBackground().statusBarsPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)
        ) {
            leading()
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = AcertoPalette.TextOnHeader)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AcertoPalette.TextOnHeaderMuted)
            }
        }
        val tiles = buildList<Any> {
            if (me != null) add(me)
            addAll(people)
            add("add")
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = if (bottom == null) 40.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            itemsIndexed(tiles, key = { _, t -> when (t) { is Person -> "p${t.id}"; is MeTile -> "me"; else -> "add" } }) { i, t ->
                // entrada em cascata
                val intro = remember { Animatable(if (reduced) 1f else 0f) }
                LaunchedEffect(Unit) {
                    if (!reduced) {
                        kotlinx.coroutines.delay(28L * i.coerceAtMost(12))
                        intro.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 420f))
                    }
                }
                Box(Modifier.graphicsLayer { alpha = intro.value; translationY = (1f - intro.value) * 24f * density }) {
                    when (t) {
                        is Person -> GridTile(
                            label = t.name,
                            selected = isSelected(t.id),
                            showCheck = multi,
                            onClick = { onTap(t.id) },
                            tag = "$tagPrefix${t.id}",
                            description = if (multi) "Dividir com ${t.name}" else "Compra de ${t.name}"
                        ) { PersonAvatarBadge(name = t.name, colorHex = t.colorHex, avatar = t.avatar, size = 72.dp) }
                        is MeTile -> GridTile(
                            label = "Eu",
                            selected = t.selected,
                            showCheck = true,
                            onClick = t.onToggle,
                            tag = "${tagPrefix}me",
                            description = "Incluir você"
                        ) {
                            Box(Modifier.size(72.dp).clip(CircleShape).background(AcertoPalette.PillLight), contentAlignment = Alignment.Center) {
                                com.example.ui.components.ProfileAvatar(size = 72.dp, interactive = false)
                            }
                        }
                        else -> GridTile(
                            label = "Nova pessoa",
                            selected = false,
                            showCheck = false,
                            onClick = onAddPerson,
                            tag = "${tagPrefix}add",
                            description = "Adicionar pessoa"
                        ) {
                            Box(
                                Modifier.size(72.dp).clip(CircleShape).border(1.5.dp, AcertoPalette.TextOnHeaderMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Add, contentDescription = null, tint = AcertoPalette.TextOnHeader, modifier = Modifier.size(30.dp)) }
                        }
                    }
                }
            }
        }
        if (bottom != null) bottom()
    }
}

@Composable
private fun GridTile(
    label: String,
    selected: Boolean,
    showCheck: Boolean,
    onClick: () -> Unit,
    tag: String,
    description: String,
    avatar: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, spring(dampingRatio = 0.5f, stiffness = 500f), label = "tile_press")
    val dim by animateFloatAsState(if (showCheck && !selected) 0.55f else 1f, tween(AcertoMotion.FAST), label = "tile_dim")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, role = if (showCheck) Role.Checkbox else Role.Button, onClick = onClick)
            .padding(vertical = 6.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = if (showCheck) "$description${if (selected) ", marcado" else ""}" else description
            }
            .testTag(tag)
    ) {
        Box {
            Box(Modifier.graphicsLayer { alpha = dim }) { avatar() }
            if (showCheck) {
                val checkScale by animateFloatAsState(if (selected) 1f else 0f, spring(dampingRatio = 0.5f, stiffness = 600f), label = "tile_check")
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .graphicsLayer { scaleX = checkScale; scaleY = checkScale }
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(AcertoPalette.Mint)
                        .border(2.dp, AcertoPalette.Header, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Check, contentDescription = null, tint = AcertoPalette.OnLime, modifier = Modifier.size(16.dp)) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = AcertoPalette.TextOnHeader.copy(alpha = if (showCheck && !selected) 0.7f else 1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
