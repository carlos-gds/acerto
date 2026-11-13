package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.components.ManagePeopleDialog
import com.example.ui.components.MonthlySummarySection
import com.example.ui.components.MyShareItemCard
import com.example.ui.components.PeopleDebtsEmptyState
import com.example.ui.components.PeopleDebtsHeader
import com.example.ui.components.PersonDebtCard
import com.example.ui.components.QuickAiEntryBottomSheet
import com.example.ui.components.SpeedDialFab
import com.example.ui.components.glass.LiquidBackground
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
    val isShowingPendingMonth by viewModel.isShowingPendingMonth.collectAsStateWithLifecycle()
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

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var showAddCardExpenseDialog by remember { mutableStateOf(false) }
    var showAddSharedBillDialog by remember { mutableStateOf(false) }
    var showManagePeopleDialog by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var expenseSearchQuery by remember { mutableStateOf("") }
    var expenseListFilter by remember { mutableStateOf(ExpenseListFilter.ALL) }
    var itemToEdit by remember { mutableStateOf<InstallmentItem?>(null) }
    var itemToDelete by remember { mutableStateOf<InstallmentItem?>(null) }

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
    LaunchedEffect(selectedTab) { bottomBarState.targetState = true }
    LaunchedEffect(activeListState) {
        var lastIndex = activeListState.firstVisibleItemIndex
        var lastOffset = activeListState.firstVisibleItemScrollOffset
        snapshotFlow { activeListState.firstVisibleItemIndex to activeListState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val down = index > lastIndex || (index == lastIndex && offset > lastOffset + 24)
                val up = index < lastIndex || (index == lastIndex && offset < lastOffset - 24)
                if (down) bottomBarState.targetState = false
                if (up || (index == 0 && offset == 0)) bottomBarState.targetState = true
                lastIndex = index
                lastOffset = offset
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
    BackHandler(enabled = selectedTab == AppTab.HOME && isSearchOpen) { closeSearch() }
    BackHandler(enabled = selectedTab == AppTab.CARD || selectedTab == AppTab.SETTINGS) {
        selectedTab = AppTab.HOME
    }

    // Fundo Obsidian Liquid Glass cobrindo a tela toda
    // Liquid Glass adaptativo: blur real no Android 12+ (fonte = conteúdo das abas);
    // no Android 7–11 ou em aparelhos fracos, backdropState fica null e vale o vidro simulado.
    val blurLevel = rememberLiquidBlurLevel()
    val backdropState = if (blurLevel != LiquidBlurLevel.NONE) rememberLiquidBackdropState() else null

    LiquidBackground(modifier = modifier) {
    CompositionLocalProvider(LocalLiquidBackdrop provides backdropState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .liquidBackdropSource(backdropState),
                transitionSpec = {
                    if (reducedMotion) {
                        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                    } else {
                        val forward = targetState.ordinal > initialState.ordinal
                        val shift = if (forward) 1 else -1
                        (fadeIn(tween(240, delayMillis = 40)) +
                            slideInHorizontally(spring(dampingRatio = 0.9f, stiffness = 380f)) { it / 10 * shift })
                            .togetherWith(fadeOut(tween(140)) + slideOutHorizontally(tween(180)) { -it / 14 * shift })
                    }
                },
                label = "tab_content"
            ) { tab ->
            when (tab) {
                AppTab.HOME -> Column(modifier = Modifier.fillMaxSize()) {
                    HomeHeader(
                        isSearchOpen = isSearchOpen,
                        isPrivacyMode = isPrivacyMode,
                        onSearchClick = { if (isSearchOpen) closeSearch() else isSearchOpen = true },
                        onTogglePrivacy = { viewModel.togglePrivacyMode() },
                        isDarkMode = isDarkMode,
                        onToggleTheme = requestThemeToggle
                    )

                    // Busca + filtros fixos no topo enquanto a busca estiver aberta
                    AnimatedVisibility(visible = isSearchOpen) {
                        ExpenseSearchFilterBar(
                            query = expenseSearchQuery,
                            onQueryChange = { expenseSearchQuery = it },
                            selectedFilter = expenseListFilter,
                            onFilterChange = { expenseListFilter = it },
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                        )
                    }

                    LazyColumn(
                        state = listStateHome,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Mês (pílula compacta) + resumo, sem ações duplicadas
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
                                onOthersDebtClick = { selectedTab = AppTab.DEBTS },
                                showPrivacyToggle = false,
                                showQuickActions = false,
                                pendingMonthHint = isShowingPendingMonth
                            )
                        }

                        // Section Title: O que os outros devem neste mês
                        item {
                            PeopleDebtsHeader(
                                onViewDetailsClick = { selectedTab = AppTab.DEBTS }
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
                            itemsIndexed(filteredPersonGroups, key = { _, g -> "person_group_${g.personId}" }) { index, group ->
                                val isExpanded = homeExpandedCardStates[group.personId] ?: true
                                PersonDebtCard(
                                    modifier = Modifier
                                        .animateItem()
                                        .staggeredAppear("person_group_${group.personId}", index, appearTracker, !reducedMotion),
                                    group = group,
                                    selectedMonth = selectedMonth,
                                    selectedYear = selectedYear,
                                    isPrivacyMode = isPrivacyMode,
                                    isExpanded = isExpanded,
                                    onToggleExpanded = { viewModel.toggleHomePersonExpanded(group.personId) },
                                    onTogglePayment = { item ->
                                        val settlesMonth = !item.isPaid &&
                                            group.pendingAmount - Money.toReal(item.amount) <= 0.001
                                        haptics.performHapticFeedback(
                                            if (settlesMonth) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                                        )
                                        viewModel.togglePayment(item)
                                        if (settlesMonth) notify("${group.personName} quitou tudo neste mês 🎉")
                                    },
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
                                    style = MaterialTheme.typography.titleMedium,
                                    color = liquid.textPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }

                            itemsIndexed(filteredMyShareItems, key = { _, it -> "my_share_${it.id}" }) { index, item ->
                                MyShareItemCard(
                                    modifier = Modifier
                                        .animateItem()
                                        .staggeredAppear("my_share_${item.id}", index + 2, appearTracker, !reducedMotion),
                                    item = item,
                                    isPrivacyMode = isPrivacyMode,
                                    onEdit = { itemToEdit = it },
                                    onDelete = { itemToDelete = it }
                                )
                            }
                        }
                    }
                }

                AppTab.CARD -> Column(modifier = Modifier.fillMaxSize()) {
                    TabHeader(title = "Cartão", subtitle = "Compras parceladas e progresso")

                    // Na aba Cartão a lista é maior: busca e filtros sempre visíveis
                    ExpenseSearchFilterBar(
                        query = expenseSearchQuery,
                        onQueryChange = { expenseSearchQuery = it },
                        selectedFilter = expenseListFilter,
                        onFilterChange = { expenseListFilter = it },
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                    )

                    LazyColumn(
                        state = listStateCard,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            CardPurchasesProgressSection(
                                purchases = filteredCardPurchases,
                                isPrivacyMode = isPrivacyMode,
                                onDeleteExpense = { id ->
                                    viewModel.deleteExpense(id)
                                    notify("Compra excluída")
                                },
                                emptyMessage = if (cardPurchases.isEmpty()) {
                                    "Nenhuma compra parcelada cadastrada"
                                } else {
                                    "Nenhum resultado encontrado para esta pesquisa"
                                }
                            )
                        }
                    }
                }

                AppTab.DEBTS -> DebtsDetailScreen(
                    viewModel = viewModel,
                    onBack = { selectedTab = AppTab.HOME },
                    onEditItem = { itemToEdit = it },
                    onDeleteItem = { itemToDelete = it },
                    showBackButton = false,
                    bottomContentPadding = 140.dp
                )

                AppTab.SETTINGS -> SettingsScreen(
                    isDarkMode = isDarkMode,
                    isPrivacyMode = isPrivacyMode,
                    peopleCount = allPeople.size,
                    onToggleTheme = { requestThemeToggle(Offset.Unspecified) },
                    onTogglePrivacy = { viewModel.togglePrivacyMode() },
                    onManagePeopleClick = { showManagePeopleDialog = true },
                    onBackupClick = { viewModel.openBackupDialog() }
                )
            }
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
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(150.dp)
                    .graphicsLayer { alpha = if (backdropState != null) 0.75f else 1f }
                    .background(liquid.bottomFade)
            )

            // Barra inferior Liquid Glass; à direita fica o botão + (SpeedDial, 58 dp)
            AnimatedVisibility(
                visibleState = bottomBarState,
                enter = slideInVertically(spring(dampingRatio = 0.8f, stiffness = 360f)) { it * 2 } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(220)) { it * 2 } + fadeOut(tween(160)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, end = 90.dp, bottom = 17.dp)
            ) {
            LiquidBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    // Se a busca/filtro estiver ativo (ex.: digitado na aba Cartão),
                    // a Início mostra a barra de busca para nada ficar filtrado escondido.
                    if (tab == AppTab.HOME &&
                        (expenseSearchQuery.isNotBlank() || expenseListFilter != ExpenseListFilter.ALL)
                    ) {
                        isSearchOpen = true
                    }
                    selectedTab = tab
                }
            )
            }

            // Expandable Speed Dial FAB with Auto-Hide and Darkened Backdrop (inalterado)
            SpeedDialFab(
                listState = activeListState,
                onAiEntryClick = { viewModel.openAiBottomSheet() },
                onSharedBillClick = { showAddSharedBillDialog = true },
                onCardExpenseClick = { showAddCardExpenseDialog = true }
            )
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
                    color = liquid.textPrimary
                )
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
                notify("Compra salva")
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
                notify("Conta dividida salva")
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
                notify("Parcela excluída")
            },
            onDeleteAllInstallments = {
                viewModel.deleteExpense(itemToDelete!!.expenseId)
                itemToDelete = null
                notify("Lançamento excluído")
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
private fun HomeHeader(
    isSearchOpen: Boolean,
    isPrivacyMode: Boolean,
    onSearchClick: () -> Unit,
    onTogglePrivacy: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: (Offset) -> Unit
) {
    var themeButtonCenter by remember { mutableStateOf(Offset.Unspecified) }
    val liquid = LocalLiquidGlassColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    append("Acerto")
                    withStyle(SpanStyle(color = liquid.accent)) { append(".") }
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = liquid.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Fatura Mensal",
                style = MaterialTheme.typography.bodySmall,
                color = liquid.textSecondary
            )
        }
        GlassIconButton(
            icon = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
            contentDescription = if (isSearchOpen) "Fechar pesquisa" else "Pesquisar",
            onClick = onSearchClick,
            accent = isSearchOpen,
            modifier = Modifier.testTag("home_search_button")
        )
        GlassIconButton(
            icon = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = if (isPrivacyMode) "Mostrar valores" else "Ocultar valores",
            onClick = onTogglePrivacy,
            accent = isPrivacyMode,
            modifier = Modifier.testTag("toggle_privacy_button")
        )
        // Tema claro/escuro (a troca anima com revelação circular a partir deste botão)
        GlassIconButton(
            icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDarkMode) "Alternar para Modo Claro" else "Alternar para Modo Escuro",
            onClick = { onToggleTheme(themeButtonCenter) },
            modifier = Modifier
                .onGloballyPositioned { themeButtonCenter = it.boundsInRoot().center }
                .testTag("home_theme_toggle_button")
        )
    }
}

/** Cabeçalho simples das abas (título com o ponto no acento + subtítulo). */
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
