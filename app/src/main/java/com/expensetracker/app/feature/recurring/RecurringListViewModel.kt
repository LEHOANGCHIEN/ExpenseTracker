package com.expensetracker.app.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.domain.repository.RecurringTransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringListViewModel @Inject constructor(
    private val recurringRepo: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _longPressed = MutableStateFlow<RecurringScheduleItem?>(null)

    val uiState: StateFlow<RecurringListUiState> = combine(
        recurringRepo.observeAll(),
        categoryRepository.observeAll(),
        walletRepository.observeAll(),
        preferencesRepository.preferences,
        _longPressed,
    ) { schedules, categories, wallets, prefs, longPressed ->
        val catMap = categories.associateBy { it.id }
        val walletMap = wallets.associateBy { it.id }
        val items = schedules
            .sortedWith(compareByDescending<com.expensetracker.app.domain.model.RecurringTransaction> { it.isActive }.thenBy { it.nextOccurrence })
            .map { s ->
                RecurringScheduleItem(
                    schedule = s,
                    category = catMap[s.categoryId],
                    wallet = walletMap[s.walletId],
                )
            }
        RecurringListUiState(
            schedules = items,
            longPressedItem = longPressed,
            isLoading = false,
            currency = prefs.currency,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecurringListUiState(isLoading = true),
    )

    fun onEvent(event: RecurringListEvent) {
        when (event) {
            is RecurringListEvent.LongPress -> _longPressed.value = event.item
            RecurringListEvent.DismissActionSheet -> _longPressed.value = null
            is RecurringListEvent.ToggleActive -> viewModelScope.launch {
                recurringRepo.getById(event.id)?.let { s ->
                    recurringRepo.update(s.copy(isActive = event.isActive))
                }
                _longPressed.value = null
            }
            is RecurringListEvent.Delete -> viewModelScope.launch {
                recurringRepo.delete(event.id)
                _longPressed.value = null
            }
        }
    }
}
