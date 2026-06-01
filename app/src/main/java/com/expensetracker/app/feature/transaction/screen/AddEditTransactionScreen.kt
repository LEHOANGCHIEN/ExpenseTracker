package com.expensetracker.app.feature.transaction.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.expensetracker.app.core.designsystem.component.ShimmerBox
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.core.util.DateUtils
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.feature.transaction.AddEditTransactionViewModel
import com.expensetracker.app.feature.transaction.AddEditUiEvent
import com.expensetracker.app.feature.transaction.screen.component.AmountKeypad
import com.expensetracker.app.feature.transaction.screen.component.CategoryPickerSheet
import com.expensetracker.app.feature.transaction.screen.component.SplitTransactionDialog
import com.expensetracker.app.feature.transaction.screen.component.TransactionDatePickerDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(AddEditUiEvent.PhotoSelected(it.toString())) }
    }

    LaunchedEffect(state.isDone) {
        if (state.isDone) onNavigateBack()
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    if (state.isLoading) {
        AddEditLoadingShimmer()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditing) "Edit Transaction" else "Add Transaction")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { viewModel.onEvent(AddEditUiEvent.DeleteRequested) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ---- Amount section (fixed) ----
            AmountSection(
                expression = state.amountExpression,
                resolvedAmount = state.resolvedAmount,
                type = state.type,
                currency = state.currency,
                onKeyPress = { viewModel.onEvent(AddEditUiEvent.AmountKeyPressed(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // ---- Form fields (scrollable) ----
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Type toggle
                TypeSegmentedButton(
                    selected = state.type,
                    onChange = { viewModel.onEvent(AddEditUiEvent.TypeChanged(it)) },
                )

                // Category row
                CategoryRow(
                    categories = state.filteredCategories,
                    selectedId = state.categoryId,
                    onCategoryClick = { viewModel.onEvent(AddEditUiEvent.CategorySelected(it)) },
                    onMoreClick = { viewModel.onEvent(AddEditUiEvent.ShowCategoryPicker) },
                )

                // Wallet + Date row
                WalletAndDateRow(state = state, onEvent = { viewModel.onEvent(it) })

                // Note
                OutlinedTextField(
                    value = state.note,
                    onValueChange = { viewModel.onEvent(AddEditUiEvent.NoteChanged(it)) },
                    label = { Text("Note") },
                    placeholder = { Text("What was this for?") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${state.note.length}/200") },
                )

                // Tags
                TagsSection(
                    tags = state.tags,
                    tagInput = state.tagInput,
                    onInputChange = { viewModel.onEvent(AddEditUiEvent.TagInputChanged(it)) },
                    onAddTag = { viewModel.onEvent(AddEditUiEvent.AddTag) },
                    onRemoveTag = { viewModel.onEvent(AddEditUiEvent.RemoveTag(it)) },
                )

                // Photo attachment
                PhotoSection(
                    photoUri = state.photoUri,
                    onPickPhoto = { galleryLauncher.launch("image/*") },
                    onRemovePhoto = { viewModel.onEvent(AddEditUiEvent.PhotoSelected("")) },
                )

                // Recurring notice
                if (state.isRecurringChild) {
                    Text(
                        text = "ℹ️ This is part of a recurring schedule. Editing only affects this occurrence.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(8.dp),
                            )
                            .padding(12.dp),
                    )
                }

                // Advanced section
                TextButton(
                    onClick = { viewModel.onEvent(AddEditUiEvent.ToggleAdvanced) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        if (state.showAdvanced) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Advanced options")
                }
                AnimatedVisibility(visible = state.showAdvanced) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { viewModel.onEvent(AddEditUiEvent.ShowSplitDialog) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Split Transaction")
                            Icon(Icons.Default.ChevronRight, null)
                        }
                        if (state.isSplitEnabled) {
                            Text(
                                text = "Split into ${state.splitParts.size} parts",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ---- Save button ----
            Button(
                onClick = { viewModel.onEvent(AddEditUiEvent.Save) },
                enabled = state.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = if (state.isEditing) "Update" else "Save",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }

    // Dialogs
    if (state.showCategoryPicker) {
        CategoryPickerSheet(
            categories = state.filteredCategories,
            selectedCategoryId = state.categoryId,
            onCategorySelected = { viewModel.onEvent(AddEditUiEvent.CategorySelected(it)) },
            onDismiss = { viewModel.onEvent(AddEditUiEvent.HideCategoryPicker) },
        )
    }

    if (state.showDatePicker) {
        TransactionDatePickerDialog(
            initialDate = state.date,
            onDateSelected = { viewModel.onEvent(AddEditUiEvent.DateChanged(it)) },
            onDismiss = { viewModel.onEvent(AddEditUiEvent.HideDatePicker) },
        )
    }

    if (state.showSplitDialog) {
        SplitTransactionDialog(
            totalAmount = state.resolvedAmount,
            currency = state.currency,
            categories = state.filteredCategories,
            initialParts = state.splitParts,
            onConfirm = { parts -> viewModel.onEvent(AddEditUiEvent.SplitConfirmed(parts)) },
            onDismiss = { viewModel.onEvent(AddEditUiEvent.HideSplitDialog) },
        )
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AddEditUiEvent.DeleteDismissed) },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(AddEditUiEvent.DeleteConfirmed) },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(AddEditUiEvent.DeleteDismissed) }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun AmountSection(
    expression: String,
    resolvedAmount: Double,
    type: TransactionType,
    currency: String,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayColor = when (type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
    }
    val expressionHasOp = expression.any { it in listOf('+', '-', '×', '÷') }

    Column(modifier = modifier) {
        Text(
            text = if (expressionHasOp) expression else CurrencyFormatter.format(resolvedAmount, currency),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
            ),
            color = displayColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textAlign = TextAlign.End,
            maxLines = 1,
        )
        if (expressionHasOp) {
            Text(
                text = "= ${CurrencyFormatter.format(resolvedAmount, currency)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
        Spacer(Modifier.height(8.dp))
        AmountKeypad(onKeyPress = onKeyPress)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSegmentedButton(
    selected: TransactionType,
    onChange: (TransactionType) -> Unit,
) {
    val options = TransactionType.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, type ->
            SegmentedButton(
                selected = type == selected,
                onClick = { onChange(type) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                label = {
                    Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                },
            )
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<Category>,
    selectedId: Long?,
    onCategoryClick: (Long) -> Unit,
    onMoreClick: () -> Unit,
) {
    val recentCategories = categories.take(6)
    Column {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            recentCategories.forEach { cat ->
                val isSelected = cat.id == selectedId
                val catColor = runCatching {
                    Color(android.graphics.Color.parseColor(cat.color))
                }.getOrElse { MaterialTheme.colorScheme.primary }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCategoryClick(cat.id) },
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) catColor else catColor.copy(alpha = 0.2f))
                            .then(
                                if (isSelected) Modifier.border(2.dp, catColor, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(cat.icon.takeIf { it.isNotBlank() } ?: "?", fontSize = 20.sp)
                    }
                    Text(
                        text = cat.name,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            // "More" button
            if (categories.size > 6) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(onClick = onMoreClick),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("•••", fontSize = 14.sp)
                    }
                    Text(
                        "More",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    tags: List<String>,
    tagInput: String,
    onInputChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = tagInput,
                onValueChange = onInputChange,
                label = { Text("Add tag") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onAddTag, enabled = tagInput.isNotBlank()) {
                Text("Add")
            }
        }
        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = {},
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove tag",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onRemoveTag(tag) },
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoSection(
    photoUri: String?,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    if (photoUri.isNullOrBlank()) {
        TextButton(
            onClick = onPickPhoto,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Attach Photo")
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Transaction photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            IconButton(
                onClick = onRemovePhoto,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove photo",
                    tint = Color.White,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletAndDateRow(
    state: com.expensetracker.app.feature.transaction.AddEditTransactionUiState,
    onEvent: (AddEditUiEvent) -> Unit,
) {
    val isTransfer = state.type == com.expensetracker.app.domain.model.TransactionType.TRANSFER
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // From wallet + date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.wallets.size > 1) {
                WalletDropdown(
                    label = state.wallets.find { it.id == state.walletId }?.name ?: "Wallet",
                    wallets = state.wallets,
                    onSelect = { onEvent(AddEditUiEvent.WalletSelected(it)) },
                )
            }
            AssistChip(
                onClick = { onEvent(AddEditUiEvent.ShowDatePicker) },
                label = { Text(DateUtils.formatRelative(state.date)) },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp)) },
            )
        }
        // To wallet (only for TRANSFER)
        if (isTransfer && state.wallets.size > 1) {
            WalletDropdown(
                label = state.wallets.find { it.id == state.toWalletId }?.name ?: "To Wallet",
                wallets = state.wallets.filter { it.id != state.walletId },
                onSelect = { onEvent(AddEditUiEvent.ToWalletSelected(it)) },
                prefix = "To: ",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDropdown(
    label: String,
    wallets: List<com.expensetracker.app.domain.model.Wallet>,
    onSelect: (Long) -> Unit,
    prefix: String = "",
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        AssistChip(
            onClick = { expanded = true },
            label = { Text("$prefix$label") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, null, Modifier.size(16.dp)) },
            modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            wallets.forEach { w ->
                DropdownMenuItem(
                    text = { Text(w.name) },
                    onClick = { onSelect(w.id); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun AddEditLoadingShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(48.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(48.dp))
    }
}
