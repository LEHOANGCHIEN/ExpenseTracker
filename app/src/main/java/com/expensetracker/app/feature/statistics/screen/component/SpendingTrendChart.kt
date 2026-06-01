package com.expensetracker.app.feature.statistics.screen.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SpendingTrendChart(
    dailyExpense: Map<LocalDate, Double>,
    startDate: LocalDate,
    endDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(dailyExpense, startDate, endDate) {
        val days = generateDateRange(startDate, endDate)
        if (days.isEmpty()) return@LaunchedEffect
        val yValues = days.map { dailyExpense[it] ?: 0.0 }
        modelProducer.runTransaction {
            lineSeries { series(y = yValues) }
        }
    }

    if (dailyExpense.isEmpty()) {
        Text(
            text = "No spending data",
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
                rememberLineCartesianLayer(),
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

internal fun generateDateRange(start: LocalDate, end: LocalDate): List<LocalDate> {
    val result = mutableListOf<LocalDate>()
    var current = start
    while (!current.isAfter(end)) {
        result.add(current)
        current = current.plusDays(1)
    }
    return result
}
