package com.expensetracker.app.data.repository

import com.expensetracker.app.data.local.dao.AiChatMessageDao
import com.expensetracker.app.data.local.dao.AiInsightDao
import com.expensetracker.app.data.mapper.toDomain
import com.expensetracker.app.data.mapper.toEntity
import com.expensetracker.app.domain.model.AiChatMessage
import com.expensetracker.app.domain.model.AiInsight
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.ParsedTransaction
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val aiChatMessageDao: AiChatMessageDao,
    private val aiInsightDao: AiInsightDao,
) : AiRepository {

    override fun observeChatMessages(sessionId: String): Flow<List<AiChatMessage>> =
        aiChatMessageDao.observeBySession(sessionId).map { list -> list.map { it.toDomain() } }

    override fun observeUndismissedInsights(): Flow<List<AiInsight>> =
        aiInsightDao.observeUndismissed().map { list -> list.map { it.toDomain() } }

    override suspend fun saveChatMessage(message: AiChatMessage): Long =
        aiChatMessageDao.insert(message.toEntity())

    override suspend fun clearSession(sessionId: String) =
        aiChatMessageDao.deleteSession(sessionId)

    override suspend fun dismissInsight(id: Long) =
        aiInsightDao.dismiss(id)

    override suspend fun saveInsights(insights: List<AiInsight>) =
        aiInsightDao.insertAll(insights.map { it.toEntity() })

    // Stubs — wired to GeminiApiService in Task 13
    override suspend fun sendChatMessage(
        history: List<AiChatMessage>,
        userContext: String,
    ): Result<String> = Result.success("AI features coming soon.")

    override suspend fun parseNaturalLanguageTransaction(
        text: String,
        categories: List<Category>,
    ): Result<ParsedTransaction> =
        Result.failure(NotImplementedError("AI features not yet implemented"))

    override suspend fun categorizeTransaction(
        note: String,
        amount: Double,
        categories: List<Category>,
    ): Result<Long?> = Result.success(null)

    override suspend fun generateMonthlyInsights(
        transactions: List<Transaction>,
        categories: List<Category>,
    ): Result<List<AiInsight>> = Result.success(emptyList())
}
