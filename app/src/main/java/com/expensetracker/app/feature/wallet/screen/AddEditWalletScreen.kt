package com.expensetracker.app.feature.wallet.screen

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.feature.category.screen.component.CategoryPaletteHex
import com.expensetracker.app.feature.wallet.AddEditWalletEvent
import com.expensetracker.app.feature.wallet.AddEditWalletViewModel
import com.expensetracker.app.feature.wallet.WALLET_CURRENCIES
import com.expensetracker.app.feature.wallet.WALLET_ICONS

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditWalletScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditWalletViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isDone) {
        if (state.isDone) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Wallet" else "New Wallet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { viewModel.onEvent(AddEditWalletEvent.DeleteRequested) }) {
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Preview card
            WalletPreview(
                name = state.name,
                icon = state.icon,
                color = state.color,
                currency = state.currency,
            )

            // Name field
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(AddEditWalletEvent.NameChanged(it)) },
                label = { Text("Wallet name") },
                placeholder = { Text("e.g. Cash, Savings") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Icon picker
            Column {
                Text(
                    "Icon",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WALLET_ICONS.forEach { icon ->
                        val isSelected = icon == state.icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                )
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp, MaterialTheme.colorScheme.primary, CircleShape,
                                    ) else Modifier
                                )
                                .clickable { viewModel.onEvent(AddEditWalletEvent.IconSelected(icon)) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(icon, fontSize = 22.sp)
                        }
                    }
                }
            }

            // Color picker
            Column {
                Text(
                    "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryPaletteHex.forEach { hex ->
                        val color = runCatching {
                            Color(android.graphics.Color.parseColor(hex))
                        }.getOrElse { Color.Gray }
                        val isSelected = hex.equals(state.color, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { viewModel.onEvent(AddEditWalletEvent.ColorSelected(hex)) },
                        )
                    }
                }
            }

            // Initial balance
            OutlinedTextField(
                value = state.initialBalanceText,
                onValueChange = { viewModel.onEvent(AddEditWalletEvent.BalanceChanged(it)) },
                label = { Text("Initial balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isEditing,
                supportingText = if (state.isEditing) ({ Text("Cannot change after creation") }) else null,
            )

            // Currency dropdown
            var currencyExpanded by remember { mutableStateOf(false) }
            Column {
                Text(
                    "Currency",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = state.currency,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(currencyExpanded) },
                    )
                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false },
                    ) {
                        WALLET_CURRENCIES.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    viewModel.onEvent(AddEditWalletEvent.CurrencyChanged(c))
                                    currencyExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.onEvent(AddEditWalletEvent.Save) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isEditing) "Update Wallet" else "Save Wallet")
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    // Delete confirm
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AddEditWalletEvent.DeleteDismissed) },
            title = { Text("Delete Wallet") },
            text = { Text("Are you sure? This will remove the wallet permanently.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(AddEditWalletEvent.DeleteConfirmed) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(AddEditWalletEvent.DeleteDismissed) }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete blocked
    if (state.deleteBlocked) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AddEditWalletEvent.DismissDeleteBlocked) },
            title = { Text("Cannot Delete") },
            text = { Text("This wallet has existing transactions and cannot be deleted.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(AddEditWalletEvent.DismissDeleteBlocked) }) {
                    Text("OK")
                }
            },
        )
    }
}

@Composable
private fun WalletPreview(
    name: String,
    icon: String,
    color: String,
    currency: String,
) {
    val baseColor = runCatching {
        Color(android.graphics.Color.parseColor(color))
    }.getOrElse { MaterialTheme.colorScheme.primary }
    val darkColor = Color(
        red = (baseColor.red * 0.65f).coerceIn(0f, 1f),
        green = (baseColor.green * 0.65f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 0.65f).coerceIn(0f, 1f),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(baseColor, darkColor)))
            .padding(20.dp),
    ) {
        Column {
            Text(icon.ifBlank { "💵" }, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = name.ifBlank { "Wallet name" },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Text(
                text = currency,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}
