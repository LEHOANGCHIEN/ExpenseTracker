package com.expensetracker.app.data.mapper

import com.expensetracker.app.data.local.entity.AiChatMessageEntity
import com.expensetracker.app.domain.model.AiChatMessage

fun AiChatMessageEntity.toDomain() = AiChatMessage(
    id = id,
    role = role,
    content = content,
    timestamp = timestamp,
    sessionId = sessionId,
)

fun AiChatMessage.toEntity() = AiChatMessageEntity(
    id = id,
    role = role,
    content = content,
    timestamp = timestamp,
    sessionId = sessionId,
)
