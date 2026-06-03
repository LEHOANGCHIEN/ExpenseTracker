package com.expensetracker.app.feature.home.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.app.R
import com.expensetracker.app.core.designsystem.component.AppCard
import com.expensetracker.app.core.designsystem.component.SectionHeader
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.feature.home.state.CategorySpending

@Composable
fun TopCategoriesCard(
    categories: List<CategorySpending>,
    currency: String,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.home_top_categories))
        Spacer(Modifier.height(12.dp))
        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.home_no_expenses_month),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            categories.forEachIndexed { index, spending ->
                CategorySpendingRow(spending = spending, currency = currency)
                if (index < categories.lastIndex) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun CategorySpendingRow(
    spending: CategorySpending,
    currency: String,
) {
    val catColor = runCatching {
        Color(android.graphics.Color.parseColor(spending.category.color))
    }.getOrElse { MaterialTheme.colorScheme.primary }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = spending.category.icon.takeIf { it.isNotBlank() } ?: "?",
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = spending.category.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                )
                Text(
                    text = CurrencyFormatter.formatCompact(spending.amount, currency),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { spending.percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = catColor,
                trackColor = catColor.copy(alpha = 0.15f),
            )
        }
        Text(
            text = "${(spending.percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp),
        )
    }
}
