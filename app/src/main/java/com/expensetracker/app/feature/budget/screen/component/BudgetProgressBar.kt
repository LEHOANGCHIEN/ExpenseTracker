package com.expensetracker.app.feature.budget.screen.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BudgetProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
) {
    val fraction = progress.coerceIn(0f, 1f)

    val gradientColors = listOf(
        Color(0xFF4CAF50), // green
        Color(0xFFFFEB3B), // yellow
        Color(0xFFFF9800), // orange
        Color(0xFFF44336), // red
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2)),
    ) {
        val w = size.width
        val h = size.height

        // Track (dimmed gradient over full width)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = gradientColors.map { it.copy(alpha = 0.18f) },
                startX = 0f,
                endX = w,
            ),
            size = Size(w, h),
        )

        // Filled bar — gradient anchored to full width so colour position is consistent
        if (fraction > 0f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = gradientColors,
                    startX = 0f,
                    endX = w,
                ),
                size = Size(w * fraction, h),
            )
        }
    }
}
