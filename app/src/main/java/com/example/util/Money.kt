package com.example.util

import kotlin.math.roundToLong

/**
 * Regras centrais para valores monetários armazenados em centavos.
 * A interface pode continuar trabalhando com Double nos campos de entrada,
 * mas o armazenamento persistente deve usar Long.
 */
object Money {
    const val CENTS_PER_REAL: Long = 100L

    fun fromReal(value: Double): Long {
        require(value.isFinite()) { "Valor monetário inválido: $value" }
        return (value * CENTS_PER_REAL).roundToLong()
    }

    fun toReal(cents: Long): Double = cents.toDouble() / CENTS_PER_REAL.toDouble()

    fun format(cents: Long, isPrivacyMode: Boolean = false): String =
        CurrencyFormatter.format(cents, isPrivacyMode)
}
