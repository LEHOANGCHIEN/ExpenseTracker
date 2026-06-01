package com.expensetracker.app.feature.budget

import com.expensetracker.app.domain.model.Budget
import com.expensetracker.app.domain.model.BudgetPeriod
import com.expensetracker.app.domain.model.Category
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class BudgetWithProgress(
    val budget: Budget,
    val category: Category?,
    val spent: Double,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
) {
    val percentage: Float
        get() = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f

    val remaining: Double get() = budget.amount - spent
    val isExceeded: Boolean get() = spent > budget.amount
    val isAlertTriggered: Boolean get() = !isExceeded && percentage >= budget.alertThreshold.toFloat()

    val daysRemaining: Int
        get() = maxOf(0, LocalDate.now().until(periodEnd, ChronoUnit.DAYS).toInt())

    val totalDays: Int
        get() = maxOf(1, (periodStart.until(periodEnd, ChronoUnit.DAYS) + 1).toInt())

    val daysElapsed: Int get() = maxOf(0, totalDays - daysRemaining)

    val avgDailySpend: Double get() = if (daysElapsed > 0) spent / daysElapsed else 0.0
    val dailyBudgetPace: Double get() = budget.amount / totalDays

    val periodLabel: String
        get() = when (budget.period) {
            BudgetPeriod.WEEKLY -> "This Week"
            BudgetPeriod.MONTHLY -> "This Month"
            BudgetPeriod.YEARLY -> "This Year"
            BudgetPeriod.CUSTOM -> {
                val fmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                "${periodStart.format(fmt)} – ${periodEnd.format(fmt)}"
            }
        }

    val categoryLabel: String
        get() = category?.let {
            if (it.icon.isNotBlank()) "${it.icon} ${it.name}" else it.name
        } ?: "Total Budget"
}

data class BudgetsUiState(
    val budgets: List<BudgetWithProgress> = emptyList(),
    val isLoading: Boolean = true,
    val currency: String = "VND",
)

sealed interface BudgetsEvent {
    data class DeleteBudget(val id: Long) : BudgetsEvent
    data class ToggleActive(val id: Long, val isActive: Boolean) : BudgetsEvent
}
