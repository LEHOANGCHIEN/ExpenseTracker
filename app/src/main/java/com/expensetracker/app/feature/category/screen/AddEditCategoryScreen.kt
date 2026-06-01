package com.expensetracker.app.feature.category.screen

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.feature.category.AddEditCategoryEvent
import com.expensetracker.app.feature.category.AddEditCategoryViewModel
import com.expensetracker.app.feature.category.screen.component.ColorPickerSheet
import com.expensetracker.app.feature.category.screen.component.IconPickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditCategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isDone) {
        if (state.isDone) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Category" else "New Category") },
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
            // Live preview
            CategoryPreview(
                name = state.name,
                icon = state.icon,
                color = state.color,
            )

            // Name field
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(AddEditCategoryEvent.NameChanged(it)) },
                label = { Text("Category name") },
                placeholder = { Text("e.g. Groceries") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("${state.name.length}/40") },
            )

            // Type toggle (only when creating)
            if (!state.isEditing) {
                val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
                Column {
                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        types.forEachIndexed { index, type ->
                            SegmentedButton(
                                selected = state.type == type,
                                onClick = { viewModel.onEvent(AddEditCategoryEvent.TypeChanged(type)) },
                                shape = SegmentedButtonDefaults.itemShape(index, types.size),
                                label = {
                                    Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                                },
                            )
                        }
                    }
                }
            }

            // Icon picker row
            Column {
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(state.icon, fontSize = 28.sp)
                    }
                    Button(
                        onClick = { viewModel.onEvent(AddEditCategoryEvent.ShowIconPicker) },
                    ) {
                        Text("Choose Icon")
                    }
                }
            }

            // Color picker row
            Column {
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val selectedColor = runCatching {
                        Color(android.graphics.Color.parseColor(state.color))
                    }.getOrElse { MaterialTheme.colorScheme.primary }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(selectedColor)
                            .clickable { viewModel.onEvent(AddEditCategoryEvent.ShowColorPicker) },
                    )
                    Button(
                        onClick = { viewModel.onEvent(AddEditCategoryEvent.ShowColorPicker) },
                    ) {
                        Text("Choose Color")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = { viewModel.onEvent(AddEditCategoryEvent.Save) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isEditing) "Update" else "Save Category")
            }
        }
    }

    // Pickers
    if (state.showIconPicker) {
        IconPickerSheet(
            selectedIcon = state.icon,
            onIconSelected = { viewModel.onEvent(AddEditCategoryEvent.IconSelected(it)) },
            onDismiss = { viewModel.onEvent(AddEditCategoryEvent.HideIconPicker) },
        )
    }

    if (state.showColorPicker) {
        ColorPickerSheet(
            selectedColor = state.color,
            onColorSelected = { viewModel.onEvent(AddEditCategoryEvent.ColorSelected(it)) },
            onDismiss = { viewModel.onEvent(AddEditCategoryEvent.HideColorPicker) },
        )
    }
}

@Composable
private fun CategoryPreview(
    name: String,
    icon: String,
    color: String,
) {
    val catColor = runCatching {
        Color(android.graphics.Color.parseColor(color))
    }.getOrElse { MaterialTheme.colorScheme.primary }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Preview",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.2f))
                .border(2.dp, catColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon.takeIf { it.isNotBlank() } ?: "?", fontSize = 32.sp)
        }
        Text(
            text = name.ifBlank { "Category name" },
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
        )
    }
}
