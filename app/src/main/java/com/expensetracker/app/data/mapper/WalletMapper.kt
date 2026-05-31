package com.expensetracker.app.data.mapper

import com.expensetracker.app.data.local.entity.WalletEntity
import com.expensetracker.app.domain.model.Wallet

fun WalletEntity.toDomain() = Wallet(
    id = id,
    name = name,
    icon = icon,
    color = color,
    initialBalance = initialBalance,
    currency = currency,
    createdAt = createdAt,
)

fun Wallet.toEntity() = WalletEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    initialBalance = initialBalance,
    currency = currency,
    createdAt = createdAt,
)
