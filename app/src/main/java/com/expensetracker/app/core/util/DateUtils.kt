package com.expensetracker.app.core.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val shortFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    private val mediumFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val shortMonthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())

    fun formatRelative(
        date: LocalDate,
        today: LocalDate = LocalDate.now(),
        todayLabel: String = "Today",
        yesterdayLabel: String = "Yesterday",
        tomorrowLabel: String = "Tomorrow",
    ): String = when {
        date == today -> todayLabel
        date == today.minusDays(1) -> yesterdayLabel
        date == today.plusDays(1) -> tomorrowLabel
        date.year == today.year -> date.format(shortFormatter)
        else -> date.format(mediumFormatter)
    }

    fun formatMonth(yearMonth: YearMonth): String = yearMonth.format(monthFormatter)

    fun formatMonthShort(yearMonth: YearMonth): String = yearMonth.format(shortMonthFormatter)

    fun monthRange(yearMonth: YearMonth): Pair<LocalDate, LocalDate> =
        Pair(yearMonth.atDay(1), yearMonth.atEndOfMonth())

    fun weekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
        val dayOfWeek = date.dayOfWeek.value
        val start = date.minusDays((dayOfWeek - 1).toLong())
        val end = start.plusDays(6)
        return Pair(start, end)
    }

    fun currentYearRange(): Pair<LocalDate, LocalDate> {
        val now = LocalDate.now()
        return Pair(LocalDate.of(now.year, 1, 1), LocalDate.of(now.year, 12, 31))
    }

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun isSameMonth(date: LocalDate, yearMonth: YearMonth): Boolean =
        date.year == yearMonth.year && date.monthValue == yearMonth.monthValue
}
