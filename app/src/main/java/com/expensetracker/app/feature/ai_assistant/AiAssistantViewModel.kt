package com.expensetracker.app.feature.ai_assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.AiChatMessage
import com.expensetracker.app.domain.model.ChatRole
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.AiRepository
import com.expensetracker.app.domain.repository.BudgetRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
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
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val sessionId = UUID.randomUUID().toString()

    private val _state = MutableStateFlow(AiAssistantUiState(currentSessionId = sessionId))
    val state: StateFlow<AiAssistantUiState> = _state

    fun onInputChanged(text: String) {
        _state.update { it.copy(inputText = text, error = null) }
    }

    fun onSendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isResponding) return

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
            )
        }

        viewModelScope.launch {
            // Save user message
            aiRepository.saveChatMessage(userMessage)

            // Build context
            val userContext = buildUserContext()

            // Send to AI
            val allMessages = _state.value.messages
            val result = aiRepository.sendChatMessage(allMessages, userContext)

            result.fold(
                onSuccess = { responseText ->
                    val assistantMessage = AiChatMessage(
                        id = 0,
                        role = ChatRole.ASSISTANT,
                        content = responseText,
                        timestamp = LocalDateTime.now(),
                        sessionId = sessionId,
                    )
                    aiRepository.saveChatMessage(assistantMessage)
                    _state.update {
                        it.copy(
                            messages = it.messages + assistantMessage,
                            isResponding = false,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isResponding = false,
                            error = error.message ?: "Failed to get response",
                        )
                    }
                },
            )
        }
    }

    private suspend fun buildUserContext(): String {
        return try {
            val prefs = preferencesRepository.preferences.first()
            val today = LocalDate.now()
            val startOfMonth = today.withDayOfMonth(1)

            val transactions = transactionRepository
                .observeByDateRange(startOfMonth, today)
                .first()

            val categories = categoryRepository.observeAll().first()
            val catMap = categories.associateBy { it.id }

            val totalExpense = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            val totalIncome = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }

            val topCategories = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryId }
                .entries
                .sortedByDescending { (_, v) -> v.sumOf { it.amount } }
                .take(5)
                .mapNotNull { (catId, txns) ->
                    val cat = catMap[catId] ?: return@mapNotNull null
                    "${cat.name}: ${txns.sumOf { it.amount }}"
                }
                .joinToString(", ")

            """
            Currency: ${prefs.currency}
            This month (${startOfMonth} to ${today}):
            - Total income: $totalIncome
            - Total expenses: $totalExpense
            - Net: ${totalIncome - totalExpense}
            - Top spending categories: $topCategories
            - Total transactions: ${transactions.size}
            """.trimIndent()
        } catch (e: Exception) {
            ""
        }
    }

    fun onClearError() {
        _state.update { it.copy(error = null) }
    }
}
