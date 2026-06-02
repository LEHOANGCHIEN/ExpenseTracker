package com.expensetracker.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.expensetracker.app.core.designsystem.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
        val CURRENCY = stringPreferencesKey("currency")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val DEFAULT_WALLET_ID = longPreferencesKey("default_wallet_id")
        val GEMINI_ENABLED = booleanPreferencesKey("gemini_enabled")
        val GEMINI_API_KEY_OVERRIDE = stringPreferencesKey("gemini_api_key_override")
        val MONTH_START_DAY = intPreferencesKey("month_start_day")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val BUDGET_ALERTS_ENABLED = booleanPreferencesKey("budget_alerts_enabled")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            UserPreferences(
                themeMode = prefs[Keys.THEME_MODE]
                    ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.SYSTEM,
                dynamicColorEnabled = prefs[Keys.DYNAMIC_COLOR] ?: true,
                currency = prefs[Keys.CURRENCY] ?: "VND",
                hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
                defaultWalletId = prefs[Keys.DEFAULT_WALLET_ID] ?: -1L,
                geminiEnabled = prefs[Keys.GEMINI_ENABLED] ?: true,
                geminiApiKeyOverride = prefs[Keys.GEMINI_API_KEY_OVERRIDE] ?: "",
                monthStartDay = prefs[Keys.MONTH_START_DAY] ?: 1,
                dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: true,
                budgetAlertsEnabled = prefs[Keys.BUDGET_ALERTS_ENABLED] ?: true,
                language = prefs[Keys.LANGUAGE] ?: "en",
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) =
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }

    suspend fun setDynamicColorEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }

    suspend fun setCurrency(currency: String) =
        dataStore.edit { it[Keys.CURRENCY] = currency }

    suspend fun setHasCompletedOnboarding(completed: Boolean) =
        dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = completed }

    suspend fun setDefaultWalletId(id: Long) =
        dataStore.edit { it[Keys.DEFAULT_WALLET_ID] = id }

    suspend fun setGeminiEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.GEMINI_ENABLED] = enabled }

    suspend fun setGeminiApiKeyOverride(key: String) =
        dataStore.edit { it[Keys.GEMINI_API_KEY_OVERRIDE] = key }

    suspend fun setMonthStartDay(day: Int) =
        dataStore.edit { it[Keys.MONTH_START_DAY] = day.coerceIn(1, 28) }

    suspend fun setDailyReminderEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.DAILY_REMINDER_ENABLED] = enabled }

    suspend fun setBudgetAlertsEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.BUDGET_ALERTS_ENABLED] = enabled }

    suspend fun setLanguage(language: String) =
        dataStore.edit { it[Keys.LANGUAGE] = language }
}
