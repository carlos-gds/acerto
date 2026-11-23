package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.AcertoMotion
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.theme.LocalLiquidGlassColors

/** Card do visual Acerto: superfície sólida; no escuro, uma borda fina separa do fundo. */
@Composable
fun Modifier.acertoCard(shape: Shape = RoundedCornerShape(20.dp)): Modifier {
    val liquid = LocalLiquidGlassColors.current
    val base = this.clip(shape).background(liquid.cardSurface)
    return if (liquid.isDark) base.border(1.dp, liquid.cardBorderSubtle, shape) else base
}

/**
 * Controle segmentado (estilo iOS): trilha cinza e o item escolhido numa
 * "pastilha" que desliza. Neutro de propósito: a cor fica para os dados.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "segment"
) {
    val liquid = LocalLiquidGlassColors.current
    val reduced = rememberReducedMotion()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(liquid.cardSurfaceVariant)
            .padding(3.dp)
    ) {
        val itemWidth = maxWidth / options.size
        val offset by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = tween(if (reduced) 0 else AcertoMotion.FAST, easing = AcertoMotion.SmoothOut),
            label = "segment_thumb"
        )
        Box(
            modifier = Modifier
                .offset(x = offset)
                .width(itemWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(if (liquid.isDark) liquid.cardSurface else androidx.compose.ui.graphics.Color.White)
                .then(if (liquid.isDark) Modifier.border(1.dp, liquid.cardBorder, RoundedCornerShape(10.dp)) else Modifier)
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val color by animateColorAsState(
                    if (selected) liquid.textPrimary else liquid.textSecondary,
                    animationSpec = tween(AcertoMotion.FAST),
                    label = "segment_text"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(role = Role.Tab, onClick = { onSelect(index) })
                        .semantics { this.selected = selected }
                        .testTag("${testTagPrefix}_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** Avatares sobrepostos (até [max]), com um "+N" quando houver mais gente. */
@Composable
fun StackedAvatars(
    people: List<Triple<String, String, String>>, // nome, cor, avatar
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    max: Int = 4,
    ringColor: androidx.compose.ui.graphics.Color? = null
) {
    val liquid = LocalLiquidGlassColors.current
    val ring = ringColor ?: liquid.cardSurface
    val shown = people.take(max)
    val extra = people.size - shown.size
    val step = size * 0.68f
    val total = step * (shown.size - 1 + if (extra > 0) 1 else 0) + size
    Box(modifier = modifier.width(total).height(size)) {
        shown.forEachIndexed { i, (name, color, avatar) ->
            Box(
                Modifier
                    .offset(x = step * i)
                    .size(size)
                    .clip(CircleShape)
                    .background(ring)
                    .padding(2.dp)
            ) {
                PersonAvatarBadge(name = name, colorHex = color, avatar = avatar, size = size - 4.dp)
            }
        }
        if (extra > 0) {
            Box(
                Modifier
                    .offset(x = step * shown.size)
                    .size(size)
                    .clip(CircleShape)
                    .background(ring)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(liquid.cardSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("+$extra", style = MaterialTheme.typography.labelMedium, color = liquid.textPrimary)
            }
        }
    }
}
