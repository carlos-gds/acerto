package com.example.ui.components.glass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Tokens de movimento do transitions.dev traduzidos para o Compose:
 * sair rápido (150 ms), entrar em 250 ms, curva "smooth out" e
 * deslocamentos pequenos (4 a 8 dp). Nada de mola com quique em UI comum.
 */
object AcertoMotion {
    /** cubic-bezier(0.22, 1, 0.36, 1) */
    val SmoothOut = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    /** cubic-bezier(0.34, 1.36, 0.64, 1): só para o "check" de sucesso. */
    val Bounce = CubicBezierEasing(0.34f, 1.36f, 0.64f, 1f)
    const val QUICK = 150
    const val FAST = 250
    const val SLOW = 400
    const val VERY_SLOW = 500
    const val STAGGER = 40
}

/** Text states swap: o texto antigo sai para cima, o novo entra de baixo (150 ms). */
fun textSwapTransform(reduced: Boolean): ContentTransform =
    if (reduced) {
        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
    } else {
        (fadeIn(tween(AcertoMotion.QUICK, easing = AcertoMotion.SmoothOut)) +
            slideInVertically(tween(AcertoMotion.QUICK, easing = AcertoMotion.SmoothOut)) { it / 5 })
            .togetherWith(
                fadeOut(tween(AcertoMotion.QUICK)) +
                    slideOutVertically(tween(AcertoMotion.QUICK)) { -it / 5 }
            )
    }

/**
 * Number pop-in: só os caracteres que mudam giram, alinhados pela direita
 * (centavos ficam no lugar quando o número cresce). Os dígitos que mudam
 * entram de baixo, menores, e assentam; os iguais ficam parados.
 */
@Composable
fun NumberPopIn(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    val reduced = rememberReducedMotion()
    Row(modifier = modifier.semantics(mergeDescendants = true) { contentDescription = text }) {
        val chars = text.toList()
        chars.forEachIndexed { index, ch ->
            val fromEnd = chars.size - 1 - index
            AnimatedContent(
                targetState = ch,
                transitionSpec = {
                    if (reduced || !(targetState.isDigit() || initialState.isDigit())) {
                        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                    } else {
                        val delay = (fromEnd.coerceAtMost(6)) * AcertoMotion.STAGGER / 2
                        (fadeIn(tween(AcertoMotion.FAST, delayMillis = delay, easing = AcertoMotion.SmoothOut)) +
                            scaleIn(tween(AcertoMotion.VERY_SLOW, delayMillis = delay, easing = AcertoMotion.Bounce), initialScale = 0.7f) +
                            slideInVertically(tween(AcertoMotion.VERY_SLOW, delayMillis = delay, easing = AcertoMotion.Bounce)) { it / 2 })
                            .togetherWith(
                                fadeOut(tween(155)) + slideOutVertically(tween(155)) { -it / 2 }
                            )
                    }
                },
                label = "digit_$fromEnd"
            ) { c ->
                Text(text = c.toString(), style = style, color = color)
            }
        }
    }
}

/**
 * Success check: o ícone entra girando de 80°, sobe de leve e assenta
 * (transitions.dev "Success check"), em 500 ms.
 */
@Composable
fun SuccessCheck(
    background: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    val reduced = rememberReducedMotion()
    val rotation = remember { Animatable(if (reduced) 0f else 80f) }
    val alpha = remember { Animatable(if (reduced) 1f else 0f) }
    val lift = remember { Animatable(if (reduced) 0f else 10f) }
    LaunchedEffect(Unit) {
        if (reduced) return@LaunchedEffect
        launch { rotation.animateTo(0f, tween(AcertoMotion.VERY_SLOW, easing = AcertoMotion.SmoothOut)) }
        launch { alpha.animateTo(1f, tween(AcertoMotion.VERY_SLOW, easing = AcertoMotion.SmoothOut)) }
        launch { lift.animateTo(0f, tween(AcertoMotion.VERY_SLOW, easing = AcertoMotion.Bounce)) }
    }
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = rotation.value
                this.alpha = alpha.value
                translationY = lift.value * density
            }
            .background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.62f)
        )
    }
}
