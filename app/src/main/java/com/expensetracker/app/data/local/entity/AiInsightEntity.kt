package com.expensetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensetracker.app.domain.model.InsightType
import java.time.LocalDateTime

@Entity(
    tableName = "ai_insights",
    indices = [Index("userId")],
)
data class AiInsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",
    val type: InsightType,
    val title: String,
    val content: String,
    val periodKey: String,
    val generatedAt: LocalDateTime,
    val dismissed: Boolean,
)
