package com.expensetracker.app.feature.recurring.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.R
import com.expensetracker.app.core.designsystem.component.EmptyState
import com.expensetracker.app.core.designsystem.component.ShimmerBox
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.core.util.DateUtils
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.feature.recurring.RecurringListEvent
import com.expensetracker.app.feature.recurring.RecurringListViewModel
import com.expensetracker.app.feature.recurring.RecurringScheduleItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecurringListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: RecurringListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_recurring)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.recurring_add))
            }
        },
    ) { padding ->
        when {
            state.isLoading -> RecurringShimmer(Modifier.padding(padding))
            state.schedules.isEmpty() -> EmptyState(
                title = stringResource(R.string.recurring_empty_title),
                message = stringResource(R.string.recurring_empty_message_alt),
                actionLabel = stringResource(R.string.recurring_add_schedule),
                onAction = onNavigateToAdd,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.schedules, key = { it.schedule.id }) { item ->
                    RecurringScheduleCard(
                        item = item,
                        currency = state.currency,
                        onLongPress = { viewModel.onEvent(RecurringListEvent.LongPress(item)) },
                        onToggleActive = { isActive ->
                            viewModel.onEvent(RecurringListEvent.ToggleActive(item.schedule.id, isActive))
                        },
                    )
                }
            }
        }
    }

    state.longPressedItem?.let { item ->
        RecurringActionSheet(
            item = item,
            onEdit = {
                viewModel.onEvent(RecurringListEvent.DismissActionSheet)
                onNavigateToEdit(item.schedule.id)
            },
            onTogglePause = {
                viewModel.onEvent(RecurringListEvent.ToggleActive(item.schedule.id, !item.schedule.isActive))
            },
            onDelete = { viewModel.onEvent(RecurringListEvent.Delete(item.schedule.id)) },
            onDismiss = { viewModel.onEvent(RecurringListEvent.DismissActionSheet) },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecurringScheduleCard(
    item: RecurringScheduleItem,
    currency: String,
    onLongPress: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
) {
    val catColor = item.category?.color?.let { hex ->
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primary

    val amountColor = when (item.schedule.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongPress),
        colors = CardDefaults.cardColors(
            containerColor = if (item.schedule.isActive)
                MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(catColor.copy(alpha = if (item.schedule.isActive) 0.2f else 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.category?.icon?.takeIf { it.isNotBlank() } ?: "🔄",
                    fontSize = 22.sp,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.category?.name ?: stringResource(R.string.transaction_detail_unknown),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (item.schedule.isActive) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = item.frequencyDescription,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.recurring_next, DateUtils.formatRelative(item.schedule.nextOccurrence)),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.schedule.isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatCompact(item.schedule.amount, currency),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = amountColor,
                )
                Switch(
                    checked = item.schedule.isActive,
                    onCheckedChange = onToggleActive,
                    modifier = Modifier.size(width = 44.dp, height = 28.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringActionSheet(
    item: RecurringScheduleItem,
    onEdit: () -> Unit,
    onTogglePause: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        ) {
            Text(
                text = "${item.category?.icon ?: "🔄"} ${item.category?.name ?: stringResource(R.string.recurring_schedule_unknown)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            HorizontalDivider()
            TextButton(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            ) {
                Icon(Icons.Default.Edit, null, Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.action_edit), modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
            }
            TextButton(
                onClick = onTogglePause,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            ) {
                Icon(
                    if (item.schedule.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    if (item.schedule.isActive) stringResource(R.string.action_pause) else stringResource(R.string.action_resume),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                )
            }
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            ) {
                Icon(Icons.Default.Delete, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.action_delete), modifier = Modifier.weight(1f), textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun RecurringShimmer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(4) { ShimmerBox(modifier = Modifier.fillMaxWidth().size(72.dp)) }
    }
}
