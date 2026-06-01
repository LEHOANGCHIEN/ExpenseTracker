package com.expensetracker.app.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.AiInsight
import com.expensetracker.app.domain.model.Budget
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.AiRepository
import com.expensetracker.app.domain.repository.BudgetRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import com.expensetracker.app.feature.home.state.BudgetProgress
import com.expensetracker.app.feature.home.state.CategorySpending
import com.expensetracker.app.feature.home.state.HomeEvent
import com.expensetracker.app.feature.home.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val aiRepository: AiRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedMonth,
        preferencesRepository.preferences,
    ) { month, prefs -> month to prefs }
        .flatMapLatest { (month, prefs) ->
            val start = month.atDay(1)
            val end = month.atEndOfMonth()

            val monthDataFlow = combine(
                walletRepository.getTotalBalance(),
                transactionRepository.observeByDateRange(start, end),
                transactionRepository.getTotalByType(TransactionType.INCOME, start, end),
                transactionRepository.getTotalByType(TransactionType.EXPENSE, start, end),
            ) { balance, txns, income, expense ->
                MonthData(balance, txns, income, expense)
            }

            val supportDataFlow = combine(
                categoryRepository.observeAll(),
                budgetRepository.observeActive(),
                aiRepository.observeUndismissedInsights(),
            ) { cats, budgets, insights ->
                SupportData(cats, budgets, insights)
            }

            combine(monthDataFlow, supportDataFlow) { monthData, supportData ->
                buildUiState(month, prefs.currency, monthData, supportData)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true),
        )

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ChangeMonth -> _selectedMonth.value = event.month
            is HomeEvent.DismissInsight -> viewModelScope.launch {
                aiRepository.dismissInsight(event.id)
            }
            HomeEvent.RefreshInsights -> viewModelScope.launch {
                _isRefreshing.value = true
                kotlinx.coroutines.delay(800L)
                _isRefreshing.value = false
            }
            HomeEvent.QuickAddClicked -> Unit
        }
    }

    private fun buildUiState(
        month: YearMonth,
        currency: String,
        monthData: MonthData,
        supportData: SupportData,
    ): HomeUiState {
        val categoryMap = supportData.categories.associateBy { it.id }
        val expenseTxns = monthData.transactions.filter { it.type == TransactionType.EXPENSE }
        val totalExpense = expenseTxns.sumOf { it.amount }

        val topCategories = expenseTxns
            .groupBy { it.categoryId }
            .entries
            .sortedByDescending { (_, txns) -> txns.sumOf { it.amount } }
            .take(5)
            .mapNotNull { (catId, catTxns) ->
                val cat = categoryMap[catId] ?: return@mapNotNull null
                val amount = catTxns.sumOf { it.amount }
                CategorySpending(
                    category = cat,
                    amount = amount,
                    percentage = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f,
                )
            }

        val recentTransactions = monthData.transactions
            .sortedByDescending { it.date }
            .take(5)

        val budgetProgressList = supportData.budgets.map { budget ->
            val cat = budget.categoryId?.let { categoryMap[it] }
            val spent = if (budget.categoryId == null) {
                totalExpense
            } else {
                expenseTxns.filter { it.categoryId == budget.categoryId }.sumOf { it.amount }
            }
            BudgetProgress(
                budget = budget,
                category = cat,
                spent = spent,
                percentage = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f,
            )
        }

        return HomeUiState(
            totalBalance = monthData.balance,
            monthIncome = monthData.income,
            monthExpense = monthData.expense,
            monthNet = monthData.income - monthData.expense,
            topCategoriesThisMonth = topCategories,
            recentTransactions = recentTransactions,
            activeBudgets = budgetProgressList,
            latestInsight = supportData.insights.firstOrNull(),
            selectedMonth = month,
            currency = currency,
            categoriesById = categoryMap,
            isLoading = false,
        )
    }

    private data class MonthData(
        val balance: Double,
        val transactions: List<Transaction>,
        val income: Double,
        val expense: Double,
    )

    private data class SupportData(
        val categories: List<Category>,
        val budgets: List<Budget>,
        val insights: List<AiInsight>,
    )
}
