package com.expensetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensetracker.app.domain.model.ChatRole
import java.time.LocalDateTime

@Entity(
    tableName = "ai_chat_messages",
    indices = [Index("userId")],
)
data class AiChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",
    val role: ChatRole,
    val content: String,
    val timestamp: LocalDateTime,
    val sessionId: String,
)
