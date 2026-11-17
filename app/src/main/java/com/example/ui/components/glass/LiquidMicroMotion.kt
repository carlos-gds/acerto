package com.example.ui.components.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LiquidAccent
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlinx.coroutines.launch

/*
 * Microinterações do Acerto, inspiradas nos padrões "Scale Pop", "Glow Border",
 * "Shake Input" e contadores. Tudo é lido na fase de desenho (graphicsLayer/drawBehind)
 * e respeita a redução de movimento do sistema.
 */

/** easeOutBack (leve overshoot), usado na abertura dos diálogos. */
private val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/** "Scale Pop": o diálogo nasce em 72% com leve overshoot e opacidade rápida. */
fun Modifier.liquidDialogEnter(): Modifier = composed {
    val reduced = rememberReducedMotion()
    val scale = remember { Animatable(if (reduced) 1f else 0.72f) }
    val alpha = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduced) {
            kotlinx.coroutines.coroutineScope {
                launch { alpha.animateTo(1f, tween(160)) }
                launch { scale.animateTo(1f, tween(420, easing = EaseOutBack)) }
            }
        }
    }
    graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        this.alpha = alpha.value
    }
}

/**
 * Campo de texto vivo: brilho periwinkle na borda ao focar ("Glow Border") e um
 * pulso curto a cada caractere digitado.
 */
fun Modifier.liquidFieldMotion(value: Any?): Modifier = composed {
    val reduced = rememberReducedMotion()
    var focused by remember { mutableStateOf(false) }
    val focus by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "field_focus"
    )
    val pulse = remember { Animatable(0f) }
    var first by remember { mutableStateOf(true) }
    LaunchedEffect(value) {
        if (first) {
            first = false
        } else if (!reduced && focused) {
            pulse.snapTo(1f)
            pulse.animateTo(0f, tween(260))
        }
    }
    this
        .onFocusChanged { focused = it.isFocused }
        .drawBehind {
            val glow = (focus * 0.55f + pulse.value * 0.35f).coerceIn(0f, 1f)
            if (glow <= 0.01f) return@drawBehind
            val spread = 3.dp.toPx() + 3.dp.toPx() * pulse.value
            drawRoundRect(
                color = LiquidAccent.copy(alpha = 0.28f * glow),
                topLeft = Offset(-spread, 8.dp.toPx() - spread),
                size = Size(size.width + spread * 2, size.height - 8.dp.toPx() + spread * 2),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = spread * 1.4f)
            )
        }
}

/**
 * "Shake Input": treme quando [key] muda para um valor não nulo
 * (x = 14·sen(2π·9·t)·e^(−6t), ~0,8 s).
 */
fun Modifier.shakeOnChange(key: Any?): Modifier = composed {
    val reduced = rememberReducedMotion()
    val t = remember { Animatable(1f) }
    LaunchedEffect(key) {
        if (key != null && !reduced) {
            t.snapTo(0f)
            t.animateTo(1f, tween(800))
        }
    }
    graphicsLayer {
        val time = t.value * 0.8f
        translationX = if (t.value >= 1f) 0f else
            (14f * sin(2 * PI * 9 * time) * exp(-6.0 * time)).toFloat() * density / 2.6f
    }
}

/**
 * Contador animado para valores em reais: rola do valor anterior até o novo.
 * Interpola em Double e termina exatamente no valor alvo (sem erro de centavos).
 */
private const val MONEY_COUNTER_ENABLED = false

@Composable
fun animatedMoney(target: Double): Double {
    // Valores não contam mais até o número: o número muda na hora (HIG: movimento só com propósito).
    if (!MONEY_COUNTER_ENABLED) return target
    val reduced = rememberReducedMotion()
    var from by remember { mutableStateOf(target) }
    var to by remember { mutableStateOf(target) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (target != to) {
            val current = from + (to - from) * progress.value
            from = current
            to = target
            if (reduced) {
                progress.snapTo(1f)
            } else {
                progress.snapTo(0f)
                progress.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
            }
        }
    }
    val p = progress.value
    return if (p >= 1f) to else from + (to - from) * p
}

/** Pop de entrada com mola (ícones de sucesso, selos). */
fun Modifier.popIn(): Modifier = composed {
    val reduced = rememberReducedMotion()
    val s = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduced) s.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 420f))
    }
    graphicsLayer {
        scaleX = s.value
        scaleY = s.value
    }
}
