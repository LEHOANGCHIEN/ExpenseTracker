package com.expensetracker.app.navigation

import kotlinx.serialization.Serializable

// ---- Bottom-nav root destinations ----
@Serializable data object Home
@Serializable data object TransactionList
@Serializable data object Statistics
@Serializable data object AiAssistant

// ---- Transaction screens ----
@Serializable data class AddEditTransaction(val id: Long? = null)
@Serializable data class TransactionDetail(val id: Long)

// ---- Feature screens ----
@Serializable data object Categories
@Serializable data object Budgets
@Serializable data object Recurring
@Serializable data object ReceiptScanner
@Serializable data object WalletManagement

// ---- App-level screens ----
@Serializable data object Settings
@Serializable data object Onboarding
