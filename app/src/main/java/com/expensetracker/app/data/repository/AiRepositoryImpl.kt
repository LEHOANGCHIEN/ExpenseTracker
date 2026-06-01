package com.expensetracker.app.data.repository

import com.expensetracker.app.data.local.dao.AiChatMessageDao
import com.expensetracker.app.data.local.dao.AiInsightDao
import com.expensetracker.app.data.mapper.toDomain
import com.expensetracker.app.data.mapper.toEntity
import com.expensetracker.app.data.remote.gemini.GeminiApiService
import com.expensetracker.app.data.remote.gemini.GeminiPrompts
import com.expensetracker.app.data.remote.gemini.InsightItemDto
import com.expensetracker.app.domain.model.AiChatMessage
import com.expensetracker.app.domain.model.AiInsight
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.ChatRole
import com.expensetracker.app.domain.model.InsightType
import com.expensetracker.app.domain.model.ParsedTransaction
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.AiRepository
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val aiChatMessageDao: AiChatMessageDao,
    private val aiInsightDao: AiInsightDao,
    private val geminiApiService: GeminiApiService,
) : AiRepository {

    private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun observeChatMessages(sessionId: String): Flow<List<AiChatMessage>> =
        aiChatMessageDao.observeBySession(sessionId).map { list -> list.map { it.toDomain() } }

    override fun observeUndismissedInsights(): Flow<List<AiInsight>> =
        aiInsightDao.observeUndismissed().map { list -> list.map { it.toDomain() } }

    override suspend fun saveChatMessage(message: AiChatMessage): Long =
        aiChatMessageDao.insert(message.toEntity())

    override suspend fun clearSession(sessionId: String) =
        aiChatMessageDao.deleteSession(sessionId)

    override suspend fun getLatestSessionId(): String? =
        aiChatMessageDao.getLatestSessionId()

    override suspend fun dismissInsight(id: Long) =
        aiInsightDao.dismiss(id)

    override suspend fun saveInsights(insights: List<AiInsight>) =
        aiInsightDao.insertAll(insights.map { it.toEntity() })

    override suspend fun sendChatMessage(
        history: List<AiChatMessage>,
        userContext: String,
    ): Result<String> {
        val lastUserMsg = history.lastOrNull { it.role == ChatRole.USER }?.content
            ?: return Result.failure(IllegalArgumentException("No user message in history"))

        val historyContents = history.dropLast(1).map { msg ->
            content(role = if (msg.role == ChatRole.USER) "user" else "model") {
                text(msg.content)
            }
        }

        return geminiApiService.chat(
            systemInstruction = GeminiPrompts.buildChatSystemPrompt(userContext),
            history = historyContents,
            userMessage = lastUserMsg,
        )
    }

    override suspend fun parseNaturalLanguageTransaction(
        text: String,
        categories: List<Category>,
    ): Result<ParsedTransaction> {
        val categoriesList = categories.joinToString(", ") { "${it.id}:${it.name}" }
        val prompt = GeminiPrompts.buildNlParsePrompt(
            text = text,
            categoriesList = categoriesList,
            currency = "VND",
        )

        return geminiApiService.generateJson(systemInstruction = prompt).mapCatching { json ->
            val obj = lenientJson.parseToJsonElement(json).jsonObject
            if (obj["error"] != null) {
                throw IllegalStateException(obj["error"]?.jsonPrimitive?.content ?: "Parse error")
            }
            ParsedTransaction(
                amount = obj["amount"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                type = when (obj["type"]?.jsonPrimitive?.content) {
                    "INCOME" -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                },
                categoryId = obj["categoryId"]?.jsonPrimitive?.longOrNull,
                note = obj["note"]?.jsonPrimitive?.content ?: text,
                date = obj["date"]?.jsonPrimitive?.content
                    ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    ?: LocalDate.now(),
            )
        }
    }

    override suspend fun categorizeTransaction(
        note: String,
        amount: Double,
        categories: List<Category>,
    ): Result<Long?> {
        if (note.isBlank()) return Result.success(null)
        val categoriesList = categories.joinToString(", ") { "${it.id}:${it.name}" }
        val prompt = GeminiPrompts.buildCategorizationPrompt(note, amount, categoriesList)

        return geminiApiService.generateJson(systemInstruction = prompt).mapCatching { json ->
            val obj = lenientJson.parseToJsonElement(json).jsonObject
            val confidence = obj["confidence"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            if (confidence >= 0.6) obj["categoryId"]?.jsonPrimitive?.longOrNull else null
        }
    }

    override suspend fun generateMonthlyInsights(
        transactions: List<Transaction>,
        categories: List<Category>,
    ): Result<List<AiInsight>> {
        val month = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        val catMap = categories.associateBy { it.id }

        val byCategory = transactions
            .groupBy { catMap[it.categoryId]?.name ?: "Other" }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val txSummaryJson = buildString {
            append("{")
            append("\"total\":${transactions.sumOf { it.amount }},")
            append("\"count\":${transactions.size},")
            append("\"byCategory\":{")
            byCategory.entries.joinTo(this, ",") { (cat, amt) -> "\"$cat\":$amt" }
            append("}}")
        }

        val prompt = GeminiPrompts.buildMonthlyInsightsPrompt(
            month = month,
            transactionsSummaryJson = txSummaryJson,
            previousMonthJson = "{}",
        )

        return geminiApiService.generateJson(systemInstruction = prompt).mapCatching { json ->
            val raw = json.trim()
            // Response may be a bare array or wrapped in {"insights":[...]}
            val arrayJson = if (raw.startsWith("[")) {
                raw
            } else {
                val obj = lenientJson.parseToJsonElement(raw).jsonObject
                obj["insights"]?.toString() ?: raw
            }
            val dtos = lenientJson.decodeFromString(
                ListSerializer(InsightItemDto.serializer()),
                arrayJson,
            )
            val periodKey = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            dtos.map { dto ->
                AiInsight(
                    id = 0,
                    type = runCatching { InsightType.valueOf(dto.type) }
                        .getOrDefault(InsightType.MONTHLY_SUMMARY),
                    title = dto.title,
                    content = dto.content,
                    periodKey = dto.periodKey.ifBlank { periodKey },
                    generatedAt = LocalDateTime.now(),
                    dismissed = false,
                )
            }
        }
    }
}
