package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.foundation.border
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Person
import com.example.ui.components.NoirPill
import com.example.ui.components.NoirRoundButton
import com.example.ui.components.PersonAvatarBadge
import com.example.ui.components.StatusBarIcons
import com.example.ui.components.glass.AcertoMotion
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.components.navBarsBottom
import com.example.ui.components.noirHeaderBackground
import com.example.ui.theme.AcertoPalette
import com.example.ui.theme.LiquidDarkTokens
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.CardPurchaseProgress
import com.example.util.CurrencyFormatter
import com.example.util.Money

/** Compras de uma pessoa no cartão, juntas num cartão colorido. */
internal data class PersonCardGroup(
    val personId: Long,
    val name: String,
    val person: Person?,
    val purchases: List<CardPurchaseProgress>,
    /** Quanto a pessoa deve no mês aberto (parcelas do cartão ainda não pagas). */
    val monthDue: Double = 0.0
) {
    val remaining: Double get() = purchases.sumOf { it.remainingAmount }
    val monthly: Double get() = purchases.filter { it.remainingInstallmentsCount > 0 }.sumOf { it.installmentAmount }
    val open: Int get() = purchases.count { it.remainingInstallmentsCount > 0 }
}

/**
 * Agrupa as compras por pessoa, na MESMA ordem usada em toda a transição
 * Início ↔ Cartão (quem deve mais no mês primeiro) — chamada tanto pela tela
 * Cartão quanto pela pilha/overlay da Início, para os dois lados nunca
 * discordarem de quem é "o amarelo", "o turquesa" etc. `internal`: usada de
 * HomeScreen.kt/HomeOverview.kt para montar a pilha e o overlay da Início.
 */
internal fun buildCardGroups(
    purchases: List<CardPurchaseProgress>,
    people: List<Person>,
    monthDue: Map<Long, Double>
): List<PersonCardGroup> = purchases
    .groupBy { it.expense.personId }
    .map { (id, list) ->
        PersonCardGroup(
            personId = id,
            name = list.first().personName,
            person = people.firstOrNull { it.id == id },
            purchases = list.sortedWith(compareBy<CardPurchaseProgress> { it.remainingInstallmentsCount == 0 }.thenByDescending { it.remainingAmount }),
            monthDue = monthDue[id] ?: 0.0
        )
    }
    .sortedWith(compareByDescending<PersonCardGroup> { it.monthDue }.thenByDescending { it.remaining })

private val CardHeight = 196.dp
private val CardInk = Color(0xFF16181A)

/**
 * Aba Cartão no estilo da tela "Cards" da referência: um cartão colorido por
 * pessoa, separados, com brilho da cor embaixo e botões redondos na borda
 * (Nova compra, Receber, Cobrar). Tocar no cartão mostra as compras dela.
 */
