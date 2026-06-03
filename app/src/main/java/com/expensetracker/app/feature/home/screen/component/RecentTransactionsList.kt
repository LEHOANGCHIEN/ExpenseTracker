package com.expensetracker.app.feature.home.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expensetracker.app.R
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.core.util.DateUtils
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType

@Composable
fun RecentTransactionsList(
    transactions: List<Transaction>,
    categoriesById: Map<Long, Category>,
    currency: String,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (transactions.isEmpty()) {
            Text(
                text = "No transactions this month",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            transactions.forEachIndexed { index, transaction ->
                RecentTransactionItem(
                    transaction = transaction,
                    category = categoriesById[transaction.categoryId],
                    currency = currency,
                    onClick = { onTransactionClick(transaction.id) },
                )
                if (index < transactions.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionItem(
    transaction: Transaction,
    category: Category?,
    currency: String,
    onClick: () -> Unit,
) {
    val todayLabel = stringResource(R.string.date_today)
    val yesterdayLabel = stringResource(R.string.date_yesterday)
    val tomorrowLabel = stringResource(R.string.date_tomorrow)
    val catColor = category?.color?.let { hex ->
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primary

    val (amountColor, amountPrefix) = when (transaction.type) {
        TransactionType.INCOME -> IncomeGreen to "+ "
        TransactionType.EXPENSE -> ExpenseRed to "- "
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface to ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = category?.icon?.takeIf { it.isNotBlank() } ?: "💸",
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note.ifBlank { category?.name ?: "Transaction" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (category != null) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$amountPrefix${CurrencyFormatter.formatCompact(transaction.amount, currency)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = amountColor,
            )
            Text(
                text = DateUtils.formatRelative(transaction.date, todayLabel = todayLabel, yesterdayLabel = yesterdayLabel, tomorrowLabel = tomorrowLabel),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
