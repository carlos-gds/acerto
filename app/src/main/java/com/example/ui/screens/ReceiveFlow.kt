package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NoirRoundButton
import com.example.ui.components.PersonAvatarBadge
import com.example.ui.components.StatusBarIcons
import com.example.ui.components.glass.AcertoMotion
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.components.glass.shakeOnChange
import com.example.ui.components.noirHeaderBackground
import com.example.ui.theme.AcertoPalette
import com.example.util.Calculator
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Pessoa que pode pagar: o que deve no mês aberto e no total. */
data class ReceiveTarget(
    val personId: Long,
    val name: String,
    val colorHex: String,
    val avatar: String,
    val monthPending: Double,
    val totalPending: Double
)

private enum class ReceiveStep { PICK, AMOUNT, DONE }

/**
 * Fluxo "Receber", no estilo da referência:
 * grade escura de pessoas → folha branca com teclado-calculadora → deslizar
 * para confirmar → tela verde de sucesso.
 */
@Composable
fun ReceiveFlow(
    targets: List<ReceiveTarget>,
    initialPersonId: Long?,
    monthName: String,
    isPrivacyMode: Boolean,
    onConfirm: (personId: Long, amount: Double) -> Unit,
    onCharge: (personId: Long) -> Unit,
    onClose: () -> Unit
) {
    var step by remember { mutableStateOf(if (initialPersonId != null) ReceiveStep.AMOUNT else ReceiveStep.PICK) }
    var personId by remember { mutableStateOf(initialPersonId) }
    var received by remember { mutableFloatStateOf(0f) }
    val target = targets.firstOrNull { it.personId == personId }
    val reduced = rememberReducedMotion()

    BackHandler {
        when {
            step == ReceiveStep.AMOUNT && initialPersonId == null -> step = ReceiveStep.PICK
            else -> onClose()
        }
    }

    // Fundo: grade escura recua e escurece quando a folha sobe (como na referência)
    val back by animateFloatAsState(
        targetValue = if (step == ReceiveStep.PICK) 0f else 1f,
        animationSpec = tween(if (reduced) 0 else AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut),
        label = "receive_back"
    )

    Box(Modifier.fillMaxSize().background(Color.Black).testTag("receive_flow")) {
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
            androidx.compose.runtime.CompositionLocalProvider(
                com.example.ui.theme.LocalLiquidGlassColors provides com.example.ui.theme.LiquidDarkTokens
            ) {
                PickPerson(
                    targets = targets,
                    isPrivacyMode = isPrivacyMode,
                    monthName = monthName,
                    onPick = { personId = it; step = ReceiveStep.AMOUNT },
                    onClose = onClose
                )
            }
            if (back > 0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f * back)))
        }

        AnimatedVisibility(
            visible = step != ReceiveStep.PICK && target != null,
            enter = slideInVertically(spring(dampingRatio = 0.86f, stiffness = 380f)) { it } + fadeIn(tween(120)),
            exit = slideOutVertically(tween(AcertoMotion.FAST)) { it } + fadeOut(tween(AcertoMotion.QUICK))
        ) {
            if (target != null) {
                AmountSheet(
                    target = target,
                    monthName = monthName,
                    isPrivacyMode = isPrivacyMode,
                    onBackToPeople = { if (initialPersonId == null) step = ReceiveStep.PICK },
                    onCharge = { onCharge(target.personId) },
                    onConfirmed = { amount ->
                        received = amount.toFloat()
                        onConfirm(target.personId, amount)
                        step = ReceiveStep.DONE
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = step == ReceiveStep.DONE,
            enter = fadeIn(tween(AcertoMotion.FAST)) + scaleIn(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut), initialScale = 0.92f),
            exit = fadeOut(tween(AcertoMotion.QUICK))
        ) {
            SuccessScreen(
                amount = received.toDouble(),
                name = target?.name.orEmpty(),
                onClose = onClose
            )
        }
    }
}

/* ---------------- 1. Escolher a pessoa (grade escura) ---------------- */

