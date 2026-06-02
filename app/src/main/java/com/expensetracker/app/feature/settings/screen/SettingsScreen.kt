package com.expensetracker.app.feature.settings.screen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.BuildConfig
import com.expensetracker.app.R
import com.expensetracker.app.core.designsystem.theme.ThemeMode
import com.expensetracker.app.feature.settings.SettingsViewModel
import java.time.LocalDateTime

private val CURRENCIES = listOf("VND", "USD", "EUR", "GBP", "JPY", "SGD", "THB")

private data class LanguageOption(val code: String, val labelRes: Int)

private val LANGUAGES = listOf(
    LanguageOption("en", R.string.settings_language_english),
    LanguageOption("vi", R.string.settings_language_vietnamese),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToGeminiTest: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val exportSuccessMsg = stringResource(R.string.settings_export_success)
    val importSuccessMsg = stringResource(R.string.settings_import_success)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportData(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.onImportSelected(it) } }

    LaunchedEffect(actionState.error, actionState.exportSuccess, actionState.importSuccess) {
        when {
            actionState.exportSuccess -> { snackbarHostState.showSnackbar(exportSuccessMsg); viewModel.clearError() }
            actionState.importSuccess -> { snackbarHostState.showSnackbar(importSuccessMsg); viewModel.clearError() }
            actionState.error != null -> { snackbarHostState.showSnackbar(actionState.error!!); viewModel.clearError() }
        }
    }

    // Import confirm dialog
    if (actionState.showImportConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissImportConfirm,
            title = { Text(stringResource(R.string.settings_import_title)) },
            text = { Text(stringResource(R.string.settings_import_message)) },
            confirmButton = { TextButton(onClick = viewModel::confirmImport) { Text(stringResource(R.string.settings_import_confirm)) } },
            dismissButton = { TextButton(onClick = viewModel::dismissImportConfirm) { Text(stringResource(R.string.action_cancel)) } },
        )
    }

    // Clear all confirm dialog
    if (actionState.showClearConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearConfirm,
            title = { Text(stringResource(R.string.settings_clear_data_title)) },
            text = { Text(stringResource(R.string.settings_clear_data_message)) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::clearAllData,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.settings_delete_everything)) }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissClearConfirm) { Text(stringResource(R.string.action_cancel)) } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ---- Language ----
            SettingsSection(stringResource(R.string.settings_section_language)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LANGUAGES.forEach { lang ->
                        FilterChip(
                            selected = prefs.language == lang.code,
                            onClick = {
                                if (prefs.language != lang.code) {
                                    viewModel.setLanguage(lang.code)
                                    (context as? Activity)?.recreate()
                                }
                            },
                            label = { Text(stringResource(lang.labelRes)) },
                        )
                    }
                }
            }

            // ---- Preferences ----
            SettingsSection(stringResource(R.string.settings_section_preferences)) {
                var currencyExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it },
                ) {
                    OutlinedTextField(
                        value = prefs.currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_currency)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(currencyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                        CURRENCIES.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { viewModel.setCurrency(c); currencyExpanded = false })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                var dayExpanded by remember { mutableStateOf(false) }
                val dayLabel = stringResource(R.string.settings_day_number, prefs.monthStartDay)
                ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = it }) {
                    OutlinedTextField(
                        value = dayLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_month_starts_on)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dayExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                        (1..28).forEach { day ->
                            val dayItemLabel = stringResource(R.string.settings_day_number, day)
                            DropdownMenuItem(text = { Text(dayItemLabel) }, onClick = { viewModel.setMonthStartDay(day); dayExpanded = false })
                        }
                    }
                }
            }

            // ---- Appearance ----
            SettingsSection(stringResource(R.string.settings_section_appearance)) {
                Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = prefs.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_dynamic_colors),
                    subtitle = stringResource(R.string.settings_dynamic_colors_subtitle),
                    checked = prefs.dynamicColorEnabled,
                    onCheckedChange = viewModel::setDynamicColor,
                )
            }

            // ---- AI ----
            SettingsSection(stringResource(R.string.settings_section_ai)) {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_ai_enabled),
                    subtitle = stringResource(R.string.settings_ai_enabled_subtitle),
                    checked = prefs.geminiEnabled,
                    onCheckedChange = viewModel::setGeminiEnabled,
                )
                Spacer(Modifier.height(8.dp))
                var apiKeyInput by remember(prefs.geminiApiKeyOverride) { mutableStateOf(prefs.geminiApiKeyOverride) }
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text(stringResource(R.string.settings_gemini_api_key)) },
                    placeholder = { Text(stringResource(R.string.settings_gemini_api_key_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text(stringResource(R.string.settings_gemini_api_key_hint)) },
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onNavigateToGeminiTest) { Text(stringResource(R.string.settings_test_gemini)) }
                    Button(onClick = { viewModel.setGeminiApiKeyOverride(apiKeyInput.trim()) }) { Text(stringResource(R.string.settings_save_key)) }
                }
            }

            // ---- Notifications ----
            SettingsSection(stringResource(R.string.settings_section_notifications)) {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_daily_reminder),
                    subtitle = stringResource(R.string.settings_daily_reminder_subtitle),
                    checked = prefs.dailyReminderEnabled,
                    onCheckedChange = viewModel::setDailyReminderEnabled,
                )
                Spacer(Modifier.height(4.dp))
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_budget_alerts),
                    subtitle = stringResource(R.string.settings_budget_alerts_subtitle),
                    checked = prefs.budgetAlertsEnabled,
                    onCheckedChange = viewModel::setBudgetAlertsEnabled,
                )
            }

            // ---- Data ----
            SettingsSection(stringResource(R.string.settings_section_data)) {
                Button(
                    onClick = {
                        val ts = LocalDateTime.now().toString().take(16).replace(":", "-")
                        exportLauncher.launch("expense_tracker_backup_$ts.json")
                    },
                    enabled = !actionState.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (actionState.isExporting) stringResource(R.string.settings_exporting) else stringResource(R.string.settings_export_json)) }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    enabled = !actionState.isImporting,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (actionState.isImporting) stringResource(R.string.settings_importing) else stringResource(R.string.settings_import_json)) }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = viewModel::requestClearData,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.settings_clear_data)) }
            }

            // ---- About ----
            SettingsSection(stringResource(R.string.settings_section_about)) {
                SettingsInfoRow(stringResource(R.string.settings_app_version), BuildConfig.VERSION_NAME)
                SettingsInfoRow(stringResource(R.string.settings_build), BuildConfig.VERSION_CODE.toString())
                SettingsInfoRow(stringResource(R.string.settings_package), BuildConfig.APPLICATION_ID)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
