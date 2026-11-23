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
private data class PersonCardGroup(
    val personId: Long,
    val name: String,
    val person: Person?,
    val purchases: List<CardPurchaseProgress>
) {
    val remaining: Double get() = purchases.sumOf { it.remainingAmount }
    val monthly: Double get() = purchases.filter { it.remainingInstallmentsCount > 0 }.sumOf { it.installmentAmount }
    val open: Int get() = purchases.count { it.remainingInstallmentsCount > 0 }
}

private val CardHeight = 196.dp
private val CardInk = Color(0xFF16181A)

/**
 * Aba Cartão no estilo da tela "Cards" da referência: um cartão colorido por
 * pessoa, separados, com brilho da cor embaixo e botões redondos na borda
 * (Nova compra, Receber, Cobrar). Tocar no cartão mostra as compras dela.
 */
@Composable
fun CardStackScreen(
    purchases: List<CardPurchaseProgress>,
    totalCount: Int,
    isPrivacyMode: Boolean,
    searchBar: @Composable () -> Unit,
    onAddPurchase: (personId: Long?) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
    people: List<Person> = emptyList(),
    onReceive: (personId: Long) -> Unit = {},
    onCharge: (personId: Long) -> Unit = {}
) {
    StatusBarIcons(lightIcons = true, lightNavIcons = true)
    val reduced = rememberReducedMotion()
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var openPersonId by rememberSaveable { mutableStateOf<Long?>(null) }

    val groups = purchases
        .groupBy { it.expense.personId }
        .map { (id, list) ->
            PersonCardGroup(
                personId = id,
                name = list.first().personName,
                person = people.firstOrNull { it.id == id },
                purchases = list.sortedWith(compareBy<CardPurchaseProgress> { it.remainingInstallmentsCount == 0 }.thenByDescending { it.remainingAmount })
            )
        }
        .sortedByDescending { it.remaining }

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
                    // entrada: sobem um depois do outro
                    val intro = remember { Animatable(if (reduced) 1f else 0f) }
                    LaunchedEffect(Unit) {
                        if (!reduced) {
                            kotlinx.coroutines.delay(70L * i.coerceAtMost(8))
                            intro.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 300f))
                        }
                    }
                    Column(
                        Modifier.graphicsLayer {
                            alpha = intro.value
                            translationY = (1f - intro.value) * 60f * density
                        }
                    ) {
                        PersonCard(
                            group = g,
                            color = color,
                            open = openPersonId == g.personId,
                            isPrivacyMode = isPrivacyMode,
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

@Composable
private fun PersonCard(
    group: PersonCardGroup,
    color: Color,
    open: Boolean,
    isPrivacyMode: Boolean,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onReceive: () -> Unit,
    onCharge: () -> Unit
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
            .graphicsLayer { scaleX = scale; scaleY = scale }
            // brilho colorido embaixo do cartão: elipse que some nas bordas
            .drawBehind {
                val w = size.width - 24.dp.toPx()
                val h = 72.dp.toPx()
                val c = Offset(size.width / 2, CardHeight.toPx() + 4.dp.toPx())
                scale(scaleX = 1f, scaleY = h / w, pivot = c) {
                    drawCircle(
                        Brush.radialGradient(listOf(color.copy(alpha = 0.42f), color.copy(alpha = 0.12f), Color.Transparent), center = c, radius = w / 2),
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
                .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClickLabel = if (open) "Fechar compras de ${group.name}" else "Ver compras de ${group.name}", onClick = onClick)
                .padding(start = 20.dp, end = 18.dp, top = 18.dp, bottom = 16.dp)
                .testTag("person_card_${group.personId}")
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
                if (group.remaining > 0.001) CurrencyFormatter.format(group.remaining, isPrivacyMode) else "Tudo quitado",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ink,
                maxLines = 1
            )
            Text(
                if (group.open > 0) "faltam · ${CurrencyFormatter.format(group.monthly, isPrivacyMode)} por mês" else "nenhuma parcela em aberto",
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
        // botões redondos atravessando a borda de baixo
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 16.dp, top = CardHeight - 25.dp)
        ) {
            EdgeButton(Icons.Default.Add, "Nova compra para ${group.name}", color, onAdd, "card_add_${group.personId}")
            if (group.remaining > 0.001) EdgeButton(Icons.Default.Payments, "Receber de ${group.name}", color, onReceive, "card_receive_${group.personId}")
            EdgeButton(Icons.Default.Share, "Cobrar ${group.name}", color, onCharge, "card_charge_${group.personId}")
        }
    }
}

@Composable
private fun EdgeButton(icon: ImageVector, label: String, cardColor: Color, onClick: () -> Unit, tag: String) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.86f else 1f, spring(dampingRatio = 0.5f, stiffness = 600f), label = "edge_press")
    // vidro escuro com um toque da cor do cartão
    val bg = Color.Black.copy(alpha = 0.34f).compositeOver(cardColor)
    Box(
        Modifier
            .size(50.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(10.dp, CircleShape, ambientColor = cardColor, spotColor = cardColor)
            .clip(CircleShape)
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label }
            .testTag(tag),
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
