package com.expensetracker.app.data.mapper

import com.expensetracker.app.data.local.entity.TransactionEntity
import com.expensetracker.app.domain.model.Transaction

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    walletId = walletId,
    categoryId = categoryId,
    amount = amount,
    type = type,
    note = note,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    photoUri = photoUri,
    location = location,
    recurringId = recurringId,
    parentSplitId = parentSplitId,
    tags = tags,
    toWalletId = toWalletId,
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    walletId = walletId,
    categoryId = categoryId,
    amount = amount,
    type = type,
    note = note,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    photoUri = photoUri,
    location = location,
    recurringId = recurringId,
    parentSplitId = parentSplitId,
    tags = tags,
    toWalletId = toWalletId,
)