@Composable
internal fun CardStackScreen(
    purchases: List<CardPurchaseProgress>,
    totalCount: Int,
    isPrivacyMode: Boolean,
    searchBar: @Composable () -> Unit,
    onAddPurchase: (personId: Long?) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
    people: List<Person> = emptyList(),
    onReceive: (personId: Long) -> Unit = {},
    onCharge: (personId: Long) -> Unit = {},
    /** Parcelas do cartão em aberto no mês aberto, por pessoa. */
    monthDue: Map<Long, Double> = emptyMap(),
    monthName: String = "este mês",
    /** Travessia Início↔Cartão: aqui só se medem os 3 primeiros cartões (ponto de chegada do
     *  overlay) e eles somem enquanto o overlay desenha o grupo por cima do pager. */
    cardTransition: CardTransitionState? = null
) {
    StatusBarIcons(lightIcons = true, lightNavIcons = true)
    val reduced = rememberReducedMotion()
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var openPersonId by rememberSaveable { mutableStateOf<Long?>(null) }
    // Lido só dentro dos graphicsLayer abaixo (fase de desenho): nunca como valor
    // lido na composição, senão esta tela inteira recomporia a cada frame do arrasto.
    val tabPosition = com.example.ui.components.LocalTabPosition.current

    val groups = buildCardGroups(purchases, people, monthDue)

    CompositionLocalProvider(LocalLiquidGlassColors provides LiquidDarkTokens) {
        Column(
            modifier
                .fillMaxSize()
                .noirHeaderBackground()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 140.dp + navBarsBottom())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 12.dp)
            ) {
                Text("Cartão", style = MaterialTheme.typography.headlineMedium, color = AcertoPalette.TextOnHeader, modifier = Modifier.weight(1f))
                NoirRoundButton(
                    icon = if (searchOpen) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (searchOpen) "Fechar pesquisa" else "Pesquisar",
                    onClick = { searchOpen = !searchOpen }
                )
                Spacer(Modifier.width(8.dp))
                NoirPill(text = "Nova compra", onClick = { onAddPurchase(null) }, light = true, icon = Icons.Default.Add, modifier = Modifier.testTag("card_new_purchase"))
            }
            Text(
                buildString {
                    append(if (totalCount == 1) "1 compra parcelada" else "$totalCount compras parceladas")
                    if (groups.size > 1) append(" · ${groups.size} pessoas")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AcertoPalette.TextOnHeaderMuted,
                modifier = Modifier.padding(start = 20.dp, top = 2.dp)
            )
            AnimatedVisibility(visible = searchOpen) { Box(Modifier.padding(top = 12.dp)) { searchBar() } }

            if (groups.isEmpty()) {
                Text(
                    if (totalCount == 0) "Nenhuma compra parcelada ainda. Toque em Nova compra." else "Nada encontrado para essa pesquisa.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AcertoPalette.TextOnHeaderMuted,
                    modifier = Modifier.padding(24.dp)
                )
                return@Column
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp).testTag("card_stack")
            ) {
                groups.forEachIndexed { i, g ->
                    val color = AcertoPalette.Cards[i % AcertoPalette.Cards.size]
                    Column(
                        Modifier
                            .zIndex((groups.size - i).toFloat())
                            .then(
                                if (i < 3 && cardTransition != null) {
                                    Modifier.onGloballyPositioned { c ->
                                        cardTransition.cardAnchors[i].value = settledRect(c, 2, tabPosition())
                                    }
                                } else Modifier
                            )
                            .graphicsLayer {
                                if (i < 3) {
                                    // Os 3 primeiros vêm da Início: durante a travessia quem os
                                    // desenha é o CardTransitionOverlay, por cima do pager; aqui
                                    // eles reaparecem no lugar exato em que o overlay os deixa.
                                    alpha = if (cardTransition?.overlayShown == true) 0f else 1f
                                    return@graphicsLayer
                                }
                                val progress = (tabPosition() - 1f).coerceIn(0f, 1f)
                                // Os demais (a Início nunca mostra mais que 3) entram deslizando
                                // da direita, um depois do outro, sem vir de lugar nenhum real.
                                if (reduced) return@graphicsLayer
                                val closed = 1f - progress
                                translationX = size.width * (0.22f + 0.18f * (i - 3).coerceAtMost(4)) * closed
                                alpha = (progress / 0.35f).coerceIn(0f, 1f)
                            }
                    ) {
                        PersonCard(
                            group = g,
                            color = color,
                            open = openPersonId == g.personId,
                            isPrivacyMode = isPrivacyMode,
                            monthName = monthName,
                            onClick = { openPersonId = if (openPersonId == g.personId) null else g.personId },
                            onAdd = { onAddPurchase(g.personId) },
                            onReceive = { onReceive(g.personId) },
                            onCharge = { onCharge(g.personId) }
                        )
                        AnimatedVisibility(
                            visible = openPersonId == g.personId,
                            enter = expandVertically(spring(dampingRatio = 0.9f, stiffness = 380f)) + fadeIn(tween(AcertoMotion.FAST)),
                            exit = shrinkVertically(tween(AcertoMotion.FAST, easing = AcertoMotion.SmoothOut)) + fadeOut(tween(AcertoMotion.QUICK))
                        ) {
                            PurchaseList(purchases = g.purchases, color = color, isPrivacyMode = isPrivacyMode, onDelete = onDelete)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Estado compartilhado da travessia Início↔Cartão. Os campos só são lidos pelo
 * overlay e por fases de desenho, nunca no corpo da HomeScreen: assim o arrasto
 * não recompõe as telas grandes. As âncoras ficam em coordenadas "assentadas"
 * (sem o deslocamento horizontal do pager), então não tremem nem atrasam um frame.
 */
@Stable
internal class CardTransitionState {
    var homeAnchor by mutableStateOf<Rect?>(null)
    val cardAnchors = List(3) { mutableStateOf<Rect?>(null) }
    /** true enquanto o overlay desenha o grupo: a pilha da Início e os 3 primeiros do Cartão somem. */
    var overlayShown by mutableStateOf(false)
}

/** Faixa de cada cartão que aparece no canto direito da Início (o resto fica fora da tela). */
private val StackPeek = 18.dp

/** Retângulo de [c] na raiz, descontado o deslocamento que o pager aplica agora à página [page]. */
private fun settledRect(c: LayoutCoordinates, page: Int, tabPosition: Float): Rect {
    val pageWidth = c.findRootCoordinates().size.width.toFloat()
    val p = c.positionInRoot()
    val left = (p.x + (tabPosition - page) * pageWidth).roundToInt().toFloat()
    val top = p.y.roundToInt().toFloat()
    return Rect(left, top, left + c.size.width, top + c.size.height)
}

/**
 * Pilha da Início como no vídeo: os cartões REAIS, no tamanho do Cartão, ficam quase
 * todos fora da tela e só uma faixa lisa de cada um aparece no canto direito (amarelo,
 * turquesa, azul — cada um por cima do anterior). Sem texto nem botões: o conteúdo
 * só aparece durante a travessia, quando eles entram deslizando.
 */
@Composable
internal fun BoxScope.HomeCardGroupStack(
    groups: List<PersonCardGroup>,
    isPrivacyMode: Boolean,
    monthName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    transition: CardTransitionState? = null
) {
    if (groups.isEmpty()) return
    val shown = groups.take(3)
    val tabPosition = com.example.ui.components.LocalTabPosition.current
    val density = LocalDensity.current
    val fullWidthPx = with(density) { (LocalConfiguration.current.screenWidthDp.dp - 40.dp).roundToPx() }
    CompositionLocalProvider(LocalLiquidGlassColors provides LiquidDarkTokens) {
        Box(
            modifier
                .align(Alignment.TopEnd)
                .width(StackPeek * shown.size)
                .graphicsLayer { alpha = if (transition?.overlayShown == true) 0f else 1f }
                .clickable(role = Role.Button, onClickLabel = "Ver compras no cartão", onClick = onClick)
                .testTag("home_card_stack")
        ) {
            shown.forEachIndexed { i, g ->
                Box(
                    Modifier
                        .zIndex(i.toFloat())
                        .offset(x = StackPeek * i)
                        .then(
                            if (i == 0 && transition != null) {
                                Modifier.onGloballyPositioned { c -> transition.homeAnchor = settledRect(c, 1, tabPosition()) }
                            } else Modifier
                        )
                        // mede no tamanho real e deixa sobrar para a direita, fora da tela
                        .layout { measurable, constraints ->
                            val p = measurable.measure(Constraints.fixedWidth(fullWidthPx))
                            layout(constraints.maxWidth, p.height) { p.place(0, 0) }
                        }
                ) {
                    PersonCard(
                        group = g,
                        color = AcertoPalette.Cards[i % AcertoPalette.Cards.size],
                        open = false,
                        isPrivacyMode = isPrivacyMode,
                        monthName = monthName,
                        onClick = {},
                        onAdd = {},
                        onReceive = {},
                        onCharge = {},
                        interactive = false,
                        contentAlpha = 0f
                    )
                }
            }
        }
    }
}

/**
 * Camada acima do HorizontalPager. Durante a travessia Início↔Cartão os até 3
 * cartões em destaque saem dos dois layouts e viajam aqui, juntos e no tamanho
 * real, presos ao progresso do próprio pager (o dedo ou a mola de
 * animateScrollToPage): entram deslizando do canto direito, sobrepostos, e o
 * conteúdo vai aparecendo. Só se separam na vertical depois de ~75%: amarelo primeiro, turquesa e azul
 * 40 ms depois cada, com mola curta. A camada só some quando o grupo chegou em
 * repouso numa das pontas — aí a pilha da Início ou os PersonCard do Cartão
 * reaparecem exatamente onde o overlay os deixou. Arrasto cancelado: o grupo
 * recolhe e volta sozinho, sem ficar parado no meio.
 */
@Composable
internal fun CardTransitionOverlay(
    groups: List<PersonCardGroup>,
    isPrivacyMode: Boolean,
    monthName: String,
    state: CardTransitionState,
    modifier: Modifier = Modifier,
    /** false com pesquisa/filtro no Cartão: a lista de lá não começa pelos mesmos 3 cartões. */
    enabled: Boolean = true
) {
    // Camada pequena: recompor a cada frame do arrasto aqui é barato (as telas grandes
    // só leem o progresso na fase de desenho).
    val tabPosition = com.example.ui.components.LocalTabPosition.current
    val progress = (tabPosition() - 1f).coerceIn(0f, 1f)
    val reduced = rememberReducedMotion()
    val shown = groups.take(3)
    val openNow = progress > 0.75f
    val opens = remember { List(3) { Animatable(if (openNow) 1f else 0f) } }
    LaunchedEffect(openNow, reduced) {
        val target = if (openNow) 1f else 0f
        opens.forEachIndexed { i, a ->
            launch {
                if (reduced) { a.snapTo(target); return@launch }
                // abrindo: amarelo primeiro; recolhendo: o de trás primeiro
                kotlinx.coroutines.delay((if (openNow) i else 2 - i) * 40L)
                a.animateTo(
                    target,
                    if (openNow) spring(dampingRatio = 0.8f, stiffness = 300f) else spring(dampingRatio = 0.9f, stiffness = 420f)
                )
            }
        }
    }
    val home = state.homeAnchor
    val card0 = state.cardAnchors[0].value
    val atRest = when {
        progress >= 1f -> opens.all { it.value >= 1f && !it.isRunning }
        progress <= 0f -> opens.all { it.value <= 0f && !it.isRunning }
        else -> false
    }
    val visible = enabled && shown.isNotEmpty() && home != null && card0 != null && !atRest
    // Antes do layout/desenho deste mesmo frame: a troca overlay ↔ cartões reais não pisca.
    SideEffect { state.overlayShown = visible }
    DisposableEffect(Unit) { onDispose { state.overlayShown = false } }
    if (!visible) return

    val density = LocalDensity.current
    val peek = with(density) { StackPeek.toPx() }
    val fallbackStep = card0.height + with(density) { 14.dp.toPx() }
    val cardWidthPx = card0.width.roundToInt()
    val origin = remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier
            .fillMaxSize()
            .onGloballyPositioned { origin.value = it.positionInRoot() }
            .zIndex(50f)
            .testTag("card_transition_overlay")
    ) {
        CompositionLocalProvider(LocalLiquidGlassColors provides LiquidDarkTokens) {
            shown.forEachIndexed { i, g ->
                val o = opens[i].value
                // os de trás seguem o amarelo com um leve atraso: abrem um vão no meio do
                // caminho e alcançam ele na chegada (como no vídeo)
                val lag = i * 0.1f
                val e = ((progress - lag) / (1f - lag)).coerceIn(0f, 1f)
                val listOffset = state.cardAnchors[i].value?.let { it.top - card0.top } ?: (i * fallbackStep)
                val startX = home.left + i * peek
                val x = startX + (card0.left - startX) * e
                val y = home.top + (card0.top - home.top) * e + listOffset * o
                // lisos na Início; o conteúdo surge enquanto eles atravessam
                val contentAlpha = ((e - 0.35f) / 0.6f).coerceIn(0f, 1f)
                Box(
                    Modifier
                        .zIndex(i.toFloat())
                        .offset { IntOffset((x - origin.value.x).roundToInt(), (y - origin.value.y).roundToInt()) }
                        .layout { measurable, _ ->
                            val p = measurable.measure(Constraints.fixedWidth(cardWidthPx))
                            layout(p.width, p.height) { p.place(0, 0) }
                        }
                ) {
                    PersonCard(
                        group = g,
                        color = AcertoPalette.Cards[i % AcertoPalette.Cards.size],
                        open = false,
                        isPrivacyMode = isPrivacyMode,
                        monthName = monthName,
                        onClick = {},
                        onAdd = {},
                        onReceive = {},
                        onCharge = {},
                        interactive = false,
                        contentAlpha = contentAlpha
                    )
                }
            }
        }
    }
}

@Composable
internal fun PersonCard(
    group: PersonCardGroup,
    color: Color,
    open: Boolean,
    isPrivacyMode: Boolean,
    monthName: String,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onReceive: () -> Unit,
    onCharge: () -> Unit,
    /** false nas cópias decorativas (pilha da Início e overlay): mesma aparência, sem toque. */
    interactive: Boolean = true,
    /** Opacidade do conteúdo (textos, avatar, botões e brilho): 0 = cartão liso, como na Início. */
    contentAlpha: Float = 1f
) {
    // texto escuro nos cartões claros, branco nos mais escuros
    val ink = if (color.luminance() > 0.4f) CardInk else Color.White
    val inkMuted = ink.copy(alpha = 0.72f)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, spring(dampingRatio = 0.6f, stiffness = 500f), label = "card_press")
    val arrow by animateFloatAsState(if (open) 180f else 0f, tween(AcertoMotion.FAST), label = "card_arrow")
    val shape = RoundedCornerShape(26.dp)

    // Cartão + botões que passam da borda de baixo (como na referência)
    Box(
        Modifier
            .fillMaxWidth()
            .then(if (interactive) Modifier else Modifier.clearAndSetSemantics {})
            .graphicsLayer { scaleX = scale; scaleY = scale }
            // brilho colorido embaixo do cartão: elipse que some nas bordas
            .drawBehind {
                val w = size.width - 24.dp.toPx()
                val h = 72.dp.toPx()
                val c = Offset(size.width / 2, CardHeight.toPx() + 4.dp.toPx())
                scale(scaleX = 1f, scaleY = h / w, pivot = c) {
                    drawCircle(
                        Brush.radialGradient(listOf(color.copy(alpha = 0.42f * contentAlpha), color.copy(alpha = 0.12f * contentAlpha), Color.Transparent), center = c, radius = w / 2),
                        radius = w / 2,
                        center = c
                    )
                }
            }
            .padding(bottom = 26.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(CardHeight)
                .shadow(18.dp, shape, ambientColor = color, spotColor = color)
                .clip(shape)
                .background(color)
                .drawBehind {
                    // círculos suaves de fundo, como no cartão da referência
                    drawCircle(Color.White.copy(alpha = 0.16f), radius = size.width * 0.42f, center = Offset(size.width * 0.8f, size.height * 0.98f))
                    drawCircle(Color.White.copy(alpha = 0.09f), radius = size.width * 0.3f, center = Offset(size.width * 0.15f, 0f))
                }
                .then(
                    if (interactive) {
                        Modifier.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClickLabel = if (open) "Fechar compras de ${group.name}" else "Ver compras de ${group.name}", onClick = onClick)
                    } else Modifier
                )
                .padding(start = 20.dp, end = 18.dp, top = 18.dp, bottom = 16.dp)
                .then(if (interactive) Modifier.testTag("person_card_${group.personId}") else Modifier)
        ) {
          Column(
              Modifier.fillMaxSize().graphicsLayer {
                  alpha = contentAlpha
                  compositingStrategy = CompositingStrategy.ModulateAlpha
              }
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(group.name, style = MaterialTheme.typography.titleMedium, color = inkMuted, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "•••• " + if (group.purchases.size == 1) "1 compra" else "${group.purchases.size} compras",
                    style = MaterialTheme.typography.titleSmall,
                    color = inkMuted
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                // valor principal: o que a pessoa deve AGORA (as parcelas do mês), como na Início
                when {
                    group.monthDue > 0.001 -> CurrencyFormatter.format(group.monthDue, isPrivacyMode)
                    group.remaining > 0.001 -> "Em dia"
                    else -> "Tudo quitado"
                },
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ink,
                maxLines = 1
            )
            Text(
                when {
                    group.monthDue > 0.001 -> "deve em $monthName · ${CurrencyFormatter.format(group.remaining, isPrivacyMode)} no total"
                    group.remaining > 0.001 -> "nada em $monthName · ${CurrencyFormatter.format(group.remaining, isPrivacyMode)} nos próximos meses"
                    else -> "nenhuma parcela em aberto"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = inkMuted
            )
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = inkMuted,
                    modifier = Modifier.size(22.dp).graphicsLayer { rotationZ = arrow }
                )
                Spacer(Modifier.width(6.dp))
                if (group.person != null) {
                    CompositionLocalProvider(LocalLiquidGlassColors provides com.example.ui.theme.LiquidLightTokens) {
                        PersonAvatarBadge(name = group.person.name, colorHex = group.person.colorHex, avatar = group.person.avatar, size = 40.dp)
                    }
                }
            }
          }
        }
        // botões redondos atravessando a borda de baixo (também nas cópias decorativas,
        // para o cartão ser o mesmo do começo ao fim da travessia)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(start = 16.dp, top = CardHeight - 25.dp)
                .graphicsLayer {
                    alpha = contentAlpha
                    // sem camada fora da tela: a sombra dos botões não vira um quadrado ao esmaecer
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                }
        ) {
            EdgeButton(Icons.Default.Add, "Nova compra para ${group.name}", color, onAdd, "card_add_${group.personId}", interactive)
            if (group.remaining > 0.001) EdgeButton(Icons.Default.Payments, "Receber de ${group.name}", color, onReceive, "card_receive_${group.personId}", interactive)
            EdgeButton(Icons.Default.Share, "Cobrar ${group.name}", color, onCharge, "card_charge_${group.personId}", interactive)
        }
    }
}

