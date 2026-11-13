package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.MonthlySummary

/**
 * Section combining the month selector and the hero summary card for the active billing cycle.
 *
 * Usa o seletor de mês compacto (pílula "‹ mês ›"). Na Home, a privacidade fica no
 * cabeçalho e as ações de adicionar ficam no botão +, por isso o card pode omiti-las.
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
    modifier: Modifier = Modifier,
    showPrivacyToggle: Boolean = true,
    showQuickActions: Boolean = true,
    pendingMonthHint: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CompactMonthSelector(
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            onMonthSelected = onMonthSelected,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            pendingHint = pendingMonthHint
        )

        SummaryCard(
            summary = summary,
            isPrivacyMode = isPrivacyMode,
            onTogglePrivacy = onTogglePrivacy,
            onAddCardPurchase = onAddCardPurchase,
            onAddSharedBill = onAddSharedBill,
            onOptionsClick = onOptionsClick,
            onOthersDebtClick = onOthersDebtClick,
            showPrivacyToggle = showPrivacyToggle,
            showQuickActions = showQuickActions
        )
    }
}
