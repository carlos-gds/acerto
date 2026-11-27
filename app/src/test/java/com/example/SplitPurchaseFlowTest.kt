package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.example.data.model.Person
import com.example.ui.screens.NewPurchaseFlow
import com.example.ui.theme.QuemMeDeveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Nova compra dividida: geladeira de R$ 2.000 em 10x entre a Mãe e a Vó. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi", sdk = [36])
class SplitPurchaseFlowTest {
    @get:Rule val rule = createComposeRule()

    private val people = listOf(
        Person(id = 1, name = "Mãe", phone = "", colorHex = "#A78BFA", avatar = "FEMALE"),
        Person(id = 2, name = "Vó", phone = "", colorHex = "#34D399", avatar = "FEMALE")
    )

    private fun settle() { rule.mainClock.advanceTimeBy(1200); rule.waitForIdle() }

    private fun abrirGeladeiraDividida(onSaved: (String) -> Unit) {
        rule.setContent {
            QuemMeDeveTheme(darkTheme = true) {
                NewPurchaseFlow(
                    people = people, currentYear = 2026, currentMonth = 11, onAddPerson = {},
                    onConfirm = { title, total, shares, n, _, _, _, _ ->
                        onSaved("$title|$total|$n|" + shares.joinToString(",") { "${it.first.name}=${it.second}" })
                    },
                    onClose = {}
                )
            }
        }
        settle()
        rule.onNodeWithTag("purchase_person_1").performClick(); settle()
        listOf("2", "0", "0", "0", "00").forEach { rule.onNodeWithTag("purchase_key_$it").performClick() }
        rule.onNodeWithTag("purchase_installments").performScrollToIndex(9); settle()
        rule.onNodeWithTag("purchase_installment_10").performClick(); settle()
        rule.onNodeWithTag("purchase_next").performClick(); settle()
        rule.onNodeWithTag("purchase_split").performClick(); settle()
        rule.onNodeWithTag("purchase_split_person_2").performClick(); settle()
        rule.onNodeWithTag("purchase_split_done").performClick(); settle()
        rule.onNodeWithTag("purchase_title").performTextInput("Geladeira"); settle()
    }

    private fun salvar() {
        rule.onNodeWithTag("swipe_to_confirm").performTouchInput { swipeRight(durationMillis = 400) }; settle()
    }

    @Test fun igual_100ParaCada() {
        var saved = ""
        abrirGeladeiraDividida { saved = it }
        salvar()
        assertEquals("Geladeira|2000.0|10|Mãe=1000.0,Vó=1000.0", saved)
    }

    @Test fun personalizado_120e80PorMes() {
        var saved = ""
        abrirGeladeiraDividida { saved = it }
        rule.onNodeWithTag("purchase_split_custom").performClick(); settle()
        rule.onNodeWithTag("purchase_share_1").performTextReplacement("120,00")
        rule.onNodeWithTag("purchase_share_2").performTextReplacement("80,00"); settle()
        salvar()
        assertEquals("Geladeira|2000.0|10|Mãe=1200.0,Vó=800.0", saved)
    }

    @Test fun personalizadoQueNaoBate_naoSalva() {
        var saved = ""
        abrirGeladeiraDividida { saved = it }
        rule.onNodeWithTag("purchase_split_custom").performClick(); settle()
        rule.onNodeWithTag("purchase_share_1").performTextReplacement("150,00"); settle()
        rule.onNodeWithTag("swipe_to_confirm").assertDoesNotExist()
        assertEquals("", saved)
    }
}
