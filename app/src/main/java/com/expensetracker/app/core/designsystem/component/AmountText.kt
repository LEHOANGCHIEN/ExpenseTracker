package com.expensetracker.app.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.domain.model.TransactionType

@Composable
fun AmountText(
    amount: Double,
    type: TransactionType,
    currency: String = "VND",
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    showSign: Boolean = true,
) {
    val (color, prefix) = when (type) {
        TransactionType.INCOME -> Pair(IncomeGreen, if (showSign) "+ " else "")
        TransactionType.EXPENSE -> Pair(ExpenseRed, if (showSign) "- " else "")
        TransactionType.TRANSFER -> Pair(MaterialTheme.colorScheme.onSurface, "")
    }

    val formatted = CurrencyFormatter.format(amount, currency)

    Text(
        text = "$prefix$formatted",
        modifier = modifier,
        color = color,
        style = style.copy(fontWeight = FontWeight.Bold),
    )
}
