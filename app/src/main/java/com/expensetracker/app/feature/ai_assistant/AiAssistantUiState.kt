package com.expensetracker.app.feature.ai_assistant

import com.expensetracker.app.domain.model.AiChatMessage

val DEFAULT_SUGGESTED_QUESTIONS = listOf(
    "How can I save more money?",
    "Where did most of my money go this month?",
    "Suggest a realistic food budget",
    "Am I spending too much on entertainment?",
)

val FOLLOW_UP_QUESTIONS = listOf(
    "Tell me more about that",
    "What's my biggest area to improve?",
    "Compare this month vs last month",
    "Give me an action plan",
)

data class AiAssistantUiState(
    val messages: List<AiChatMessage> = emptyList(),
    val isResponding: Boolean = false,
    val inputText: String = "",
    val currentSessionId: String = "",
    val error: String? = null,
    val suggestedQuestions: List<String> = DEFAULT_SUGGESTED_QUESTIONS,
)