@Composable
private fun PickPerson(
    targets: List<ReceiveTarget>,
    isPrivacyMode: Boolean,
    monthName: String,
    onPick: (Long) -> Unit,
    onClose: () -> Unit
) {
    StatusBarIcons(lightIcons = true)
    val owing = targets.filter { it.totalPending > 0.001 }.sortedByDescending { it.monthPending }
    Column(Modifier.fillMaxSize().noirHeaderBackground().statusBarsPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 12.dp)
        ) {
            Text(
                "Receber de",
                style = MaterialTheme.typography.headlineMedium,
                color = AcertoPalette.TextOnHeader,
                modifier = Modifier.weight(1f)
            )
            NoirRoundButton(icon = Icons.Default.Close, contentDescription = "Fechar", onClick = onClose, modifier = Modifier.testTag("receive_close"))
        }
        if (owing.isEmpty()) {
            Text(
                "Ninguém te deve nada agora. 🎉",
                style = MaterialTheme.typography.bodyLarge,
                color = AcertoPalette.TextOnHeaderMuted,
                modifier = Modifier.padding(24.dp)
            )
            return@Column
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).padding(horizontal = 12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(owing, key = { it.personId }) { t ->
                var pressed by remember { mutableStateOf(false) }
                val sc by animateFloatAsState(if (pressed) 0.9f else 1f, spring(dampingRatio = 0.5f, stiffness = 500f), label = "pick_press")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer { scaleX = sc; scaleY = sc }
                        .clip(RoundedCornerShape(16.dp))
                        .pointerInput(t.personId) {
                            detectTapGestures(
                                onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                                onTap = { onPick(t.personId) }
                            )
                        }
                        .padding(vertical = 6.dp)
                        .semantics(mergeDescendants = true) { contentDescription = "Receber de ${t.name}" }
                        .testTag("receive_person_${t.personId}")
                ) {
                    PersonAvatarBadge(name = t.name, colorHex = t.colorHex, avatar = t.avatar, size = 72.dp)
                    Spacer(Modifier.height(8.dp))
                    Text(t.name, style = MaterialTheme.typography.bodyLarge, color = AcertoPalette.TextOnHeader, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        CurrencyFormatter.format(if (t.monthPending > 0.001) t.monthPending else t.totalPending, isPrivacyMode),
                        style = MaterialTheme.typography.bodySmall,
                        color = AcertoPalette.TextOnHeaderMuted,
                        maxLines = 1
                    )
                }
            }
        }
        // Folha branca: total em aberto de cada um (como a lista da referência)
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color.White)
                .padding(top = 18.dp)
        ) {
            Text(
                "Em aberto no total",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF16181A),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
            owing.sortedByDescending { it.totalPending }.forEach { t ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClickLabel = "Receber de ${t.name}") { onPick(t.personId) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        com.example.ui.theme.LocalLiquidGlassColors provides com.example.ui.theme.LiquidLightTokens
                    ) { PersonAvatarBadge(name = t.name, colorHex = t.colorHex, avatar = t.avatar, size = 40.dp) }
                    Spacer(Modifier.width(12.dp))
                    Text(t.name, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF16181A), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(CurrencyFormatter.format(t.totalPending, isPrivacyMode), style = MaterialTheme.typography.bodyLarge, color = Color(0xFF63666B))
                }
            }
        }
    }
}

/* ---------------- 2. Valor: folha branca com teclado-calculadora ---------------- */

