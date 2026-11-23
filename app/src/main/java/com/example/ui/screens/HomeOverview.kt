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
import com.example.ui.theme.AcertoPalette
import com.example.ui.components.noirHeaderBackground
import com.example.ui.components.SplitMoney
import com.example.ui.components.SheetTop
import com.example.ui.components.PeekingCardStack
import com.example.ui.components.NoirHeaderTile
import com.example.ui.components.NoirRoundButton
import com.example.ui.components.NoirPill
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Today
import com.example.ui.components.acertoCard
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.Autorenew
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
import androidx.compose.ui.semantics.customActions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Check
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
    modifier: Modifier = Modifier,
    /** Todas as parcelas: previsão dos próximos meses e comparação com o mês anterior. */
    allInstallments: List<InstallmentItem> = emptyList(),
    /** Contas fixas (divididas e recorrentes) do mês, para a pílula de resumo. */
    recurring: com.example.util.Forecast.Recurring? = null,
    onSelectMonth: (Int, Int) -> Unit = { _, _ -> },
    /** Volta para o mês de hoje (aparece quando outro mês está aberto). */
    onGoToCurrentMonth: () -> Unit = {},
    /** Abre o fluxo "Receber" (escolher pessoa, digitar valor, deslizar para confirmar). */
    onReceive: () -> Unit = {},
    /** Abre a aba Cartão (toque nos cartões empilhados). */
    onOpenCards: () -> Unit = {},
    /** Quantas compras no cartão existem (cartões empilhados no cabeçalho). */
    cardCount: Int = 0,
    /** Foto de perfil: abre os Ajustes. */
    onOpenProfile: () -> Unit = {},
    /** Deslizar a linha da pessoa para a esquerda: marca o mês dela como pago. */
    onMarkPersonPaid: (PersonMonthlyGroup) -> Unit = {}
) {
    val liquid = LocalLiquidGlassColors.current
    val reduced = rememberReducedMotion()
    val totals = totalsOf(groups)
    val forecast = remember(allInstallments, selectedYear, selectedMonth) {
        com.example.util.Forecast.nextMonths(allInstallments, selectedYear, selectedMonth, 6)
    }
    var moreMenu by remember { mutableStateOf(false) }

    // Cabeçalho escuro: ícones claros na barra de status
    com.example.ui.components.StatusBarIcons(lightIcons = true)

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(liquid.sheetSurface),
        contentPadding = PaddingValues(bottom = 140.dp + com.example.ui.components.navBarsBottom())
    ) {
        // ---------- Cabeçalho escuro ----------
        item(key = "noir_header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noirHeaderBackground()
                    .statusBarsPadding()
                    .padding(top = 8.dp, bottom = 34.dp)
            ) {
                PeekingCardStack(count = cardCount, onClick = onOpenCards, modifier = Modifier.padding(top = 128.dp))
                Column(Modifier.fillMaxWidth()) {
                    // Linha de cima: marca, mês e botões
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp)
                    ) {
                        com.example.ui.components.ProfileAvatar(size = 48.dp, onClick = onOpenProfile)
                        Spacer(Modifier.weight(1f))
                        NoirRoundButton(
                            icon = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchOpen) "Fechar pesquisa" else "Pesquisar",
                            onClick = onSearchClick,
                            modifier = Modifier.testTag("home_search_button")
                        )
                        Spacer(Modifier.width(8.dp))
                        NoirRoundButton(
                            icon = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPrivacyMode) "Mostrar valores" else "Ocultar valores",
                            onClick = onTogglePrivacy,
                            modifier = Modifier.testTag("toggle_privacy_button")
                        )
                    }

                    // Mês (setas discretas)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 18.dp)
                    ) {
                        NoirMonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mês anterior", onPreviousMonth, Modifier.testTag("month_previous_button"))
                        AnimatedContent(
                            targetState = selectedYear to selectedMonth,
                            transitionSpec = { textSwapTransform(reduced) },
                            label = "noir_month"
                        ) { (y, m) ->
                            Text(
                                "${CurrencyFormatter.getMonthName(m)} $y",
                                style = MaterialTheme.typography.bodyLarge,
                                color = AcertoPalette.TextOnHeaderMuted,
                                modifier = Modifier.testTag("month_label")
                            )
                        }
                        NoirMonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo mês", onNextMonth, Modifier.testTag("month_next_button"))
                        if (showingPendingMonth) {
                            Text(
                                "pendências",
                                style = MaterialTheme.typography.labelMedium,
                                color = AcertoPalette.Yellow,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                    if (!isCurrentMonth) {
                        Row(Modifier.padding(start = 20.dp)) {
                            NoirChip(text = "Ir para o mês atual", onClick = onGoToCurrentMonth, modifier = Modifier.testTag("go_to_current_month"))
                        }
                    }

                    // Valor principal
                    Column(Modifier.padding(start = 20.dp, end = 100.dp, top = 6.dp)) {
                        Text(
                            if (totals.people > 0 && totals.pending <= 0.001) "Tudo recebido em ${monthLower(selectedMonth)}" else "Falta receber",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AcertoPalette.TextOnHeaderMuted
                        )
                        SplitMoney(
                            text = CurrencyFormatter.format(if (totals.pending > 0.001) totals.pending else totals.total, isPrivacyMode),
                            big = MaterialTheme.typography.displaySmall.copy(fontSize = 46.sp, lineHeight = 52.sp, letterSpacing = (-1).sp),
                            color = AcertoPalette.TextOnHeader,
                            centsColor = AcertoPalette.TextOnHeaderMuted,
                            modifier = Modifier.testTag("home_pending_total")
                        )
                    }

                    // Pílulas de ação
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 20.dp, top = 14.dp)
                    ) {
                        NoirPill(
                            text = "Receber",
                            onClick = onReceive,
                            light = true,
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            modifier = Modifier.testTag("home_receive_button")
                        )
                        NoirPill(
                            text = "Compra",
                            onClick = onAddCardPurchase,
                            light = false,
                            icon = Icons.Default.Add,
                            modifier = Modifier.testTag("home_new_purchase_button")
                        )
                        Box {
                            NoirRoundButton(
                                icon = Icons.Default.MoreHoriz,
                                contentDescription = "Mais ações",
                                onClick = { moreMenu = true },
                                modifier = Modifier.testTag("home_more_button")
                            )
                            DropdownMenu(expanded = moreMenu, onDismissRequest = { moreMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Dividir conta") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null) },
                                    onClick = { moreMenu = false; onAddSharedBill() },
                                    modifier = Modifier.testTag("home_split_button")
                                )
                                DropdownMenuItem(
                                    text = { Text("Lançar com IA") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                                    onClick = { moreMenu = false; onAiEntry() },
                                    modifier = Modifier.testTag("home_ai_button")
                                )
                            }
                        }
                    }

                    // Bloco "Recebido" com quem deve
                    androidx.compose.runtime.CompositionLocalProvider(
                        com.example.ui.theme.LocalLiquidGlassColors provides com.example.ui.theme.LiquidDarkTokens
                    ) { NoirHeaderTile(
                        label = "Recebido de ${peopleLabel(totals.people)}",
                        value = "${CurrencyFormatter.format(totals.total - totals.pending, isPrivacyMode)} de ${CurrencyFormatter.format(totals.total, isPrivacyMode)}",
                        people = groups.map { Triple(it.personName, it.colorHex, it.avatar) },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 26.dp).testTag("monthly_summary_card")
                    ) }
                }
            }
        }

        // ---------- Folha branca ----------
        item(key = "sheet_top") {
            SheetTop(sheetColor = liquid.sheetSurface) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp)
                ) {
                    SheetIconButton(Icons.Default.FilterList, "Filtrar", onSearchClick)
                    Text(
                        "Quem te deve",
                        style = MaterialTheme.typography.titleMedium,
                        color = liquid.textPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    SheetIconButton(Icons.Default.Search, "Pesquisar", onSearchClick)
                }
                if (isSearchOpen) searchBar()
            }
        }

        if (groups.isNotEmpty()) {
            val sorted = groups.sortedWith(
                compareBy<PersonMonthlyGroup> { it.pendingAmount <= 0.001 }.thenByDescending { it.pendingAmount }
            )
            items(sorted, key = { "person_${it.personId}" }) { group ->
                SwipeToPayRow(group = group, onPaid = { onMarkPersonPaid(group) }) {
    NoirPersonRow(group = group, isPrivacyMode = isPrivacyMode, onClick = { onOpenPerson(group.personId) })
                }
            }
        } else {
            item(key = "empty") {
                Text(
                    if (hasSearchQuery) "Nada encontrado para essa pesquisa."
                    else "Nenhuma parcela em ${monthLower(selectedMonth)}. Toque em Nova compra para lançar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                )
            }
        }

        // Destaques automáticos (sempre a partir do mês de hoje)
        if (!hasSearchQuery) {
            item(key = "insights") {
                val today = remember { java.util.Calendar.getInstance() }
                val insights = remember(allInstallments, isPrivacyMode) {
                    com.example.util.Insights.build(
                        allInstallments,
                        today.get(java.util.Calendar.YEAR),
                        today.get(java.util.Calendar.MONTH) + 1,
                        isPrivacyMode
                    )
                }
                if (insights.isNotEmpty()) InsightsRow(insights = insights, onOpenPerson = onOpenPerson)
            }
        }

        if (!hasSearchQuery && forecast.any { it.amount > 0.0 }) {
            item(key = "forecast_header") {
                val sum = forecast.sumOf { it.amount }
                val last = forecast.last()
                Box(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(
                        title = "Próximos meses",
                        trailing = "${CurrencyFormatter.format(sum, isPrivacyMode)} até ${CurrencyFormatter.getMonthShortName(last.month).lowercase()}"
                    )
                }
            }
            item(key = "forecast_chart") {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    ForecastChart(months = forecast, isPrivacyMode = isPrivacyMode, onSelect = { y, m -> onSelectMonth(y, m) })
                }
            }
        }

        if (recurring != null && !hasSearchQuery) {
            item(key = "recurring_pill") {
                Box(Modifier.padding(horizontal = 16.dp)) { RecurringPill(recurring = recurring, isPrivacyMode = isPrivacyMode) }
            }
        }

        if (myShareItems.isNotEmpty()) {
            item(key = "my_share_header") {
                Box(Modifier.padding(horizontal = 16.dp)) { SectionHeader(title = "Sua parte nas contas", trailing = null) }
            }
            items(myShareItems, key = { "my_share_${it.id}" }) { item ->
                MyShareItemCard(
                    item = item,
                    isPrivacyMode = isPrivacyMode,
                    onEdit = onEditItem,
                    onDelete = onDeleteItem,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                )
            }
        }
    }
}

