package com.expensetracker.app.feature.statistics.screen.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.feature.statistics.StatCategorySpending

@Composable
fun CategoryPieChart(
    categories: List<StatCategorySpending>,
    totalExpense: Double,
    currency: String,
    modifier: Modifier = Modifier,
) {
    if (categories.isEmpty()) {
        Text(
            text = "No expense categories",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth(),
        )
        return
    }

    val sliceColors = categories.map { cat ->
        runCatching { Color(android.graphics.Color.parseColor(cat.category.color)) }
            .getOrElse { MaterialTheme.colorScheme.primary }
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = size.width * 0.18f
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            // Draw track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
            )

            // Draw slices
            var startAngle = -90f
            categories.forEachIndexed { index, item ->
                val sweep = 360f * item.percentage.coerceIn(0f, 1f)
                drawArc(
                    color = sliceColors[index],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
                startAngle += sweep
            }
        }

        // Center text
        Text(
            text = CurrencyFormatter.formatCompact(totalExpense, currency),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
