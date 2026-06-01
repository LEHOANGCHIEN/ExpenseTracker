package com.expensetracker.app.feature.statistics.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DailySpendHeatmap(
    dailyExpense: Map<LocalDate, Double>,
    startDate: LocalDate,
    endDate: LocalDate,
    onDayClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val maxExpense = dailyExpense.values.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    val today = LocalDate.now()
    val daysInMonth = startDate.lengthOfMonth()
    val firstDayOffset = startDate.dayOfWeek.value - 1 // 0=Mon, 6=Sun
    val totalCells = firstDayOffset + daysInMonth
    val weeks = (totalCells + 6) / 7

    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = startDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        repeat(weeks) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = week * 7 + dayOfWeek
                    val dayNumber = cellIndex - firstDayOffset + 1

                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = startDate.withDayOfMonth(dayNumber)
                        val expense = dailyExpense[date] ?: 0.0
                        val intensity = (expense / maxExpense).toFloat().coerceIn(0f, 1f)
                        val cellColor = lerp(emptyColor, ExpenseRed, intensity)
                        val isToday = date == today

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(cellColor)
                                .then(
                                    if (isToday) Modifier.border(
                                        1.dp,
                                        primaryColor,
                                        RoundedCornerShape(4.dp),
                                    ) else Modifier,
                                )
                                .clickable { onDayClick(date) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (intensity > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}
