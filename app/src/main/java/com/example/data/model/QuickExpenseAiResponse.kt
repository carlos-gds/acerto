package com.example.data.model

data class QuickExpenseAiResponse(
    val description: String,
    val total_amount: Double,
    val installments: Int,
    val target_person: String,
    val start_month: Int? = null,
    val start_year: Int? = null
)
