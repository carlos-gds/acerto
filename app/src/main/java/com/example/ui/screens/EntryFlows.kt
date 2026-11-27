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
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
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
import com.example.util.Money
import kotlin.math.abs

/*
 * Lançamentos em etapas, no estilo da referência:
 *  - Nova compra:   Quem comprou (grade escura) → Valor e parcelas (teclado) → Detalhes → sucesso
 *  - Dividir conta: Valor (teclado) → Com quem (grade escura) → Detalhes → sucesso
 *  - Editar:        abre no resumo; tocar no valor abre o teclado, na pessoa a grade
 * A folha branca sobe por cima da grade; entre as etapas da folha o conteúdo
 * desliza para o lado (para a frente ou para trás).
 */

private val Ink = Color(0xFF16181A)
private val InkMuted = Color(0xFF63666B)
private val Soft = Color(0xFFF2F2F3)
private val Line = Color(0xFFE4E4E7)

/* =========================== Nova compra =========================== */

private enum class PurchaseStep { WHO, AMOUNT, DETAILS, SPLIT_WHO, DONE }

@Composable
fun NewPurchaseFlow(
    people: List<Person>,
    currentYear: Int,
    currentMonth: Int,
    onAddPerson: () -> Unit,
    /** shares: quanto da compra é de cada pessoa (uma só, ou várias numa compra dividida). */
    onConfirm: (
        title: String,
        totalAmount: Double,
        shares: List<Pair<Person, Double>>,
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
    // quem mais divide a compra (além de personId), em ordem de escolha
    var extraIds by rememberSaveable { mutableStateOf("") }
    var splitEqual by rememberSaveable { mutableStateOf(true) }
    val perMonth = remember { mutableStateMapOf<Long, Long>() }
    var expr by rememberSaveable { mutableStateOf("") }
    var installments by rememberSaveable { mutableIntStateOf(1) }
    var title by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var startYear by rememberSaveable { mutableIntStateOf(currentYear) }
    var startMonth by rememberSaveable { mutableIntStateOf(currentMonth) }
    var markPast by rememberSaveable { mutableStateOf(true) }
    val person = people.firstOrNull { it.id == personId }
    val total = Calculator.evaluate(expr) ?: 0.0
    val extras = extraIds.split(',').mapNotNull { it.toLongOrNull() }.filter { it != personId }
    val members = listOfNotNull(person) + extras.mapNotNull { id -> people.firstOrNull { it.id == id } }
    val totalCents = Math.round(total * 100)
    val split = members.size > 1
    val splitOk = !split || splitEqual || splitMatches(totalCents, installments, members.map { it.id }, perMonth)

    BackHandler {
        step = when (step) {
            PurchaseStep.SPLIT_WHO -> PurchaseStep.DETAILS
            PurchaseStep.DETAILS -> PurchaseStep.AMOUNT
            PurchaseStep.AMOUNT -> PurchaseStep.WHO
            else -> { onClose(); step }
        }
    }

    FlowScaffold(
        sheetVisible = step == PurchaseStep.AMOUNT || step == PurchaseStep.DETAILS,
        done = step == PurchaseStep.DONE,
        background = {
            if (step == PurchaseStep.SPLIT_WHO) {
                PeopleGrid(
                    title = "Dividir com quem?",
                    subtitle = "Cada pessoa paga a parte dela das parcelas",
                    people = people,
                    isSelected = { id -> members.any { it.id == id } },
                    multi = true,
                    onTap = { id ->
                        val ids = members.map { it.id }
                        val next = if (id in ids) (if (ids.size > 1) ids - id else ids) else ids + id
                        personId = next.first()
                        extraIds = next.drop(1).joinToString(",")
                        perMonth.clear()
                        splitEqual = true
                    },
                    onAddPerson = onAddPerson,
                    leading = {
                        NoirRoundButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", onClick = { step = PurchaseStep.DETAILS })
                    },
                    tagPrefix = "purchase_split_person_",
                    bottom = {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                                .background(Color.White)
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            PrimaryPill(
                                text = if (members.size > 1) "Dividir entre ${members.size}" else "Continuar",
                                enabled = true,
                                onClick = { step = PurchaseStep.DETAILS },
                                tag = "purchase_split_done"
                            )
                        }
                    }
                )
            } else {
                PeopleGrid(
                    title = "Quem comprou?",
                    subtitle = "Compra no seu cartão",
                    people = people,
                    isSelected = { it == personId },
                    multi = false,
                    onTap = { id -> personId = id; extraIds = ""; step = PurchaseStep.AMOUNT },
                    onAddPerson = onAddPerson,
                    leading = { NoirRoundButton(icon = Icons.Default.Close, contentDescription = "Fechar", onClick = onClose, modifier = Modifier.testTag("purchase_close")) },
                    tagPrefix = "purchase_person_"
                )
            }
        },
        sheetStep = step,
        sheet = { s ->
            when (s) {
                PurchaseStep.DETAILS -> PurchaseDetails(
                    person = person,
                    members = members,
                    onSplitPeople = { step = PurchaseStep.SPLIT_WHO },
                    canSave = splitOk,
                    split = {
                        SplitSharesBlock(
                            members = members,
                            totalCents = totalCents,
                            installments = installments,
                            equal = splitEqual,
                            onEqual = { e ->
                                if (!e && perMonth.isEmpty()) prefillPerMonth(totalCents, installments, members.map { it.id }, perMonth)
                                splitEqual = e
                            },
                            perMonth = perMonth,
                            onChangePeople = { step = PurchaseStep.SPLIT_WHO }
                        )
                    },
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
                        if (members.isEmpty()) return@PurchaseDetails
                        val shares = if (!split) listOf(members.first() to total)
                        else members.zip(splitTotals(totalCents, installments, members.map { it.id }, splitEqual, perMonth)) { m, c -> m to c / 100.0 }
                        onConfirm(title.trim(), total, shares, installments, startYear, startMonth, notes.trim(), markPast)
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
                subtitle = when {
                    split -> "Dividida entre " + members.joinToString(", ") { it.name }.replaceLastComma()
                    installments > 1 -> "${installments}x · compra de ${person?.name.orEmpty()}"
                    else -> "Compra de ${person?.name.orEmpty()}"
                }
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
    members: List<Person>,
    onSplitPeople: () -> Unit,
    canSave: Boolean,
    split: @Composable () -> Unit,
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
            person = if (members.size > 1) null else person,
            title = if (members.size > 1) members.joinToString(", ") { it.name }.replaceLastComma() else person?.name ?: "",
            line = CurrencyFormatter.format(total) + if (installments > 1) " · ${installments}x de ${CurrencyFormatter.format(total / installments)}" else " · à vista",
            onClick = onBack
        )
        BigTextField(
            value = title,
            onValue = onTitle,
            placeholder = "Ex: Tênis, celular, mercado",
            tag = "purchase_title"
        )
        if (members.size > 1) split() else AddSplitRow(onClick = onSplitPeople)
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
        SaveSwipe(enabled = title.isNotBlank() && canSave, onSave = onSave)
    }
}

/* ---------- Compra dividida: a parte de cada pessoa, por mês ---------- */

/** Parcela da compra inteira, em centavos. */
private fun installmentCents(totalCents: Long, installments: Int): Long =
    if (installments <= 0) 0L else Math.round(totalCents.toDouble() / installments)

/** Personalizado começa igual: a parcela dividida entre todos (o resto fica com o primeiro). */
private fun prefillPerMonth(totalCents: Long, installments: Int, ids: List<Long>, perMonth: MutableMap<Long, Long>) {
    if (ids.isEmpty()) return
    val inst = installmentCents(totalCents, installments)
    val base = inst / ids.size
    ids.forEachIndexed { i, id -> perMonth[id] = if (i == 0) inst - base * (ids.size - 1) else base }
}

/** As partes por mês somam a parcela (tolerância de 1 centavo de arredondamento)? */
private fun splitMatches(totalCents: Long, installments: Int, ids: List<Long>, perMonth: Map<Long, Long>): Boolean {
    if (ids.any { (perMonth[it] ?: 0L) <= 0L }) return false
    return abs(ids.sumOf { perMonth[it] ?: 0L } - installmentCents(totalCents, installments)) <= 1
}

/**
 * Total da compra que fica com cada pessoa, em centavos, na ordem de [ids].
 * Igual: o total dividido por todos. Personalizado: a parte do mês × parcelas.
 * A diferença de arredondamento fica com o primeiro, para a soma bater com o total.
 */
private fun splitTotals(totalCents: Long, installments: Int, ids: List<Long>, equal: Boolean, perMonth: Map<Long, Long>): List<Long> {
    if (ids.isEmpty()) return emptyList()
    val raw = if (equal) {
        val base = totalCents / ids.size
        ids.map { base }
    } else {
        ids.map { (perMonth[it] ?: 0L) * installments.coerceAtLeast(1) }
    }
    val diff = totalCents - raw.sum()
    return raw.mapIndexed { i, c -> if (i == 0) c + diff else c }
}

/** "+ Dividir com mais alguém" (abre a grade para escolher quem mais divide). */
@Composable
private fun AddSplitRow(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Soft)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("purchase_split")
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("Dividir com mais alguém", style = MaterialTheme.typography.titleSmall, color = Ink)
            Text("Cada pessoa paga a parte dela de cada parcela", style = MaterialTheme.typography.bodySmall, color = InkMuted)
        }
    }
}

