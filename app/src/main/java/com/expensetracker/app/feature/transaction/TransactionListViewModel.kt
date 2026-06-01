package com.expensetracker.app.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(TransactionFilter())
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _pendingDelete = MutableStateFlow<Transaction?>(null)
    private val _showFilterSheet = MutableStateFlow(false)

    val uiState: StateFlow<TransactionListUiState> = combine(
        combine(
            transactionRepository.observeAll(),
            categoryRepository.observeAll(),
            walletRepository.observeAll(),
        ) { txns, cats, wallets -> Triple(txns, cats, wallets) },
        combine(
            _searchQuery,
            _filter,
            preferencesRepository.preferences,
        ) { query, filter, prefs -> Triple(query, filter, prefs) },
        combine(_selectedIds, _pendingDelete, _showFilterSheet) { sel, del, show ->
            Triple(sel, del, show)
        },
    ) { (txns, cats, wallets), (query, filter, prefs), (selected, pendingDel, showFilter) ->
        val catMap = cats.associateBy { it.id }
        val walletMap = wallets.associateBy { it.id }

        val filtered = txns.filter { txn ->
            val matchesQuery = query.isEmpty() || txn.note.contains(query, ignoreCase = true)
            val matchesDateRange = when {
                filter.startDate != null && filter.endDate != null ->
                    txn.date >= filter.startDate && txn.date <= filter.endDate
                filter.startDate != null -> txn.date >= filter.startDate
                filter.endDate != null -> txn.date <= filter.endDate
                else -> true
            }
            val matchesCategory = filter.categoryIds.isEmpty() || txn.categoryId in filter.categoryIds
            val matchesWallet = filter.walletIds.isEmpty() || txn.walletId in filter.walletIds
            val matchesType = filter.types.isEmpty() || txn.type in filter.types
            val matchesMin = filter.minAmount == null || txn.amount >= filter.minAmount
            val matchesMax = filter.maxAmount == null || txn.amount <= filter.maxAmount
            matchesQuery && matchesDateRange && matchesCategory && matchesWallet &&
                matchesType && matchesMin && matchesMax
        }.sortedByDescending { it.date }

        TransactionListUiState(
            groupedTransactions = filtered.groupBy { it.date },
            allTransactions = filtered,
            categories = catMap,
            wallets = walletMap,
            allCategories = cats,
            searchQuery = query,
            filter = filter,
            isLoading = false,
            selectedIds = selected,
            pendingDeleteTransaction = pendingDel,
            currency = prefs.currency,
            showFilterSheet = showFilter,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionListUiState(isLoading = true),
    )

    fun onEvent(event: TransactionListUiEvent) {
        when (event) {
            is TransactionListUiEvent.SearchQueryChanged ->
                _searchQuery.value = event.query
            is TransactionListUiEvent.FilterChanged -> {
                _filter.value = event.filter
                _showFilterSheet.value = false
            }
            is TransactionListUiEvent.DeleteTransaction -> deleteTransaction(event.id)
            TransactionListUiEvent.UndoDelete -> undoDelete()
            is TransactionListUiEvent.ToggleSelect -> _selectedIds.update { sel ->
                if (event.id in sel) sel - event.id else sel + event.id
            }
            TransactionListUiEvent.ClearSelection -> _selectedIds.value = emptySet()
            is TransactionListUiEvent.BulkDelete -> bulkDelete(event.ids)
            TransactionListUiEvent.ShowFilterSheet -> _showFilterSheet.value = true
            TransactionListUiEvent.HideFilterSheet -> _showFilterSheet.value = false
        }
    }

    private fun deleteTransaction(id: Long) {
        val transaction = uiState.value.allTransactions.find { it.id == id } ?: return
        _pendingDelete.value = transaction
        viewModelScope.launch {
            transactionRepository.delete(id)
        }
    }

    private fun undoDelete() {
        val transaction = _pendingDelete.value ?: return
        _pendingDelete.value = null
        viewModelScope.launch {
            transactionRepository.add(transaction)
        }
    }

    private fun bulkDelete(ids: Set<Long>) {
        viewModelScope.launch {
            ids.forEach { id -> transactionRepository.delete(id) }
            _selectedIds.value = emptySet()
        }
    }
}
