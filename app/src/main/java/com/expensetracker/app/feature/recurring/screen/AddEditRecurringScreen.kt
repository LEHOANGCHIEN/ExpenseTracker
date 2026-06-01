package com.expensetracker.app.feature.recurring.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.core.util.DateUtils
import com.expensetracker.app.domain.model.RecurrenceFrequency
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.feature.recurring.AddEditRecurringEvent
import com.expensetracker.app.feature.recurring.AddEditRecurringViewModel
import com.expensetracker.app.feature.transaction.screen.component.CategoryPickerSheet
import com.expensetracker.app.feature.transaction.screen.component.TransactionDatePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditRecurringViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isDone) {
        if (state.isDone) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Schedule" else "New Schedule") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Amount + type
            OutlinedTextField(
                value = state.amountText,
                onValueChange = { viewModel.onEvent(AddEditRecurringEvent.AmountChanged(it)) },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.EventRepeat, null, Modifier.size(18.dp)) },
            )

            // Type segmented button
            val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                types.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.type == type,
                        onClick = { viewModel.onEvent(AddEditRecurringEvent.TypeChanged(type)) },
                        shape = SegmentedButtonDefaults.itemShape(index, types.size),
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            // Category
            Column {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                val selectedCat = state.filteredCategories.find { it.id == state.categoryId }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (selectedCat != null) {
                        val catColor = runCatching {
                            Color(android.graphics.Color.parseColor(selectedCat.color))
                        }.getOrElse { MaterialTheme.colorScheme.primary }
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(catColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) { Text(selectedCat.icon.takeIf { it.isNotBlank() } ?: "?", fontSize = 20.sp) }
                        Text(selectedCat.name, style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(onClick = { viewModel.onEvent(AddEditRecurringEvent.ShowCategoryPicker) }) {
                        Text(if (selectedCat == null) "Choose Category" else "Change")
                    }
                }
            }

            // Wallet
            if (state.wallets.size > 1) {
                WalletDropdown(
                    wallets = state.wallets,
                    selectedId = state.walletId,
                    onSelect = { viewModel.onEvent(AddEditRecurringEvent.WalletSelected(it)) },
                )
            }

            // Note
            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.onEvent(AddEditRecurringEvent.NoteChanged(it)) },
                label = { Text("Note (optional)") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            // Frequency
            FrequencySelector(
                selected = state.frequency,
                onChange = { viewModel.onEvent(AddEditRecurringEvent.FrequencyChanged(it)) },
            )

            // Interval stepper
            Column {
                Text(
                    "Repeat every",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IconButton(
                        onClick = { viewModel.onEvent(AddEditRecurringEvent.IntervalChanged(state.interval - 1)) },
                        enabled = state.interval > 1,
                    ) { Icon(Icons.Default.Remove, null) }
                    Text(
                        text = "${state.interval}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.width(36.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    IconButton(
                        onClick = { viewModel.onEvent(AddEditRecurringEvent.IntervalChanged(state.interval + 1)) },
                        enabled = state.interval < 99,
                    ) { Icon(Icons.Default.Add, null) }
                    Text(
                        text = when (state.frequency) {
                            RecurrenceFrequency.DAILY -> if (state.interval == 1) "day" else "days"
                            RecurrenceFrequency.WEEKLY -> if (state.interval == 1) "week" else "weeks"
                            RecurrenceFrequency.MONTHLY -> if (state.interval == 1) "month" else "months"
                            RecurrenceFrequency.YEARLY -> if (state.interval == 1) "year" else "years"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "→ ${state.frequencyLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Start date
            Column {
                Text(
                    "Start date",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                AssistChip(
                    onClick = { viewModel.onEvent(AddEditRecurringEvent.ShowStartDatePicker) },
                    label = { Text(DateUtils.formatRelative(state.startDate)) },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp)) },
                )
            }

            // End date (optional)
            Column {
                Text(
                    "End date (optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { viewModel.onEvent(AddEditRecurringEvent.ShowEndDatePicker) },
                        label = { Text(state.endDate?.let { DateUtils.formatRelative(it) } ?: "No end") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp)) },
                    )
                    if (state.endDate != null) {
                        TextButton(onClick = { viewModel.onEvent(AddEditRecurringEvent.EndDateChanged(null)) }) {
                            Text("Clear")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.onEvent(AddEditRecurringEvent.Save) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isEditing) "Update" else "Save Schedule")
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
        }
    }

    if (state.showCategoryPicker) {
        CategoryPickerSheet(
            categories = state.filteredCategories,
            selectedCategoryId = state.categoryId,
            onCategorySelected = { viewModel.onEvent(AddEditRecurringEvent.CategorySelected(it)) },
            onDismiss = { viewModel.onEvent(AddEditRecurringEvent.HideCategoryPicker) },
        )
    }
    if (state.showStartDatePicker) {
        TransactionDatePickerDialog(
            initialDate = state.startDate,
            onDateSelected = { viewModel.onEvent(AddEditRecurringEvent.StartDateChanged(it)) },
            onDismiss = { viewModel.onEvent(AddEditRecurringEvent.HideStartDatePicker) },
        )
    }
    if (state.showEndDatePicker) {
        TransactionDatePickerDialog(
            initialDate = state.endDate ?: state.startDate,
            onDateSelected = { viewModel.onEvent(AddEditRecurringEvent.EndDateChanged(it)) },
            onDismiss = { viewModel.onEvent(AddEditRecurringEvent.HideEndDatePicker) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrequencySelector(
    selected: RecurrenceFrequency,
    onChange: (RecurrenceFrequency) -> Unit,
) {
    val options = RecurrenceFrequency.entries
    Column {
        Text(
            "Frequency",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, freq ->
                SegmentedButton(
                    selected = freq == selected,
                    onClick = { onChange(freq) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    label = {
                        Text(
                            freq.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDropdown(
    wallets: List<com.expensetracker.app.domain.model.Wallet>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = wallets.find { it.id == selectedId }?.name ?: "Select Wallet"

    Column {
        Text(
            "Wallet",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                wallets.forEach { w ->
                    DropdownMenuItem(
                        text = { Text(w.name) },
                        onClick = { onSelect(w.id); expanded = false },
                    )
                }
            }
        }
    }
}