/** Bloco da compra dividida: Igual / Personalizado e quanto cada pessoa paga por mês. */
@Composable
private fun SplitSharesBlock(
    members: List<Person>,
    totalCents: Long,
    installments: Int,
    equal: Boolean,
    onEqual: (Boolean) -> Unit,
    perMonth: MutableMap<Long, Long>,
    onChangePeople: (() -> Unit)?
) {
    val inst = installmentCents(totalCents, installments)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("purchase_split_block")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text("Dividida entre ${members.size}", style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(
                    if (installments > 1) "Parcela de R$ ${Calculator.formatCents(inst)} por mês" else "Compra de R$ ${Calculator.formatCents(totalCents)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
            }
            if (onChangePeople != null) {
                Text(
                    "Pessoas",
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Soft)
                        .clickable(role = Role.Button, onClickLabel = "Mudar quem divide", onClick = onChangePeople)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("purchase_split_people")
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
            ModeChip("Igual", selected = equal, onClick = { onEqual(true) }, modifier = Modifier.testTag("purchase_split_equal"))
            ModeChip("Personalizado", selected = !equal, onClick = { onEqual(false) }, modifier = Modifier.testTag("purchase_split_custom"))
        }
        val equalShare = if (members.isEmpty()) 0.0 else inst / 100.0 / members.size
        members.forEach { m ->
            ShareLine(
                name = m.name,
                person = m,
                equal = equal,
                equalShare = equalShare,
                cents = perMonth[m.id] ?: 0L,
                onCents = { perMonth[m.id] = it },
                tag = "purchase_share_${m.id}",
                suffix = if (installments > 1) "por mês" else null
            )
        }
        if (!equal) {
            val diff = inst - members.sumOf { perMonth[it.id] ?: 0L }
            Text(
                when {
                    abs(diff) <= 1 -> "Bate com a parcela ✓"
                    diff > 0 -> "Faltam R$ ${Calculator.formatCents(diff)} na parcela"
                    else -> "Passou R$ ${Calculator.formatCents(-diff)} da parcela"
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (abs(diff) <= 1) Color(0xFF15803D) else Color(0xFFB45309),
                modifier = Modifier.align(Alignment.End).testTag("purchase_split_diff")
            )
        }
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

/* =========================== Fatura do cartão =========================== */

/** Folha com o teclado para informar o total da fatura do mês (a sua parte aparece embaixo). */
@Composable
fun FaturaAmountSheet(
    monthName: String,
    initialCents: Long?,
    othersCents: Long,
    onSave: (Long) -> Unit,
    onClose: () -> Unit
) {
    var expr by rememberSaveable { mutableStateOf(initialCents?.takeIf { it > 0 }?.toString().orEmpty()) }
    BackHandler { onClose() }
    StatusBarIcons(lightIcons = true)
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).testTag("fatura_sheet")) {
        CompositionLocalProvider(LocalLiquidGlassColors provides LiquidLightTokens) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(Color.White)
                    .navigationBarsPadding()
            ) {
                Box(
                    Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp).width(36.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp)).background(Color(0x22000000))
                )
                AmountStep(
                    heading = "Fatura de $monthName",
                    person = null,
                    expr = expr,
                    onExpr = { expr = it },
                    onBack = onClose,
                    backIsClose = true,
                    tagPrefix = "fatura_",
                    nextLabel = "Salvar",
                    extra = { value ->
                        val cents = Math.round(value * 100)
                        Text(
                            when {
                                cents <= 0L -> "Valor total da fatura do cartão"
                                cents < othersCents -> "Menor que as compras dos outros (R$ ${Calculator.formatCents(othersCents)})"
                                else -> "Sua parte: R$ ${Calculator.formatCents(cents - othersCents)} · dos outros R$ ${Calculator.formatCents(othersCents)}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = if (cents in 1 until othersCents) Color(0xFFB45309) else InkMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp).testTag("fatura_mine")
                        )
                    },
                    onNext = { onSave(Math.round((Calculator.evaluate(expr) ?: 0.0) * 100)) }
                )
            }
        }
    }
}

