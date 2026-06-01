package com.expensetracker.app.feature.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val transactionId: Long? = savedStateHandle.get<Long?>("id")

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
                it.copy(type = event.type, categoryId = null)
            }
            is AddEditUiEvent.CategorySelected -> _state.update {
                it.copy(categoryId = event.categoryId, showCategoryPicker = false)
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
            is AddEditUiEvent.NoteChanged -> _state.update {
                it.copy(note = event.note.take(200))
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
