package com.expensetracker.app.domain.model

import java.time.LocalDateTime

data class AiChatMessage(
    val id: Long = 0,
    val role: ChatRole,
    val content: String,
    val timestamp: LocalDateTime,
    val sessionId: String,
)
