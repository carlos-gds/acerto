package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.Person
import com.example.ui.components.NoirHeaderTile
import com.example.ui.components.NoirPill
import com.example.ui.components.NoirRoundButton
import com.example.ui.components.PersonAvatarBadge
import com.example.ui.components.SheetTop
import com.example.ui.components.SplitMoney
import com.example.ui.components.StatusBarIcons
import com.example.ui.components.glass.AcertoMotion
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.components.navBarsBottom
import com.example.ui.components.noirHeaderBackground
import com.example.ui.theme.AcertoPalette
import com.example.ui.theme.LiquidDarkTokens
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.MonthlyHistorySummary
import com.example.util.CurrencyFormatter
import com.example.util.Money

/*
 * Aba Pessoas no estilo "Recipients" da referência: a visão de longo prazo
 * de cada um (a Início mostra o mês). Grade de pessoas no cabeçalho escuro,
 * histórico mensal na folha; tocar numa pessoa abre o perfil dela.
 */

private data class PersonTotals(val person: Person, val open: Double, val paid: Double, val total: Double)

@Composable
fun PeopleScreen(
    people: List<Person>,
    installments: List<InstallmentItem>,
    history: List<MonthlyHistorySummary>,
    isPrivacyMode: Boolean,
    onAddPerson: () -> Unit,
    onEditPeople: () -> Unit,
    onReceive: (Long) -> Unit,
    onCharge: (Long) -> Unit,
    onOpenMonth: (personId: Long, year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var openId by rememberSaveable { mutableStateOf<Long?>(null) }
    val owed = installments.filter { !it.isMyShare }
    val totals = people.map { p ->
        val mine = owed.filter { it.personId == p.id }
        val open = Money.toReal(mine.filter { !it.isPaid }.sumOf { it.amount })
        val total = Money.toReal(mine.sumOf { it.amount })
        PersonTotals(p, open, total - open, total)
    }.sortedWith(compareByDescending<PersonTotals> { it.open }.thenBy { it.person.name.lowercase() })

    BackHandler(enabled = openId != null) { openId = null }

    val reduced = rememberReducedMotion()
    AnimatedContent(
        targetState = openId,
        transitionSpec = {
            if (reduced) fadeIn(tween(0)) togetherWith fadeOut(tween(0))
            else (fadeIn(tween(AcertoMotion.FAST)) + scaleIn(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut), initialScale = 0.96f)) togetherWith
                (fadeOut(tween(AcertoMotion.QUICK)) + scaleOut(tween(AcertoMotion.FAST), targetScale = 1.02f))
        },
        label = "people_profile",
        modifier = modifier.fillMaxSize()
    ) { id ->
        val t = totals.firstOrNull { it.person.id == id }
        if (id != null && t != null) {
            PersonProfile(
                totals = t,
                items = owed.filter { it.personId == id },
                isPrivacyMode = isPrivacyMode,
                onBack = { openId = null },
                onEdit = onEditPeople,
                onReceive = { onReceive(id) },
                onCharge = { onCharge(id) },
                onOpenMonth = { y, m -> onOpenMonth(id, y, m) }
            )
        } else {
            PeopleGrid(
                totals = totals,
                history = history,
                isPrivacyMode = isPrivacyMode,
                onOpen = { openId = it },
                onAddPerson = onAddPerson
            )
        }
    }
}

/* ---------------- Grade + histórico ---------------- */

