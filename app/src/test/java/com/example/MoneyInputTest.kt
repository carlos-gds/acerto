package com.example

import com.example.util.MoneyInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyInputTest {

    private fun assertParses(expected: Double, input: String) {
        assertEquals("entrada: '$input'", expected, MoneyInput.parse(input)!!, 0.0001)
    }

    @Test
    fun aceitaFormatosComuns() {
        assertParses(50.0, "50")
        assertParses(50.5, "50,5")
        assertParses(50.5, "50.5")
        assertParses(1234.56, "1.234,56")
        assertParses(1234.56, "1,234.56")
        assertParses(1234.56, "R$ 1.234,56")
        assertParses(1234.56, "1 234,56")
        assertParses(1500.0, "1.500")
        assertParses(1234567.0, "1.234.567")
        assertParses(0.99, "0,99")
        assertParses(300.0, " R$300 ")
    }

    @Test
    fun rejeitaEntradasInvalidas() {
        assertNull(MoneyInput.parse(""))
        assertNull(MoneyInput.parse("   "))
        assertNull(MoneyInput.parse(null))
        assertNull(MoneyInput.parse("abc"))
        assertNull(MoneyInput.parse("-50"))
        assertNull(MoneyInput.parse("12a,5"))
    }
}
