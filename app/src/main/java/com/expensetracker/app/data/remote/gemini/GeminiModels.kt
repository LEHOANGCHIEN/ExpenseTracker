package com.expensetracker.app.data.remote.gemini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParsedTransactionDto(
    val amount: Double = 0.0,
    val type: String = "EXPENSE",
    @SerialName("categoryId") val categoryId: Long? = null,
    val note: String = "",
    val date: String = "",
    val confidence: Double = 0.0,
    val error: String? = null,
)

@Serializable
data class CategorizationResultDto(
    @SerialName("categoryId") val categoryId: Long? = null,
    val confidence: Double = 0.0,
)

@Serializable
data class InsightItemDto(
    val type: String = "MONTHLY_SUMMARY",
    val title: String = "",
    val content: String = "",
    val periodKey: String = "",
)

@Serializable
data class InsightsResponseDto(
    val insights: List<InsightItemDto> = emptyList(),
)

@Serializable
data class ReceiptParseResultDto(
    val amount: Double? = null,
    val date: String? = null,
    val merchant: String? = null,
    val currency: String? = null,
)
