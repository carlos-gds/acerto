package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.glass.liquidGlass
import com.example.ui.theme.LiquidShapes
import com.example.ui.theme.LocalLiquidElevation
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.CurrencyFormatter
import java.util.Calendar

data class MonthOption(val year: Int, val month: Int)

@Composable
fun MonthSelector(
    selectedYear: Int,
    selectedMonth: Int,
    onMonthSelected: (year: Int, month: Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    val currentCal = Calendar.getInstance()
    val thisYear = currentCal.get(Calendar.YEAR)
    val thisMonth = currentCal.get(Calendar.MONTH) + 1

    // Window of months
    val monthOptions = ((-3)..9).map { offset ->
        val totalMonths = (thisYear * 12 + (thisMonth - 1)) + offset
        MonthOption(year = totalMonths / 12, month = (totalMonths % 12) + 1)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(selectedYear, selectedMonth) {
        val index = monthOptions.indexOfFirst { it.year == selectedYear && it.month == selectedMonth }
        if (index >= 0) {
            listState.animateScrollToItem(maxOf(0, index - 2))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = LiquidShapes.card,
                elevation = LocalLiquidElevation.current.medium
            )
            .testTag("month_selector")
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)) {
            // Header with navigation arrows and month title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(liquid.circleButtonBg)
                        .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                        .clickable { onPreviousMonth() }
                        .testTag("prev_month_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Mês anterior",
                        tint = liquid.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${CurrencyFormatter.getMonthName(selectedMonth)} $selectedYear",
                        style = MaterialTheme.typography.titleLarge,
                        color = liquid.textPrimary
                    )
                    if (selectedYear == thisYear && selectedMonth == thisMonth) {
                        Box(
                            modifier = Modifier
                                .clip(LiquidShapes.pill)
                                .background(liquid.accent.copy(alpha = 0.16f))
                                .border(1.dp, liquid.accent.copy(alpha = 0.35f), LiquidShapes.pill)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Atual",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = liquid.accent
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(liquid.circleButtonBg)
                        .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                        .clickable { onNextMonth() }
                        .testTag("next_month_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Próximo mês",
                        tint = liquid.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Month Pills Scroll
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                items(monthOptions, key = { "${it.year}_${it.month}" }) { opt ->
                    val isSelected = opt.year == selectedYear && opt.month == selectedMonth

                    val chipModifier = if (isSelected) {
                        Modifier
                            .clip(LiquidShapes.row)
                            .background(liquid.accentGradient)
                            .border(1.dp, liquid.specularBorder, LiquidShapes.row)
                    } else {
                        Modifier
                            .clip(LiquidShapes.row)
                            .background(liquid.cardSurfaceVariant)
                            .border(1.dp, liquid.cardBorderSubtle, LiquidShapes.row)
                    }
                    Box(
                        modifier = chipModifier
                            .clickable { onMonthSelected(opt.year, opt.month) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("month_chip_${opt.month}_${opt.year}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = CurrencyFormatter.getMonthShortName(opt.month),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                color = if (isSelected) liquid.pillPrimaryText
                                else liquid.textPrimary
                            )
                            Text(
                                text = "${opt.year}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) liquid.pillPrimaryText.copy(alpha = 0.7f)
                                else liquid.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Seletor de mês compacto em pílula de vidro: "‹ Novembro 2026 ›".
 * Tocar no nome do mês volta para o mês atual (quando não estiver nele).
 */
@Composable
fun CompactMonthSelector(
    selectedYear: Int,
    selectedMonth: Int,
    onMonthSelected: (year: Int, month: Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
    /** true quando o app ficou neste mês por haver pendências antigas. */
    pendingHint: Boolean = false
) {
    val liquid = LocalLiquidGlassColors.current
    val currentCal = Calendar.getInstance()
    val thisYear = currentCal.get(Calendar.YEAR)
    val thisMonth = currentCal.get(Calendar.MONTH) + 1
    val isCurrent = selectedYear == thisYear && selectedMonth == thisMonth

    Row(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = LiquidShapes.pill,
                surface = liquid.pillSecondaryBg,
                elevation = LocalLiquidElevation.current.low
            )
            .padding(4.dp)
            .testTag("month_selector"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(liquid.circleButtonBg)
                .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                .clickable { onPreviousMonth() }
                .testTag("prev_month_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Mês anterior",
                tint = liquid.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Row(
            modifier = Modifier
                .clip(LiquidShapes.pill)
                .clickable(enabled = !isCurrent) { onMonthSelected(thisYear, thisMonth) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nome do mês desliza na direção da navegação
            AnimatedContent(
                targetState = selectedYear * 12 + (selectedMonth - 1),
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 420f)) { it / 3 * dir } + fadeIn(tween(180)))
                        .togetherWith(slideOutHorizontally(tween(160)) { -it / 3 * dir } + fadeOut(tween(120)))
                        .using(SizeTransform(clip = false))
                },
                label = "compact_month"
            ) { total ->
                Text(
                    text = "${CurrencyFormatter.getMonthName(total % 12 + 1)} ${total / 12}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = liquid.textPrimary
                )
            }
            Box(
                modifier = Modifier
                    .clip(LiquidShapes.pill)
                    .background((if (pendingHint) liquid.accentPending else liquid.accent).copy(alpha = 0.16f))
                    .border(1.dp, (if (pendingHint) liquid.accentPending else liquid.accent).copy(alpha = 0.35f), LiquidShapes.pill)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = when {
                        pendingHint -> "Pendências"
                        isCurrent -> "Atual"
                        else -> "Ir para hoje"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (pendingHint) liquid.accentPending else liquid.accent
                )
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(liquid.circleButtonBg)
                .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                .clickable { onNextMonth() }
                .testTag("next_month_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Próximo mês",
                tint = liquid.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
