package com.expensetracker.app.domain.repository

import com.expensetracker.app.core.designsystem.theme.ThemeMode
import com.expensetracker.app.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCurrency(currency: String)
    suspend fun setHasCompletedOnboarding(completed: Boolean)
    suspend fun setDefaultWalletId(id: Long)
    suspend fun setGeminiEnabled(enabled: Boolean)
    suspend fun setMonthStartDay(day: Int)
}
