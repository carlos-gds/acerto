package com.example.data.backup

import com.example.data.model.ExpenseType
import com.example.data.model.MY_PERSON_ID

sealed class BackupValidationResult {
    data object Valid : BackupValidationResult()
    data class Invalid(val reason: String) : BackupValidationResult()
}

object BackupValidator {

    private const val SUPPORTED_VERSION = 1

    fun validate(payload: BackupPayload): BackupValidationResult {
        // 1. Version check
        if (payload.backupVersion != SUPPORTED_VERSION) {
            return BackupValidationResult.Invalid(
                "Versão de backup não suportada: ${payload.backupVersion}. Versão esperada: $SUPPORTED_VERSION."
            )
        }

        // 2. App name check
        if (payload.appName.isBlank()) {
            return BackupValidationResult.Invalid("Identificador do aplicativo ausente ou inválido no backup.")
        }

        // 3. Duplicate IDs in People
        val personIds = mutableSetOf<Long>()
        for (person in payload.people) {
            if (person.name.isBlank()) {
                return BackupValidationResult.Invalid("Existe uma pessoa com nome em branco no arquivo de backup.")
            }
            if (!personIds.add(person.id)) {
                return BackupValidationResult.Invalid("ID de pessoa duplicado no backup: ${person.id}.")
            }
        }

        // 4. Duplicate IDs and integrity in Expenses
        val expenseIds = mutableSetOf<Long>()
        val validPersonIds = personIds + setOf(MY_PERSON_ID)

        for (expense in payload.expenses) {
            if (expense.title.isBlank()) {
                return BackupValidationResult.Invalid("Existe uma despesa com título em branco no arquivo de backup.")
            }
            if (!expenseIds.add(expense.id)) {
                return BackupValidationResult.Invalid("ID de despesa duplicado no backup: ${expense.id}.")
            }
            if (expense.totalAmount < 0.0 || expense.myShareAmount < 0.0 || expense.otherShareAmount < 0.0) {
                return BackupValidationResult.Invalid(
                    "Despesa '${expense.title}' possui valores negativos indevidos."
                )
            }
            if (expense.totalInstallments < 1) {
                return BackupValidationResult.Invalid(
                    "Despesa '${expense.title}' possui quantidade de parcelas menor que 1 (${expense.totalInstallments})."
                )
            }
            if (expense.startMonth !in 1..12) {
                return BackupValidationResult.Invalid(
                    "Despesa '${expense.title}' possui mês de início inválido (${expense.startMonth}). Deve ser entre 1 e 12."
                )
            }
            val isValidType = try {
                ExpenseType.valueOf(expense.type)
                true
            } catch (e: Exception) {
                false
            }
            if (!isValidType) {
                return BackupValidationResult.Invalid(
                    "Despesa '${expense.title}' possui tipo desconhecido: '${expense.type}'."
                )
            }
            if (expense.personId !in validPersonIds) {
                return BackupValidationResult.Invalid(
                    "Despesa '${expense.title}' referencia pessoa inexistente no backup (personId: ${expense.personId})."
                )
            }
        }

        // 5. Duplicate IDs and integrity in Installments
        val installmentIds = mutableSetOf<Long>()
        for (inst in payload.installments) {
            if (!installmentIds.add(inst.id)) {
                return BackupValidationResult.Invalid("ID de parcela duplicado no backup: ${inst.id}.")
            }
            if (inst.amount < 0.0) {
                return BackupValidationResult.Invalid("Parcela ${inst.id} possui valor negativo: ${inst.amount}.")
            }
            if (inst.installmentNumber < 1 || inst.totalInstallments < 1) {
                return BackupValidationResult.Invalid(
                    "Parcela ${inst.id} possui numeração ou total de parcelas inválido (${inst.installmentNumber}/${inst.totalInstallments})."
                )
            }
            if (inst.month !in 1..12) {
                return BackupValidationResult.Invalid(
                    "Parcela ${inst.id} possui mês inválido: ${inst.month}. Deve ser entre 1 e 12."
                )
            }
            val isValidType = try {
                ExpenseType.valueOf(inst.expenseType)
                true
            } catch (e: Exception) {
                false
            }
            if (!isValidType) {
                return BackupValidationResult.Invalid(
                    "Parcela ${inst.id} possui tipo de despesa desconhecido: '${inst.expenseType}'."
                )
            }
            if (inst.expenseId !in expenseIds) {
                return BackupValidationResult.Invalid(
                    "Parcela ${inst.id} referencia despesa inexistente no backup (expenseId: ${inst.expenseId})."
                )
            }
            if (inst.personId !in validPersonIds) {
                return BackupValidationResult.Invalid(
                    "Parcela ${inst.id} referencia pessoa inexistente no backup (personId: ${inst.personId})."
                )
            }
        }

        return BackupValidationResult.Valid
    }
}
