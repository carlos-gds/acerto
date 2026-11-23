package com.example.util

/**
 * Teclado-calculadora dos valores (Receber, Nova compra, Dividir conta).
 *
 * A vírgula é automática, como nos apps de banco: os dígitos entram pela
 * direita nos centavos (1 → 0,01 · 12 → 0,12 · 123 → 1,23). Depois de × ou ÷
 * o número é um fator inteiro ("143,00 × 2"), porque multiplicar dinheiro por
 * dinheiro não faz sentido. A tecla "00" acrescenta dois zeros de uma vez.
 *
 * A expressão guarda só dígitos e operadores ("14300×2"). Funções puras, testáveis.
 */
object Calculator {
    const val PLUS = '+'
    const val MINUS = '−'
    const val TIMES = '×'
    const val DIVIDE = '÷'
    const val DOUBLE_ZERO = "00"
    private val OPS = setOf(PLUS, MINUS, TIMES, DIVIDE)
    private const val MAX_MONEY_DIGITS = 10 // até 99.999.999,99
    private const val MAX_FACTOR_DIGITS = 3

    fun isOp(c: Char) = c in OPS

    /** O número sendo digitado agora é um fator (veio depois de × ou ÷)? */
    private fun currentIsFactor(expr: String): Boolean {
        val op = expr.lastOrNull { isOp(it) } ?: return false
        return op == TIMES || op == DIVIDE
    }

    /** Aplica uma tecla ("0".."9", "00", "⌫" ou um operador) à expressão. */
    fun press(expr: String, key: String): String {
        if (key == "⌫") return expr.dropLast(1)
        val current = expr.takeLastWhile { !isOp(it) }
        val limit = if (currentIsFactor(expr)) MAX_FACTOR_DIGITS else MAX_MONEY_DIGITS
        if (key == DOUBLE_ZERO) {
            return if (current.isEmpty() || current.length + 2 > limit) expr else expr + "00"
        }
        val c = key.singleOrNull() ?: return expr
        return when {
            c.isDigit() -> when {
                current.isEmpty() && c == '0' -> expr // zero à esquerda não muda nada
                current.length >= limit -> expr
                else -> expr + c
            }
            isOp(c) -> when {
                expr.isEmpty() -> expr
                isOp(expr.last()) -> expr.dropLast(1) + c
                else -> expr + c
            }
            else -> expr
        }
    }

    fun hasOperator(expr: String) = expr.dropLastWhile { isOp(it) }.any { isOp(it) }

    /** Valor em reais já com centavos, pronto para o teclado: 286.0 → "28600". */
    fun fromAmount(value: Double): String {
        val cents = Math.round(value * 100)
        return if (cents <= 0) "" else cents.toString()
    }

    /** Centavos formatados no padrão brasileiro: 143000 → "1.430,00". */
    fun formatCents(cents: Long): String {
        val reais = cents / 100
        val c = cents % 100
        val grouped = reais.toString().reversed().chunked(3).joinToString(".").reversed()
        return "$grouped,${c.toString().padStart(2, '0')}"
    }

    /**
     * Partes da expressão para mostrar: o primeiro valor ("143,00") e o resto
     * (" × 2", " + 10,00"). Vazio vira "0,00".
     */
    fun displayParts(expr: String): Pair<String, String> {
        if (expr.isEmpty()) return "0,00" to ""
        val tokens = tokenize(expr)
        val first = formatCents(tokens.first().second.toLongOrNull() ?: 0L)
        val rest = StringBuilder()
        for (i in 1 until tokens.size) {
            val (op, digits) = tokens[i]
            rest.append(' ').append(op).append(' ')
            if (digits.isNotEmpty()) {
                if (op == TIMES || op == DIVIDE) rest.append(digits.toLong())
                else rest.append(formatCents(digits.toLong()))
            }
        }
        return first to rest.toString().trim()
    }

    fun display(expr: String): String = displayParts(expr).let { (a, b) -> if (b.isEmpty()) a else "$a $b" }

    /** Lista de (operador antes, dígitos). O primeiro tem operador '\u0000'. */
    private fun tokenize(expr: String): List<Pair<Char, String>> {
        val out = mutableListOf<Pair<Char, String>>()
        var op = '\u0000'
        val sb = StringBuilder()
        for (ch in expr) {
            if (isOp(ch)) {
                out += op to sb.toString()
                sb.clear()
                op = ch
            } else if (ch.isDigit()) sb.append(ch)
        }
        out += op to sb.toString()
        return out
    }

    /** Resultado em reais (operador no fim é ignorado), ou null se vazio/inválido. */
    fun evaluate(expr: String): Double? {
        val e = expr.dropLastWhile { isOp(it) }
        if (e.isEmpty()) return null
        val tokens = tokenize(e)
        // valores em centavos; fatores ficam como inteiros
        val numbers = mutableListOf<Double>()
        val ops = mutableListOf<Char>()
        tokens.forEachIndexed { i, (op, digits) ->
            val n = digits.toLongOrNull() ?: return null
            if (i > 0) ops += op
            numbers += if (op == TIMES || op == DIVIDE) n.toDouble() else n / 100.0
        }
        var i = 0
        while (i < ops.size) {
            val op = ops[i]
            if (op == TIMES || op == DIVIDE) {
                val b = numbers[i + 1]
                if (op == DIVIDE && b == 0.0) return null
                numbers[i] = if (op == TIMES) numbers[i] * b else numbers[i] / b
                numbers.removeAt(i + 1)
                ops.removeAt(i)
            } else i++
        }
        var result = numbers[0]
        ops.forEachIndexed { k, op -> result = if (op == PLUS) result + numbers[k + 1] else result - numbers[k + 1] }
        return Math.round(result * 100) / 100.0
    }
}
