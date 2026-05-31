package com.expensetracker.app.data.mapper

import com.expensetracker.app.data.local.entity.AiInsightEntity
import com.expensetracker.app.domain.model.AiInsight

fun AiInsightEntity.toDomain() = AiInsight(
    id = id,
    type = type,
    title = title,
    content = content,
    periodKey = periodKey,
    generatedAt = generatedAt,
    dismissed = dismissed,
)

fun AiInsight.toEntity() = AiInsightEntity(
    id = id,
    type = type,
    title = title,
    content = content,
    periodKey = periodKey,
    generatedAt = generatedAt,
    dismissed = dismissed,
)
