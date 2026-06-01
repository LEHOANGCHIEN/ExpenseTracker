package com.expensetracker.app.feature.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.Wallet
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val category: Category? = null,
    val wallet: Wallet? = null,
    val splitChildren: List<Transaction> = emptyList(),
    val childCategories: Map<Long, Category> = emptyMap(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val currency: String = "VND",
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val transactionId: Long = requireNotNull(savedStateHandle.get<Long>("id"))

    private val _state = MutableStateFlow(TransactionDetailUiState())
    val state: StateFlow<TransactionDetailUiState> = _state

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferences.first()
            val transaction = transactionRepository.getById(transactionId)
            if (transaction == null) {
                _state.update { it.copy(isLoading = false, isDeleted = true) }
                return@launch
            }

            val category = transaction.categoryId.let { categoryRepository.getById(it) }
            val wallet = walletRepository.getById(transaction.walletId)

            _state.update {
                it.copy(
                    transaction = transaction,
                    category = category,
                    wallet = wallet,
                    currency = prefs.currency,
                    isLoading = false,
                )
            }

            // Observe split children reactively
            transactionRepository.observeSplitChildren(transactionId).collect { children ->
                val cats = categoryRepository.observeAll().first()
                val catMap = cats.associateBy { it.id }
                _state.update { it.copy(splitChildren = children, childCategories = catMap) }
            }
        }
    }

    fun requestDelete() = _state.update { it.copy(showDeleteConfirm = true) }
    fun dismissDelete() = _state.update { it.copy(showDeleteConfirm = false) }

    fun confirmDelete() {
        viewModelScope.launch {
            transactionRepository.delete(transactionId)
            _state.update { it.copy(showDeleteConfirm = false, isDeleted = true) }
        }
    }
}
