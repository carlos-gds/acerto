package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstallmentItem
import com.example.ui.components.MyShareItemCard
import com.example.ui.components.PersonAvatarBadge
import com.example.ui.components.glass.AcertoMotion
import com.example.ui.components.glass.NumberPopIn
import com.example.ui.components.glass.SuccessCheck
import com.example.ui.components.glass.pressScale
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.components.glass.textSwapTransform
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.PersonMonthlyGroup
import com.example.util.CurrencyFormatter
import com.example.util.DebtMessageFormatter
import com.example.util.Money
import com.example.util.PixKeyStore
import com.example.util.ShareIntentHelper

/*
 * Início do Acerto (2026-11), seguindo Apple HIG + Taste Skill:
 * a tela responde uma pergunta — "quanto falta receber este mês, e de quem?".
 * O maior bloco é esse número; ações logo abaixo; depois uma linha por pessoa.
 * Superfícies sólidas, sem vidro, sombra ou degradê; vermelho só como marca.
 */

private val CardShape = RoundedCornerShape(22.dp)

/**
 * Fonte do sistema grande (acessibilidade): a partir de 130% o layout empilha
 * o que antes ficava lado a lado, como o iOS faz com Dynamic Type.
 */
@Composable
private fun isLargeFont(): Boolean = androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.3f
private val ButtonShape = RoundedCornerShape(14.dp)
private val ListShape = RoundedCornerShape(20.dp)

private fun monthLower(month: Int) = CurrencyFormatter.getMonthName(month).lowercase()

/** Resumo do mês calculado a partir das linhas (pessoas). */
private data class HomeTotals(val total: Double, val pending: Double, val paidPeople: Int, val people: Int)

private fun totalsOf(groups: List<PersonMonthlyGroup>) = HomeTotals(
    total = groups.sumOf { it.totalAmount },
    pending = groups.sumOf { it.pendingAmount },
    paidPeople = groups.count { it.pendingAmount <= 0.001 },
    people = groups.size
)

