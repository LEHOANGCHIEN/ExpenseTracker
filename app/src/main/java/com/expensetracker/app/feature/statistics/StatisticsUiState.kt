package com.expensetracker.app.feature.statistics

import com.expensetracker.app.domain.model.Category
import java.time.LocalDate

enum class StatPeriod { THIS_WEEK, THIS_MONTH, LAST_MONTH, THIS_YEAR, CUSTOM }

data class StatCategorySpending(
    val category: Category,
    val amount: Double,
    val percentage: Float,
)

data class StatisticsUiState(
    val period: StatPeriod = StatPeriod.THIS_MONTH,
    val startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    val endDate: LocalDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
    val prevStartDate: LocalDate = LocalDate.now().minusMonths(1).withDayOfMonth(1),
    val prevEndDate: LocalDate = LocalDate.now().minusMonths(1).withDayOfMonth(
        LocalDate.now().minusMonths(1).lengthOfMonth(),
    ),

    // Summary
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalNet: Double = 0.0,
    val prevIncome: Double = 0.0,
    val prevExpense: Double = 0.0,

    // Daily expense amounts indexed by date
    val dailyExpense: Map<LocalDate, Double> = emptyMap(),
    val dailyIncome: Map<LocalDate, Double> = emptyMap(),

    // Category breakdown (expenses)
    val categoryBreakdown: List<StatCategorySpending> = emptyList(),

    // Top notes/merchants (note text → frequency count)
    val topNotes: List<Pair<String, Int>> = emptyList(),

    val isLoading: Boolean = true,
    val currency: String = "VND",

    // Custom period date picker state
    val showCustomStartPicker: Boolean = false,
    val showCustomEndPicker: Boolean = false,
) {
    val incomeChangePct: Double
        get() = if (prevIncome == 0.0) 0.0 else ((totalIncome - prevIncome) / prevIncome) * 100

    val expenseChangePct: Double
        get() = if (prevExpense == 0.0) 0.0 else ((totalExpense - prevExpense) / prevExpense) * 100
}

sealed interface StatisticsEvent {
    data class PeriodChanged(val period: StatPeriod) : StatisticsEvent
    data class CustomStartChanged(val date: LocalDate) : StatisticsEvent
    data class CustomEndChanged(val date: LocalDate) : StatisticsEvent
    data object ShowCustomStartPicker : StatisticsEvent
    data object HideCustomStartPicker : StatisticsEvent
    data object ShowCustomEndPicker : StatisticsEvent
    data object HideCustomEndPicker : StatisticsEvent
}
