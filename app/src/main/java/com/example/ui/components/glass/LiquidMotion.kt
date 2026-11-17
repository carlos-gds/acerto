package com.example.ui.components.glass

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/*
 * Movimento e glow do Liquid Glass.
 *
 * Desempenho (telas de 90/120 Hz): os valores animados são lidos dentro de
 * graphicsLayer {} e drawBehind {}, então a animação acontece só na fase de
 * desenho, sem recompor o conteúdo a cada quadro. Não há animações infinitas.
 */

/** Molas padrão do app: rápidas e sem quique exagerado. */
object LiquidMotion {
    /** "Press Down": mola subamortecida (ζ = 0,42, ω₀ = 20 rad/s -> rigidez 400), com leve overshoot. */
    val press = spring<Float>(dampingRatio = 0.42f, stiffness = 400f)
    val selection = spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
    val appear = tween<Float>(durationMillis = 320, easing = FastOutSlowInEasing)
}

/**
 * true quando o usuário desativou ou reduziu as animações do sistema
 * (Opções do desenvolvedor / Acessibilidade > Remover animações).
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val scale = try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        } catch (_: Exception) {
            1f
        }
        scale == 0f
    }
}

/** Feedback de toque: encolhe levemente enquanto pressionado e volta com mola. */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.95f
): Modifier = composed {
    val pressed = interactionSource.collectIsPressedAsState()
    val scale = animateFloatAsState(
        targetValue = if (pressed.value) pressedScale else 1f,
        animationSpec = LiquidMotion.press,
        label = "press_scale"
    )
    graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * Glow simulado: halo radial atrás do elemento (sem blur real, Android 7+).
 * [intensity] é lido na fase de desenho, então pode ser animado sem recompor.
 */
private const val GLOW_DISABLED = true

fun Modifier.glow(
    color: Color,
    radius: Dp = 28.dp,
    intensity: () -> Float = { 1f }
): Modifier = drawBehind {
    // Sem brilhos no visual Acerto (Taste Skill reprova glow/sombra em repouso).
    if (GLOW_DISABLED) return@drawBehind
    val alpha = intensity().coerceIn(0f, 1f)
    if (alpha <= 0.01f) return@drawBehind
    val r = radius.toPx()
    val c = Offset(size.width / 2f, size.height / 2f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = color.alpha * 0.55f * alpha), Color.Transparent),
            center = c,
            radius = r
        ),
        radius = r,
        center = c
    )
}

/** Registro dos itens que já animaram a entrada (para não reanimar ao rolar de volta). */
class AppearTracker internal constructor() {
    internal val seen = HashSet<Any>()
}

@Composable
fun rememberAppearTracker(): AppearTracker = remember { AppearTracker() }

/**
 * Aparição progressiva de itens de lista: fade + subida curta, com atraso
 * escalonado pelo índice (limitado para não atrasar listas longas).
 */
/** Listas em cascata desligadas: em tela de uso diário o movimento só atrasa a leitura. */
private const val STAGGER_ENABLED = false

fun Modifier.staggeredAppear(
    key: Any,
    index: Int,
    tracker: AppearTracker,
    enabled: Boolean = true
): Modifier = if (!STAGGER_ENABLED) this else composed {
    // Decidido uma vez por item: recomposições durante a animação não a interrompem.
    val shouldAnimate = remember(key) { enabled && key !in tracker.seen }
    if (!shouldAnimate) return@composed this
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        tracker.seen.add(key)
        delay(index.coerceIn(0, 6) * 45L)
        progress.animateTo(1f, LiquidMotion.appear)
    }
    graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * 20.dp.toPx()
    }
}