@Composable
private fun PeopleGrid(
    totals: List<PersonTotals>,
    history: List<MonthlyHistorySummary>,
    isPrivacyMode: Boolean,
    onOpen: (Long) -> Unit,
    onAddPerson: () -> Unit
) {
    StatusBarIcons(lightIcons = true)
    val liquid = LocalLiquidGlassColors.current
    val reduced = rememberReducedMotion()
    val tabPosition = com.example.ui.components.LocalTabPosition.current
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val shown = if (query.isBlank()) totals else totals.filter { it.person.name.contains(query.trim(), ignoreCase = true) }
    val openTotal = totals.sumOf { it.open }

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier.fillMaxSize().background(liquid.sheetSurface).testTag("people_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 140.dp + navBarsBottom())
    ) {
        item(key = "people_header") {
            CompositionLocalProvider(LocalLiquidGlassColors provides LiquidDarkTokens) {
                Column(Modifier.fillMaxWidth().noirHeaderBackground().statusBarsPadding().padding(bottom = 30.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 12.dp)
                    ) {
                        Text("Pessoas", style = MaterialTheme.typography.headlineMedium, color = AcertoPalette.TextOnHeader, modifier = Modifier.weight(1f))
                        NoirRoundButton(
                            icon = if (searchOpen) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchOpen) "Fechar pesquisa" else "Pesquisar pessoa",
                            onClick = { searchOpen = !searchOpen; if (!searchOpen) query = "" },
                            modifier = Modifier.testTag("people_search_button")
                        )
                        Spacer(Modifier.width(8.dp))
                        NoirPill(text = "Nova", onClick = onAddPerson, light = true, icon = Icons.Default.Add, modifier = Modifier.testTag("people_add_button"))
                    }
                    Text(
                        when {
                            totals.isEmpty() -> "Ninguém cadastrado ainda"
                            openTotal > 0.001 -> "${totals.size} ${if (totals.size == 1) "pessoa" else "pessoas"} · ${CurrencyFormatter.format(openTotal, isPrivacyMode)} em aberto"
                            else -> "${totals.size} ${if (totals.size == 1) "pessoa" else "pessoas"} · todos em dia"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AcertoPalette.TextOnHeaderMuted,
                        modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                    )
                    AnimatedVisibility(visible = searchOpen) {
                        Box(
                            Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 14.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(AcertoPalette.HeaderTile)
                                .padding(horizontal = 18.dp, vertical = 13.dp)
                        ) {
                            if (query.isEmpty()) Text("Buscar pelo nome", style = MaterialTheme.typography.bodyLarge, color = AcertoPalette.TextOnHeaderMuted)
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = AcertoPalette.TextOnHeader),
                                cursorBrush = SolidColor(AcertoPalette.TextOnHeader),
                                modifier = Modifier.fillMaxWidth().testTag("people_search_input")
                            )
                        }
                    }
                    // grade de 3 colunas (como a referência)
                    val gridOrigin = remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                    val gridWidth = remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, top = 24.dp)
                            .onGloballyPositioned { gridOrigin.value = it.positionInRoot(); gridWidth.floatValue = it.size.width.toFloat() }
                    ) {
                        (shown.map<PersonTotals, PersonTotals?> { it } + listOf(null)).chunked(3).forEachIndexed { row, chunk ->
                            Row(Modifier.fillMaxWidth()) {
                                chunk.forEachIndexed { col, t ->
                                    val index = row * 3 + col
                                    val intro = remember { Animatable(if (reduced) 1f else 0f) }
                                    LaunchedEffect(Unit) {
                                        if (!reduced) {
                                            kotlinx.coroutines.delay(30L * index.coerceAtMost(12))
                                            intro.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 420f))
                                        }
                                    }
                                    val tileRoot = remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .onGloballyPositioned { tileRoot.value = it.positionInRoot() }
                                            .graphicsLayer {
                                                // vindo da Início: as fotos chegam juntas (como a pilha de
                                                // avatares de lá) e se espalham pela grade, uma de cada vez
                                                val e = tabPosition().coerceIn(0f, 1f) // 1 = ainda na Início
                                                if (!reduced && e > 0f) {
                                                    val lag = 0.07f * index.coerceAtMost(8)
                                                    val local = (((1f - e) - lag) / (1f - lag)).coerceIn(0f, 1f)
                                                    val k = 1f - androidx.compose.animation.core.FastOutSlowInEasing.transform(local)
                                                    val relX = tileRoot.value.x - gridOrigin.value.x
                                                    val relY = tileRoot.value.y - gridOrigin.value.y
                                                    val gatherX = gridWidth.floatValue * 0.55f + index.coerceAtMost(5) * 22.dp.toPx() - size.width / 2f
                                                    val gatherY = 8.dp.toPx()
                                                    translationX = (gatherX - relX) * k
                                                    translationY = (gatherY - relY) * k
                                                    val sc = 1f - 0.5f * k
                                                    scaleX = sc; scaleY = sc
                                                }
                                            }
                                    ) {
                                        if (t != null) PersonTile(t, isPrivacyMode) { onOpen(t.person.id) }
                                        else AddTile(onAddPerson)
                                    }
                                }
                                repeat(3 - chunk.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }

        item(key = "history_top") {
            SheetTop(sheetColor = liquid.sheetSurface) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text("Histórico mensal", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = liquid.textPrimary)
                    Text("O que entrou e o que falta, mês a mês", style = MaterialTheme.typography.bodyMedium, color = liquid.textSecondary)
                }
            }
        }
        if (history.isEmpty()) {
            item(key = "history_empty") {
                Text(
                    "Nada lançado ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }
        items(history, key = { "h_${it.year}_${it.month}" }) { h ->
            MonthLine(
                label = "${CurrencyFormatter.getMonthName(h.month)} ${h.year}",
                detail = if (h.installmentCount == 1) "1 parcela" else "${h.installmentCount} parcelas",
                total = h.totalAmount,
                pending = h.pendingAmount,
                isPrivacyMode = isPrivacyMode,
                onClick = null
            )
        }
    }
}

@Composable
private fun PersonTile(t: PersonTotals, isPrivacyMode: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, spring(dampingRatio = 0.5f, stiffness = 500f), label = "people_tile")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Abrir ${t.person.name}" }
            .testTag("people_tile_${t.person.id}")
    ) {
        PersonAvatarBadge(name = t.person.name, colorHex = t.person.colorHex, avatar = t.person.avatar, size = 72.dp)
        Spacer(Modifier.height(8.dp))
        Text(t.person.name, style = MaterialTheme.typography.bodyLarge, color = AcertoPalette.TextOnHeader, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            if (t.open > 0.001) CurrencyFormatter.format(t.open, isPrivacyMode) else "Em dia",
            style = MaterialTheme.typography.bodySmall,
            color = if (t.open > 0.001) AcertoPalette.TextOnHeaderMuted else AcertoPalette.Mint,
            maxLines = 1
        )
    }
}

