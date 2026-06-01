package com.expensetracker.app.feature.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.ParsedTransaction
import com.expensetracker.app.domain.repository.AiRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickAddUiState(
    val nlInput: String = "",
    val isLoading: Boolean = false,
    val parsedResult: ParsedTransaction? = null,
    val parsedCategoryName: String? = null,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
)

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuickAddUiState())
    val state: StateFlow<QuickAddUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cats = categoryRepository.observeAll().first()
            _state.update { it.copy(categories = cats) }
        }
    }

    fun onInputChanged(text: String) {
        _state.update { it.copy(nlInput = text, parsedResult = null, error = null) }
    }

    fun parseInput() {
        val text = _state.value.nlInput.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            aiRepository.parseNaturalLanguageTransaction(text, _state.value.categories)
                .fold(
                    onSuccess = { parsed ->
                        val catName = _state.value.categories.find { it.id == parsed.categoryId }?.name
                        _state.update {
                            it.copy(isLoading = false, parsedResult = parsed, parsedCategoryName = catName)
                        }
                    },
                    onFailure = { e ->
                        _state.update {
                            it.copy(isLoading = false, error = e.message ?: "Could not parse. Try being more specific.")
                        }
                    },
                )
        }
    }

    fun reset() {
        _state.update { it.copy(nlInput = "", parsedResult = null, parsedCategoryName = null, error = null) }
    }
}