/** Seta do mês sobre o cabeçalho escuro. */
@Composable
private fun NoirMonthArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = label, tint = AcertoPalette.TextOnHeaderMuted, modifier = Modifier.size(22.dp)) }
}

/** Chip escuro do cabeçalho. */
@Composable
private fun NoirChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AcertoPalette.PillDark)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(Icons.Default.Today, contentDescription = null, tint = AcertoPalette.TextOnHeader, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = AcertoPalette.TextOnHeader)
    }
}

/** Botão redondo cinza da folha branca (filtro, busca). */
@Composable
private fun SheetIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val liquid = LocalLiquidGlassColors.current
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(liquid.cardSurface)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = label, tint = liquid.textSecondary, modifier = Modifier.size(22.dp)) }
}

/** Linha da folha branca: avatar com bolinha de status, nome e valor (como "Transactions"). */
@Composable
private fun NoirPersonRow(group: PersonMonthlyGroup, isPrivacyMode: Boolean, onClick: () -> Unit) {
    val liquid = LocalLiquidGlassColors.current
    val paid = group.pendingAmount <= 0.001
    val large = isLargeFont()
    val subtitle = if (group.items.size == 1) {
        val it = group.items.first()
        "${it.expenseTitle} · ${it.installmentNumber} de ${it.totalInstallments}"
    } else "${group.items.size} itens neste mês"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clickable(role = Role.Button, onClickLabel = "Abrir ${group.personName}", onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("person_row_${group.personId}")
    ) {
        Box {
            PersonAvatarBadge(name = group.personName, colorHex = group.colorHex, avatar = group.avatar, size = 46.dp)
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(liquid.sheetSurface)
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(if (paid) AcertoPalette.Mint else AcertoPalette.Yellow)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(group.personName, style = MaterialTheme.typography.bodyLarge, color = liquid.textPrimary, maxLines = if (large) 2 else 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = liquid.textSecondary, maxLines = if (large) 2 else 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                CurrencyFormatter.format(if (paid) group.totalAmount else group.pendingAmount, isPrivacyMode),
                style = MaterialTheme.typography.bodyLarge,
                color = if (paid) liquid.accentPaid else liquid.textPrimary
            )
            Text(if (paid) "Pago" else "Pendente", style = MaterialTheme.typography.labelMedium, color = if (paid) liquid.accentPaid else liquid.textSecondary)
        }
    }
}

