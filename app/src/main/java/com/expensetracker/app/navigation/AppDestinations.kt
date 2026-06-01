package com.expensetracker.app.navigation

import kotlinx.serialization.Serializable

// ---- Bottom-nav root destinations ----
@Serializable data object Home
@Serializable data object TransactionList
@Serializable data object Statistics
@Serializable data object AiAssistant

// ---- Transaction screens ----
@Serializable data class AddEditTransaction(
    val id: Long? = null,
    val prefillAmount: Double? = null,
    val prefillNote: String? = null,
    val prefillCategoryId: Long? = null,
)
@Serializable data class TransactionDetail(val id: Long)

// ---- Feature screens ----
@Serializable data object Categories
@Serializable data class AddEditCategory(val id: Long? = null)
@Serializable data object Budgets
@Serializable data class AddEditBudget(val id: Long? = null)
@Serializable data object Recurring
@Serializable data class AddEditRecurring(val id: Long? = null)
@Serializable data object ReceiptScanner
@Serializable data object WalletManagement
@Serializable data class AddEditWallet(val id: Long? = null)

// ---- App-level screens ----
@Serializable data object Settings
@Serializable data object Onboarding
@Serializable data object GeminiTest
