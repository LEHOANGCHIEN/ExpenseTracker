package com.expensetracker.app.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.expensetracker.app.core.util.CurrencyFormatter

@Composable
fun AnimatedAmountText(
    amount: Double,
    currency: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    compact: Boolean = false,
) {
    val animatedValue by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "animated_amount",
    )
    val formatted = if (compact) {
        CurrencyFormatter.formatCompact(animatedValue.toDouble(), currency)
    } else {
        CurrencyFormatter.format(animatedValue.toDouble(), currency)
    }
    Text(
        text = formatted,
        style = style,
        color = color,
        modifier = modifier,
    )
}