@Composable
private fun AmountSheet(
    target: ReceiveTarget,
    monthName: String,
    isPrivacyMode: Boolean,
    onBackToPeople: () -> Unit,
    onCharge: () -> Unit,
    onConfirmed: (Double) -> Unit
) {
    StatusBarIcons(lightIcons = true)
    val haptics = LocalHapticFeedback.current
    var expr by remember { mutableStateOf("") }
    var errorTick by remember { mutableIntStateOf(0) }
    var confirmed by remember { mutableStateOf(false) }
    val value = Calculator.evaluate(expr)
    val tooMuch = value != null && value > target.totalPending + 0.004
    val valid = value != null && value > 0.0 && !tooMuch
    val amountColor by animateColorAsState(if (confirmed) Color(0xFF16A34A) else Color(0xFF16181A), tween(AcertoMotion.FAST), label = "amount_color")

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(Color.White)
            .navigationBarsPadding()
            .testTag("receive_amount_sheet")
    ) {
        Box(
            Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp).width(36.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(Color(0x22000000))
        )
        // Receber | Cobrar
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 14.dp)
        ) {
            ModeChip("Receber", selected = true, onClick = {})
            ModeChip("Cobrar", selected = false, onClick = onCharge, modifier = Modifier.testTag("receive_charge_chip"))
        }
        Text(
            "${target.name} · deve ${CurrencyFormatter.format(target.monthPending.takeIf { it > 0.001 } ?: target.totalPending, isPrivacyMode)}" +
                if (target.monthPending > 0.001) " em $monthName" else " no total",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF63666B),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp, start = 24.dp, end = 24.dp)
                .clickable(role = Role.Button, onClick = onBackToPeople)
        )

        // Valor
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.shakeOnChange(errorTick)) {
                ExpressionText(expr = expr, color = amountColor)
                AnimatedVisibility(visible = valid, enter = fadeIn() + scaleIn(initialScale = 0.6f), exit = fadeOut()) {
                    Box(Modifier.padding(start = 10.dp)) {
                        PersonAvatarBadge(name = target.name, colorHex = target.colorHex, avatar = target.avatar, size = 34.dp)
                    }
                }
            }
            if (Calculator.hasOperator(expr) && value != null) {
                Text(
                    "= ${CurrencyFormatter.format(value)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF63666B),
                    modifier = Modifier.padding(top = 4.dp).testTag("receive_result")
                )
            }
            Spacer(Modifier.height(10.dp))
            if (tooMuch) {
                Text(
                    "Mais que o total em aberto (${CurrencyFormatter.format(target.totalPending, isPrivacyMode)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDC2626)
                )
            } else if (!valid) {
                val fill = target.monthPending.takeIf { it > 0.001 } ?: target.totalPending
                Text(
                    "Em aberto  ${CurrencyFormatter.format(fill, isPrivacyMode)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF63666B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClickLabel = "Usar o valor em aberto") {
                            expr = Calculator.fromAmount(fill)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("receive_fill_pending")
                )
            }
            AnimatedVisibility(visible = valid, enter = fadeIn(tween(AcertoMotion.FAST)), exit = fadeOut(tween(AcertoMotion.QUICK))) {
                SwipeToConfirm(
                    label = "Deslize para a direita para confirmar",
                    onConfirmed = {
                        confirmed = true
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfirmed(value ?: 0.0)
                    },
                    modifier = Modifier.padding(top = 18.dp)
                )
            }
        }

        // Operadores
        OperatorRow(onOp = { op -> expr = Calculator.press(expr, op.toString()) })

        Keypad(
            onKey = { key ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                val next = Calculator.press(expr, key)
                if (next == expr && key != "⌫") errorTick++ else expr = next
            },
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )
    }
}

/** + − × ÷ acima do teclado. */
@Composable
internal fun OperatorRow(onOp: (Char) -> Unit, tagPrefix: String = "receive_op_") {
    val haptics = LocalHapticFeedback.current
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp, vertical = 6.dp)
    ) {
        listOf(Calculator.PLUS, Calculator.MINUS, Calculator.TIMES, Calculator.DIVIDE).forEach { op ->
            Box(
                Modifier.size(50.dp).clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Operador $op") {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOp(op)
                    }
                    .testTag("$tagPrefix$op"),
                contentAlignment = Alignment.Center
            ) { Text(op.toString(), fontSize = 28.sp, color = Color(0xFF6E7175)) }
        }
    }
}

@Composable
internal fun ModeChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFFF2F2F3) else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium),
            color = if (selected) Color(0xFF16181A) else Color(0xFF9A9DA1)
        )
    }
}

