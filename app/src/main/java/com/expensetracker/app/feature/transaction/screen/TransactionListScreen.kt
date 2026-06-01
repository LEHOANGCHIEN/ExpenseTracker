package com.expensetracker.app.feature.transaction.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.core.designsystem.component.EmptyState
import com.expensetracker.app.core.designsystem.component.ShimmerBox
import com.expensetracker.app.core.util.DateUtils
import com.expensetracker.app.feature.transaction.TransactionListUiEvent
import com.expensetracker.app.feature.transaction.TransactionListViewModel
import com.expensetracker.app.feature.transaction.screen.component.FilterBottomSheet
import com.expensetracker.app.feature.transaction.screen.component.TransactionListItem
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddEdit: (Long?) -> Unit,
    viewModel: TransactionListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(state.pendingDeleteTransaction) {
        state.pendingDeleteTransaction?.let { txn ->
            val result = snackbarHostState.showSnackbar(
                message = "Transaction deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onEvent(TransactionListUiEvent.UndoDelete)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (state.isInSelectionMode) {
                            Text("${state.selectedIds.size} selected")
                        } else {
                            Text("Transactions")
                        }
                    },
                    navigationIcon = {
                        if (state.isInSelectionMode) {
                            IconButton(onClick = { viewModel.onEvent(TransactionListUiEvent.ClearSelection) }) {
                                Icon(Icons.Default.Close, "Clear selection")
                            }
                        } else {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        }
                    },
                    actions = {
                        if (state.isInSelectionMode) {
                            IconButton(onClick = {
                                viewModel.onEvent(TransactionListUiEvent.BulkDelete(state.selectedIds))
                            }) {
                                Icon(Icons.Default.Delete, "Delete selected", tint = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            IconButton(onClick = { isSearchActive = !isSearchActive }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                            BadgedBox(
                                badge = {
                                    if (state.filter.isActive) {
                                        Badge()
                                    }
                                }
                            ) {
                                IconButton(onClick = { viewModel.onEvent(TransactionListUiEvent.ShowFilterSheet) }) {
                                    Icon(Icons.Default.FilterList, "Filter")
                                }
                            }
                        }
                    },
                )

                // Search bar
                AnimatedVisibility(visible = isSearchActive, enter = fadeIn(), exit = fadeOut()) {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = state.searchQuery,
                                onQueryChange = { viewModel.onEvent(TransactionListUiEvent.SearchQueryChanged(it)) },
                                onSearch = {},
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = { Text("Search transactions…") },
                                trailingIcon = {
                                    if (state.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onEvent(TransactionListUiEvent.SearchQueryChanged("")) }) {
                                            Icon(Icons.Default.Close, null)
                                        }
                                    }
                                },
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {}
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoading -> TransactionListShimmer(Modifier.padding(padding))
            state.isEmpty -> EmptyState(
                title = "No transactions",
                message = "Add your first transaction to get started",
                actionLabel = "Add Transaction",
                onAction = { onNavigateToAddEdit(null) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            else -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    state.groupedTransactions.entries
                        .sortedByDescending { it.key }
                        .forEach { (date, transactions) ->
                            stickyHeader(key = "header_${date}") {
                                DateGroupHeader(date = date)
                            }
                            items(
                                items = transactions,
                                key = { it.id },
                            ) { transaction ->
                                TransactionListItem(
                                    transaction = transaction,
                                    category = state.categories[transaction.categoryId],
                                    currency = state.currency,
                                    isSelected = transaction.id in state.selectedIds,
                                    onCLick = {
                                        if (state.isInSelectionMode) {
                                            viewModel.onEvent(TransactionListUiEvent.ToggleSelect(transaction.id))
                                        } else {
                                            onNavigateToDetail(transaction.id)
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.onEvent(TransactionListUiEvent.ToggleSelect(transaction.id))
                                    },
                                    onDismissed = {
                                        viewModel.onEvent(TransactionListUiEvent.DeleteTransaction(transaction.id))
                                    },
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                }
            }
        }
    }

    if (state.showFilterSheet) {
        FilterBottomSheet(
            currentFilter = state.filter,
            categories = state.allCategories,
            onFilterApplied = { viewModel.onEvent(TransactionListUiEvent.FilterChanged(it)) },
            onDismiss = { viewModel.onEvent(TransactionListUiEvent.HideFilterSheet) },
        )
    }
}

@Composable
private fun DateGroupHeader(date: LocalDate) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = DateUtils.formatRelative(date),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun TransactionListShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(6) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(64.dp),
            )
        }
    }
}
