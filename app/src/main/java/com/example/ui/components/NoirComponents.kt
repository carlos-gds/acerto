package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.ui.components.glass.AcertoMotion
import com.example.ui.components.glass.pressScale
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.theme.AcertoPalette

/*
 * Peças do visual "Noir": cabeçalho escuro com brilho, pílulas, cartões
 * empilhados espiando na borda e a folha branca que sobe por cima.
 */

/** Ícones da barra de status claros (sobre o cabeçalho escuro) ou escuros. */
@Composable
fun StatusBarIcons(lightIcons: Boolean, lightNavIcons: Boolean? = null) {
    val view = LocalView.current
    val activity = LocalContext.current as? Activity ?: return
    DisposableEffect(lightIcons, lightNavIcons) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        val before = controller.isAppearanceLightStatusBars
        val navBefore = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = !lightIcons
        if (lightNavIcons != null) controller.isAppearanceLightNavigationBars = !lightNavIcons
        onDispose {
            controller.isAppearanceLightStatusBars = before
            if (lightNavIcons != null) controller.isAppearanceLightNavigationBars = navBefore
        }
    }
}

/**
 * Fundo do cabeçalho: grafite com um brilho azul-esverdeado no canto direito.
 * O brilho fica preso à TELA (não à página): ao arrastar entre abas, todas
 * pintam o mesmo fundo no mesmo lugar e não aparece "corte" entre elas.
 */
fun Modifier.noirHeaderBackground(): Modifier = this.composed {
    val origin = remember { androidx.compose.runtime.mutableStateOf(Offset.Zero) }
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenW = with(density) { config.screenWidthDp.dp.toPx() }
    val glowY = with(density) { 230.dp.toPx() }
    Modifier
        .androidx_onPositioned { origin.value = it }
        .background(AcertoPalette.Header)
        .drawBehind {
            val o = origin.value
            drawRect(
                Brush.radialGradient(
                    colors = listOf(AcertoPalette.HeaderGlow.copy(alpha = 0.85f), Color.Transparent),
                    center = Offset(screenW * 0.92f - o.x, glowY - o.y),
                    radius = screenW * 0.75f
                )
            )
        }
}

private fun Modifier.androidx_onPositioned(onRoot: (Offset) -> Unit): Modifier =
    this.then(Modifier.onGloballyPositioned { onRoot(it.positionInRoot()) })

/** Pílula do cabeçalho: clara (principal, com ícone num círculo escuro) ou escura. */
@Composable
fun NoirPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    light: Boolean = true,
    icon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val bg = if (light) AcertoPalette.PillLight else AcertoPalette.PillDark
    val fg = if (light) Color(0xFF16181A) else AcertoPalette.TextOnHeader
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = 44.dp)
            .pressScale(interaction, pressedScale = 0.95f)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(start = if (icon != null) 6.dp else 18.dp, end = 18.dp, top = 6.dp, bottom = 6.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(if (light) Color(0xFF16181A) else AcertoPalette.HeaderTile),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = if (light) Color.White else fg, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.titleMedium, color = fg, maxLines = 1)
    }
}

/** Botão redondo escuro do cabeçalho (busca, privacidade). */
@Composable
fun NoirRoundButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .pressScale(interaction, pressedScale = 0.92f)
            .clip(CircleShape)
            .background(AcertoPalette.PillDark)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = AcertoPalette.TextOnHeader, modifier = Modifier.size(22.dp)) }
}

/** Bloco cinza do cabeçalho com rótulo, valor e avatares empilhados. */
@Composable
fun NoirHeaderTile(
    label: String,
    value: String,
    people: List<Triple<String, String, String>>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AcertoPalette.HeaderTile)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = AcertoPalette.TextOnHeaderMuted)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AcertoPalette.TextOnHeader, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (people.isNotEmpty()) {
            // indo para Pessoas: os avatares somem aqui e reaparecem lá, se espalhando na grade
            val tabPosition = LocalTabPosition.current
            Box(Modifier.graphicsLayer { alpha = 1f - ((1f - tabPosition()) * 2.5f).coerceIn(0f, 1f) }) {
                StackedAvatars(people = people, size = 34.dp, ringColor = AcertoPalette.HeaderTile)
            }
        }
    }
}

/** Topo arredondado da folha branca, desenhado sobre o fim do cabeçalho escuro. */
@Composable
fun SheetTop(
    sheetColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // atrás dos cantos arredondados: o mesmo fundo do cabeçalho (com o brilho), sem "pontinha"
    Box(modifier = modifier.fillMaxWidth().noirHeaderBackground()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(sheetColor)
                .padding(top = 10.dp)
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x22000000))
            )
            content()
        }
    }
}

/** Valor com os centavos menores e apagados: "R$ 7.854" + ",43". */
@Composable
fun SplitMoney(
    text: String,
    big: androidx.compose.ui.text.TextStyle,
    color: Color,
    centsColor: Color,
    modifier: Modifier = Modifier
) {
    val comma = text.lastIndexOf(',')
    Row(verticalAlignment = Alignment.Bottom, modifier = modifier.semantics(mergeDescendants = true) { contentDescription = text }) {
        if (comma > 0 && comma >= text.length - 3) {
            com.example.ui.components.glass.NumberPopIn(text = text.substring(0, comma), style = big, color = color)
            Text(
                text.substring(comma),
                style = big.copy(fontSize = big.fontSize * 0.55f, fontWeight = FontWeight.Medium),
                color = centsColor,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
        } else {
            Text(text, style = big, color = color)
        }
    }
}


/** Altura da barra de navegação do Android (gestos ou botões), para o conteúdo passar por trás dela. */
@Composable
fun navBarsBottom(): Dp =
    androidx.compose.foundation.layout.WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

/**
 * Posição contínua das abas enquanto arrasta (0 = Pessoas, 1 = Início, 2 = Cartão).
 * Lida só dentro de graphicsLayer/draw, para animar sem recompor.
 */
val LocalTabPosition = androidx.compose.runtime.staticCompositionLocalOf<() -> Float> { { 1f } }
