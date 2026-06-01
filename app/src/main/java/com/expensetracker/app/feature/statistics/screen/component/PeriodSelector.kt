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
import androidx.compose.ui.unit.dp
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
                label = { Text(period.label) },
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

private val StatPeriod.label: String
    get() = when (this) {
        StatPeriod.THIS_WEEK -> "This Week"
        StatPeriod.THIS_MONTH -> "This Month"
        StatPeriod.LAST_MONTH -> "Last Month"
        StatPeriod.THIS_YEAR -> "This Year"
        StatPeriod.CUSTOM -> "Custom"
    }
