package com.expensetracker.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Any = Home,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // ---- Bottom-nav screens (placeholders until Tasks 6, 7, 12, 14) ----
        composable<Home> {
            PlaceholderScreen("Home Dashboard")
        }
        composable<TransactionList> {
            PlaceholderScreen("Transactions")
        }
        composable<Statistics> {
            PlaceholderScreen("Statistics")
        }
        composable<AiAssistant> {
            PlaceholderScreen("AI Assistant ✨")
        }

        // ---- Transaction detail/edit ----
        composable<AddEditTransaction> { backStackEntry ->
            val dest: AddEditTransaction = backStackEntry.toRoute()
            PlaceholderScreen(if (dest.id == null) "Add Transaction" else "Edit Transaction #${dest.id}")
        }
        composable<TransactionDetail> { backStackEntry ->
            val dest: TransactionDetail = backStackEntry.toRoute()
            PlaceholderScreen("Transaction Detail #${dest.id}")
        }

        // ---- Feature screens ----
        composable<Categories> {
            PlaceholderScreen("Categories")
        }
        composable<Budgets> {
            PlaceholderScreen("Budgets")
        }
        composable<Recurring> {
            PlaceholderScreen("Recurring Transactions")
        }
        composable<ReceiptScanner> {
            PlaceholderScreen("Receipt Scanner")
        }
        composable<WalletManagement> {
            PlaceholderScreen("Wallets")
        }

        // ---- App-level screens ----
        composable<Settings> {
            PlaceholderScreen("Settings")
        }
        composable<Onboarding> {
            PlaceholderScreen("Onboarding")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            modifier = Modifier.wrapContentSize(),
        )
    }
}
