package com.expensetracker.app.feature.home.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.R
import com.expensetracker.app.core.designsystem.component.EmptyState
import com.expensetracker.app.core.designsystem.component.SectionHeader
import com.expensetracker.app.core.designsystem.component.ShimmerBox
import com.expensetracker.app.core.util.DateUtils
import com.expensetracker.app.feature.home.screen.component.AiInsightCard
import com.expensetracker.app.feature.home.screen.component.BalanceCard
import com.expensetracker.app.feature.home.screen.component.BudgetProgressCard
import com.expensetracker.app.feature.home.screen.component.MonthSummaryCard
import com.expensetracker.app.feature.home.screen.component.RecentTransactionsList
import com.expensetracker.app.feature.home.screen.component.TopCategoriesCard
import com.expensetracker.app.feature.home.state.HomeEvent
import com.expensetracker.app.feature.home.viewmodel.HomeViewModel
import java.time.LocalTime
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onNavigateToReceiptScanner: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val isEmpty = uiState.recentTransactions.isEmpty() &&
        uiState.monthIncome == 0.0 &&
        uiState.monthExpense == 0.0 &&
        uiState.totalBalance == 0.0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, stringResource(R.string.nav_settings))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            HomeLoadingShimmer(Modifier.padding(paddingValues))
        } else {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onEvent(HomeEvent.RefreshInsights) },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
            item {
                HomeHeader(
                    selectedMonth = uiState.selectedMonth,
                    onPrevMonth = {
                        viewModel.onEvent(HomeEvent.ChangeMonth(uiState.selectedMonth.minusMonths(1)))
                    },
                    onNextMonth = {
                        viewModel.onEvent(HomeEvent.ChangeMonth(uiState.selectedMonth.plusMonths(1)))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (isEmpty) {
                item {
                    EmptyState(
                        title = stringResource(R.string.home_empty_title),
                        message = stringResource(R.string.home_empty_message),
                        actionLabel = stringResource(R.string.home_add_transaction),
                        onAction = onNavigateToAddTransaction,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                item {
                    AnimatedContent(
                        targetState = uiState.selectedMonth,
                        transitionSpec = {
                            val direction = if (targetState > initialState) 1 else -1
                            (slideInHorizontally { it * direction } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it * direction } + fadeOut())
                        },
                        label = "month_balance",
                    ) { _ ->
                        BalanceCard(
                            totalBalance = uiState.totalBalance,
                            monthIncome = uiState.monthIncome,
                            monthExpense = uiState.monthExpense,
                            currency = uiState.currency,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    MonthSummaryCard(
                        monthIncome = uiState.monthIncome,
                        monthExpense = uiState.monthExpense,
                        monthNet = uiState.monthNet,
                        currency = uiState.currency,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                uiState.latestInsight?.let { insight ->
                    item { Spacer(Modifier.height(12.dp)) }
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                            AiInsightCard(
                                insight = insight,
                                onTellMeMore = onNavigateToAiAssistant,
                                onDismiss = {
                                    viewModel.onEvent(HomeEvent.DismissInsight(insight.id))
                                },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }

                if (uiState.activeBudgets.isNotEmpty()) {
                    item { Spacer(Modifier.height(12.dp)) }
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            SectionHeader(title = stringResource(R.string.home_active_budgets))
                        }
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(uiState.activeBudgets) { budgetProgress ->
                                BudgetProgressCard(
                                    budgetProgress = budgetProgress,
                                    currency = uiState.currency,
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    TopCategoriesCard(
                        categories = uiState.topCategoriesThisMonth,
                        currency = uiState.currency,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    SectionHeader(
                        title = stringResource(R.string.home_recent_transactions),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        action = {
                            TextButton(onClick = onNavigateToTransactions) {
                                Text(stringResource(R.string.action_see_all))
                            }
                        },
                    )
                }

                item {
                    RecentTransactionsList(
                        transactions = uiState.recentTransactions,
                        categoriesById = uiState.categoriesById,
                        currency = uiState.currency,
                        onTransactionClick = onNavigateToTransactionDetail,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    QuickActionsRow(
                        onScanReceipt = onNavigateToReceiptScanner,
                        onAiChat = onNavigateToAiAssistant,
                        onAddRecurring = onNavigateToRecurring,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
        }   // PullToRefreshBox
        }   // else
    }       // Scaffold
}

@Composable
private fun HomeHeader(
    selectedMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = greeting(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.home_prev_month),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = DateUtils.formatMonth(selectedMonth),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.width(140.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.home_next_month),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onScanReceipt: () -> Unit,
    onAiChat: () -> Unit,
    onAddRecurring: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuickActionButton(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            label = stringResource(R.string.home_quick_scan_receipt),
            onClick = onScanReceipt,
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            icon = Icons.Default.AutoAwesome,
            label = stringResource(R.string.home_quick_ai_chat),
            onClick = onAiChat,
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            icon = Icons.Default.Repeat,
            label = stringResource(R.string.home_quick_recurring),
            onClick = onAddRecurring,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HomeLoadingShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(24.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(160.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(200.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(200.dp))
    }
}

@Composable
private fun greeting(): String = when (LocalTime.now().hour) {
    in 0..11 -> stringResource(R.string.greeting_morning)
    in 12..17 -> stringResource(R.string.greeting_afternoon)
    else -> stringResource(R.string.greeting_evening)
}
