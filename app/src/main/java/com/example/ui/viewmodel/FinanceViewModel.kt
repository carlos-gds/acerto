package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BillParticipantShare
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.Person
import com.example.data.repository.FinanceRepository
import com.example.util.Money
import android.net.Uri
import com.example.data.backup.BackupPayload
import com.example.data.backup.BackupRepository
import com.example.data.backup.BackupSerializer
import com.example.data.backup.BackupValidationResult
import com.example.data.backup.BackupValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthlySummary(
    val totalInvoice: Double = 0.0,
    val myShare: Double = 0.0,
    val othersTotal: Double = 0.0,
    val othersPaid: Double = 0.0,
    val othersPending: Double = 0.0
)

data class PersonMonthlyGroup(
    val personId: Long,
    val personName: String,
    val colorHex: String,
    val phone: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val items: List<InstallmentItem>,
    /** Avatar da pessoa (nome do enum PersonAvatar). */
    val avatar: String = com.example.data.model.PersonAvatar.NEUTRAL.name
)

data class CardPurchaseProgress(
    val expense: Expense,
    val personName: String,
    val personColor: String,
    val paidInstallmentsCount: Int,
    val totalInstallmentsCount: Int,
    val remainingInstallmentsCount: Int,
    val installmentAmount: Double,
    val remainingAmount: Double,
    val progress: Float,
    /** Compra dividida: com quem mais ela é dividida (vazio = só desta pessoa). */
    val groupWith: List<String> = emptyList(),
    /** Compra dividida: valor da compra inteira (soma das partes). */
    val groupTotal: Double? = null
)

data class MonthlyHistorySummary(
    val year: Int,
    val month: Int,
    val totalAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val installmentCount: Int
)

