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
        val CURRENCY = stringPreferencesKey("currency")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val DEFAULT_WALLET_ID = longPreferencesKey("default_wallet_id")
        val GEMINI_ENABLED = booleanPreferencesKey("gemini_enabled")
        val MONTH_START_DAY = intPreferencesKey("month_start_day")
    }

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            UserPreferences(
                themeMode = prefs[Keys.THEME_MODE]
                    ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.SYSTEM,
                currency = prefs[Keys.CURRENCY] ?: "VND",
                hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
                defaultWalletId = prefs[Keys.DEFAULT_WALLET_ID] ?: -1L,
                geminiEnabled = prefs[Keys.GEMINI_ENABLED] ?: true,
                monthStartDay = prefs[Keys.MONTH_START_DAY] ?: 1,
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setCurrency(currency: String) {
        dataStore.edit { it[Keys.CURRENCY] = currency }
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = completed }
    }

    suspend fun setDefaultWalletId(id: Long) {
        dataStore.edit { it[Keys.DEFAULT_WALLET_ID] = id }
    }

    suspend fun setGeminiEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.GEMINI_ENABLED] = enabled }
    }

    suspend fun setMonthStartDay(day: Int) {
        dataStore.edit { it[Keys.MONTH_START_DAY] = day.coerceIn(1, 28) }
    }
}
