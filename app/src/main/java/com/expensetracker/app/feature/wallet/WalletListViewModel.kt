package com.expensetracker.app.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WalletListViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<WalletListUiState> = combine(
        walletRepository.observeAll(),
        transactionRepository.observeAll(),
        preferencesRepository.preferences,
    ) { wallets, allTransactions, prefs ->
        val nonSplit = allTransactions.filter { it.parentSplitId == null }

        val walletItems = wallets.map { wallet ->
            val balance = wallet.initialBalance + nonSplit.sumOf { txn ->
                when {
                    txn.type == TransactionType.INCOME && txn.walletId == wallet.id -> txn.amount
                    txn.type == TransactionType.EXPENSE && txn.walletId == wallet.id -> -txn.amount
                    txn.type == TransactionType.TRANSFER && txn.walletId == wallet.id -> -txn.amount
                    txn.type == TransactionType.TRANSFER && txn.toWalletId == wallet.id -> txn.amount
                    else -> 0.0
                }
            }
            WalletItem(wallet = wallet, balance = balance)
        }

        WalletListUiState(
            wallets = walletItems,
            totalBalance = walletItems.sumOf { it.balance },
            isLoading = false,
            currency = prefs.currency,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WalletListUiState(isLoading = true),
    )
}
