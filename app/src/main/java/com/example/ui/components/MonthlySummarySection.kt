package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.MonthlySummary

/**
 * Section combining the month selector and the hero summary card for the active billing cycle.
 */
@Composable
fun MonthlySummarySection(
    selectedYear: Int,
    selectedMonth: Int,
    summary: MonthlySummary,
    isPrivacyMode: Boolean,
    onMonthSelected: (year: Int, month: Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onAddCardPurchase: () -> Unit,
    onAddSharedBill: () -> Unit,
    onOptionsClick: () -> Unit,
    onOthersDebtClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MonthSelector(
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            onMonthSelected = onMonthSelected,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )

        SummaryCard(
            summary = summary,
            isPrivacyMode = isPrivacyMode,
            onTogglePrivacy = onTogglePrivacy,
            onAddCardPurchase = onAddCardPurchase,
            onAddSharedBill = onAddSharedBill,
            onOptionsClick = onOptionsClick,
            onOthersDebtClick = onOthersDebtClick
        )
    }
}