/** "R$ 143 × 2": o operador e o número depois dele ficam mais claros. */
@Composable
internal fun ExpressionText(expr: String, color: Color, tag: String = "receive_amount") {
    val (first, rest) = Calculator.displayParts(expr)
    // valores longos encolhem para caber numa linha
    val size = when {
        first.length + rest.length > 16 -> 36.sp
        first.length + rest.length > 11 -> 44.sp
        else -> 54.sp
    }
    val big = MaterialTheme.typography.displaySmall.copy(fontSize = size, lineHeight = size * 1.12f, letterSpacing = (-1.5).sp, fontWeight = FontWeight.Bold)
    val shown = Calculator.display(expr)
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.testTag(tag).semantics { contentDescription = "R$ $shown" }) {
        Text("R$ ", style = big.copy(fontSize = size * 0.6f), color = if (expr.isEmpty()) Color(0xFFD0D0D3) else color, modifier = Modifier.padding(bottom = 6.dp))
        com.example.ui.components.glass.NumberPopIn(text = first, style = big, color = if (expr.isEmpty()) Color(0xFFD0D0D3) else color)
        if (rest.isNotEmpty()) {
            Text(
                " $rest",
                style = big.copy(fontSize = size * 0.72f),
                color = Color(0xFFB8BABD),
                maxLines = 1,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

/**
 * Teclado numérico como na referência: ao encostar, uma pílula cinza-escura
 * aparece na tecla (o número fica branco) e desliza de tecla em tecla
 * conforme você digita; ao soltar, ela some devagar.
 */
@Composable
internal fun Keypad(onKey: (String) -> Unit, modifier: Modifier = Modifier, tagPrefix: String = "receive_key_") {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", Calculator.DOUBLE_ZERO, "0", "⌫")
    val reduced = rememberReducedMotion()
    val scope = rememberCoroutineScope()
    val positions = remember { mutableMapOf<String, Offset>() }
    var keySize by remember { mutableStateOf(IntSize.Zero) }
    val hx = remember { Animatable(0f) }
    val hy = remember { Animatable(0f) }
    val ha = remember { Animatable(0f) }
    var litKey by remember { mutableStateOf<String?>(null) }
    var hideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val density = LocalDensity.current

    fun light(key: String) {
        val target = positions[key] ?: return
        hideJob?.cancel()
        litKey = key
        scope.launch {
            if (reduced || ha.value < 0.05f) {
                hx.snapTo(target.x); hy.snapTo(target.y)
            } else {
                launch { hx.animateTo(target.x, spring(dampingRatio = 0.82f, stiffness = 1100f)) }
                launch { hy.animateTo(target.y, spring(dampingRatio = 0.82f, stiffness = 1100f)) }
            }
            ha.animateTo(1f, tween(if (reduced) 0 else 70))
        }
    }
    fun release() {
        hideJob?.cancel()
        hideJob = scope.launch {
            delay(140)
            litKey = null
            ha.animateTo(0f, tween(if (reduced) 0 else 260, easing = AcertoMotion.SmoothOut))
        }
    }

    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color(0xFFF7F7F8)).padding(8.dp)) {
        // pílula escura que desliza entre as teclas
        Box(
            Modifier
                .offset { IntOffset(hx.value.roundToInt(), hy.value.roundToInt()) }
                .size(with(density) { keySize.width.toDp() }, with(density) { keySize.height.toDp() })
                .graphicsLayer { alpha = ha.value }
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF6F7175))
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            keys.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        val interaction = remember { MutableInteractionSource() }
                        LaunchedEffect(interaction) {
                            interaction.interactions.collect { i ->
                                when (i) {
                                    is androidx.compose.foundation.interaction.PressInteraction.Press -> light(key)
                                    is androidx.compose.foundation.interaction.PressInteraction.Release,
                                    is androidx.compose.foundation.interaction.PressInteraction.Cancel -> release()
                                }
                            }
                        }
                        val lit = litKey == key
                        val fg by animateColorAsState(if (lit) Color.White else Color(0xFF3A3D40), tween(if (lit) 60 else 220), label = "key_fg")
                        val keyBg by animateColorAsState(if (lit) Color.Transparent else Color.White, tween(if (lit) 60 else 220), label = "key_bg")
                        Box(
                            Modifier
                                .weight(1f)
                                .height(60.dp)
                                .onGloballyPositioned { c ->
                                    val p = c.positionInParent()
                                    val parent = c.parentLayoutCoordinates?.positionInParent() ?: Offset.Zero
                                    positions[key] = Offset(p.x, p.y + parent.y)
                                    keySize = c.size
                                }
                                .clip(RoundedCornerShape(24.dp))
                                .background(keyBg)
                                .clickable(
                                    interactionSource = interaction,
                                    indication = null,
                                    role = Role.Button,
                                    onClickLabel = when (key) { "⌫" -> "Apagar"; Calculator.DOUBLE_ZERO -> "Dois zeros"; else -> key }
                                ) {
                                    // toque rápido (sem "press" visível): acende mesmo assim
                                    if (litKey != key) { light(key); release() }
                                    onKey(key)
                                }
                                .testTag("$tagPrefix$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "⌫") {
                                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, tint = if (lit) Color.White else Color(0xFF6E7175), modifier = Modifier.size(24.dp))
                            } else {
                                Text(key, fontSize = 26.sp, fontWeight = FontWeight.Medium, color = fg)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * "Deslize para a direita para confirmar", como na referência: só o texto e
 * uma mãozinha que balança. Arrastar para a direita sobre essa área (ou
 * tocar, para acessibilidade) confirma; o texto acompanha o dedo.
 */
@Composable
fun SwipeToConfirm(label: String, onConfirmed: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val reduced = rememberReducedMotion()
    val x = remember { Animatable(0f) }
    var done by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    // mãozinha balançando para a direita
    val nudge = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (!reduced) while (true) {
            nudge.animateTo(1f, tween(520, easing = AcertoMotion.SmoothOut))
            nudge.animateTo(0f, tween(420))
            delay(700)
        }
    }
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(96.dp)
            .testTag("swipe_to_confirm")
    ) {
        val limit = with(density) { maxWidth.toPx() } * 0.32f
        val progress = (x.value / limit).coerceIn(0f, 1f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    state = rememberDraggableState { delta -> if (!done) scope.launch { x.snapTo((x.value + delta).coerceAtLeast(0f)) } },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (done) return@draggable
                        if (x.value >= limit) {
                            done = true
                            onConfirmed()
                        } else {
                            x.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 400f))
                        }
                    }
                )
                .semantics { contentDescription = label }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClickLabel = label
                ) {
                    if (!done) scope.launch {
                        if (!reduced) x.animateTo(limit, tween(AcertoMotion.FAST, easing = AcertoMotion.SmoothOut))
                        done = true
                        onConfirmed()
                    }
                }
                .graphicsLayer { translationX = x.value * 0.6f; alpha = 1f - progress * 0.55f }
        ) {
            Icon(
                Icons.Default.TouchApp,
                contentDescription = null,
                tint = Color(0xFFB0B2B6),
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { translationX = nudge.value * 14f * density.density; rotationZ = nudge.value * 8f }
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (done) "Confirmado" else label,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF9A9DA1)
            )
        }
    }
}

