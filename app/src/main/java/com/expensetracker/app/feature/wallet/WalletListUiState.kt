package com.expensetracker.app.feature.wallet

import com.expensetracker.app.domain.model.Wallet

data class WalletItem(
    val wallet: Wallet,
    val balance: Double,
)

data class WalletListUiState(
    val wallets: List<WalletItem> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = true,
    val currency: String = "VND",
)
