package com.expensetracker.app.feature.transaction

import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.model.Wallet
import java.time.LocalDate

data class TransactionFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val categoryIds: Set<Long> = emptySet(),
    val walletIds: Set<Long> = emptySet(),
    val types: Set<TransactionType> = emptySet(),
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
) {
    val isActive: Boolean
        get() = startDate != null || endDate != null ||
            categoryIds.isNotEmpty() || walletIds.isNotEmpty() ||
            types.isNotEmpty() || minAmount != null || maxAmount != null
}

data class TransactionListUiState(
    val groupedTransactions: Map<LocalDate, List<Transaction>> = emptyMap(),
    val allTransactions: List<Transaction> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val wallets: Map<Long, Wallet> = emptyMap(),
    val allCategories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val filter: TransactionFilter = TransactionFilter(),
    val isLoading: Boolean = true,
    val selectedIds: Set<Long> = emptySet(),
    val pendingDeleteTransaction: Transaction? = null,
    val currency: String = "VND",
    val showFilterSheet: Boolean = false,
) {
    val isInSelectionMode: Boolean get() = selectedIds.isNotEmpty()
    val isEmpty: Boolean get() = !isLoading && groupedTransactions.isEmpty()
}

sealed interface TransactionListUiEvent {
    data class SearchQueryChanged(val query: String) : TransactionListUiEvent
    data class FilterChanged(val filter: TransactionFilter) : TransactionListUiEvent
    data class DeleteTransaction(val id: Long) : TransactionListUiEvent
    data object UndoDelete : TransactionListUiEvent
    data class ToggleSelect(val id: Long) : TransactionListUiEvent
    data object ClearSelection : TransactionListUiEvent
    data class BulkDelete(val ids: Set<Long>) : TransactionListUiEvent
    data object ShowFilterSheet : TransactionListUiEvent
    data object HideFilterSheet : TransactionListUiEvent
}
