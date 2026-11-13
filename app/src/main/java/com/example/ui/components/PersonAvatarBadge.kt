package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonAvatar
import com.example.ui.theme.LocalLiquidGlassColors

/** Converte "#RRGGBB" em Color, com fallback seguro. */
fun personColorOf(hex: String?, fallback: Color): Color = try {
    if (hex.isNullOrBlank()) fallback else Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    fallback
}

/** Ícone do avatar (null = neutro, que usa as iniciais). */
fun PersonAvatar.icon(): ImageVector? = when (this) {
    PersonAvatar.MALE -> Icons.Default.Man
    PersonAvatar.FEMALE -> Icons.Default.Woman
    PersonAvatar.NEUTRAL -> null
}

fun PersonAvatar.label(): String = when (this) {
    PersonAvatar.NEUTRAL -> "Neutro"
    PersonAvatar.MALE -> "Masculino"
    PersonAvatar.FEMALE -> "Feminino"
}

/** Iniciais (até 2 letras) do nome. */
fun initialsOf(name: String): String = name
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercase() }
    .joinToString("")
    .ifEmpty { "?" }

/**
 * Avatar circular de vidro na cor da pessoa: iniciais (neutro) ou ícone masculino/feminino.
 * A troca de avatar anima (giro + escala) e a cor transita suavemente.
 */
@Composable
fun PersonAvatarBadge(
    name: String,
    colorHex: String?,
    avatar: String?,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    val liquid = LocalLiquidGlassColors.current
    val kind = remember(avatar) { PersonAvatar.from(avatar) }
    val target = personColorOf(colorHex, liquid.accent)
    val color by animateColorAsState(target, animationSpec = tween(280), label = "avatar_color")

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.22f))
            .border((size.value / 32f).coerceIn(1f, 1.6f).dp, color.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = kind,
            transitionSpec = {
                (scaleIn(spring(dampingRatio = 0.55f, stiffness = 420f), initialScale = 0.4f) + fadeIn(tween(160)))
                    .togetherWith(scaleOut(tween(140), targetScale = 0.4f) + fadeOut(tween(120)))
            },
            label = "avatar_kind"
        ) { k ->
            val icon = k.icon()
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = k.label(),
                    tint = liquid.textPrimary,
                    modifier = Modifier.size(size * 0.62f)
                )
            } else {
                val initials = initialsOf(name)
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * (if (initials.length > 1) 0.34f else 0.42f)).sp
                    ),
                    color = liquid.textPrimary
                )
            }
        }
    }
}

/** Ícone genérico de pessoa, para quando não há nome (ex.: "Eu"). */
val PersonFallbackIcon: ImageVector get() = Icons.Default.Person
