package com.expensetracker.app.feature.budget.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.core.util.DateUtils
import com.expensetracker.app.domain.model.BudgetPeriod
import com.expensetracker.app.feature.budget.AddEditBudgetEvent
import com.expensetracker.app.feature.budget.AddEditBudgetViewModel
import com.expensetracker.app.feature.transaction.screen.component.TransactionDatePickerDialog
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditBudgetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isDone) {
        if (state.isDone) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Budget" else "New Budget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Category selector
            CategoryDropdown(
                label = state.categoryLabel,
                categories = state.categories,
                expanded = state.showCategoryDropdown,
                selectedId = state.categoryId,
                onExpand = { viewModel.onEvent(AddEditBudgetEvent.ShowCategoryDropdown) },
                onDismiss = { viewModel.onEvent(AddEditBudgetEvent.HideCategoryDropdown) },
                onSelect = { id -> viewModel.onEvent(AddEditBudgetEvent.CategorySelected(id)) },
            )

            // Amount
            OutlinedTextField(
                value = state.amountText,
                onValueChange = { viewModel.onEvent(AddEditBudgetEvent.AmountChanged(it)) },
                label = { Text("Budget amount") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Period segmented button
            PeriodSelector(
                selected = state.period,
                onChange = { viewModel.onEvent(AddEditBudgetEvent.PeriodChanged(it)) },
            )

            // Date display (auto or manual for CUSTOM)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Period",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.period == BudgetPeriod.CUSTOM) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(
                            onClick = { viewModel.onEvent(AddEditBudgetEvent.ShowStartDatePicker) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Start: ${DateUtils.formatRelative(state.startDate)}")
                        }
                        TextButton(
                            onClick = { viewModel.onEvent(AddEditBudgetEvent.ShowEndDatePicker) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                "End: ${state.endDate?.let { DateUtils.formatRelative(it) } ?: "Select"}"
                            )
                        }
                    }
                } else {
                    val endLabel = state.periodEndDate?.let { DateUtils.formatRelative(it) } ?: "—"
                    Text(
                        text = "${DateUtils.formatRelative(state.periodStartDate)} → $endLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Alert threshold slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Alert threshold",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${(state.alertThreshold * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Slider(
                    value = state.alertThreshold,
                    onValueChange = { viewModel.onEvent(AddEditBudgetEvent.AlertThresholdChanged(it)) },
                    valueRange = 0.1f..1.0f,
                    steps = 17,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Notify me when spending reaches this %",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = { viewModel.onEvent(AddEditBudgetEvent.Save) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = androidx.compose.ui.Modifier.width(18.dp).height(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isEditing) "Update" else "Save Budget")
            }

            state.error?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    // Date pickers
    if (state.showStartDatePicker) {
        TransactionDatePickerDialog(
            initialDate = state.startDate,
            onDateSelected = { viewModel.onEvent(AddEditBudgetEvent.StartDateChanged(it)) },
            onDismiss = { viewModel.onEvent(AddEditBudgetEvent.HideStartDatePicker) },
        )
    }
    if (state.showEndDatePicker) {
        TransactionDatePickerDialog(
            initialDate = state.endDate ?: state.startDate,
            onDateSelected = { viewModel.onEvent(AddEditBudgetEvent.EndDateChanged(it)) },
            onDismiss = { viewModel.onEvent(AddEditBudgetEvent.HideEndDatePicker) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    label: String,
    categories: List<com.expensetracker.app.domain.model.Category>,
    expanded: Boolean,
    selectedId: Long?,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Long?) -> Unit,
) {
    Column {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (it) onExpand() else onDismiss() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
            ) {
                DropdownMenuItem(
                    text = { Text("Overall (all expenses)") },
                    onClick = { onSelect(null) },
                )
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = {
                            Text(if (cat.icon.isNotBlank()) "${cat.icon} ${cat.name}" else cat.name)
                        },
                        onClick = { onSelect(cat.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selected: BudgetPeriod,
    onChange: (BudgetPeriod) -> Unit,
) {
    val options = BudgetPeriod.entries
    Column {
        Text(
            text = "Period",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, period ->
                SegmentedButton(
                    selected = period == selected,
                    onClick = { onChange(period) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    label = {
                        Text(
                            text = period.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                        )
                    },
                )
            }
        }
    }
}
