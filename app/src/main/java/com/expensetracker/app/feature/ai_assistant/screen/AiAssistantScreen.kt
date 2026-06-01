package com.expensetracker.app.feature.ai_assistant.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.feature.ai_assistant.AiAssistantViewModel
import com.expensetracker.app.feature.ai_assistant.screen.component.ChatBubble
import com.expensetracker.app.feature.ai_assistant.screen.component.ChatInput
import com.expensetracker.app.feature.ai_assistant.screen.component.SuggestedQuestionChips
import com.expensetracker.app.feature.ai_assistant.screen.component.TypingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: AiAssistantViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearDialog by remember { mutableStateOf(false) }

    // Scroll to top (reverseLayout means newest = index 0)
    LaunchedEffect(state.messages.size, state.isResponding) {
        if (state.messages.isNotEmpty() || state.isResponding) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onClearError()
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear history?") },
            text = { Text("All messages in this session will be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant ✨") },
                actions = {
                    IconButton(onClick = viewModel::newChat) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = "New chat",
                        )
                    }
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear history",
                        )
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
                .imePadding(),
        ) {
            // Message list — weight(1f) so input stays pinned at bottom
            Box(modifier = Modifier.weight(1f)) {
                if (state.messages.isEmpty() && !state.isResponding) {
                    EmptyState(
                        questions = state.suggestedQuestions,
                        onQuestionSelected = viewModel::onSendSuggestedQuestion,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true,
                    ) {
                        if (state.isResponding) {
                            item(key = "typing") {
                                TypingIndicator()
                            }
                        }
                        items(
                            items = state.messages.reversed(),
                            key = { "${it.id}_${it.timestamp}" },
                        ) { message ->
                            ChatBubble(message = message)
                        }
                    }
                }
            }

            // Quick-reply chips when conversation is active
            if (state.messages.isNotEmpty() && !state.isResponding) {
                HorizontalDivider()
                SuggestedQuestionChips(
                    questions = state.suggestedQuestions,
                    onQuestionSelected = viewModel::onSendSuggestedQuestion,
                )
            }

            HorizontalDivider()
            ChatInput(
                value = state.inputText,
                onValueChange = viewModel::onInputChanged,
                onSend = viewModel::onSendMessage,
                isLoading = state.isResponding,
            )
        }
    }
}

@Composable
private fun EmptyState(
    questions: List<String>,
    onQuestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.then(Modifier.padding(bottom = 16.dp)),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "AI Financial Assistant",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Ask me anything about your finances.\nI use your actual data to give you personalized advice.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        SuggestedQuestionChips(
            questions = questions,
            onQuestionSelected = onQuestionSelected,
        )
    }
}
