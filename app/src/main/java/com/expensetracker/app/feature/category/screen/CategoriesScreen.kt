package com.expensetracker.app.feature.category.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.R
import com.expensetracker.app.core.designsystem.component.EmptyState
import com.expensetracker.app.core.designsystem.component.ShimmerBox
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.feature.category.CategoriesEvent
import com.expensetracker.app.feature.category.CategoriesViewModel
import com.expensetracker.app.feature.category.CategoryWithSpending

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Long) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_categories)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCategory,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.category_add))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab row
            TabRow(selectedTabIndex = if (state.selectedTab == TransactionType.EXPENSE) 0 else 1) {
                Tab(
                    selected = state.selectedTab == TransactionType.EXPENSE,
                    onClick = { viewModel.onEvent(CategoriesEvent.TabSelected(TransactionType.EXPENSE)) },
                    text = { Text(stringResource(R.string.category_expense_tab)) },
                )
                Tab(
                    selected = state.selectedTab == TransactionType.INCOME,
                    onClick = { viewModel.onEvent(CategoriesEvent.TabSelected(TransactionType.INCOME)) },
                    text = { Text(stringResource(R.string.category_income_tab)) },
                )
            }

            when {
                state.isLoading -> CategoriesShimmer()
                state.currentTabCategories.isEmpty() -> EmptyState(
                    title = stringResource(R.string.category_empty_title),
                    message = if (state.selectedTab == TransactionType.EXPENSE)
                        stringResource(R.string.category_empty_expense_hint)
                    else stringResource(R.string.category_empty_income_hint),
                    actionLabel = stringResource(R.string.category_add),
                    onAction = onNavigateToAddCategory,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> CategoryGrid(
                    categories = state.currentTabCategories,
                    currency = state.currency,
                    onLongPress = { cat ->
                        viewModel.onEvent(CategoriesEvent.LongPressCategory(cat))
                    },
                )
            }
        }
    }

    // Long-press action sheet
    state.longPressedCategory?.let { cat ->
        CategoryActionSheet(
            category = cat,
            onEdit = {
                viewModel.onEvent(CategoriesEvent.DismissActionSheet)
                onNavigateToEditCategory(cat.id)
            },
            onArchive = { viewModel.onEvent(CategoriesEvent.ArchiveCategory(cat.id)) },
            onDelete = { viewModel.onEvent(CategoriesEvent.DeleteCategory(cat.id)) },
            onDismiss = { viewModel.onEvent(CategoriesEvent.DismissActionSheet) },
        )
    }

    // Delete blocked dialog
    state.deleteBlockedCategory?.let { cat ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(CategoriesEvent.DismissDeleteBlocked) },
            title = { Text(stringResource(R.string.category_cannot_delete_title)) },
            text = {
                Text(stringResource(R.string.category_cannot_delete_message, cat.name))
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(CategoriesEvent.ArchiveCategory(cat.id)) }) {
                    Text(stringResource(R.string.action_archive))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(CategoriesEvent.DismissDeleteBlocked) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryGrid(
    categories: List<CategoryWithSpending>,
    currency: String,
    onLongPress: (Category) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(categories, key = { it.category.id }) { item ->
            CategoryGridItem(
                item = item,
                currency = currency,
                onLongPress = { onLongPress(item.category) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryGridItem(
    item: CategoryWithSpending,
    currency: String,
    onLongPress: () -> Unit,
) {
    val catColor = runCatching {
        Color(android.graphics.Color.parseColor(item.category.color))
    }.getOrElse { MaterialTheme.colorScheme.primary }

    Column(
        modifier = Modifier
            .combinedClickable(onClick = {}, onLongClick = onLongPress),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.category.icon.takeIf { it.isNotBlank() } ?: "?",
                fontSize = 28.sp,
            )
        }
        Text(
            text = item.category.name,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.monthlySpending > 0) {
            Text(
                text = CurrencyFormatter.formatCompact(item.monthlySpending, currency),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryActionSheet(
    category: Category,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "${category.icon} ${category.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            HorizontalDivider()

            ActionSheetItem(
                icon = Icons.Default.Edit,
                label = stringResource(R.string.action_edit),
                onClick = onEdit,
            )
            ActionSheetItem(
                icon = Icons.Default.Archive,
                label = stringResource(R.string.action_archive),
                onClick = onArchive,
            )
            ActionSheetItem(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun ActionSheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = label,
            color = tint,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun CategoriesShimmer() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(9) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ShimmerBox(modifier = Modifier.size(64.dp), shape = CircleShape)
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(14.dp))
            }
        }
    }
}
