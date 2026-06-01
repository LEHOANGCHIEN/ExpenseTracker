package com.expensetracker.app.feature.home.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.feature.home.state.BudgetProgress

@Composable
fun BudgetProgressCard(
    budgetProgress: BudgetProgress,
    currency: String,
    modifier: Modifier = Modifier,
) {
    val fraction = budgetProgress.percentage.coerceIn(0f, 1f)
    val progressColor = lerp(
        start = Color(0xFF4CAF50),
        stop = Color(0xFFF44336),
        fraction = fraction,
    )
    val categoryLabel = budgetProgress.category?.let { cat ->
        if (cat.icon.isNotBlank()) "${cat.icon} ${cat.name}" else cat.name
    } ?: "Total Budget"

    Card(
        modifier = modifier.width(160.dp),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
            )
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.2f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = CurrencyFormatter.formatCompact(budgetProgress.spent, currency),
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor,
                )
                Text(
                    text = CurrencyFormatter.formatCompact(budgetProgress.budget.amount, currency),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${(fraction * 100).toInt()}% used",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
