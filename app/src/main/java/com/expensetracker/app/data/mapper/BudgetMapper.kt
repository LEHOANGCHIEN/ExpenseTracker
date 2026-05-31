package com.expensetracker.app.data.mapper

import com.expensetracker.app.data.local.entity.BudgetEntity
import com.expensetracker.app.domain.model.Budget

fun BudgetEntity.toDomain() = Budget(
    id = id,
    categoryId = categoryId,
    amount = amount,
    period = period,
    startDate = startDate,
    endDate = endDate,
    alertThreshold = alertThreshold,
    isActive = isActive,
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    categoryId = categoryId,
    amount = amount,
    period = period,
    startDate = startDate,
    endDate = endDate,
    alertThreshold = alertThreshold,
    isActive = isActive,
)
