package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.platform.LocalDensity
import com.example.ui.components.glass.AcertoMotion
import com.example.ui.components.glass.popIn
import androidx.compose.runtime.key
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.components.glass.LiquidBlurDefaults
import com.example.ui.components.glass.LiquidBlurLevel
import com.example.ui.components.glass.LocalLiquidBackdrop
import com.example.ui.components.glass.liquidBackdropBlur
import com.example.ui.components.glass.liquidBackdropSource
import com.example.ui.components.glass.rememberLiquidBackdropState
import com.example.ui.components.glass.rememberLiquidBlurLevel
import com.example.ui.components.glass.liquidGlass
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.theme.LiquidShapes
import com.example.util.Money
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.ui.theme.LiquidDarkTokens
import com.example.ui.theme.LiquidLightTokens
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.example.ui.components.glass.rememberAppearTracker
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.components.glass.staggeredAppear
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.example.ui.components.AppTab
import com.example.ui.components.LiquidBottomBar
import com.example.ui.components.glass.GlassIconButton
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.ui.components.AiExpenseConfirmationDialog
import com.example.ui.components.BackupSettingsDialog
import com.example.ui.components.CardPurchasesProgressSection
import com.example.ui.components.DeleteExpenseDialog
import com.example.ui.components.EditExpenseDialog
import com.example.ui.components.EditSharedBillDialog
import com.example.ui.components.ExpenseListFilter
import com.example.ui.components.ExpenseSearchFilterBar
import com.example.ui.components.ManagePeopleDialog
import com.example.ui.components.MyShareItemCard
import com.example.ui.components.QuickAiEntryBottomSheet
import com.example.ui.components.glass.LiquidBackground
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier,
    launchAction: String? = null,
    onLaunchActionHandled: () -> Unit = {}
) {
    val liquid = LocalLiquidGlassColors.current

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val isShowingPendingMonth by viewModel.isShowingPendingMonth.collectAsStateWithLifecycle()
    val allPeople by viewModel.allPeople.collectAsStateWithLifecycle()
    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val personGroups by viewModel.personMonthlyGroups.collectAsStateWithLifecycle()
    val cardPurchases by viewModel.cardPurchasesProgress.collectAsStateWithLifecycle()
    val monthlyInstallments by viewModel.monthlyInstallments.collectAsStateWithLifecycle()
    val homeExpandedCardStates by viewModel.homeExpandedCardStates.collectAsStateWithLifecycle()
    val allInstallments by viewModel.allInstallments.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val monthlyHistory by viewModel.monthlyHistory.collectAsStateWithLifecycle()

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

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) viewModel.exportCsvToUri(context.contentResolver, uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.readAndValidateBackupUri(context.contentResolver, uri)
        }
    }

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var showAddCardExpenseDialog by remember { mutableStateOf(false) }
    var showAddSharedBillDialog by remember { mutableStateOf(false) }
    /** Pessoa já escolhida ao abrir a Nova compra (ex.: botão + no cartão dela). */
    var purchasePersonId by remember { mutableStateOf<Long?>(null) }
    var showManagePeopleDialog by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var expenseSearchQuery by remember { mutableStateOf("") }
    var expenseListFilter by remember { mutableStateOf(ExpenseListFilter.ALL) }
    var itemToEdit by remember { mutableStateOf<InstallmentItem?>(null) }
    var itemToDelete by remember { mutableStateOf<InstallmentItem?>(null) }
    // Pessoa aberta na Início (tela lado a lado); null = resumo do mês
    var openPersonId by rememberSaveable { mutableStateOf<Long?>(null) }
    // Fluxo "Receber" (tela cheia por cima de tudo)
    var receiveOpen by rememberSaveable { mutableStateOf(false) }
    var receivePersonId by rememberSaveable { mutableStateOf<Long?>(null) }

    // Atalhos do ícone e do widget
    LaunchedEffect(launchAction) {
        when (launchAction) {
            com.example.util.AppActions.RECEIVE -> { selectedTab = AppTab.HOME; receivePersonId = null; receiveOpen = true }
            com.example.util.AppActions.NEW_PURCHASE -> showAddCardExpenseDialog = true
            com.example.util.AppActions.AI -> viewModel.openAiBottomSheet()
            else -> return@LaunchedEffect
        }
        onLaunchActionHandled()
    }

    // Widget: mantém o "falta receber" atualizado
    LaunchedEffect(allInstallments, isPrivacyMode) {
        com.example.util.AcertoWidget.save(context, com.example.util.AcertoWidget.snapshotOf(allInstallments), isPrivacyMode)
    }
    val pagePx = with(LocalDensity.current) { 12.dp.roundToPx() }

    val listStateHome = rememberLazyListState()
    val listStateCard = rememberLazyListState()
    val listStateOther = rememberLazyListState()
    val activeListState = when (selectedTab) {
        AppTab.HOME -> listStateHome
        AppTab.CARD -> listStateCard
        else -> listStateOther
    }

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

    // Quanto cada pessoa deve no cartão neste mês (valor em destaque do Cartão e ordem dos cartões)
    val cardMonthDue = remember(allInstallments, selectedYear, selectedMonth) {
        allInstallments
            .filter {
                !it.isPaid && !it.isMyShare && it.expenseType == ExpenseType.CARD_PURCHASE &&
                    it.year == selectedYear && it.month == selectedMonth
            }
            .groupBy { it.personId }
            .mapValues { (_, l) -> Money.toReal(l.sumOf { it.amount }) }
    }
    // Grupos de pessoas no cartão, na mesma ordem usada tanto na pilha da Início quanto na
    // lista do Cartão (e no overlay que os liga durante o arrastar entre os dois).
    val cardGroups = remember(cardPurchases, allPeople, cardMonthDue) {
        buildCardGroups(cardPurchases, allPeople, cardMonthDue)
    }
    // Âncoras e visibilidade da travessia Início↔Cartão: escritas pelas telas, lidas só pelo
    // overlay e em fases de desenho — nunca aqui, para o arrasto não recompor esta tela.
    val cardTransition = remember { CardTransitionState() }

    // Movimento: entrada progressiva dos cards (uma vez por item) e respeito à redução de animações
    val reducedMotion = rememberReducedMotion()

    // Troca de tema com revelação circular: a cor do novo tema se espalha a partir do
    // botão, o tema muda por baixo e a camada some em fade. Tudo na fase de desenho.
    val themeScope = rememberCoroutineScope()

    // Lei de Doherty / pico-fim: resposta imediata às ações (aviso curto + vibração leve)
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    val notify: (String) -> Unit = { message ->
        themeScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }
    // Exclusão com "Desfazer": o aviso fica alguns segundos e restaura tudo se tocado
    val notifyUndo: (String) -> Unit = { message ->
        themeScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Desfazer",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastDelete()
                notify("Exclusão desfeita")
            }
        }
    }
    val revealProgress = remember { Animatable(0f) }
    val revealAlpha = remember { Animatable(0f) }
    var revealToDark by remember { mutableStateOf<Boolean?>(null) }
    var revealOrigin by remember { mutableStateOf(Offset.Unspecified) }
    val requestThemeToggle: (Offset) -> Unit = toggle@{ origin ->
        if (revealToDark != null) return@toggle
        if (reducedMotion) {
            viewModel.toggleDarkMode()
            return@toggle
        }
        revealOrigin = origin
        revealToDark = !isDarkMode
        themeScope.launch {
            revealAlpha.snapTo(1f)
            revealProgress.snapTo(0f)
            revealProgress.animateTo(1f, tween(durationMillis = 460, easing = FastOutSlowInEasing))
            viewModel.toggleDarkMode()
            withFrameNanos { }
            withFrameNanos { }
            revealAlpha.animateTo(0f, tween(durationMillis = 280))
            revealToDark = null
        }
    }
    val appearTracker = rememberAppearTracker()

    // Barra inferior: entra ao abrir o app, some ao rolar para baixo e volta ao rolar para cima
    val bottomBarState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) { bottomBarState.targetState = true }
    LaunchedEffect(selectedTab) { bottomBarState.targetState = selectedTab != AppTab.SETTINGS }
    LaunchedEffect(activeListState) {
        var lastIndex = activeListState.firstVisibleItemIndex
        var lastOffset = activeListState.firstVisibleItemScrollOffset
        snapshotFlow { activeListState.firstVisibleItemIndex to activeListState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val down = index > lastIndex || (index == lastIndex && offset > lastOffset + 24)
                val up = index < lastIndex || (index == lastIndex && offset < lastOffset - 24)
                if (down) bottomBarState.targetState = false
                if ((up || (index == 0 && offset == 0)) && selectedTab != AppTab.SETTINGS) bottomBarState.targetState = true
                lastIndex = index
                lastOffset = offset
            }
    }

    // Qualquer rolagem em qualquer aba (listas ou telas roláveis) esconde/mostra a barra
    val barScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): Offset {
                if (selectedTab == AppTab.SETTINGS) return Offset.Zero
                if (available.y < -4f) bottomBarState.targetState = false
                else if (available.y > 4f) bottomBarState.targetState = true
                return Offset.Zero
            }
        }
    }

    // Fechar a busca limpa o texto e o filtro, para nada ficar filtrado sem estar visível.
    val closeSearch: () -> Unit = {
        isSearchOpen = false
        expenseSearchQuery = ""
        expenseListFilter = ExpenseListFilter.ALL
    }

    // Voltar: fecha a busca da Início; em outras abas, volta para Início.
    // (A aba Cobranças trata o voltar no próprio BackHandler, via onBack.)
    BackHandler(enabled = selectedTab == AppTab.HOME && openPersonId != null) { openPersonId = null }
    BackHandler(enabled = selectedTab == AppTab.HOME && isSearchOpen && openPersonId == null) { closeSearch() }
    BackHandler(enabled = selectedTab == AppTab.CARD || selectedTab == AppTab.SETTINGS || selectedTab == AppTab.DEBTS) {
        selectedTab = AppTab.HOME
    }

    // Abas em páginas: arrastar para o lado troca de aba (Pessoas, Início, Cartão)
    val pagerTabs = remember { listOf(AppTab.DEBTS, AppTab.HOME, AppTab.CARD) }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = pagerTabs.indexOf(selectedTab).takeIf { it >= 0 } ?: 1
    ) { pagerTabs.size }
    // toque na barra (ou voltar) → página desliza até a aba
    LaunchedEffect(selectedTab) {
        val target = pagerTabs.indexOf(selectedTab)
        if (target >= 0 && pagerState.currentPage != target) {
            if (reducedMotion) pagerState.scrollToPage(target)
            else pagerState.animateScrollToPage(target, animationSpec = spring(dampingRatio = 0.9f, stiffness = 380f))
        }
    }
    // arrastou e soltou numa página → ela vira a aba atual
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val tab = pagerTabs[page]
            if (selectedTab != AppTab.SETTINGS && selectedTab != tab) {
                if (tab != AppTab.HOME) openPersonId = null
                selectedTab = tab
            }
        }
    }
    // sem arrastar enquanto algo está aberto por cima
    val swipeEnabled = !receiveOpen && !showAddCardExpenseDialog && !showAddSharedBillDialog && selectedTab != AppTab.SETTINGS

    // Fundo Obsidian Liquid Glass cobrindo a tela toda
    // Liquid Glass adaptativo: blur real no Android 12+ (fonte = conteúdo das abas);
    // no Android 7–11 ou em aparelhos fracos, backdropState fica null e vale o vidro simulado.
    val blurLevel = rememberLiquidBlurLevel()
    val backdropState = if (blurLevel != LiquidBlurLevel.NONE) rememberLiquidBackdropState() else null

    LiquidBackground(modifier = modifier) {
    CompositionLocalProvider(LocalLiquidBackdrop provides backdropState) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(barScrollConnection)
                    .liquidBackdropSource(backdropState)
            ) {
            // Progresso contínuo do pager, só lido na fase de desenho (dentro de graphicsLayer)
            // pelas telas grandes, ou dentro do próprio CardTransitionOverlay (pequeno): nunca
            // como valor de composição aqui, senão a árvore toda recomporia a cada frame do arrasto.
            CompositionLocalProvider(
                com.example.ui.components.LocalTabPosition provides { pagerState.currentPage + pagerState.currentPageOffsetFraction }
            ) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                userScrollEnabled = swipeEnabled,
                key = { pagerTabs[it] },
                modifier = Modifier.fillMaxSize().testTag("tabs_pager")
            ) { page ->
            // Transição horizontal de verdade: a página que sai desliza para a esquerda e a
            // que entra vem da direita (o próprio arrasto do pager, em ~300 ms ao soltar ou
            // tocar na barra, com a mola de animateScrollToPage abaixo). O fundo de cada tela
            // continua ancorado na raiz (noirHeaderBackground), então não aparece costura.
            Box(Modifier.fillMaxSize()) {
            when (pagerTabs[page]) {
                AppTab.HOME -> AnimatedContent(
                    targetState = openPersonId,
                    transitionSpec = {
                        // Page side-by-side (transitions.dev): 250 ms, poucos dp, curva smooth-out
                        if (reducedMotion) {
                            fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                        } else {
                            val forward = targetState != null
                            val dir = if (forward) 1 else -1
                            (fadeIn(tween(AcertoMotion.FAST, easing = AcertoMotion.SmoothOut)) +
                                slideInHorizontally(tween(AcertoMotion.FAST, easing = AcertoMotion.SmoothOut)) { pagePx * dir })
                                .togetherWith(
                                    fadeOut(tween(AcertoMotion.QUICK)) +
                                        slideOutHorizontally(tween(AcertoMotion.QUICK)) { -pagePx * dir }
                                )
                        }
                    },
                    label = "home_pages"
                ) { personId ->
                    val openGroup = personId?.let { id -> personGroups.firstOrNull { it.personId == id } }
                    if (openGroup != null) {
                        PersonMonthDetail(
                            group = openGroup,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            isPrivacyMode = isPrivacyMode,
                            onBack = { openPersonId = null },
                            onTogglePayment = { item ->
                                val settlesMonth = !item.isPaid &&
                                    openGroup.pendingAmount - Money.toReal(item.amount) <= 0.001
                                haptics.performHapticFeedback(
                                    if (settlesMonth) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                                )
                                viewModel.togglePayment(item)
                                if (settlesMonth) {
                                    notify("${openGroup.personName} pagou tudo de ${CurrencyFormatter.getMonthName(selectedMonth).lowercase()}")
                                }
                            },
                            onEditItem = { itemToEdit = it },
                            onDeleteItem = { itemToDelete = it },
                            onReceive = { receivePersonId = openGroup.personId; receiveOpen = true },
                            payoff = remember(allInstallments, openGroup.personId) {
                                com.example.util.Forecast.payoff(allInstallments, openGroup.personId)
                            },
                            onPartialPayment = { item, amount ->
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.registerPartialPayment(item, amount)
                                notify("Pagamento de ${CurrencyFormatter.format(amount)} registrado")
                            }
                        )
                    } else {
                        HomeOverview(
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            isCurrentMonth = selectedYear == viewModel.currentYear && selectedMonth == viewModel.currentMonth,
                            showingPendingMonth = isShowingPendingMonth,
                            groups = filteredPersonGroups,
                            myShareItems = filteredMyShareItems,
                            isPrivacyMode = isPrivacyMode,
                            isSearchOpen = isSearchOpen,
                            hasSearchQuery = normalizedSearchQuery.isNotBlank() || expenseListFilter != ExpenseListFilter.ALL,
                            listState = listStateHome,
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onTogglePrivacy = { viewModel.togglePrivacyMode() },
                            onSearchClick = { if (isSearchOpen) closeSearch() else isSearchOpen = true },
                            onAddCardPurchase = { showAddCardExpenseDialog = true },
                            onAddSharedBill = { showAddSharedBillDialog = true },
                            onAiEntry = { viewModel.openAiBottomSheet() },
                            onOpenPerson = { openPersonId = it },
                            onMarkPersonPaid = { g ->
                                val open = g.items.filter { !it.isPaid && !it.isMyShare }
                                open.forEach { viewModel.togglePayment(it) }
                                themeScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    val r = snackbarHostState.showSnackbar("${g.personName}: mês marcado como pago", actionLabel = "Desfazer", duration = SnackbarDuration.Long)
                                    if (r == SnackbarResult.ActionPerformed) open.forEach { viewModel.togglePayment(it.copy(isPaid = true)) }
                                }
                            },
                            onEditItem = { itemToEdit = it },
                            onDeleteItem = { itemToDelete = it },
                            allInstallments = allInstallments,
                            recurring = remember(monthlyInstallments, allExpenses) {
                                com.example.util.Forecast.recurring(
                                    monthlyInstallments,
                                    allExpenses.filter { it.isRecurring }.map { it.id }.toSet()
                                )
                            },
                            onSelectMonth = { y, m -> viewModel.selectMonth(y, m) },
                            onGoToCurrentMonth = { viewModel.selectMonth(viewModel.currentYear, viewModel.currentMonth) },
                            onReceive = { receivePersonId = null; receiveOpen = true },
                            onOpenCards = { selectedTab = AppTab.CARD },
                            onOpenProfile = { selectedTab = AppTab.SETTINGS },
                            cardGroups = cardGroups,
                            cardTransition = cardTransition,
                            searchBar = {
                                ExpenseSearchFilterBar(
                                    query = expenseSearchQuery,
                                    onQueryChange = { expenseSearchQuery = it },
                                    selectedFilter = expenseListFilter,
                                    onFilterChange = { expenseListFilter = it },
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                                )
                            }
                        )
                    }
                }

                AppTab.CARD -> CardStackScreen(
                    purchases = filteredCardPurchases,
                    totalCount = cardPurchases.size,
                    isPrivacyMode = isPrivacyMode,
                    searchBar = {
                        ExpenseSearchFilterBar(
                            query = expenseSearchQuery,
                            onQueryChange = { expenseSearchQuery = it },
                            selectedFilter = expenseListFilter,
                            onFilterChange = { expenseListFilter = it },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    },
                    onAddPurchase = { personId -> purchasePersonId = personId; showAddCardExpenseDialog = true },
                    onDelete = { id ->
                        viewModel.deleteExpense(id)
                        notifyUndo("Compra excluída")
                    },
                    people = allPeople,
                    onReceive = { id -> receivePersonId = id; receiveOpen = true },
                    onCharge = { id -> selectedTab = AppTab.HOME; openPersonId = id },
                    monthDue = cardMonthDue,
                    monthName = CurrencyFormatter.getMonthName(selectedMonth).lowercase(),
                    cardTransition = cardTransition
                )

                AppTab.DEBTS -> PeopleScreen(
                    people = allPeople,
                    installments = allInstallments,
                    history = monthlyHistory,
                    isPrivacyMode = isPrivacyMode,
                    onAddPerson = { showManagePeopleDialog = true },
                    onEditPeople = { showManagePeopleDialog = true },
                    onReceive = { id -> receivePersonId = id; receiveOpen = true },
                    onCharge = { id -> selectedTab = AppTab.HOME; openPersonId = id },
                    onOpenMonth = { id, y, m ->
                        viewModel.selectMonth(y, m)
                        selectedTab = AppTab.HOME
                        openPersonId = id
                    }
                )

                AppTab.SETTINGS -> Unit // fica por cima das páginas
            }
            }
            }

            // Ajustes: entram por cima, vindos da direita (abertos pela foto de perfil)
            androidx.compose.animation.AnimatedVisibility(
                visible = selectedTab == AppTab.SETTINGS,
                enter = if (reducedMotion) fadeIn(tween(0)) else slideInHorizontally(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut)) { it / 3 } + fadeIn(tween(AcertoMotion.FAST)),
                exit = if (reducedMotion) fadeOut(tween(0)) else slideOutHorizontally(tween(AcertoMotion.FAST, easing = AcertoMotion.SmoothOut)) { it / 3 } + fadeOut(tween(AcertoMotion.QUICK))
            ) {
                Box(Modifier.fillMaxSize().background(liquid.background)) {
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        isPrivacyMode = isPrivacyMode,
                        peopleCount = allPeople.size,
                        onToggleTheme = { requestThemeToggle(Offset.Unspecified) },
                        onTogglePrivacy = { viewModel.togglePrivacyMode() },
                        onManagePeopleClick = { showManagePeopleDialog = true },
                        onBackupClick = { viewModel.openBackupDialog() },
                        onBack = { selectedTab = AppTab.HOME },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp + com.example.ui.components.navBarsBottom())
                    )
                }
            }

            // Grupo de cartões em destaque, por cima do pager, durante a travessia Início↔Cartão
            // (some sozinho fora dela: a pose real da Início ou os PersonCard do Cartão assumem).
            CardTransitionOverlay(
                groups = cardGroups,
                isPrivacyMode = isPrivacyMode,
                monthName = CurrencyFormatter.getMonthName(selectedMonth).lowercase(),
                state = cardTransition,
                enabled = normalizedSearchQuery.isBlank() && expenseListFilter == ExpenseListFilter.ALL
            )
            } // fecha CompositionLocalProvider(LocalTabPosition)
            }

            // Fade inferior do Figma: o conteúdo some suavemente atrás da barra.
            // Android 13+: blur progressivo (mais borrado embaixo) por baixo do fade.
            if (blurLevel == LiquidBlurLevel.PROGRESSIVE) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(150.dp)
                        .liquidBackdropBlur(radius = LiquidBlurDefaults.fade, progressive = true)
                )
            }
            // Cor do fim de cada tela: o fade e a barra acompanham essa cor ao trocar de aba
            val screenBottom = when {
                selectedTab == AppTab.CARD -> com.example.ui.theme.AcertoPalette.Header
                selectedTab == AppTab.SETTINGS -> liquid.background
                selectedTab == AppTab.HOME && openPersonId != null -> liquid.background
                else -> liquid.sheetSurface
            }
            val bottomColor by animateColorAsState(
                screenBottom,
                tween(com.example.ui.components.glass.AcertoMotion.SLOW, easing = com.example.ui.components.glass.AcertoMotion.SmoothOut),
                label = "screen_bottom_color"
            )
            val darkBottom = screenBottom.luminance() < 0.25f
            val barColor by animateColorAsState(
                // fundo escuro: a barra se mistura com ele (só a pílula clara aparece);
                // fundo claro: pílula escura
                if (darkBottom) screenBottom else com.example.ui.theme.AcertoPalette.NavBar,
                tween(com.example.ui.components.glass.AcertoMotion.SLOW, easing = com.example.ui.components.glass.AcertoMotion.SmoothOut),
                label = "bottom_bar_color"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(150.dp + com.example.ui.components.navBarsBottom())
                    .background(
                        Brush.verticalGradient(
                            0f to bottomColor.copy(alpha = 0f),
                            0.45f to bottomColor.copy(alpha = 0.72f),
                            1f to bottomColor
                        )
                    )
                    .testTag("bottom_fade")
            )

            // Barra inferior compacta e centralizada (sem botão + separado)
            AnimatedVisibility(
                visibleState = bottomBarState,
                enter = slideInVertically(spring(dampingRatio = 0.8f, stiffness = 360f)) { it * 2 } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(220)) { it * 2 } + fadeOut(tween(160)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 17.dp)
            ) {
            LiquidBottomBar(
                containerColor = barColor,
                indicatorPosition = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    // Se a busca/filtro estiver ativo (ex.: digitado na aba Cartão),
                    // a Início mostra a barra de busca para nada ficar filtrado escondido.
                    if (tab == AppTab.HOME &&
                        (expenseSearchQuery.isNotBlank() || expenseListFilter != ExpenseListFilter.ALL)
                    ) {
                        isSearchOpen = true
                    }
                    if (tab != AppTab.HOME || selectedTab == AppTab.HOME) openPersonId = null
                    selectedTab = tab
                }
            )
            }

        }

        // Avisos curtos em vidro, acima da barra inferior
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 96.dp)
        ) { data ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = LiquidShapes.field,
                        surface = if (backdropState != null) liquid.sheetSurface.copy(alpha = 0.7f) else liquid.sheetSurface,
                        elevation = 8.dp,
                        blur = LiquidBlurDefaults.toast
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("liquid_snackbar")
            ) {
                // "Check": ✓ surge com mola a cada aviso novo
                key(data) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = liquid.accentPaid,
                        modifier = Modifier
                            .size(20.dp)
                            .popIn()
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = liquid.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                val action = data.visuals.actionLabel
                if (action != null) {
                    TextButton(
                        onClick = { data.performAction() },
                        modifier = Modifier.heightIn(min = 44.dp).testTag("snackbar_undo_button")
                    ) {
                        Text(
                            action,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = liquid.textPrimary
                        )
                    }
                }
            }
        }

        // Camada da troca de tema (só existe durante a animação; bloqueia toques nesse tempo)
        val targetDark = revealToDark
        if (targetDark != null) {
            val targetTokens = if (targetDark) LiquidDarkTokens else LiquidLightTokens
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
                    .drawBehind {
                        val center = if (revealOrigin.isSpecified) revealOrigin else Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = listOf(
                            Offset(0f, 0f), Offset(size.width, 0f),
                            Offset(0f, size.height), Offset(size.width, size.height)
                        ).maxOf { (it - center).getDistance() }
                        drawCircle(
                            brush = targetTokens.backgroundGradient,
                            radius = maxRadius * revealProgress.value,
                            center = center,
                            alpha = revealAlpha.value
                        )
                    }
                    .testTag("theme_reveal_overlay")
            )
        }

        // Receber: grade de pessoas → teclado-calculadora → deslizar → sucesso
        androidx.compose.animation.AnimatedVisibility(
            visible = receiveOpen,
            enter = fadeIn(tween(AcertoMotion.FAST)),
            exit = fadeOut(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut)) +
                androidx.compose.animation.scaleOut(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut), targetScale = 1.04f)
        ) {
            val targets = remember(allPeople, allInstallments, selectedYear, selectedMonth) {
                allPeople.map { p ->
                    val open = allInstallments.filter { it.personId == p.id && !it.isMyShare && !it.isPaid }
                    ReceiveTarget(
                        personId = p.id,
                        name = p.name,
                        colorHex = p.colorHex,
                        avatar = p.avatar,
                        monthPending = Money.toReal(open.filter { it.year == selectedYear && it.month == selectedMonth }.sumOf { it.amount }),
                        totalPending = Money.toReal(open.sumOf { it.amount })
                    )
                }
            }
            ReceiveFlow(
                targets = targets,
                initialPersonId = receivePersonId,
                monthName = CurrencyFormatter.getMonthName(selectedMonth).lowercase(),
                isPrivacyMode = isPrivacyMode,
                onConfirm = { id, amount -> viewModel.receivePayment(id, amount) },
                onCharge = { id ->
                    receiveOpen = false
                    selectedTab = AppTab.HOME
                    openPersonId = id
                },
                onClose = { receiveOpen = false }
            )
        }

        // Nova compra em etapas: quem comprou → valor e parcelas → detalhes
        androidx.compose.animation.AnimatedVisibility(
            visible = showAddCardExpenseDialog,
            enter = fadeIn(tween(AcertoMotion.FAST)),
            exit = fadeOut(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut)) +
                androidx.compose.animation.scaleOut(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut), targetScale = 1.04f)
        ) {
            NewPurchaseFlow(
                people = allPeople,
                initialPersonId = purchasePersonId,
                currentYear = selectedYear,
                currentMonth = selectedMonth,
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
                },
                onClose = { showAddCardExpenseDialog = false; purchasePersonId = null }
            )
        }

        // Dividir conta em etapas: valor → com quem → detalhes
        androidx.compose.animation.AnimatedVisibility(
            visible = showAddSharedBillDialog,
            enter = fadeIn(tween(AcertoMotion.FAST)),
            exit = fadeOut(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut)) +
                androidx.compose.animation.scaleOut(tween(AcertoMotion.SLOW, easing = AcertoMotion.SmoothOut), targetScale = 1.04f)
        ) {
            SplitBillFlow(
                people = allPeople,
                currentYear = selectedYear,
                currentMonth = selectedMonth,
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
                },
                onClose = { showAddSharedBillDialog = false }
            )
        }
    }
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
                    notify("Alterações salvas")
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
                    notify("Alterações salvas")
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
                notifyUndo("Parcela excluída")
            },
            onDeleteAllInstallments = {
                viewModel.deleteExpense(itemToDelete!!.expenseId)
                itemToDelete = null
                notifyUndo("Lançamento excluído")
            }
        )
    }

    // Dialog: Gerenciar Pessoas
    if (showManagePeopleDialog) {
        ManagePeopleDialog(
            people = allPeople,
            onDismiss = { showManagePeopleDialog = false },
            onAddPerson = { name, phone, colorHex, avatar ->
                viewModel.addPerson(name, phone, colorHex, avatar)
                notify("Pessoa cadastrada")
            },
            onUpdatePerson = { person -> viewModel.updatePerson(person) },
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
                notify("Lançamento da IA salvo")
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
                val suggestedFileName = "acerto-backup-$dateStr.json"
                exportLauncher.launch(suggestedFileName)
            },
            onExportCsvClick = {
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                csvLauncher.launch("acerto-parcelas-$dateStr.csv")
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


/** Cabeçalho da Início: só título, busca e privacidade. */
@Composable
private fun TabHeader(title: String, subtitle: String) {
    val liquid = LocalLiquidGlassColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append(title)
                withStyle(SpanStyle(color = liquid.accent)) { append(".") }
            },
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = liquid.textPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = liquid.textSecondary
        )
    }
}