/* =========================== Editar lançamentos =========================== */

private enum class EditStep { WHO, AMOUNT, DETAILS, DONE }

/**
 * Editar compra no cartão, no mesmo estilo da Nova compra: abre direto no resumo
 * (valor, nome, pessoa e primeira parcela). Tocar no valor abre o teclado com as
 * parcelas; tocar na pessoa abre a grade escura. Deslize para salvar.
 */
@Composable
fun EditPurchaseFlow(
    item: InstallmentItem,
    people: List<Person>,
    onAddPerson: () -> Unit,
    onConfirm: (
        expenseId: Long,
        title: String,
        totalAmount: Double,
        person: Person,
        totalInstallments: Int,
        startYear: Int,
        startMonth: Int
    ) -> Unit,
    onClose: () -> Unit,
    /** Compra dividida: todas as partes (a desta pessoa incluída). Vazio = compra solta. */
    group: List<Expense> = emptyList(),
    /** Salva a compra dividida inteira: (id da parte, pessoa, valor) de cada parte. */
    onConfirmGroup: (parts: List<Triple<Long, Person, Double>>, title: String, totalInstallments: Int, startYear: Int, startMonth: Int) -> Unit = { _, _, _, _, _ -> }
) {
    val isGroup = group.size > 1
    val initialTotal = when {
        isGroup -> group.sumOf { Money.toReal(it.totalAmount) }
        item.expenseType == ExpenseType.CARD_PURCHASE -> Money.toReal(item.amount * item.totalInstallments.toLong())
        else -> Money.toReal(item.amount)
    }
    val groupMembers = group.mapNotNull { e -> people.firstOrNull { it.id == e.personId }?.let { e to it } }
    val perMonth = remember {
        mutableStateMapOf<Long, Long>().apply {
            val n = item.totalInstallments.coerceAtLeast(1)
            group.forEach { e -> put(e.personId, Math.round(e.totalAmount.toDouble() / n)) }
        }
    }
    var splitEqual by rememberSaveable {
        mutableStateOf(group.map { it.totalAmount }.let { t -> t.isEmpty() || (t.maxOrNull() ?: 0L) - (t.minOrNull() ?: 0L) <= t.size.toLong() })
    }
    val firstIdx = (item.year * 12 + (item.month - 1)) - (item.installmentNumber - 1)
    var step by rememberSaveable { mutableStateOf(EditStep.DETAILS) }
    var personId by rememberSaveable { mutableStateOf<Long?>(item.personId) }
    var expr by rememberSaveable { mutableStateOf(Calculator.fromAmount(initialTotal)) }
    var installments by rememberSaveable { mutableIntStateOf(item.totalInstallments.coerceIn(1, 24)) }
    var title by rememberSaveable { mutableStateOf(item.expenseTitle) }
    var startYear by rememberSaveable { mutableIntStateOf(firstIdx / 12) }
    var startMonth by rememberSaveable { mutableIntStateOf(firstIdx % 12 + 1) }
    val person = people.firstOrNull { it.id == personId }
    val total = Calculator.evaluate(expr) ?: 0.0

    BackHandler {
        when (step) {
            EditStep.WHO, EditStep.AMOUNT -> step = EditStep.DETAILS
            else -> onClose()
        }
    }

    FlowScaffold(
        sheetVisible = step == EditStep.AMOUNT || step == EditStep.DETAILS,
        done = step == EditStep.DONE,
        background = {
            PeopleGrid(
                title = "Quem comprou?",
                subtitle = "Trocar a pessoa da compra",
                people = people,
                isSelected = { it == personId },
                multi = false,
                onTap = { id -> personId = id; step = EditStep.DETAILS },
                onAddPerson = onAddPerson,
                leading = {
                    NoirRoundButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", onClick = { step = EditStep.DETAILS })
                },
                tagPrefix = "edit_person_"
            )
        },
        sheetStep = step,
        sheet = { s ->
            when (s) {
                EditStep.AMOUNT -> AmountStep(
                    heading = "Valor da compra",
                    person = person,
                    expr = expr,
                    onExpr = { expr = it },
                    onBack = { step = EditStep.DETAILS },
                    tagPrefix = "edit_",
                    extra = { value ->
                        InstallmentPicker(total = value, selected = installments, onSelect = { installments = it })
                    },
                    onNext = { step = EditStep.DETAILS }
                )
                else -> DetailsColumn(heading = if (isGroup) "Editar compra dividida" else "Editar compra", onBack = onClose, backIsClose = true) {
                    val totalCents = Math.round(total * 100)
                    val ids = groupMembers.map { it.second.id }
                    SummaryCard(
                        person = null,
                        title = CurrencyFormatter.format(total),
                        line = if (installments > 1) "${installments}x de ${CurrencyFormatter.format(total / installments)}" else "À vista",
                        onClick = { step = EditStep.AMOUNT }
                    )
                    BigTextField(
                        value = title,
                        onValue = { title = it },
                        placeholder = "Ex: Tênis, celular, mercado",
                        tag = "edit_title",
                        autoFocus = false
                    )
                    if (isGroup) {
                        SplitSharesBlock(
                            members = groupMembers.map { it.second },
                            totalCents = totalCents,
                            installments = installments,
                            equal = splitEqual,
                            onEqual = { e ->
                                if (!e && !splitMatches(totalCents, installments, ids, perMonth)) prefillPerMonth(totalCents, installments, ids, perMonth)
                                splitEqual = e
                            },
                            perMonth = perMonth,
                            onChangePeople = null
                        )
                    } else {
                        PersonRow(label = "Quem comprou", person = person, onClick = { step = EditStep.WHO })
                    }
                    MonthStepper(label = "Primeira parcela", year = startYear, month = startMonth, onChange = { y, m -> startYear = y; startMonth = m })
                    val groupOk = !isGroup || splitEqual || splitMatches(totalCents, installments, ids, perMonth)
                    SaveSwipe(enabled = title.isNotBlank() && total > 0.0 && (isGroup || person != null) && groupOk) {
                        if (isGroup) {
                            val totals = splitTotals(totalCents, installments, ids, splitEqual, perMonth)
                            val parts = groupMembers.zip(totals) { (e, p), c -> Triple(e.id, p, c / 100.0) }
                            onConfirmGroup(parts, title.trim(), installments, startYear, startMonth)
                        } else {
                            val p = person ?: return@SaveSwipe
                            onConfirm(item.expenseId, title.trim(), total, p, installments, startYear, startMonth)
                        }
                        step = EditStep.DONE
                    }
                }
            }
        },
        success = {
            SuccessScreen(amount = total, name = person?.name.orEmpty(), onClose = onClose, subtitle = if (isGroup) "Compra dividida atualizada" else "Compra atualizada")
        }
    )
}

