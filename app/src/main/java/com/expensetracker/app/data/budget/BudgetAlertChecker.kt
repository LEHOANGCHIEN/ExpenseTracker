package com.expensetracker.app.data.budget

import com.expensetracker.app.domain.model.Budget
import com.expensetracker.app.domain.model.BudgetPeriod
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.BudgetRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Foundation class for budget threshold alerts.
 * Called after a transaction is inserted; returns budgets that have crossed
 * their alert threshold. Full notification dispatch is wired in Task 17.
 */
@Singleton
class BudgetAlertChecker @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend fun checkThresholdsForCategory(categoryId: Long): List<Budget> {
        val activeBudgets = budgetRepository.observeActive().first()
        val today = LocalDate.now()

        return activeBudgets
            .filter { budget ->
                budget.categoryId == null || budget.categoryId == categoryId
            }
            .filter { budget ->
                val (start, end) = budget.periodDateRange(today)
                val spent = transactionRepository.observeAll().first()
                    .filter { txn ->
                        txn.type == TransactionType.EXPENSE &&
                            txn.date >= start && txn.date <= end &&
                            (budget.categoryId == null || txn.categoryId == budget.categoryId)
                    }
                    .sumOf { it.amount }

                val percentage = if (budget.amount > 0) spent / budget.amount else 0.0
                percentage >= budget.alertThreshold
            }
    }

    private fun Budget.periodDateRange(today: LocalDate): Pair<LocalDate, LocalDate> =
        when (period) {
            BudgetPeriod.WEEKLY -> {
                val dow = today.dayOfWeek.value
                val start = today.minusDays((dow - 1).toLong())
                start to start.plusDays(6)
            }
            BudgetPeriod.MONTHLY ->
                today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
            BudgetPeriod.YEARLY ->
                LocalDate.of(today.year, 1, 1) to LocalDate.of(today.year, 12, 31)
            BudgetPeriod.CUSTOM ->
                startDate to (endDate ?: today)
        }
}
