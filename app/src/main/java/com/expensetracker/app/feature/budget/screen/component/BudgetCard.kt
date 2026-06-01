package com.expensetracker.app.feature.budget.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.WarningOrange
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.feature.budget.BudgetWithProgress

@Composable
fun BudgetCard(
    budgetProgress: BudgetWithProgress,
    currency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pct = budgetProgress.percentage
    val isExceeded = budgetProgress.isExceeded
    val isAlert = budgetProgress.isAlertTriggered

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isExceeded -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                isAlert -> WarningOrange.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: title + period label + alert badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budgetProgress.categoryLabel,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = budgetProgress.periodLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isExceeded || isAlert) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = if (isExceeded) "Over budget" else "Alert",
                        tint = if (isExceeded) ExpenseRed else WarningOrange,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Spent / Budget amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = CurrencyFormatter.format(budgetProgress.spent, currency),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                        color = when {
                            isExceeded -> ExpenseRed
                            isAlert -> WarningOrange
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Text(
                        text = "of ${CurrencyFormatter.format(budgetProgress.budget.amount, currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${(pct * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = when {
                        isExceeded -> ExpenseRed
                        isAlert -> WarningOrange
                        else -> MaterialTheme.colorScheme.primary
                    },
                )
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            BudgetProgressBar(progress = pct)

            Spacer(Modifier.height(8.dp))

            // Remaining / Over by + Days left
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val remainingLabel = if (isExceeded) {
                    "Over by ${CurrencyFormatter.formatCompact(-budgetProgress.remaining, currency)}"
                } else {
                    "${CurrencyFormatter.formatCompact(budgetProgress.remaining, currency)} left"
                }
                Text(
                    text = remainingLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isExceeded) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${budgetProgress.daysRemaining}d left",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(4.dp))

            // Avg daily spend vs pace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Avg/day: ${CurrencyFormatter.formatCompact(budgetProgress.avgDailySpend, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Pace: ${CurrencyFormatter.formatCompact(budgetProgress.dailyBudgetPace, currency)}/day",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
