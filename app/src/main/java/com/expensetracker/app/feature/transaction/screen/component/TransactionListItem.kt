package com.expensetracker.app.feature.transaction.screen.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.core.util.DateUtils
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionListItem(
    transaction: Transaction,
    category: Category?,
    currency: String,
    isSelected: Boolean,
    onCLick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismissed()
                true
            } else false
        }
    )

    // Reset if re-added after undo
    LaunchedEffect(transaction.id) {
        dismissState.reset()
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        TransactionItemContent(
            transaction = transaction,
            category = category,
            currency = currency,
            isSelected = isSelected,
            onClick = onCLick,
            onLongClick = onLongClick,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionItemContent(
    transaction: Transaction,
    category: Category?,
    currency: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val catColor = category?.color?.let { hex ->
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primary

    val (amountColor, amountPrefix) = when (transaction.type) {
        TransactionType.INCOME -> IncomeGreen to "+"
        TransactionType.EXPENSE -> ExpenseRed to "-"
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface to ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = if (isSelected) 0.4f else 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Text(
                    text = category?.icon?.takeIf { it.isNotBlank() } ?: "💸",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
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
                text = "$amountPrefix ${CurrencyFormatter.formatCompact(transaction.amount, currency)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = amountColor,
            )
            Text(
                text = DateUtils.formatRelative(transaction.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
