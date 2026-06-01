package com.expensetracker.app.feature.home.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.app.core.designsystem.component.AppCard
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.util.CurrencyFormatter
import kotlin.math.abs

@Composable
fun MonthSummaryCard(
    monthIncome: Double,
    monthExpense: Double,
    monthNet: Double,
    currency: String,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonthSummaryItem(
                label = "Income",
                amount = monthIncome,
                currency = currency,
                color = IncomeGreen,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(40.dp))
            MonthSummaryItem(
                label = "Expense",
                amount = monthExpense,
                currency = currency,
                color = ExpenseRed,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(40.dp))
            MonthSummaryItem(
                label = "Net",
                amount = abs(monthNet),
                currency = currency,
                color = if (monthNet >= 0) IncomeGreen else ExpenseRed,
                prefix = if (monthNet >= 0) "+" else "-",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthSummaryItem(
    label: String,
    amount: Double,
    currency: String,
    color: Color,
    modifier: Modifier = Modifier,
    prefix: String = "",
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$prefix${CurrencyFormatter.formatCompact(amount, currency)}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}
