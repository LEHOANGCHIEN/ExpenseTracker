package com.expensetracker.app.domain.model

import java.time.LocalDateTime

data class AiInsight(
    val id: Long = 0,
    val type: InsightType,
    val title: String,
    val content: String,
    val periodKey: String,
    val generatedAt: LocalDateTime,
    val dismissed: Boolean,
)
