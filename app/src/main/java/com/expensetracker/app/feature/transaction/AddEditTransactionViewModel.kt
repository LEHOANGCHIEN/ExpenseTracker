package com.expensetracker.app.feature.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.AiInsight
import com.expensetracker.app.domain.model.InsightType
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.AiRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val preferencesRepository: PreferencesRepository,
    private val aiRepository: AiRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var categorizationJob: Job? = null
    private val categorizationCache = HashMap<String, Long?>()

    private val transactionId: Long? = savedStateHandle.get<Long?>("id")
    private val prefillAmount: Double? = savedStateHandle.get<Double?>("prefillAmount")
    private val prefillNote: String? = savedStateHandle.get<String?>("prefillNote")
    private val prefillCategoryId: Long? = savedStateHandle.get<Long?>("prefillCategoryId")

    private val _state = MutableStateFlow(AddEditTransactionUiState())
    val state: StateFlow<AddEditTransactionUiState> = _state

    init {
        viewModelScope.launch {
            combine(
                categoryRepository.observeAll(),
                walletRepository.observeAll(),
                preferencesRepository.preferences,
            ) { cats, wallets, prefs ->
                Triple(cats, wallets, prefs)
            }.first().let { (cats, wallets, prefs) ->
                val defaultWalletId = if (prefs.defaultWalletId > 0) prefs.defaultWalletId
                    else wallets.firstOrNull()?.id

                if (transactionId != null) {
                    val existing = transactionRepository.getById(transactionId)
                    if (existing != null) {
                        _state.update {
                            it.copy(
                                transactionId = existing.id,
                                amountExpression = formatAmount(existing.amount),
                                resolvedAmount = existing.amount,
                                type = existing.type,
                                categoryId = existing.categoryId,
                                walletId = existing.walletId,
                                date = existing.date,
                                note = existing.note,
                                photoUri = existing.photoUri,
                                tags = existing.tags,
                                recurringId = existing.recurringId,
                                parentSplitId = existing.parentSplitId,
                                isRecurringChild = existing.recurringId != null,
                                isEditing = true,
                                categories = cats,
                                wallets = wallets,
                                currency = prefs.currency,
                                isLoading = false,
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            categories = cats,
                            wallets = wallets,
                            walletId = defaultWalletId,
                            currency = prefs.currency,
                            isLoading = false,
                            amountExpression = prefillAmount?.let { amt -> formatAmount(amt) } ?: "0",
                            resolvedAmount = prefillAmount ?: 0.0,
                            note = prefillNote ?: "",
                            categoryId = prefillCategoryId,
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditUiEvent) {
        when (event) {
            is AddEditUiEvent.AmountKeyPressed -> handleAmountKey(event.key)
            is AddEditUiEvent.TypeChanged -> _state.update {
                it.copy(type = event.type, categoryId = null, aiCategorySuggestion = null)
            }
            is AddEditUiEvent.CategorySelected -> _state.update {
                it.copy(categoryId = event.categoryId, showCategoryPicker = false, aiCategorySuggestion = null)
            }
            is AddEditUiEvent.WalletSelected -> _state.update {
                it.copy(walletId = event.walletId)
            }
            is AddEditUiEvent.ToWalletSelected -> _state.update {
                it.copy(toWalletId = event.walletId)
            }
            is AddEditUiEvent.DateChanged -> _state.update {
                it.copy(date = event.date, showDatePicker = false)
            }
            is AddEditUiEvent.NoteChanged -> {
                _state.update { it.copy(note = event.note.take(200)) }
                scheduleCategorizationDebounce()
            }
            is AddEditUiEvent.PhotoSelected -> _state.update {
                it.copy(photoUri = event.uri)
            }
            is AddEditUiEvent.TagInputChanged -> _state.update {
                it.copy(tagInput = event.input)
            }
            AddEditUiEvent.AddTag -> {
                val tag = _state.value.tagInput.trim()
                if (tag.isNotEmpty() && tag !in _state.value.tags) {
                    _state.update { it.copy(tags = it.tags + tag, tagInput = "") }
                }
            }
            is AddEditUiEvent.RemoveTag -> _state.update {
                it.copy(tags = it.tags - event.tag)
            }
            AddEditUiEvent.Save -> save()
            AddEditUiEvent.DeleteRequested -> _state.update { it.copy(showDeleteConfirm = true) }
            AddEditUiEvent.DeleteConfirmed -> delete()
            AddEditUiEvent.DeleteDismissed -> _state.update { it.copy(showDeleteConfirm = false) }
            AddEditUiEvent.ShowCategoryPicker -> _state.update { it.copy(showCategoryPicker = true) }
            AddEditUiEvent.HideCategoryPicker -> _state.update { it.copy(showCategoryPicker = false) }
            AddEditUiEvent.ShowDatePicker -> _state.update { it.copy(showDatePicker = true) }
            AddEditUiEvent.HideDatePicker -> _state.update { it.copy(showDatePicker = false) }
            AddEditUiEvent.ShowSplitDialog -> _state.update { it.copy(showSplitDialog = true) }
            AddEditUiEvent.HideSplitDialog -> _state.update { it.copy(showSplitDialog = false) }
            is AddEditUiEvent.SplitConfirmed -> _state.update {
                it.copy(splitParts = event.parts, isSplitEnabled = true, showSplitDialog = false)
            }
            AddEditUiEvent.ToggleAdvanced -> _state.update { it.copy(showAdvanced = !it.showAdvanced) }
        }
    }

    private fun handleAmountKey(key: String) {
        val expr = _state.value.amountExpression
        val newExpr = when (key) {
            "AC" -> "0"
            "⌫" -> if (expr.length <= 1) "0" else expr.dropLast(1)
            in listOf("+", "-", "×", "÷") -> {
                val lastChar = expr.lastOrNull()
                when {
                    lastChar != null && lastChar in listOf('+', '-', '×', '÷') -> expr.dropLast(1) + key
                    expr == "0" -> expr
                    else -> expr + key
                }
            }
            "." -> {
                val lastOpIdx = expr.indexOfLast { it in listOf('+', '-', '×', '÷') }
                val currentNum = if (lastOpIdx == -1) expr else expr.substring(lastOpIdx + 1)
                when {
                    '.' in currentNum -> expr
                    currentNum.isEmpty() -> "${expr}0."
                    else -> "$expr."
                }
            }
            "00" -> {
                if (expr == "0") expr else expr + "00"
            }
            else -> {
                if (expr == "0") key else expr + key
            }
        }
        val resolved = evaluateExpression(newExpr)
        _state.update { it.copy(amountExpression = newExpr, resolvedAmount = resolved) }
    }

    private fun save() {
        val s = _state.value
        val amount = s.resolvedAmount
        val categoryId = s.categoryId ?: return
        val walletId = s.walletId ?: return

        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val now = LocalDateTime.now()
            val transaction = Transaction(
                id = s.transactionId ?: 0L,
                walletId = walletId,
                categoryId = categoryId,
                amount = amount,
                type = s.type,
                note = s.note,
                date = s.date,
                createdAt = now,
                updatedAt = now,
                photoUri = s.photoUri,
                location = null,
                recurringId = s.recurringId,
                parentSplitId = s.parentSplitId,
                tags = s.tags,
                toWalletId = if (s.type == TransactionType.TRANSFER) s.toWalletId else null,
            )
            try {
                if (s.isEditing) {
                    transactionRepository.update(transaction)
                    // Handle split parts if enabled
                    if (s.isSplitEnabled && s.splitParts.isNotEmpty()) {
                        val children = s.splitParts.mapIndexed { idx, part ->
                            Transaction(
                                id = 0L,
                                walletId = walletId,
                                categoryId = part.categoryId ?: categoryId,
                                amount = part.amount,
                                type = s.type,
                                note = part.note,
                                date = s.date,
                                createdAt = now,
                                updatedAt = now,
                                photoUri = null,
                                location = null,
                                recurringId = null,
                                parentSplitId = s.transactionId,
                                tags = emptyList(),
                            )
                        }
                        transactionRepository.split(s.transactionId!!, children)
                    }
                    _state.update { it.copy(isSaving = false, isDone = true) }
                } else {
                    val newId = transactionRepository.add(transaction)
                    if (s.isSplitEnabled && s.splitParts.isNotEmpty()) {
                        val children = s.splitParts.map { part ->
                            Transaction(
                                id = 0L,
                                walletId = walletId,
                                categoryId = part.categoryId ?: categoryId,
                                amount = part.amount,
                                type = s.type,
                                note = part.note,
                                date = s.date,
                                createdAt = now,
                                updatedAt = now,
                                photoUri = null,
                                location = null,
                                recurringId = null,
                                parentSplitId = newId,
                                tags = emptyList(),
                            )
                        }
                        transactionRepository.split(newId, children)
                    }
                    // Anomaly detection for new expense transactions
                    if (transaction.type == TransactionType.EXPENSE) {
                        checkForAnomaly(transaction.copy(id = newId))
                    }
                    _state.update { it.copy(isSaving = false, isDone = true) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private fun delete() {
        val id = _state.value.transactionId ?: return
        viewModelScope.launch {
            try {
                transactionRepository.delete(id)
                _state.update { it.copy(showDeleteConfirm = false, isDone = true) }
            } catch (e: Exception) {
                _state.update { it.copy(showDeleteConfirm = false, error = e.message) }
            }
        }
    }

    // ---- Feature A: Smart Auto-Categorization ----

    private fun scheduleCategorizationDebounce() {
        val note = _state.value.note
        val amount = _state.value.resolvedAmount
        // Only suggest when there's no manual category and enough note text
        if (note.length < 3 || _state.value.categoryId != null) {
            _state.update { it.copy(aiCategorySuggestion = null) }
            return
        }
        val cacheKey = "$note|${amount.toLong()}"
        if (cacheKey in categorizationCache) {
            _state.update { it.copy(aiCategorySuggestion = categorizationCache[cacheKey]) }
            return
        }
        categorizationJob?.cancel()
        categorizationJob = viewModelScope.launch {
            delay(600)
            val cats = _state.value.filteredCategories
            if (cats.isEmpty()) return@launch
            aiRepository.categorizeTransaction(note, amount, cats)
                .getOrNull()
                ?.let { suggestedId ->
                    categorizationCache[cacheKey] = suggestedId
                    if (_state.value.categoryId == null) {
                        _state.update { it.copy(aiCategorySuggestion = suggestedId) }
                    }
                }
        }
    }

    // ---- Feature D: Anomaly Detection ----

    private fun checkForAnomaly(savedTransaction: Transaction) {
        viewModelScope.launch {
            runCatching {
                val ninetyDaysAgo = LocalDate.now().minusDays(90)
                val pastTxns = transactionRepository
                    .observeByDateRange(ninetyDaysAgo, LocalDate.now())
                    .first()
                    .filter {
                        it.type == TransactionType.EXPENSE &&
                            it.categoryId == savedTransaction.categoryId &&
                            it.id != savedTransaction.id &&
                            it.parentSplitId == null
                    }

                if (pastTxns.size < 3) return@runCatching

                val amounts = pastTxns.map { it.amount }
                val mean = amounts.average()
                val variance = amounts.sumOf { (it - mean) * (it - mean) } / amounts.size
                val stdDev = sqrt(variance)
                if (stdDev == 0.0) return@runCatching

                val zScore = (savedTransaction.amount - mean) / stdDev
                val minThreshold = 10_000.0 // Only flag if amount is meaningful

                if (zScore > 2.5 && savedTransaction.amount > minThreshold) {
                    val cat = _state.value.categories.find { it.id == savedTransaction.categoryId }
                    val catName = cat?.name ?: "this category"
                    val insight = AiInsight(
                        id = 0,
                        type = InsightType.ANOMALY,
                        title = "Unusual ${catName} spend",
                        content = "This ${catName} transaction (${savedTransaction.amount.toLong()}) is ${String.format("%.1f", zScore)}x above your usual amount. Was there a special occasion?",
                        periodKey = LocalDate.now().toString(),
                        generatedAt = LocalDateTime.now(),
                        dismissed = false,
                    )
                    aiRepository.saveInsights(listOf(insight))
                }
            }
        }
    }

    companion object {
        fun evaluateExpression(expr: String): Double {
            if (expr.isEmpty() || expr == "0") return 0.0
            val cleaned = expr.trimEnd('+', '-', '×', '÷')
            if (cleaned.isEmpty()) return 0.0

            val tokens = mutableListOf<String>()
            val current = StringBuilder()
            for (c in cleaned) {
                if (c in listOf('+', '-', '×', '÷')) {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                    tokens.add(c.toString())
                } else {
                    current.append(c)
                }
            }
            if (current.isNotEmpty()) tokens.add(current.toString())

            if (tokens.isEmpty()) return 0.0
            var result = tokens[0].toDoubleOrNull() ?: return 0.0
            var i = 1
            while (i + 1 < tokens.size) {
                val op = tokens[i]
                val right = tokens[i + 1].toDoubleOrNull() ?: break
                result = when (op) {
                    "+" -> result + right
                    "-" -> result - right
                    "×" -> result * right
                    "÷" -> if (right != 0.0) result / right else result
                    else -> result
                }
                i += 2
            }
            return result
        }

        private fun formatAmount(amount: Double): String {
            return if (amount == amount.toLong().toDouble()) {
                amount.toLong().toString()
            } else {
                amount.toString()
            }
        }
    }
}