@Composable
fun HomeOverview(
    selectedYear: Int,
    selectedMonth: Int,
    isCurrentMonth: Boolean,
    showingPendingMonth: Boolean,
    groups: List<PersonMonthlyGroup>,
    myShareItems: List<InstallmentItem>,
    isPrivacyMode: Boolean,
    isSearchOpen: Boolean,
    hasSearchQuery: Boolean,
    listState: LazyListState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onSearchClick: () -> Unit,
    onAddCardPurchase: () -> Unit,
    onAddSharedBill: () -> Unit,
    onAiEntry: () -> Unit,
    onOpenPerson: (Long) -> Unit,
    onEditItem: (InstallmentItem) -> Unit,
    onDeleteItem: (InstallmentItem) -> Unit,
    searchBar: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val reduced = rememberReducedMotion()
    val totals = totalsOf(groups)

    // statusBarsPadding: o conteúdo começa abaixo da barra de status (relógio, bateria)
    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        // Cabeçalho: nome com o ponto vermelho da marca, busca e privacidade
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Text("Acerto", style = MaterialTheme.typography.displaySmall, color = liquid.textPrimary)
                Text(".", style = MaterialTheme.typography.displaySmall, color = brandColor())
            }
            RoundButton(
                onClick = onSearchClick,
                contentDescription = if (isSearchOpen) "Fechar pesquisa" else "Pesquisar",
                modifier = Modifier.testTag("home_search_button")
            ) {
                AnimatedContent(targetState = isSearchOpen, transitionSpec = { iconSwap(reduced) }, label = "search_icon") { open ->
                    Icon(if (open) Icons.Default.Close else Icons.Default.Search, contentDescription = null, tint = liquid.textPrimary)
                }
            }
            Spacer(Modifier.width(8.dp))
            RoundButton(
                onClick = onTogglePrivacy,
                contentDescription = if (isPrivacyMode) "Mostrar valores" else "Ocultar valores",
                modifier = Modifier.testTag("toggle_privacy_button")
            ) {
                AnimatedContent(targetState = isPrivacyMode, transitionSpec = { iconSwap(reduced) }, label = "eye_icon") { hidden ->
                    Icon(if (hidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = liquid.textPrimary)
                }
            }
        }

        if (isSearchOpen) searchBar()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item(key = "month") {
                MonthRow(
                    year = selectedYear,
                    month = selectedMonth,
                    isCurrentMonth = isCurrentMonth,
                    showingPendingMonth = showingPendingMonth,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth
                )
            }

            item(key = "hero") {
                HeroCard(
                    totals = totals,
                    month = selectedMonth,
                    isPrivacyMode = isPrivacyMode,
                    filtered = hasSearchQuery
                )
            }

            item(key = "actions") {
                Column(
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BigButton(
                        text = "Nova compra",
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        primary = true,
                        onClick = onAddCardPurchase,
                        modifier = Modifier.fillMaxWidth().testTag("home_new_purchase_button")
                    )
                    val split: @Composable (Modifier) -> Unit = { m ->
                        BigButton(
                            text = "Dividir conta",
                            icon = { Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null) },
                            primary = false,
                            onClick = onAddSharedBill,
                            modifier = m.testTag("home_split_button")
                        )
                    }
                    val ai: @Composable (Modifier) -> Unit = { m ->
                        BigButton(
                            text = "Com IA",
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                            primary = false,
                            onClick = onAiEntry,
                            modifier = m.testTag("home_ai_button")
                        )
                    }
                    if (isLargeFont()) {
                        split(Modifier.fillMaxWidth())
                        ai(Modifier.fillMaxWidth())
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            split(Modifier.weight(1f))
                            ai(Modifier.weight(1f))
                        }
                    }
                }
            }

            if (groups.isNotEmpty()) {
                item(key = "people_header") {
                    SectionHeader(
                        title = "Quem te deve",
                        trailing = if (groups.size == 1) "1 pessoa" else "${groups.size} pessoas"
                    )
                }
                item(key = "people_list") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ListShape)
                            .background(liquid.cardSurface)
                    ) {
                        val sorted = groups.sortedWith(
                            compareBy<PersonMonthlyGroup> { it.pendingAmount <= 0.001 }.thenByDescending { it.pendingAmount }
                        )
                        sorted.forEachIndexed { index, group ->
                            PersonRow(
                                group = group,
                                isPrivacyMode = isPrivacyMode,
                                showDivider = index > 0,
                                onClick = { onOpenPerson(group.personId) }
                            )
                        }
                    }
                }
            } else if (hasSearchQuery) {
                item(key = "no_results") {
                    Text(
                        "Nada encontrado para essa pesquisa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = liquid.textSecondary,
                        modifier = Modifier.padding(top = 20.dp, start = 4.dp)
                    )
                }
            }

            if (myShareItems.isNotEmpty()) {
                item(key = "my_share_header") {
                    SectionHeader(title = "Sua parte nas contas", trailing = null)
                }
                items(myShareItems, key = { "my_share_${it.id}" }) { item ->
                    MyShareItemCard(
                        item = item,
                        isPrivacyMode = isPrivacyMode,
                        onEdit = onEditItem,
                        onDelete = onDeleteItem,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun brandColor(): Color {
    val liquid = LocalLiquidGlassColors.current
    return if (liquid.isDark) Color(0xFFF0354B) else liquid.pillPrimaryBg
}

private fun iconSwap(reduced: Boolean) =
    if (reduced) {
        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
    } else {
        (fadeIn(tween(AcertoMotion.FAST)) + scaleIn(tween(AcertoMotion.FAST, easing = AcertoMotion.SmoothOut), initialScale = 0.6f))
            .togetherWith(fadeOut(tween(AcertoMotion.QUICK)) + scaleOut(tween(AcertoMotion.QUICK), targetScale = 0.6f))
    }

@Composable
private fun RoundButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(44.dp)
            .pressScale(interaction, pressedScale = 0.94f)
            .clip(CircleShape)
            .background(liquid.cardSurfaceVariant)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClickLabel = contentDescription, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun MonthRow(
    year: Int,
    month: Int,
    isCurrentMonth: Boolean,
    showingPendingMonth: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val reduced = rememberReducedMotion()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        ArrowButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mês anterior", onPrevious, Modifier.testTag("month_previous_button"))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = Triple(year, month, isCurrentMonth to showingPendingMonth),
                transitionSpec = { textSwapTransform(reduced) },
                label = "month_label"
            ) { (y, m, flags) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${CurrencyFormatter.getMonthName(m)} $y",
                        style = MaterialTheme.typography.titleMedium,
                        color = liquid.textPrimary,
                        modifier = Modifier.testTag("month_label")
                    )
                    val hint = when {
                        flags.second -> "pendências"
                        flags.first -> "este mês"
                        else -> null
                    }
                    if (hint != null) {
                        Text(
                            hint,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (flags.second) liquid.accentPending else liquid.textSecondary
                        )
                    }
                }
            }
        }
        ArrowButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo mês", onNext, Modifier.testTag("month_next_button"))
    }
}

