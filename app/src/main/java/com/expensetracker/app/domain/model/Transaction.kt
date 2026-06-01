package com.expensetracker.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Transaction(
    val id: Long = 0,
    val walletId: Long,
    val categoryId: Long,
    val amount: Double,
    val type: TransactionType,
    val note: String,
    val date: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val photoUri: String?,
    val location: String?,
    val recurringId: Long?,
    val parentSplitId: Long?,
    val tags: List<String>,
    val toWalletId: Long? = null,
)