@Composable
private fun AddTile(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClickLabel = "Adicionar pessoa", onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(AcertoPalette.PillDark),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Add, contentDescription = null, tint = AcertoPalette.TextOnHeader, modifier = Modifier.size(30.dp)) }
        Spacer(Modifier.height(8.dp))
        Text("Nova pessoa", style = MaterialTheme.typography.bodyLarge, color = AcertoPalette.TextOnHeader, maxLines = 1)
    }
}

/** Linha de um mês: rótulo, quantidade, total e quanto falta (ou "Pago"), com barrinha. */
@Composable
private fun MonthLine(
    label: String,
    detail: String,
    total: Double,
    pending: Double,
    isPrivacyMode: Boolean,
    onClick: (() -> Unit)?
) {
    val liquid = LocalLiquidGlassColors.current
    val paid = pending <= 0.001
    val progress = if (total > 0) ((total - pending) / total).toFloat().coerceIn(0f, 1f) else 0f
    Column(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = liquid.textPrimary)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = liquid.textSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(CurrencyFormatter.format(total, isPrivacyMode), style = MaterialTheme.typography.bodyLarge, color = liquid.textPrimary)
                Text(
                    if (paid) "Pago" else "Falta ${CurrencyFormatter.format(pending, isPrivacyMode)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (paid) liquid.accentPaid else liquid.accentPending
                )
            }
        }
        Box(Modifier.padding(top = 8.dp).fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(liquid.cardSurfaceVariant)) {
            Box(Modifier.fillMaxWidth(progress).height(4.dp).clip(RoundedCornerShape(2.dp)).background(if (paid) liquid.accentPaid else liquid.textPrimary))
        }
    }
}

/* ---------------- Perfil da pessoa ---------------- */