sealed class BackupUiState {
    data object Idle : BackupUiState()
    data object Exporting : BackupUiState()
    data object Importing : BackupUiState()
    data class ConfirmingRestore(val payload: BackupPayload, val summary: String) : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    private val backupRepository: BackupRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db.personDao(), db.expenseDao(), db.installmentDao(), db)
        backupRepository = BackupRepository(db)
    }

    private val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH) + 1

    private val _selectedYear = MutableStateFlow(currentYear)
    val selectedYear: StateFlow<Int> = _selectedYear

    private val _selectedMonth = MutableStateFlow(currentMonth)
    val selectedMonth: StateFlow<Int> = _selectedMonth

    /**
     * Seleção automática do mês: enquanto o usuário não troca de mês manualmente,
     * o app fica no mês pendente mais antigo (ou no atual, se tudo estiver quitado)
     * e avança sozinho quando as pendências desse mês são quitadas.
     */
    private var autoMonthEnabled = true

    /** true quando o mês exibido foi escolhido automaticamente por haver pendências antigas. */
    private val _isShowingPendingMonth = MutableStateFlow(false)
    val isShowingPendingMonth: StateFlow<Boolean> = _isShowingPendingMonth.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllInstallments().collect { items ->
                if (!autoMonthEnabled) return@collect
                val (year, month) = com.example.util.MonthSelectionPolicy.autoMonth(items, currentYear, currentMonth)
                _selectedYear.value = year
                _selectedMonth.value = month
                _isShowingPendingMonth.value = year != currentYear || month != currentMonth
            }
        }
    }

    private fun disableAutoMonth() {
        autoMonthEnabled = false
        _isShowingPendingMonth.value = false
    }

    val allPeople: StateFlow<List<Person>> = repository.allPeople
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInstallments: StateFlow<List<InstallmentItem>> = repository.getAllInstallments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyHistory: StateFlow<List<MonthlyHistorySummary>> = allInstallments
        .map { items ->
            items.groupBy { it.year to it.month }
                .map { (period, periodItems) ->
                    val total = periodItems.sumOf { Money.toReal(it.amount) }
                    val paid = periodItems.filter { it.isPaid }.sumOf { Money.toReal(it.amount) }
                    MonthlyHistorySummary(
                        year = period.first,
                        month = period.second,
                        totalAmount = total,
                        paidAmount = paid,
                        pendingAmount = total - paid,
                        installmentCount = periodItems.size
                    )
                }
                .sortedWith(compareByDescending<MonthlyHistorySummary> { it.year }.thenByDescending { it.month })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Installments for the selected month and year
    val monthlyInstallments: StateFlow<List<InstallmentItem>> = combine(
        _selectedYear,
        _selectedMonth
    ) { year, month -> Pair(year, month) }
        .flatMapLatest { (year, month) ->
            repository.getInstallmentsForMonth(year, month)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary for the selected month
    val monthlySummary: StateFlow<MonthlySummary> = monthlyInstallments.combine(allPeople) { items, _ ->
        var totalInvoice = 0.0
        var myShare = 0.0
        var othersTotal = 0.0
        var othersPaid = 0.0
        var othersPending = 0.0

        for (item in items) {
            totalInvoice += Money.toReal(item.amount)
            if (item.isMyShare) {
                myShare += Money.toReal(item.amount)
            } else {
                othersTotal += Money.toReal(item.amount)
                if (item.isPaid) {
                    othersPaid += Money.toReal(item.amount)
                } else {
                    othersPending += Money.toReal(item.amount)
                }
            }
        }

        MonthlySummary(
            totalInvoice = totalInvoice,
            myShare = myShare,
            othersTotal = othersTotal,
            othersPaid = othersPaid,
            othersPending = othersPending
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlySummary())

    // Grouped by person for the selected month
    val personMonthlyGroups: StateFlow<List<PersonMonthlyGroup>> = combine(
        monthlyInstallments,
        allPeople
    ) { items, people ->
        val peopleMap = people.associateBy { it.id }

        // Filter out user's own share items for the debtor groups
        val debtorItems = items.filter { !it.isMyShare }

        val grouped = debtorItems.groupBy { it.personId }

        grouped.map { (personId, personItems) ->
            val person = peopleMap[personId]
            val total = personItems.sumOf { Money.toReal(it.amount) }
            val paid = personItems.filter { it.isPaid }.sumOf { Money.toReal(it.amount) }
            val pending = total - paid

            PersonMonthlyGroup(
                personId = personId,
                personName = person?.name ?: personItems.firstOrNull()?.personName ?: "Desconhecido",
                colorHex = person?.colorHex ?: "#10B981",
                phone = person?.phone ?: "",
                totalAmount = total,
                paidAmount = paid,
                pendingAmount = pending,
                avatar = person?.avatar ?: com.example.data.model.PersonAvatar.NEUTRAL.name,
                items = personItems
            )
        }.sortedByDescending { it.pendingAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grouped by person across all installments (All-Time debts)
    val personAllTimeGroups: StateFlow<List<PersonMonthlyGroup>> = combine(
        allInstallments,
        allPeople
    ) { items, people ->
        val peopleMap = people.associateBy { it.id }
        val debtorItems = items.filter { !it.isMyShare }
        val grouped = debtorItems.groupBy { it.personId }

        grouped.map { (personId, personItems) ->
            val person = peopleMap[personId]
            val total = personItems.sumOf { Money.toReal(it.amount) }
            val paid = personItems.filter { it.isPaid }.sumOf { Money.toReal(it.amount) }
            val pending = total - paid

            PersonMonthlyGroup(
                personId = personId,
                personName = person?.name ?: personItems.firstOrNull()?.personName ?: "Desconhecido",
                colorHex = person?.colorHex ?: "#10B981",
                phone = person?.phone ?: "",
                totalAmount = total,
                paidAmount = paid,
                pendingAmount = pending,
                avatar = person?.avatar ?: com.example.data.model.PersonAvatar.NEUTRAL.name,
                items = personItems.sortedWith(compareBy({ it.year }, { it.month }, { it.installmentNumber }))
            )
        }.sortedByDescending { it.pendingAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ongoing card purchases tracking: total installments, remaining installments
    val cardPurchasesProgress: StateFlow<List<CardPurchaseProgress>> = combine(
        allExpenses,
        allInstallments,
        allPeople
    ) { expenses, installments, people ->
        val peopleMap = people.associateBy { it.id }
        val cardExpenses = expenses.filter { it.type == ExpenseType.CARD_PURCHASE }
        val groups = cardExpenses.filter { it.groupId != null }.groupBy { it.groupId }

        cardExpenses.mapNotNull { expense ->
            val expenseInstallments = installments.filter { it.expenseId == expense.id }
            if (expenseInstallments.isEmpty()) return@mapNotNull null

            // Uma parcela pode estar dividida em partes (pagamento parcial): conta por número
            val byNumber = expenseInstallments.groupBy { it.installmentNumber }
            val totalCount = byNumber.size
            val paidCount = byNumber.values.count { parts -> parts.all { it.isPaid } }
            val remainingCount = totalCount - paidCount
            val person = peopleMap[expense.personId]
            val installmentAmount = byNumber.values.firstOrNull()?.let { parts -> Money.toReal(parts.sumOf { it.amount }) }
                ?: (Money.toReal(expense.totalAmount) / totalCount)
            val remainingAmount = expenseInstallments.filter { !it.isPaid }.sumOf { Money.toReal(it.amount) }
            val progress = if (totalCount > 0) paidCount.toFloat() / totalCount.toFloat() else 1f

            CardPurchaseProgress(
                expense = expense,
                personName = person?.name ?: "Pessoa",
                personColor = person?.colorHex ?: "#3B82F6",
                paidInstallmentsCount = paidCount,
                totalInstallmentsCount = totalCount,
                remainingInstallmentsCount = remainingCount,
                installmentAmount = installmentAmount,
                remainingAmount = remainingAmount,
                progress = progress,
                groupWith = expense.groupId?.let { gid ->
                    groups[gid].orEmpty().filter { it.id != expense.id }.mapNotNull { peopleMap[it.personId]?.name }
                }.orEmpty(),
                groupTotal = expense.groupId?.let { gid ->
                    groups[gid]?.takeIf { it.size > 1 }?.sumOf { Money.toReal(it.totalAmount) }
                }
            )
        }.sortedBy { it.remainingInstallmentsCount == 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMonth(year: Int, month: Int) {
        disableAutoMonth() // troca manual: o app passa a respeitar a escolha do usuário
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun previousMonth() {
        disableAutoMonth() // troca manual: o app passa a respeitar a escolha do usuário
        var m = _selectedMonth.value - 1
        var y = _selectedYear.value
        if (m < 1) {
            m = 12
            y -= 1
        }
        _selectedMonth.value = m
        _selectedYear.value = y
    }

    fun nextMonth() {
        disableAutoMonth() // troca manual: o app passa a respeitar a escolha do usuário
        var m = _selectedMonth.value + 1
        var y = _selectedYear.value
        if (m > 12) {
            m = 1
            y += 1
        }
        _selectedMonth.value = m
        _selectedYear.value = y
    }

    fun togglePayment(item: InstallmentItem) {
        viewModelScope.launch {
            repository.setInstallmentPaid(item.id, !item.isPaid)
        }
    }

    /** "Receber": aplica o valor às parcelas pendentes da pessoa (mais antigas primeiro). */
    fun receivePayment(personId: Long, amount: Double) {
        viewModelScope.launch {
            repository.receivePayment(personId, com.example.util.Money.fromReal(amount))
        }
    }

    /** Registra que a pessoa pagou só [amount] reais desta parcela. */
    fun registerPartialPayment(item: InstallmentItem, amount: Double) {
        viewModelScope.launch {
            repository.registerPartialPayment(item.id, com.example.util.Money.fromReal(amount))
        }
    }

    fun addPerson(
        name: String,
        phone: String = "",
        colorHex: String = "#10B981",
        avatar: String = com.example.data.model.PersonAvatar.NEUTRAL.name
    ) {
        viewModelScope.launch {
            repository.insertPerson(name, phone, colorHex, avatar)
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            repository.updatePerson(person)
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            repository.deletePerson(person)
        }
    }

    fun addCardPurchase(
        title: String,
        totalAmount: Double,
        person: Person,
        totalInstallments: Int,
        startYear: Int,
        startMonth: Int,
        notes: String = "",
        markPastInstallmentsAsPaid: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addCardPurchase(
                title = title,
                totalAmount = totalAmount,
                person = person,
                totalInstallments = totalInstallments,
                startYear = startYear,
                startMonth = startMonth,
                notes = notes,
                markPastInstallmentsAsPaid = markPastInstallmentsAsPaid,
                referenceYear = _selectedYear.value,
                referenceMonth = _selectedMonth.value
            )
        }
    }

    /** Compra dividida entre várias pessoas: uma parte para cada uma, todas ligadas. */
    fun addCardPurchaseGroup(
        title: String,
        shares: List<Pair<Person, Double>>,
        totalInstallments: Int,
        startYear: Int,
        startMonth: Int,
        notes: String = "",
        markPastInstallmentsAsPaid: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addCardPurchaseGroup(
                title = title,
                shares = shares,
                totalInstallments = totalInstallments,
                startYear = startYear,
                startMonth = startMonth,
                notes = notes,
                markPastInstallmentsAsPaid = markPastInstallmentsAsPaid,
                referenceYear = _selectedYear.value,
                referenceMonth = _selectedMonth.value
            )
        }
    }

    /**
     * Edita a compra dividida inteira: mesmo nome, parcelas e primeira parcela para todas
     * as partes, cada uma com o novo valor dela. Pagamentos já marcados continuam.
     */
    fun updateCardPurchaseGroup(
        parts: List<Triple<Long, Person, Double>>,
        newTitle: String,
        newTotalInstallments: Int,
        startYear: Int,
        startMonth: Int
    ) {
        viewModelScope.launch {
            parts.forEach { (expenseId, person, amount) ->
                repository.updateExpenseDetails(
                    expenseId = expenseId,
                    newTitle = newTitle,
                    newTotalAmount = amount,
                    newPerson = person,
                    newTotalInstallments = newTotalInstallments,
                    startYear = startYear,
                    startMonth = startMonth
                )
            }
        }
    }

    suspend fun getExpensesByGroupId(groupId: Long): List<Expense> = repository.getExpensesByGroupId(groupId)

    fun addSharedBill(
        title: String,
        totalAmount: Double,
        person: Person,
        myShareAmount: Double,
        otherShareAmount: Double,
        startYear: Int,
        startMonth: Int,
        isRecurring: Boolean,
        recurrenceMonthsCount: Int = 12,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addSharedBill(
                title = title,
                totalAmount = totalAmount,
                person = person,
                myShareAmount = myShareAmount,
                otherShareAmount = otherShareAmount,
                startYear = startYear,
                startMonth = startMonth,
                isRecurring = isRecurring,
                recurrenceMonthsCount = recurrenceMonthsCount,
                notes = notes
            )
        }
    }

    fun addSharedBillWithParticipants(
        title: String,
        totalAmount: Double,
        participants: List<BillParticipantShare>,
        startYear: Int,
        startMonth: Int,
        isRecurring: Boolean,
        recurrenceMonthsCount: Int = 12,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addSharedBillWithParticipants(
                title = title,
                totalAmount = totalAmount,
                participants = participants,
                startYear = startYear,
                startMonth = startMonth,
                isRecurring = isRecurring,
                recurrenceMonthsCount = recurrenceMonthsCount,
                notes = notes
            )
        }
    }

    /** Última exclusão, para o botão "Desfazer" do aviso. */
    private var lastDeleted: List<FinanceRepository.ExpenseSnapshot> = emptyList()

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            lastDeleted = listOfNotNull(repository.snapshotExpense(expenseId))
            repository.deleteExpense(expenseId)
        }
    }

    fun deleteSingleInstallment(installmentId: Long) {
        viewModelScope.launch {
            val expenseId = monthlyInstallments.value.firstOrNull { it.id == installmentId }?.expenseId
                ?: repository.getInstallmentExpenseId(installmentId)
            lastDeleted = listOfNotNull(expenseId?.let { repository.snapshotExpense(it) })
            repository.deleteSingleInstallment(installmentId)
        }
    }

    /** Exclui a compra dividida inteira (todas as partes), com "Desfazer". */
    fun deleteExpenseGroup(groupId: Long) {
        viewModelScope.launch {
            val parts = repository.getExpensesByGroupId(groupId)
            lastDeleted = parts.mapNotNull { repository.snapshotExpense(it.id) }
            parts.forEach { repository.deleteExpense(it.id) }
        }
    }

    /** Desfaz a última exclusão (compra inteira ou parcela). */
    fun undoLastDelete() {
        val snapshots = lastDeleted.ifEmpty { return }
        lastDeleted = emptyList()
        viewModelScope.launch { snapshots.forEach { repository.restoreExpense(it) } }
    }

    fun updateExpense(
        expenseId: Long,
        newTitle: String,
        newTotalAmount: Double,
        newPerson: Person,
        newTotalInstallments: Int,
        startYear: Int,
        startMonth: Int,
        markPastAsPaid: Boolean = false
    ) {
        viewModelScope.launch {
            repository.updateExpenseDetails(
                expenseId = expenseId,
                newTitle = newTitle,
                newTotalAmount = newTotalAmount,
                newPerson = newPerson,
                newTotalInstallments = newTotalInstallments,
                startYear = startYear,
                startMonth = startMonth,
                markPastInstallmentsAsPaid = markPastAsPaid
            )
        }
    }

    fun updateSharedBill(
        expenseId: Long,
        newTitle: String,
        newTotalAmount: Double,
        newPerson: Person,
        newMyShareAmount: Double,
        newOtherShareAmount: Double,
        updateAllMonths: Boolean,
        specificYear: Int,
        specificMonth: Int
    ) {
        viewModelScope.launch {
            repository.updateSharedBill(
                expenseId = expenseId,
                newTitle = newTitle,
                newTotalAmount = newTotalAmount,
                newPerson = newPerson,
                newMyShareAmount = newMyShareAmount,
                newOtherShareAmount = newOtherShareAmount,
                updateAllMonths = updateAllMonths,
                specificYear = specificYear,
                specificMonth = specificMonth
            )
        }
    }

    fun getExpenseById(expenseId: Long, onResult: (Expense?) -> Unit) {
        viewModelScope.launch {
            val expense = repository.getExpenseById(expenseId)
            onResult(expense)
        }
    }

    suspend fun getExpense(expenseId: Long): Expense? {
        return repository.getExpenseById(expenseId)
    }

    // Liquid Glass Theme Mode (Light / Dark)
    private val prefs = getApplication<Application>().getSharedPreferences("liquid_glass_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Lembrado entre aberturas: quem liga a privacidade não quer os valores à mostra ao reabrir
    private val _isPrivacyMode = MutableStateFlow(prefs.getBoolean("is_privacy_mode", false))
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()

    fun toggleDarkMode() {
        val next = !_isDarkMode.value
        _isDarkMode.value = next
        prefs.edit().putBoolean("is_dark_mode", next).apply()
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        prefs.edit().putBoolean("is_dark_mode", dark).apply()
    }

    fun togglePrivacyMode() {
        _isPrivacyMode.value = !_isPrivacyMode.value
        prefs.edit().putBoolean("is_privacy_mode", _isPrivacyMode.value).apply()
    }

    // Person Cards Expansion State (keyed by personId to survive scrolling and recomposition)
    private val _homeExpandedCardStates = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val homeExpandedCardStates: StateFlow<Map<Long, Boolean>> = _homeExpandedCardStates.asStateFlow()

    fun isHomePersonExpanded(personId: Long, default: Boolean = true): Boolean {
        return _homeExpandedCardStates.value[personId] ?: default
    }

    fun toggleHomePersonExpanded(personId: Long, default: Boolean = true) {
        val current = isHomePersonExpanded(personId, default)
        _homeExpandedCardStates.value = _homeExpandedCardStates.value + (personId to !current)
    }

    fun setHomePersonExpanded(personId: Long, expanded: Boolean) {
        _homeExpandedCardStates.value = _homeExpandedCardStates.value + (personId to expanded)
    }

    private val _detailExpandedCardStates = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val detailExpandedCardStates: StateFlow<Map<Long, Boolean>> = _detailExpandedCardStates.asStateFlow()

    fun isDetailPersonExpanded(personId: Long, default: Boolean = true): Boolean {
        return _detailExpandedCardStates.value[personId] ?: default
    }

    fun toggleDetailPersonExpanded(personId: Long, default: Boolean = true) {
        val current = isDetailPersonExpanded(personId, default)
        _detailExpandedCardStates.value = _detailExpandedCardStates.value + (personId to !current)
    }

    fun setDetailPersonExpanded(personId: Long, expanded: Boolean) {
        _detailExpandedCardStates.value = _detailExpandedCardStates.value + (personId to expanded)
    }

    // ==========================================
    // BACKUP & RESTAURAÇÃO DE DADOS
    // ==========================================

    private val _isBackupDialogVisible = MutableStateFlow(false)
    val isBackupDialogVisible: StateFlow<Boolean> = _isBackupDialogVisible.asStateFlow()

    private val _backupUiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val backupUiState: StateFlow<BackupUiState> = _backupUiState.asStateFlow()

    fun openBackupDialog() {
        _isBackupDialogVisible.value = true
        _backupUiState.value = BackupUiState.Idle
    }

    fun closeBackupDialog() {
        _isBackupDialogVisible.value = false
        _backupUiState.value = BackupUiState.Idle
    }

    fun dismissBackupConfirmation() {
        _backupUiState.value = BackupUiState.Idle
    }

    fun resetBackupStatus() {
        _backupUiState.value = BackupUiState.Idle
    }

    fun exportBackupToUri(contentResolver: android.content.ContentResolver, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _backupUiState.value = BackupUiState.Exporting
            try {
                val payload = backupRepository.createBackupPayload()
                val jsonString = BackupSerializer.serialize(payload)
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                } ?: throw IllegalStateException("Não foi possível abrir o arquivo para escrita.")

                com.example.ui.components.BackupReminder.markBackedUp(getApplication())
                _backupUiState.value = BackupUiState.Success(
                    "Backup exportado com sucesso! Contém ${payload.people.size} pessoas, ${payload.expenses.size} despesas e ${payload.installments.size} parcelas."
                )
            } catch (e: Exception) {
                _backupUiState.value = BackupUiState.Error("Falha ao exportar backup: ${e.message ?: "Erro desconhecido"}")
            }
        }
    }

    /** Planilha com todas as parcelas (para Excel / Google Planilhas). */
    fun exportCsvToUri(contentResolver: android.content.ContentResolver, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _backupUiState.value = BackupUiState.Exporting
            try {
                val items = repository.getAllInstallments().first()
                contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(com.example.util.CsvExport.build(items).toByteArray(Charsets.UTF_8))
                    out.flush()
                } ?: throw IllegalStateException("Não foi possível abrir o arquivo para escrita.")
                _backupUiState.value = BackupUiState.Success("Planilha exportada com ${items.size} parcelas.")
            } catch (e: Exception) {
                _backupUiState.value = BackupUiState.Error("Falha ao exportar planilha: ${e.message ?: "Erro desconhecido"}")
            }
        }
    }

    fun readAndValidateBackupUri(contentResolver: android.content.ContentResolver, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _backupUiState.value = BackupUiState.Importing
            try {
                val jsonString = contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                } ?: throw IllegalStateException("Não foi possível abrir o arquivo para leitura.")

                val payload = BackupSerializer.deserialize(jsonString)
                val validation = backupRepository.validateBackup(payload)
                if (validation is BackupValidationResult.Invalid) {
                    _backupUiState.value = BackupUiState.Error("Arquivo inválido: ${validation.reason}")
                    return@launch
                }

                val summary = "${payload.people.size} pessoas, ${payload.expenses.size} despesas e ${payload.installments.size} parcelas"
                _backupUiState.value = BackupUiState.ConfirmingRestore(payload, summary)
            } catch (e: Exception) {
                _backupUiState.value = BackupUiState.Error("Erro ao ler backup: ${e.message ?: "Arquivo inválido"}")
            }
        }
    }

    fun confirmRestoreBackup() {
        val current = _backupUiState.value
        if (current !is BackupUiState.ConfirmingRestore) return

        viewModelScope.launch(Dispatchers.IO) {
            _backupUiState.value = BackupUiState.Importing
            val result = backupRepository.restoreBackup(current.payload)
            if (result.isSuccess) {
                _backupUiState.value = BackupUiState.Success(
                    "Dados restaurados com sucesso! ${current.payload.people.size} pessoas, ${current.payload.expenses.size} despesas e ${current.payload.installments.size} parcelas importadas."
                )
            } else {
                _backupUiState.value = BackupUiState.Error(
                    "Falha ao restaurar banco de dados: ${result.exceptionOrNull()?.message ?: "Erro na transação"}"
                )
            }
        }
    }
}