@Composable
private fun brandColor(): Color {
    val liquid = LocalLiquidGlassColors.current
    return liquid.textPrimary
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
    onNext: () -> Unit,
    onGoToCurrent: () -> Unit = {}
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (hint != null) {
                            Text(
                                hint,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (flags.second) liquid.accentPending else liquid.textSecondary
                            )
                        }
                        if (!flags.first) {
                            GoToCurrentMonthChip(onClick = onGoToCurrent)
                        }
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
private fun HeroCard(
    totals: HomeTotals,
    groups: List<PersonMonthlyGroup>,
    month: Int,
    prevMonth: Int,
    prevTotal: Double,
    isPrivacyMode: Boolean,
    filtered: Boolean
) {
    val liquid = LocalLiquidGlassColors.current
    val reduced = rememberReducedMotion()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .acertoCard(CardShape)
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 18.dp)
            .testTag("monthly_summary_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (totals.people == 0 && !filtered) {
            Text("Nada a receber em ${monthLower(month)}", style = MaterialTheme.typography.bodyMedium, color = liquid.textSecondary)
            Text("Nenhuma parcela", style = MaterialTheme.typography.headlineMedium, color = liquid.textPrimary)
            Text(
                "Compras feitas no seu cartão para outras pessoas aparecem aqui no mês de cada parcela.",
                style = MaterialTheme.typography.bodyMedium,
                color = liquid.textSecondary
            )
            return@Column
        }

        // Linha de cima: pergunta da tela + quem deve (avatares empilhados)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Falta receber em ${monthLower(month)}",
                style = MaterialTheme.typography.bodyMedium,
                color = liquid.textSecondary,
                modifier = Modifier.weight(1f)
            )
            if (groups.isNotEmpty()) {
                com.example.ui.components.StackedAvatars(
                    people = groups.map { Triple(it.personName, it.colorHex, it.avatar) },
                    size = 30.dp
                )
            }
        }

        if (totals.pending <= 0.001) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SuccessCheck(background = liquid.accentPaid, tint = liquid.cardSurface, size = 32.dp)
                Text("Tudo recebido", style = MaterialTheme.typography.headlineMedium, color = liquid.textPrimary)
            }
        } else {
            NumberPopIn(
                text = CurrencyFormatter.format(totals.pending, isPrivacyMode),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-1).sp),
                color = liquid.textPrimary,
                modifier = Modifier.testTag("home_pending_total")
            )
        }

        // Comparação com o mês anterior (neutra: receber mais não é bom nem ruim)
        if (prevTotal > 0.0 && !isPrivacyMode) {
            val diff = totals.total - prevTotal
            val text = when {
                kotlin.math.abs(diff) < 0.01 -> "Igual a ${monthLower(prevMonth)}"
                diff > 0 -> "▲ ${CurrencyFormatter.format(diff)} a mais que ${monthLower(prevMonth)}"
                else -> "▼ ${CurrencyFormatter.format(-diff)} a menos que ${monthLower(prevMonth)}"
            }
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = liquid.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(liquid.cardSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("home_compare_badge")
            )
        }

        val fraction = if (totals.total > 0) ((totals.total - totals.pending) / totals.total).toFloat() else 0f
        ProgressBar(fraction = fraction, reduced = reduced)

        // Números em pares: recebido e total do mês
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                label = "Recebido",
                value = CurrencyFormatter.format(totals.total - totals.pending, isPrivacyMode),
                detail = "${totals.paidPeople} de ${peopleLabel(totals.people)}",
                valueColor = liquid.accentPaid,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = "Total do mês",
                value = CurrencyFormatter.format(totals.total, isPrivacyMode),
                detail = if (totals.people == 1) "1 pessoa" else "${totals.people} pessoas",
                valueColor = liquid.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, detail: String, valueColor: Color, modifier: Modifier = Modifier) {
    val liquid = LocalLiquidGlassColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(liquid.cardSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = liquid.textSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(detail, style = MaterialTheme.typography.bodySmall, color = liquid.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Colunas dos próximos 6 meses; o mês aberto fica na cor da marca. Toque abre o mês. */
@Composable
private fun ForecastChart(
    months: List<com.example.util.Forecast.MonthAmount>,
    isPrivacyMode: Boolean,
    onSelect: (Int, Int) -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val reduced = rememberReducedMotion()
    val brand = brandColor()
    val max = (months.maxOfOrNull { it.amount } ?: 0.0).coerceAtLeast(0.01)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .acertoCard(ListShape)
            .padding(horizontal = 10.dp, vertical = 16.dp)
            .testTag("forecast_chart"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        months.forEachIndexed { i, m ->
            val target = (m.amount / max).toFloat().coerceIn(0f, 1f)
            val h by animateFloatAsState(
                targetValue = target,
                animationSpec = tween(if (reduced) 0 else AcertoMotion.SLOW, delayMillis = if (reduced) 0 else i * AcertoMotion.STAGGER, easing = AcertoMotion.SmoothOut),
                label = "bar_$i"
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button, onClickLabel = "Abrir ${CurrencyFormatter.getMonthName(m.month)}") { onSelect(m.year, m.month) }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    if (isPrivacyMode) "•••" else compactMoney(m.amount),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == 0) liquid.textPrimary else liquid.textSecondary,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier.height(96.dp).width(26.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(if (m.amount > 0) h.coerceAtLeast(0.04f) else 0.04f)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (i == 0) brand else liquid.textSecondary.copy(alpha = if (liquid.isDark) 0.35f else 0.28f))
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    CurrencyFormatter.getMonthShortName(m.month).lowercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (i == 0) liquid.textPrimary else liquid.textSecondary
                )
            }
        }
    }
}

