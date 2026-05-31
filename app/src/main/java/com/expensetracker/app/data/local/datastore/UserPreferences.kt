package com.expensetracker.app.data.local.datastore

import com.expensetracker.app.core.designsystem.theme.ThemeMode

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currency: String = "VND",
    val hasCompletedOnboarding: Boolean = false,
    val defaultWalletId: Long = -1L,
    val geminiEnabled: Boolean = true,
    val monthStartDay: Int = 1,
)
