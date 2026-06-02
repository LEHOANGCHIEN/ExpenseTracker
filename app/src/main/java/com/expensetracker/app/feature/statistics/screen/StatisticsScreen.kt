package com.expensetracker.app.feature.statistics.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.R
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.feature.statistics.StatCategorySpending
import com.expensetracker.app.feature.statistics.StatPeriod
import com.expensetracker.app.feature.statistics.StatisticsEvent
import com.expensetracker.app.feature.statistics.StatisticsUiState
import com.expensetracker.app.feature.statistics.StatisticsViewModel
import com.expensetracker.app.feature.statistics.screen.component.CategoryBreakdownList
import com.expensetracker.app.feature.statistics.screen.component.CategoryPieChart
import com.expensetracker.app.feature.statistics.screen.component.DailySpendHeatmap
import com.expensetracker.app.feature.statistics.screen.component.IncomeVsExpenseBarChart
import com.expensetracker.app.feature.statistics.screen.component.PeriodSelector
import com.expensetracker.app.feature.statistics.screen.component.SpendingTrendChart
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatisticsScreen(
    onNavigateToTransactions: () -> Unit = {},
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    StatisticsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToTransactions = onNavigateToTransactions,
    )

    if (state.showCustomStartPicker) {
        StatDatePickerDialog(
            initialDate = state.startDate,
            onDateSelected = { viewModel.onEvent(StatisticsEvent.CustomStartChanged(it)) },
            onDismiss = { viewModel.onEvent(StatisticsEvent.HideCustomStartPicker) },
        )
    }

    if (state.showCustomEndPicker) {
        StatDatePickerDialog(
            initialDate = state.endDate,
            onDateSelected = { viewModel.onEvent(StatisticsEvent.CustomEndChanged(it)) },
            onDismiss = { viewModel.onEvent(StatisticsEvent.HideCustomEndPicker) },
        )
    }
}

@Composable
private fun StatisticsContent(
    state: StatisticsUiState,
    onEvent: (StatisticsEvent) -> Unit,
    onNavigateToTransactions: () -> Unit,
) {
    val showHeatmap = state.period == StatPeriod.THIS_MONTH || state.period == StatPeriod.LAST_MONTH

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            PeriodSelector(
                selected = state.period,
                onSelect = { onEvent(StatisticsEvent.PeriodChanged(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        if (state.period == StatPeriod.CUSTOM) {
            item {
                CustomDateRangeRow(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onStartClick = { onEvent(StatisticsEvent.ShowCustomStartPicker) },
                    onEndClick = { onEvent(StatisticsEvent.ShowCustomEndPicker) },
                )
            }
        }

        item {
            SummaryCard(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        item {
            ChartSection(title = stringResource(R.string.statistics_spending_trend)) {
                SpendingTrendChart(
                    dailyExpense = state.dailyExpense,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            ChartSection(title = stringResource(R.string.statistics_income_vs_expense)) {
                IncomeVsExpenseBarChart(
                    dailyIncome = state.dailyIncome,
                    dailyExpense = state.dailyExpense,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            CategoryBreakdownSection(
                categories = state.categoryBreakdown,
                totalExpense = state.totalExpense,
                currency = state.currency,
            )
        }

        if (showHeatmap) {
            item {
                ChartSection(title = stringResource(R.string.statistics_daily_heatmap)) {
                    DailySpendHeatmap(
                        dailyExpense = state.dailyExpense,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        onDayClick = { onNavigateToTransactions() },
                    )
                }
            }
        }

        if (state.topNotes.isNotEmpty()) {
            item {
                TopNotesSection(notes = state.topNotes)
            }
        }

        // Refresh Insights button (Feature C)
        item {
            Button(
                onClick = { onEvent(StatisticsEvent.RefreshInsights) },
                enabled = !state.isGeneratingInsights,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                if (state.isGeneratingInsights) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.padding(start = 8.dp))
                }
                Text(if (state.isGeneratingInsights) stringResource(R.string.statistics_generating_insights) else stringResource(R.string.statistics_refresh_insights))
            }
        }
    }
}

@Composable
private fun CustomDateRangeRow(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
) {
    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = onStartClick,
            label = { Text(stringResource(R.string.statistics_from, startDate.format(fmt))) },
        )
        Text("-", style = MaterialTheme.typography.bodyMedium)
        AssistChip(
            onClick = onEndClick,
            label = { Text(stringResource(R.string.statistics_to, endDate.format(fmt))) },
        )
    }
}

@Composable
private fun SummaryCard(
    state: StatisticsUiState,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.statistics_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SummaryItem(
                    label = stringResource(R.string.statistics_income),
                    amount = state.totalIncome,
                    currency = state.currency,
                    color = IncomeGreen,
                    changePct = state.incomeChangePct,
                )
                SummaryItem(
                    label = stringResource(R.string.statistics_expense),
                    amount = state.totalExpense,
                    currency = state.currency,
                    color = ExpenseRed,
                    changePct = state.expenseChangePct,
                )
                SummaryItem(
                    label = stringResource(R.string.statistics_net),
                    amount = state.totalNet,
                    currency = state.currency,
                    color = if (state.totalNet >= 0) IncomeGreen else ExpenseRed,
                    changePct = null,
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Double,
    currency: String,
    color: Color,
    changePct: Double?,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = CurrencyFormatter.formatCompact(amount, currency),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        if (changePct != null) {
            val isPositive = changePct >= 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = if (isPositive) IncomeGreen else ExpenseRed,
                )
                Text(
                    text = "${kotlin.math.abs(changePct).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPositive) IncomeGreen else ExpenseRed,
                )
            }
        }
    }
}

@Composable
private fun ChartSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

@Composable
private fun CategoryBreakdownSection(
    categories: List<StatCategorySpending>,
    totalExpense: Double,
    currency: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.statistics_by_category),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.statistics_no_expense_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            return@Column
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CategoryPieChart(
                categories = categories,
                totalExpense = totalExpense,
                currency = currency,
            )
        }

        Spacer(Modifier.height(12.dp))

        CategoryBreakdownList(
            categories = categories,
            currency = currency,
        )
    }
}

@Composable
private fun TopNotesSection(notes: List<Pair<String, Int>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.statistics_frequent_notes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        notes.forEach { (note, count) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.statistics_times, count),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = initialDate
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(date)
                }
            }) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
