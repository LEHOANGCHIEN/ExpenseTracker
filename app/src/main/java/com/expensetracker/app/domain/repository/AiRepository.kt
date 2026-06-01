package com.expensetracker.app.domain.repository

import com.expensetracker.app.domain.model.AiChatMessage
import com.expensetracker.app.domain.model.AiInsight
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.ParsedTransaction
import com.expensetracker.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    fun observeChatMessages(sessionId: String): Flow<List<AiChatMessage>>
    fun observeUndismissedInsights(): Flow<List<AiInsight>>
    suspend fun saveChatMessage(message: AiChatMessage): Long
    suspend fun clearSession(sessionId: String)
    suspend fun getLatestSessionId(): String?
    suspend fun dismissInsight(id: Long)
    suspend fun saveInsights(insights: List<AiInsight>)
    suspend fun sendChatMessage(history: List<AiChatMessage>, userContext: String): Result<String>
    suspend fun parseNaturalLanguageTransaction(text: String, categories: List<Category>): Result<ParsedTransaction>
    suspend fun categorizeTransaction(note: String, amount: Double, categories: List<Category>): Result<Long?>
    suspend fun generateMonthlyInsights(transactions: List<Transaction>, categories: List<Category>): Result<List<AiInsight>>
}
