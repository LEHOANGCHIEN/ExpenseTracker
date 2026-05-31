package com.expensetracker.app.data.mapper

import com.expensetracker.app.data.local.entity.RecurringTransactionEntity
import com.expensetracker.app.domain.model.RecurringTransaction

fun RecurringTransactionEntity.toDomain() = RecurringTransaction(
    id = id,
    walletId = walletId,
    categoryId = categoryId,
    amount = amount,
    type = type,
    note = note,
    frequency = frequency,
    interval = interval,
    startDate = startDate,
    endDate = endDate,
    nextOccurrence = nextOccurrence,
    lastProcessed = lastProcessed,
    isActive = isActive,
)

fun RecurringTransaction.toEntity() = RecurringTransactionEntity(
    id = id,
    walletId = walletId,
    categoryId = categoryId,
    amount = amount,
    type = type,
    note = note,
    frequency = frequency,
    interval = interval,
    startDate = startDate,
    endDate = endDate,
    nextOccurrence = nextOccurrence,
    lastProcessed = lastProcessed,
    isActive = isActive,
)
