package com.example.util

/**
 * Leitura tolerante de valores digitados pelo usuário (Lei de Postel:
 * "seja flexível no que aceita"). Só interpreta o texto do campo; o valor
 * resultante segue o mesmo caminho de antes (Double -> centavos no Repository).
 *
 * Aceita, por exemplo: "50", "50,5", "50.5", "1.234,56", "1,234.56",
 * "R$ 1.234,56", "1 234,56", "1.500" (milhar no padrão brasileiro).
 */
object MoneyInput {

    /** Retorna o valor em reais, ou null se o texto não for um valor válido e não negativo. */
    fun parse(text: String?): Double? {
        if (text == null) return null
        var s = text.trim()
            .replace("R$", "", ignoreCase = true)
            .replace(" ", "")
            .replace(" ", "")
        if (s.isEmpty()) return null
        if (s.any { !(it.isDigit() || it == ',' || it == '.') }) return null

        val lastComma = s.lastIndexOf(',')
        val lastDot = s.lastIndexOf('.')
        s = when {
            // Os dois separadores: o último é o decimal, o outro é milhar.
            lastComma >= 0 && lastDot >= 0 -> {
                if (lastComma > lastDot) s.replace(".", "").replace(',', '.')
                else s.replace(",", "")
            }
            // Só vírgula: decimal no padrão brasileiro (várias vírgulas = milhar).
            lastComma >= 0 -> {
                if (s.count { it == ',' } > 1) s.replace(",", "") else s.replace(',', '.')
            }
            // Só ponto: vários pontos = milhar; "1.500" (3 dígitos após) = milhar; senão decimal.
            lastDot >= 0 -> {
                val dots = s.count { it == '.' }
                val digitsAfter = s.length - lastDot - 1
                if (dots > 1 || digitsAfter == 3) s.replace(".", "") else s
            }
            else -> s
        }
        val value = s.toDoubleOrNull() ?: return null
        return if (value.isFinite() && value >= 0.0) value else null
    }
}
