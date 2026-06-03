package com.expensetracker.app.data.local.datastore

import com.expensetracker.app.core.designsystem.theme.ThemeMode

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val currency: String = "VND",
    val hasCompletedOnboarding: Boolean = false,
    val defaultWalletId: Long = -1L,
    val geminiEnabled: Boolean = true,
    val geminiApiKeyOverride: String = "",
    val monthStartDay: Int = 1,
    val dailyReminderEnabled: Boolean = true,
    val budgetAlertsEnabled: Boolean = true,
    val language: String = "vi",
)
