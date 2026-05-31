package com.expensetracker.app.feature.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.expensetracker.app.core.designsystem.component.AppPrimaryButton
import com.expensetracker.app.core.designsystem.component.AppTextField
import com.expensetracker.app.navigation.AddEditTransaction
import com.expensetracker.app.navigation.AiAssistant
import com.expensetracker.app.navigation.AppNavHost
import com.expensetracker.app.navigation.Budgets
import com.expensetracker.app.navigation.Categories
import com.expensetracker.app.navigation.Home
import com.expensetracker.app.navigation.Onboarding
import com.expensetracker.app.navigation.ReceiptScanner
import com.expensetracker.app.navigation.Recurring
import com.expensetracker.app.navigation.Settings
import com.expensetracker.app.navigation.Statistics
import com.expensetracker.app.navigation.TransactionDetail
import com.expensetracker.app.navigation.TransactionList
import com.expensetracker.app.navigation.WalletManagement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell() {
    val navController = rememberNavController()
    var showQuickAdd by remember { mutableStateOf(false) }

    val currentEntry by navController.currentBackStackEntryAsState()
    val currentDest = currentEntry?.destination

    val showBottomBar = currentDest.isTabDestination()

    Scaffold(
        topBar = {
            AppTopBar(
                currentDest = currentDest,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToSettings = { navController.navigate(Settings) },
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                AppBottomBar(
                    currentDest = currentDest,
                    navController = navController,
                    onQuickAdd = { showQuickAdd = true },
                )
            }
        },
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
        )
    }

    if (showQuickAdd) {
        QuickAddBottomSheet(onDismiss = { showQuickAdd = false })
    }
}

// ---- Top App Bar ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentDest: NavDestination?,
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val (title, isTopLevel) = topBarInfo(currentDest)

    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (!isTopLevel) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
        actions = {
            if (isTopLevel) {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

private fun topBarInfo(dest: NavDestination?): Pair<String, Boolean> = when {
    dest?.hasRoute(Home::class) == true -> "Expense Tracker" to true
    dest?.hasRoute(TransactionList::class) == true -> "Transactions" to true
    dest?.hasRoute(Statistics::class) == true -> "Statistics" to true
    dest?.hasRoute(AiAssistant::class) == true -> "AI Assistant ✨" to true
    dest?.hasRoute(AddEditTransaction::class) == true -> "Add Transaction" to false
    dest?.hasRoute(TransactionDetail::class) == true -> "Transaction" to false
    dest?.hasRoute(Categories::class) == true -> "Categories" to false
    dest?.hasRoute(Budgets::class) == true -> "Budgets" to false
    dest?.hasRoute(Recurring::class) == true -> "Recurring" to false
    dest?.hasRoute(ReceiptScanner::class) == true -> "Scan Receipt" to false
    dest?.hasRoute(WalletManagement::class) == true -> "Wallets" to false
    dest?.hasRoute(Settings::class) == true -> "Settings" to false
    dest?.hasRoute(Onboarding::class) == true -> "Get Started" to false
    else -> "Expense Tracker" to true
}

// ---- Bottom Navigation Bar ----

@Composable
private fun AppBottomBar(
    currentDest: NavDestination?,
    navController: NavController,
    onQuickAdd: () -> Unit,
) {
    Box {
        NavigationBar {
            NavigationBarItem(
                selected = currentDest?.hasRoute(Home::class) == true,
                onClick = { navController.navigateToTab(Home) },
                icon = {
                    Icon(
                        imageVector = if (currentDest?.hasRoute(Home::class) == true) {
                            Icons.Filled.Home
                        } else Icons.Outlined.Home,
                        contentDescription = "Home",
                    )
                },
                label = { Text("Home") },
            )
            NavigationBarItem(
                selected = currentDest?.hasRoute(TransactionList::class) == true,
                onClick = { navController.navigateToTab(TransactionList) },
                icon = {
                    Icon(
                        imageVector = if (currentDest?.hasRoute(TransactionList::class) == true) {
                            Icons.AutoMirrored.Filled.ReceiptLong
                        } else Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = "Transactions",
                    )
                },
                label = { Text("Transactions") },
            )

            // Center placeholder for the FAB (equal weight to keep symmetry)
            Box(modifier = Modifier.weight(1f))

            NavigationBarItem(
                selected = currentDest?.hasRoute(Statistics::class) == true,
                onClick = { navController.navigateToTab(Statistics) },
                icon = {
                    Icon(
                        imageVector = if (currentDest?.hasRoute(Statistics::class) == true) {
                            Icons.Filled.BarChart
                        } else Icons.Outlined.BarChart,
                        contentDescription = "Statistics",
                    )
                },
                label = { Text("Stats") },
            )
            NavigationBarItem(
                selected = currentDest?.hasRoute(AiAssistant::class) == true,
                onClick = { navController.navigateToTab(AiAssistant) },
                icon = {
                    Icon(
                        imageVector = if (currentDest?.hasRoute(AiAssistant::class) == true) {
                            Icons.Filled.AutoAwesome
                        } else Icons.Outlined.AutoAwesome,
                        contentDescription = "AI",
                    )
                },
                label = { Text("AI") },
            )
        }

        // Centered Quick-Add FAB floating above the navigation bar
        FloatingActionButton(
            onClick = onQuickAdd,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Quick Add",
            )
        }
    }
}

// ---- Quick-Add Bottom Sheet (stub) ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = "Quick Add",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))

            var amount by remember { mutableStateOf("") }
            var note by remember { mutableStateOf("") }

            AppTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount",
                placeholder = "0",
            )
            Spacer(Modifier.height(8.dp))
            AppTextField(
                value = note,
                onValueChange = { note = it },
                label = "Note",
                placeholder = "What was this for?",
            )
            Spacer(Modifier.height(16.dp))

            AppPrimaryButton(
                text = "Save",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )

            // Bottom padding for gesture navigation
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ---- Helpers ----

private fun NavDestination?.isTabDestination(): Boolean =
    this?.hasRoute(Home::class) == true ||
        this?.hasRoute(TransactionList::class) == true ||
        this?.hasRoute(Statistics::class) == true ||
        this?.hasRoute(AiAssistant::class) == true

private fun <T : Any> NavController.navigateToTab(route: T) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
