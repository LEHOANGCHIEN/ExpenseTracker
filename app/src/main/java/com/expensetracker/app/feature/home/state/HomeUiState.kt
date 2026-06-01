package com.expensetracker.app.feature.home.state

import com.expensetracker.app.domain.model.AiInsight
import com.expensetracker.app.domain.model.Budget
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.Transaction
import java.time.YearMonth

data class CategorySpending(
    val category: Category,
    val amount: Double,
    val percentage: Float,
)

data class BudgetProgress(
    val budget: Budget,
    val category: Category?,
    val spent: Double,
    val percentage: Float,
)

data class HomeUiState(
    val totalBalance: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthNet: Double = 0.0,
    val topCategoriesThisMonth: List<CategorySpending> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val activeBudgets: List<BudgetProgress> = emptyList(),
    val latestInsight: AiInsight? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val currency: String = "VND",
    val categoriesById: Map<Long, Category> = emptyMap(),
    val isLoading: Boolean = true,
)

sealed interface HomeEvent {
    data class ChangeMonth(val month: YearMonth) : HomeEvent
    data class DismissInsight(val id: Long) : HomeEvent
    data object RefreshInsights : HomeEvent
    data object QuickAddClicked : HomeEvent
}
