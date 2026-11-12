package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.util.DebtMessageFormatter
import com.example.util.ShareIntentHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.ui.components.MonthlyHistorySection
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.PersonMonthlyGroup
import com.example.util.CurrencyFormatter

enum class DebtScopeFilter {
    SELECTED_MONTH,
    ALL_TIME
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DebtsDetailScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onEditItem: (InstallmentItem) -> Unit,
    onDeleteItem: (InstallmentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val liquid = LocalLiquidGlassColors.current
    val context = LocalContext.current

    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsStateWithLifecycle()

    val monthlyGroups by viewModel.personMonthlyGroups.collectAsStateWithLifecycle()
    val allTimeGroups by viewModel.personAllTimeGroups.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val monthlyHistory by viewModel.monthlyHistory.collectAsStateWithLifecycle()

    val expenseNotesMap = remember(allExpenses) {
        allExpenses.associate { it.id to it.notes }
    }

    var currentFilter by remember { mutableStateOf(DebtScopeFilter.SELECTED_MONTH) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }
    var pendingPrivacyShareGroup by remember { mutableStateOf<PersonMonthlyGroup?>(null) }

    val performShare: (PersonMonthlyGroup) -> Unit = { group ->
        val scope = if (currentFilter == DebtScopeFilter.SELECTED_MONTH) {
            DebtMessageFormatter.DebtScope.SELECTED_MONTH
        } else {
            DebtMessageFormatter.DebtScope.ALL_TIME
        }
        val message = DebtMessageFormatter.format(
            group = group,
            scope = scope,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            notesByExpenseId = expenseNotesMap
        )
        ShareIntentHelper.sharePlainText(
            context = context,
            text = message,
            chooserTitle = "Cobrar ${group.personName}"
        )
    }

    val onTriggerShare: (PersonMonthlyGroup) -> Unit = { group ->
        if (isPrivacyMode) {
            pendingPrivacyShareGroup = group
        } else {
            performShare(group)
        }
    }

    val rawGroups = if (currentFilter == DebtScopeFilter.SELECTED_MONTH) monthlyGroups else allTimeGroups

    val filteredGroups = remember(rawGroups, searchQuery) {
        if (searchQuery.isBlank()) {
            rawGroups
        } else {
            val q = searchQuery.trim().lowercase()
            rawGroups.mapNotNull { group ->
                val nameMatches = group.personName.lowercase().contains(q)
                val matchingItems = group.items.filter { it.expenseTitle.lowercase().contains(q) }
                if (nameMatches) {
                    group
                } else if (matchingItems.isNotEmpty()) {
                    group.copy(items = matchingItems)
                } else {
                    null
                }
            }
        }
    }

    val totalOwed = rawGroups.sumOf { it.totalAmount }
    val totalPending = rawGroups.sumOf { it.pendingAmount }
    val totalPaid = rawGroups.sumOf { it.paidAmount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(liquid.backgroundGradient)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(liquid.circleButtonBg)
                            .border(1.dp, liquid.cardBorderSubtle, CircleShape)
                            .clickable { onBack() }
                            .testTag("debts_detail_back_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = liquid.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Detalhamento de Dívidas",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = liquid.textPrimary
                        )
                        Text(
                            text = "Quem deve e o que deve",
                            style = MaterialTheme.typography.labelSmall,
                            color = liquid.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { showSearchField = !showSearchField },
                    modifier = Modifier.testTag("toggle_search_debts")
                ) {
                    Icon(
                        imageVector = if (showSearchField) Icons.Default.Clear else Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = liquid.textSecondary
                    )
                }
            }

            // Search Bar (expandable)
            AnimatedVisibility(visible = showSearchField) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por pessoa ou compra...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_debts_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = liquid.cardSurfaceVariant,
                        unfocusedContainerColor = liquid.cardSurfaceVariant,
                        focusedTextColor = liquid.textPrimary,
                        unfocusedTextColor = liquid.textPrimary
                    ),
                    singleLine = true
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Scope Filter Tabs
                item(key = "scope_filter_chips") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = currentFilter == DebtScopeFilter.SELECTED_MONTH,
                            onClick = { currentFilter = DebtScopeFilter.SELECTED_MONTH },
                            label = {
                                Text(
                                    text = "Mês: ${CurrencyFormatter.getMonthShortName(selectedMonth)}/$selectedYear"
                                )
                            },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = liquid.pillPrimaryBg,
                                selectedLabelColor = liquid.pillPrimaryText
                            ),
                            modifier = Modifier.testTag("filter_selected_month")
                        )

                        if (currentFilter == DebtScopeFilter.SELECTED_MONTH) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.previousMonth() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Mês anterior",
                                        tint = liquid.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.nextMonth() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Próximo mês",
                                        tint = liquid.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        FilterChip(
                            selected = currentFilter == DebtScopeFilter.ALL_TIME,
                            onClick = { currentFilter = DebtScopeFilter.ALL_TIME },
                            label = { Text("Geral (Tudo que Devem)") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = liquid.pillPrimaryBg,
                                selectedLabelColor = liquid.pillPrimaryText
                            ),
                            modifier = Modifier.testTag("filter_all_time")
                        )
                    }
                }

                // Overview Hero Glass Card
                item(key = "overview_hero_card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = liquid.cardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (currentFilter == DebtScopeFilter.SELECTED_MONTH) {
                                    "TOTAL QUE DEVEM A VOCÊ NESTE MÊS"
                                } else {
                                    "TOTAL GERAL QUE DEVEM A VOCÊ"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 11.sp
                                ),
                                color = liquid.textSecondary
                            )

                            val displayOwed = CurrencyFormatter.format(totalOwed, isPrivacyMode)
                            Text(
                                text = displayOwed,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = liquid.textPrimary
                            )

                            // Status metrics row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Pending box
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = liquid.cardSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Pendente",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = liquid.textSecondary
                                        )
                                        val displayPending = CurrencyFormatter.format(totalPending, isPrivacyMode)
                                        Text(
                                            text = displayPending,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (totalPending > 0) liquid.accentPending else liquid.accentPaid
                                        )
                                    }
                                }

                                // Paid box
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = liquid.cardSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Já Quitado",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = liquid.textSecondary
                                        )
                                        val displayPaid = CurrencyFormatter.format(totalPaid, isPrivacyMode)
                                        Text(
                                            text = displayPaid,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = liquid.accentPaid
                                        )
                                    }
                                }
                            }

                            // Progress bar
                            if (totalOwed > 0) {
                                val progress = (totalPaid / totalOwed).toFloat().coerceIn(0f, 1f)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = liquid.accentPaid,
                                        trackColor = liquid.cardBorderSubtle
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Recebido: ${(progress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = liquid.textSecondary
                                        )
                                        Text(
                                            text = "${filteredGroups.size} pessoas",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = liquid.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "monthly_history") {
                    MonthlyHistorySection(
                        history = monthlyHistory,
                        isPrivacyMode = isPrivacyMode
                    )
                }

                // Section Label
                item(key = "section_people_header") {
                    Text(
                        text = "Detalhamento por Pessoa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = liquid.textPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Empty State
                if (filteredGroups.isEmpty()) {
                    item(key = "empty_debts_state") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = liquid.cardSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(liquid.accentPaid.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = liquid.accentPaid,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = "Nenhuma dívida encontrada!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = liquid.textPrimary
                                )
                                Text(
                                    text = if (searchQuery.isNotBlank()) {
                                        "Nenhum resultado para \"$searchQuery\"."
                                    } else {
                                        "Não há pendências de outros cadastradas para este período."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = liquid.textSecondary
                                )
                            }
                        }
                    }
                }

                // List of Person Debt Cards
                items(filteredGroups, key = { "person_debt_group_${it.personId}_${it.personName}" }) { personGroup ->
                    PersonDebtDetailCard(
                        group = personGroup,
                        isPrivacyMode = isPrivacyMode,
                        onTogglePayment = { item ->
                            viewModel.togglePayment(item)
                        },
                        onEditItem = onEditItem,
                        onDeleteItem = onDeleteItem,
                        onShareDebt = { onTriggerShare(personGroup) },
                        onShareWhatsApp = { onTriggerShare(personGroup) }
                    )
                }

                item(key = "bottom_space") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    pendingPrivacyShareGroup?.let { targetGroup ->
        AlertDialog(
            onDismissRequest = { pendingPrivacyShareGroup = null },
            title = {
                Text(
                    text = "Modo Privacidade Ativo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = liquid.textPrimary
                )
            },
            text = {
                Text(
                    text = "O texto a ser compartilhado conterá os valores financeiros reais (não mascarados) das pendências de ${targetGroup.personName}. Deseja continuar com o compartilhamento?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = liquid.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val groupToShare = targetGroup
                        pendingPrivacyShareGroup = null
                        performShare(groupToShare)
                    },
                    modifier = Modifier.testTag("confirm_privacy_share_btn")
                ) {
                    Text(
                        text = "Continuar e Compartilhar",
                        fontWeight = FontWeight.Bold,
                        color = liquid.accentPaid
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingPrivacyShareGroup = null },
                    modifier = Modifier.testTag("cancel_privacy_share_btn")
                ) {
                    Text(
                        text = "Cancelar",
                        color = liquid.textSecondary
                    )
                }
            },
            containerColor = liquid.cardSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
    }
}

@Composable
fun PersonDebtDetailCard(
    group: PersonMonthlyGroup,
    isPrivacyMode: Boolean,
    onTogglePayment: (InstallmentItem) -> Unit,
    onEditItem: (InstallmentItem) -> Unit,
    onDeleteItem: (InstallmentItem) -> Unit,
    onShareDebt: () -> Unit,
    onShareWhatsApp: () -> Unit = onShareDebt,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    var isExpanded by remember { mutableStateOf(true) }

    val personColor = try {
        Color(android.graphics.Color.parseColor(group.colorHex))
    } catch (_: Exception) {
        liquid.pillPrimaryBg
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("person_debt_card_${group.personId}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = liquid.cardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Person Header: Avatar + Name + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(personColor.copy(alpha = 0.2f))
                            .border(1.5.dp, personColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = group.personName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = personColor
                        )
                    }

                    Column {
                        Text(
                            text = group.personName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = liquid.textPrimary
                        )
                        val pendingText = if (group.pendingAmount <= 0) {
                            "Tudo pago 🎉"
                        } else {
                            "Pendente: ${CurrencyFormatter.format(group.pendingAmount, isPrivacyMode)}"
                        }
                        Text(
                            text = pendingText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (group.pendingAmount <= 0) liquid.accentPaid else liquid.accentPending
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // System Share Button
                    Surface(
                        shape = CircleShape,
                        color = liquid.circleButtonBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onShareDebt() }
                            .testTag("share_debt_button_${group.personId}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartilhar cobrança",
                                tint = liquid.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // WhatsApp Share Button (preserved testTag and contentDescription)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF25D366).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onShareWhatsApp() }
                            .testTag("share_whatsapp_${group.personId}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Cobrar no WhatsApp",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Cobrar",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF25D366)
                            )
                        }
                    }

                    // Expand / Collapse
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "▲" else "▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = liquid.textSecondary
                        )
                    }
                }
            }

            // Quick totals row for this person
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = liquid.cardSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Total", style = MaterialTheme.typography.labelSmall, color = liquid.textSecondary)
                        val disp = CurrencyFormatter.format(group.totalAmount, isPrivacyMode)
                        Text(disp, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = liquid.textPrimary)
                    }
                }

                // Pendente
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = liquid.cardSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Pendente", style = MaterialTheme.typography.labelSmall, color = liquid.textSecondary)
                        val disp = CurrencyFormatter.format(group.pendingAmount, isPrivacyMode)
                        Text(
                            disp,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (group.pendingAmount > 0) liquid.accentPending else liquid.accentPaid
                        )
                    }
                }

                // Pago
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = liquid.cardSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Pago", style = MaterialTheme.typography.labelSmall, color = liquid.textSecondary)
                        val disp = CurrencyFormatter.format(group.paidAmount, isPrivacyMode)
                        Text(disp, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = liquid.accentPaid)
                    }
                }
            }

            // Detailed items list: "O que ela deve"
            if (isExpanded) {
                HorizontalDivider(color = liquid.cardBorderSubtle)

                Text(
                    text = "O QUE ELA DEVE (${group.items.size} ${if (group.items.size == 1) "item" else "itens"}):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontSize = 10.sp
                    ),
                    color = liquid.textSecondary
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    group.items.forEach { item ->
                        DebtItemRow(
                            item = item,
                            isPrivacyMode = isPrivacyMode,
                            onTogglePayment = { onTogglePayment(item) },
                            onEdit = { onEditItem(item) },
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebtItemRow(
    item: InstallmentItem,
    isPrivacyMode: Boolean,
    onTogglePayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("debt_item_row_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        color = liquid.cardSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, liquid.cardBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(liquid.circleButtonBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.expenseType == ExpenseType.CARD_PURCHASE) {
                            Icons.Default.CreditCard
                        } else {
                            Icons.Default.Splitscreen
                        },
                        contentDescription = null,
                        tint = liquid.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = item.expenseTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = liquid.textPrimary
                    )

                    val subText = if (item.expenseType == ExpenseType.CARD_PURCHASE) {
                        "Parcela ${item.installmentNumber}/${item.totalInstallments} • ${CurrencyFormatter.getMonthShortName(item.month)}/${item.year}"
                    } else {
                        "Conta Dividida • ${CurrencyFormatter.getMonthShortName(item.month)}/${item.year}"
                    }
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.labelSmall,
                        color = liquid.textMuted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val displayAmount = CurrencyFormatter.format(item.amount, isPrivacyMode)
                Text(
                    text = displayAmount,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = liquid.textPrimary
                )

                // Interactive Status Badge: Pendente vs Pago
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (item.isPaid) liquid.accentPaid.copy(alpha = 0.15f) else liquid.accentPending.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.isPaid) liquid.accentPaid.copy(alpha = 0.4f) else liquid.accentPending.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .clickable { onTogglePayment() }
                        .testTag("toggle_payment_btn_${item.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isPaid) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (item.isPaid) liquid.accentPaid else liquid.accentPending
                        )
                        Text(
                            text = if (item.isPaid) "Pago" else "Pendente",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (item.isPaid) liquid.accentPaid else liquid.accentPending
                        )
                    }
                }

                // 3-dots Menu for Edit / Delete
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            tint = liquid.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(liquid.cardSurface)
                            .border(1.dp, liquid.cardBorderSubtle, RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar", color = liquid.textPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = liquid.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
