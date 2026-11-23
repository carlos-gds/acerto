package com.example

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.QuemMeDeveTheme
import com.example.ui.viewmodel.FinanceViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Arrastar para o lado troca de aba, e a barra acompanha. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-xxhdpi", sdk = [36])
class TabSwipeTest {
    @get:Rule val rule = createComposeRule()

    @Before fun limpa() {
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = com.example.data.db.AppDatabase.getDatabase(ctx)
        val t = Thread { db.clearAllTables() }; t.start(); t.join()
    }

    private fun settle() { rule.mainClock.advanceTimeBy(1500); rule.waitForIdle() }

    @Test
    fun arrastarTrocaDeAba() {
        rule.setContent {
            val vm: FinanceViewModel = viewModel()
            QuemMeDeveTheme(darkTheme = false) { HomeScreen(viewModel = vm) }
        }
        rule.waitUntil(30_000) { rule.onAllNodes(hasText("Falta receber")).fetchSemanticsNodes().isNotEmpty() }
        settle()
        rule.onNodeWithTag("bottom_nav_home").assertIsSelected()

        rule.onNodeWithTag("tabs_pager").performTouchInput { swipeLeft() }; settle()
        rule.onNodeWithTag("bottom_nav_card").assertIsSelected()

        rule.onNodeWithTag("tabs_pager").performTouchInput { swipeRight() }; settle()
        rule.onNodeWithTag("tabs_pager").performTouchInput { swipeRight() }; settle()
        rule.onNodeWithTag("bottom_nav_debts").assertIsSelected()

        // toque na barra continua funcionando
        rule.onNodeWithTag("bottom_nav_card").performClick(); settle()
        rule.onNodeWithTag("bottom_nav_card").assertIsSelected()
    }
}
