package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.ui.components.AddCardExpenseDialog
import com.example.ui.components.AddSharedBillDialog
import com.example.ui.components.AiExpenseConfirmationDialog
import com.example.ui.components.BackupSettingsDialog
import com.example.ui.components.CardPurchasesProgressSection
import com.example.ui.components.DeleteExpenseDialog
import com.example.ui.components.EditExpenseDialog
import com.example.ui.components.EditSharedBillDialog
import com.example.ui.components.ExpenseListFilter
import com.example.ui.components.ExpenseSearchFilterBar
import com.example.ui.components.HomeTopBar
import com.example.ui.components.ManagePeopleDialog
import com.example.ui.components.MonthlySummarySection
import com.example.ui.components.MyShareItemCard
import com.example.ui.components.PeopleDebtsEmptyState
import com.example.ui.components.PeopleDebtsHeader
import com.example.ui.components.PersonDebtCard
import com.example.ui.components.QuickAiEntryBottomSheet
import com.example.ui.components.SpeedDialFab
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val liquid = LocalLiquidGlassColors.current

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val allPeople by viewModel.allPeople.collectAsStateWithLifecycle()
    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val personGroups by viewModel.personMonthlyGroups.collectAsStateWithLifecycle()
    val cardPurchases by viewModel.cardPurchasesProgress.collectAsStateWithLifecycle()
    val monthlyInstallments by viewModel.monthlyInstallments.collectAsStateWithLifecycle()
    val homeExpandedCardStates by viewModel.homeExpandedCardStates.collectAsStateWithLifecycle()

    val isAiBottomSheetVisible by viewModel.isAiBottomSheetVisible.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiError by viewModel.aiError.collectAsStateWithLifecycle()
    val pendingAiExpense by viewModel.pendingAiExpense.collectAsStateWithLifecycle()
    val showAiConfirmationDialog by viewModel.showAiConfirmationDialog.collectAsStateWithLifecycle()

    val isBackupDialogVisible by viewModel.isBackupDialogVisible.collectAsStateWithLifecycle()
    val backupUiState by viewModel.backupUiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupToUri(context.contentResolver, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.readAndValidateBackupUri(context.contentResolver, uri)
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddCardExpenseDialog by remember { mutableStateOf(false) }
    var showAddSharedBillDialog by remember { mutableStateOf(false) }
    var showManagePeopleDialog by remember { mutableStateOf(false) }
    var isDebtsDetailScreenVisible by remember { mutableStateOf(false) }
    var expenseSearchQuery by remember { mutableStateOf("") }
    var expenseListFilter by remember { mutableStateOf(ExpenseListFilter.ALL) }
    var itemToEdit by remember { mutableStateOf<InstallmentItem?>(null) }
    var itemToDelete by remember { mutableStateOf<InstallmentItem?>(null) }

    val listStateTab0 = rememberLazyListState()
    val listStateTab1 = rememberLazyListState()
    val activeListState = if (selectedTabIndex == 0) listStateTab0 else listStateTab1

    val myShareItems = monthlyInstallments.filter { it.isMyShare }
    val normalizedSearchQuery = expenseSearchQuery.trim()
    val filteredPersonGroups = personGroups.filter { group ->
        val matchesSearch = normalizedSearchQuery.isBlank() ||
            group.personName.contains(normalizedSearchQuery, ignoreCase = true) ||
            group.items.any {
                it.expenseTitle.contains(normalizedSearchQuery, ignoreCase = true)
            }
        val matchesFilter = when (expenseListFilter) {
            ExpenseListFilter.ALL -> true
            ExpenseListFilter.PENDING -> group.pendingAmount > 0.0
            ExpenseListFilter.PAID -> group.pendingAmount <= 0.0
        }
        matchesSearch && matchesFilter
    }
    val filteredMyShareItems = myShareItems.filter { item ->
        val matchesSearch = normalizedSearchQuery.isBlank() ||
            item.expenseTitle.contains(normalizedSearchQuery, ignoreCase = true)
        val matchesFilter = when (expenseListFilter) {
            ExpenseListFilter.ALL -> true
            ExpenseListFilter.PENDING -> !item.isPaid
            ExpenseListFilter.PAID -> item.isPaid
        }
        matchesSearch && matchesFilter
    }
    val filteredCardPurchases = cardPurchases.filter { purchase ->
        val matchesSearch = normalizedSearchQuery.isBlank() ||
            purchase.expense.title.contains(normalizedSearchQuery, ignoreCase = true) ||
            purchase.personName.contains(normalizedSearchQuery, ignoreCase = true)
        val matchesFilter = when (expenseListFilter) {
            ExpenseListFilter.ALL -> true
            ExpenseListFilter.PENDING -> purchase.remainingInstallmentsCount > 0
            ExpenseListFilter.PAID -> purchase.remainingInstallmentsCount == 0
        }
        matchesSearch && matchesFilter
    }

    if (isDebtsDetailScreenVisible) {
        DebtsDetailScreen(
            viewModel = viewModel,
            onBack = { isDebtsDetailScreenVisible = false },
            onEditItem = { itemToEdit = it },
            onDeleteItem = { itemToDelete = it }
        )
    } else {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            containerColor = Color.Transparent,
        topBar = {
            HomeTopBar(
                selectedTabIndex = selectedTabIndex,
                isDarkMode = isDarkMode,
                onManagePeopleClick = { showManagePeopleDialog = true },
                onAiQuickClick = { viewModel.openAiBottomSheet() },
                onToggleThemeClick = { viewModel.toggleDarkMode() },
                onBackupClick = { viewModel.openBackupDialog() },
                onTabSelected = { selectedTabIndex = it }
            )
    ) { innerPadding ->
        // Fluid Liquid Gradient canvas background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(liquid.backgroundGradient)
                .padding(innerPadding)
        ) {
            if (selectedTabIndex == 0) {
                // Tab 0: Fatura do Mês
                LazyColumn(
                    state = listStateTab0,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ExpenseSearchFilterBar(
                            query = expenseSearchQuery,
                            onQueryChange = { expenseSearchQuery = it },
                            selectedFilter = expenseListFilter,
                            onFilterChange = { expenseListFilter = it }
                        )
                    }

                    // Monthly Hero Summary Section (Month selector & Summary Card)
                    item {
                        MonthlySummarySection(
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            summary = summary,
                            isPrivacyMode = isPrivacyMode,
                            onMonthSelected = { y, m -> viewModel.selectMonth(y, m) },
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onTogglePrivacy = { viewModel.togglePrivacyMode() },
                            onAddCardPurchase = { showAddCardExpenseDialog = true },
                            onAddSharedBill = { showAddSharedBillDialog = true },
                            onOptionsClick = { showManagePeopleDialog = true },
                            onOthersDebtClick = { isDebtsDetailScreenVisible = true }
                        )
                    }

                    // Section Title: O que os outros devem neste mês
                    item {
                        PeopleDebtsHeader(
                            onViewDetailsClick = { isDebtsDetailScreenVisible = true }
                        )
                    }

                    if (filteredPersonGroups.isEmpty()) {
                        item {
                            PeopleDebtsEmptyState(
                                message = if (personGroups.isEmpty()) {
                                    "Nenhuma compra ou conta de terceiros para este mês."
                                } else {
                                    "Nenhum resultado encontrado para esta pesquisa."
                                }
                            )
                        }
                    } else {
                        items(filteredPersonGroups, key = { "person_group_${it.personId}" }) { group ->
                            val isExpanded = homeExpandedCardStates[group.personId] ?: true
                            PersonDebtCard(
                                group = group,
                                selectedMonth = selectedMonth,
                                selectedYear = selectedYear,
                                isPrivacyMode = isPrivacyMode,
                                isExpanded = isExpanded,
                                onToggleExpanded = { viewModel.toggleHomePersonExpanded(group.personId) },
                                onTogglePayment = { item -> viewModel.togglePayment(item) },
                                onEditItem = { item -> itemToEdit = item },
                                onDeleteItem = { item -> itemToDelete = item }
                            )
                        }
                    }

                    // Section: Minha Parte (Contas divididas que eu pago)
                    if (filteredMyShareItems.isNotEmpty()) {
                        item(key = "header_my_share") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Minha Parte nas Contas Divididas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = liquid.textPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        items(filteredMyShareItems, key = { "my_share_${it.id}" }) { item ->
                            MyShareItemCard(
                                item = item,
                                isPrivacyMode = isPrivacyMode,
                                onEdit = { itemToEdit = it },
                                onDelete = { itemToDelete = it }
                            )
                        }
                    }
                }
            } else {
                // Tab 1: Compras no Cartão (Parcelas Restantes)
                LazyColumn(
                    state = listStateTab1,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ExpenseSearchFilterBar(
                            query = expenseSearchQuery,
                            onQueryChange = { expenseSearchQuery = it },
                            selectedFilter = expenseListFilter,
                            onFilterChange = { expenseListFilter = it }
                        )
                    }

                    item {
                        CardPurchasesProgressSection(
                            purchases = filteredCardPurchases,
                            isPrivacyMode = isPrivacyMode,
                            onDeleteExpense = { id -> viewModel.deleteExpense(id) },
                            emptyMessage = if (cardPurchases.isEmpty()) {
                                "Nenhuma compra parcelada cadastrada"
                            } else {
                                "Nenhum resultado encontrado para esta pesquisa"
                            }
                        )
                    }
                }
            }

            // Expandable Speed Dial FAB with Auto-Hide and Darkened Backdrop
            SpeedDialFab(
                listState = activeListState,
                onAiEntryClick = { viewModel.openAiBottomSheet() },
                onSharedBillClick = { showAddSharedBillDialog = true },
                onCardExpenseClick = { showAddCardExpenseDialog = true }
            )

        }
    }
    }

    // Dialog: Nova Compra no Cartão
    if (showAddCardExpenseDialog) {
        AddCardExpenseDialog(
            people = allPeople,
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            onDismiss = { showAddCardExpenseDialog = false },
            onAddPerson = { showManagePeopleDialog = true },
            onConfirm = { title, totalAmount, person, installments, startYear, startMonth, notes, markPastAsPaid ->
                viewModel.addCardPurchase(
                    title = title,
                    totalAmount = totalAmount,
                    person = person,
                    totalInstallments = installments,
                    startYear = startYear,
                    startMonth = startMonth,
                    notes = notes,
                    markPastInstallmentsAsPaid = markPastAsPaid
                )
                showAddCardExpenseDialog = false
            }
        )
    }

    // Dialog: Nova Conta Dividida
    if (showAddSharedBillDialog) {
        AddSharedBillDialog(
            people = allPeople,
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            onDismiss = { showAddSharedBillDialog = false },
            onAddPerson = { showManagePeopleDialog = true },
            onConfirm = { title, totalAmount, participants, startYear, startMonth, isRecurring, count, notes ->
                viewModel.addSharedBillWithParticipants(
                    title = title,
                    totalAmount = totalAmount,
                    participants = participants,
                    startYear = startYear,
                    startMonth = startMonth,
                    isRecurring = isRecurring,
                    recurrenceMonthsCount = count,
                    notes = notes
                )
                showAddSharedBillDialog = false
            }
        )
    }

    // Dialog: Editar Lançamento (Conta Dividida ou Compra no Cartão)
    if (itemToEdit != null) {
        val editingItem = itemToEdit!!
        if (editingItem.expenseType == ExpenseType.SHARED_BILL || editingItem.isMyShare) {
            EditSharedBillDialog(
                item = editingItem,
                people = allPeople,
                selectedYear = editingItem.year,
                selectedMonth = editingItem.month,
                onLoadExpense = { id -> viewModel.getExpense(id) },
                onDismiss = { itemToEdit = null },
                onConfirm = { expenseId, newTitle, newTotalAmount, newPerson, newMyShare, newOtherShare, updateAllMonths ->
                    viewModel.updateSharedBill(
                        expenseId = expenseId,
                        newTitle = newTitle,
                        newTotalAmount = newTotalAmount,
                        newPerson = newPerson,
                        newMyShareAmount = newMyShare,
                        newOtherShareAmount = newOtherShare,
                        updateAllMonths = updateAllMonths,
                        specificYear = editingItem.year,
                        specificMonth = editingItem.month
                    )
                    itemToEdit = null
                }
            )
        } else {
            EditExpenseDialog(
                item = editingItem,
                people = allPeople,
                onDismiss = { itemToEdit = null },
                onConfirm = { expenseId, newTitle, newTotalAmount, newPerson, newTotalInstallments, startYear, startMonth ->
                    viewModel.updateExpense(
                        expenseId = expenseId,
                        newTitle = newTitle,
                        newTotalAmount = newTotalAmount,
                        newPerson = newPerson,
                        newTotalInstallments = newTotalInstallments,
                        startYear = startYear,
                        startMonth = startMonth
                    )
                    itemToEdit = null
                }
            )
        }
    }

    // Dialog: Excluir Lançamento
    if (itemToDelete != null) {
        DeleteExpenseDialog(
            item = itemToDelete!!,
            isPrivacyMode = isPrivacyMode,
            onDismiss = { itemToDelete = null },
            onDeleteSingleInstallment = {
                viewModel.deleteSingleInstallment(itemToDelete!!.id)
                itemToDelete = null
            },
            onDeleteAllInstallments = {
                viewModel.deleteExpense(itemToDelete!!.expenseId)
                itemToDelete = null
            }
        )
    }

    // Dialog: Gerenciar Pessoas
    if (showManagePeopleDialog) {
        ManagePeopleDialog(
            people = allPeople,
            onDismiss = { showManagePeopleDialog = false },
            onAddPerson = { name, phone, colorHex ->
                viewModel.addPerson(name, phone, colorHex)
            },
            onDeletePerson = { person ->
                viewModel.deletePerson(person)
            }
        )
    }

    // BottomSheet: Entrada Rápida via IA
    QuickAiEntryBottomSheet(
        isVisible = isAiBottomSheetVisible,
        isLoading = isAiLoading,
        errorMessage = aiError,
        onDismiss = { viewModel.closeAiBottomSheet() },
        onSubmitPrompt = { prompt ->
            viewModel.processAiPrompt(prompt)
        }
    )

    // Dialog: Confirmação da Despesa da IA
    if (showAiConfirmationDialog && pendingAiExpense != null) {
        AiExpenseConfirmationDialog(
            initialData = pendingAiExpense!!,
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            onDismiss = { viewModel.dismissAiConfirmationDialog() },
            onConfirm = { description, totalAmount, installments, targetPerson, startYear, startMonth, markPastAsPaid ->
                viewModel.confirmAndSaveAiExpense(
                    description = description,
                    totalAmount = totalAmount,
                    installments = installments,
                    personName = targetPerson,
                    startYear = startYear,
                    startMonth = startMonth,
                    markPastAsPaid = markPastAsPaid
                )
            }
        )
    }

    // Dialog: Backup e Segurança dos Dados
    if (isBackupDialogVisible) {
        BackupSettingsDialog(
            uiState = backupUiState,
            onDismiss = { viewModel.closeBackupDialog() },
            onExportClick = {
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                val suggestedFileName = "quem-me-deve-backup-$dateStr.json"
                exportLauncher.launch(suggestedFileName)
            },
            onImportClick = {
                importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
            },
            onConfirmRestore = { viewModel.confirmRestoreBackup() },
            onDismissConfirmation = { viewModel.dismissBackupConfirmation() },
            onResetStatus = { viewModel.resetBackupStatus() }
        )
    }
}
