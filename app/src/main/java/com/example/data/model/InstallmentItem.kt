package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "installments",
    foreignKeys = [
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["expenseId"]),
        Index(value = ["year", "month"]),
        Index(value = ["personId"])
    ]
)
data class InstallmentItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expenseId: Long,
    val personId: Long,
    val personName: String,
    val expenseTitle: String,
    val expenseType: ExpenseType,
    val installmentNumber: Int,
    val totalInstallments: Int,
    val year: Int,
    val month: Int, // 1 to 12
    /** Amount stored as integer cents. */
    val amount: Long,
    val isPaid: Boolean = false,
    val paidAt: Long? = null,
    val isMyShare: Boolean = false
)
