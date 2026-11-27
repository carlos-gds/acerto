package com.example.ui.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.theme.AcertoPalette
import com.example.ui.theme.LocalLiquidGlassColors
import kotlin.math.roundToInt

/*
 * Guia da primeira vez: o cartão "Vamos começar?" (3 passos que se marcam
 * sozinhos conforme a pessoa usa o app) e dicas de uma vez só, que aparecem
 * na hora certa destacando o elemento de verdade na tela.
 */

/** O que o guia lembra neste aparelho (fora do banco e do backup). */
object GuideStore {
    private const val PREFS = "acerto_prefs"
    private const val START_CLOSED = "guide_start_closed"
    private const val TIP_PREFIX = "guide_tip_"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isStartClosed(context: Context) = prefs(context).getBoolean(START_CLOSED, false)
    fun closeStart(context: Context) { prefs(context).edit().putBoolean(START_CLOSED, true).apply() }
    fun isTipSeen(context: Context, id: String) = prefs(context).getBoolean(TIP_PREFIX + id, false)
    fun markTipSeen(context: Context, id: String) { prefs(context).edit().putBoolean(TIP_PREFIX + id, true).apply() }

    /** "Ver as dicas de novo" (Ajustes): as dicas voltam a aparecer, cada uma uma vez. */
    fun resetTips(context: Context) {
        val p = prefs(context)
        val e = p.edit()
        p.all.keys.filter { it.startsWith(TIP_PREFIX) }.forEach { e.remove(it) }
        e.apply()
    }

    internal fun isGuideKey(key: String?) = key == START_CLOSED || key?.startsWith(TIP_PREFIX) == true
}

/** Estado observável do guia (atualiza quando os Ajustes mandam mostrar as dicas de novo). */
@Stable
class GuideState internal constructor(private val context: Context) {
    var startClosed by mutableStateOf(GuideStore.isStartClosed(context))
        private set
    private val seen = mutableStateMapOf<String, Boolean>()

    fun isSeen(id: String): Boolean = seen.getOrPut(id) { GuideStore.isTipSeen(context, id) }
    fun markSeen(id: String) { GuideStore.markTipSeen(context, id); seen[id] = true }
    fun closeStart() { GuideStore.closeStart(context); startClosed = true }
    internal fun reload() {
        startClosed = GuideStore.isStartClosed(context)
        seen.keys.toList().forEach { seen[it] = GuideStore.isTipSeen(context, it) }
    }
}

@Composable
fun rememberGuideState(): GuideState {
    val context = LocalContext.current
    val state = remember(context) { GuideState(context) }
    DisposableEffect(context) {
        val p = context.applicationContext.getSharedPreferences("acerto_prefs", Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || GuideStore.isGuideKey(key)) state.reload()
        }
        p.registerOnSharedPreferenceChangeListener(listener)
        onDispose { p.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return state
}

/* ---------- Cartão "Vamos começar?" ---------- */

data class StartGuideSteps(val hasPeople: Boolean, val hasPurchase: Boolean, val hasReceived: Boolean) {
    val done get() = listOf(hasPeople, hasPurchase, hasReceived).count { it }
    val allDone get() = done == 3
}

@Composable
fun StartGuideCard(
    steps: StartGuideSteps,
    onAddPerson: () -> Unit,
    onAddPurchase: () -> Unit,
    onReceive: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(liquid.cardSurfaceVariant)
            .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(24.dp))
            .padding(start = 18.dp, end = 8.dp, top = 14.dp, bottom = 10.dp)
            .testTag("start_guide")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Vamos começar?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = liquid.textPrimary)
                Text("${steps.done} de 3 · o Acerto fica pronto em 3 passos", style = MaterialTheme.typography.bodySmall, color = liquid.textMuted)
            }
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Fechar o guia", onClick = onClose)
                    .testTag("start_guide_close"),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Close, contentDescription = "Fechar o guia", tint = liquid.textSecondary, modifier = Modifier.size(20.dp)) }
        }
        // barrinha de progresso
        Box(
            Modifier.padding(top = 10.dp, bottom = 6.dp, end = 10.dp).fillMaxWidth().height(5.dp)
                .clip(RoundedCornerShape(3.dp)).background(liquid.cardBorderSubtle)
        ) {
            Box(Modifier.fillMaxWidth(steps.done / 3f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(AcertoPalette.Mint))
        }
        StartStep(1, "Cadastre quem te deve", "Mãe, amigo, irmão… quem usa o seu cartão", steps.hasPeople, onAddPerson, "start_step_people")
        StartStep(2, "Lance a primeira compra", "À vista ou parcelada, e dá para dividir", steps.hasPurchase, onAddPurchase, "start_step_purchase")
        StartStep(
            3, "Marque quando receber",
            if (steps.hasPurchase) "Toque em Receber quando alguém te pagar" else "Depois da primeira compra",
            steps.hasReceived,
            if (steps.hasPurchase) onReceive else onAddPurchase,
            "start_step_receive"
        )
    }
}

