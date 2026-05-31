package com.expensetracker.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.expensetracker.app.core.designsystem.theme.CategoryPalette
import com.expensetracker.app.core.designsystem.theme.ExpenseTrackerTheme
import com.expensetracker.app.domain.model.TransactionType

@PreviewLightDark
@Composable
private fun DesignSystemPreview() {
    ExpenseTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Buttons
                Text("Buttons", style = MaterialTheme.typography.titleMedium)
                AppPrimaryButton(text = "Primary Button", onClick = {})
                AppPrimaryButton(text = "Loading…", onClick = {}, isLoading = true)
                AppPrimaryButton(text = "With Icon", onClick = {}, leadingIcon = Icons.Default.Add)
                AppSecondaryButton(text = "Secondary Button", onClick = {})
                AppTextButton(text = "Text Button", onClick = {})

                Spacer(Modifier.height(8.dp))

                // Cards
                Text("Card", style = MaterialTheme.typography.titleMedium)
                AppCard {
                    Text("Card Content", style = MaterialTheme.typography.bodyMedium)
                    Text("More content here", style = MaterialTheme.typography.bodySmall)
                }
                AppCard(onClick = {}) {
                    Text("Clickable Card", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(8.dp))

                // Text Field
                Text("Text Field", style = MaterialTheme.typography.titleMedium)
                AppTextField(
                    value = "Sample input",
                    onValueChange = {},
                    label = "Amount",
                    leadingIcon = Icons.Outlined.AccountBalanceWallet,
                )
                AppTextField(
                    value = "",
                    onValueChange = {},
                    label = "Search",
                    leadingIcon = Icons.Outlined.Search,
                    isError = true,
                    errorMessage = "Required field",
                )

                Spacer(Modifier.height(8.dp))

                // Amount Text
                Text("Amount Text", style = MaterialTheme.typography.titleMedium)
                AmountText(amount = 250000.0, type = TransactionType.EXPENSE, currency = "VND")
                AmountText(amount = 5000000.0, type = TransactionType.INCOME, currency = "VND")
                AmountText(amount = 100.0, type = TransactionType.TRANSFER, currency = "USD")

                Spacer(Modifier.height(8.dp))

                // Category Chips
                Text("Category Chips", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryChip(name = "Food", icon = "🍔", color = CategoryPalette[0])
                    CategoryChip(name = "Transport", icon = "🚗", color = CategoryPalette[4])
                    CategoryChip(name = "Shopping", icon = "🛍️", color = CategoryPalette[2])
                }

                Spacer(Modifier.height(8.dp))

                // Section Header
                Text("Section Header", style = MaterialTheme.typography.titleMedium)
                SectionHeader(
                    title = "Recent Transactions",
                    action = { AppTextButton(text = "See All", onClick = {}) },
                )

                Spacer(Modifier.height(8.dp))

                // Shimmer
                Text("Shimmer", style = MaterialTheme.typography.titleMedium)
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(60.dp))
                ShimmerBox(modifier = Modifier.size(120.dp, 20.dp))

                Spacer(Modifier.height(8.dp))

                // Loading
                Text("Loading Indicator", style = MaterialTheme.typography.titleMedium)
                LoadingIndicator()

                Spacer(Modifier.height(8.dp))

                // Empty State
                Text("Empty State", style = MaterialTheme.typography.titleMedium)
                EmptyState(
                    title = "No transactions yet",
                    message = "Start tracking your expenses by adding your first transaction.",
                    actionLabel = "Add Transaction",
                    onAction = {},
                )
            }
        }
    }
}
