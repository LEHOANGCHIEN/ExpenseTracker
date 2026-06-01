package com.expensetracker.app.feature.recurring

import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.RecurrenceFrequency
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.model.Wallet
import java.time.LocalDate

data class AddEditRecurringUiState(
    val scheduleId: Long? = null,
    val walletId: Long? = null,
    val categoryId: Long? = null,
    val amountText: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val note: String = "",
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val interval: Int = 1,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val error: String? = null,
) {
    val amount: Double get() = amountText.toDoubleOrNull() ?: 0.0
    val canSave: Boolean get() = amount > 0 && categoryId != null && !isSaving

    val filteredCategories: List<Category>
        get() = categories.filter { !it.isArchived && it.type == type }

    val frequencyLabel: String
        get() {
            val unit = when (frequency) {
                RecurrenceFrequency.DAILY -> if (interval == 1) "day" else "days"
                RecurrenceFrequency.WEEKLY -> if (interval == 1) "week" else "weeks"
                RecurrenceFrequency.MONTHLY -> if (interval == 1) "month" else "months"
                RecurrenceFrequency.YEARLY -> if (interval == 1) "year" else "years"
            }
            return if (interval == 1) "Every $unit" else "Every $interval $unit"
        }
}

sealed interface AddEditRecurringEvent {
    data class AmountChanged(val text: String) : AddEditRecurringEvent
    data class TypeChanged(val type: TransactionType) : AddEditRecurringEvent
    data class CategorySelected(val id: Long) : AddEditRecurringEvent
    data class WalletSelected(val id: Long) : AddEditRecurringEvent
    data class NoteChanged(val note: String) : AddEditRecurringEvent
    data class FrequencyChanged(val freq: RecurrenceFrequency) : AddEditRecurringEvent
    data class IntervalChanged(val interval: Int) : AddEditRecurringEvent
    data class StartDateChanged(val date: LocalDate) : AddEditRecurringEvent
    data class EndDateChanged(val date: LocalDate?) : AddEditRecurringEvent
    data object ShowCategoryPicker : AddEditRecurringEvent
    data object HideCategoryPicker : AddEditRecurringEvent
    data object ShowStartDatePicker : AddEditRecurringEvent
    data object HideStartDatePicker : AddEditRecurringEvent
    data object ShowEndDatePicker : AddEditRecurringEvent
    data object HideEndDatePicker : AddEditRecurringEvent
    data object Save : AddEditRecurringEvent
}