/**
 * Editar conta dividida (você e mais uma pessoa), no estilo do Dividir conta:
 * resumo com o valor, nome, com quem, divisão Igual/Personalizado e se a
 * mudança vale para todos os meses ou só para a fatura aberta.
 */
@Composable
fun EditSplitFlow(
    item: InstallmentItem,
    people: List<Person>,
    selectedYear: Int,
    selectedMonth: Int,
    onLoadExpense: suspend (Long) -> Expense?,
    onAddPerson: () -> Unit,
    onConfirm: (
        expenseId: Long,
        newTitle: String,
        newTotalAmount: Double,
        newPerson: Person,
        newMyShare: Double,
        newOtherShare: Double,
        updateAllMonths: Boolean
    ) -> Unit,
    onClose: () -> Unit
) {
    var step by rememberSaveable { mutableStateOf(EditStep.DETAILS) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf(item.expenseTitle) }
    var expr by rememberSaveable { mutableStateOf(Calculator.fromAmount(Money.toReal(item.amount) * 2.0)) }
    var personId by rememberSaveable { mutableStateOf<Long?>(people.firstOrNull { it.id == item.personId }?.id ?: people.firstOrNull()?.id) }
    var equal by rememberSaveable { mutableStateOf(true) }
    var myCents by rememberSaveable { mutableStateOf(item.amount) }
    var otherCents by rememberSaveable { mutableStateOf(item.amount) }
    var updateAll by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(item.expenseId) {
        if (loaded) return@LaunchedEffect
        val exp = onLoadExpense(item.expenseId)
        if (exp != null) {
            title = exp.title
            val total = Money.toReal(exp.totalAmount)
            expr = Calculator.fromAmount(total)
            people.firstOrNull { it.id == exp.personId }?.let { personId = it.id }
            val mine = if (exp.myShareAmount > 0) Money.toReal(exp.myShareAmount) else total / 2.0
            val other = if (exp.otherShareAmount > 0) Money.toReal(exp.otherShareAmount) else total - mine
            equal = abs(mine - other) < 0.05
            myCents = Math.round(mine * 100)
            otherCents = Math.round(other * 100)
        }
        loaded = true
    }

    val total = Calculator.evaluate(expr) ?: 0.0
    val totalCents = Math.round(total * 100)
    val person = people.firstOrNull { it.id == personId }
    val myShare = if (equal) total / 2.0 else myCents / 100.0
    val otherShare = if (equal) total / 2.0 else otherCents / 100.0
    val valid = title.isNotBlank() && total > 0.0 && person != null && myShare >= 0.0 && otherShare >= 0.0 && (myShare > 0.0 || otherShare > 0.0)

    BackHandler {
        when (step) {
            EditStep.WHO, EditStep.AMOUNT -> step = EditStep.DETAILS
            else -> onClose()
        }
    }

    FlowScaffold(
        sheetVisible = step == EditStep.AMOUNT || step == EditStep.DETAILS,
        done = step == EditStep.DONE,
        background = {
            PeopleGrid(
                title = "Com quem dividir?",
                subtitle = if (total > 0) "Conta de ${CurrencyFormatter.format(total)}" else null,
                people = people,
                isSelected = { it == personId },
                multi = false,
                onTap = { id -> personId = id; step = EditStep.DETAILS },
                onAddPerson = onAddPerson,
                leading = {
                    NoirRoundButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", onClick = { step = EditStep.DETAILS })
                },
                tagPrefix = "edit_split_person_"
            )
        },
        sheetStep = step,
        sheet = { s ->
            when (s) {
                EditStep.AMOUNT -> AmountStep(
                    heading = "Valor da conta",
                    person = person,
                    expr = expr,
                    onExpr = { expr = it },
                    onBack = { step = EditStep.DETAILS },
                    tagPrefix = "edit_split_",
                    extra = { value ->
                        Text(
                            if (value > 0 && equal) "${CurrencyFormatter.format(value / 2)} para cada um" else "Valor total da conta",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkMuted
                        )
                    },
                    onNext = { step = EditStep.DETAILS }
                )
                else -> DetailsColumn(heading = "Editar conta", onBack = onClose, backIsClose = true) {
                    SummaryCard(
                        person = null,
                        title = CurrencyFormatter.format(total),
                        line = "Entre você e ${person?.name ?: "—"}" +
                            if (equal) " · ${CurrencyFormatter.format(total / 2)} cada" else " · valores personalizados",
                        onClick = { step = EditStep.AMOUNT }
                    )
                    BigTextField(
                        value = title,
                        onValue = { title = it },
                        placeholder = "Ex: Internet, luz, aluguel",
                        tag = "edit_split_title",
                        autoFocus = false
                    )
                    PersonRow(label = "Dividir com", person = person, onClick = { step = EditStep.WHO })
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, Line, RoundedCornerShape(16.dp)).padding(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            ModeChip("Igual", selected = equal, onClick = { equal = true }, modifier = Modifier.testTag("edit_split_equal"))
                            ModeChip(
                                "Personalizado",
                                selected = !equal,
                                onClick = {
                                    if (equal && myCents + otherCents <= 0L && totalCents > 0) {
                                        myCents = totalCents / 2
                                        otherCents = totalCents - myCents
                                    }
                                    equal = false
                                },
                                modifier = Modifier.testTag("edit_split_custom")
                            )
                        }
                        ShareLine(name = "Você", person = null, equal = equal, equalShare = total / 2, cents = myCents, onCents = { myCents = it }, tag = "edit_split_mine")
                        ShareLine(name = person?.name ?: "Outra pessoa", person = person, equal = equal, equalShare = total / 2, cents = otherCents, onCents = { otherCents = it }, tag = "edit_split_other")
                        if (!equal) {
                            val diff = totalCents - (myCents + otherCents)
                            Text(
                                when {
                                    abs(diff) <= 10 -> "Bate com o total ✓"
                                    diff > 0 -> "Faltam R$ ${Calculator.formatCents(diff)} para o total"
                                    else -> "Passou R$ ${Calculator.formatCents(-diff)} do total"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (abs(diff) <= 10) Color(0xFF15803D) else Color(0xFFB45309),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Soft)
                            .clickable(role = Role.Switch) { updateAll = !updateAll }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Mudar em todos os meses", style = MaterialTheme.typography.titleSmall, color = Ink)
                            Text(
                                if (updateAll) "Todas as faturas desta conta"
                                else "Só na fatura de ${CurrencyFormatter.getMonthShortName(selectedMonth).lowercase()}/$selectedYear",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkMuted
                            )
                        }
                        Switch(
                            checked = updateAll,
                            onCheckedChange = { updateAll = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, checkedTrackColor = Ink,
                                uncheckedThumbColor = InkMuted, uncheckedTrackColor = Line, uncheckedBorderColor = Line
                            ),
                            modifier = Modifier.testTag("edit_split_all_months")
                        )
                    }
                    SaveSwipe(enabled = valid) {
                        val p = person ?: return@SaveSwipe
                        onConfirm(item.expenseId, title.trim(), total, p, myShare, otherShare, updateAll)
                        step = EditStep.DONE
                    }
                }
            }
        },
        success = {
            SuccessScreen(amount = total, name = person?.name.orEmpty(), onClose = onClose, subtitle = "Conta atualizada")
        }
    )
}

