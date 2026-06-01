package com.expensetracker.app.feature.home.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.util.CurrencyFormatter

@Composable
fun BalanceCard(
    totalBalance: Double,
    monthIncome: Double,
    monthExpense: Double,
    currency: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00897B), Color(0xFF004D40)),
                ),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(24.dp),
    ) {
        Column {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.format(totalBalance, currency),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BalanceSummaryChip(
                    label = "Income",
                    amount = monthIncome,
                    currency = currency,
                    icon = Icons.Default.ArrowUpward,
                    iconTint = IncomeGreen,
                )
                BalanceSummaryChip(
                    label = "Expense",
                    amount = monthExpense,
                    currency = currency,
                    icon = Icons.Default.ArrowDownward,
                    iconTint = ExpenseRed,
                )
            }
        }
    }
}

@Composable
private fun BalanceSummaryChip(
    label: String,
    amount: Double,
    currency: String,
    icon: ImageVector,
    iconTint: Color,
) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(14.dp),
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
                Text(
                    text = CurrencyFormatter.formatCompact(amount, currency),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
    }
}
