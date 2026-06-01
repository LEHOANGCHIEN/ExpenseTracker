package com.expensetracker.app.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.feature.settings.GeminiTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiTestScreen(
    onNavigateBack: () -> Unit,
    viewModel: GeminiTestViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemini API Test") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.response.isNotBlank() || state.error != null) {
                        TextButton(onClick = viewModel::clearResponse) {
                            Text("Clear")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // API key status
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.apiKeyAvailable)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Text(
                    text = if (state.apiKeyAvailable)
                        "✓ API key configured"
                    else
                        "✗ API key not configured. Add GEMINI_API_KEY=... to local.properties.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.apiKeyAvailable)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            // Prompt input
            OutlinedTextField(
                value = state.prompt,
                onValueChange = viewModel::onPromptChanged,
                label = { Text("Prompt") },
                placeholder = { Text("Ask Gemini anything…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                trailingIcon = if (state.prompt.isNotBlank()) {
                    {
                        IconButton(onClick = { viewModel.onPromptChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear prompt")
                        }
                    }
                } else null,
            )

            // Send button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = viewModel::sendPrompt,
                    enabled = state.prompt.isNotBlank() && !state.isLoading && state.apiKeyAvailable,
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Text(text = "  Send")
                    }
                }
            }

            // Error
            if (state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        text = "Error: ${state.error}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            // Response
            if (state.response.isNotBlank()) {
                Text(
                    text = "Response",
                    style = MaterialTheme.typography.titleSmall,
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = state.response,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
