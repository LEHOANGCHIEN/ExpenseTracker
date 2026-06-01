package com.expensetracker.app.feature.ai_assistant

import com.expensetracker.app.domain.model.AiChatMessage

data class AiAssistantUiState(
    val messages: List<AiChatMessage> = emptyList(),
    val isResponding: Boolean = false,
    val inputText: String = "",
    val currentSessionId: String = "",
    val error: String? = null,
)
