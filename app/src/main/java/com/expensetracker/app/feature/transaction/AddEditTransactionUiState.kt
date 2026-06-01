package com.expensetracker.app.feature.transaction

import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.model.Wallet
import java.time.LocalDate

data class SplitPart(
    val tempId: Int = 0,
    val categoryId: Long? = null,
    val amount: Double = 0.0,
    val note: String = "",
)

data class AddEditTransactionUiState(
    val transactionId: Long? = null,
    val amountExpression: String = "0",
    val resolvedAmount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val walletId: Long? = null,
    val toWalletId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val photoUri: String? = null,
    val tags: List<String> = emptyList(),
    val tagInput: String = "",
    val recurringId: Long? = null,
    val parentSplitId: Long? = null,
    val splitParts: List<SplitPart> = emptyList(),
    val isSplitEnabled: Boolean = false,
    val showAdvanced: Boolean = false,
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val currency: String = "VND",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val isRecurringChild: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showDatePicker: Boolean = false,
    val showSplitDialog: Boolean = false,
    val isDone: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean
        get() = resolvedAmount > 0.0 && categoryId != null && !isSaving &&
            (type != TransactionType.TRANSFER || toWalletId != null)

    val filteredCategories: List<Category>
        get() = categories.filter { !it.isArchived && it.type == type }
}

sealed interface AddEditUiEvent {
    data class AmountKeyPressed(val key: String) : AddEditUiEvent
    data class TypeChanged(val type: TransactionType) : AddEditUiEvent
    data class CategorySelected(val categoryId: Long) : AddEditUiEvent
    data class WalletSelected(val walletId: Long) : AddEditUiEvent
    data class ToWalletSelected(val walletId: Long) : AddEditUiEvent
    data class DateChanged(val date: LocalDate) : AddEditUiEvent
    data class NoteChanged(val note: String) : AddEditUiEvent
    data class PhotoSelected(val uri: String) : AddEditUiEvent
    data class TagInputChanged(val input: String) : AddEditUiEvent
    data object AddTag : AddEditUiEvent
    data class RemoveTag(val tag: String) : AddEditUiEvent
    data object Save : AddEditUiEvent
    data object DeleteRequested : AddEditUiEvent
    data object DeleteConfirmed : AddEditUiEvent
    data object DeleteDismissed : AddEditUiEvent
    data object ShowCategoryPicker : AddEditUiEvent
    data object HideCategoryPicker : AddEditUiEvent
    data object ShowDatePicker : AddEditUiEvent
    data object HideDatePicker : AddEditUiEvent
    data object ShowSplitDialog : AddEditUiEvent
    data object HideSplitDialog : AddEditUiEvent
    data class SplitConfirmed(val parts: List<SplitPart>) : AddEditUiEvent
    data object ToggleAdvanced : AddEditUiEvent
}
