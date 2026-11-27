package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
enum class ExpenseType {
    CARD_PURCHASE, // Someone bought something on my card
    SHARED_BILL    // Shared monthly/recurring or one-off bill (e.g., Internet, Light)
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    /** Total stored as integer cents. */
    val totalAmount: Long,
    val type: ExpenseType,
    val totalInstallments: Int = 1,
    val isRecurring: Boolean = false,
    val startYear: Int,
    val startMonth: Int, // 1 to 12
    val personId: Long,  // For CARD_PURCHASE: the person who bought. For SHARED_BILL: the person sharing with me
    val myShareAmount: Long = 0L,
    val otherShareAmount: Long = 0L,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    /**
     * Compra dividida entre várias pessoas: cada pessoa tem a própria compra (com a parte
     * dela e as próprias parcelas) e todas as partes têm o mesmo groupId. null = compra solta.
     */
    val groupId: Long? = null
)

/**
 * Identifier representing the user themselves ("Eu") in shared bill splits and participants.
 */
const val MY_PERSON_ID: Long = 0L

data class BillParticipantShare(
    val personId: Long = MY_PERSON_ID, // MY_PERSON_ID for "Eu", person.id for others
    val personName: String,
    val amount: Double,
    val isMyShare: Boolean
)
