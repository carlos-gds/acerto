package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.LiquidBlurDefaults
import com.example.ui.components.glass.LocalLiquidBackdrop
import com.example.ui.components.glass.glow
import com.example.ui.components.glass.liquidBackdropBlur
import com.example.ui.components.glass.pressScale
import com.example.ui.theme.LiquidAccent
import com.example.ui.theme.LiquidAccentEnd
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidGlassColors

/** Azul mais intenso do FAB (glow e gradiente). */
private val FabBlue = Color(0xFF3D5AFE)

@Composable
fun SpeedDialFab(
    listState: LazyListState,
    onAiEntryClick: () -> Unit,
    onSharedBillClick: () -> Unit,
    onCardExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val hasBackdrop = LocalLiquidBackdrop.current != null
    var isExpanded by remember { mutableStateOf(false) }
    var isFabVisible by remember { mutableStateOf(true) }

    // Track scroll direction for auto-hide
    var prevIndex by remember { mutableIntStateOf(listState.firstVisibleItemIndex) }
    var prevOffset by remember { mutableIntStateOf(listState.firstVisibleItemScrollOffset) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val currIndex = listState.firstVisibleItemIndex
        val currOffset = listState.firstVisibleItemScrollOffset

        if (currIndex > prevIndex || (currIndex == prevIndex && currOffset > prevOffset + 12)) {
            // Scrolling down -> hide FAB and collapse menu
            isFabVisible = false
            if (isExpanded) isExpanded = false
        } else if (currIndex < prevIndex || (currIndex == prevIndex && currOffset < prevOffset - 12)) {
            // Scrolling up -> show FAB
            isFabVisible = true
        }

        prevIndex = currIndex
        prevOffset = currOffset
    }

    // Auto-close menu if scrolling starts while open
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && isExpanded) {
            isExpanded = false
        }
    }

    // "+" -> "×" com mola
    val rotation = animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 380f),
        label = "fab_rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim escurecido ao abrir o menu
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isExpanded = false
                    }
                    .testTag("speed_dial_scrim")
            )
        }

        // Speed Dial Container at bottom-right
        AnimatedVisibility(
            visible = isFabVisible,
            enter = fadeIn() + scaleIn(initialScale = 0.6f, animationSpec = spring(0.7f, 400f)) +
                slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + scaleOut(targetScale = 0.6f) + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Itens entram de baixo para cima, escalonados (o mais perto do + primeiro)
                SpeedDialMenuItem(
                    visible = isExpanded,
                    order = 2,
                    label = "Entrada IA",
                    icon = Icons.Default.AutoAwesome,
                    highlighted = true,
                    testTag = "speed_dial_ai_entry",
                    onClick = {
                        isExpanded = false
                        onAiEntryClick()
                    }
                )
                SpeedDialMenuItem(
                    visible = isExpanded,
                    order = 1,
                    label = "Dividir Conta",
                    icon = Icons.Default.Splitscreen,
                    highlighted = false,
                    testTag = "speed_dial_shared_bill",
                    onClick = {
                        isExpanded = false
                        onSharedBillClick()
                    }
                )
                SpeedDialMenuItem(
                    visible = isExpanded,
                    order = 0,
                    label = "Compra no Cartão",
                    icon = Icons.Default.CreditCard,
                    highlighted = false,
                    testTag = "speed_dial_card_expense",
                    onClick = {
                        isExpanded = false
                        onCardExpenseClick()
                    }
                )

                Spacer(modifier = Modifier.height(2.dp))

                // FAB principal: vidro + gradiente azul intenso + glow
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val glowIntensity = animateFloatAsState(
                    targetValue = when {
                        pressed -> 1f
                        isExpanded -> 0.9f
                        else -> 0.6f
                    },
                    animationSpec = tween(260),
                    label = "fab_glow"
                )
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .pressScale(interaction, pressedScale = 0.92f)
                        .glow(FabBlue, radius = 56.dp) { glowIntensity.value }
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            ambientColor = FabBlue,
                            spotColor = FabBlue
                        )
                        .clip(CircleShape)
                        // Android 12+: conteúdo de trás desfocado; o gradiente fica levemente translúcido
                        .liquidBackdropBlur(CircleShape, LiquidBlurDefaults.fab)
                        .background(
                            Brush.linearGradient(
                                if (hasBackdrop) listOf(FabBlue.copy(alpha = 0.86f), LiquidAccentEnd.copy(alpha = 0.78f))
                                else listOf(FabBlue, LiquidAccentEnd)
                            )
                        )
                        .background(liquid.glassHighlight)
                        .border(1.dp, liquid.specularBorder, CircleShape)
                        .clickable(
                            interactionSource = interaction,
                            indication = LocalIndication.current,
                            role = Role.Button
                        ) { isExpanded = !isExpanded }
                        .testTag("speed_dial_main_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isExpanded) "Fechar Menu" else "Adicionar Gasto",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { rotationZ = rotation.value }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedDialMenuItem(
    visible: Boolean,
    order: Int,
    label: String,
    icon: ImageVector,
    highlighted: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current
    val delay = order * 45

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200, delayMillis = delay)) +
            scaleIn(tween(240, delayMillis = delay), initialScale = 0.7f, transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)) +
            slideInVertically(tween(240, delayMillis = delay)) { it / 2 },
        exit = fadeOut(tween(120)) + scaleOut(tween(140), targetScale = 0.8f)
    ) {
        val interaction = remember { MutableInteractionSource() }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .pressScale(interaction)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick
                )
        ) {
            // Rótulo em vidro opaco (legível sobre o scrim)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = liquid.textPrimary,
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color.Black)
                    .clip(RoundedCornerShape(12.dp))
                    .background(liquid.sheetSurface)
                    .border(1.dp, liquid.specularBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )

            // Mini FAB de vidro; "Entrada IA" em destaque com gradiente e glow
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .then(if (highlighted) Modifier.glow(LiquidAccent, radius = 40.dp) else Modifier)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (highlighted) liquid.accentGradient else Brush.linearGradient(listOf(liquid.sheetSurface, liquid.sheetSurface)))
                    .background(liquid.glassHighlight)
                    .border(1.dp, liquid.specularBorder, CircleShape)
                    .testTag(testTag),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (highlighted) Color.White else liquid.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
