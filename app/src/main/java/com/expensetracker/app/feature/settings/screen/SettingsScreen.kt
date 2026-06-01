package com.expensetracker.app.feature.settings.screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.BuildConfig
import com.expensetracker.app.core.designsystem.theme.ThemeMode
import com.expensetracker.app.feature.settings.SettingsViewModel
import java.time.LocalDateTime

private val CURRENCIES = listOf("VND", "USD", "EUR", "GBP", "JPY", "SGD", "THB")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToGeminiTest: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportData(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.onImportSelected(it) } }

    LaunchedEffect(actionState.error, actionState.exportSuccess, actionState.importSuccess) {
        when {
            actionState.exportSuccess -> { snackbarHostState.showSnackbar("Data exported successfully"); viewModel.clearError() }
            actionState.importSuccess -> { snackbarHostState.showSnackbar("Data imported successfully"); viewModel.clearError() }
            actionState.error != null -> { snackbarHostState.showSnackbar(actionState.error!!); viewModel.clearError() }
        }
    }

    // Import confirm dialog
    if (actionState.showImportConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissImportConfirm,
            title = { Text("Import data?") },
            text = { Text("This will add imported wallets and categories. Existing data is kept.") },
            confirmButton = { TextButton(onClick = viewModel::confirmImport) { Text("Import") } },
            dismissButton = { TextButton(onClick = viewModel::dismissImportConfirm) { Text("Cancel") } },
        )
    }

    // Clear all confirm dialog
    if (actionState.showClearConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearConfirm,
            title = { Text("Clear all data?") },
            text = { Text("This will permanently delete all transactions, wallets, categories, and budgets. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::clearAllData,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete everything") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissClearConfirm) { Text("Cancel") } },
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ---- Preferences ----
            SettingsSection("Preferences") {
                var currencyExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it },
                ) {
                    OutlinedTextField(
                        value = prefs.currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
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
                ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = it }) {
                    OutlinedTextField(
                        value = "Day ${prefs.monthStartDay}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Month starts on") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dayExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                        (1..28).forEach { day ->
                            DropdownMenuItem(text = { Text("Day $day") }, onClick = { viewModel.setMonthStartDay(day); dayExpanded = false })
                        }
                    }
                }
            }

            // ---- Appearance ----
            SettingsSection("Appearance") {
                Text("Theme", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    title = "Dynamic colors",
                    subtitle = "Use wallpaper colors (Android 12+)",
                    checked = prefs.dynamicColorEnabled,
                    onCheckedChange = viewModel::setDynamicColor,
                )
            }

            // ---- AI ----
            SettingsSection("AI Features") {
                SettingsSwitchRow(
                    title = "Enable AI features",
                    subtitle = "Requires a Gemini API key",
                    checked = prefs.geminiEnabled,
                    onCheckedChange = viewModel::setGeminiEnabled,
                )
                Spacer(Modifier.height(8.dp))
                var apiKeyInput by remember(prefs.geminiApiKeyOverride) { mutableStateOf(prefs.geminiApiKeyOverride) }
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Custom Gemini API key") },
                    placeholder = { Text("Overrides build config key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Leave blank to use the default key") },
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onNavigateToGeminiTest) { Text("Test Gemini ✨") }
                    Button(onClick = { viewModel.setGeminiApiKeyOverride(apiKeyInput.trim()) }) { Text("Save Key") }
                }
            }

            // ---- Notifications ----
            SettingsSection("Notifications") {
                SettingsSwitchRow(
                    title = "Daily reminder",
                    subtitle = "Remind me to log expenses at 8 PM",
                    checked = prefs.dailyReminderEnabled,
                    onCheckedChange = viewModel::setDailyReminderEnabled,
                )
                Spacer(Modifier.height(4.dp))
                SettingsSwitchRow(
                    title = "Budget alerts",
                    subtitle = "Notify when approaching budget limits",
                    checked = prefs.budgetAlertsEnabled,
                    onCheckedChange = viewModel::setBudgetAlertsEnabled,
                )
            }

            // ---- Data ----
            SettingsSection("Data") {
                Button(
                    onClick = {
                        val ts = LocalDateTime.now().toString().take(16).replace(":", "-")
                        exportLauncher.launch("expense_tracker_backup_$ts.json")
                    },
                    enabled = !actionState.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (actionState.isExporting) "Exporting…" else "Export to JSON") }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    enabled = !actionState.isImporting,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (actionState.isImporting) "Importing…" else "Import from JSON") }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = viewModel::requestClearData,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Clear all data") }
            }

            // ---- About ----
            SettingsSection("About") {
                SettingsInfoRow("App version", BuildConfig.VERSION_NAME)
                SettingsInfoRow("Build", BuildConfig.VERSION_CODE.toString())
                SettingsInfoRow("Package", BuildConfig.APPLICATION_ID)
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
