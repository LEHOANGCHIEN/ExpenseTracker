package com.expensetracker.app.data.repository

import com.expensetracker.app.core.designsystem.theme.ThemeMode
import com.expensetracker.app.data.local.datastore.UserPreferences
import com.expensetracker.app.data.local.datastore.UserPreferencesDataStore
import com.expensetracker.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
) : PreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.preferences

    override suspend fun setThemeMode(mode: ThemeMode) { dataStore.setThemeMode(mode) }
    override suspend fun setDynamicColorEnabled(enabled: Boolean) { dataStore.setDynamicColorEnabled(enabled) }
    override suspend fun setCurrency(currency: String) { dataStore.setCurrency(currency) }
    override suspend fun setHasCompletedOnboarding(completed: Boolean) { dataStore.setHasCompletedOnboarding(completed) }
    override suspend fun setDefaultWalletId(id: Long) { dataStore.setDefaultWalletId(id) }
    override suspend fun setGeminiEnabled(enabled: Boolean) { dataStore.setGeminiEnabled(enabled) }
    override suspend fun setGeminiApiKeyOverride(key: String) { dataStore.setGeminiApiKeyOverride(key) }
    override suspend fun setMonthStartDay(day: Int) { dataStore.setMonthStartDay(day) }
    override suspend fun setDailyReminderEnabled(enabled: Boolean) { dataStore.setDailyReminderEnabled(enabled) }
    override suspend fun setBudgetAlertsEnabled(enabled: Boolean) { dataStore.setBudgetAlertsEnabled(enabled) }
    override suspend fun setLanguage(language: String) { dataStore.setLanguage(language) }
}
