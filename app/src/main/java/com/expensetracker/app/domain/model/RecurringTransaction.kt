package com.expensetracker.app.domain.model

import java.time.LocalDate

data class RecurringTransaction(
    val id: Long = 0,
    val walletId: Long,
    val categoryId: Long,
    val amount: Double,
    val type: TransactionType,
    val note: String,
    val frequency: RecurrenceFrequency,
    val interval: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val nextOccurrence: LocalDate,
    val lastProcessed: LocalDate?,
    val isActive: Boolean,
)
