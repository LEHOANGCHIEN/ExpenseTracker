package com.expensetracker.app.feature.transaction.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KEYPAD_LAYOUT = listOf(
    listOf("7", "8", "9", "÷"),
    listOf("4", "5", "6", "×"),
    listOf("1", "2", "3", "-"),
    listOf(".", "0", "⌫", "+"),
)

@Composable
fun AmountKeypad(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        KEYPAD_LAYOUT.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { key ->
                    KeypadButton(
                        label = key,
                        isOperator = key in listOf("+", "-", "×", "÷"),
                        onClick = { onKeyPress(key) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        // Clear row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            KeypadButton(
                label = "AC",
                isOperator = false,
                isDestructive = true,
                onClick = { onKeyPress("AC") },
                modifier = Modifier.weight(1f),
            )
            KeypadButton(
                label = "00",
                isOperator = false,
                onClick = { onKeyPress("00") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    isOperator: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    val shape = RoundedCornerShape(12.dp)
    if (isOperator) {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier.height(52.dp),
            shape = shape,
        ) {
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(52.dp),
            shape = shape,
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = if (isDestructive) FontWeight.Bold else FontWeight.Normal,
                color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
