package com.expensetracker.app.feature.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.Budget
import com.expensetracker.app.domain.model.BudgetPeriod
import com.expensetracker.app.domain.repository.BudgetRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddEditBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val budgetId: Long? = savedStateHandle.get<Long?>("id")

    private val _state = MutableStateFlow(AddEditBudgetUiState())
    val state: StateFlow<AddEditBudgetUiState> = _state

    init {
        viewModelScope.launch {
            val cats = categoryRepository.observeAll().first()
            if (budgetId != null) {
                val existing = budgetRepository.getById(budgetId)
                if (existing != null) {
                    _state.update {
                        it.copy(
                            budgetId = existing.id,
                            categoryId = existing.categoryId,
                            amountText = formatAmount(existing.amount),
                            period = existing.period,
                            startDate = existing.startDate,
                            endDate = existing.endDate,
                            alertThreshold = existing.alertThreshold.toFloat(),
                            categories = cats,
                            isEditing = true,
                        )
                    }
                }
            } else {
                _state.update { it.copy(categories = cats) }
            }
        }
    }

    fun onEvent(event: AddEditBudgetEvent) {
        when (event) {
            is AddEditBudgetEvent.CategorySelected -> _state.update {
                it.copy(categoryId = event.id, showCategoryDropdown = false)
            }
            is AddEditBudgetEvent.AmountChanged -> _state.update {
                it.copy(amountText = event.text)
            }
            is AddEditBudgetEvent.PeriodChanged -> _state.update {
                val today = LocalDate.now()
                val newStart = when (event.period) {
                    BudgetPeriod.WEEKLY -> {
                        val dow = today.dayOfWeek.value
                        today.minusDays((dow - 1).toLong())
                    }
                    BudgetPeriod.MONTHLY -> today.withDayOfMonth(1)
                    BudgetPeriod.YEARLY -> LocalDate.of(today.year, 1, 1)
                    BudgetPeriod.CUSTOM -> it.startDate
                }
                it.copy(period = event.period, startDate = newStart)
            }
            is AddEditBudgetEvent.StartDateChanged -> _state.update {
                it.copy(startDate = event.date, showStartDatePicker = false)
            }
            is AddEditBudgetEvent.EndDateChanged -> _state.update {
                it.copy(endDate = event.date, showEndDatePicker = false)
            }
            is AddEditBudgetEvent.AlertThresholdChanged -> _state.update {
                it.copy(alertThreshold = event.value)
            }
            AddEditBudgetEvent.ShowCategoryDropdown -> _state.update { it.copy(showCategoryDropdown = true) }
            AddEditBudgetEvent.HideCategoryDropdown -> _state.update { it.copy(showCategoryDropdown = false) }
            AddEditBudgetEvent.ShowStartDatePicker -> _state.update { it.copy(showStartDatePicker = true) }
            AddEditBudgetEvent.HideStartDatePicker -> _state.update { it.copy(showStartDatePicker = false) }
            AddEditBudgetEvent.ShowEndDatePicker -> _state.update { it.copy(showEndDatePicker = true) }
            AddEditBudgetEvent.HideEndDatePicker -> _state.update { it.copy(showEndDatePicker = false) }
            AddEditBudgetEvent.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value
        if (s.amount <= 0) return
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val budget = Budget(
                    id = s.budgetId ?: 0L,
                    categoryId = s.categoryId,
                    amount = s.amount,
                    period = s.period,
                    startDate = s.periodStartDate,
                    endDate = s.periodEndDate,
                    alertThreshold = s.alertThreshold.toDouble(),
                    isActive = true,
                )
                if (s.isEditing) budgetRepository.update(budget)
                else budgetRepository.add(budget)
                _state.update { it.copy(isSaving = false, isDone = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private fun formatAmount(amount: Double): String =
        if (amount == amount.toLong().toDouble()) amount.toLong().toString()
        else amount.toString()
}
