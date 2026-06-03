package com.expensetracker.app.feature.statistics.screen.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.expensetracker.app.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun IncomeVsExpenseBarChart(
    dailyIncome: Map<LocalDate, Double>,
    dailyExpense: Map<LocalDate, Double>,
    startDate: LocalDate,
    endDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(dailyIncome, dailyExpense, startDate, endDate) {
        val days = generateDateRange(startDate, endDate)
        if (days.isEmpty()) return@LaunchedEffect

        // Group by week (or by day if range is <= 14 days)
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val buckets: List<Pair<Double, Double>> = if (totalDays <= 14) {
            days.map { (dailyIncome[it] ?: 0.0) to (dailyExpense[it] ?: 0.0) }
        } else {
            // Group by week
            days.chunked(7).map { week ->
                week.sumOf { dailyIncome[it] ?: 0.0 } to week.sumOf { dailyExpense[it] ?: 0.0 }
            }
        }

        val incomeValues = buckets.map { it.first }
        val expenseValues = buckets.map { it.second }

        if (incomeValues.all { it == 0.0 } && expenseValues.all { it == 0.0 }) return@LaunchedEffect

        modelProducer.runTransaction {
            columnSeries {
                series(y = incomeValues)
                series(y = expenseValues)
            }
        }
    }

    if (dailyIncome.isEmpty() && dailyExpense.isEmpty()) {
        Text(
            text = stringResource(R.string.statistics_no_data_period),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth(),
        )
        return
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
            modelProducer = modelProducer,
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
        )
    }
}
