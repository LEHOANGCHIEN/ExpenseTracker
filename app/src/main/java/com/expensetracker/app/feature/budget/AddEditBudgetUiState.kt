package com.expensetracker.app.feature.budget

import com.expensetracker.app.domain.model.BudgetPeriod
import com.expensetracker.app.domain.model.Category
import java.time.LocalDate

data class AddEditBudgetUiState(
    val budgetId: Long? = null,
    val categoryId: Long? = null,
    val amountText: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    val endDate: LocalDate? = null,
    val alertThreshold: Float = 0.8f,
    val categories: List<Category> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showCategoryDropdown: Boolean = false,
    val error: String? = null,
) {
    val amount: Double get() = amountText.toDoubleOrNull() ?: 0.0
    val canSave: Boolean get() = amount > 0 && !isSaving

    val periodStartDate: LocalDate
        get() {
            val today = LocalDate.now()
            return when (period) {
                BudgetPeriod.WEEKLY -> {
                    val dow = today.dayOfWeek.value
                    today.minusDays((dow - 1).toLong())
                }
                BudgetPeriod.MONTHLY -> today.withDayOfMonth(1)
                BudgetPeriod.YEARLY -> LocalDate.of(today.year, 1, 1)
                BudgetPeriod.CUSTOM -> startDate
            }
        }

    val periodEndDate: LocalDate?
        get() {
            val today = LocalDate.now()
            return when (period) {
                BudgetPeriod.WEEKLY -> periodStartDate.plusDays(6)
                BudgetPeriod.MONTHLY -> today.withDayOfMonth(today.lengthOfMonth())
                BudgetPeriod.YEARLY -> LocalDate.of(today.year, 12, 31)
                BudgetPeriod.CUSTOM -> endDate
            }
        }

    val categoryLabel: String
        get() = categoryId?.let { id ->
            categories.find { it.id == id }?.let { cat ->
                if (cat.icon.isNotBlank()) "${cat.icon} ${cat.name}" else cat.name
            }
        } ?: "Overall (all expenses)"
}

sealed interface AddEditBudgetEvent {
    data class CategorySelected(val id: Long?) : AddEditBudgetEvent
    data class AmountChanged(val text: String) : AddEditBudgetEvent
    data class PeriodChanged(val period: BudgetPeriod) : AddEditBudgetEvent
    data class StartDateChanged(val date: LocalDate) : AddEditBudgetEvent
    data class EndDateChanged(val date: LocalDate) : AddEditBudgetEvent
    data class AlertThresholdChanged(val value: Float) : AddEditBudgetEvent
    data object ShowCategoryDropdown : AddEditBudgetEvent
    data object HideCategoryDropdown : AddEditBudgetEvent
    data object ShowStartDatePicker : AddEditBudgetEvent
    data object HideStartDatePicker : AddEditBudgetEvent
    data object ShowEndDatePicker : AddEditBudgetEvent
    data object HideEndDatePicker : AddEditBudgetEvent
    data object Save : AddEditBudgetEvent
}