@Composable
private fun ArrowButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val liquid = LocalLiquidGlassColors.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(44.dp)
            .pressScale(interaction, pressedScale = 0.92f)
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = liquid.textSecondary, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun HeroCard(totals: HomeTotals, month: Int, isPrivacyMode: Boolean, filtered: Boolean) {
    val liquid = LocalLiquidGlassColors.current
    val reduced = rememberReducedMotion()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(liquid.cardSurface)
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp)
            .testTag("monthly_summary_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            totals.people == 0 && !filtered -> {
                Text("Nada a receber em ${monthLower(month)}", style = MaterialTheme.typography.bodyMedium, color = liquid.textSecondary)
                Text("Nenhuma parcela", style = MaterialTheme.typography.headlineMedium, color = liquid.textPrimary)
                Text(
                    "Compras feitas no seu cartão para outras pessoas aparecem aqui no mês de cada parcela.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
            }
            totals.people > 0 && totals.pending <= 0.001 -> {
                Text("Falta receber em ${monthLower(month)}", style = MaterialTheme.typography.bodyMedium, color = liquid.textSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SuccessCheck(background = liquid.accentPaid, tint = liquid.cardSurface, size = 32.dp)
                    Text("Tudo recebido", style = MaterialTheme.typography.headlineMedium, color = liquid.textPrimary)
                }
                Text(
                    "${CurrencyFormatter.format(totals.total, isPrivacyMode)} de ${peopleLabel(totals.people)}. Todos pagaram.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
                ProgressBar(fraction = 1f, reduced = reduced)
            }
            else -> {
                Text("Falta receber em ${monthLower(month)}", style = MaterialTheme.typography.bodyMedium, color = liquid.textSecondary)
                NumberPopIn(
                    text = CurrencyFormatter.format(totals.pending, isPrivacyMode),
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-1).sp),
                    color = liquid.textPrimary,
                    modifier = Modifier.testTag("home_pending_total")
                )
                Text(
                    "de ${CurrencyFormatter.format(totals.total, isPrivacyMode)} · ${totals.paidPeople} de ${peopleLabel(totals.people)} " +
                        if (totals.people == 1) "pagou" else "pagaram",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
                val fraction = if (totals.total > 0) ((totals.total - totals.pending) / totals.total).toFloat() else 0f
                ProgressBar(fraction = fraction, reduced = reduced)
            }
        }
    }
}

private fun peopleLabel(n: Int) = if (n == 1) "1 pessoa" else "$n pessoas"

@Composable
private fun ProgressBar(fraction: Float, reduced: Boolean) {
    val liquid = LocalLiquidGlassColors.current
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(if (reduced) 0 else AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut),
        label = "hero_progress"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(liquid.cardSurfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(liquid.accentPaid)
        )
    }
}

@Composable
private fun BigButton(
    text: String,
    icon: @Composable () -> Unit,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val interaction = remember { MutableInteractionSource() }
    val bg = if (primary) liquid.pillPrimaryBg else liquid.cardSurfaceVariant
    val fg = if (primary) liquid.pillPrimaryText else liquid.textPrimary
    Row(
        modifier = modifier
            .heightIn(min = 50.dp)
            .pressScale(interaction, pressedScale = 0.97f)
            .clip(ButtonShape)
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides fg) {
            Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) { icon() }
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.titleMedium, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String?) {
    val liquid = LocalLiquidGlassColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 22.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = liquid.textPrimary, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodyMedium, color = liquid.textSecondary)
        }
    }
}

@Composable
private fun PersonRow(
    group: PersonMonthlyGroup,
    isPrivacyMode: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val paid = group.pendingAmount <= 0.001
    val subtitle = if (group.items.size == 1) {
        val it = group.items.first()
        "${it.expenseTitle} · ${it.installmentNumber} de ${it.totalInstallments}"
    } else {
        "${group.items.size} itens neste mês"
    }
    val statusColor by animateColorAsState(
        if (paid) liquid.accentPaid else liquid.accentPending,
        animationSpec = tween(AcertoMotion.FAST),
        label = "row_status"
    )
    Column {
        if (showDivider) {
            Box(
                Modifier
                    .padding(start = 72.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(liquid.cardBorderSubtle)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 70.dp)
                .clickable(role = Role.Button, onClickLabel = "Abrir ${group.personName}", onClick = onClick)
                .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp)
                .testTag("person_row_${group.personId}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val large = isLargeFont()
            val amountText = CurrencyFormatter.format(if (paid) group.totalAmount else group.pendingAmount, isPrivacyMode)
            val statusText = if (paid) "Pago" else "Pendente"
            PersonAvatarBadge(name = group.personName, colorHex = group.colorHex, avatar = group.avatar, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    group.personName, style = MaterialTheme.typography.titleMedium, color = liquid.textPrimary,
                    maxLines = if (large) 2 else 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle, style = MaterialTheme.typography.bodyMedium, color = liquid.textSecondary,
                    maxLines = if (large) 2 else 1, overflow = TextOverflow.Ellipsis
                )
                if (large) {
                    Text(amountText, style = MaterialTheme.typography.titleMedium, color = liquid.textPrimary)
                    Text(statusText, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = statusColor)
                }
            }
            if (!large) {
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(amountText, style = MaterialTheme.typography.titleMedium, color = liquid.textPrimary)
                    Text(statusText, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = statusColor)
                }
            }
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = liquid.textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        }
    }
}

