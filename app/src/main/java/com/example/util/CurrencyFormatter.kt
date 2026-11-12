package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val ptBrLocale = Locale.forLanguageTag("pt-BR")
    private val currencyFormat = NumberFormat.getCurrencyInstance(ptBrLocale)

    const val MASKED_VALUE = "R$ ••••••"

    /** Formats a value stored as integer cents. */
    fun format(cents: Long, isPrivacyMode: Boolean = false): String {
        if (isPrivacyMode) return MASKED_VALUE
        return format(cents.toDouble() / 100.0)
    }

    fun format(value: Double, isPrivacyMode: Boolean = false): String {
        if (isPrivacyMode) return MASKED_VALUE
        return try {
            currencyFormat.format(value)
        } catch (e: Exception) {
            "R$ %.2f".format(Locale.US, value).replace(".", ",")
        }
    }

    fun formatWithPrivacy(value: Double, isPrivacyMode: Boolean): String {
        return format(value, isPrivacyMode)
    }

    fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Janeiro"
            2 -> "Fevereiro"
            3 -> "Março"
            4 -> "Abril"
            5 -> "Maio"
            6 -> "Junho"
            7 -> "Julho"
            8 -> "Agosto"
            9 -> "Setembro"
            10 -> "Outubro"
            11 -> "Novembro"
            12 -> "Dezembro"
            else -> "Mês $month"
        }
    }

    fun getMonthShortName(month: Int): String {
        return when (month) {
            1 -> "Jan"
            2 -> "Fev"
            3 -> "Mar"
            4 -> "Abr"
            5 -> "Mai"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Ago"
            9 -> "Set"
            10 -> "Out"
            11 -> "Nov"
            12 -> "Dez"
            else -> "$month"
        }
    }
}
