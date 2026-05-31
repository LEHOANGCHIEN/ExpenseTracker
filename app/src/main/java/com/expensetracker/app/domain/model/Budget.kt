package com.expensetracker.app.domain.model

import java.time.LocalDate

data class Budget(
    val id: Long = 0,
    val categoryId: Long?,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val alertThreshold: Double,
    val isActive: Boolean,
)