/* ---------------- 3. Sucesso (verde-limão) ---------------- */

@Composable
internal fun SuccessScreen(amount: Double, name: String, onClose: () -> Unit, subtitle: String = "Recebido de $name") {
    StatusBarIcons(lightIcons = false)
    val reduced = rememberReducedMotion()
    val draw = remember { Animatable(if (reduced) 1f else 0f) }
    val textIn = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduced) {
            launch { draw.animateTo(1f, tween(AcertoMotion.VERY_SLOW, delayMillis = 120, easing = AcertoMotion.SmoothOut)) }
            launch { textIn.animateTo(1f, tween(AcertoMotion.SLOW, delayMillis = 260, easing = AcertoMotion.SmoothOut)) }
        }
        // fica um instante e fecha sozinho (a tela some com fade, quem chamou anima a saída)
        delay(if (reduced) 1200L else 1700L)
        onClose()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(AcertoPalette.Lime)
            .testTag("receive_success"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(72.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.52f)
                    lineTo(size.width * 0.42f, size.height * 0.76f)
                    lineTo(size.width * 0.84f, size.height * 0.26f)
                }
                val measure = androidx.compose.ui.graphics.PathMeasure().apply { setPath(path, false) }
                val partial = Path()
                measure.getSegment(0f, measure.length * draw.value, partial, true)
                drawPath(partial, AcertoPalette.OnLime, style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            Spacer(Modifier.height(18.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = textIn.value
                    translationY = (1f - textIn.value) * 16f * density
                }
            ) {
                Text(
                    CurrencyFormatter.format(amount),
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 44.sp, fontWeight = FontWeight.Bold),
                    color = AcertoPalette.OnLime
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AcertoPalette.OnLime.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
