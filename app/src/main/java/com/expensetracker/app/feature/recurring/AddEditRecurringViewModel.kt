package com.expensetracker.app.feature.recurring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.RecurringTransaction
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.RecurringTransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddEditRecurringViewModel @Inject constructor(
    private val recurringRepo: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val scheduleId: Long? = savedStateHandle.get<Long?>("id")

    private val _state = MutableStateFlow(AddEditRecurringUiState())
    val state: StateFlow<AddEditRecurringUiState> = _state

    init {
        viewModelScope.launch {
            val cats = categoryRepository.observeAll().first()
            val wallets = walletRepository.observeAll().first()
            val prefs = preferencesRepository.preferences.first()
            val defaultWalletId = if (prefs.defaultWalletId > 0) prefs.defaultWalletId
                else wallets.firstOrNull()?.id

            if (scheduleId != null) {
                val existing = recurringRepo.getById(scheduleId)
                if (existing != null) {
                    _state.update {
                        it.copy(
                            scheduleId = existing.id,
                            walletId = existing.walletId,
                            categoryId = existing.categoryId,
                            amountText = formatAmount(existing.amount),
                            type = existing.type,
                            note = existing.note,
                            frequency = existing.frequency,
                            interval = existing.interval,
                            startDate = existing.startDate,
                            endDate = existing.endDate,
                            categories = cats,
                            wallets = wallets,
                            isEditing = true,
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        categories = cats,
                        wallets = wallets,
                        walletId = defaultWalletId,
                    )
                }
            }
        }
    }

    fun onEvent(event: AddEditRecurringEvent) {
        when (event) {
            is AddEditRecurringEvent.AmountChanged -> _state.update { it.copy(amountText = event.text) }
            is AddEditRecurringEvent.TypeChanged -> _state.update { it.copy(type = event.type, categoryId = null) }
            is AddEditRecurringEvent.CategorySelected -> _state.update {
                it.copy(categoryId = event.id, showCategoryPicker = false)
            }
            is AddEditRecurringEvent.WalletSelected -> _state.update { it.copy(walletId = event.id) }
            is AddEditRecurringEvent.NoteChanged -> _state.update { it.copy(note = event.note.take(200)) }
            is AddEditRecurringEvent.FrequencyChanged -> _state.update { it.copy(frequency = event.freq) }
            is AddEditRecurringEvent.IntervalChanged -> _state.update {
                it.copy(interval = event.interval.coerceIn(1, 99))
            }
            is AddEditRecurringEvent.StartDateChanged -> _state.update {
                it.copy(startDate = event.date, showStartDatePicker = false)
            }
            is AddEditRecurringEvent.EndDateChanged -> _state.update {
                it.copy(endDate = event.date, showEndDatePicker = false)
            }
            AddEditRecurringEvent.ShowCategoryPicker -> _state.update { it.copy(showCategoryPicker = true) }
            AddEditRecurringEvent.HideCategoryPicker -> _state.update { it.copy(showCategoryPicker = false) }
            AddEditRecurringEvent.ShowStartDatePicker -> _state.update { it.copy(showStartDatePicker = true) }
            AddEditRecurringEvent.HideStartDatePicker -> _state.update { it.copy(showStartDatePicker = false) }
            AddEditRecurringEvent.ShowEndDatePicker -> _state.update { it.copy(showEndDatePicker = true) }
            AddEditRecurringEvent.HideEndDatePicker -> _state.update { it.copy(showEndDatePicker = false) }
            AddEditRecurringEvent.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value
        val categoryId = s.categoryId ?: return
        val walletId = s.walletId ?: return
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val schedule = RecurringTransaction(
                    id = s.scheduleId ?: 0L,
                    walletId = walletId,
                    categoryId = categoryId,
                    amount = s.amount,
                    type = s.type,
                    note = s.note,
                    frequency = s.frequency,
                    interval = s.interval,
                    startDate = s.startDate,
                    endDate = s.endDate,
                    nextOccurrence = s.startDate,
                    lastProcessed = null,
                    isActive = true,
                )
                if (s.isEditing) recurringRepo.update(schedule)
                else recurringRepo.add(schedule)
                _state.update { it.copy(isSaving = false, isDone = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private fun formatAmount(amount: Double): String =
        if (amount == amount.toLong().toDouble()) amount.toLong().toString()
        else amount.toString()
}
