package com.expensetracker.app.feature.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.core.designsystem.theme.ThemeMode
import com.expensetracker.app.core.util.LocaleHelper
import com.expensetracker.app.data.backup.BackupService
import com.expensetracker.app.data.local.datastore.UserPreferences
import com.expensetracker.app.data.local.database.AppDatabase
import com.expensetracker.app.domain.repository.AuthRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SettingsActionState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null,
    val showClearConfirm: Boolean = false,
    val showImportConfirm: Boolean = false,
    val pendingImportUri: Uri? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val backupService: BackupService,
    private val appDatabase: AppDatabase,
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val currentUserEmail: String?
        get() = authRepository.currentUser?.email

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences(),
        )

    private val _actionState = MutableStateFlow(SettingsActionState())
    val actionState: StateFlow<SettingsActionState> = _actionState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { preferencesRepository.setDynamicColorEnabled(enabled) }
    fun setCurrency(currency: String) = viewModelScope.launch { preferencesRepository.setCurrency(currency) }
    fun setMonthStartDay(day: Int) = viewModelScope.launch { preferencesRepository.setMonthStartDay(day) }
    fun setGeminiEnabled(enabled: Boolean) = viewModelScope.launch { preferencesRepository.setGeminiEnabled(enabled) }
    fun setGeminiApiKeyOverride(key: String) = viewModelScope.launch { preferencesRepository.setGeminiApiKeyOverride(key) }
    fun setDailyReminderEnabled(enabled: Boolean) = viewModelScope.launch { preferencesRepository.setDailyReminderEnabled(enabled) }
    fun setBudgetAlertsEnabled(enabled: Boolean) = viewModelScope.launch { preferencesRepository.setBudgetAlertsEnabled(enabled) }
    fun setLanguage(language: String) {
        LocaleHelper.setLanguage(context, language)
        viewModelScope.launch { preferencesRepository.setLanguage(language) }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _actionState.update { it.copy(isExporting = true, error = null) }
            backupService.export(uri)
                .onSuccess { _actionState.update { it.copy(isExporting = false, exportSuccess = true) } }
                .onFailure { e -> _actionState.update { it.copy(isExporting = false, error = "Export failed: ${e.message}") } }
        }
    }

    fun onImportSelected(uri: Uri) {
        _actionState.update { it.copy(showImportConfirm = true, pendingImportUri = uri) }
    }

    fun confirmImport() {
        val uri = _actionState.value.pendingImportUri ?: return
        viewModelScope.launch {
            _actionState.update { it.copy(isImporting = true, showImportConfirm = false, error = null) }
            backupService.import(uri)
                .mapCatching { backup -> backupService.restoreFrom(backup).getOrThrow() }
                .onSuccess { _actionState.update { it.copy(isImporting = false, importSuccess = true) } }
                .onFailure { e -> _actionState.update { it.copy(isImporting = false, error = "Import failed: ${e.message}") } }
        }
    }

    fun dismissImportConfirm() = _actionState.update { it.copy(showImportConfirm = false, pendingImportUri = null) }

    fun requestClearData() = _actionState.update { it.copy(showClearConfirm = true) }
    fun dismissClearConfirm() = _actionState.update { it.copy(showClearConfirm = false) }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                Log.d("CLEAR_DATA", "BUTTON_PRESSED")

                withContext(Dispatchers.IO) {
                    appDatabase.clearAllTables()
                }

                Log.d("CLEAR_DATA", "SUCCESS")
            } catch (e: Exception) {
                Log.e("CLEAR_DATA", "FAILED", e)
            }

            _actionState.update {
                it.copy(showClearConfirm = false)
            }
        }
    }

    fun clearError() = _actionState.update { it.copy(error = null, exportSuccess = false, importSuccess = false) }
}
