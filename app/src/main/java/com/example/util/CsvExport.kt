package com.example.util

import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Planilha das parcelas para abrir no Excel / Google Planilhas.
 * Usa ";" e vírgula decimal (padrão brasileiro) e BOM para os acentos.
 */
object CsvExport {
    const val BOM = "﻿"
    private val header = listOf("Pessoa", "Descrição", "Tipo", "Parcela", "Mês", "Valor", "Situação", "Pago em")

    fun build(installments: List<InstallmentItem>): String {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val rows = installments
            .sortedWith(compareBy<InstallmentItem>({ it.year }, { it.month }, { it.personName.lowercase() }, { it.installmentNumber }))
            .map { i ->
                listOf(
                    if (i.isMyShare) "Eu" else i.personName,
                    i.expenseTitle,
                    if (i.expenseType == ExpenseType.CARD_PURCHASE) "Cartão" else "Conta dividida",
                    "${i.installmentNumber}/${i.totalInstallments}",
                    "%02d/%04d".format(i.month, i.year),
                    decimal(i.amount),
                    if (i.isPaid) "Pago" else "Pendente",
                    i.paidAt?.let { date.format(Date(it)) } ?: ""
                )
            }
        return BOM + (listOf(header) + rows).joinToString("\r\n") { r -> r.joinToString(";") { cell(it) } } + "\r\n"
    }

    private fun decimal(cents: Long): String {
        val sign = if (cents < 0) "-" else ""
        val abs = kotlin.math.abs(cents)
        return "$sign${abs / 100},${"%02d".format(abs % 100)}"
    }

    private fun cell(v: String): String =
        if (v.any { it == ';' || it == '"' || it == '\n' || it == '\r' }) "\"" + v.replace("\"", "\"\"") + "\"" else v
}
