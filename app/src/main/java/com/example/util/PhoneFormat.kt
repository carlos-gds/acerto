package com.example.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Telefone brasileiro em blocos (Lei de Miller: a memória curta guarda melhor
 * informação dividida em grupos). Só formata a exibição; o valor guardado
 * continua sendo os dígitos.
 *
 * "11987654321" -> "(11) 98765-4321"; "1133334444" -> "(11) 3333-4444".
 */
object PhoneFormat {

    fun format(raw: String): String {
        val d = raw.filter { it.isDigit() }
        if (d.isEmpty() || d.length != raw.trim().length || d.length > 11) return raw
        return buildString {
            d.forEachIndexed { i, c ->
                when {
                    i == 0 -> append('(')
                    i == 2 -> append(") ")
                }
                // Hífen antes dos 4 últimos dígitos quando o número já tem a parte final
                if (d.length >= 10 && i == d.length - 4) append('-')
                append(c)
            }
        }
    }
}

/** VisualTransformation para o campo de telefone (dígitos -> blocos). */
object PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = PhoneFormat.format(raw)
        if (formatted == raw) return TransformedText(text, OffsetMapping.Identity)

        // Posição de cada dígito original dentro do texto formatado
        val positions = IntArray(raw.length)
        var digitIndex = 0
        formatted.forEachIndexed { pos, c ->
            if (c.isDigit() && digitIndex < raw.length) positions[digitIndex++] = pos
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 0 -> if (raw.isEmpty()) 0 else positions[0]
                offset >= raw.length -> formatted.length
                else -> positions[offset]
            }

            override fun transformedToOriginal(offset: Int): Int {
                var count = 0
                for (i in 0 until minOf(offset, formatted.length)) if (formatted[i].isDigit()) count++
                return count.coerceIn(0, raw.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
