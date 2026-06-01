package com.expensetracker.app.feature.recurring

import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.RecurrenceFrequency
import com.expensetracker.app.domain.model.RecurringTransaction
import com.expensetracker.app.domain.model.Wallet

data class RecurringScheduleItem(
    val schedule: RecurringTransaction,
    val category: Category?,
    val wallet: Wallet?,
) {
    val frequencyDescription: String
        get() {
            val interval = schedule.interval
            val unit = when (schedule.frequency) {
                RecurrenceFrequency.DAILY -> if (interval == 1) "day" else "days"
                RecurrenceFrequency.WEEKLY -> if (interval == 1) "week" else "weeks"
                RecurrenceFrequency.MONTHLY -> if (interval == 1) "month" else "months"
                RecurrenceFrequency.YEARLY -> if (interval == 1) "year" else "years"
            }
            return if (interval == 1) "Every $unit" else "Every $interval $unit"
        }
}

data class RecurringListUiState(
    val schedules: List<RecurringScheduleItem> = emptyList(),
    val longPressedItem: RecurringScheduleItem? = null,
    val isLoading: Boolean = true,
    val currency: String = "VND",
)

sealed interface RecurringListEvent {
    data class LongPress(val item: RecurringScheduleItem) : RecurringListEvent
    data object DismissActionSheet : RecurringListEvent
    data class ToggleActive(val id: Long, val isActive: Boolean) : RecurringListEvent
    data class Delete(val id: Long) : RecurringListEvent
}
