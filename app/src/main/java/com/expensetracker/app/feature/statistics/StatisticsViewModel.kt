package com.expensetracker.app.feature.statistics

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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _config = MutableStateFlow(buildConfig(StatPeriod.THIS_MONTH, null, null))
    private val _uiExtras = MutableStateFlow(
        UiExtras(
            period = StatPeriod.THIS_MONTH,
            showCustomStartPicker = false,
            showCustomEndPicker = false,
        ),
    )

    val uiState: StateFlow<StatisticsUiState> = _config
        .flatMapLatest { cfg ->
            combine(
                transactionRepository.observeByDateRange(cfg.start, cfg.end),
                transactionRepository.observeByDateRange(cfg.prevStart, cfg.prevEnd),
                categoryRepository.observeAll(),
                preferencesRepository.preferences,
                _uiExtras,
            ) { txns, prevTxns, cats, prefs, extras ->
                val catMap = cats.associateBy { it.id }
                val expenseTxns = txns.filter { it.type == TransactionType.EXPENSE && it.parentSplitId == null }
                val incomeTxns = txns.filter { it.type == TransactionType.INCOME && it.parentSplitId == null }
                val prevExpense = prevTxns.filter { it.type == TransactionType.EXPENSE && it.parentSplitId == null }.sumOf { it.amount }
                val prevIncome = prevTxns.filter { it.type == TransactionType.INCOME && it.parentSplitId == null }.sumOf { it.amount }

                val dailyExpense = expenseTxns.groupBy { it.date }
                    .mapValues { (_, v) -> v.sumOf { it.amount } }
                val dailyIncome = incomeTxns.groupBy { it.date }
                    .mapValues { (_, v) -> v.sumOf { it.amount } }

                val totalExpense = expenseTxns.sumOf { it.amount }
                val totalIncome = incomeTxns.sumOf { it.amount }

                val catBreakdown = expenseTxns
                    .groupBy { it.categoryId }
                    .entries
                    .sortedByDescending { (_, v) -> v.sumOf { it.amount } }
                    .take(8)
                    .mapNotNull { (catId, catTxns) ->
                        val cat = catMap[catId] ?: return@mapNotNull null
                        val amount = catTxns.sumOf { it.amount }
                        StatCategorySpending(
                            category = cat,
                            amount = amount,
                            percentage = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f,
                        )
                    }

                val topNotes = txns
                    .filter { it.note.isNotBlank() && it.parentSplitId == null }
                    .groupBy { it.note.take(30) }
                    .entries
                    .sortedByDescending { (_, v) -> v.size }
                    .take(5)
                    .map { (note, v) -> note to v.size }

                StatisticsUiState(
                    period = extras.period,
                    startDate = cfg.start,
                    endDate = cfg.end,
                    prevStartDate = cfg.prevStart,
                    prevEndDate = cfg.prevEnd,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    totalNet = totalIncome - totalExpense,
                    prevIncome = prevIncome,
                    prevExpense = prevExpense,
                    dailyExpense = dailyExpense,
                    dailyIncome = dailyIncome,
                    categoryBreakdown = catBreakdown,
                    topNotes = topNotes,
                    isLoading = false,
                    currency = prefs.currency,
                    showCustomStartPicker = extras.showCustomStartPicker,
                    showCustomEndPicker = extras.showCustomEndPicker,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState(isLoading = true),
        )

    fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.PeriodChanged -> {
                val prev = _uiExtras.value
                _uiExtras.update { it.copy(period = event.period) }
                val newCfg = buildConfig(
                    event.period,
                    if (event.period == StatPeriod.CUSTOM) _config.value.start else null,
                    if (event.period == StatPeriod.CUSTOM) _config.value.end else null,
                )
                _config.value = newCfg
            }
            is StatisticsEvent.CustomStartChanged -> {
                val end = _config.value.end
                _config.value = buildConfig(StatPeriod.CUSTOM, event.date, end)
                _uiExtras.update { it.copy(showCustomStartPicker = false) }
            }
            is StatisticsEvent.CustomEndChanged -> {
                val start = _config.value.start
                _config.value = buildConfig(StatPeriod.CUSTOM, start, event.date)
                _uiExtras.update { it.copy(showCustomEndPicker = false) }
            }
            StatisticsEvent.ShowCustomStartPicker -> _uiExtras.update { it.copy(showCustomStartPicker = true) }
            StatisticsEvent.HideCustomStartPicker -> _uiExtras.update { it.copy(showCustomStartPicker = false) }
            StatisticsEvent.ShowCustomEndPicker -> _uiExtras.update { it.copy(showCustomEndPicker = true) }
            StatisticsEvent.HideCustomEndPicker -> _uiExtras.update { it.copy(showCustomEndPicker = false) }
        }
    }

    private data class PeriodConfig(
        val start: LocalDate,
        val end: LocalDate,
        val prevStart: LocalDate,
        val prevEnd: LocalDate,
    )

    private data class UiExtras(
        val period: StatPeriod,
        val showCustomStartPicker: Boolean,
        val showCustomEndPicker: Boolean,
    )

    private fun buildConfig(
        period: StatPeriod,
        customStart: LocalDate?,
        customEnd: LocalDate?,
    ): PeriodConfig {
        val today = LocalDate.now()
        return when (period) {
            StatPeriod.THIS_WEEK -> {
                val dow = today.dayOfWeek.value
                val start = today.minusDays((dow - 1).toLong())
                val end = start.plusDays(6)
                val prevStart = start.minusWeeks(1)
                val prevEnd = end.minusWeeks(1)
                PeriodConfig(start, end, prevStart, prevEnd)
            }
            StatPeriod.THIS_MONTH -> {
                val start = today.withDayOfMonth(1)
                val end = today.withDayOfMonth(today.lengthOfMonth())
                val prevStart = start.minusMonths(1)
                val prevEnd = prevStart.withDayOfMonth(prevStart.lengthOfMonth())
                PeriodConfig(start, end, prevStart, prevEnd)
            }
            StatPeriod.LAST_MONTH -> {
                val prevMonth = today.minusMonths(1)
                val start = prevMonth.withDayOfMonth(1)
                val end = prevMonth.withDayOfMonth(prevMonth.lengthOfMonth())
                val prevStart = start.minusMonths(1)
                val prevEnd = prevStart.withDayOfMonth(prevStart.lengthOfMonth())
                PeriodConfig(start, end, prevStart, prevEnd)
            }
            StatPeriod.THIS_YEAR -> {
                val start = LocalDate.of(today.year, 1, 1)
                val end = LocalDate.of(today.year, 12, 31)
                val prevStart = LocalDate.of(today.year - 1, 1, 1)
                val prevEnd = LocalDate.of(today.year - 1, 12, 31)
                PeriodConfig(start, end, prevStart, prevEnd)
            }
            StatPeriod.CUSTOM -> {
                val start = customStart ?: today.withDayOfMonth(1)
                val end = customEnd ?: today
                val duration = start.until(end).days.toLong()
                val prevEnd = start.minusDays(1)
                val prevStart = prevEnd.minusDays(duration)
                PeriodConfig(start, end, prevStart, prevEnd)
            }
        }
    }
}
