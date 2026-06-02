package com.expensetracker.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.expensetracker.app.feature.budget.screen.AddEditBudgetScreen
import com.expensetracker.app.feature.wallet.screen.AddEditWalletScreen
import com.expensetracker.app.feature.wallet.screen.WalletListScreen
import com.expensetracker.app.feature.recurring.screen.AddEditRecurringScreen
import com.expensetracker.app.feature.recurring.screen.RecurringListScreen
import com.expensetracker.app.feature.budget.screen.BudgetsScreen
import com.expensetracker.app.feature.category.screen.AddEditCategoryScreen
import com.expensetracker.app.feature.category.screen.CategoriesScreen
import com.expensetracker.app.feature.home.screen.HomeScreen
import com.expensetracker.app.feature.transaction.screen.AddEditTransactionScreen
import com.expensetracker.app.feature.transaction.screen.TransactionDetailScreen
import com.expensetracker.app.feature.ai_assistant.screen.AiAssistantScreen
import com.expensetracker.app.feature.ocr_scan.screen.ReceiptScannerScreen
import com.expensetracker.app.feature.onboarding.screen.OnboardingScreen
import com.expensetracker.app.feature.settings.screen.GeminiTestScreen
import com.expensetracker.app.feature.settings.screen.SettingsScreen
import com.expensetracker.app.feature.statistics.screen.StatisticsScreen
import com.expensetracker.app.feature.transaction.screen.TransactionListScreen

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
        enterTransition = { slideInHorizontally { it / 3 } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it / 3 } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 3 } + fadeOut() },
    ) {
        // ---- Bottom-nav screens ----
        composable<Home> {
            HomeScreen(
                onNavigateToTransactions = { navController.navigate(TransactionList) },
                onNavigateToAddTransaction = { navController.navigate(AddEditTransaction()) },
                onNavigateToTransactionDetail = { id -> navController.navigate(TransactionDetail(id)) },
                onNavigateToAiAssistant = { navController.navigate(AiAssistant) },
                onNavigateToReceiptScanner = { navController.navigate(ReceiptScanner) },
                onNavigateToRecurring = { navController.navigate(Recurring) },
                onNavigateToSettings = { navController.navigate(Settings) },
            )
        }
        composable<TransactionList> {
            TransactionListScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToDetail = { id -> navController.navigate(TransactionDetail(id)) },
                onNavigateToAddEdit = { id -> navController.navigate(AddEditTransaction(id)) },
            )
        }
        composable<Statistics> {
            StatisticsScreen(
                onNavigateToTransactions = { navController.navigate(TransactionList) },
            )
        }
        composable<AiAssistant> {
            AiAssistantScreen()
        }

        // ---- Transaction detail/edit ----
        composable<AddEditTransaction> {
            AddEditTransactionScreen(
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable<TransactionDetail> {
            TransactionDetailScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEdit = { id -> navController.navigate(AddEditTransaction(id)) },
            )
        }

        // ---- Feature screens ----
        composable<Categories> {
            CategoriesScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAddCategory = { navController.navigate(AddEditCategory()) },
                onNavigateToEditCategory = { id -> navController.navigate(AddEditCategory(id)) },
            )
        }
        composable<AddEditCategory> {
            AddEditCategoryScreen(
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable<Budgets> {
            BudgetsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAddBudget = { navController.navigate(AddEditBudget()) },
                onNavigateToEditBudget = { id -> navController.navigate(AddEditBudget(id)) },
            )
        }
        composable<AddEditBudget> {
            AddEditBudgetScreen(
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable<Recurring> {
            RecurringListScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAdd = { navController.navigate(AddEditRecurring()) },
                onNavigateToEdit = { id -> navController.navigate(AddEditRecurring(id)) },
            )
        }
        composable<AddEditRecurring> {
            AddEditRecurringScreen(
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable<ReceiptScanner> {
            ReceiptScannerScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAddTransaction = { amount, note, categoryId ->
                    navController.navigate(
                        AddEditTransaction(
                            prefillAmount = amount,
                            prefillNote = note,
                            prefillCategoryId = categoryId,
                        ),
                    )
                },
            )
        }
        composable<WalletManagement> {
            WalletListScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAddWallet = { navController.navigate(AddEditWallet()) },
                onNavigateToEditWallet = { id -> navController.navigate(AddEditWallet(id)) },
            )
        }
        composable<AddEditWallet> {
            AddEditWalletScreen(
                onNavigateBack = { navController.navigateUp() },
            )
        }

        // ---- App-level screens ----
        composable<Settings> {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToGeminiTest = { navController.navigate(GeminiTest) },
            )
        }
        composable<GeminiTest> {
            GeminiTestScreen(
                onNavigateBack = { navController.navigateUp() },
            )
        }
        composable<Onboarding> {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Home) {
                        popUpTo(Onboarding) { inclusive = true }
                    }
                },
            )
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
