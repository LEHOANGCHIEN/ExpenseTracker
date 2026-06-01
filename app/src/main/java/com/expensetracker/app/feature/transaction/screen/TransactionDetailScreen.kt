package com.expensetracker.app.feature.transaction.screen

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.expensetracker.app.core.designsystem.component.ShimmerBox
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.core.util.DateUtils
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.feature.transaction.TransactionDetailViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    state.transaction?.let { txn ->
                        IconButton(onClick = { onNavigateToEdit(txn.id) }) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                        IconButton(onClick = { viewModel.requestDelete() }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> DetailShimmer(Modifier.padding(padding))
            state.transaction == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Transaction not found")
                }
            }
            else -> {
                val txn = state.transaction!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Hero amount section
                    AmountHero(
                        transaction = txn,
                        currency = state.currency,
                        categoryIcon = state.category?.icon ?: "💸",
                        categoryColor = state.category?.color,
                    )

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Category row
                        DetailRow(
                            icon = Icons.Default.Category,
                            label = "Category",
                            value = state.category?.let {
                                if (it.icon.isNotBlank()) "${it.icon} ${it.name}" else it.name
                            } ?: "Unknown",
                        )

                        // Wallet row
                        state.wallet?.let { wallet ->
                            DetailRow(
                                icon = Icons.Default.AccountBalanceWallet,
                                label = "Wallet",
                                value = wallet.name,
                            )
                        }

                        // Date row
                        DetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Date",
                            value = txn.date.format(
                                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
                            ),
                        )

                        // Note row
                        if (txn.note.isNotBlank()) {
                            DetailSection(label = "Note") {
                                Text(
                                    text = txn.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        // Tags
                        if (txn.tags.isNotEmpty()) {
                            DetailSection(label = "Tags") {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    txn.tags.forEach { tag ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(tag) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Tag, null, Modifier.size(14.dp))
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        // Recurring notice
                        if (txn.recurringId != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        RoundedCornerShape(8.dp),
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    Icons.Default.Repeat,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                                Text(
                                    text = "Part of a recurring transaction",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }

                        // Split children
                        if (state.splitChildren.isNotEmpty()) {
                            HorizontalDivider()
                            Text(
                                text = "Split (${state.splitChildren.size} parts)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            state.splitChildren.forEach { child ->
                                SplitChildRow(
                                    child = child,
                                    categoryIcon = state.childCategories[child.categoryId]?.icon ?: "💸",
                                    categoryName = state.childCategories[child.categoryId]?.name ?: "Unknown",
                                    currency = state.currency,
                                )
                            }
                        }

                        // Photo
                        if (!txn.photoUri.isNullOrBlank()) {
                            HorizontalDivider()
                            AsyncImage(
                                model = txn.photoUri,
                                contentDescription = "Receipt photo",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                        }

                        // Metadata
                        HorizontalDivider()
                        Text(
                            text = "Created ${txn.createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm"))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Delete Transaction") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun AmountHero(
    transaction: Transaction,
    currency: String,
    categoryIcon: String,
    categoryColor: String?,
) {
    val catColor = categoryColor?.let { hex ->
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primary

    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
    }
    val prefix = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.TRANSFER -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(categoryIcon, style = MaterialTheme.typography.headlineMedium)
        }
        Text(
            text = "$prefix ${CurrencyFormatter.format(transaction.amount, currency)}",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = amountColor,
        )
        Text(
            text = transaction.type.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DetailSection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun SplitChildRow(
    child: Transaction,
    categoryIcon: String,
    categoryName: String,
    currency: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(categoryIcon, style = MaterialTheme.typography.titleSmall)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = child.note.ifBlank { categoryName },
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = CurrencyFormatter.formatCompact(child.amount, currency),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = ExpenseRed,
        )
    }
}

@Composable
private fun DetailShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
        repeat(4) { ShimmerBox(modifier = Modifier.fillMaxWidth().height(48.dp)) }
    }
}