/** R$ 1,2 mil / R$ 980: cabe embaixo de cada coluna. */
private fun compactMoney(v: Double): String = when {
    v <= 0.0 -> "—"
    v >= 1000 -> "R$ " + String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.1f", v / 1000).replace(",0", "") + " mil"
    else -> "R$ " + kotlin.math.round(v).toInt()
}

/** Pílula de resumo das contas fixas divididas do mês. */
@Composable
private fun RecurringPill(recurring: com.example.util.Forecast.Recurring, isPrivacyMode: Boolean) {
    val liquid = LocalLiquidGlassColors.current
    val label = if (recurring.count == 1) "1 conta fixa dividida" else "${recurring.count} contas fixas divididas"
    Row(
        modifier = Modifier
            .padding(top = 14.dp)
            .fillMaxWidth()
            .acertoCard(RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("recurring_pill"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Autorenew, contentDescription = null, tint = liquid.textSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = liquid.textPrimary)
            Text(
                "Sua parte ${CurrencyFormatter.format(recurring.myShareMonthly, isPrivacyMode)} por mês · outros ${CurrencyFormatter.format(recurring.othersMonthly, isPrivacyMode)}",
                style = MaterialTheme.typography.bodySmall,
                color = liquid.textSecondary
            )
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
    /** Abre o fluxo Receber já com esta pessoa. */
    onReceive: () -> Unit = {},
    modifier: Modifier = Modifier,
    /** Quanto a pessoa ainda deve no total e quando quita (todas as parcelas futuras). */
    payoff: com.example.util.Forecast.Payoff? = null
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
            pixBank = com.example.util.PixKeyStore.getBank(context),
            pixCity = com.example.util.PixKeyStore.getCity(context)
        )
        // Cobrança como imagem + texto (o texto traz a chave PIX para copiar)
        val image = com.example.util.ChargeImage.render(
            personName = group.personName,
            month = selectedMonth,
            year = selectedYear,
            items = group.items,
            pixKey = PixKeyStore.get(context),
            pixName = PixKeyStore.getName(context),
            pixBank = PixKeyStore.getBank(context)
        )
        com.example.util.ChargeImage.share(context, image, text, "Cobrar ${group.personName}")
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
        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp + com.example.ui.components.navBarsBottom())
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
                if (payoff != null) {
                    val sameMonth = payoff.lastYear == selectedYear && payoff.lastMonth == selectedMonth
                    Text(
                        if (sameMonth) "Esta é a última parcela."
                        else "Faltam ${CurrencyFormatter.format(payoff.remaining, isPrivacyMode)} no total · quita em " +
                            "${CurrencyFormatter.getMonthName(payoff.lastMonth).lowercase()} de ${payoff.lastYear}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = liquid.textSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(liquid.cardSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("person_payoff")
                    )
                }
            }
        }
        item(key = "installments") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .acertoCard(ListShape)
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
            if (group.pendingAmount > 0.001) {
                BigButton(
                    text = "Receber de $firstName",
                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) },
                    primary = true,
                    onClick = onReceive,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("person_receive_button")
                )
            }
            BigButton(
                text = "Cobrar $firstName",
                icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                primary = group.pendingAmount <= 0.001,
                onClick = { if (isPrivacyMode) confirmPrivacyShare = true else share() },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).testTag("person_charge_button")
            )
            Text(
                "Vai uma imagem com as parcelas de ${monthLower(selectedMonth)} e o total, e uma mensagem com a sua chave PIX, se estiver salva em Ajustes.",
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

/** Atalho "Ir para o mês atual", mostrado quando outro mês está aberto. */
@Composable
fun GoToCurrentMonthChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val liquid = LocalLiquidGlassColors.current
    val interaction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(top = 4.dp)
            .heightIn(min = 32.dp)
            .pressScale(interaction, pressedScale = 0.95f)
            .clip(RoundedCornerShape(16.dp))
            .background(liquid.cardSurfaceVariant)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("go_to_current_month")
    ) {
        Icon(Icons.Default.Today, contentDescription = null, tint = liquid.textPrimary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Ir para o mês atual", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = liquid.textPrimary)
    }
}