@Composable
private fun EdgeButton(icon: ImageVector, label: String, cardColor: Color, onClick: () -> Unit, tag: String, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.86f else 1f, spring(dampingRatio = 0.5f, stiffness = 600f), label = "edge_press")
    // vidro na cor do cartão, como no vídeo: um pouco mais escuro embaixo, brilho suave
    // no canto de cima e borda clara fina, com sombra macia
    val top = Color.Black.copy(alpha = 0.16f).compositeOver(cardColor)
    val bottom = Color.Black.copy(alpha = 0.34f).compositeOver(cardColor)
    Box(
        Modifier
            .size(50.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(6.dp, CircleShape, ambientColor = Color.Black, spotColor = Color.Black.copy(alpha = 0.55f))
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.32f, size.height * 0.26f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.32f, size.height * 0.26f)
                )
            }
            .border(
                1.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.06f))),
                CircleShape
            )
            .then(
                if (enabled) {
                    Modifier
                        .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
                        .semantics { contentDescription = label }
                        .testTag(tag)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) }
}

/** Compras da pessoa: uma linha por compra, com progresso na cor do cartão. */
@Composable
private fun PurchaseList(
    purchases: List<CardPurchaseProgress>,
    color: Color,
    isPrivacyMode: Boolean,
    onDelete: (Long) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(AcertoPalette.HeaderTile)
            .animateContentSize()
            .padding(vertical = 6.dp)
    ) {
        purchases.forEach { p ->
            val done = p.remainingInstallmentsCount == 0
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 6.dp, top = 10.dp, bottom = 10.dp).testTag("purchase_card_${p.expense.id}")
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            p.expense.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = AcertoPalette.TextOnHeader,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (done) "Quitada" else CurrencyFormatter.format(p.remainingAmount, isPrivacyMode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (done) AcertoPalette.Mint else AcertoPalette.TextOnHeader
                        )
                    }
                    Text(
                        "${p.paidInstallmentsCount} de ${p.totalInstallmentsCount} pagas · ${p.totalInstallmentsCount}x de ${CurrencyFormatter.format(p.installmentAmount, isPrivacyMode)} · total ${CurrencyFormatter.format(Money.toReal(p.expense.totalAmount), isPrivacyMode)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AcertoPalette.TextOnHeaderMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Box(Modifier.padding(top = 8.dp).fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.10f))) {
                        Box(Modifier.fillMaxWidth(p.progress.coerceIn(0f, 1f)).height(5.dp).clip(RoundedCornerShape(3.dp)).background(color))
                    }
                }
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .clickable(role = Role.Button, onClickLabel = "Excluir ${p.expense.title}") { onDelete(p.expense.id) }
                        .testTag("purchase_delete_${p.expense.id}"),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Delete, contentDescription = "Excluir ${p.expense.title}", tint = AcertoPalette.TextOnHeaderMuted, modifier = Modifier.size(20.dp)) }
            }
        }
    }
}