/** "Quem comprou   (avatar) Pai   Trocar" — abre a grade de pessoas. */
@Composable
private fun PersonRow(label: String, person: Person?, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Soft)
            .clickable(role = Role.Button, onClickLabel = "Trocar pessoa", onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("edit_person_row")
    ) {
        if (person != null) PersonAvatarBadge(name = person.name, colorHex = person.colorHex, avatar = person.avatar, size = 40.dp)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = InkMuted)
            Text(person?.name ?: "Escolher pessoa", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            "Trocar",
            style = MaterialTheme.typography.labelLarge,
            color = Ink,
            modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(Color.White).padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

/** Uma linha da divisão: avatar, nome e a parte (texto na divisão igual, campo no personalizado). */
@Composable
private fun ShareLine(name: String, person: Person?, equal: Boolean, equalShare: Double, cents: Long, onCents: (Long) -> Unit, tag: String, suffix: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (person != null) PersonAvatarBadge(name = person.name, colorHex = person.colorHex, avatar = person.avatar, size = 36.dp)
        else Box(Modifier.size(36.dp).clip(CircleShape).background(Soft), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (suffix != null) Text(suffix, style = MaterialTheme.typography.bodySmall, color = InkMuted)
        }
        if (equal) {
            Text(CurrencyFormatter.format(equalShare), style = MaterialTheme.typography.bodyLarge, color = InkMuted)
        } else {
            CentsField(cents = cents, onCents = onCents, modifier = Modifier.width(132.dp).testTag(tag))
        }
    }
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
    backIsClose: Boolean = false,
    nextLabel: String = "Continuar"
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
            text = nextLabel,
            enabled = value > 0.0,
            onClick = onNext,
            tag = "${tagPrefix}next",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)
        )
    }
}

/** Coluna rolável das etapas de detalhes (sobe junto com o teclado do celular). */
@Composable
private fun DetailsColumn(heading: String, onBack: () -> Unit, backIsClose: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding()) {
        SheetTopBar(heading = heading, onBack = onBack, backIsClose = backIsClose)
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
private fun BigTextField(value: String, onValue: (String) -> Unit, placeholder: String, tag: String, autoFocus: Boolean = true) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (!autoFocus) return@LaunchedEffect
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
