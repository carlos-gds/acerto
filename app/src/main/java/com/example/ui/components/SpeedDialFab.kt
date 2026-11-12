package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalLiquidGlassColors

@Composable
fun SpeedDialFab(
    listState: LazyListState,
    onAiEntryClick: () -> Unit,
    onSharedBillClick: () -> Unit,
    onCardExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
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

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "fab_rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Darkened backdrop scrim overlay when menu is open
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(180)),
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
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Expanded Menu Items
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(200)
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = tween(150)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Entrada IA (Special Highlight - Purple)
                        SpeedDialMenuItem(
                            label = "Entrada IA",
                            icon = Icons.Default.AutoAwesome,
                            iconColor = Color.White,
                            fabContainerColor = Color(0xFF8B5CF6),
                            labelContainerColor = liquid.cardSurface,
                            labelTextColor = liquid.textPrimary,
                            testTag = "speed_dial_ai_entry",
                            onClick = {
                                isExpanded = false
                                onAiEntryClick()
                            }
                        )

                        // 2. Dividir Conta
                        SpeedDialMenuItem(
                            label = "Dividir Conta",
                            icon = Icons.Default.Splitscreen,
                            iconColor = liquid.textPrimary,
                            fabContainerColor = liquid.cardSurface,
                            labelContainerColor = liquid.cardSurface,
                            labelTextColor = liquid.textPrimary,
                            testTag = "speed_dial_shared_bill",
                            onClick = {
                                isExpanded = false
                                onSharedBillClick()
                            }
                        )

                        // 3. Compra no Cartão
                        SpeedDialMenuItem(
                            label = "Compra no Cartão",
                            icon = Icons.Default.CreditCard,
                            iconColor = liquid.pillPrimaryText,
                            fabContainerColor = liquid.pillPrimaryBg,
                            labelContainerColor = liquid.cardSurface,
                            labelTextColor = liquid.textPrimary,
                            testTag = "speed_dial_card_expense",
                            onClick = {
                                isExpanded = false
                                onCardExpenseClick()
                            }
                        )

                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                // Primary Floating Action Button with smoothly rotating '+' -> 'x'
                FloatingActionButton(
                    onClick = { isExpanded = !isExpanded },
                    containerColor = if (isExpanded) liquid.cardSurfaceVariant else liquid.pillPrimaryBg,
                    contentColor = if (isExpanded) liquid.textPrimary else liquid.pillPrimaryText,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(58.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = if (liquid.isDark) Color.Black else Color(0x3F0F172A)
                        )
                        .border(1.dp, liquid.cardBorder, CircleShape)
                        .testTag("speed_dial_main_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isExpanded) "Fechar Menu" else "Adicionar Gasto",
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(rotationAngle)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedDialMenuItem(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    fabContainerColor: Color,
    labelContainerColor: Color,
    labelTextColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val liquid = LocalLiquidGlassColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // Text Label Chip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = labelContainerColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle),
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = if (liquid.isDark) Color.Black else Color(0x1F0F172A)
                )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = labelTextColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }

        // Mini FAB
        SmallFloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = fabContainerColor,
            contentColor = iconColor,
            modifier = Modifier
                .size(44.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
