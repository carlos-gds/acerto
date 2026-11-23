package com.example

import com.example.util.Calculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorTest {
    private fun type(vararg keys: String) = keys.fold("") { e, k -> Calculator.press(e, k) }

    @Test
    fun virgulaAutomatica() {
        assertEquals("0,01", Calculator.display(type("1")))
        assertEquals("0,12", Calculator.display(type("1", "2")))
        assertEquals("1,23", Calculator.display(type("1", "2", "3")))
        assertEquals("1.234,56", Calculator.display(type("1", "2", "3", "4", "5", "6")))
        assertEquals(1.23, Calculator.evaluate(type("1", "2", "3"))!!, 0.001)
        assertEquals("0,00", Calculator.display(""))
    }

    @Test
    fun teclaDuploZero_eZeroAEsquerda() {
        assertEquals("5,00", Calculator.display(type("5", "00")))
        assertEquals("", type("0", "00")) // nada para multiplicar por cem
        assertEquals("150,00", Calculator.display(type("1", "5", "0", "00")))
    }

    @Test
    fun contaComoNoVideo_fatorInteiro() {
        val e = type("1", "4", "3", "00", "×", "2")
        assertEquals("143,00 × 2", Calculator.display(e))
        assertEquals(286.0, Calculator.evaluate(e)!!, 0.001)
    }

    @Test
    fun precedencia_eSomaDeValores() {
        // 10,00 + 5,00 × 3 = 25,00
        assertEquals(25.0, Calculator.evaluate(type("1", "0", "00", "+", "5", "00", "×", "3"))!!, 0.001)
        assertEquals(100.0, Calculator.evaluate(type("3", "0", "0", "00", "÷", "3"))!!, 0.001)
    }

    @Test
    fun operadoresSeguidosEApagar() {
        assertEquals("5−", type("5", "+", "−"))
        assertEquals("", type("+"))
        assertEquals("5", type("5", "+", "⌫"))
        assertEquals(0.05, Calculator.evaluate("5+")!!, 0.001) // operador no fim é ignorado
        assertNull(Calculator.evaluate("5÷0"))
        assertEquals("5÷", type("5", "÷", "0")) // fator não começa com zero
        assertNull(Calculator.evaluate(""))
    }

    @Test
    fun limites_eValorPronto() {
        assertEquals("1234567890", type(*Array(12) { ((it + 1) % 10).toString() }))
        assertEquals("28600", Calculator.fromAmount(286.0))
        assertEquals("9533", Calculator.fromAmount(95.333))
    }
}