/** Linha rolável de destaques: cartõezinhos com a cor do tipo de aviso. */
@Composable
private fun InsightsRow(
    insights: List<com.example.util.Insights.Insight>,
    onOpenPerson: (Long) -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    Column(Modifier.padding(top = 8.dp).testTag("home_insights")) {
        Box(Modifier.padding(horizontal = 16.dp)) { SectionHeader(title = "Destaques", trailing = null) }
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(insights, key = { it.kind.name }) { insight ->
                val dot = when (insight.kind) {
                    com.example.util.Insights.Kind.LATE -> com.example.ui.theme.AcertoPalette.Yellow
                    com.example.util.Insights.Kind.TOP -> com.example.ui.theme.AcertoPalette.Cards[3]
                    com.example.util.Insights.Kind.NEXT_MONTH -> com.example.ui.theme.AcertoPalette.Blue
                    com.example.util.Insights.Kind.RECEIVED -> com.example.ui.theme.AcertoPalette.Mint
                }
                val id = insight.personId
                Column(
                    Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(liquid.cardSurface)
                        .then(if (id != null) Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button) { onOpenPerson(id) } else Modifier)
                        .padding(16.dp)
                ) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(dot))
                    Spacer(Modifier.height(10.dp))
                    Text(insight.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = liquid.textPrimary, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(insight.detail, style = MaterialTheme.typography.bodySmall, color = liquid.textSecondary, maxLines = 1)
                }
            }
        }
    }
}

