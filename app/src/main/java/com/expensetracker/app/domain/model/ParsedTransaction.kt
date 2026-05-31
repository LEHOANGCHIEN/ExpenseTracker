package com.expensetracker.app.domain.model

import java.time.LocalDate

data class ParsedTransaction(
    val amount: Double,
    val categoryId: Long?,
    val note: String,
    val date: LocalDate,
    val type: TransactionType,
)
