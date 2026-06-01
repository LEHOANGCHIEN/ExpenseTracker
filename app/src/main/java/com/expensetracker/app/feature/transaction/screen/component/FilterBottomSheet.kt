package com.expensetracker.app.feature.transaction.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.feature.transaction.TransactionFilter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentFilter: TransactionFilter,
    categories: List<Category>,
    onFilterApplied: (TransactionFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var selectedCategoryIds by remember { mutableStateOf(currentFilter.categoryIds) }
    var selectedTypes by remember { mutableStateOf(currentFilter.types) }
    var minAmount by remember { mutableStateOf(currentFilter.minAmount?.toString() ?: "") }
    var maxAmount by remember { mutableStateOf(currentFilter.maxAmount?.toString() ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Filter Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                TextButton(onClick = {
                    selectedCategoryIds = emptySet()
                    selectedTypes = emptySet()
                    minAmount = ""
                    maxAmount = ""
                }) {
                    Text("Clear All")
                }
            }

            // Type filter
            Text(
                text = "Type",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.entries.forEach { type ->
                    FilterChip(
                        selected = type in selectedTypes,
                        onClick = {
                            selectedTypes = if (type in selectedTypes)
                                selectedTypes - type else selectedTypes + type
                        },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            // Category filter
            if (categories.isNotEmpty()) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = cat.id in selectedCategoryIds,
                            onClick = {
                                selectedCategoryIds = if (cat.id in selectedCategoryIds)
                                    selectedCategoryIds - cat.id else selectedCategoryIds + cat.id
                            },
                            label = {
                                Text(
                                    text = if (cat.icon.isNotBlank()) "${cat.icon} ${cat.name}"
                                        else cat.name,
                                )
                            },
                        )
                    }
                }
            }

            // Amount range
            Text(
                text = "Amount Range",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = minAmount,
                    onValueChange = { minAmount = it },
                    label = { Text("Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = maxAmount,
                    onValueChange = { maxAmount = it },
                    label = { Text("Max") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // Apply / Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onFilterApplied(
                            TransactionFilter(
                                categoryIds = selectedCategoryIds,
                                types = selectedTypes,
                                minAmount = minAmount.toDoubleOrNull(),
                                maxAmount = maxAmount.toDoubleOrNull(),
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Apply")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
