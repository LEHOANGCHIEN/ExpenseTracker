package com.expensetracker.app.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.BudgetPeriod
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.BudgetRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<BudgetsUiState> = combine(
        budgetRepository.observeActive(),
        transactionRepository.observeAll(),
        categoryRepository.observeAll(),
        preferencesRepository.preferences,
    ) { budgets, allTransactions, categories, prefs ->
        val catMap = categories.associateBy { it.id }
        val today = LocalDate.now()

        val expenseTxns = allTransactions.filter { it.type == TransactionType.EXPENSE }

        val budgetProgress = budgets.map { budget ->
            val (start, end) = budget.periodDateRange(today)

            val spent = expenseTxns
                .filter { txn ->
                    txn.date >= start && txn.date <= end &&
                        (budget.categoryId == null || txn.categoryId == budget.categoryId)
                }
                .sumOf { it.amount }

            BudgetWithProgress(
                budget = budget,
                category = budget.categoryId?.let { catMap[it] },
                spent = spent,
                periodStart = start,
                periodEnd = end,
            )
        }.sortedByDescending { it.percentage }

        BudgetsUiState(
            budgets = budgetProgress,
            isLoading = false,
            currency = prefs.currency,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetsUiState(isLoading = true),
    )

    fun onEvent(event: BudgetsEvent) {
        when (event) {
            is BudgetsEvent.DeleteBudget -> viewModelScope.launch {
                budgetRepository.delete(event.id)
            }
            is BudgetsEvent.ToggleActive -> viewModelScope.launch {
                budgetRepository.getById(event.id)?.let { budget ->
                    budgetRepository.update(budget.copy(isActive = event.isActive))
                }
            }
        }
    }

    companion object {
        fun com.expensetracker.app.domain.model.Budget.periodDateRange(
            today: LocalDate = LocalDate.now(),
        ): Pair<LocalDate, LocalDate> = when (period) {
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
}
