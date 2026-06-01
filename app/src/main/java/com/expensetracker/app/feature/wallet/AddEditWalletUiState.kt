package com.expensetracker.app.feature.wallet

val WALLET_ICONS = listOf("💵", "🏦", "💳", "💰", "📱", "🏧", "💼", "🐖", "📊", "🔐")

val WALLET_CURRENCIES = listOf("VND", "USD", "EUR", "GBP", "JPY", "KRW", "CNY", "SGD", "THB", "AUD")

data class AddEditWalletUiState(
    val walletId: Long? = null,
    val name: String = "",
    val icon: String = "💵",
    val color: String = "#26A69A",
    val initialBalanceText: String = "0",
    val currency: String = "VND",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val deleteBlocked: Boolean = false,
    val error: String? = null,
) {
    val initialBalance: Double get() = initialBalanceText.toDoubleOrNull() ?: 0.0
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

sealed interface AddEditWalletEvent {
    data class NameChanged(val name: String) : AddEditWalletEvent
    data class IconSelected(val icon: String) : AddEditWalletEvent
    data class ColorSelected(val color: String) : AddEditWalletEvent
    data class BalanceChanged(val text: String) : AddEditWalletEvent
    data class CurrencyChanged(val currency: String) : AddEditWalletEvent
    data object Save : AddEditWalletEvent
    data object DeleteRequested : AddEditWalletEvent
    data object DeleteConfirmed : AddEditWalletEvent
    data object DeleteDismissed : AddEditWalletEvent
    data object DismissDeleteBlocked : AddEditWalletEvent
}