/* ---------------- Tela da pessoa (lado a lado com a Início) ---------------- */

@Composable
fun PersonMonthDetail(
    group: PersonMonthlyGroup,
    selectedYear: Int,
    selectedMonth: Int,
    isPrivacyMode: Boolean,
    onBack: () -> Unit,
    onTogglePayment: (InstallmentItem) -> Unit,
    onEditItem: (InstallmentItem) -> Unit,
    onDeleteItem: (InstallmentItem) -> Unit,
    onPartialPayment: (InstallmentItem, Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val context = LocalContext.current
    var partialFor by remember { mutableStateOf<InstallmentItem?>(null) }
    partialFor?.let { target ->
        PartialPaymentDialog(
            item = target,
            onDismiss = { partialFor = null },
            onConfirm = { amount -> partialFor = null; onPartialPayment(target, amount) }
        )
    }
    val reduced = rememberReducedMotion()
    var confirmPrivacyShare by remember { mutableStateOf(false) }
    // Nome curto vai inteiro ("Vó Maria"); nome longo, só o primeiro ("Cobrar Ana").
    val firstName = group.personName.trim().let { n -> if (n.length <= 16) n else n.substringBefore(' ') }

    fun share() {
        val text = DebtMessageFormatter.format(
            group = group,
            scope = DebtMessageFormatter.DebtScope.SELECTED_MONTH,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            pixKey = PixKeyStore.get(context),
            pixName = com.example.util.PixKeyStore.getName(context),
            pixCity = com.example.util.PixKeyStore.getCity(context)
        )
        ShareIntentHelper.sharePlainText(context = context, text = text, chooserTitle = "Cobrar ${group.personName}")
    }

    if (confirmPrivacyShare) {
        AlertDialog(
            onDismissRequest = { confirmPrivacyShare = false },
            containerColor = liquid.sheetSurface,
            title = { Text("Enviar com valores?", style = MaterialTheme.typography.titleLarge, color = liquid.textPrimary) },
            text = {
                Text(
                    "O modo privacidade está ligado, mas a mensagem para ${group.personName} vai com os valores reais.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmPrivacyShare = false; share() }, modifier = Modifier.testTag("confirm_privacy_share_btn")) {
                    Text("Enviar", color = liquid.textPrimary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmPrivacyShare = false }) { Text("Cancelar", color = liquid.textSecondary) }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
    ) {
        item(key = "back") {
            val interaction = remember { MutableInteractionSource() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .pressScale(interaction, pressedScale = 0.97f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onBack)
                    .padding(end = 10.dp)
                    .testTag("person_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = liquid.textPrimary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(6.dp))
                Text("Início", style = MaterialTheme.typography.bodyLarge, color = liquid.textPrimary)
            }
        }
        item(key = "person_header") {
            Column(
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PersonAvatarBadge(name = group.personName, colorHex = group.colorHex, avatar = group.avatar, size = 64.dp)
                Text(group.personName, style = MaterialTheme.typography.headlineMedium, color = liquid.textPrimary)
                AnimatedContent(
                    targetState = group.pendingAmount <= 0.001,
                    transitionSpec = { textSwapTransform(reduced) },
                    label = "person_status"
                ) { allPaid ->
                    Text(
                        if (allPaid) "Pagou tudo de ${monthLower(selectedMonth)}."
                        else "Falta ${CurrencyFormatter.format(group.pendingAmount, isPrivacyMode)} em ${monthLower(selectedMonth)}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = liquid.textSecondary
                    )
                }
            }
        }
        item(key = "installments") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ListShape)
                    .background(liquid.cardSurface)
            ) {
                group.items.forEachIndexed { index, item ->
                    InstallmentLine(
                        item = item,
                        isPrivacyMode = isPrivacyMode,
                        showDivider = index > 0,
                        onToggle = { onTogglePayment(item) },
                        onEdit = { onEditItem(item) },
                        onDelete = { onDeleteItem(item) },
                        onPartial = { partialFor = item }
                    )
                }
            }
        }
        item(key = "charge") {
            BigButton(
                text = "Cobrar $firstName",
                icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                primary = true,
                onClick = { if (isPrivacyMode) confirmPrivacyShare = true else share() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("person_charge_button")
            )
            Text(
                "A mensagem leva as parcelas de ${monthLower(selectedMonth)} e a sua chave PIX, se estiver salva em Ajustes.",
                style = MaterialTheme.typography.bodySmall,
                color = liquid.textSecondary,
                modifier = Modifier.padding(top = 10.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun InstallmentLine(
    item: InstallmentItem,
    isPrivacyMode: Boolean,
    showDivider: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPartial: () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val reduced = rememberReducedMotion()
    var menuOpen by remember { mutableStateOf(false) }
    Column {
        if (showDivider) {
            Box(Modifier.padding(start = 16.dp).fillMaxWidth().height(1.dp).background(liquid.cardBorderSubtle))
        }
        val large = isLargeFont()
        if (large) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
                Text(item.expenseTitle, style = MaterialTheme.typography.titleMedium, color = liquid.textPrimary)
                Text(
                    "Parcela ${item.installmentNumber} de ${item.totalInstallments}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
                Text(
                    CurrencyFormatter.format(Money.toReal(item.amount), isPrivacyMode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (large) 56.dp else 72.dp)
                .padding(start = 16.dp, end = 4.dp, top = if (large) 4.dp else 12.dp, bottom = 12.dp)
                .testTag("installment_line_${item.id}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                if (!large) {
                    Text(item.expenseTitle, style = MaterialTheme.typography.titleMedium, color = liquid.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "Parcela ${item.installmentNumber} de ${item.totalInstallments} · ${CurrencyFormatter.format(Money.toReal(item.amount), isPrivacyMode)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = liquid.textSecondary
                    )
                }
            }
            val interaction = remember { MutableInteractionSource() }
            val bg by animateColorAsState(
                if (item.isPaid) Color.Transparent else liquid.cardSurfaceVariant,
                animationSpec = tween(AcertoMotion.FAST, easing = AcertoMotion.SmoothOut),
                label = "pay_bg"
            )
            Box(
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .pressScale(interaction, pressedScale = 0.96f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(bg)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = if (item.isPaid) "Desmarcar pagamento" else "Marcar como pago",
                        onClick = onToggle
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("toggle_paid_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(targetState = item.isPaid, transitionSpec = { textSwapTransform(reduced) }, label = "pay_label") { isPaid ->
                    if (isPaid) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SuccessCheck(background = liquid.accentPaid, tint = liquid.cardSurface, size = 20.dp)
                            Text("Pago", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = liquid.accentPaid)
                        }
                    } else {
                        Text("Marcar pago", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = liquid.textPrimary)
                    }
                }
            }
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClickLabel = "Mais opções") { menuOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = liquid.textSecondary)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (!item.isPaid) {
                        DropdownMenuItem(text = { Text("Pagou uma parte") }, onClick = { menuOpen = false; onPartial() })
                    }
                    DropdownMenuItem(text = { Text("Editar") }, onClick = { menuOpen = false; onEdit() })
                    DropdownMenuItem(
                        text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                        onClick = { menuOpen = false; onDelete() }
                    )
                }
            }
        }
    }
}

/** Diálogo do pagamento parcial: quanto a pessoa pagou desta parcela. */
@Composable
private fun PartialPaymentDialog(
    item: InstallmentItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val total = Money.toReal(item.amount)
    var text by remember { mutableStateOf("") }
    val value = com.example.util.MoneyInput.parse(text)
    val error = when {
        text.isBlank() -> null
        value == null || value <= 0.0 -> "Digite um valor válido"
        value >= total -> "Para o valor inteiro, use Marcar pago"
        else -> null
    }
    val canConfirm = value != null && value > 0.0 && value < total
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = liquid.sheetSurface,
        title = { Text("Quanto foi pago?", style = MaterialTheme.typography.titleLarge, color = liquid.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "${item.expenseTitle}, parcela ${item.installmentNumber} de ${item.totalInstallments}: ${CurrencyFormatter.format(total)}. O que faltar continua pendente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
                androidx.compose.material3.OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(14) },
                    singleLine = true,
                    label = { Text("Valor pago") },
                    prefix = { Text("R$ ") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    colors = com.example.ui.components.glass.liquidTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("partial_payment_input")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { value?.let(onConfirm) },
                enabled = canConfirm,
                modifier = Modifier.testTag("partial_payment_confirm")
            ) { Text("Registrar", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = liquid.textSecondary) } }
    )
}
