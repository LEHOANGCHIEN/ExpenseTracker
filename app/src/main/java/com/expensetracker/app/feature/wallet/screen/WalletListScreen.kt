package com.expensetracker.app.feature.wallet.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.core.designsystem.component.EmptyState
import com.expensetracker.app.core.designsystem.component.ShimmerBox
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.feature.wallet.WalletListViewModel
import com.expensetracker.app.feature.wallet.screen.component.WalletCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddWallet: () -> Unit,
    onNavigateToEditWallet: (Long) -> Unit,
    viewModel: WalletListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallets") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddWallet,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, "Add Wallet")
            }
        },
    ) { padding ->
        when {
            state.isLoading -> WalletShimmer(Modifier.padding(padding))
            state.wallets.isEmpty() -> EmptyState(
                title = "No wallets yet",
                message = "Add a wallet to start tracking your money",
                actionLabel = "Add Wallet",
                onAction = onNavigateToAddWallet,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.wallets, key = { it.wallet.id }) { item ->
                    WalletCard(
                        item = item,
                        onClick = { onNavigateToEditWallet(item.wallet.id) },
                    )
                }

                item {
                    TotalBalanceCard(
                        totalBalance = state.totalBalance,
                        currency = state.currency,
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalBalanceCard(
    totalBalance: Double,
    currency: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = CurrencyFormatter.format(totalBalance, currency),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun WalletShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(3) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
        }
    }
}
