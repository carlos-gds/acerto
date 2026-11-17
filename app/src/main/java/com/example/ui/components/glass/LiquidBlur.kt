package com.example.ui.components.glass

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/*
 * Liquid Glass adaptativo.
 *
 * - Android 7–11 (API 24–30): vidro simulado (translucidez + brilho + borda), sem blur.
 * - Android 12+ (API 31+): blur real do conteúdo atrás do vidro (RenderEffect via GraphicsLayer)
 *   e blur da janela atrás de diálogos e bottom sheets (cross-window blur do sistema).
 * - Android 13+ (API 33+): blur progressivo (máscara em gradiente) no fade inferior.
 *
 * O blur é desligado em aparelhos de pouca memória e no modo economia de bateria.
 * Só áreas pequenas e fixas recebem blur (barra, FAB, avisos, fade inferior); listas não.
 */

/** Nível de blur disponível neste aparelho. */
enum class LiquidBlurLevel { NONE, BLUR, PROGRESSIVE }

/** Raios padrão do Figma (backdrop-blur 50 nos cards, 2,5–6 nas pílulas). */
object LiquidBlurDefaults {
    val bar: Dp = 24.dp
    val fab: Dp = 20.dp
    val toast: Dp = 24.dp
    val fade: Dp = 16.dp
    const val dialogBehindRadiusDp = 36
}

@Composable
fun rememberLiquidBlurLevel(): LiquidBlurLevel {
    val context = LocalContext.current
    return remember(context) { detectBlurLevel(context) }
}

private fun detectBlurLevel(context: Context): LiquidBlurLevel {
    // Visual Acerto (2026-11): sem vidro nem blur, seguindo o HIG e o Taste Skill.
    // As superfícies agora são sólidas, então o blur só custaria desempenho.
    if (BLUR_DISABLED) return LiquidBlurLevel.NONE
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return LiquidBlurLevel.NONE
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    // Aparelhos de baixo desempenho / pouca memória / economia de bateria: sem blur.
    if (am?.isLowRamDevice == true) return LiquidBlurLevel.NONE
    if ((am?.memoryClass ?: 0) in 1 until 192) return LiquidBlurLevel.NONE
    if (pm?.isPowerSaveMode == true) return LiquidBlurLevel.NONE
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) LiquidBlurLevel.PROGRESSIVE
    else LiquidBlurLevel.BLUR
}

private const val BLUR_DISABLED = true

/** Fonte do "fundo" que os vidros desfocam (o conteúdo das abas). */
@Stable
class LiquidBackdropState internal constructor(internal val layer: GraphicsLayer) {
    internal var sourceCoords by mutableStateOf<LayoutCoordinates?>(null)
}

/** Disponível quando o blur real está ativo; null usa o vidro simulado. */
val LocalLiquidBackdrop = staticCompositionLocalOf<LiquidBackdropState?> { null }

@Composable
fun rememberLiquidBackdropState(): LiquidBackdropState {
    val layer = rememberGraphicsLayer()
    return remember(layer) { LiquidBackdropState(layer) }
}

/**
 * Marca o conteúdo que fica atrás dos vidros. Ele é desenhado uma única vez numa
 * camada (GraphicsLayer) e essa camada é reaproveitada pelos vidros, sem redesenhar a lista.
 */
fun Modifier.liquidBackdropSource(state: LiquidBackdropState?): Modifier =
    if (state == null) this else this
        .onGloballyPositioned { state.sourceCoords = it }
        .drawWithContent {
            state.layer.record { this@drawWithContent.drawContent() }
            drawLayer(state.layer)
        }

/**
 * Desenha, recortado em [shape], o conteúdo de trás desfocado (Android 12+).
 * Sem fonte de backdrop (Android antigo ou blur desligado), não faz nada e o vidro
 * simulado assume. Com [progressive], o blur cresce de cima para baixo (Android 13+).
 */
fun Modifier.liquidBackdropBlur(
    shape: Shape = RectangleShape,
    radius: Dp = LiquidBlurDefaults.bar,
    progressive: Boolean = false
): Modifier = composed {
    val state = LocalLiquidBackdrop.current ?: return@composed this
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@composed this
    val ownLayer = rememberGraphicsLayer()
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    this
        .onGloballyPositioned { coords = it }
        .drawBehind {
            val src = state.sourceCoords ?: return@drawBehind
            val me = coords ?: return@drawBehind
            if (!src.isAttached || !me.isAttached) return@drawBehind
            val offset = src.localPositionOf(me, Offset.Zero)
            val r = radius.toPx()
            ownLayer.renderEffect = BlurEffect(r, r, TileMode.Clamp)
            ownLayer.record {
                translate(-offset.x, -offset.y) { drawLayer(state.layer) }
                if (progressive) {
                    // Máscara: transparente em cima, cheio embaixo (blur progressivo)
                    drawRect(
                        brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
                        blendMode = BlendMode.DstIn
                    )
                }
            }
            val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawBehind)) }
            clipPath(path) { drawLayer(ownLayer) }
        }
}

/** true quando o sistema permite blur entre janelas (Android 12+, configuração do aparelho). */
@Composable
fun rememberCrossWindowBlurEnabled(): Boolean {
    val context = LocalContext.current
    val level = rememberLiquidBlurLevel()
    var enabled by remember { mutableStateOf(false) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && level != LiquidBlurLevel.NONE) {
        DisposableEffect(context) {
            val wm = context.getSystemService(WindowManager::class.java)
            val listener = java.util.function.Consumer<Boolean> { enabled = it }
            enabled = wm?.isCrossWindowBlurEnabled == true
            wm?.addCrossWindowBlurEnabledListener(listener)
            onDispose { wm?.removeCrossWindowBlurEnabledListener(listener) }
        }
    }
    return enabled
}

/**
 * Superfície dos diálogos e sheets: mais transparente quando o fundo da janela é
 * desfocado (Android 12+), quase opaca no fallback para manter a leitura.
 */
@Composable
fun liquidDialogSurface(): Color {
    val liquid = com.example.ui.theme.LocalLiquidGlassColors.current
    return if (rememberCrossWindowBlurEnabled()) {
        liquid.sheetSurface.copy(alpha = if (liquid.isDark) 0.72f else 0.78f)
    } else {
        liquid.sheetSurface
    }
}

/**
 * Chamado dentro do conteúdo de um diálogo/sheet: desfoca o app atrás da janela
 * (Android 12+) e suaviza o escurecimento. No Android antigo não faz nada.
 */
@Composable
fun LiquidDialogBlur(radiusDp: Int = LiquidBlurDefaults.dialogBehindRadiusDp) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    if (!rememberCrossWindowBlurEnabled()) return
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? DialogWindowProvider)?.window
            ?: (view as? DialogWindowProvider)?.window
        if (window != null) {
            val px = (radiusDp * view.resources.displayMetrics.density).toInt()
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply { blurBehindRadius = px }
            window.setDimAmount(0.32f)
        }
        onDispose { }
    }
}
