package com.example.data.backup

import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.Person
import com.example.util.Money

/**
 * Standard versioned schema for local JSON backups in "Quem Me Deve".
 * Uses numeric epoch millisecond timestamps consistently for [createdAt] and [InstallmentBackupDto.paidAt].
 */
data class BackupPayload(
    val backupVersion: Int = 1,
    val appName: String = "Quem Me Deve",
    val createdAt: Long = System.currentTimeMillis(),
    val people: List<PersonBackupDto> = emptyList(),
    val expenses: List<ExpenseBackupDto> = emptyList(),
    val installments: List<InstallmentBackupDto> = emptyList()
)

data class PersonBackupDto(
    val id: Long,
    val name: String,
    val phone: String = "",
    val colorHex: String = "#10B981",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): Person = Person(
        id = id,
        name = name,
        phone = phone,
        colorHex = colorHex,
        createdAt = createdAt
    )

    companion object {
        fun fromEntity(person: Person): PersonBackupDto = PersonBackupDto(
            id = person.id,
            name = person.name,
            phone = person.phone,
            colorHex = person.colorHex,
            createdAt = person.createdAt
        )
    }
}

data class ExpenseBackupDto(
    val id: Long,
    val title: String,
    val totalAmount: Double,
    val type: String,
    val totalInstallments: Int = 1,
    val isRecurring: Boolean = false,
    val startYear: Int,
    val startMonth: Int,
    val personId: Long,
    val myShareAmount: Double = 0.0,
    val otherShareAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): Expense {
        val expenseType = try {
            ExpenseType.valueOf(type)
        } catch (e: Exception) {
            ExpenseType.CARD_PURCHASE
        }
        return Expense(
            id = id,
            title = title,
            totalAmount = Money.fromReal(totalAmount),
            type = expenseType,
            totalInstallments = totalInstallments,
            isRecurring = isRecurring,
            startYear = startYear,
            startMonth = startMonth,
            personId = personId,
            myShareAmount = Money.fromReal(myShareAmount),
            otherShareAmount = Money.fromReal(otherShareAmount),
            notes = notes,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromEntity(expense: Expense): ExpenseBackupDto = ExpenseBackupDto(
            id = expense.id,
            title = expense.title,
            totalAmount = Money.toReal(expense.totalAmount),
            type = expense.type.name,
            totalInstallments = expense.totalInstallments,
            isRecurring = expense.isRecurring,
            startYear = expense.startYear,
            startMonth = expense.startMonth,
            personId = expense.personId,
            myShareAmount = Money.toReal(expense.myShareAmount),
            otherShareAmount = Money.toReal(expense.otherShareAmount),
            notes = expense.notes,
            createdAt = expense.createdAt
        )
    }
}

data class InstallmentBackupDto(
    val id: Long,
    val expenseId: Long,
    val personId: Long,
    val personName: String,
    val expenseTitle: String,
    val expenseType: String,
    val installmentNumber: Int,
    val totalInstallments: Int,
    val year: Int,
    val month: Int,
    val amount: Double,
    val isPaid: Boolean,
    val paidAt: Long? = null,
    val isMyShare: Boolean = false
) {
    fun toEntity(): InstallmentItem {
        val expType = try {
            ExpenseType.valueOf(expenseType)
        } catch (e: Exception) {
            ExpenseType.CARD_PURCHASE
        }
        return InstallmentItem(
            id = id,
            expenseId = expenseId,
            personId = personId,
            personName = personName,
            expenseTitle = expenseTitle,
            expenseType = expType,
            installmentNumber = installmentNumber,
            totalInstallments = totalInstallments,
            year = year,
            month = month,
            amount = Money.fromReal(amount),
            isPaid = isPaid,
            paidAt = paidAt,
            isMyShare = isMyShare
        )
    }

    companion object {
        fun fromEntity(item: InstallmentItem): InstallmentBackupDto = InstallmentBackupDto(
            id = item.id,
            expenseId = item.expenseId,
            personId = item.personId,
            personName = item.personName,
            expenseTitle = item.expenseTitle,
            expenseType = item.expenseType.name,
            installmentNumber = item.installmentNumber,
            totalInstallments = item.totalInstallments,
            year = item.year,
            month = item.month,
            amount = Money.toReal(item.amount),
            isPaid = item.isPaid,
            paidAt = item.paidAt,
            isMyShare = item.isMyShare
        )
    }
}
