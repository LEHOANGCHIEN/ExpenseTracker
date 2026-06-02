package com.expensetracker.app.feature.ai_assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.AiChatMessage
import com.expensetracker.app.domain.model.ChatRole
import com.expensetracker.app.domain.model.RecurrenceFrequency
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.AiRepository
import com.expensetracker.app.domain.repository.BudgetRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.RecurringTransactionRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringRepository: RecurringTransactionRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AiAssistantUiState())
    val state: StateFlow<AiAssistantUiState> = _state

    init {
        viewModelScope.launch {
            // Restore the most recent session or start a new one
            val sessionId = aiRepository.getLatestSessionId() ?: UUID.randomUUID().toString()
            val history = aiRepository.observeChatMessages(sessionId).first()
            _state.update {
                it.copy(
                    currentSessionId = sessionId,
                    messages = history,
                )
            }
        }
    }

    fun onInputChanged(text: String) {
        _state.update { it.copy(inputText = text, error = null) }
    }

    fun onSendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isResponding) return

        val sessionId = _state.value.currentSessionId.ifBlank { UUID.randomUUID().toString() }

        val userMessage = AiChatMessage(
            id = 0,
            role = ChatRole.USER,
            content = text,
            timestamp = LocalDateTime.now(),
            sessionId = sessionId,
        )

        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isResponding = true,
                error = null,
                currentSessionId = sessionId,
            )
        }

        viewModelScope.launch {
            // Persist user message
            val savedId = aiRepository.saveChatMessage(userMessage)
            val savedUserMessage = userMessage.copy(id = savedId)
            _state.update { s ->
                s.copy(messages = s.messages.dropLast(1) + savedUserMessage)
            }

            // Build context and send
            val userContext = buildUserContext()
            val result = aiRepository.sendChatMessage(_state.value.messages, userContext)

            result.fold(
                onSuccess = { responseText ->
                    val assistantMessage = AiChatMessage(
                        id = 0,
                        role = ChatRole.ASSISTANT,
                        content = responseText,
                        timestamp = LocalDateTime.now(),
                        sessionId = sessionId,
                    )
                    val savedAssistantId = aiRepository.saveChatMessage(assistantMessage)
                    _state.update {
                        it.copy(
                            messages = it.messages + assistantMessage.copy(id = savedAssistantId),
                            isResponding = false,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isResponding = false,
                            error = error.message ?: "Failed to get response. Please try again.",
                        )
                    }
                },
            )
        }
    }

    fun onSendSuggestedQuestion(question: String) {
        _state.update { it.copy(inputText = question) }
        onSendMessage()
    }

    fun newChat() {
        val newSessionId = UUID.randomUUID().toString()
        _state.update {
            it.copy(
                messages = emptyList(),
                currentSessionId = newSessionId,
                inputText = "",
                isResponding = false,
                error = null,
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            val sessionId = _state.value.currentSessionId
            if (sessionId.isNotBlank()) {
                aiRepository.clearSession(sessionId)
            }
            _state.update {
                it.copy(
                    messages = emptyList(),
                    error = null,
                )
            }
        }
    }

    fun onClearError() {
        _state.update { it.copy(error = null) }
    }

    private suspend fun buildUserContext(): String = try {
        val prefs = preferencesRepository.preferences.first()
        val today = LocalDate.now()
        val thisMonthStart = today.withDayOfMonth(1)
        val lastMonthStart = thisMonthStart.minusMonths(1)
        val lastMonthEnd = thisMonthStart.minusDays(1)

        val currentTxns = transactionRepository.observeByDateRange(thisMonthStart, today).first()
        val lastTxns = transactionRepository.observeByDateRange(lastMonthStart, lastMonthEnd).first()
        val categories = categoryRepository.observeAll().first()
        val activeBudgets = budgetRepository.observeActive().first()
        val recurringTxns = recurringRepository.observeActive().first()

        val catMap = categories.associateBy { it.id }

        fun buildMonthSummary(txns: List<com.expensetracker.app.domain.model.Transaction>): String {
            val income = txns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val byCategory = txns
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { catMap[it.categoryId]?.name ?: "Other" }
                .mapValues { (_, v) -> v.sumOf { it.amount } }
                .entries
                .sortedByDescending { it.value }
                .take(5)
                .joinToString(",") { (cat, amt) -> "\"$cat\":$amt" }
            return """{"income":$income,"expense":$expense,"byCategory":{$byCategory}}"""
        }

        val budgetSummary = activeBudgets.joinToString(",") { budget ->
            val catName = catMap[budget.categoryId]?.name ?: "Overall"
            val spent = currentTxns
                .filter { t ->
                    t.type == TransactionType.EXPENSE &&
                        (budget.categoryId == null || t.categoryId == budget.categoryId)
                }
                .sumOf { it.amount }
            val pct = if (budget.amount > 0) (spent / budget.amount * 100).toInt() else 0
            """{"category":"$catName","spent":$spent,"limit":${budget.amount},"percentage":$pct}"""
        }

        val recurringMonthly = recurringTxns
            .filter { it.type == TransactionType.EXPENSE && it.frequency == RecurrenceFrequency.MONTHLY }
            .sumOf { it.amount }

        buildString {
            append("{")
            append("\"currency\":\"${prefs.currency}\",")
            append("\"currentMonth\":${buildMonthSummary(currentTxns)},")
            append("\"lastMonth\":${buildMonthSummary(lastTxns)},")
            append("\"activeBudgets\":[$budgetSummary],")
            append("\"recurringMonthly\":$recurringMonthly")
            append("}")
        }
    } catch (e: Exception) {
        "{}"
    }
}
