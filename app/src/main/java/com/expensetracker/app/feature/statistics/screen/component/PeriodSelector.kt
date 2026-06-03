package com.expensetracker.app.feature.statistics.screen.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.expensetracker.app.R
import com.expensetracker.app.feature.statistics.StatPeriod

@Composable
fun PeriodSelector(
    selected: StatPeriod,
    onSelect: (StatPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatPeriod.entries.forEach { period ->
            FilterChip(
                selected = period == selected,
                onClick = { onSelect(period) },
                label = { Text(period.label()) },
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun StatPeriod.label(): String = when (this) {
    StatPeriod.THIS_WEEK -> stringResource(R.string.statistics_period_this_week)
    StatPeriod.THIS_MONTH -> stringResource(R.string.statistics_period_this_month)
    StatPeriod.LAST_MONTH -> stringResource(R.string.statistics_period_last_month)
    StatPeriod.THIS_YEAR -> stringResource(R.string.statistics_period_this_year)
    StatPeriod.CUSTOM -> stringResource(R.string.statistics_period_custom)
}
