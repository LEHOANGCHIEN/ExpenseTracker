package com.expensetracker.app.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(TransactionType.EXPENSE)
    private val _longPressed = MutableStateFlow<com.expensetracker.app.domain.model.Category?>(null)
    private val _deleteBlocked = MutableStateFlow<com.expensetracker.app.domain.model.Category?>(null)

    private val monthStart: LocalDate = YearMonth.now().atDay(1)
    private val monthEnd: LocalDate = YearMonth.now().atEndOfMonth()

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.observeAll(),
        transactionRepository.observeByDateRange(monthStart, monthEnd),
        preferencesRepository.preferences,
        _selectedTab,
        combine(_longPressed, _deleteBlocked) { lp, db -> lp to db },
    ) { categories, monthTransactions, prefs, tab, (longPressed, deleteBlocked) ->
        val spendingByCat = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val active = categories.filter { !it.isArchived }

        val expenseCats = active
            .filter { it.type == TransactionType.EXPENSE }
            .map { cat -> CategoryWithSpending(cat, spendingByCat[cat.id] ?: 0.0) }

        val incomeCats = active
            .filter { it.type == TransactionType.INCOME }
            .map { cat -> CategoryWithSpending(cat, spendingByCat[cat.id] ?: 0.0) }

        CategoriesUiState(
            expenseCategories = expenseCats,
            incomeCategories = incomeCats,
            selectedTab = tab,
            longPressedCategory = longPressed,
            deleteBlockedCategory = deleteBlocked,
            isLoading = false,
            currency = prefs.currency,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(isLoading = true),
    )

    fun onEvent(event: CategoriesEvent) {
        when (event) {
            is CategoriesEvent.TabSelected -> _selectedTab.value = event.type
            is CategoriesEvent.LongPressCategory -> _longPressed.value = event.category
            CategoriesEvent.DismissActionSheet -> _longPressed.value = null
            is CategoriesEvent.ArchiveCategory -> archiveCategory(event.id)
            is CategoriesEvent.DeleteCategory -> deleteCategory(event.id)
            CategoriesEvent.DismissDeleteBlocked -> _deleteBlocked.value = null
        }
    }

    private fun archiveCategory(id: Long) {
        viewModelScope.launch {
            val cat = categoryRepository.getById(id) ?: return@launch
            categoryRepository.update(cat.copy(isArchived = true))
            _longPressed.value = null
        }
    }

    private fun deleteCategory(id: Long) {
        viewModelScope.launch {
            val count = categoryRepository.getTransactionCount(id)
            if (count > 0) {
                // Block delete — offer archive instead
                val cat = categoryRepository.getById(id)
                _longPressed.value = null
                _deleteBlocked.value = cat
            } else {
                categoryRepository.delete(id)
                _longPressed.value = null
            }
        }
    }
}