@Composable
private fun StartStep(n: Int, title: String, subtitle: String, done: Boolean, onClick: () -> Unit, tag: String) {
    val liquid = LocalLiquidGlassColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !done, role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Passo $n: $title" + if (done) ", feito" else "" }
            .testTag(tag)
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape)
                .background(if (done) AcertoPalette.Mint else androidx.compose.ui.graphics.Color.Transparent)
                .border(1.5.dp, if (done) AcertoPalette.Mint else liquid.textMuted, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (done) Icon(Icons.Default.Check, contentDescription = null, tint = AcertoPalette.OnLime, modifier = Modifier.size(18.dp))
            else Text("$n", style = MaterialTheme.typography.labelLarge, color = liquid.textSecondary)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(textDecoration = if (done) TextDecoration.LineThrough else null),
                color = if (done) liquid.textMuted else liquid.textPrimary
            )
            if (!done) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = liquid.textMuted)
        }
    }
}

/* ---------- Dicas (coach marks) ---------- */

/** Onde está cada elemento que uma dica pode destacar (medido ao vivo, na raiz). */
@Stable
class CoachTargets {
    internal val rects = mutableStateMapOf<String, Rect>()
    operator fun get(id: String): Rect? = rects[id]
}

val LocalCoachTargets = staticCompositionLocalOf<CoachTargets?> { null }

/** Marca este elemento como alvo da dica [id]; some do mapa quando sai da tela. */
fun Modifier.coachTarget(id: String): Modifier = composed {
    val targets = LocalCoachTargets.current ?: return@composed this
    DisposableEffect(id) { onDispose { targets.rects.remove(id) } }
    this.onGloballyPositioned { c ->
        val p = c.positionInRoot()
        val r = Rect(p, Size(c.size.width.toFloat(), c.size.height.toFloat()))
        if (targets.rects[id] != r) targets.rects[id] = r
    }
}

data class CoachTip(val id: String, val title: String, val text: String)

/**
 * Escurece a tela, deixa um recorte iluminado em volta do alvo e mostra o
 * balão com a dica. Qualquer toque fecha (a dica não volta mais).
 */
@Composable
fun CoachMarkOverlay(tip: CoachTip, target: Rect, onDismiss: () -> Unit) {
    val density = LocalDensity.current
    val reduced = rememberReducedMotion()
    val appear = remember(tip.id) { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(tip.id) { appear.animateTo(1f, tween(260)) }
    val origin = remember { mutableStateOf(Offset.Zero) }
    val screen = remember { mutableStateOf(Size.Zero) }
    val pad = with(density) { 8.dp.toPx() }
    val hole = Rect(target.left - pad, target.top - pad, target.right + pad, target.bottom + pad)
        .translate(-origin.value.x, -origin.value.y)
    val below = hole.center.y < screen.value.height / 2f
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                origin.value = it.positionInRoot()
                screen.value = Size(it.size.width.toFloat(), it.size.height.toFloat())
            }
            .graphicsLayer { alpha = appear.value; compositingStrategy = CompositingStrategy.Offscreen }
            .pointerInput(tip.id) { detectTapGestures { onDismiss() } }
            .drawBehind {
                drawRect(Color.Black.copy(alpha = 0.66f))
                drawRoundRect(
                    Color.Transparent,
                    topLeft = hole.topLeft,
                    size = hole.size,
                    cornerRadius = CornerRadius(26.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
            }
            .testTag("coach_${tip.id}")
    ) {
        val gap = with(density) { 14.dp.toPx() }
        Column(
            Modifier
                .offset {
                    // balão embaixo do recorte (ou em cima, se o alvo estiver na metade de baixo)
                    val y = if (below) hole.bottom + gap else hole.top - gap
                    IntOffset(0, y.roundToInt())
                }
                .graphicsLayer {
                    if (!below) translationY = -size.height
                    translationY += (1f - appear.value) * 16.dp.toPx() * (if (below) 1f else -1f)
                }
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(tip.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF16181A))
            Spacer(Modifier.height(4.dp))
            Text(tip.text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4A4D52))
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF16181A))
                    .clickable(role = Role.Button, onClick = onDismiss)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("coach_ok")
            ) { Text("Entendi", style = MaterialTheme.typography.labelLarge, color = Color.White) }
        }
    }
}
