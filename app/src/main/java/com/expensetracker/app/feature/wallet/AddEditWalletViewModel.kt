package com.expensetracker.app.feature.wallet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.Wallet
import com.expensetracker.app.domain.repository.TransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val walletId: Long? = savedStateHandle.get<Long?>("id")

    private val _state = MutableStateFlow(AddEditWalletUiState())
    val state: StateFlow<AddEditWalletUiState> = _state

    init {
        if (walletId != null) {
            viewModelScope.launch {
                val w = walletRepository.getById(walletId)
                if (w != null) {
                    _state.update {
                        it.copy(
                            walletId = w.id,
                            name = w.name,
                            icon = w.icon,
                            color = w.color,
                            initialBalanceText = formatAmount(w.initialBalance),
                            currency = w.currency,
                            isEditing = true,
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditWalletEvent) {
        when (event) {
            is AddEditWalletEvent.NameChanged -> _state.update { it.copy(name = event.name.take(50)) }
            is AddEditWalletEvent.IconSelected -> _state.update { it.copy(icon = event.icon) }
            is AddEditWalletEvent.ColorSelected -> _state.update { it.copy(color = event.color) }
            is AddEditWalletEvent.BalanceChanged -> _state.update { it.copy(initialBalanceText = event.text) }
            is AddEditWalletEvent.CurrencyChanged -> _state.update { it.copy(currency = event.currency) }
            AddEditWalletEvent.Save -> save()
            AddEditWalletEvent.DeleteRequested -> _state.update { it.copy(showDeleteConfirm = true) }
            AddEditWalletEvent.DeleteConfirmed -> delete()
            AddEditWalletEvent.DeleteDismissed -> _state.update { it.copy(showDeleteConfirm = false) }
            AddEditWalletEvent.DismissDeleteBlocked -> _state.update { it.copy(deleteBlocked = false) }
        }
    }

    private fun save() {
        val s = _state.value
        if (s.name.isBlank()) return
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val wallet = Wallet(
                    id = s.walletId ?: 0L,
                    name = s.name.trim(),
                    icon = s.icon,
                    color = s.color,
                    initialBalance = s.initialBalance,
                    currency = s.currency,
                    createdAt = LocalDateTime.now(),
                )
                if (s.isEditing) walletRepository.update(wallet)
                else walletRepository.add(wallet)
                _state.update { it.copy(isSaving = false, isDone = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private fun delete() {
        val id = _state.value.walletId ?: return
        viewModelScope.launch {
            val txns = transactionRepository.observeByWallet(id).first()
            if (txns.isNotEmpty()) {
                _state.update { it.copy(showDeleteConfirm = false, deleteBlocked = true) }
            } else {
                walletRepository.delete(id)
                _state.update { it.copy(showDeleteConfirm = false, isDone = true) }
            }
        }
    }

    private fun formatAmount(amount: Double): String =
        if (amount == amount.toLong().toDouble()) amount.toLong().toString()
        else amount.toString()
}