/**
 * Deslizar para a esquerda revela "Pago" em verde-água; soltando depois do
 * limite, o mês da pessoa é marcado como pago e a linha volta ao lugar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToPayRow(group: PersonMonthlyGroup, onPaid: () -> Unit, content: @Composable () -> Unit) {
    val pending = group.pendingAmount > 0.001
    if (!pending) { content(); return }
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    val state = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { v ->
            if (v == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onPaid()
            }
            false // sempre volta: a linha continua, agora como paga
        },
        positionalThreshold = { it * 0.4f }
    )
    androidx.compose.material3.SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        modifier = Modifier.semantics {
            customActions = listOf(
                androidx.compose.ui.semantics.CustomAccessibilityAction("Marcar ${group.personName} como pago") { onPaid(); true }
            )
        },
        backgroundContent = {
            val reached = state.targetValue == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
            val bg by androidx.compose.animation.animateColorAsState(
                if (reached) AcertoPalette.Mint else AcertoPalette.Mint.copy(alpha = 0.45f), label = "swipe_bg"
            )
            Box(
                Modifier.fillMaxSize().background(bg).padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0B2A0C), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pago", style = MaterialTheme.typography.titleSmall, color = Color(0xFF0B2A0C))
                }
            }
        }
    ) {
        Box(Modifier.background(LocalLiquidGlassColors.current.sheetSurface)) { content() }
    }
}