@Composable
private fun PersonProfile(
    totals: PersonTotals,
    items: List<InstallmentItem>,
    isPrivacyMode: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onReceive: () -> Unit,
    onCharge: () -> Unit,
    onOpenMonth: (Int, Int) -> Unit
) {
    StatusBarIcons(lightIcons = true)
    val liquid = LocalLiquidGlassColors.current
    val p = totals.person
    val months = items.groupBy { it.year * 12 + (it.month - 1) }.toSortedMap(compareByDescending { it })
    val purchases = items.groupBy { it.expenseId }.values
        .map { list ->
            val first = list.first()
            Triple(first, Money.toReal(list.filter { !it.isPaid }.sumOf { it.amount }), Money.toReal(list.sumOf { it.amount }))
        }
        .sortedWith(compareByDescending<Triple<InstallmentItem, Double, Double>> { it.second > 0.001 }.thenByDescending { it.second })

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(liquid.sheetSurface).testTag("person_profile"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 140.dp + navBarsBottom())
    ) {
        item(key = "profile_header") {
            CompositionLocalProvider(LocalLiquidGlassColors provides LiquidDarkTokens) {
                Column(Modifier.fillMaxWidth().noirHeaderBackground().statusBarsPadding().padding(bottom = 30.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        NoirRoundButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", onClick = onBack, modifier = Modifier.testTag("profile_back"))
                        Spacer(Modifier.weight(1f))
                        NoirRoundButton(icon = Icons.Default.Edit, contentDescription = "Editar pessoas", onClick = onEdit, modifier = Modifier.testTag("profile_edit"))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        PersonAvatarBadge(name = p.name, colorHex = p.colorHex, avatar = p.avatar, size = 88.dp)
                        Spacer(Modifier.height(10.dp))
                        Text(p.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), color = AcertoPalette.TextOnHeader, textAlign = TextAlign.Center)
                        Text(
                            if (totals.open > 0.001) "Deve no total" else "Está em dia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AcertoPalette.TextOnHeaderMuted,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        SplitMoney(
                            text = CurrencyFormatter.format(totals.open, isPrivacyMode),
                            big = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = AcertoPalette.TextOnHeader,
                            centsColor = AcertoPalette.TextOnHeaderMuted,
                            modifier = Modifier.testTag("profile_total")
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
                    ) {
                        if (totals.open > 0.001) {
                            NoirPill(text = "Receber", onClick = onReceive, light = true, icon = Icons.AutoMirrored.Filled.ArrowBack, modifier = Modifier.testTag("profile_receive"))
                            NoirPill(text = "Cobrar", onClick = onCharge, light = false, modifier = Modifier.testTag("profile_charge"))
                        } else {
                            NoirPill(text = "Ver no mês", onClick = onCharge, light = false)
                        }
                    }
                    NoirHeaderTile(
                        label = "Já pagou",
                        value = "${CurrencyFormatter.format(totals.paid, isPrivacyMode)} de ${CurrencyFormatter.format(totals.total, isPrivacyMode)}",
                        people = emptyList(),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp)
                    )
                }
            }
        }

        item(key = "months_top") {
            SheetTop(sheetColor = liquid.sheetSurface) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text("Mês a mês", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = liquid.textPrimary)
                    Text("Toque num mês para ver os itens e cobrar", style = MaterialTheme.typography.bodyMedium, color = liquid.textSecondary)
                }
            }
        }
        if (months.isEmpty()) {
            item(key = "months_empty") {
                Text("Nada lançado para ${p.name}.", style = MaterialTheme.typography.bodyMedium, color = liquid.textSecondary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))
            }
        }
        months.forEach { (idx, list) ->
            val y = idx / 12
            val m = idx % 12 + 1
            item(key = "pm_$idx") {
                MonthLine(
                    label = "${CurrencyFormatter.getMonthName(m)} $y",
                    detail = if (list.size == 1) list.first().expenseTitle else "${list.size} itens",
                    total = Money.toReal(list.sumOf { it.amount }),
                    pending = Money.toReal(list.filter { !it.isPaid }.sumOf { it.amount }),
                    isPrivacyMode = isPrivacyMode,
                    onClick = { onOpenMonth(y, m) }
                )
            }
        }

        if (purchases.isNotEmpty()) {
            item(key = "purchases_title") {
                Text(
                    "Compras e contas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = liquid.textPrimary,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp)
                )
            }
            items(purchases, key = { "pp_${it.first.expenseId}" }) { (first, open, total) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(first.expenseTitle, style = MaterialTheme.typography.bodyLarge, color = liquid.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            if (first.expenseType == ExpenseType.CARD_PURCHASE) "Cartão · ${first.totalInstallments}x · total ${CurrencyFormatter.format(total, isPrivacyMode)}"
                            else "Conta dividida · total ${CurrencyFormatter.format(total, isPrivacyMode)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = liquid.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        if (open > 0.001) CurrencyFormatter.format(open, isPrivacyMode) else "Quitada",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (open > 0.001) liquid.textPrimary else liquid.accentPaid
                    )
                }
            }
        }
    }
}
