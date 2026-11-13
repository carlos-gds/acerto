package com.example

import com.example.util.PhoneFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneFormatTest {
    @Test
    fun formataEmBlocos() {
        assertEquals("(11) 98765-4321", PhoneFormat.format("11987654321"))
        assertEquals("(11) 3333-4444", PhoneFormat.format("1133334444"))
        assertEquals("(11) 9876", PhoneFormat.format("119876"))
        assertEquals("(1", PhoneFormat.format("1"))
    }

    @Test
    fun mantemTextoQueNaoEhSoDigitos() {
        assertEquals("(11) 9 8765-4321", PhoneFormat.format("(11) 9 8765-4321"))
        assertEquals("+55 11 98765-4321", PhoneFormat.format("+55 11 98765-4321"))
        assertEquals("", PhoneFormat.format(""))
    }
}
