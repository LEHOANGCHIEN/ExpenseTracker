package com.expensetracker.app.feature.transaction.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.feature.transaction.SplitPart
import kotlin.math.abs

@Composable
fun SplitTransactionDialog(
    totalAmount: Double,
    currency: String,
    categories: List<Category>,
    initialParts: List<SplitPart>,
    onConfirm: (List<SplitPart>) -> Unit,
    onDismiss: () -> Unit,
) {
    var parts by remember {
        mutableStateOf(
            if (initialParts.isNotEmpty()) initialParts
            else listOf(
                SplitPart(tempId = 0, amount = totalAmount / 2),
                SplitPart(tempId = 1, amount = totalAmount / 2),
            )
        )
    }

    val partsSum = parts.sumOf { it.amount }
    val remaining = totalAmount - partsSum
    val isBalanced = abs(remaining) < 0.01

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Split Transaction") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Total: ${CurrencyFormatter.format(totalAmount, currency)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!isBalanced) {
                    Text(
                        text = "Remaining: ${CurrencyFormatter.format(abs(remaining), currency)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (remaining > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                    )
                }

                parts.forEachIndexed { index, part ->
                    SplitPartRow(
                        index = index,
                        part = part,
                        categories = categories,
                        onAmountChanged = { newAmount ->
                            parts = parts.toMutableList().also { list ->
                                list[index] = part.copy(amount = newAmount)
                            }
                        },
                        onNoteChanged = { newNote ->
                            parts = parts.toMutableList().also { list ->
                                list[index] = part.copy(note = newNote)
                            }
                        },
                        onCategoryChanged = { catId ->
                            parts = parts.toMutableList().also { list ->
                                list[index] = part.copy(categoryId = catId)
                            }
                        },
                        onRemove = if (parts.size > 2) ({
                            parts = parts.toMutableList().also { it.removeAt(index) }
                        }) else null,
                    )
                }

                TextButton(
                    onClick = {
                        val nextId = (parts.maxOfOrNull { it.tempId } ?: 0) + 1
                        parts = parts + SplitPart(tempId = nextId, amount = 0.0)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add part")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(parts) },
                enabled = isBalanced && parts.all { it.amount > 0 },
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SplitPartRow(
    index: Int,
    part: SplitPart,
    categories: List<Category>,
    onAmountChanged: (Double) -> Unit,
    onNoteChanged: (String) -> Unit,
    onCategoryChanged: (Long?) -> Unit,
    onRemove: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Part ${index + 1}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
            )
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        OutlinedTextField(
            value = if (part.amount == 0.0) "" else part.amount.toString(),
            onValueChange = { text ->
                onAmountChanged(text.toDoubleOrNull() ?: 0.0)
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = part.note,
            onValueChange = onNoteChanged,
            label = { Text("Note (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
